package com.ringgitwise.app.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FinanceRepository(private val context: Context) {

    private val prefs = context.getSharedPreferences("ringgitwise_native_prefs", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }

    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()

    private val _monthlyBudget = MutableStateFlow(3500.0)
    val monthlyBudget: StateFlow<Double> = _monthlyBudget.asStateFlow()

    private val _selectedMonth = MutableStateFlow(currentMonthString())
    val selectedMonth: StateFlow<String> = _selectedMonth.asStateFlow()

    // Profile Customizations (Avatar / Emoji / Logo)
    private val _userProfileEmoji = MutableStateFlow(prefs.getString("profile_emoji", "RM") ?: "RM")
    val userProfileEmoji: StateFlow<String> = _userProfileEmoji.asStateFlow()

    private val _avatarUpdateTrigger = MutableStateFlow(System.currentTimeMillis())
    val avatarUpdateTrigger: StateFlow<Long> = _avatarUpdateTrigger.asStateFlow()

    private val _hasCustomProfileImage = MutableStateFlow(File(context.filesDir, "user_avatar.jpg").exists())
    val hasCustomProfileImage: StateFlow<Boolean> = _hasCustomProfileImage.asStateFlow()

    private val _profileScale = MutableStateFlow(prefs.getFloat("profile_scale", 1.0f))
    val profileScale: StateFlow<Float> = _profileScale.asStateFlow()

    private val _profileOffsetX = MutableStateFlow(prefs.getFloat("profile_offset_x", 0f))
    val profileOffsetX: StateFlow<Float> = _profileOffsetX.asStateFlow()

    private val _profileOffsetY = MutableStateFlow(prefs.getFloat("profile_offset_y", 0f))
    val profileOffsetY: StateFlow<Float> = _profileOffsetY.asStateFlow()

    // Custom Background Image
    private val _hasCustomBackground = MutableStateFlow(File(context.filesDir, "user_bg.jpg").exists())
    val hasCustomBackground: StateFlow<Boolean> = _hasCustomBackground.asStateFlow()

    private val _bgUpdateTrigger = MutableStateFlow(System.currentTimeMillis())
    val bgUpdateTrigger: StateFlow<Long> = _bgUpdateTrigger.asStateFlow()

    private val _customBgDim = MutableStateFlow(prefs.getFloat("custom_bg_dim", 0.78f))
    val customBgDim: StateFlow<Float> = _customBgDim.asStateFlow()

    private val _customBgScale = MutableStateFlow(prefs.getFloat("custom_bg_scale", 1.0f))
    val customBgScale: StateFlow<Float> = _customBgScale.asStateFlow()

    private val _customBgOffsetX = MutableStateFlow(prefs.getFloat("custom_bg_offset_x", 0f))
    val customBgOffsetX: StateFlow<Float> = _customBgOffsetX.asStateFlow()

    private val _customBgOffsetY = MutableStateFlow(prefs.getFloat("custom_bg_offset_y", 0f))
    val customBgOffsetY: StateFlow<Float> = _customBgOffsetY.asStateFlow()

    // Glassmorphism Card Opacity when Wallpaper is active
    private val _cardAlpha = MutableStateFlow(prefs.getFloat("card_alpha", 0.55f))
    val cardAlpha: StateFlow<Float> = _cardAlpha.asStateFlow()

    // Google Gemini AI Configuration
    private val _geminiApiKey = MutableStateFlow(prefs.getString("gemini_api_key", "") ?: "")
    val geminiApiKey: StateFlow<String> = _geminiApiKey.asStateFlow()

    private val _geminiModel: MutableStateFlow<String> = run {
        val raw = prefs.getString("gemini_model", "gemini-3.5-flash-lite") ?: "gemini-3.5-flash-lite"
        val actual = if (raw == "gemini-2.5-flash-lite") "gemini-3.5-flash-lite" else raw
        MutableStateFlow(actual)
    }
    val geminiModel: StateFlow<String> = _geminiModel.asStateFlow()

    // Chart Display Mode ("half_pie", "donut", "bar", "timeline")
    private val _chartMode = MutableStateFlow(prefs.getString("chart_mode", "half_pie") ?: "half_pie")
    val chartMode: StateFlow<String> = _chartMode.asStateFlow()

    // Saved DuitNow / Payment details for Tolong Beli claims
    private val _userDuitNowInfo = MutableStateFlow(prefs.getString("user_duitnow_info", "") ?: "")
    val userDuitNowInfo: StateFlow<String> = _userDuitNowInfo.asStateFlow()

    // GitHub Releases Auto-Update
    private val _autoCheckUpdates = MutableStateFlow(prefs.getBoolean("auto_check_updates", true))
    val autoCheckUpdates: StateFlow<Boolean> = _autoCheckUpdates.asStateFlow()

    private val _githubRepoSlug = MutableStateFlow(prefs.getString("github_repo_slug", UpdateManager.DEFAULT_REPO) ?: UpdateManager.DEFAULT_REPO)
    val githubRepoSlug: StateFlow<String> = _githubRepoSlug.asStateFlow()

    private val _availableUpdate = MutableStateFlow<AppUpdateInfo?>(null)
    val availableUpdate: StateFlow<AppUpdateInfo?> = _availableUpdate.asStateFlow()

    init {
        loadData()
        if (_autoCheckUpdates.value) {
            kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
                try {
                    val res = UpdateManager.checkForUpdates(context, _githubRepoSlug.value)
                    res.onSuccess {
                        val dismissedVersion = prefs.getString("dismissed_update_version", "") ?: ""
                        if (it.hasUpdate && it.latestVersion != dismissedVersion) {
                            _availableUpdate.value = it
                        }
                    }
                } catch (_: Exception) {}
            }
        }
    }

    private fun currentMonthString(): String {
        return SimpleDateFormat("yyyy-MM", Locale.ENGLISH).format(Date())
    }

    private fun loadData() {
        val budgetVal = prefs.getFloat("monthly_budget", 3500f).toDouble()
        _monthlyBudget.value = budgetVal

        val monthVal = prefs.getString("selected_month", null) ?: currentMonthString()
        _selectedMonth.value = monthVal

        val txJson = prefs.getString("transactions_json", null)
        if (!txJson.isNullOrEmpty()) {
            try {
                val list = json.decodeFromString<List<Transaction>>(txJson)
                _transactions.value = list
            } catch (_: Exception) {
                _transactions.value = emptyList()
            }
        } else {
            _transactions.value = emptyList()
        }
    }

    fun addTransaction(tx: Transaction) {
        val current = _transactions.value.toMutableList()
        current.add(0, tx)
        _transactions.value = current
        saveTransactions(current)
    }

    fun addMultipleTransactions(txs: List<Transaction>) {
        val current = _transactions.value.toMutableList()
        current.addAll(0, txs)
        _transactions.value = current
        saveTransactions(current)
    }

    fun deleteTransaction(id: String) {
        val current = _transactions.value.filter { it.id != id }
        _transactions.value = current
        saveTransactions(current)
    }

    fun updateTransaction(tx: Transaction) {
        val current = _transactions.value.toMutableList()
        val idx = current.indexOfFirst { it.id == tx.id }
        if (idx != -1) {
            current[idx] = tx
            _transactions.value = current
            saveTransactions(current)
        }
    }

    fun toggleTransactionSettled(id: String) {
        val current = _transactions.value.toMutableList()
        val idx = current.indexOfFirst { it.id == id }
        if (idx != -1) {
            val old = current[idx]
            current[idx] = old.copy(isSettled = !old.isSettled)
            _transactions.value = current
            saveTransactions(current)
        }
    }

    fun setDuitNowInfo(info: String) {
        _userDuitNowInfo.value = info.trim()
        prefs.edit().putString("user_duitnow_info", info.trim()).apply()
    }

    fun setAutoCheckUpdates(enabled: Boolean) {
        _autoCheckUpdates.value = enabled
        prefs.edit().putBoolean("auto_check_updates", enabled).apply()
    }

    fun setGithubRepoSlug(slug: String) {
        val clean = slug.trim().ifBlank { UpdateManager.DEFAULT_REPO }
        _githubRepoSlug.value = clean
        prefs.edit().putString("github_repo_slug", clean).apply()
    }

    fun dismissUpdate(version: String? = null) {
        _availableUpdate.value = null
        if (!version.isNullOrBlank()) {
            prefs.edit().putString("dismissed_update_version", version).apply()
        }
    }

    suspend fun checkForUpdates(): Result<AppUpdateInfo> = withContext(Dispatchers.IO) {
        val res = UpdateManager.checkForUpdates(context, _githubRepoSlug.value)
        res.onSuccess { info ->
            if (info.hasUpdate) {
                _availableUpdate.value = info
            }
        }
        res
    }

    fun updateBudget(newBudget: Double) {
        _monthlyBudget.value = newBudget
        prefs.edit().putFloat("monthly_budget", newBudget.toFloat()).apply()
    }

    fun selectMonth(month: String) {
        _selectedMonth.value = month
        prefs.edit().putString("selected_month", month).apply()
    }

    fun setChartMode(mode: String) {
        _chartMode.value = mode
        prefs.edit().putString("chart_mode", mode).apply()
    }

    fun setProfileEmoji(emoji: String) {
        _userProfileEmoji.value = emoji
        prefs.edit().putString("profile_emoji", emoji).apply()
        removeProfileImage()
    }

    fun setGeminiApiKey(key: String) {
        _geminiApiKey.value = key.trim()
        prefs.edit().putString("gemini_api_key", key.trim()).apply()
    }

    fun setGeminiModel(model: String) {
        val cleaned = when (model.trim()) {
            "gemini-2.5-flash-lite", "gemini-2.5-flash" -> "gemini-3.5-flash-lite"
            else -> model.trim().ifBlank { "gemini-3.5-flash-lite" }
        }
        _geminiModel.value = cleaned
        prefs.edit().putString("gemini_model", cleaned).apply()
    }

    suspend fun testGeminiApiKey(key: String, model: String): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = key.trim()
        if (apiKey.isEmpty()) return@withContext Result.failure(Exception("API Key cannot be blank"))
        val selectedModel = when (model.trim()) {
            "gemini-2.5-flash-lite", "gemini-2.5-flash" -> "gemini-3.5-flash-lite"
            else -> model.trim().ifBlank { "gemini-3.5-flash-lite" }
        }
        val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/$selectedModel:generateContent?key=$apiKey"

        try {
            val url = URL(endpoint)
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 30000
                readTimeout = 30000
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            }
            val testPayload = """{"contents":[{"parts":[{"text":"Hello, test connection."}]}]}"""
            conn.outputStream.use { it.write(testPayload.toByteArray(Charsets.UTF_8)) }
            val code = conn.responseCode
            if (code in 200..299) {
                Result.success("Connected to $selectedModel successfully!")
            } else {
                val err = conn.errorStream?.bufferedReader()?.use { it.readText() } ?: "HTTP $code"
                Result.failure(Exception("HTTP $code: $err"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun callGemini(
        prompt: String,
        fileBytes: ByteArray? = null,
        mimeType: String? = null,
        includeLedgerContext: Boolean = true
    ): GeminiExecutionResult = withContext(Dispatchers.IO) {
        val apiKey = _geminiApiKey.value.trim()
        if (apiKey.isEmpty()) {
            return@withContext GeminiExecutionResult(
                success = false,
                summary = "Please enter your free Google Gemini API key in Settings to use the AI Assistant.",
                errorMessage = "API key not configured"
            )
        }

        val rawModel = _geminiModel.value.trim()
        val model = when (rawModel) {
            "gemini-2.5-flash-lite", "gemini-2.5-flash" -> "gemini-3.5-flash-lite"
            else -> rawModel.ifBlank { "gemini-3.5-flash-lite" }
        }
        val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"

        try {
            val recentTxs = _transactions.value.take(40)
            val ledgerContext = if (includeLedgerContext && recentTxs.isNotEmpty()) {
                val sample = recentTxs.joinToString("\n") {
                    "- [ID: ${it.id}] ${it.date} ${it.time}: ${it.title} (${it.type}) RM ${String.format(Locale.US, "%.2f", it.amount)} [Cat: ${it.category}]"
                }
                "\n\nCURRENT RECENT TRANSACTIONS IN USER'S LOCAL LEDGER (use targetId matching the ID when user requests deleting or editing an existing transaction):\n$sample"
            } else ""

            val systemInstruction = """
You are RinggitWise AI, an expert Malaysian personal finance assistant.
Today's date is ${SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(Date())}.

Supported Malaysian expense categories:
- Mamak & Dining
- Drinks & Cafe
- Groceries
- Fuel & Transport
- Parking & Tolls
- Shopping & Online
- Utilities & Bills
- Healthcare & Pharmacy
- Snacks & Desserts
- Top Up (Credit)
- Tolong Beli (Help Buy)

TASK & CAPABILITIES:
1. When given an image, receipt photo, bank statement screenshot, or PDF document (e.g. Touch 'n Go eWallet, Maybank MAE, CIMB, Bank Islam, RHB, Public Bank, APSpace / student card):
   - Extract ALL distinct financial transactions found.
   - For each transaction, create an action with actionType="ADD".
   - Extract title (clean store/merchant name), amount (positive float e.g. 15.50), type ("debit" for expenses, "credit" for income/refund/transfer in), category (best match from supported categories), date (YYYY-MM-DD), time (HH:mm:ss if present), and payment method (e.g. "DuitNow QR", "TNG eWallet", "Debit Card", etc.).
2. When given natural language user requests (e.g., "Add RM 12 for chicken rice", "I helped Ah Ming buy Starbucks RM 15.50", "Tolong beli lunch for Sarah RM 12", "Delete my Spotify payment", "Update yesterday's Grab to RM 18", "How much have I spent on food?"):
   - For additions: actionType="ADD"
   - When user mentions helping someone buy, buying on behalf of a friend/colleague/family (e.g. "helped Ah Ming buy", "tolong beli for Sarah", "claim from boss"):
     Set category="Tolong Beli (Help Buy)", isHelpBuy=true, and extract claimRecipient with the person's name (e.g. "Ah Ming", "Sarah").
   - For deletions: actionType="DELETE" and set targetId matching the transaction ID from the ledger context
   - For updates/corrections: actionType="UPDATE" and set targetId with updated fields
   - For questions / advice: actionType="MESSAGE" with no modifications
3. OUTPUT REQUIREMENT:
You MUST respond ONLY with a valid JSON object matching this exact structure:
{
  "summary": "Clear, friendly explanation in Malaysian English of what was detected or actioned",
  "actions": [
    {
      "actionType": "ADD" or "DELETE" or "UPDATE" or "MESSAGE",
      "title": "Merchant / Description",
      "amount": 0.0,
      "type": "debit" or "credit",
      "category": "Mamak & Dining",
      "method": "DuitNow QR",
      "date": "YYYY-MM-DD",
      "time": "HH:mm:ss",
      "targetId": "",
      "note": "",
      "explanation": "Brief reason",
      "isHelpBuy": false,
      "claimRecipient": ""
    }
  ]
}
$ledgerContext
""".trimIndent()

            val requestJson = JSONObject()
            val contentsArray = JSONArray()
            val contentObj = JSONObject()
            val partsArray = JSONArray()

            // Text prompt
            val fullPrompt = "$systemInstruction\n\nUSER REQUEST / INPUT:\n$prompt"
            val textPart = JSONObject().apply {
                put("text", fullPrompt)
            }
            partsArray.put(textPart)

            // File attachment (PDF or Image)
            if (fileBytes != null && mimeType != null) {
                val base64Data = Base64.encodeToString(fileBytes, Base64.NO_WRAP)
                val inlineDataObj = JSONObject().apply {
                    put("mime_type", mimeType)
                    put("data", base64Data)
                }
                val filePart = JSONObject().apply {
                    put("inline_data", inlineDataObj)
                }
                partsArray.put(filePart)
            }

            contentObj.put("parts", partsArray)
            contentsArray.put(contentObj)
            requestJson.put("contents", contentsArray)

            val genConfig = JSONObject().apply {
                put("temperature", 0.2)
                put("responseMimeType", "application/json")
            }
            requestJson.put("generationConfig", genConfig)

            val url = URL(endpoint)
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 30000
                readTimeout = 60000
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            }

            val postBytes = requestJson.toString().toByteArray(Charsets.UTF_8)
            conn.outputStream.use { it.write(postBytes) }

            val responseCode = conn.responseCode
            val responseBody = if (responseCode in 200..299) {
                conn.inputStream.bufferedReader().use { it.readText() }
            } else {
                val err = conn.errorStream?.bufferedReader()?.use { it.readText() } ?: "HTTP $responseCode"
                throw Exception("Gemini API Error ($responseCode): $err")
            }

            val rootObj = JSONObject(responseBody)
            val candidates = rootObj.optJSONArray("candidates")
            if (candidates == null || candidates.length() == 0) {
                return@withContext GeminiExecutionResult(
                    success = false,
                    summary = "Gemini returned no response.",
                    rawResponse = responseBody,
                    errorMessage = "Empty candidates"
                )
            }

            val firstCandidate = candidates.getJSONObject(0)
            val contentRes = firstCandidate.optJSONObject("content")
            val partsRes = contentRes?.optJSONArray("parts")
            val textRes = partsRes?.optJSONObject(0)?.optString("text") ?: ""

            val cleanJson = textRes.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
            val parsedJson = JSONObject(cleanJson)

            val summary = parsedJson.optString("summary", "Processed successfully.")
            val actionsArray = parsedJson.optJSONArray("actions")
            val actionsList = mutableListOf<GeminiProposedAction>()

            if (actionsArray != null) {
                for (i in 0 until actionsArray.length()) {
                    val act = actionsArray.getJSONObject(i)
                    val actionType = act.optString("actionType", "ADD").uppercase()
                    val title = act.optString("title", "Expense")
                    val amount = act.optDouble("amount", 0.0)
                    val type = act.optString("type", "debit")
                    val cat = act.optString("category", "Mamak & Dining")
                    val method = act.optString("method", "DuitNow QR")
                    val date = act.optString("date", SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(Date()))
                    val time = act.optString("time", SimpleDateFormat("HH:mm:ss", Locale.ENGLISH).format(Date()))
                    val targetId = act.optString("targetId", "")
                    val note = act.optString("note", "")
                    val explanation = act.optString("explanation", "")
                    val isHelpBuy = act.optBoolean("isHelpBuy", false) || cat.contains("Tolong Beli", ignoreCase = true)
                    val claimRecipient = act.optString("claimRecipient", "")

                    actionsList.add(
                        GeminiProposedAction(
                            actionType = actionType,
                            title = title,
                            amount = amount,
                            type = type,
                            category = cat,
                            method = method,
                            date = date,
                            time = time,
                            note = note,
                            targetId = targetId,
                            explanation = explanation,
                            isHelpBuy = isHelpBuy,
                            claimRecipient = claimRecipient
                        )
                    )
                }
            }

            GeminiExecutionResult(
                success = true,
                summary = summary,
                actions = actionsList,
                rawResponse = textRes
            )
        } catch (e: Exception) {
            GeminiExecutionResult(
                success = false,
                summary = "Error contacting Gemini: ${e.message}",
                errorMessage = e.message ?: "Unknown error",
                rawResponse = e.stackTraceToString()
            )
        }
    }

    fun executeAction(action: GeminiProposedAction) {
        when (action.actionType.uppercase()) {
            "ADD" -> {
                addTransaction(
                    Transaction(
                        date = action.date.ifBlank { SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(Date()) },
                        time = action.time.ifBlank { SimpleDateFormat("HH:mm:ss", Locale.ENGLISH).format(Date()) },
                        title = action.title.ifBlank { "Expense" },
                        amount = action.amount,
                        type = action.type.ifBlank { "debit" },
                        category = action.category.ifBlank { "Mamak & Dining" },
                        method = action.method.ifBlank { "DuitNow QR" },
                        note = action.note.ifBlank { "Added via RinggitWise AI" },
                        isHelpBuy = action.isHelpBuy || action.category.contains("Tolong Beli", ignoreCase = true),
                        claimRecipient = action.claimRecipient
                    )
                )
            }
            "DELETE" -> {
                if (action.targetId.isNotBlank()) {
                    deleteTransaction(action.targetId)
                } else if (action.title.isNotBlank()) {
                    val match = _transactions.value.find {
                        it.title.contains(action.title, ignoreCase = true) || action.title.contains(it.title, ignoreCase = true)
                    }
                    if (match != null) {
                        deleteTransaction(match.id)
                    }
                }
            }
            "UPDATE" -> {
                val existing = if (action.targetId.isNotBlank()) {
                    _transactions.value.find { it.id == action.targetId }
                } else {
                    _transactions.value.find {
                        it.title.contains(action.title, ignoreCase = true) || action.title.contains(it.title, ignoreCase = true)
                    }
                }
                if (existing != null) {
                    val updated = existing.copy(
                        title = if (action.title.isNotBlank()) action.title else existing.title,
                        amount = if (action.amount > 0) action.amount else existing.amount,
                        category = if (action.category.isNotBlank()) action.category else existing.category,
                        date = if (action.date.isNotBlank()) action.date else existing.date,
                        time = if (action.time.isNotBlank()) action.time else existing.time,
                        type = if (action.type.isNotBlank()) action.type else existing.type,
                        isHelpBuy = if (action.isHelpBuy) true else existing.isHelpBuy,
                        claimRecipient = if (action.claimRecipient.isNotBlank()) action.claimRecipient else existing.claimRecipient
                    )
                    updateTransaction(updated)
                }
            }
        }
    }

    fun executeMultipleActions(actions: List<GeminiProposedAction>) {
        actions.forEach { executeAction(it) }
    }

    fun getProfileAvatarFile(): File = File(context.filesDir, "user_avatar.jpg")
    fun getRawAvatarFile(): File = File(context.filesDir, "raw_avatar.jpg")

    fun getBackgroundFile(): File = File(context.filesDir, "user_bg.jpg")
    fun getRawBackgroundFile(): File = File(context.filesDir, "raw_bg.jpg")

    fun loadSafeBitmapFromUri(uri: Uri, maxDim: Int = 2048): Bitmap? {
        return try {
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, options)
            }

            var sampleSize = 1
            var w = options.outWidth
            var h = options.outHeight
            while (w / 2 >= maxDim || h / 2 >= maxDim) {
                w /= 2
                h /= 2
                sampleSize *= 2
            }

            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            val bitmap = context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, decodeOptions)
            } ?: return null

            val curW = bitmap.width
            val curH = bitmap.height
            if (curW > maxDim || curH > maxDim) {
                val factor = maxDim.toFloat() / maxOf(curW, curH)
                val targetW = (curW * factor).toInt().coerceAtLeast(1)
                val targetH = (curH * factor).toInt().coerceAtLeast(1)
                Bitmap.createScaledBitmap(bitmap, targetW, targetH, true)
            } else {
                bitmap
            }
        } catch (_: Throwable) {
            null
        }
    }

    fun saveCroppedAvatar(bitmap: Bitmap, rawSource: Bitmap? = null) {
        try {
            FileOutputStream(getProfileAvatarFile()).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
            }
            rawSource?.let { raw ->
                FileOutputStream(getRawAvatarFile()).use { out ->
                    raw.compress(Bitmap.CompressFormat.JPEG, 90, out)
                }
            }
            _hasCustomProfileImage.value = true
            _avatarUpdateTrigger.value = System.currentTimeMillis()
        } catch (_: Exception) {}
    }

    fun saveCroppedBackground(bitmap: Bitmap, rawSource: Bitmap? = null) {
        try {
            FileOutputStream(getBackgroundFile()).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 88, out)
            }
            rawSource?.let { raw ->
                FileOutputStream(getRawBackgroundFile()).use { out ->
                    raw.compress(Bitmap.CompressFormat.JPEG, 85, out)
                }
            }
            _hasCustomBackground.value = true
            _bgUpdateTrigger.value = System.currentTimeMillis()
        } catch (_: Exception) {}
    }

    fun getRawAvatarBitmap(): Bitmap? {
        val raw = getRawAvatarFile()
        if (raw.exists()) return loadSafeBitmap(raw, maxDim = 2048)
        val cropped = getProfileAvatarFile()
        if (cropped.exists()) return loadSafeBitmap(cropped, maxDim = 2048)
        return null
    }

    fun getRawBackgroundBitmap(): Bitmap? {
        val raw = getRawBackgroundFile()
        if (raw.exists()) return loadSafeBitmap(raw, maxDim = 2048)
        val cropped = getBackgroundFile()
        if (cropped.exists()) return loadSafeBitmap(cropped, maxDim = 2048)
        return null
    }

    private fun saveScaledImage(uri: Uri, destFile: File, maxDim: Int, quality: Int) {
        try {
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, options)
            }

            var sampleSize = 1
            var w = options.outWidth
            var h = options.outHeight
            while (w / 2 >= maxDim || h / 2 >= maxDim) {
                w /= 2
                h /= 2
                sampleSize *= 2
            }

            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            val bitmap = context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, decodeOptions)
            } ?: return

            val curW = bitmap.width
            val curH = bitmap.height
            val finalBitmap = if (curW > maxDim || curH > maxDim) {
                val factor = maxDim.toFloat() / maxOf(curW, curH)
                val targetW = (curW * factor).toInt().coerceAtLeast(1)
                val targetH = (curH * factor).toInt().coerceAtLeast(1)
                Bitmap.createScaledBitmap(bitmap, targetW, targetH, true)
            } else {
                bitmap
            }

            FileOutputStream(destFile).use { out ->
                finalBitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
            }
        } catch (_: Exception) {}
    }

    fun saveProfileImage(uri: Uri) {
        saveScaledImage(uri, getProfileAvatarFile(), maxDim = 512, quality = 90)
        _hasCustomProfileImage.value = true
        _avatarUpdateTrigger.value = System.currentTimeMillis()
    }

    fun removeProfileImage() {
        val file = getProfileAvatarFile()
        if (file.exists()) file.delete()
        val raw = getRawAvatarFile()
        if (raw.exists()) raw.delete()
        _hasCustomProfileImage.value = false
        _avatarUpdateTrigger.value = System.currentTimeMillis()
    }

    fun setProfilePosition(scale: Float, offsetX: Float, offsetY: Float) {
        _profileScale.value = scale
        _profileOffsetX.value = offsetX
        _profileOffsetY.value = offsetY
        prefs.edit()
            .putFloat("profile_scale", scale)
            .putFloat("profile_offset_x", offsetX)
            .putFloat("profile_offset_y", offsetY)
            .apply()
    }

    fun saveBackgroundImage(uri: Uri) {
        saveScaledImage(uri, getBackgroundFile(), maxDim = 1600, quality = 85)
        _hasCustomBackground.value = true
        _bgUpdateTrigger.value = System.currentTimeMillis()
    }

    fun removeBackgroundImage() {
        val file = getBackgroundFile()
        if (file.exists()) file.delete()
        val raw = getRawBackgroundFile()
        if (raw.exists()) raw.delete()
        _hasCustomBackground.value = false
        _bgUpdateTrigger.value = System.currentTimeMillis()
    }

    fun setBackgroundPosition(scale: Float, offsetX: Float, offsetY: Float) {
        _customBgScale.value = scale
        _customBgOffsetX.value = offsetX
        _customBgOffsetY.value = offsetY
        prefs.edit()
            .putFloat("custom_bg_scale", scale)
            .putFloat("custom_bg_offset_x", offsetX)
            .putFloat("custom_bg_offset_y", offsetY)
            .apply()
    }

    fun setCustomBgDim(dim: Float) {
        _customBgDim.value = dim
        prefs.edit().putFloat("custom_bg_dim", dim).apply()
    }

    fun setCardAlpha(alpha: Float) {
        _cardAlpha.value = alpha
        prefs.edit().putFloat("card_alpha", alpha).apply()
    }

    fun loadSafeBitmap(file: File, maxDim: Int): Bitmap? {
        if (!file.exists()) return null
        return try {
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(file.absolutePath, options)
            var sampleSize = 1
            var w = options.outWidth
            var h = options.outHeight
            while (w / 2 >= maxDim || h / 2 >= maxDim) {
                w /= 2
                h /= 2
                sampleSize *= 2
            }
            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            BitmapFactory.decodeFile(file.absolutePath, decodeOptions)
        } catch (_: Throwable) {
            null
        }
    }

    fun clearAllData() {
        _transactions.value = emptyList()
        prefs.edit().remove("transactions_json").apply()
    }

    private fun saveTransactions(list: List<Transaction>) {
        try {
            val encoded = json.encodeToString(list)
            prefs.edit().putString("transactions_json", encoded).apply()
        } catch (_: Exception) {}
    }
}
