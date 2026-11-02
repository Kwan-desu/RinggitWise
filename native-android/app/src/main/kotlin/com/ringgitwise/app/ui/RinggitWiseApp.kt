package com.ringgitwise.app.ui

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ringgitwise.app.data.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RinggitWiseApp(repository: FinanceRepository) {
    val transactions by repository.transactions.collectAsState()
    val monthlyBudget by repository.monthlyBudget.collectAsState()
    val selectedMonth by repository.selectedMonth.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddSheet by remember { mutableStateOf(false) }
    var showBudgetDialog by remember { mutableStateOf(false) }
    var showClearDialog by remember { mutableStateOf(false) }
    var showMonthPicker by remember { mutableStateOf(false) }

    // Filter only genuine expenses (exclude credit/topups)
    val expenses = remember(transactions) {
        transactions.filter { it.amount > 0 && it.type != "credit" }
    }

    // Active month expenses
    val activeMonthExpenses = remember(expenses, selectedMonth) {
        if (selectedMonth == "all") expenses
        else expenses.filter { it.date.startsWith(selectedMonth) }
    }

    val totalSpent = remember(activeMonthExpenses) {
        activeMonthExpenses.sumOf { it.amount }
    }

    val remaining = (monthlyBudget - totalSpent).coerceAtLeast(0.0)
    val percentUsed = if (monthlyBudget > 0) min(100, ((totalSpent / monthlyBudget) * 100).toInt()) else 0
    val isOverBudget = totalSpent > monthlyBudget

    // Date calculations for daily allowance
    val calendar = Calendar.getInstance()
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
    val daysRemaining = (daysInMonth - currentDay).coerceAtLeast(1)
    val dailySafeSpend = if (remaining > 0) remaining / daysRemaining else 0.0

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("RM", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("RinggitWise", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Material You • Malaysia", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                },
                actions = {
                    // Month Stepper with Left/Right chevrons and interactive calendar picker
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 2.dp, vertical = 2.dp)
                        ) {
                            IconButton(
                                onClick = { repository.selectMonth(getPreviousMonth(selectedMonth)) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    Icons.Default.ChevronLeft,
                                    contentDescription = "Previous Month",
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { showMonthPicker = true }
                                    .padding(horizontal = 6.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = formatDisplayMonth(selectedMonth),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            IconButton(
                                onClick = { repository.selectMonth(getNextMonth(selectedMonth)) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    Icons.Default.ChevronRight,
                                    contentDescription = "Next Month",
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    IconButton(onClick = { showClearDialog = true }) {
                        Icon(Icons.Outlined.DeleteSweep, contentDescription = "Clear Data", tint = MaterialTheme.colorScheme.error)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainer) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Overview") },
                    label = { Text("Overview") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.PieChart, contentDescription = "Charts") },
                    label = { Text("Charts") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.ReceiptLong, contentDescription = "Import") },
                    label = { Text("TNG Parse") }
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.History, contentDescription = "Ledger") },
                    label = { Text("Ledger") }
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddSheet = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(18.dp)
            ) {
                Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Add, contentDescription = "Add Expense")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add RM", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            when (selectedTab) {
                0 -> OverviewTab(
                    totalSpent = totalSpent,
                    monthlyBudget = monthlyBudget,
                    remaining = remaining,
                    dailySafeSpend = dailySafeSpend,
                    percentUsed = percentUsed,
                    isOverBudget = isOverBudget,
                    daysRemaining = daysRemaining,
                    selectedMonth = selectedMonth,
                    recentTransactions = activeMonthExpenses.take(5),
                    onEditBudget = { showBudgetDialog = true },
                    onNavigateToHistory = { selectedTab = 3 },
                    onDeleteTransaction = { repository.deleteTransaction(it) }
                )
                1 -> ChartsTab(
                    expenses = activeMonthExpenses,
                    totalSpent = totalSpent,
                    selectedMonth = selectedMonth
                )
                2 -> ImportTab(
                    onImportTransactions = { list ->
                        repository.addMultipleTransactions(list)
                        selectedTab = 3
                    }
                )
                3 -> LedgerTab(
                    transactions = activeMonthExpenses,
                    onDelete = { repository.deleteTransaction(it) }
                )
            }
        }
    }

    // Add Expense Bottom Sheet
    if (showAddSheet) {
        AddExpenseSheet(
            onDismiss = { showAddSheet = false },
            onAdd = { newTx ->
                repository.addTransaction(newTx)
                showAddSheet = false
            }
        )
    }

    // Edit Budget Dialog
    if (showBudgetDialog) {
        EditBudgetDialog(
            currentBudget = monthlyBudget,
            onDismiss = { showBudgetDialog = false },
            onSave = { newBudget ->
                repository.updateBudget(newBudget)
                showBudgetDialog = false
            }
        )
    }

    // Month Picker Dialog
    if (showMonthPicker) {
        MonthPickerDialog(
            currentMonth = selectedMonth,
            transactions = transactions,
            onDismiss = { showMonthPicker = false },
            onSelect = { month ->
                repository.selectMonth(month)
                showMonthPicker = false
            }
        )
    }

    // Clear Data Confirmation Dialog
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Reset All Data?") },
            text = { Text("This will permanently clear all recorded expenses and start fresh from zero.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        repository.clearAllData()
                        showClearDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Clear Everything")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// -------------------------------------------------------------
// OVERVIEW TAB
// -------------------------------------------------------------
@Composable
fun OverviewTab(
    totalSpent: Double,
    monthlyBudget: Double,
    remaining: Double,
    dailySafeSpend: Double,
    percentUsed: Int,
    isOverBudget: Boolean,
    daysRemaining: Int,
    selectedMonth: String,
    recentTransactions: List<Transaction>,
    onEditBudget: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onDeleteTransaction: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 4 Material You KPI Cards in 2x2 Grid
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            // 1. Total Spent Card
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("TOTAL SPENT", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(formatRM(totalSpent), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                    Spacer(modifier = Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = { (percentUsed / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                        color = if (isOverBudget) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceContainer
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("$percentUsed% of budget", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // 2. Monthly Budget Card
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("BUDGET", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        IconButton(onClick = onEditBudget, modifier = Modifier.size(20.dp)) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(formatRM(monthlyBudget), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                    Spacer(modifier = Modifier.height(14.dp))
                    Text("Ceiling Limit (RM)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            // 3. Remaining Balance Card
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("REMAINING", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Surface(
                            shape = CircleShape,
                            color = if (isOverBudget) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = if (isOverBudget) "Over" else "Track",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isOverBudget) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isOverBudget) "-${formatRM(totalSpent - monthlyBudget)}" else formatRM(remaining),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = if (isOverBudget) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text("$daysRemaining days left in month", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // 4. Daily Safe Allowance Card
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("DAILY SAFE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(formatRM(dailySafeSpend), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(14.dp))
                    Text("Guilt-free daily burn rate", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        // Recent Transactions Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Recent Expenses", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    TextButton(onClick = onNavigateToHistory) {
                        Text("View All")
                    }
                }

                if (recentTransactions.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp), contentAlignment = Alignment.Center) {
                        Text("No expenses recorded yet.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    recentTransactions.forEach { tx ->
                        TransactionRow(tx = tx, onDelete = { onDeleteTransaction(tx.id) })
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(60.dp))
    }
}

// -------------------------------------------------------------
// CHARTS TAB (180° Semi-circular Arch Chart)
// -------------------------------------------------------------
@Composable
fun ChartsTab(
    expenses: List<Transaction>,
    totalSpent: Double,
    selectedMonth: String
) {
    // Aggregate by category
    val catMap = remember(expenses) {
        val map = mutableMapOf<String, Double>()
        expenses.forEach { tx ->
            val cat = tx.category.ifEmpty { "Other" }
            map[cat] = (map[cat] ?: 0.0) + tx.amount
        }
        map.toList().sortedByDescending { it.second }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Category Spending Breakdown", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("180° Arch Distribution in Malaysian Ringgit", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                Spacer(modifier = Modifier.height(20.dp))

                // Native Jetpack Compose Canvas: 180° Semi-Circular Arch
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidth = 38.dp.toPx()
                        val diameter = min(size.width, size.height * 2) - strokeWidth * 2
                        val radius = diameter / 2
                        val centerX = size.width / 2
                        val centerY = size.height - 20.dp.toPx()
                        val topLeft = Offset(centerX - radius, centerY - radius)
                        val arcSize = Size(radius * 2, radius * 2)

                        // Base gray arc guide
                        drawArc(
                            color = Color(0x22888888),
                            startAngle = 180f,
                            sweepAngle = 180f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )

                        if (totalSpent > 0 && catMap.isNotEmpty()) {
                            var currentAngle = 180f
                            val gap = if (catMap.size > 1) 2.5f else 0f
                            val totalGaps = (catMap.size - 1) * gap
                            val availableSpan = 180f - totalGaps

                            catMap.forEach { (catName, amount) ->
                                val pct = (amount / totalSpent).toFloat()
                                val sweep = (pct * availableSpan).coerceAtLeast(1f)
                                val catObj = DEFAULT_CATEGORIES.find { it.name.equals(catName, true) }
                                val color = catObj?.color ?: Color(0xFF54B3D6)

                                drawArc(
                                    color = color,
                                    startAngle = currentAngle,
                                    sweepAngle = sweep,
                                    useCenter = false,
                                    topLeft = topLeft,
                                    size = arcSize,
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                                )
                                currentAngle += sweep + gap
                            }
                        }
                    }

                    // Center Hub Metrics inside the 180° arch
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(top = 70.dp)
                    ) {
                        Text("TOTAL SPENT", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(formatRM(totalSpent), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                        Text("${expenses.size} expenses logged", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Legend row
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    catMap.forEach { (catName, amount) ->
                        val catObj = DEFAULT_CATEGORIES.find { it.name.equals(catName, true) }
                        val color = catObj?.color ?: Color(0xFF54B3D6)
                        val pct = if (totalSpent > 0) (amount / totalSpent * 100).toInt() else 0

                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            modifier = Modifier.padding(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(catName, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("$pct%", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(60.dp))
    }
}

// -------------------------------------------------------------
// TNG / BANK STATEMENT IMPORT TAB
// -------------------------------------------------------------
@Composable
fun ImportTab(onImportTransactions: (List<Transaction>) -> Unit) {
    var rawText by remember { mutableStateOf("") }
    var parsedList by remember { mutableStateOf<List<Transaction>>(emptyList()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DocumentScanner, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Touch 'n Go eWallet & Statement Parser", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }

                Text(
                    "Paste your Touch 'n Go (TNG) eWallet statement, university card (APSpace), or bank report text below to parse and categorize transactions automatically.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = rawText,
                    onValueChange = {
                        rawText = it
                        parsedList = StatementParser.parseFullText(it)
                    },
                    modifier = Modifier.fillMaxWidth().height(160.dp),
                    shape = RoundedCornerShape(16.dp),
                    placeholder = {
                        Text(
                            "e.g.\n15/10/2026 14:23:10 Successful Payment Restoran Ali Food Court -15.50 120.40\n16/10/2026 08:30:15 Successful RFID PLUS TOLL SUBANG -2.50 117.90\n18 2026-10-16 12:48:04 Parking Fee 3.94",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { parsedList = StatementParser.parseFullText(rawText) },
                        modifier = Modifier.weight(1f),
                        shape = CircleShape
                    ) {
                        Text("Parse Text")
                    }

                    if (parsedList.isNotEmpty()) {
                        Button(
                            onClick = {
                                onImportTransactions(parsedList)
                                rawText = ""
                                parsedList = emptyList()
                            },
                            modifier = Modifier.weight(1f),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Text("Import (${parsedList.size})")
                        }
                    }
                }
            }
        }

        if (parsedList.isNotEmpty()) {
            Text("Detected Transactions (${parsedList.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            parsedList.forEach { tx ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(tx.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            Text("${tx.date} • ${tx.category} • ${tx.method}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(formatRM(tx.amount), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(60.dp))
    }
}

// -------------------------------------------------------------
// TRANSACTION LEDGER TAB
// -------------------------------------------------------------
@Composable
fun LedgerTab(
    transactions: List<Transaction>,
    onDelete: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("All") }

    val filtered = remember(transactions, searchQuery, selectedCategoryFilter) {
        transactions.filter { tx ->
            val matchesSearch = tx.title.contains(searchQuery, true) ||
                    tx.category.contains(searchQuery, true) ||
                    tx.note.contains(searchQuery, true)
            val matchesCat = selectedCategoryFilter == "All" || tx.category.equals(selectedCategoryFilter, true)
            matchesSearch && matchesCat
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp)) {
        // Search TextField
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            shape = CircleShape,
            placeholder = { Text("Filter transactions...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Horizontal Category Filter Chips
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedCategoryFilter == "All",
                onClick = { selectedCategoryFilter = "All" },
                label = { Text("All") },
                shape = CircleShape
            )
            DEFAULT_CATEGORIES.forEach { cat ->
                FilterChip(
                    selected = selectedCategoryFilter == cat.name,
                    onClick = { selectedCategoryFilter = cat.name },
                    label = { Text(cat.name) },
                    shape = CircleShape
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Transactions LazyColumn
        if (filtered.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No transactions found.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(filtered, key = { it.id }) { tx ->
                    TransactionCardItem(tx = tx, onDelete = { onDelete(tx.id) })
                }
            }
        }
    }
}

@Composable
fun TransactionCardItem(tx: Transaction, onDelete: () -> Unit) {
    val catObj = DEFAULT_CATEGORIES.find { it.name.equals(tx.category, true) }
    val color = catObj?.color ?: MaterialTheme.colorScheme.primary

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Row(
            modifier = Modifier.padding(14.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = color,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = tx.title.take(1).uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = tx.title.ifEmpty { tx.category },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${tx.date} • ${tx.category} • ${tx.method}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = formatRM(tx.amount),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black
                )
                IconButton(onClick = onDelete) {
                    Icon(Icons.Outlined.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.outline)
                }
            }
        }
    }
}

@Composable
fun TransactionRow(tx: Transaction, onDelete: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(tx.title.ifEmpty { tx.category }, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            Text("${tx.date} • ${tx.category}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(formatRM(tx.amount), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Black)
    }
}

// -------------------------------------------------------------
// ADD EXPENSE BOTTOM SHEET
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseSheet(
    onDismiss: () -> Unit,
    onAdd: (Transaction) -> Unit
) {
    var amountStr by remember { mutableStateOf("") }
    var titleStr by remember { mutableStateOf("") }
    var selectedCat by remember { mutableStateOf(DEFAULT_CATEGORIES[0].name) }
    var selectedMethod by remember { mutableStateOf("DuitNow QR") }
    var dateStr by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(Date())) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Record Expense", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

            // Amount Field (RM)
            OutlinedTextField(
                value = amountStr,
                onValueChange = { amountStr = it },
                label = { Text("Amount (RM)") },
                prefix = { Text("RM ", fontWeight = FontWeight.Bold) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )

            // Description (OPTIONAL - as explicitly requested!)
            OutlinedTextField(
                value = titleStr,
                onValueChange = { titleStr = it },
                label = { Text("Description (Optional)") },
                placeholder = { Text("e.g. $selectedCat") },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )

            // Category Picker
            Text("Category", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DEFAULT_CATEGORIES.forEach { cat ->
                    FilterChip(
                        selected = selectedCat == cat.name,
                        onClick = { selectedCat = cat.name },
                        label = { Text(cat.name) },
                        shape = CircleShape
                    )
                }
            }

            // Method Picker
            Text("Payment Method", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            val methods = listOf("DuitNow QR", "Touch 'n Go eWallet", "MAE by Maybank", "CIMB OCTO", "Cash", "Credit Card")
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                methods.forEach { method ->
                    FilterChip(
                        selected = selectedMethod == method,
                        onClick = { selectedMethod = method },
                        label = { Text(method) },
                        shape = CircleShape
                    )
                }
            }

            // Date
            OutlinedTextField(
                value = dateStr,
                onValueChange = { dateStr = it },
                label = { Text("Date (YYYY-MM-DD)") },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(4.dp))

            Button(
                onClick = {
                    val amt = amountStr.toDoubleOrNull() ?: 0.0
                    if (amt > 0) {
                        val finalTitle = titleStr.trim().ifEmpty { selectedCat }
                        onAdd(
                            Transaction(
                                date = dateStr,
                                title = finalTitle,
                                amount = amt,
                                category = selectedCat,
                                method = selectedMethod
                            )
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = CircleShape
            ) {
                Text("Save Expense (RM)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

// -------------------------------------------------------------
// EDIT BUDGET DIALOG
// -------------------------------------------------------------
@Composable
fun EditBudgetDialog(
    currentBudget: Double,
    onDismiss: () -> Unit,
    onSave: (Double) -> Unit
) {
    var budgetStr by remember { mutableStateOf(currentBudget.toInt().toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set Monthly Budget") },
        text = {
            Column {
                Text("Enter your total monthly spending ceiling limit in Malaysian Ringgit:")
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = budgetStr,
                    onValueChange = { budgetStr = it },
                    prefix = { Text("RM ", fontWeight = FontWeight.Bold) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val b = budgetStr.toDoubleOrNull() ?: 3500.0
                    if (b > 0) onSave(b)
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

// -------------------------------------------------------------
// MONTH PICKER DIALOG & HELPERS
// -------------------------------------------------------------
fun getPreviousMonth(current: String): String {
    if (current == "all") {
        val cal = Calendar.getInstance()
        val y = cal.get(Calendar.YEAR)
        val m = cal.get(Calendar.MONTH) + 1
        return String.format(Locale.US, "%04d-%02d", y, m)
    }
    val parts = current.split("-")
    if (parts.size == 2) {
        var y = parts[0].toIntOrNull() ?: 2026
        var m = parts[1].toIntOrNull() ?: 10
        m--
        if (m < 1) {
            m = 12
            y--
        }
        return String.format(Locale.US, "%04d-%02d", y, m)
    }
    return current
}

fun getNextMonth(current: String): String {
    if (current == "all") {
        val cal = Calendar.getInstance()
        val y = cal.get(Calendar.YEAR)
        val m = cal.get(Calendar.MONTH) + 1
        return String.format(Locale.US, "%04d-%02d", y, m)
    }
    val parts = current.split("-")
    if (parts.size == 2) {
        var y = parts[0].toIntOrNull() ?: 2026
        var m = parts[1].toIntOrNull() ?: 10
        m++
        if (m > 12) {
            m = 1
            y++
        }
        return String.format(Locale.US, "%04d-%02d", y, m)
    }
    return current
}

fun formatDisplayMonth(monthStr: String): String {
    if (monthStr == "all") return "All Time"
    val parts = monthStr.split("-")
    if (parts.size == 2) {
        val y = parts[0]
        val m = parts[1].toIntOrNull() ?: return monthStr
        val monthNames = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
        if (m in 1..12) {
            return "${monthNames[m - 1]} $y"
        }
    }
    return monthStr
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthPickerDialog(
    currentMonth: String,
    transactions: List<Transaction> = emptyList(),
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    val currentCal = Calendar.getInstance()
    val currYear = currentCal.get(Calendar.YEAR)

    val years = remember(transactions, currYear) {
        val baseYears = listOf(currYear + 1, currYear, currYear - 1, currYear - 2)
        val txYears = transactions.mapNotNull { tx ->
            if (tx.date.length >= 4) tx.date.take(4).toIntOrNull() else null
        }
        (baseYears + txYears).distinct().sortedDescending()
    }

    var selectedYearTab by remember {
        val initYear = if (currentMonth.length >= 4) currentMonth.take(4).toIntOrNull() ?: currYear else currYear
        mutableIntStateOf(initYear)
    }

    val monthNames = listOf(
        "01" to "January",
        "02" to "February",
        "03" to "March",
        "04" to "April",
        "05" to "May",
        "06" to "June",
        "07" to "July",
        "08" to "August",
        "09" to "September",
        "10" to "October",
        "11" to "November",
        "12" to "December"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Select Spending Period", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // "All Time" Quick Chip
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (currentMonth == "all") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onSelect("all") }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "🕒 All Time (Everything)",
                            fontWeight = if (currentMonth == "all") FontWeight.Bold else FontWeight.Medium,
                            color = if (currentMonth == "all") MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                        )
                        if (currentMonth == "all") {
                            Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Year selector chips
                Text("Select Year:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (yr in years) {
                        val isYrSelected = selectedYearTab == yr
                        Surface(
                            shape = CircleShape,
                            color = if (isYrSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable { selectedYearTab = yr }
                        ) {
                            Text(
                                text = yr.toString(),
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelLarge,
                                color = if (isYrSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Months list for selected year
                Text("Months in $selectedYearTab:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(6.dp))

                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                ) {
                    for (pair in monthNames.reversed()) {
                        val mCode = pair.first
                        val mName = pair.second
                        val fullCode = "$selectedYearTab-$mCode"
                        val isSelected = currentMonth == fullCode
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onSelect(fullCode) }
                                .padding(vertical = 10.dp, horizontal = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "$mName $selectedYearTab",
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}
