package com.ringgitwise.app.ui

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Paint as AndroidPaint
import android.graphics.Matrix as AndroidMatrix
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import kotlin.math.roundToInt
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.ringgitwise.app.data.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.min

// Composition locals for dynamic frosted-glass Material You styling
val LocalIsWallpaperActive = staticCompositionLocalOf { false }
val LocalCardAlpha = staticCompositionLocalOf { 0.55f }

@Composable
fun glassCardColors(
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh
): CardColors {
    val isWp = LocalIsWallpaperActive.current
    val alpha = LocalCardAlpha.current
    val effectiveColor = if (isWp) containerColor.copy(alpha = alpha) else containerColor
    return CardDefaults.cardColors(containerColor = effectiveColor)
}

@Composable
fun glassCardBorder(): BorderStroke? {
    val isWp = LocalIsWallpaperActive.current
    return if (isWp) BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)) else null
}

@Composable
fun glassSurfaceColor(
    baseColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh
): Color {
    val isWp = LocalIsWallpaperActive.current
    val alpha = LocalCardAlpha.current
    return if (isWp) baseColor.copy(alpha = alpha) else baseColor
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RinggitWiseApp(repository: FinanceRepository) {
    val transactions by repository.transactions.collectAsState()
    val monthlyBudget by repository.monthlyBudget.collectAsState()
    val selectedMonth by repository.selectedMonth.collectAsState()

    // Profile & Background Customizations
    val userProfileEmoji by repository.userProfileEmoji.collectAsState()
    val hasCustomProfileImage by repository.hasCustomProfileImage.collectAsState()
    val avatarTrigger by repository.avatarUpdateTrigger.collectAsState()
    val profileScale by repository.profileScale.collectAsState()
    val profileOffsetX by repository.profileOffsetX.collectAsState()
    val profileOffsetY by repository.profileOffsetY.collectAsState()

    val hasCustomBackground by repository.hasCustomBackground.collectAsState()
    val bgTrigger by repository.bgUpdateTrigger.collectAsState()
    val customBgDim by repository.customBgDim.collectAsState()
    val customBgScale by repository.customBgScale.collectAsState()
    val customBgOffsetX by repository.customBgOffsetX.collectAsState()
    val customBgOffsetY by repository.customBgOffsetY.collectAsState()

    val chartMode by repository.chartMode.collectAsState()
    val cardAlpha by repository.cardAlpha.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var previousTab by remember { mutableIntStateOf(0) }
    var showAddSheet by remember { mutableStateOf(false) }
    var showBudgetDialog by remember { mutableStateOf(false) }
    var showClearDialog by remember { mutableStateOf(false) }
    var showMonthPicker by remember { mutableStateOf(false) }

    // Safe downsampled bitmap decoding to prevent Canvas memory crashes
    val avatarBitmap = remember(hasCustomProfileImage, avatarTrigger) {
        if (hasCustomProfileImage) {
            repository.loadSafeBitmap(repository.getProfileAvatarFile(), maxDim = 512)
        } else null
    }

    val bgBitmap = remember(hasCustomBackground, bgTrigger) {
        if (hasCustomBackground) {
            repository.loadSafeBitmap(repository.getBackgroundFile(), maxDim = 1600)
        } else null
    }

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

    var cropperSourceBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var cropperRawBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var cropperIsCircle by remember { mutableStateOf(true) }
    var cropperTitle by remember { mutableStateOf("Framing: Profile Avatar") }

    val profilePhotoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val loaded = repository.loadSafeBitmapFromUri(uri, maxDim = 2048)
            if (loaded != null) {
                cropperRawBitmap = loaded
                cropperSourceBitmap = loaded
                cropperIsCircle = true
                cropperTitle = "Framing: Profile Avatar"
            }
        }
    }

    val bgPhotoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val loaded = repository.loadSafeBitmapFromUri(uri, maxDim = 2048)
            if (loaded != null) {
                cropperRawBitmap = loaded
                cropperSourceBitmap = loaded
                cropperIsCircle = false
                cropperTitle = "Framing: App Wallpaper"
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Custom Wallpaper Layer
        if (bgBitmap != null) {
            Image(
                bitmap = bgBitmap.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            // Contrast Scrim Overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = customBgDim))
            )
        }

        CompositionLocalProvider(
            LocalIsWallpaperActive provides (bgBitmap != null),
            LocalCardAlpha provides cardAlpha
        ) {
            Scaffold(
                containerColor = if (bgBitmap != null) Color.Transparent else MaterialTheme.colorScheme.surface,
                topBar = {
                    TopAppBar(
                        navigationIcon = {
                            if (selectedTab == 4) {
                                IconButton(onClick = { selectedTab = previousTab }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        },
                        title = {
                            if (selectedTab == 4) {
                                Text("Settings & Personalization", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .clickable {
                                                previousTab = selectedTab
                                                selectedTab = 4
                                            }
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            if (avatarBitmap != null) {
                                                Image(
                                                    bitmap = avatarBitmap.asImageBitmap(),
                                                    contentDescription = "Profile Avatar",
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                            } else if (userProfileEmoji.isNotEmpty() && userProfileEmoji != "RM") {
                                                Text(userProfileEmoji, fontSize = 18.sp)
                                            } else {
                                                Text("RM", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "RinggitWise",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.clickable {
                                            previousTab = selectedTab
                                            selectedTab = 4
                                        }
                                    )
                                }
                            }
                        },
                        actions = {
                            if (selectedTab != 4) {
                                // Month Stepper with Left/Right chevrons and interactive calendar picker
                                Surface(
                                    shape = CircleShape,
                                    color = glassSurfaceColor(MaterialTheme.colorScheme.surfaceContainerHigh),
                                    border = glassCardBorder(),
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

                                IconButton(onClick = {
                                    previousTab = selectedTab
                                    selectedTab = 4
                                }) {
                                    Icon(Icons.Outlined.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = glassSurfaceColor(MaterialTheme.colorScheme.surface)
                        )
                    )
                },
                bottomBar = {
                    NavigationBar(
                        containerColor = glassSurfaceColor(MaterialTheme.colorScheme.surfaceContainer)
                    ) {
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
                            icon = { Icon(Icons.Default.DocumentScanner, contentDescription = "AI OCR Scan") },
                            label = { Text("AI Scan") }
                        )
                        NavigationBarItem(
                            selected = selectedTab == 3,
                            onClick = { selectedTab = 3 },
                            icon = { Icon(Icons.Default.History, contentDescription = "Ledger") },
                            label = { Text("Ledger") }
                        )
                        NavigationBarItem(
                            selected = selectedTab == 4,
                            onClick = {
                                previousTab = selectedTab
                                selectedTab = 4
                            },
                            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                            label = { Text("Settings") }
                        )
                    }
                },
                floatingActionButton = {
                    if (selectedTab in listOf(0, 1, 3)) {
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
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .background(if (bgBitmap != null) Color.Transparent else MaterialTheme.colorScheme.surface)
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
                            monthlyBudget = monthlyBudget,
                            selectedMonth = selectedMonth,
                            chartMode = chartMode,
                            onSelectChartMode = { repository.setChartMode(it) }
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
                        4 -> SettingsPage(
                            userProfileEmoji = userProfileEmoji,
                            hasCustomProfileImage = hasCustomProfileImage,
                            avatarBitmap = avatarBitmap,
                            onPickProfileImage = {
                                profilePhotoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            },
                            onAdjustProfileFraming = {
                                val raw = repository.getRawAvatarBitmap()
                                if (raw != null) {
                                    cropperRawBitmap = raw
                                    cropperSourceBitmap = raw
                                    cropperIsCircle = true
                                    cropperTitle = "Framing: Profile Avatar"
                                }
                            },
                            onResetProfile = {
                                repository.removeProfileImage()
                                repository.setProfileEmoji("RM")
                            },
                            onSetEmoji = { repository.setProfileEmoji(it) },
                            hasCustomBackground = hasCustomBackground,
                            customBgDim = customBgDim,
                            cardAlpha = cardAlpha,
                            onPickBackgroundImage = {
                                bgPhotoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            },
                            onAdjustBackgroundFraming = {
                                val raw = repository.getRawBackgroundBitmap()
                                if (raw != null) {
                                    cropperRawBitmap = raw
                                    cropperSourceBitmap = raw
                                    cropperIsCircle = false
                                    cropperTitle = "Framing: App Wallpaper"
                                }
                            },
                            onRemoveBackground = { repository.removeBackgroundImage() },
                            onSetBgDim = { repository.setCustomBgDim(it) },
                            onSetCardAlpha = { repository.setCardAlpha(it) },
                            monthlyBudget = monthlyBudget,
                            onEditBudget = { showBudgetDialog = true },
                            onClearAllData = { showClearDialog = true }
                        )
                    }
                }
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

    // Interactive Gesture Cropper Dialog (Avatar or Wallpaper)
    if (cropperSourceBitmap != null) {
        InteractiveImageCropperDialog(
            sourceBitmap = cropperSourceBitmap!!,
            isCircle = cropperIsCircle,
            title = cropperTitle,
            onConfirm = { croppedBmp ->
                if (cropperIsCircle) {
                    repository.saveCroppedAvatar(croppedBmp, cropperRawBitmap)
                } else {
                    repository.saveCroppedBackground(croppedBmp, cropperRawBitmap)
                }
                cropperSourceBitmap = null
                cropperRawBitmap = null
            },
            onDismiss = {
                cropperSourceBitmap = null
                cropperRawBitmap = null
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
                colors = glassCardColors(MaterialTheme.colorScheme.surfaceContainerHigh),
                border = glassCardBorder()
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
                colors = glassCardColors(MaterialTheme.colorScheme.surfaceContainerHigh),
                border = glassCardBorder()
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
                colors = glassCardColors(MaterialTheme.colorScheme.surfaceContainerHigh),
                border = glassCardBorder()
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
                colors = glassCardColors(MaterialTheme.colorScheme.surfaceContainerHigh),
                border = glassCardBorder()
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
            colors = glassCardColors(MaterialTheme.colorScheme.surfaceContainerHigh),
            border = glassCardBorder()
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
// CHARTS TAB (180° Half Pie, 360° Donut, Ranked Bars & Timeline)
// -------------------------------------------------------------
@Composable
fun ChartsTab(
    expenses: List<Transaction>,
    totalSpent: Double,
    monthlyBudget: Double,
    selectedMonth: String,
    chartMode: String = "half_pie",
    onSelectChartMode: (String) -> Unit = {}
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

    // Daily distribution (Days 1 to 31)
    val dailyMap = remember(expenses) {
        val map = sortedMapOf<Int, Double>()
        for (d in 1..31) map[d] = 0.0
        expenses.forEach { tx ->
            val dayPart = tx.date.takeLast(2).toIntOrNull()
            if (dayPart != null && dayPart in 1..31) {
                map[dayPart] = (map[dayPart] ?: 0.0) + tx.amount
            }
        }
        map
    }
    val maxDaily = remember(dailyMap) { (dailyMap.values.maxOrNull() ?: 1.0).coerceAtLeast(1.0) }
    val highestSpendDay = remember(dailyMap) { dailyMap.filter { it.value > 0 }.maxByOrNull { it.value } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Chart Style Selector Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = chartMode == "half_pie",
                onClick = { onSelectChartMode("half_pie") },
                label = { Text("⌒ 180° Half Pie", fontWeight = FontWeight.Bold) },
                shape = CircleShape,
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = glassSurfaceColor(MaterialTheme.colorScheme.surfaceContainer),
                    selectedContainerColor = if (LocalIsWallpaperActive.current) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f) else MaterialTheme.colorScheme.primaryContainer
                ),
                border = glassCardBorder()
            )
            FilterChip(
                selected = chartMode == "donut",
                onClick = { onSelectChartMode("donut") },
                label = { Text("◯ 360° Donut", fontWeight = FontWeight.Bold) },
                shape = CircleShape,
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = glassSurfaceColor(MaterialTheme.colorScheme.surfaceContainer),
                    selectedContainerColor = if (LocalIsWallpaperActive.current) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f) else MaterialTheme.colorScheme.primaryContainer
                ),
                border = glassCardBorder()
            )
            FilterChip(
                selected = chartMode == "bar",
                onClick = { onSelectChartMode("bar") },
                label = { Text("📊 Ranked Bars", fontWeight = FontWeight.Bold) },
                shape = CircleShape,
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = glassSurfaceColor(MaterialTheme.colorScheme.surfaceContainer),
                    selectedContainerColor = if (LocalIsWallpaperActive.current) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f) else MaterialTheme.colorScheme.primaryContainer
                ),
                border = glassCardBorder()
            )
            FilterChip(
                selected = chartMode == "timeline",
                onClick = { onSelectChartMode("timeline") },
                label = { Text("📅 Daily Timeline", fontWeight = FontWeight.Bold) },
                shape = CircleShape,
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = glassSurfaceColor(MaterialTheme.colorScheme.surfaceContainer),
                    selectedContainerColor = if (LocalIsWallpaperActive.current) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f) else MaterialTheme.colorScheme.primaryContainer
                ),
                border = glassCardBorder()
            )
        }

        when (chartMode) {
            "half_pie" -> {
                // 1. 180° Half Pie / Arch Chart
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = glassCardColors(MaterialTheme.colorScheme.surfaceContainerHigh),
                    border = glassCardBorder()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Category Spending Breakdown", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("180° Arch Distribution in Malaysian Ringgit", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        Spacer(modifier = Modifier.height(20.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val strokeWidth = 34.dp.toPx()
                                val diameter = min(size.width - 40.dp.toPx(), (size.height - 20.dp.toPx()) * 2)
                                val radius = diameter / 2
                                val centerX = size.width / 2
                                val centerY = size.height - 12.dp.toPx()
                                val topLeft = Offset(centerX - radius, centerY - radius)
                                val arcSize = Size(radius * 2, radius * 2)

                                // Base arc guide
                                drawArc(
                                    color = Color(0x1F888888),
                                    startAngle = 180f,
                                    sweepAngle = 180f,
                                    useCenter = false,
                                    topLeft = topLeft,
                                    size = arcSize,
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
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

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(top = 55.dp)
                            ) {
                                Text("TOTAL SPENT", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(formatRM(totalSpent), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                                val countLabel = if (expenses.size == 1) "1 expense logged" else "${expenses.size} expenses logged"
                                Text(countLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Category Chips
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
            }

            "donut" -> {
                // 2. 360° Full Donut Chart
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = glassCardColors(MaterialTheme.colorScheme.surfaceContainerHigh),
                    border = glassCardBorder()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Full 360° Donut Distribution", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Complete budget allocation across all categories", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        Spacer(modifier = Modifier.height(20.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(240.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val strokeWidth = 30.dp.toPx()
                                val diameter = min(size.width, size.height) - strokeWidth * 2 - 16.dp.toPx()
                                val radius = diameter / 2
                                val centerX = size.width / 2
                                val centerY = size.height / 2
                                val topLeft = Offset(centerX - radius, centerY - radius)
                                val arcSize = Size(radius * 2, radius * 2)

                                // Base ring track
                                drawArc(
                                    color = Color(0x1F888888),
                                    startAngle = 0f,
                                    sweepAngle = 360f,
                                    useCenter = false,
                                    topLeft = topLeft,
                                    size = arcSize,
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                                )

                                if (totalSpent > 0 && catMap.isNotEmpty()) {
                                    var currentAngle = -90f // Start from 12 o'clock
                                    val gap = if (catMap.size > 1) 3f else 0f
                                    val totalGaps = catMap.size * gap
                                    val availableSpan = 360f - totalGaps

                                    catMap.forEach { (catName, amount) ->
                                        val pct = (amount / totalSpent).toFloat()
                                        val sweep = (pct * availableSpan).coerceAtLeast(2f)
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

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("TOTAL SPENT", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(formatRM(totalSpent), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                                val pctBudget = if (monthlyBudget > 0) ((totalSpent / monthlyBudget) * 100).toInt() else 0
                                Text("$pctBudget% of budget", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Category Chips
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
            }

            "bar" -> {
                // 3. Ranked Category Progress Bars
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = glassCardColors(MaterialTheme.colorScheme.surfaceContainerHigh),
                    border = glassCardBorder()
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Ranked Spending by Category", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text("${catMap.size} active categories in $selectedMonth", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text(formatRM(totalSpent), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                        }

                        if (catMap.isEmpty()) {
                            Text("No expenses logged for this month.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            catMap.forEachIndexed { idx, (catName, amount) ->
                                val pct = if (totalSpent > 0) (amount / totalSpent).toFloat() else 0f
                                val pctInt = (pct * 100).toInt()
                                val catObj = DEFAULT_CATEGORIES.find { it.name.equals(catName, true) }
                                val color = catObj?.color ?: Color(0xFF54B3D6)
                                val count = expenses.count { it.category.equals(catName, true) }

                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = glassSurfaceColor(MaterialTheme.colorScheme.surfaceContainer),
                                    border = glassCardBorder(),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text("#${idx + 1}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(color))
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(catName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                            }
                                            Text(formatRM(amount), fontWeight = FontWeight.Black, style = MaterialTheme.typography.bodyMedium)
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        // Linear Progress Bar
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(8.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth(pct.coerceIn(0.01f, 1f))
                                                    .fillMaxHeight()
                                                    .clip(CircleShape)
                                                    .background(color)
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("$count transaction(s)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text("$pctInt% of monthly spend", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = color)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            "timeline" -> {
                // 4. Daily Spending Timeline
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = glassCardColors(MaterialTheme.colorScheme.surfaceContainerHigh),
                    border = glassCardBorder()
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Column {
                            Text("Daily Spending Burn Rate", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Spending spikes across the 31 days of $selectedMonth", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        if (highestSpendDay != null && highestSpendDay.value > 0) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Peak Spending: Day ${highestSpendDay.key}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                    Text(formatRM(highestSpendDay.value), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                }
                            }
                        }

                        // Daily Timeline Canvas
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .padding(vertical = 8.dp)
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val barCount = 31
                                val availableW = size.width
                                val barW = (availableW / barCount) * 0.7f
                                val spacing = availableW / barCount
                                val maxH = size.height - 16.dp.toPx()

                                for (d in 1..barCount) {
                                    val spentOnDay = dailyMap[d] ?: 0.0
                                    val barH = if (maxDaily > 0) ((spentOnDay / maxDaily).toFloat() * maxH).coerceAtLeast(4.dp.toPx()) else 4.dp.toPx()
                                    val leftX = (d - 1) * spacing + (spacing - barW) / 2
                                    val topY = size.height - barH

                                    val isPeak = highestSpendDay != null && d == highestSpendDay.key && spentOnDay > 0
                                    val barColor = if (isPeak) Color(0xFFF43F5E) else if (spentOnDay > 0) Color(0xFF10B981) else Color(0x22888888)

                                    drawRoundRect(
                                        color = barColor,
                                        topLeft = Offset(leftX, topY),
                                        size = Size(barW, barH),
                                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx())
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Day 1", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Day 15", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Day 31", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(60.dp))
    }
}

// -------------------------------------------------------------
// TNG / BANK STATEMENT & GOOGLE ML KIT OCR SCANNER TAB
// -------------------------------------------------------------
@Composable
fun ImportTab(onImportTransactions: (List<Transaction>) -> Unit) {
    val context = LocalContext.current
    val recognizer = remember { TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS) }

    var isScanning by remember { mutableStateOf(false) }
    var scanStatus by remember { mutableStateOf("") }
    var rawText by remember { mutableStateOf("") }
    var parsedList by remember { mutableStateOf<List<Transaction>>(emptyList()) }
    var activeMode by remember { mutableIntStateOf(0) } // 0: AI OCR Scanner, 1: Paste Text
    var showRawText by remember { mutableStateOf(false) }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
        if (uri != null) {
            try {
                isScanning = true
                scanStatus = "Scanning image with Google ML Kit OCR..."
                val image = InputImage.fromFilePath(context, uri)
                recognizer.process(image)
                    .addOnSuccessListener { visionText ->
                        val text = visionText.text
                        rawText = text
                        val results = StatementParser.parseFinancialText(text)
                        parsedList = results
                        isScanning = false
                        scanStatus = if (results.isEmpty()) {
                            "OCR finished: No financial transactions detected. You can review the raw text below."
                        } else {
                            "Successfully parsed ${results.size} transaction(s)!"
                        }
                    }
                    .addOnFailureListener { e ->
                        isScanning = false
                        scanStatus = "OCR Scan error: ${e.localizedMessage}"
                    }
            } catch (e: Exception) {
                isScanning = false
                scanStatus = "Could not open image: ${e.localizedMessage}"
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
        if (bitmap != null) {
            try {
                isScanning = true
                scanStatus = "Scanning photo with Google ML Kit OCR..."
                val image = InputImage.fromBitmap(bitmap, 0)
                recognizer.process(image)
                    .addOnSuccessListener { visionText ->
                        val text = visionText.text
                        rawText = text
                        val results = StatementParser.parseFinancialText(text)
                        parsedList = results
                        isScanning = false
                        scanStatus = if (results.isEmpty()) {
                            "OCR finished: No financial transactions detected. You can review the raw text below."
                        } else {
                            "Successfully parsed ${results.size} transaction(s)!"
                        }
                    }
                    .addOnFailureListener { e ->
                        isScanning = false
                        scanStatus = "Camera OCR error: ${e.localizedMessage}"
                    }
            } catch (e: Exception) {
                isScanning = false
                scanStatus = "Camera capture error: ${e.localizedMessage}"
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Mode Selector Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = activeMode == 0,
                onClick = { activeMode = 0 },
                label = { Text("📷 AI OCR Scanner", fontWeight = FontWeight.Bold) },
                shape = CircleShape,
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = glassSurfaceColor(MaterialTheme.colorScheme.surfaceContainer),
                    selectedContainerColor = if (LocalIsWallpaperActive.current) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f) else MaterialTheme.colorScheme.primaryContainer
                ),
                border = glassCardBorder(),
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = activeMode == 1,
                onClick = { activeMode = 1 },
                label = { Text("📝 Paste Statement", fontWeight = FontWeight.Bold) },
                shape = CircleShape,
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = glassSurfaceColor(MaterialTheme.colorScheme.surfaceContainer),
                    selectedContainerColor = if (LocalIsWallpaperActive.current) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f) else MaterialTheme.colorScheme.primaryContainer
                ),
                border = glassCardBorder(),
                modifier = Modifier.weight(1f)
            )
        }

        if (activeMode == 0) {
            // AI OCR Scanner Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = glassCardColors(MaterialTheme.colorScheme.surfaceContainerHigh),
                border = glassCardBorder()
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.DocumentScanner, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("AI Receipt & Statement OCR", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Fast on-device Google ML Kit scanner", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        }
                    }

                    Text(
                        "Scan physical paper receipts, DuitNow QR slips, Touch 'n Go eWallet screenshots, or bills. The app recognizes amounts, dates, and merchants automatically.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Action buttons: Camera & Gallery
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { cameraLauncher.launch(null) },
                            modifier = Modifier.weight(1f).height(50.dp),
                            shape = RoundedCornerShape(16.dp),
                            enabled = !isScanning
                        ) {
                            Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Camera", fontWeight = FontWeight.Bold)
                        }

                        FilledTonalButton(
                            onClick = {
                                galleryLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier.weight(1f).height(50.dp),
                            shape = RoundedCornerShape(16.dp),
                            enabled = !isScanning
                        ) {
                            Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Gallery", fontWeight = FontWeight.Bold)
                        }
                    }

                    // Scanning State
                    if (isScanning) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceContainer, RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(scanStatus, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                        }
                    } else if (scanStatus.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = scanStatus,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            }
        } else {
            // Text Paste Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = glassCardColors(MaterialTheme.colorScheme.surfaceContainerHigh),
                border = glassCardBorder()
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ContentPaste, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Touch 'n Go & Bank Statement Text", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }

                    Text(
                        "Paste raw report lines from TNG eWallet, APCard/APSpace, or online banking reports:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = rawText,
                        onValueChange = {
                            rawText = it
                            parsedList = StatementParser.parseFinancialText(it)
                        },
                        modifier = Modifier.fillMaxWidth().height(150.dp),
                        shape = RoundedCornerShape(16.dp),
                        placeholder = {
                            Text(
                                "e.g.\n15/10/2026 14:23:10 Successful Payment Restoran Ali Food Court -15.50 120.40\n16/10/2026 08:30:15 Successful RFID PLUS TOLL SUBANG -2.50 117.90\n18 2026-10-16 12:48:04 Parking Fee 3.94",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )

                    Button(
                        onClick = { parsedList = StatementParser.parseFinancialText(rawText) },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = CircleShape
                    ) {
                        Text("Parse Statement Text", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Detected Transactions Section
        if (parsedList.isNotEmpty()) {
            val totalBatch = parsedList.sumOf { it.amount }
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Detected Transactions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text("${parsedList.size} item(s) • Total: ${formatRM(totalBatch)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }

                    Button(
                        onClick = {
                            onImportTransactions(parsedList)
                            rawText = ""
                            parsedList = emptyList()
                            scanStatus = "Imported successfully into Ledger!"
                        },
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Import All", fontWeight = FontWeight.Bold)
                    }
                }
            }

            parsedList.forEach { tx ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = glassCardColors(MaterialTheme.colorScheme.surfaceContainer),
                    border = glassCardBorder()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(tx.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                "${tx.date} • ${tx.category} • ${tx.method}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            formatRM(tx.amount),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // Collapsible Raw Text Inspector
        if (rawText.isNotEmpty()) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showRawText = !showRawText }
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Raw OCR / Statement Text", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        Text(if (showRawText) "Hide ▲" else "View ▼", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    }
                    if (showRawText) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = rawText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
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
        colors = glassCardColors(MaterialTheme.colorScheme.surfaceContainerHigh),
        border = glassCardBorder()
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

// -------------------------------------------------------------
// INTERACTIVE GESTURE-BASED IMAGE CROPPER DIALOG (MODERN VIEWPORT)
// -------------------------------------------------------------
@Composable
fun InteractiveImageCropperDialog(
    sourceBitmap: Bitmap,
    isCircle: Boolean,
    title: String,
    onConfirm: (Bitmap) -> Unit,
    onDismiss: () -> Unit
) {
    var scale by remember { mutableFloatStateOf(1.0f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true
        )
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            val canvasWidth = constraints.maxWidth.toFloat()
            val canvasHeight = constraints.maxHeight.toFloat()

            // Calculate framing window / cutout bounds in exact canvas pixels
            val cutoutRect = remember(canvasWidth, canvasHeight, isCircle) {
                if (isCircle) {
                    val size = minOf(canvasWidth, canvasHeight) * 0.72f
                    val left = (canvasWidth - size) / 2f
                    val top = (canvasHeight - size) / 2f
                    Rect(left, top, left + size, top + size)
                } else {
                    val screenAspect = canvasWidth / canvasHeight
                    var cutH = canvasHeight * 0.70f
                    var cutW = cutH * screenAspect
                    if (cutW > canvasWidth * 0.85f) {
                        cutW = canvasWidth * 0.85f
                        cutH = cutW / screenAspect
                    }
                    val left = (canvasWidth - cutW) / 2f
                    val top = (canvasHeight - cutH) / 2f
                    Rect(left, top, left + cutW, top + cutH)
                }
            }

            // Base scale so the source image initially covers the entire crop frame
            val baseScale = remember(sourceBitmap, cutoutRect) {
                maxOf(
                    cutoutRect.width / sourceBitmap.width.toFloat(),
                    cutoutRect.height / sourceBitmap.height.toFloat()
                )
            }

            // Exact layout geometry in canvas pixels
            val totalScale = baseScale * scale
            val imgW = sourceBitmap.width * totalScale
            val imgH = sourceBitmap.height * totalScale
            val imgLeft = cutoutRect.center.x - (imgW / 2f) + offset.x
            val imgTop = cutoutRect.center.y - (imgH / 2f) + offset.y

            // Single unified Canvas: direct drawing prevents any ContentScale/graphicsLayer offset bugs!
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(0.5f, 5.0f)
                            offset = Offset(offset.x + pan.x, offset.y + pan.y)
                        }
                    }
            ) {
                // 1. Draw source image at exact pixel coordinates
                drawImage(
                    image = sourceBitmap.asImageBitmap(),
                    dstOffset = IntOffset(imgLeft.roundToInt(), imgTop.roundToInt()),
                    dstSize = IntSize(imgW.roundToInt(), imgH.roundToInt()),
                    filterQuality = FilterQuality.Low
                )

                // 2. Draw scrim & cutout
                val outerPath = Path().apply {
                    addRect(Rect(0f, 0f, size.width, size.height))
                }
                val cutoutPath = Path().apply {
                    if (isCircle) {
                        addOval(cutoutRect)
                    } else {
                        addRoundRect(RoundRect(cutoutRect, CornerRadius(24f, 24f)))
                    }
                }
                val scrimPath = Path().apply {
                    op(outerPath, cutoutPath, PathOperation.Difference)
                }
                // Dimmed backdrop outside cutout
                drawPath(scrimPath, color = Color.Black.copy(alpha = 0.72f))

                // 3. Draw border outline & alignment guides
                if (isCircle) {
                    drawCircle(
                        color = Color.White.copy(alpha = 0.9f),
                        radius = cutoutRect.width / 2f,
                        center = cutoutRect.center,
                        style = Stroke(width = 2.5.dp.toPx())
                    )
                    val r = cutoutRect.width / 2f
                    val c = cutoutRect.center
                    drawLine(
                        color = Color.White.copy(alpha = 0.25f),
                        start = Offset(c.x - r * 0.45f, c.y),
                        end = Offset(c.x + r * 0.45f, c.y),
                        strokeWidth = 1.dp.toPx()
                    )
                    drawLine(
                        color = Color.White.copy(alpha = 0.25f),
                        start = Offset(c.x, c.y - r * 0.45f),
                        end = Offset(c.x, c.y + r * 0.45f),
                        strokeWidth = 1.dp.toPx()
                    )
                } else {
                    drawRoundRect(
                        color = Color.White.copy(alpha = 0.9f),
                        topLeft = cutoutRect.topLeft,
                        size = cutoutRect.size,
                        cornerRadius = CornerRadius(24f, 24f),
                        style = Stroke(width = 2.5.dp.toPx())
                    )
                    val stepX = cutoutRect.width / 3f
                    val stepY = cutoutRect.height / 3f
                    for (i in 1..2) {
                        drawLine(
                            color = Color.White.copy(alpha = 0.22f),
                            start = Offset(cutoutRect.left + stepX * i, cutoutRect.top),
                            end = Offset(cutoutRect.left + stepX * i, cutoutRect.bottom),
                            strokeWidth = 1.dp.toPx()
                        )
                        drawLine(
                            color = Color.White.copy(alpha = 0.22f),
                            start = Offset(cutoutRect.left, cutoutRect.top + stepY * i),
                            end = Offset(cutoutRect.right, cutoutRect.top + stepY * i),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                }
            }

            // Top Bar
            Surface(
                color = Color.Black.copy(alpha = 0.65f),
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                "🖐️ Drag to pan • 🤏 Pinch to zoom",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.75f)
                            )
                        }
                    }
                    TextButton(onClick = {
                        scale = 1.0f
                        offset = Offset.Zero
                    }) {
                        Text("Reset", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Bottom Action Bar
            Surface(
                color = Color.Black.copy(alpha = 0.75f),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Zoom Slider for fine thumb adjustments
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.ZoomOut, contentDescription = null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
                        Slider(
                            value = scale,
                            onValueChange = { scale = it },
                            valueRange = 0.8f..4.0f,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 10.dp),
                            colors = SliderDefaults.colors(
                                thumbColor = Color.White,
                                activeTrackColor = MaterialTheme.colorScheme.primary
                            )
                        )
                        Icon(Icons.Default.ZoomIn, contentDescription = null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            shape = CircleShape,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                        ) {
                            Text("Cancel")
                        }

                        Button(
                            onClick = {
                                val targetW: Int
                                val targetH: Int
                                if (isCircle) {
                                    targetW = 512
                                    targetH = 512
                                } else {
                                    targetW = 1080
                                    targetH = (1080f * (cutoutRect.height / cutoutRect.width)).toInt().coerceIn(1080, 2400)
                                }

                                // 100% exact pixel correspondence between Canvas dstOffset and crop matrix!
                                val srcLeft = (cutoutRect.left - imgLeft) / totalScale
                                val srcTop = (cutoutRect.top - imgTop) / totalScale
                                val srcWidth = cutoutRect.width / totalScale
                                val srcHeight = cutoutRect.height / totalScale

                                try {
                                    val cropped = Bitmap.createBitmap(targetW, targetH, Bitmap.Config.ARGB_8888)
                                    val canvas = AndroidCanvas(cropped)
                                    val matrix = AndroidMatrix().apply {
                                        postTranslate(-srcLeft, -srcTop)
                                        postScale(targetW.toFloat() / srcWidth, targetH.toFloat() / srcHeight)
                                    }
                                    val paint = AndroidPaint(AndroidPaint.FILTER_BITMAP_FLAG or AndroidPaint.ANTI_ALIAS_FLAG)
                                    canvas.drawBitmap(sourceBitmap, matrix, paint)
                                    onConfirm(cropped)
                                } catch (_: Throwable) {
                                    onConfirm(sourceBitmap)
                                }
                            },
                            modifier = Modifier.weight(1.3f),
                            shape = CircleShape
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isCircle) "Set Avatar" else "Set Wallpaper", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// SETTINGS & PERSONALIZATION PAGE (FULL SCREEN DESTINATION)
// -------------------------------------------------------------
@Composable
fun SettingsPage(
    userProfileEmoji: String,
    hasCustomProfileImage: Boolean,
    avatarBitmap: Bitmap?,
    onPickProfileImage: () -> Unit,
    onAdjustProfileFraming: () -> Unit,
    onResetProfile: () -> Unit,
    onSetEmoji: (String) -> Unit,
    hasCustomBackground: Boolean,
    customBgDim: Float,
    cardAlpha: Float,
    onPickBackgroundImage: () -> Unit,
    onAdjustBackgroundFraming: () -> Unit,
    onRemoveBackground: () -> Unit,
    onSetBgDim: (Float) -> Unit,
    onSetCardAlpha: (Float) -> Unit,
    monthlyBudget: Double,
    onEditBudget: () -> Unit,
    onClearAllData: () -> Unit
) {
    val quickEmojis = listOf("💰", "🦁", "🇲🇾", "🚀", "💳", "📈", "🍔", "☕", "🚗", "🏠", "🐱", "✨", "RM")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section 1: Profile Avatar & Identity Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = glassCardColors(MaterialTheme.colorScheme.surfaceContainerHigh),
            border = glassCardBorder()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.AccountCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text("Profile Avatar & App Logo", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        border = BorderStroke(2.5.dp, MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (avatarBitmap != null) {
                                Image(
                                    bitmap = avatarBitmap.asImageBitmap(),
                                    contentDescription = "Avatar Preview",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else if (userProfileEmoji.isNotEmpty() && userProfileEmoji != "RM") {
                                Text(userProfileEmoji, fontSize = 34.sp)
                            } else {
                                Text("RM", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (avatarBitmap != null) "Custom Photo Avatar" else if (userProfileEmoji != "RM") "Emoji: $userProfileEmoji" else "Default RinggitWise Logo",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Displayed on the top bar and personal ledger",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Avatar Actions
                if (hasCustomProfileImage && avatarBitmap != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onAdjustProfileFraming,
                            modifier = Modifier.weight(1.2f),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.Crop, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Adjust Framing")
                        }

                        OutlinedButton(
                            onClick = onPickProfileImage,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("Change Photo")
                        }
                    }

                    TextButton(
                        onClick = onResetProfile,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Reset to Default RM Logo", color = MaterialTheme.colorScheme.error)
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onPickProfileImage,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Upload Photo")
                        }

                        FilledTonalButton(
                            onClick = onResetProfile,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("Reset Logo")
                        }
                    }
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                Text("Or select a quick emoji emblem:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    quickEmojis.forEach { em ->
                        val isSelected = avatarBitmap == null && userProfileEmoji == em
                        Surface(
                            shape = CircleShape,
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else glassSurfaceColor(MaterialTheme.colorScheme.surfaceContainer),
                            border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else glassCardBorder(),
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .clickable { onSetEmoji(em) }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (em == "RM") {
                                    Text("RM", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                                } else {
                                    Text(em, fontSize = 22.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section 2: Custom Wallpaper & Glassmorphism Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = glassCardColors(MaterialTheme.colorScheme.surfaceContainerHigh),
            border = glassCardBorder()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Wallpaper, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text("App Wallpaper & Material You Glass", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }

                Text(
                    "Set any photo or art from your gallery as the full app background, and fine-tune card translucency.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (hasCustomBackground) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onAdjustBackgroundFraming,
                            modifier = Modifier.weight(1.2f),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.Crop, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Adjust Framing")
                        }

                        OutlinedButton(
                            onClick = onPickBackgroundImage,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("Change Image")
                        }
                    }

                    FilledTonalButton(
                        onClick = onRemoveBackground,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Text("Remove Wallpaper", color = MaterialTheme.colorScheme.onErrorContainer)
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                    // Material You Elements Transparency Slider
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = glassSurfaceColor(MaterialTheme.colorScheme.surfaceContainer),
                        border = glassCardBorder(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.BlurOn, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Card & UI Translucency", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                }
                                Text(
                                    "${((1f - cardAlpha) * 100).toInt()}% Glass",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Text(
                                "Makes Material You cards translucent so your wallpaper shines through beautifully.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Slider(
                                value = cardAlpha,
                                onValueChange = onSetCardAlpha,
                                valueRange = 0.15f..0.95f
                            )
                        }
                    }

                    // Background Darkness / Contrast Scrim Slider
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = glassSurfaceColor(MaterialTheme.colorScheme.surfaceContainer),
                        border = glassCardBorder(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.BrightnessMedium, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Wallpaper Darkness / Dim", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                }
                                Text(
                                    "${(customBgDim * 100).toInt()}% Dim",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                "Darkens the wallpaper behind cards to ensure white text remains sharp and readable.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Slider(
                                value = customBgDim,
                                onValueChange = onSetBgDim,
                                valueRange = 0.35f..0.92f
                            )
                        }
                    }
                } else {
                    Button(
                        onClick = onPickBackgroundImage,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Wallpaper, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Set Custom Wallpaper")
                    }
                }
            }
        }

        // Section 3: Budget Ceiling Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = glassCardColors(MaterialTheme.colorScheme.surfaceContainerHigh),
            border = glassCardBorder()
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Savings, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text("Monthly Spending Ceiling", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Current Budget Limit", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(formatRM(monthlyBudget), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }

                    FilledTonalButton(
                        onClick = onEditBudget,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Edit Limit")
                    }
                }
            }
        }

        // Section 4: Data Management & Reset
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = glassCardColors(MaterialTheme.colorScheme.surfaceContainerHigh),
            border = glassCardBorder()
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Storage, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Text("Data Management", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onClearAllData,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Outlined.DeleteSweep, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Clear All Recorded Expenses")
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "RinggitWise Native v1.0 • Material You • Made for Malaysia",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(24.dp))
    }
}
