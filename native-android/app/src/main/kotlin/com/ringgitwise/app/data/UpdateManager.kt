package com.ringgitwise.app.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

data class AppUpdateInfo(
    val hasUpdate: Boolean,
    val latestVersion: String,
    val currentVersion: String,
    val releaseTitle: String,
    val releaseNotes: String,
    val publishedAt: String,
    val apkDownloadUrl: String?,
    val apkFileName: String,
    val apkFileSize: Long,
    val htmlUrl: String
)

object UpdateManager {

    const val DEFAULT_REPO = "Kwan-desu/RinggitWise"

    /**
     * Checks GitHub API for the latest release and compares versions.
     */
    suspend fun checkForUpdates(
        context: Context,
        repoSlug: String = DEFAULT_REPO
    ): Result<AppUpdateInfo> = withContext(Dispatchers.IO) {
        try {
            val currentVersion = getCurrentVersion(context)
            val apiUrl = "https://api.github.com/repos/$repoSlug/releases/latest"
            val url = URL(apiUrl)
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 15000
                readTimeout = 15000
                setRequestProperty("Accept", "application/vnd.github.v3+json")
                setRequestProperty("User-Agent", "RinggitWise-App")
            }

            val code = conn.responseCode
            if (code == 404) {
                // No releases published yet
                return@withContext Result.success(
                    AppUpdateInfo(
                        hasUpdate = false,
                        latestVersion = currentVersion,
                        currentVersion = currentVersion,
                        releaseTitle = "Up to date",
                        releaseNotes = "No new releases published on GitHub yet.",
                        publishedAt = "",
                        apkDownloadUrl = null,
                        apkFileName = "",
                        apkFileSize = 0,
                        htmlUrl = "https://github.com/$repoSlug/releases"
                    )
                )
            }

            if (code !in 200..299) {
                val err = conn.errorStream?.bufferedReader()?.readText() ?: "HTTP $code"
                return@withContext Result.failure(Exception("GitHub API Error: $err"))
            }

            val body = conn.inputStream.bufferedReader().readText()
            val json = JSONObject(body)

            val tagName = json.optString("tag_name", "").trim()
            val releaseTitle = json.optString("name", tagName)
            val releaseNotes = json.optString("body", "Bug fixes and improvements.")
            val publishedAt = json.optString("published_at", "")
            val htmlUrl = json.optString("html_url", "https://github.com/$repoSlug/releases")

            // Scan release assets for .apk file
            var apkUrl: String? = null
            var apkName = "RinggitWise-update.apk"
            var apkSize: Long = 0

            val assets = json.optJSONArray("assets")
            if (assets != null) {
                for (i in 0 until assets.length()) {
                    val asset = assets.getJSONObject(i)
                    val name = asset.optString("name", "")
                    if (name.endsWith(".apk", ignoreCase = true)) {
                        apkUrl = asset.optString("browser_download_url", "")
                        apkName = name
                        apkSize = asset.optLong("size", 0)
                        if (name.contains("release", ignoreCase = true)) {
                            // Prefer release over debug apk if both are present
                            break
                        }
                    }
                }
            }

            val isNewer = compareVersions(tagName, currentVersion) > 0

            Result.success(
                AppUpdateInfo(
                    hasUpdate = isNewer,
                    latestVersion = tagName.ifBlank { currentVersion },
                    currentVersion = currentVersion,
                    releaseTitle = releaseTitle,
                    releaseNotes = releaseNotes,
                    publishedAt = publishedAt,
                    apkDownloadUrl = apkUrl,
                    apkFileName = apkName,
                    apkFileSize = apkSize,
                    htmlUrl = htmlUrl
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Downloads APK file with real-time percentage progress callback.
     */
    suspend fun downloadApk(
        context: Context,
        downloadUrl: String,
        fileName: String,
        onProgress: (Float) -> Unit
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            val destDir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.cacheDir, "updates")
            if (!destDir.exists()) destDir.mkdirs()

            val destFile = File(destDir, fileName.ifBlank { "RinggitWise-update.apk" })
            if (destFile.exists()) destFile.delete()

            // Follow redirects (GitHub assets redirect to AWS S3 CDN)
            var currentUrl = downloadUrl
            var connection: HttpURLConnection
            var redirectCount = 0

            while (true) {
                val url = URL(currentUrl)
                connection = (url.openConnection() as HttpURLConnection).apply {
                    connectTimeout = 30000
                    readTimeout = 60000
                    instanceFollowRedirects = true
                    setRequestProperty("User-Agent", "RinggitWise-App")
                }

                val code = connection.responseCode
                if (code == HttpURLConnection.HTTP_MOVED_PERM ||
                    code == HttpURLConnection.HTTP_MOVED_TEMP ||
                    code == HttpURLConnection.HTTP_SEE_OTHER ||
                    code == 307 || code == 308
                ) {
                    currentUrl = connection.getHeaderField("Location")
                    redirectCount++
                    if (redirectCount > 8) throw Exception("Too many redirects")
                    continue
                }
                break
            }

            val totalBytes = connection.contentLengthLong
            val input = connection.inputStream
            val output = FileOutputStream(destFile)

            val buffer = ByteArray(8192)
            var bytesRead: Int
            var totalRead: Long = 0

            while (input.read(buffer).also { bytesRead = it } != -1) {
                output.write(buffer, 0, bytesRead)
                totalRead += bytesRead
                if (totalBytes > 0) {
                    val progress = (totalRead.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f)
                    withContext(Dispatchers.Main) {
                        onProgress(progress)
                    }
                }
            }

            output.flush()
            output.close()
            input.close()

            withContext(Dispatchers.Main) {
                onProgress(1.0f)
            }

            Result.success(destFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Prompts the Android OS package installer to install the downloaded APK.
     */
    fun installApk(context: Context, apkFile: File) {
        if (!apkFile.exists()) return

        // On Android 8.0+ check unknown sources permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!context.packageManager.canRequestPackageInstalls()) {
                val settingsIntent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                    data = Uri.parse("package:${context.packageName}")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(settingsIntent)
                return
            }
        }

        val apkUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            apkFile
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        context.startActivity(intent)
    }

    fun getCurrentVersion(context: Context): String {
        return try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            pInfo.versionName ?: "1.0"
        } catch (_: Exception) {
            "1.0"
        }
    }

    /**
     * Semver comparator: returns > 0 if v1 > v2, 0 if equal, < 0 if v1 < v2.
     * Handles formats like "v1.2.3", "1.2.0", "1.0", "v2.0-beta".
     */
    fun compareVersions(v1: String, v2: String): Int {
        val clean1 = v1.trim().removePrefix("v").removePrefix("V").substringBefore("-")
        val clean2 = v2.trim().removePrefix("v").removePrefix("V").substringBefore("-")

        val parts1 = clean1.split(".").mapNotNull { it.toIntOrNull() }
        val parts2 = clean2.split(".").mapNotNull { it.toIntOrNull() }

        val maxLen = maxOf(parts1.size, parts2.size)
        for (i in 0 until maxLen) {
            val num1 = parts1.getOrElse(i) { 0 }
            val num2 = parts2.getOrElse(i) { 0 }
            if (num1 != num2) {
                return num1.compareTo(num2)
            }
        }
        return 0
    }
}
