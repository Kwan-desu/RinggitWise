package com.ringgitwise.app.ui

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.ringgitwise.app.data.ExportManager
import com.ringgitwise.app.data.FinanceRepository
import com.ringgitwise.app.data.Transaction
import com.ringgitwise.app.ui.theme.Info
import com.ringgitwise.app.ui.theme.Warning
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun ExportScreen(
    repository: FinanceRepository,
    allTransactions: List<Transaction>,
    selectedMonth: String,
    monthlyBudget: Double
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var allTime by remember { mutableStateOf(selectedMonth == "all") }
    var pendingRestore by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }

    val scoped = remember(allTransactions, allTime, selectedMonth) {
        if (allTime || selectedMonth == "all") allTransactions else allTransactions.filter { it.date.startsWith(selectedMonth) }
    }
    val periodLabel = if (allTime || selectedMonth == "all") "All time" else formatDisplayMonth(selectedMonth)
    val fileTag = if (allTime || selectedMonth == "all") "all-time" else selectedMonth

    fun toast(msg: String) = Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()

    val csvLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        if (uri != null) scope.launch {
            busy = true
            val ok = withContext(Dispatchers.IO) {
                runCatching { context.contentResolver.openOutputStream(uri)?.use { it.write(ExportManager.buildCsv(scoped).toByteArray()) } }.isSuccess
            }
            busy = false
            toast(if (ok) "CSV saved" else "Couldn't save the CSV")
        }
    }
    val pdfLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri ->
        if (uri != null) scope.launch {
            busy = true
            val ok = withContext(Dispatchers.IO) {
                runCatching {
                    context.contentResolver.openOutputStream(uri)?.use {
                        ExportManager.writePdfReport(it, periodLabel, scoped, if (allTime) null else monthlyBudget)
                    }
                }.isSuccess
            }
            busy = false
            toast(if (ok) "PDF report saved" else "Couldn't create the PDF")
        }
    }
    val backupLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) scope.launch {
            busy = true
            val ok = withContext(Dispatchers.IO) {
                runCatching { context.contentResolver.openOutputStream(uri)?.use { it.write(repository.exportBackupJson().toByteArray()) } }.isSuccess
            }
            busy = false
            toast(if (ok) "Backup saved" else "Couldn't save the backup")
        }
    }
    val restoreLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) scope.launch {
            val text = withContext(Dispatchers.IO) {
                runCatching { context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() } }.getOrNull()
            }
            if (text.isNullOrBlank()) toast("Couldn't read that file") else pendingRestore = text
        }
    }

    fun shareCsv() {
        scope.launch {
            val file = withContext(Dispatchers.IO) {
                runCatching {
                    File(context.cacheDir, "ringgitwise-$fileTag.csv").apply { writeText(ExportManager.buildCsv(scoped)) }
                }.getOrNull()
            } ?: return@launch toast("Couldn't prepare the file")
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val send = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "RinggitWise expenses – $periodLabel")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(send, "Share expenses"))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Space.lg)
            .padding(top = Space.sm, bottom = 48.dp),
        verticalArrangement = Arrangement.spacedBy(Space.md)
    ) {
        SectionHeader("Reports")
        AppCard {
            Text("Period", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(Space.sm))
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = !allTime && selectedMonth != "all",
                    enabled = selectedMonth != "all",
                    onClick = { allTime = false },
                    shape = SegmentedButtonDefaults.itemShape(0, 2)
                ) { Text(if (selectedMonth == "all") "Month" else formatDisplayMonth(selectedMonth)) }
                SegmentedButton(
                    selected = allTime || selectedMonth == "all",
                    onClick = { allTime = true },
                    shape = SegmentedButtonDefaults.itemShape(1, 2)
                ) { Text("All time") }
            }
            Spacer(Modifier.height(Space.sm))
            Text(
                "${scoped.size} transactions • ${formatRMExact(scoped.filter { it.type != "credit" }.sumOf { it.amount })} spent",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        AppCard(contentPadding = PaddingValues(horizontal = Space.lg, vertical = Space.xs)) {
            ListRow(Icons.Default.PictureAsPdf, Warning, "PDF report", "Summary, category bars and full transaction list",
                onClick = { pdfLauncher.launch("ringgitwise-report-$fileTag.pdf") })
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            ListRow(Icons.Default.TableChart, MaterialTheme.colorScheme.primary, "Save as CSV", "Open in Excel or Google Sheets",
                onClick = { csvLauncher.launch("ringgitwise-$fileTag.csv") })
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            ListRow(Icons.Default.Share, Info, "Share CSV", "Send via WhatsApp, email or Drive", onClick = { shareCsv() })
        }

        SectionHeader("Backup & restore")
        AppCard(contentPadding = PaddingValues(horizontal = Space.lg, vertical = Space.xs)) {
            ListRow(Icons.Default.CloudUpload, MaterialTheme.colorScheme.primary, "Save backup",
                "Expenses, bills, goals and budget in one file",
                onClick = { backupLauncher.launch("ringgitwise-backup-${System.currentTimeMillis() / 1000}.json") })
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            ListRow(Icons.Default.Restore, Warning, "Restore from backup", "Replaces the data on this phone",
                onClick = { restoreLauncher.launch(arrayOf("application/json", "text/plain", "*/*")) })
        }
        Text(
            "Tip: save a backup to Google Drive before switching phones.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = Space.xs)
        )
        if (busy) LinearProgressIndicator(Modifier.fillMaxWidth())
    }

    if (pendingRestore != null) {
        AlertDialog(
            onDismissRequest = { pendingRestore = null },
            icon = { Icon(Icons.Default.Restore, contentDescription = null) },
            title = { Text("Restore this backup?") },
            text = { Text("All expenses, bills and goals currently on this phone will be replaced. This can't be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    val raw = pendingRestore!!
                    pendingRestore = null
                    repository.importBackupJson(raw)
                        .onSuccess { toast("Restored $it transactions") }
                        .onFailure { toast(it.message ?: "Restore failed") }
                }) { Text("Restore") }
            },
            dismissButton = { TextButton(onClick = { pendingRestore = null }) { Text("Cancel") } }
        )
    }
}
