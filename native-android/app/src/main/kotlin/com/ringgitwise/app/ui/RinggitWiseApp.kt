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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
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
import android.Manifest
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import android.content.Intent
import android.content.ClipboardManager
import android.content.ClipData
import android.content.Context
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import kotlin.math.roundToInt
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.ringgitwise.app.BuildConfig
import com.ringgitwise.app.data.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.min
import kotlinx.coroutines.launch

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

enum class LaunchAction { NONE, ADD_EXPENSE, OPEN_BILLS }

private enum class SubScreen(val title: String) {
    NONE(""),
    BILLS("Bills & Subscriptions"),
    GOALS("Savings Goals"),
    CATEGORIES("Categories"),
    EXPORT("Export & Backup"),
    SCAN("Scan & AI Assistant"),
    SETTINGS("Settings")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RinggitWiseApp(
    repository: FinanceRepository,
    launchAction: LaunchAction = LaunchAction.NONE,
    onLaunchActionConsumed: () -> Unit = {}
) {
    val transactions by repository.transactions.collectAsState()
    val monthlyBudget by repository.monthlyBudget.collectAsState()
    val selectedMonth by repository.selectedMonth.collectAsState()
    val bills by repository.bills.collectAsState()
    val goals by repository.goals.collectAsState()
    val customCategories by repository.customCategories.collectAsState()
    val themeMode by repository.themeMode.collectAsState()
    val billRemindersEnabled by repository.billRemindersEnabled.collectAsState()

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
    val geminiApiKey by repository.geminiApiKey.collectAsState()
    val geminiModel by repository.geminiModel.collectAsState()
    val userDuitNowInfo by repository.userDuitNowInfo.collectAsState()
    val autoCheckUpdates by repository.autoCheckUpdates.collectAsState()
    val githubRepoSlug by repository.githubRepoSlug.collectAsState()
    val availableUpdate by repository.availableUpdate.collectAsState()
    var activeUpdateInfo by remember { mutableStateOf<AppUpdateInfo?>(null) }

    // 4 primary tabs: 0 = Home, 1 = Activity, 2 = Insights, 3 = More
    var selectedTab by remember { mutableIntStateOf(0) }
    var subScreen by remember { mutableStateOf(SubScreen.NONE) }
    var initialOpenGoalId by remember { mutableStateOf<String?>(null) }
    var showAddMenuSheet by remember { mutableStateOf(false) }
    var showAddSheet by remember { mutableStateOf(false) }
    var showBudgetDialog by remember { mutableStateOf(false) }
    var showClearDialog by remember { mutableStateOf(false) }
    var showMonthPicker by remember { mutableStateOf(false) }
    var initialLedgerFilter by remember { mutableStateOf("All") }
    var claimShareTarget by remember { mutableStateOf<Pair<String, List<Transaction>>?>(null) }
    var addSheetInitialCategory by remember { mutableStateOf<String?>(null) }

    val notifPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        repository.setBillRemindersEnabled(granted)
    }

    LaunchedEffect(launchAction) {
        when (launchAction) {
            LaunchAction.ADD_EXPENSE -> {
                showAddSheet = true
                onLaunchActionConsumed()
            }
            LaunchAction.OPEN_BILLS -> {
                subScreen = SubScreen.BILLS
                onLaunchActionConsumed()
            }
            LaunchAction.NONE -> {}
        }
    }

    BackHandler(enabled = subScreen != SubScreen.NONE) {
        subScreen = SubScreen.NONE
    }

    val tolongBeliPendingList = remember(transactions) {
        transactions.filter { (it.isHelpBuy || it.category.contains("Tolong Beli", ignoreCase = true)) && !it.isSettled }
    }
    val pendingClaimsTotal = remember(tolongBeliPendingList) { tolongBeliPendingList.sumOf { it.amount } }
    val pendingClaimsCount = tolongBeliPendingList.size

    val allRecipients = remember(transactions) {
        transactions.mapNotNull {
            val r = it.claimRecipient.trim()
            if (r.isNotBlank()) r else null
        }.distinct()
    }

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

    val todayIso = remember { SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(Date()) }
    val todaySpent = remember(expenses, todayIso) {
        expenses.filter { it.date == todayIso }.sumOf { it.amount }
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
    val daysRemaining = (daysInMonth - currentDay + 1).coerceAtLeast(1)
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

    val currentHour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
    val timeGreeting = remember(currentHour) {
        when (currentHour) {
            in 5..11 -> "Good morning"
            in 12..17 -> "Good afternoon"
            in 18..21 -> "Good evening"
            else -> "Welcome back"
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
                containerColor = if (bgBitmap != null) Color.Transparent else MaterialTheme.colorScheme.background,
                topBar = {
                    if (subScreen != SubScreen.NONE) {
                        SubScreenTopBar(
                            title = subScreen.title,
                            onBack = { subScreen = SubScreen.NONE }
                        )
                    } else {
                        TopAppBar(
                            navigationIcon = {
                                Box(
                                    modifier = Modifier
                                        .padding(start = 12.dp, end = 8.dp)
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer)
                                        .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                        .clickable { subScreen = SubScreen.SETTINGS },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (avatarBitmap != null) {
                                        Image(
                                            bitmap = avatarBitmap.asImageBitmap(),
                                            contentDescription = "Profile Avatar",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize().clip(CircleShape)
                                        )
                                    } else if (userProfileEmoji.isNotEmpty() && userProfileEmoji != "RM") {
                                        Text(userProfileEmoji, fontSize = 18.sp)
                                    } else {
                                        Text("RM", fontWeight = FontWeight.Black, fontSize = 13.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                    }
                                }
                            },
                            title = {
                                Column(modifier = Modifier.clickable { subScreen = SubScreen.SETTINGS }) {
                                    Text(
                                        text = timeGreeting,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "RinggitWise",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = (-0.3).sp
                                    )
                                }
                            },
                            actions = {
                                Surface(
                                    shape = CircleShape,
                                    color = glassSurfaceColor(MaterialTheme.colorScheme.surfaceContainerHigh),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                    modifier = Modifier.padding(end = 8.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 2.dp, vertical = 2.dp)
                                    ) {
                                        IconButton(
                                            onClick = { repository.selectMonth(getPreviousMonth(selectedMonth)) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.ChevronLeft,
                                                contentDescription = "Previous Month",
                                                modifier = Modifier.size(18.dp),
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .clip(CircleShape)
                                                .clickable { showMonthPicker = true }
                                                .padding(horizontal = 8.dp, vertical = 6.dp)
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
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.ChevronRight,
                                                contentDescription = "Next Month",
                                                modifier = Modifier.size(18.dp),
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = glassSurfaceColor(MaterialTheme.colorScheme.background)
                            )
                        )
                    }
                },
                bottomBar = {
                    if (subScreen == SubScreen.NONE) {
                        FintechBottomBar(
                            selectedTab = selectedTab,
                            onSelectTab = { selectedTab = it },
                            onCenterAddClick = { showAddMenuSheet = true }
                        )
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .background(if (bgBitmap != null) Color.Transparent else MaterialTheme.colorScheme.background)
                ) {
                    when (subScreen) {
                        SubScreen.BILLS -> BillsScreen(
                            bills = bills,
                            customCategories = customCategories,
                            remindersEnabled = billRemindersEnabled,
                            onToggleReminders = { enable ->
                                if (enable && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    notifPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    repository.setBillRemindersEnabled(enable)
                                }
                            },
                            onUpsert = { repository.upsertBill(it) },
                            onDelete = { repository.deleteBill(it) },
                            onMarkPaid = { repository.markBillPaid(it, logExpense = true) },
                            onUnmarkPaid = { repository.unmarkBillPaid(it) },
                            onAddCustomCategory = { repository.upsertCustomCategory(it) }
                        )
                        SubScreen.GOALS -> GoalsScreen(
                            goals = goals,
                            initialOpenGoalId = initialOpenGoalId,
                            onInitialConsumed = { initialOpenGoalId = null },
                            onUpsert = { repository.upsertGoal(it) },
                            onDelete = { repository.deleteGoal(it) },
                            onContribute = { id, amt -> repository.contributeToGoal(id, amt) }
                        )
                        SubScreen.CATEGORIES -> CategoriesScreen(
                            customCategories = customCategories,
                            transactions = transactions,
                            onUpsert = { cat, prevName -> repository.upsertCustomCategory(cat, prevName) },
                            onDelete = { repository.deleteCustomCategory(it) }
                        )
                        SubScreen.EXPORT -> ExportScreen(
                            repository = repository,
                            allTransactions = transactions,
                            selectedMonth = selectedMonth,
                            monthlyBudget = monthlyBudget
                        )
                        SubScreen.SCAN -> ImportTab(
                            repository = repository,
                            onImportTransactions = { list ->
                                repository.addMultipleTransactions(list)
                                subScreen = SubScreen.NONE
                                selectedTab = 1
                            },
                            onNavigateToSettings = {
                                subScreen = SubScreen.SETTINGS
                            }
                        )
                        SubScreen.SETTINGS -> SettingsPage(
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
                            geminiApiKey = geminiApiKey,
                            geminiModel = geminiModel,
                            onSetGeminiApiKey = { repository.setGeminiApiKey(it) },
                            onSetGeminiModel = { repository.setGeminiModel(it) },
                            onTestGeminiKey = { key, model -> repository.testGeminiApiKey(key, model) },
                            userDuitNowInfo = userDuitNowInfo,
                            onSetDuitNowInfo = { repository.setDuitNowInfo(it) },
                            monthlyBudget = monthlyBudget,
                            onEditBudget = { showBudgetDialog = true },
                            onClearAllData = { showClearDialog = true },
                            autoCheckUpdates = autoCheckUpdates,
                            onSetAutoCheckUpdates = { repository.setAutoCheckUpdates(it) },
                            githubRepoSlug = githubRepoSlug,
                            onSetGithubRepoSlug = { repository.setGithubRepoSlug(it) },
                            availableUpdate = availableUpdate,
                            onCheckForUpdates = { repository.checkForUpdates() },
                            onOpenUpdateModal = { activeUpdateInfo = it }
                        )
                        SubScreen.NONE -> when (selectedTab) {
                            0 -> HomeScreen(
                                selectedMonth = selectedMonth,
                                totalSpent = totalSpent,
                                monthlyBudget = monthlyBudget,
                                todaySpent = todaySpent,
                                dailySafeSpend = dailySafeSpend,
                                daysRemaining = daysRemaining,
                                recentTransactions = activeMonthExpenses.take(5),
                                bills = bills,
                                goals = goals,
                                pendingClaimsTotal = pendingClaimsTotal,
                                pendingClaimsCount = pendingClaimsCount,
                                availableUpdate = availableUpdate,
                                onOpenUpdate = { activeUpdateInfo = availableUpdate },
                                onDismissUpdate = { repository.dismissUpdate(availableUpdate?.latestVersion) },
                                onEditBudget = { showBudgetDialog = true },
                                onAddExpense = { showAddSheet = true },
                                onScan = { subScreen = SubScreen.SCAN },
                                onOpenBills = { subScreen = SubScreen.BILLS },
                                onOpenGoals = { subScreen = SubScreen.GOALS },
                                onPayBill = { repository.markBillPaid(it, logExpense = true) },
                                onOpenGoal = { goal ->
                                    initialOpenGoalId = goal.id
                                    subScreen = SubScreen.GOALS
                                },
                                onOpenActivity = {
                                    initialLedgerFilter = "All"
                                    selectedTab = 1
                                },
                                onOpenClaims = {
                                    initialLedgerFilter = "Tolong Beli (Help Buy)"
                                    selectedTab = 1
                                }
                            )
                            1 -> LedgerTab(
                                transactions = activeMonthExpenses,
                                customCategories = customCategories,
                                duitNowInfo = userDuitNowInfo,
                                initialFilter = initialLedgerFilter,
                                onDelete = { repository.deleteTransaction(it) },
                                onToggleSettled = { repository.toggleTransactionSettled(it) },
                                onAddCustomCategory = { repository.upsertCustomCategory(it) },
                                onManageCategories = { subScreen = SubScreen.CATEGORIES },
                                onShareClaim = { recip, txs ->
                                    claimShareTarget = Pair(recip, txs)
                                }
                            )
                            2 -> ChartsTab(
                                allTransactions = transactions,
                                expenses = activeMonthExpenses,
                                customCategories = customCategories,
                                totalSpent = totalSpent,
                                monthlyBudget = monthlyBudget,
                                selectedMonth = selectedMonth,
                                chartMode = chartMode,
                                onSelectChartMode = { repository.setChartMode(it) },
                                onOpenCategories = { subScreen = SubScreen.CATEGORIES },
                                onAddCustomCategory = { repository.upsertCustomCategory(it) },
                                onAddExpenseForCategory = { catName ->
                                    addSheetInitialCategory = catName
                                    showAddSheet = true
                                }
                            )
                            else -> MoreScreen(
                                avatarBitmap = avatarBitmap,
                                profileEmoji = userProfileEmoji,
                                bills = bills,
                                goals = goals,
                                customCategoriesCount = customCategories.size,
                                pendingClaimsCount = pendingClaimsCount,
                                themeMode = themeMode,
                                onSetThemeMode = { repository.setThemeMode(it) },
                                onOpenBills = { subScreen = SubScreen.BILLS },
                                onOpenGoals = { subScreen = SubScreen.GOALS },
                                onOpenCategories = { subScreen = SubScreen.CATEGORIES },
                                onOpenClaims = {
                                    initialLedgerFilter = "Tolong Beli (Help Buy)"
                                    selectedTab = 1
                                },
                                onOpenExport = { subScreen = SubScreen.EXPORT },
                                onOpenScan = { subScreen = SubScreen.SCAN },
                                onOpenSettings = { subScreen = SubScreen.SETTINGS }
                            )
                        }
                    }
                }
            }
        }
    }

    // Centre "+" quick action chooser sheet
    if (showAddMenuSheet) {
        AddActionSheet(
            onDismiss = { showAddMenuSheet = false },
            onAddExpense = { showAddSheet = true },
            onScan = { subScreen = SubScreen.SCAN },
            onAddBill = { subScreen = SubScreen.BILLS },
            onAddToGoal = { subScreen = SubScreen.GOALS }
        )
    }

    // Add Expense Bottom Sheet
    if (showAddSheet) {
        AddExpenseSheet(
            customCategories = customCategories,
            recentRecipients = allRecipients,
            initialCategory = addSheetInitialCategory,
            onDismiss = {
                showAddSheet = false
                addSheetInitialCategory = null
            },
            onAddCustomCategory = { repository.upsertCustomCategory(it) },
            onManageCategories = {
                showAddSheet = false
                addSheetInitialCategory = null
                subScreen = SubScreen.CATEGORIES
            },
            onAdd = { newTx ->
                repository.addTransaction(newTx)
                showAddSheet = false
                addSheetInitialCategory = null
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

    // Share Claim Bottom Sheet / Dialog
    if (claimShareTarget != null) {
        val (recip, txs) = claimShareTarget!!
        ShareClaimDialog(
            recipientName = recip,
            transactionsToClaim = txs,
            savedDuitNow = userDuitNowInfo,
            onDismiss = { claimShareTarget = null },
            onMarkAllSettled = {
                txs.forEach { tx ->
                    if (!tx.isSettled) {
                        repository.toggleTransactionSettled(tx.id)
                    }
                }
                claimShareTarget = null
            },
            onSaveDuitNow = { repository.setDuitNowInfo(it) }
        )
    }

    // App Update Sheet / Dialog
    if (activeUpdateInfo != null) {
        val ver = activeUpdateInfo!!.latestVersion
        AppUpdateDialog(
            updateInfo = activeUpdateInfo!!,
            onDismiss = {
                activeUpdateInfo = null
                repository.dismissUpdate(ver)
            }
        )
    }
}

@Composable
private fun FintechBottomBar(
    selectedTab: Int,
    onSelectTab: (Int) -> Unit,
    onCenterAddClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp),
        color = glassSurfaceColor(MaterialTheme.colorScheme.surfaceContainer),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shadowElevation = 10.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            BottomTabItem(
                selected = selectedTab == 0,
                icon = if (selectedTab == 0) Icons.Default.Home else Icons.Outlined.Home,
                label = "Home",
                modifier = Modifier.weight(1f),
                onClick = { onSelectTab(0) }
            )
            BottomTabItem(
                selected = selectedTab == 1,
                icon = if (selectedTab == 1) Icons.Default.ReceiptLong else Icons.Outlined.ReceiptLong,
                label = "Activity",
                modifier = Modifier.weight(1f),
                onClick = { onSelectTab(1) }
            )

            // Centre primary "+" action button
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                FloatingActionButton(
                    onClick = onCenterAddClick,
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp),
                    modifier = Modifier.size(54.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Quick add", modifier = Modifier.size(26.dp))
                }
            }

            BottomTabItem(
                selected = selectedTab == 2,
                icon = if (selectedTab == 2) Icons.Default.PieChart else Icons.Outlined.PieChart,
                label = "Insights",
                modifier = Modifier.weight(1f),
                onClick = { onSelectTab(2) }
            )
            BottomTabItem(
                selected = selectedTab == 3,
                icon = if (selectedTab == 3) Icons.Default.GridView else Icons.Outlined.GridView,
                label = "More",
                modifier = Modifier.weight(1f),
                onClick = { onSelectTab(3) }
            )
        }
    }
}

@Composable
private fun BottomTabItem(
    selected: Boolean,
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    Column(
        modifier = modifier
            .heightIn(min = 52.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.16f) else Color.Transparent)
                .padding(horizontal = 14.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = tint
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
    pendingClaimsTotal: Double = 0.0,
    pendingClaimsCount: Int = 0,
    availableUpdate: AppUpdateInfo? = null,
    onOpenUpdateModal: () -> Unit = {},
    onDismissUpdate: () -> Unit = {},
    onEditBudget: () -> Unit,
    onQuickAdd: () -> Unit = {},
    onNavigateToScan: () -> Unit = {},
    onNavigateToAnalytics: () -> Unit = {},
    onNavigateToHistory: () -> Unit,
    onNavigateToTolongBeli: () -> Unit = {},
    onDeleteTransaction: (String) -> Unit
) {
    val isWp = LocalIsWallpaperActive.current
    val cardAlpha = LocalCardAlpha.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // GitHub Release Auto-Update Notification Banner
        if (availableUpdate != null && availableUpdate.hasUpdate) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenUpdateModal() },
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Default.SystemUpdate,
                        contentDescription = "New Version Available",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "New Version Available • ${availableUpdate.latestVersion}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            "Tap to view release notes & download APK",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                    FilledTonalButton(
                        onClick = onOpenUpdateModal,
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("Update", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    IconButton(
                        onClick = onDismissUpdate,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Dismiss",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }

        // HERO FINANCIAL HEALTH CARD
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isWp) MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = cardAlpha) else MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            border = BorderStroke(1.dp, Color.White.copy(alpha = if (isWp) 0.18f else 0.08f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = if (isWp) {
                                listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                                    Color.Transparent
                                )
                            } else {
                                listOf(
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                                    MaterialTheme.colorScheme.surfaceContainerHigh
                                )
                            }
                        )
                    )
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Header Row: Cycle & Status Badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                Icons.Default.CalendarMonth,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                "CYCLE • ${formatDisplayMonth(selectedMonth).uppercase()}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Surface(
                            shape = CircleShape,
                            color = if (isOverBudget) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
                            border = BorderStroke(
                                1.dp,
                                if (isOverBudget) MaterialTheme.colorScheme.error.copy(alpha = 0.5f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                            )
                        ) {
                            Text(
                                text = if (isOverBudget) "Over by ${formatRM(totalSpent - monthlyBudget)}" else "${formatRM(remaining)} left",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isOverBudget) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Main Total Spent Number
                    Column {
                        Text(
                            text = "TOTAL SPENT",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = formatRM(totalSpent),
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-0.5).sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Progress Bar & Budget Limit Row
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        LinearProgressIndicator(
                            progress = { (percentUsed / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(CircleShape),
                            color = if (isOverBudget) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.6f)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "$percentUsed% of ${formatRM(monthlyBudget)} ceiling",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onEditBudget() }
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "Edit Budget",
                                    modifier = Modifier.size(12.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "Edit",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                    // 2-Column Micro-Metrics Footer
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "DAILY SAFE ALLOWANCE",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${formatRM(dailySafeSpend)} / day",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                "Guilt-free pace",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .height(40.dp)
                                .width(1.dp)
                                .background(Color.White.copy(alpha = 0.15f))
                        )

                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                "CYCLE TIME",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "$daysRemaining days",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "Left in this month",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }

        // FINTECH QUICK ACTION DOCK (4 Circular Modern Action Buttons)
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = glassSurfaceColor(MaterialTheme.colorScheme.surfaceContainer),
            border = BorderStroke(1.dp, Color.White.copy(alpha = if (isWp) 0.15f else 0.05f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 14.dp, horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Add Expense Action
                QuickActionButton(
                    icon = Icons.Default.Add,
                    label = "Add RM",
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    iconTint = MaterialTheme.colorScheme.onPrimaryContainer,
                    onClick = onQuickAdd
                )

                // 2. AI Scanner Action
                QuickActionButton(
                    icon = Icons.Default.DocumentScanner,
                    label = "AI Scan",
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    iconTint = MaterialTheme.colorScheme.onTertiaryContainer,
                    onClick = onNavigateToScan
                )

                // 3. Tolong Beli Action with badge
                QuickActionButton(
                    icon = Icons.Default.VolunteerActivism,
                    label = "Tolong Beli",
                    containerColor = Color(0xFF38BDF8).copy(alpha = 0.2f),
                    iconTint = Color(0xFF0284C7),
                    badgeCount = pendingClaimsCount,
                    onClick = onNavigateToTolongBeli
                )

                // 4. Analytics Action
                QuickActionButton(
                    icon = Icons.Default.PieChart,
                    label = "Analytics",
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    iconTint = MaterialTheme.colorScheme.onSecondaryContainer,
                    onClick = onNavigateToAnalytics
                )
            }
        }

        // Tolong Beli Pending Claim Alert Banner
        if (pendingClaimsCount > 0) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToTolongBeli() },
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF38BDF8).copy(alpha = 0.16f)
                ),
                border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF38BDF8),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.VolunteerActivism, contentDescription = null, tint = Color.Black, modifier = Modifier.size(20.dp))
                            }
                        }
                        Column {
                            Text(
                                "Tolong Beli • ${formatRM(pendingClaimsTotal)} to Collect",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "$pendingClaimsCount pending claims from friends",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    TextButton(onClick = onNavigateToTolongBeli) {
                        Text("View Claims >", fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
                    }
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

@Composable
fun QuickActionButton(
    icon: ImageVector,
    label: String,
    containerColor: Color,
    iconTint: Color,
    badgeCount: Int = 0,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Box {
            Surface(
                shape = CircleShape,
                color = containerColor,
                modifier = Modifier.size(52.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = iconTint,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            if (badgeCount > 0) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(18.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = if (badgeCount > 9) "9+" else "$badgeCount",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onError
                        )
                    }
                }
            }
        }

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

// -------------------------------------------------------------
// CHARTS TAB (180° Half Pie, 360° Donut, Ranked Bars & Timeline)
// -------------------------------------------------------------
data class CategoryInsightItem(
    val name: String,
    val amount: Double,
    val count: Int,
    val isCustom: Boolean,
    val color: Color,
    val icon: ImageVector
)

@Composable
fun ChartsTab(
    allTransactions: List<Transaction> = emptyList(),
    expenses: List<Transaction>,
    customCategories: List<CustomCategory> = emptyList(),
    totalSpent: Double,
    monthlyBudget: Double,
    selectedMonth: String,
    chartMode: String = "half_pie",
    onSelectChartMode: (String) -> Unit = {},
    onOpenCategories: () -> Unit = {},
    onAddCustomCategory: (CustomCategory) -> Unit = {},
    onAddExpenseForCategory: (String) -> Unit = {}
) {
    var selectedCatName by remember { mutableStateOf<String?>(null) }
    var showAddCustomSheet by remember { mutableStateOf(false) }

    // Aggregate by category
    val catMap = remember(expenses) {
        val map = mutableMapOf<String, Double>()
        expenses.forEach { tx ->
            val cat = tx.category.ifEmpty { "Other" }
            map[cat] = (map[cat] ?: 0.0) + tx.amount
        }
        map.toList().sortedByDescending { it.second }
    }

    // Combine spending categories + all custom categories
    val allCategoryItems = remember(catMap, customCategories, expenses) {
        val customNamesSet = customCategories.map { it.name.trim().lowercase() }.toSet()
        val items = mutableListOf<CategoryInsightItem>()
        val seen = mutableSetOf<String>()

        // 1. Categories with spending in this month (highest first)
        catMap.forEach { (catName, amount) ->
            val clean = catName.trim()
            val key = clean.lowercase()
            seen.add(key)
            val count = expenses.count { it.category.equals(clean, true) }
            val isCustom = customNamesSet.contains(key)
            items.add(
                CategoryInsightItem(
                    name = clean,
                    amount = amount,
                    count = count,
                    isCustom = isCustom,
                    color = CategoryRegistry.resolveColor(clean, customCategories),
                    icon = getCategoryIcon(clean)
                )
            )
        }

        // 2. Custom categories created by user that have 0 spending in this month
        customCategories.forEach { customCat ->
            val clean = customCat.name.trim()
            val key = clean.lowercase()
            if (!seen.contains(key)) {
                seen.add(key)
                items.add(
                    CategoryInsightItem(
                        name = clean,
                        amount = 0.0,
                        count = 0,
                        isCustom = true,
                        color = CategoryRegistry.resolveColor(clean, customCategories),
                        icon = getCategoryIcon(clean)
                    )
                )
            }
        }
        items
    }

    val selectedCatItem = remember(selectedCatName, allCategoryItems) {
        selectedCatName?.let { name -> allCategoryItems.find { it.name.equals(name, ignoreCase = true) } }
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
        InsightsHeader(
            allTransactions = allTransactions.ifEmpty { expenses },
            monthExpenses = expenses
        )

        SectionHeader("Visual Breakdown")

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
                leadingIcon = {
                    Icon(
                        Icons.Default.PieChart,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (chartMode == "half_pie") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                label = { Text("180° Half Pie", fontWeight = FontWeight.Bold) },
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
                leadingIcon = {
                    Icon(
                        Icons.Default.DonutLarge,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (chartMode == "donut") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                label = { Text("360° Donut", fontWeight = FontWeight.Bold) },
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
                leadingIcon = {
                    Icon(
                        Icons.Default.BarChart,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (chartMode == "bar") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                label = { Text("Ranked Bars", fontWeight = FontWeight.Bold) },
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
                leadingIcon = {
                    Icon(
                        Icons.Default.Timeline,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (chartMode == "timeline") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                label = { Text("Daily Timeline", fontWeight = FontWeight.Bold) },
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
                                .height(210.dp),
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
                                        val color = CategoryRegistry.resolveColor(catName, customCategories)

                                        val isHighlighted = selectedCatName == null || selectedCatName.equals(catName, ignoreCase = true)
                                        val sliceAlpha = if (isHighlighted) 1.0f else 0.35f
                                        val sliceStroke = if (selectedCatName.equals(catName, ignoreCase = true)) strokeWidth + 4.dp.toPx() else strokeWidth

                                        drawArc(
                                            color = color.copy(alpha = sliceAlpha),
                                            startAngle = currentAngle,
                                            sweepAngle = sweep,
                                            useCenter = false,
                                            topLeft = topLeft,
                                            size = arcSize,
                                            style = Stroke(width = sliceStroke, cap = StrokeCap.Butt)
                                        )
                                        currentAngle += sweep + gap
                                    }
                                }
                            }

                            if (selectedCatItem != null) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(top = 38.dp)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = selectedCatItem.color.copy(alpha = 0.2f),
                                        border = BorderStroke(1.dp, selectedCatItem.color.copy(alpha = 0.5f)),
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(selectedCatItem.icon, contentDescription = null, tint = selectedCatItem.color, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(selectedCatItem.name, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        if (selectedCatItem.isCustom) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Surface(
                                                shape = CircleShape,
                                                color = selectedCatItem.color.copy(alpha = 0.22f)
                                            ) {
                                                Text(
                                                    "Custom",
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = selectedCatItem.color,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                    }
                                    Text(formatRM(selectedCatItem.amount), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                                    val pct = if (totalSpent > 0) (selectedCatItem.amount / totalSpent * 100).toInt() else 0
                                    Text(
                                        if (selectedCatItem.amount > 0) "$pct% • ${selectedCatItem.count} tx" else "0 transactions",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    FilledTonalButton(
                                        onClick = { onAddExpenseForCategory(selectedCatItem.name) },
                                        shape = CircleShape,
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                        modifier = Modifier.height(26.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text("Log", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            } else {
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
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Category Chips
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            allCategoryItems.forEach { item ->
                                val isSelected = selectedCatName.equals(item.name, ignoreCase = true)
                                val pct = if (totalSpent > 0) (item.amount / totalSpent * 100).toInt() else 0

                                Surface(
                                    shape = CircleShape,
                                    color = if (isSelected) item.color.copy(alpha = 0.22f) else MaterialTheme.colorScheme.surfaceContainer,
                                    border = if (isSelected) BorderStroke(1.5.dp, item.color) else glassCardBorder(),
                                    onClick = { selectedCatName = if (isSelected) null else item.name },
                                    modifier = Modifier.padding(4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            item.icon,
                                            contentDescription = null,
                                            tint = item.color,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(item.name, style = MaterialTheme.typography.labelSmall, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium)
                                        if (item.isCustom) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Surface(
                                                shape = CircleShape,
                                                color = item.color.copy(alpha = 0.25f)
                                            ) {
                                                Text(
                                                    "Custom",
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = item.color,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(
                                            if (item.amount > 0) "$pct%" else "RM 0",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (item.amount > 0) item.color else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            // "+ Custom" chip
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                                onClick = { showAddCustomSheet = true },
                                modifier = Modifier.padding(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Custom", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                            }

                            // "Manage" chip
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                border = glassCardBorder(),
                                onClick = onOpenCategories,
                                modifier = Modifier.padding(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Tune, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Manage", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                                .height(250.dp),
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
                                        val color = CategoryRegistry.resolveColor(catName, customCategories)

                                        val isHighlighted = selectedCatName == null || selectedCatName.equals(catName, ignoreCase = true)
                                        val sliceAlpha = if (isHighlighted) 1.0f else 0.35f
                                        val sliceStroke = if (selectedCatName.equals(catName, ignoreCase = true)) strokeWidth + 4.dp.toPx() else strokeWidth

                                        drawArc(
                                            color = color.copy(alpha = sliceAlpha),
                                            startAngle = currentAngle,
                                            sweepAngle = sweep,
                                            useCenter = false,
                                            topLeft = topLeft,
                                            size = arcSize,
                                            style = Stroke(width = sliceStroke, cap = StrokeCap.Butt)
                                        )
                                        currentAngle += sweep + gap
                                    }
                                }
                            }

                            if (selectedCatItem != null) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Surface(
                                        shape = CircleShape,
                                        color = selectedCatItem.color.copy(alpha = 0.2f),
                                        border = BorderStroke(1.dp, selectedCatItem.color.copy(alpha = 0.5f)),
                                        modifier = Modifier.size(42.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(selectedCatItem.icon, contentDescription = null, tint = selectedCatItem.color, modifier = Modifier.size(20.dp))
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(selectedCatItem.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        if (selectedCatItem.isCustom) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Surface(
                                                shape = CircleShape,
                                                color = selectedCatItem.color.copy(alpha = 0.22f)
                                            ) {
                                                Text(
                                                    "Custom",
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = selectedCatItem.color,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                    }
                                    Text(formatRM(selectedCatItem.amount), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                                    val pct = if (totalSpent > 0) (selectedCatItem.amount / totalSpent * 100).toInt() else 0
                                    Text(
                                        if (selectedCatItem.amount > 0) "$pct% of spend • ${selectedCatItem.count} tx" else "0 transactions",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    FilledTonalButton(
                                        onClick = { onAddExpenseForCategory(selectedCatItem.name) },
                                        shape = CircleShape,
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Log Expense", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("TOTAL SPENT", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(formatRM(totalSpent), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                                    val pctBudget = if (monthlyBudget > 0) ((totalSpent / monthlyBudget) * 100).toInt() else 0
                                    Text("$pctBudget% of budget", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Category Chips
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            allCategoryItems.forEach { item ->
                                val isSelected = selectedCatName.equals(item.name, ignoreCase = true)
                                val pct = if (totalSpent > 0) (item.amount / totalSpent * 100).toInt() else 0

                                Surface(
                                    shape = CircleShape,
                                    color = if (isSelected) item.color.copy(alpha = 0.22f) else MaterialTheme.colorScheme.surfaceContainer,
                                    border = if (isSelected) BorderStroke(1.5.dp, item.color) else glassCardBorder(),
                                    onClick = { selectedCatName = if (isSelected) null else item.name },
                                    modifier = Modifier.padding(4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            item.icon,
                                            contentDescription = null,
                                            tint = item.color,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(item.name, style = MaterialTheme.typography.labelSmall, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium)
                                        if (item.isCustom) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Surface(
                                                shape = CircleShape,
                                                color = item.color.copy(alpha = 0.25f)
                                            ) {
                                                Text(
                                                    "Custom",
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = item.color,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(
                                            if (item.amount > 0) "$pct%" else "RM 0",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (item.amount > 0) item.color else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            // "+ Custom" chip
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                                onClick = { showAddCustomSheet = true },
                                modifier = Modifier.padding(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Custom", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                            }

                            // "Manage" chip
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                border = glassCardBorder(),
                                onClick = onOpenCategories,
                                modifier = Modifier.padding(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Tune, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Manage", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                                Text("${allCategoryItems.size} categories (${customCategories.size} custom) in $selectedMonth", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text(formatRM(totalSpent), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                        }

                        if (allCategoryItems.isEmpty()) {
                            Text("No categories or expenses logged for this month.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            allCategoryItems.forEachIndexed { idx, item ->
                                val pct = if (totalSpent > 0) (item.amount / totalSpent).toFloat() else 0f
                                val pctInt = (pct * 100).toInt()

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
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text(
                                                    if (item.amount > 0) "#${idx + 1}" else "—",
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (item.amount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                    fontSize = 12.sp
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = item.color.copy(alpha = 0.18f),
                                                    modifier = Modifier.size(26.dp)
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Icon(item.icon, contentDescription = null, tint = item.color, modifier = Modifier.size(15.dp))
                                                    }
                                                }
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(item.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                                if (item.isCustom) {
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Surface(
                                                        shape = CircleShape,
                                                        color = item.color.copy(alpha = 0.2f)
                                                    ) {
                                                        Text(
                                                            "Custom",
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = item.color,
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                                        )
                                                    }
                                                }
                                            }
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(formatRM(item.amount), fontWeight = FontWeight.Black, style = MaterialTheme.typography.bodyMedium)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Surface(
                                                    shape = CircleShape,
                                                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                                    border = BorderStroke(1.dp, item.color.copy(alpha = 0.35f)),
                                                    onClick = { onAddExpenseForCategory(item.name) }
                                                ) {
                                                    Box(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                                                        Text("+ Log", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = item.color)
                                                    }
                                                }
                                            }
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
                                            if (item.amount > 0) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth(pct.coerceIn(0.01f, 1f))
                                                        .fillMaxHeight()
                                                        .clip(CircleShape)
                                                        .background(item.color)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("${item.count} transaction(s)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(
                                                if (item.amount > 0) "$pctInt% of monthly spend" else "No spending yet",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = if (item.amount > 0) item.color else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
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
                            val activeBarColor = MaterialTheme.colorScheme.primary
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
                                    val barColor = if (isPeak) Color(0xFFF43F5E) else if (spentOnDay > 0) activeBarColor else Color(0x22888888)

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

        // Custom Categories Dedicated Section
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
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Custom Categories", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            if (customCategories.isEmpty()) "Add personalized spending buckets"
                            else "${customCategories.size} custom categories active",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilledTonalButton(
                            onClick = { showAddCustomSheet = true },
                            shape = CircleShape,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("New", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                        TextButton(
                            onClick = onOpenCategories,
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Manage", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                if (customCategories.isEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        border = glassCardBorder(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                "No custom categories created yet. Tap '+ New' to create categories like Pets, Hobbies, or Holiday and see them automatically tracked here!",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        customCategories.forEach { customCat ->
                            val color = CategoryRegistry.resolveColor(customCat.name, customCategories)
                            val icon = getCategoryIcon(customCat.name)
                            val customSpent = expenses.filter { it.category.equals(customCat.name, true) }.sumOf { it.amount }
                            val customCount = expenses.count { it.category.equals(customCat.name, true) }
                            val customPct = if (totalSpent > 0) (customSpent / totalSpent * 100).toInt() else 0

                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = glassSurfaceColor(MaterialTheme.colorScheme.surfaceContainer),
                                border = glassCardBorder(),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = color.copy(alpha = 0.18f),
                                            border = BorderStroke(1.dp, color.copy(alpha = 0.35f)),
                                            modifier = Modifier.size(40.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
                                            }
                                        }
                                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(customCat.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Surface(
                                                    shape = CircleShape,
                                                    color = color.copy(alpha = 0.2f)
                                                ) {
                                                    Text(
                                                        "Custom",
                                                        fontSize = 8.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = color,
                                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                                    )
                                                }
                                            }
                                            Text(
                                                if (customCount > 0) "$customCount tx in $selectedMonth • $customPct%"
                                                else "0 transactions in $selectedMonth",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            formatRM(customSpent),
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Black,
                                            color = if (customSpent > 0) color else MaterialTheme.colorScheme.onSurface
                                        )
                                        FilledTonalButton(
                                            onClick = { onAddExpenseForCategory(customCat.name) },
                                            shape = CircleShape,
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            modifier = Modifier.height(30.dp)
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Log", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showAddCustomSheet) {
            CustomCategorySheet(
                onDismiss = { showAddCustomSheet = false },
                onSave = { newCat ->
                    onAddCustomCategory(newCat)
                    showAddCustomSheet = false
                }
            )
        }

        Spacer(modifier = Modifier.height(60.dp))
    }
}

// -------------------------------------------------------------
// HYBRID IMPORT TAB: GOOGLE ML KIT LOCAL OCR + GOOGLE GEMINI AI
// -------------------------------------------------------------
@Composable
fun ImportTab(
    repository: FinanceRepository,
    onImportTransactions: (List<Transaction>) -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val recognizer = remember { TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS) }

    // Top Engine Switcher: 0 = Local OCR (Offline), 1 = Gemini AI
    var currentScanEngine by remember { mutableIntStateOf(0) }

    // State for Local OCR
    var isLocalScanning by remember { mutableStateOf(false) }
    var localScanStatus by remember { mutableStateOf("") }
    var localRawText by remember { mutableStateOf("") }
    var localParsedList by remember { mutableStateOf<List<Transaction>>(emptyList()) }
    var localActiveMode by remember { mutableIntStateOf(0) } // 0: AI OCR Scanner, 1: Paste Text
    var showLocalRawText by remember { mutableStateOf(false) }

    // State for Gemini AI
    val geminiApiKey by repository.geminiApiKey.collectAsState()
    val geminiModel by repository.geminiModel.collectAsState()
    var isGeminiProcessing by remember { mutableStateOf(false) }
    var geminiStatus by remember { mutableStateOf("") }
    var geminiResult by remember { mutableStateOf<GeminiExecutionResult?>(null) }
    var proposedActions by remember { mutableStateOf<List<GeminiProposedAction>>(emptyList()) }
    var userChatPrompt by remember { mutableStateOf("") }

    // Local ML Kit Pickers (100% Offline)
    val localGalleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
        if (uri != null) {
            try {
                isLocalScanning = true
                localScanStatus = "Scanning image with Google ML Kit OCR..."
                val image = InputImage.fromFilePath(context, uri)
                recognizer.process(image)
                    .addOnSuccessListener { visionText ->
                        val text = visionText.text
                        localRawText = text
                        val results = StatementParser.parseFinancialText(text)
                        localParsedList = results
                        isLocalScanning = false
                        localScanStatus = if (results.isEmpty()) {
                            "OCR finished: No financial transactions detected. You can review raw text below."
                        } else {
                            "Successfully parsed ${results.size} transaction(s)!"
                        }
                    }
                    .addOnFailureListener { e ->
                        isLocalScanning = false
                        localScanStatus = "OCR Scan error: ${e.localizedMessage}"
                    }
            } catch (e: Exception) {
                isLocalScanning = false
                localScanStatus = "Could not open image: ${e.localizedMessage}"
            }
        }
    }

    val localCameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
        if (bitmap != null) {
            try {
                isLocalScanning = true
                localScanStatus = "Scanning photo with Google ML Kit OCR..."
                val image = InputImage.fromBitmap(bitmap, 0)
                recognizer.process(image)
                    .addOnSuccessListener { visionText ->
                        val text = visionText.text
                        localRawText = text
                        val results = StatementParser.parseFinancialText(text)
                        localParsedList = results
                        isLocalScanning = false
                        localScanStatus = if (results.isEmpty()) {
                            "OCR finished: No financial transactions detected. You can review raw text below."
                        } else {
                            "Successfully parsed ${results.size} transaction(s)!"
                        }
                    }
                    .addOnFailureListener { e ->
                        isLocalScanning = false
                        localScanStatus = "Camera OCR error: ${e.localizedMessage}"
                    }
            } catch (e: Exception) {
                isLocalScanning = false
                localScanStatus = "Camera capture error: ${e.localizedMessage}"
            }
        }
    }

    // Gemini AI Launchers (Camera, Gallery, and PDF Document)
    val geminiCameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
        if (bitmap != null) {
            scope.launch {
                try {
                    isGeminiProcessing = true
                    geminiStatus = "Analyzing photo with $geminiModel..."
                    val bos = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bos)
                    val bytes = bos.toByteArray()
                    val result = repository.callGemini(
                        prompt = "Extract all transactions from this captured receipt or statement document accurately.",
                        fileBytes = bytes,
                        mimeType = "image/jpeg"
                    )
                    geminiResult = result
                    proposedActions = result.actions
                    geminiStatus = if (result.success) "Extracted ${result.actions.size} action(s)!" else (result.errorMessage ?: "Failed")
                } catch (e: Exception) {
                    geminiStatus = "Error: ${e.localizedMessage}"
                } finally {
                    isGeminiProcessing = false
                }
            }
        }
    }

    val geminiGalleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                try {
                    isGeminiProcessing = true
                    geminiStatus = "Uploading screenshot & analyzing with $geminiModel..."
                    val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    val mime = context.contentResolver.getType(uri) ?: "image/jpeg"
                    if (bytes != null && bytes.isNotEmpty()) {
                        val result = repository.callGemini(
                            prompt = "Extract all transactions from this receipt or financial screenshot accurately.",
                            fileBytes = bytes,
                            mimeType = mime
                        )
                        geminiResult = result
                        proposedActions = result.actions
                        geminiStatus = if (result.success) "Extracted ${result.actions.size} action(s)!" else (result.errorMessage ?: "Failed")
                    } else {
                        geminiStatus = "Could not read image file."
                    }
                } catch (e: Exception) {
                    geminiStatus = "Error: ${e.localizedMessage}"
                } finally {
                    isGeminiProcessing = false
                }
            }
        }
    }

    val geminiPdfLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                try {
                    isGeminiProcessing = true
                    geminiStatus = "Reading PDF document & sending to $geminiModel..."
                    val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    if (bytes != null && bytes.isNotEmpty()) {
                        val result = repository.callGemini(
                            prompt = "Parse this entire bank statement / university / eWallet PDF document and extract every transaction accurately.",
                            fileBytes = bytes,
                            mimeType = "application/pdf"
                        )
                        geminiResult = result
                        proposedActions = result.actions
                        geminiStatus = if (result.success) "Extracted ${result.actions.size} action(s) from PDF!" else (result.errorMessage ?: "Failed")
                    } else {
                        geminiStatus = "Could not read PDF bytes."
                    }
                } catch (e: Exception) {
                    geminiStatus = "PDF error: ${e.localizedMessage}"
                } finally {
                    isGeminiProcessing = false
                }
            }
        }
    }

    fun submitGeminiPrompt(text: String) {
        if (text.isBlank()) return
        scope.launch {
            try {
                isGeminiProcessing = true
                geminiStatus = "Executing command with $geminiModel..."
                val result = repository.callGemini(prompt = text)
                geminiResult = result
                proposedActions = result.actions
                geminiStatus = if (result.success) "Ready!" else (result.errorMessage ?: "Error")
                userChatPrompt = ""
            } catch (e: Exception) {
                geminiStatus = "Error: ${e.localizedMessage}"
            } finally {
                isGeminiProcessing = false
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
        // TOP ENGINE SWITCHER: Local OCR vs Gemini AI
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = currentScanEngine == 0,
                onClick = { currentScanEngine = 0 },
                leadingIcon = {
                    Icon(
                        Icons.Default.DocumentScanner,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (currentScanEngine == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                label = { Text("Local OCR", fontWeight = FontWeight.Bold) },
                shape = CircleShape,
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = glassSurfaceColor(MaterialTheme.colorScheme.surfaceContainer),
                    selectedContainerColor = if (LocalIsWallpaperActive.current) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f) else MaterialTheme.colorScheme.primaryContainer
                ),
                border = glassCardBorder(),
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = currentScanEngine == 1,
                onClick = { currentScanEngine = 1 },
                leadingIcon = {
                    Icon(
                        Icons.Default.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (currentScanEngine == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                label = { Text("Gemini Cloud AI", fontWeight = FontWeight.Bold) },
                shape = CircleShape,
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = glassSurfaceColor(MaterialTheme.colorScheme.surfaceContainer),
                    selectedContainerColor = if (LocalIsWallpaperActive.current) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f) else MaterialTheme.colorScheme.primaryContainer
                ),
                border = glassCardBorder(),
                modifier = Modifier.weight(1.15f)
            )
        }

        // ==========================================
        // OPTION 1: LOCAL GOOGLE ML KIT OCR (OFFLINE)
        // ==========================================
        if (currentScanEngine == 0) {
            // Mode Selector Chips within Local OCR
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = localActiveMode == 0,
                    onClick = { localActiveMode = 0 },
                    leadingIcon = {
                        Icon(
                            Icons.Default.PhotoCamera,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (localActiveMode == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    label = { Text("Camera & Gallery", fontWeight = FontWeight.Bold) },
                    shape = CircleShape,
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = glassSurfaceColor(MaterialTheme.colorScheme.surfaceContainerLow),
                        selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = localActiveMode == 1,
                    onClick = { localActiveMode = 1 },
                    leadingIcon = {
                        Icon(
                            Icons.Default.ContentPaste,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (localActiveMode == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    label = { Text("Paste Text", fontWeight = FontWeight.Bold) },
                    shape = CircleShape,
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = glassSurfaceColor(MaterialTheme.colorScheme.surfaceContainerLow),
                        selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    modifier = Modifier.weight(1f)
                )
            }

            if (localActiveMode == 0) {
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
                                Text("Local Receipt & Statement OCR", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text("100% on-device Google ML Kit • No internet needed", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
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
                                onClick = { localCameraLauncher.launch(null) },
                                modifier = Modifier.weight(1f).height(50.dp),
                                shape = RoundedCornerShape(16.dp),
                                enabled = !isLocalScanning
                            ) {
                                Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Camera", fontWeight = FontWeight.Bold)
                            }

                            FilledTonalButton(
                                onClick = {
                                    localGalleryLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                modifier = Modifier.weight(1f).height(50.dp),
                                shape = RoundedCornerShape(16.dp),
                                enabled = !isLocalScanning
                            ) {
                                Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Gallery", fontWeight = FontWeight.Bold)
                            }
                        }

                        // Scanning State
                        if (isLocalScanning) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceContainer, RoundedCornerShape(12.dp))
                                    .padding(12.dp)
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(localScanStatus, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                            }
                        } else if (localScanStatus.isNotEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceContainer,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = localScanStatus,
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
                            value = localRawText,
                            onValueChange = {
                                localRawText = it
                                localParsedList = StatementParser.parseFinancialText(it)
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
                            onClick = { localParsedList = StatementParser.parseFinancialText(localRawText) },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = CircleShape
                        ) {
                            Text("Parse Statement Text", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Prompt to switch to Gemini AI if 0 transactions found
            if (!isLocalScanning && localRawText.isNotEmpty() && localParsedList.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.9f))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text("Need deeper document analysis?", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            "Local regex didn't recognize this format. Gemini AI can read complex tables, PDF statements, and handwritten notes.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Button(
                            onClick = { currentScanEngine = 1 },
                            shape = CircleShape,
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Switch to Gemini AI", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Detected Transactions Section
            if (localParsedList.isNotEmpty()) {
                val totalBatch = localParsedList.sumOf { it.amount }
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
                            Text("${localParsedList.size} item(s) • Total: ${formatRM(totalBatch)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }

                        Button(
                            onClick = {
                                onImportTransactions(localParsedList)
                                localRawText = ""
                                localParsedList = emptyList()
                                localScanStatus = "Imported successfully into Ledger!"
                            },
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Import All", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                localParsedList.forEach { tx ->
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
            if (localRawText.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showLocalRawText = !showLocalRawText }
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Raw OCR / Statement Text", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(if (showLocalRawText) "Hide" else "View", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                Icon(
                                    if (showLocalRawText) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        if (showLocalRawText) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = localRawText,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // ==========================================
        // OPTION 2: GOOGLE GEMINI AI (PDF, IMAGE & CHAT)
        // ==========================================
        if (currentScanEngine == 1) {
            if (geminiApiKey.isBlank()) {
                // API Key Setup Prompt Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = glassCardColors(MaterialTheme.colorScheme.surfaceContainerHigh),
                    border = glassCardBorder()
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                            Column {
                                Text("Google Gemini AI Assistant", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text("PDF Bank Statements & Smart Tools", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                            }
                        }

                        Text(
                            "Scan multi-page PDF bank statements (Maybank, CIMB, TNG eWallet, etc.), receipts in any language, or talk to AI to add, edit and delete finances.\n\nRequires your free personal API key from Google AI Studio.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Button(
                            onClick = onNavigateToSettings,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Enter Free API Key in Settings", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                // Multimodal Input Hub Card (PDF, Camera, Gallery)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = glassCardColors(MaterialTheme.colorScheme.surfaceContainerHigh),
                    border = glassCardBorder()
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Text("Multimodal Document Scan", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            ) {
                                Text(
                                    geminiModel,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Text(
                            "Select any financial document to auto-extract transactions. Gemini natively reads PDF bank statements and receipt images.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // 3 Primary Buttons: Camera, Image, PDF
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { geminiCameraLauncher.launch(null) },
                                modifier = Modifier.weight(1f).height(48.dp),
                                shape = RoundedCornerShape(14.dp),
                                enabled = !isGeminiProcessing
                            ) {
                                Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Camera", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            FilledTonalButton(
                                onClick = {
                                    geminiGalleryLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                modifier = Modifier.weight(1f).height(48.dp),
                                shape = RoundedCornerShape(14.dp),
                                enabled = !isGeminiProcessing
                            ) {
                                Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Image", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            FilledTonalButton(
                                onClick = { geminiPdfLauncher.launch(arrayOf("application/pdf")) },
                                modifier = Modifier.weight(1f).height(48.dp),
                                shape = RoundedCornerShape(14.dp),
                                enabled = !isGeminiProcessing
                            ) {
                                Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("PDF", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Natural Language Assistant Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = glassCardColors(MaterialTheme.colorScheme.surfaceContainerHigh),
                    border = glassCardBorder()
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Chat, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text("Smart Financial Commands", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }

                        Text(
                            "Type commands to log, modify, delete transactions, or ask questions about your budget:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Quick Chips
                        val quickPrompts = listOf(
                            Triple(Icons.Default.Add, "Lunch at Mamak RM 14.50", "Add RM 14.50 for Mamak lunch"),
                            Triple(Icons.Default.Add, "Touch 'n Go Toll RM 3.50", "Add RM 3.50 for toll"),
                            Triple(Icons.Default.DeleteOutline, "Delete latest expense", "Delete my latest expense"),
                            Triple(Icons.Default.BarChart, "Monthly spend", "How much did I spend this month?")
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            quickPrompts.forEach { (icon, label, prompt) ->
                                SuggestionChip(
                                    onClick = {
                                        userChatPrompt = prompt
                                        submitGeminiPrompt(userChatPrompt)
                                    },
                                    icon = {
                                        Icon(
                                            icon,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    },
                                    label = { Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium) },
                                    shape = CircleShape
                                )
                            }
                        }

                        // Text input + Send button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = userChatPrompt,
                                onValueChange = { userChatPrompt = it },
                                placeholder = { Text("e.g. Add RM 25 for Pelita dinner today") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(16.dp),
                                singleLine = true
                            )

                            IconButton(
                                onClick = { submitGeminiPrompt(userChatPrompt) },
                                enabled = !isGeminiProcessing && userChatPrompt.isNotBlank(),
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(
                                        if (userChatPrompt.isNotBlank() && !isGeminiProcessing) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainer,
                                        CircleShape
                                    )
                            ) {
                                Icon(
                                    Icons.Default.Send,
                                    contentDescription = "Send",
                                    tint = if (userChatPrompt.isNotBlank() && !isGeminiProcessing) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Processing Spinner State
                if (isGeminiProcessing) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.5.dp, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(14.dp))
                            Text(geminiStatus, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                }

                // AI Result Summary
                if (geminiResult != null && !isGeminiProcessing) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = glassSurfaceColor(MaterialTheme.colorScheme.surfaceContainerHigh),
                        border = glassCardBorder(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                Text("RinggitWise AI Summary", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            }
                            Text(
                                geminiResult!!.summary,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Proposed Actions Preview & Confirmation Card
                if (proposedActions.isNotEmpty() && !isGeminiProcessing) {
                    val addActions = proposedActions.filter { it.actionType == "ADD" }
                    val totalAdd = addActions.sumOf { it.amount }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Action Preview", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                    Text(
                                        "${proposedActions.size} action(s)" + if (addActions.isNotEmpty()) " • Total: ${formatRM(totalAdd)}" else "",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }

                                Button(
                                    onClick = {
                                        repository.executeMultipleActions(proposedActions)
                                        val count = proposedActions.size
                                        proposedActions = emptyList()
                                        geminiStatus = "Successfully executed $count action(s) in your ledger!"
                                        onImportTransactions(emptyList())
                                    },
                                    shape = CircleShape,
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Text("Apply All", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Individual Action Items
                    proposedActions.forEach { act ->
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
                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = when (act.actionType) {
                                                "ADD" -> MaterialTheme.colorScheme.primaryContainer
                                                "DELETE" -> MaterialTheme.colorScheme.errorContainer
                                                "UPDATE" -> MaterialTheme.colorScheme.secondaryContainer
                                                else -> MaterialTheme.colorScheme.surfaceVariant
                                            }
                                        ) {
                                            Text(
                                                act.actionType,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Black,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                color = when (act.actionType) {
                                                    "ADD" -> MaterialTheme.colorScheme.primary
                                                    "DELETE" -> MaterialTheme.colorScheme.error
                                                    "UPDATE" -> MaterialTheme.colorScheme.secondary
                                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                                }
                                            )
                                        }
                                        Text(act.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                    }

                                    Text(
                                        "${act.date} • ${act.category} • ${act.method}" + if (act.explanation.isNotBlank()) " (${act.explanation})" else "",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    if (act.amount > 0) {
                                        Text(
                                            formatRM(act.amount),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Black,
                                            color = if (act.actionType == "DELETE") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    IconButton(
                                        onClick = { proposedActions = proposedActions.filter { it != act } },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Dismiss", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
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
// TRANSACTION LEDGER TAB
// -------------------------------------------------------------
@Composable
fun LedgerTab(
    transactions: List<Transaction>,
    customCategories: List<CustomCategory> = emptyList(),
    duitNowInfo: String = "",
    initialFilter: String = "All",
    onDelete: (String) -> Unit,
    onToggleSettled: (String) -> Unit = {},
    onAddCustomCategory: (CustomCategory) -> Unit = {},
    onManageCategories: () -> Unit = {},
    onShareClaim: (String, List<Transaction>) -> Unit = { _, _ -> }
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember(initialFilter) { mutableStateOf(initialFilter) }
    var selectedRecipientFilter by remember { mutableStateOf("All People") }
    var showNewCatSheet by remember { mutableStateOf(false) }

    val tolongBeliTxs = remember(transactions) {
        transactions.filter { it.isHelpBuy || it.category.contains("Tolong Beli", ignoreCase = true) }
    }
    val pendingClaims = remember(tolongBeliTxs) {
        tolongBeliTxs.filter { !it.isSettled }
    }
    val settledClaims = remember(tolongBeliTxs) {
        tolongBeliTxs.filter { it.isSettled }
    }
    val totalPending = remember(pendingClaims) { pendingClaims.sumOf { it.amount } }
    val totalSettled = remember(settledClaims) { settledClaims.sumOf { it.amount } }

    val recipients = remember(tolongBeliTxs) {
        tolongBeliTxs.map { it.claimRecipient.trim() }.filter { it.isNotBlank() }.distinct()
    }

    val isViewingTolongBeli = selectedCategoryFilter.contains("Tolong Beli", ignoreCase = true)

    val filtered = remember(transactions, searchQuery, selectedCategoryFilter, selectedRecipientFilter, isViewingTolongBeli) {
        transactions.filter { tx ->
            val matchesSearch = tx.title.contains(searchQuery, true) ||
                    tx.category.contains(searchQuery, true) ||
                    tx.note.contains(searchQuery, true) ||
                    tx.claimRecipient.contains(searchQuery, true)

            val matchesCat = if (isViewingTolongBeli) {
                tx.isHelpBuy || tx.category.contains("Tolong Beli", ignoreCase = true)
            } else {
                selectedCategoryFilter == "All" || tx.category.equals(selectedCategoryFilter, true)
            }

            val matchesRecipient = if (isViewingTolongBeli && selectedRecipientFilter != "All People") {
                tx.claimRecipient.equals(selectedRecipientFilter, true)
            } else true

            matchesSearch && matchesCat && matchesRecipient
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp)) {
        // Search TextField
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            shape = CircleShape,
            placeholder = { Text("Filter transactions or friend...") },
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

            // Dedicated Tolong Beli chip with pending badge
            val isTolongSelected = selectedCategoryFilter.contains("Tolong Beli", ignoreCase = true)
            FilterChip(
                selected = isTolongSelected,
                onClick = { selectedCategoryFilter = "Tolong Beli (Help Buy)" },
                leadingIcon = {
                    Icon(
                        Icons.Default.VolunteerActivism,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (isTolongSelected) MaterialTheme.colorScheme.primary else Color(0xFF38BDF8)
                    )
                },
                label = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Tolong Beli", fontWeight = FontWeight.Bold)
                        if (pendingClaims.isNotEmpty()) {
                            Surface(
                                shape = CircleShape,
                                color = if (isTolongSelected) MaterialTheme.colorScheme.primary else Color(0xFF38BDF8),
                                modifier = Modifier.size(18.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        "${pendingClaims.size}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                }
                            }
                        }
                    }
                },
                shape = CircleShape
            )

            CategoryRegistry.allCategories(customCategories)
                .filter { !it.name.contains("Tolong Beli", ignoreCase = true) }
                .forEach { cat ->
                    FilterChip(
                        selected = selectedCategoryFilter == cat.name,
                        onClick = { selectedCategoryFilter = cat.name },
                        leadingIcon = {
                            Icon(
                                getCategoryIcon(cat.name),
                                contentDescription = null,
                                modifier = Modifier.size(15.dp),
                                tint = cat.color
                            )
                        },
                        label = { Text(cat.name) },
                        shape = CircleShape
                    )
                }

            AssistChip(
                onClick = { showNewCatSheet = true },
                leadingIcon = {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                label = { Text("Custom", fontWeight = FontWeight.Bold) },
                shape = CircleShape
            )

            AssistChip(
                onClick = onManageCategories,
                leadingIcon = {
                    Icon(
                        Icons.Default.Category,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                label = { Text("Manage") },
                shape = CircleShape
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Tolong Beli Tracker Summary Card (When Viewing Tolong Beli)
        if (isViewingTolongBeli) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = glassCardColors(MaterialTheme.colorScheme.surfaceContainerHigh),
                border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.45f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF38BDF8).copy(alpha = 0.2f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.VolunteerActivism, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(20.dp))
                                }
                            }
                            Column {
                                Text("Tolong Beli Claims", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text("Stuff you helped friends buy", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    // Stats: Pending vs Settled
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFFF59E0B).copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.35f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("UNCOLLECTED", style = MaterialTheme.typography.labelSmall, color = Color(0xFFF59E0B), fontWeight = FontWeight.Bold)
                                Text(formatRM(totalPending), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = Color(0xFFF59E0B))
                                Text("${pendingClaims.size} pending", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFF10B981).copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.35f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("SETTLED", style = MaterialTheme.typography.labelSmall, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                                Text(formatRM(totalSettled), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = Color(0xFF10B981))
                                Text("${settledClaims.size} settled", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    // Recipient Filter Row
                    if (recipients.isNotEmpty()) {
                        Text("Filter by Friend:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            FilterChip(
                                selected = selectedRecipientFilter == "All People",
                                onClick = { selectedRecipientFilter = "All People" },
                                label = { Text("All (${recipients.size})") },
                                shape = CircleShape
                            )
                            recipients.forEach { person ->
                                val personPending = pendingClaims.filter { it.claimRecipient.equals(person, true) }.sumOf { it.amount }
                                FilterChip(
                                    selected = selectedRecipientFilter == person,
                                    onClick = { selectedRecipientFilter = person },
                                    label = {
                                        Text(if (personPending > 0) "$person (${formatRM(personPending)})" else person)
                                    },
                                    shape = CircleShape
                                )
                            }
                        }
                    }

                    // Share Batch Claim Button
                    val claimableForSelected = if (selectedRecipientFilter == "All People") {
                        pendingClaims
                    } else {
                        pendingClaims.filter { it.claimRecipient.equals(selectedRecipientFilter, true) }
                    }

                    if (claimableForSelected.isNotEmpty()) {
                        Button(
                            onClick = {
                                onShareClaim(selectedRecipientFilter, claimableForSelected)
                            },
                            modifier = Modifier.fillMaxWidth().height(46.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Share Combined Claim (${formatRM(claimableForSelected.sumOf { it.amount })})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }

        // Transactions LazyColumn
        if (filtered.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    if (isViewingTolongBeli) "No Tolong Beli expenses recorded yet.\nTag items when recording or use AI scan!" else "No transactions found.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(filtered, key = { it.id }) { tx ->
                    TransactionCardItem(
                        tx = tx,
                        onDelete = { onDelete(tx.id) },
                        onToggleSettled = { onToggleSettled(tx.id) },
                        onShareClaim = { onShareClaim(tx.claimRecipient, listOf(tx)) }
                    )
                }
            }
        }
    }

    if (showNewCatSheet) {
        CustomCategorySheet(
            onDismiss = { showNewCatSheet = false },
            onSave = { newCat ->
                onAddCustomCategory(newCat)
                selectedCategoryFilter = newCat.name
                showNewCatSheet = false
            }
        )
    }
}

fun getCategoryIcon(category: String): ImageVector {
    CategoryRegistry.findCategory(category)?.let { cat ->
        if (cat.iconKey.isNotBlank()) {
            return categoryIconByKey(cat.iconKey)
        }
    }
    return when {
        category.contains("Parking", true) || category.contains("Toll", true) -> Icons.Default.DirectionsCar
        category.contains("Mamak", true) || category.contains("Dining", true) || category.contains("Food", true) -> Icons.Default.Restaurant
        category.contains("Drink", true) || category.contains("Cafe", true) || category.contains("Coffee", true) -> Icons.Default.LocalCafe
        category.contains("Grocer", true) || category.contains("Supermarket", true) -> Icons.Default.ShoppingCart
        category.contains("Fuel", true) || category.contains("Petrol", true) || category.contains("Transport", true) -> Icons.Default.LocalGasStation
        category.contains("Shop", true) || category.contains("Online", true) -> Icons.Default.ShoppingBag
        category.contains("Util", true) || category.contains("Bill", true) || category.contains("Subscript", true) -> Icons.Default.ReceiptLong
        category.contains("Health", true) || category.contains("Pharm", true) || category.contains("Clinic", true) -> Icons.Default.MedicalServices
        category.contains("Snack", true) || category.contains("Dessert", true) -> Icons.Default.Fastfood
        category.contains("Top Up", true) || category.contains("Credit", true) -> Icons.Default.AccountBalanceWallet
        category.contains("Tolong Beli", true) || category.contains("Help Buy", true) -> Icons.Default.VolunteerActivism
        category.contains("Pet", true) || category.contains("Cat", true) || category.contains("Dog", true) -> Icons.Default.Pets
        category.contains("Gym", true) || category.contains("Sport", true) || category.contains("Fitness", true) -> Icons.Default.FitnessCenter
        category.contains("Game", true) || category.contains("Gaming", true) -> Icons.Default.SportsEsports
        category.contains("Movie", true) || category.contains("Cinema", true) || category.contains("Fun", true) -> Icons.Default.Movie
        category.contains("Travel", true) || category.contains("Flight", true) || category.contains("Holiday", true) -> Icons.Default.Flight
        category.contains("School", true) || category.contains("Edu", true) || category.contains("Book", true) -> Icons.Default.School
        category.contains("Gift", true) || category.contains("Charity", true) -> Icons.Default.CardGiftcard
        category.contains("Beauty", true) || category.contains("Barber", true) || category.contains("Salon", true) -> Icons.Default.Spa
        category.contains("Tech", true) || category.contains("Gadget", true) -> Icons.Default.Devices
        category.contains("Home", true) || category.contains("Rent", true) || category.contains("Laundry", true) -> Icons.Default.Home
        category.contains("Work", true) || category.contains("Office", true) -> Icons.Default.Work
        else -> Icons.Default.Label
    }
}

fun getPaymentMethodIcon(method: String): ImageVector {
    return when {
        method.contains("QR", true) || method.contains("DuitNow", true) -> Icons.Default.QrCode2
        method.contains("TNG", true) || method.contains("Wallet", true) || method.contains("Touch", true) -> Icons.Default.AccountBalanceWallet
        method.contains("Card", true) || method.contains("Visa", true) || method.contains("Master", true) -> Icons.Default.CreditCard
        method.contains("Bank", true) || method.contains("FPX", true) || method.contains("Transfer", true) -> Icons.Default.AccountBalance
        else -> Icons.Default.Payments
    }
}

@Composable
fun PaymentMethodBadge(method: String, modifier: Modifier = Modifier) {
    val clean = method.replace(Regex("\\s+"), " ").trim()
    if (clean.isEmpty()) return
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = getPaymentMethodIcon(clean),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = clean,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun TransactionCardItem(
    tx: Transaction,
    onDelete: () -> Unit,
    onToggleSettled: () -> Unit = {},
    onShareClaim: () -> Unit = {}
) {
    val color = CategoryRegistry.resolveColor(tx.category)
    val icon = getCategoryIcon(tx.category)
    val isHelp = tx.isHelpBuy || tx.category.contains("Tolong Beli", ignoreCase = true)
    val cleanTitle = tx.title.replace(Regex("\\s+"), " ").trim().ifEmpty { tx.category }
    val cleanMethod = tx.method.replace(Regex("\\s+"), " ").trim()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = glassCardColors(MaterialTheme.colorScheme.surfaceContainer),
        border = appCardBorder()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Category Squircle Icon
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = color.copy(alpha = 0.18f),
                        border = BorderStroke(1.dp, color.copy(alpha = 0.35f)),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = icon,
                                contentDescription = tx.category,
                                tint = color,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text = cleanTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            text = "${tx.date} • ${tx.category}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (cleanMethod.isNotEmpty() || isHelp) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                if (cleanMethod.isNotEmpty()) {
                                    PaymentMethodBadge(cleanMethod)
                                }
                                if (isHelp) {
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(0xFF38BDF8).copy(alpha = 0.18f),
                                        border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.35f))
                                    ) {
                                        Text(
                                            "Claim",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF38BDF8),
                                            maxLines = 1,
                                            softWrap = false,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = formatRM(tx.amount),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        maxLines = 1,
                        softWrap = false,
                        color = if (tx.type == "credit") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                    if (isHelp) {
                        IconButton(onClick = onShareClaim, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Outlined.Share, contentDescription = "Share Claim", tint = Color(0xFF38BDF8), modifier = Modifier.size(18.dp))
                        }
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Outlined.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(18.dp))
                    }
                }
            }

            // Tolong Beli Recipient & Settlement Status Row
            if (isHelp) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF38BDF8).copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.35f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFF38BDF8))
                            Text(
                                text = if (tx.claimRecipient.isNotBlank()) "For ${tx.claimRecipient}" else "Tolong Beli",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF38BDF8)
                            )
                        }
                    }

                    Surface(
                        shape = CircleShape,
                        color = if (tx.isSettled) Color(0xFF10B981).copy(alpha = 0.18f) else Color(0xFFF59E0B).copy(alpha = 0.18f),
                        border = BorderStroke(1.dp, if (tx.isSettled) Color(0xFF10B981).copy(alpha = 0.45f) else Color(0xFFF59E0B).copy(alpha = 0.45f)),
                        modifier = Modifier.clickable { onToggleSettled() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                if (tx.isSettled) Icons.Default.CheckCircle else Icons.Default.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = if (tx.isSettled) Color(0xFF10B981) else Color(0xFFF59E0B)
                            )
                            Text(
                                text = if (tx.isSettled) "Settled (Tap to undo)" else "Unpaid (Tap to settle)",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (tx.isSettled) Color(0xFF10B981) else Color(0xFFF59E0B)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionRow(tx: Transaction, onDelete: () -> Unit) {
    val color = CategoryRegistry.resolveColor(tx.category)
    val icon = getCategoryIcon(tx.category)
    val isHelp = tx.isHelpBuy || tx.category.contains("Tolong Beli", ignoreCase = true)

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = glassSurfaceColor(MaterialTheme.colorScheme.surfaceContainer).copy(alpha = if (LocalIsWallpaperActive.current) 0.5f else 0.7f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = if (LocalIsWallpaperActive.current) 0.12f else 0.05f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Category squircle icon
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = color.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, color.copy(alpha = 0.35f)),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = tx.category,
                            tint = color,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    val cleanTitle = tx.title.replace(Regex("\\s+"), " ").trim().ifEmpty { tx.category }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = cleanTitle,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (isHelp) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF38BDF8).copy(alpha = 0.2f)
                            ) {
                                Text(
                                    "Claim",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF38BDF8),
                                    maxLines = 1,
                                    softWrap = false,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = "${tx.date} • ${tx.category}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Text(
                text = formatRM(tx.amount),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Black,
                color = if (tx.type == "credit") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

// -------------------------------------------------------------
// ADD EXPENSE BOTTOM SHEET
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseSheet(
    customCategories: List<CustomCategory> = emptyList(),
    recentRecipients: List<String> = emptyList(),
    initialCategory: String? = null,
    onDismiss: () -> Unit,
    onAddCustomCategory: (CustomCategory) -> Unit = {},
    onManageCategories: () -> Unit = {},
    onAdd: (Transaction) -> Unit
) {
    val allCategories = remember(customCategories) {
        CategoryRegistry.allCategories(customCategories)
    }
    var amountStr by remember { mutableStateOf("") }
    var titleStr by remember { mutableStateOf("") }
    var selectedCat by remember(initialCategory, allCategories) {
        mutableStateOf(
            initialCategory?.takeIf { catName -> allCategories.any { it.name.equals(catName, ignoreCase = true) } }
                ?: initialCategory
                ?: allCategories.firstOrNull()?.name
                ?: DEFAULT_CATEGORIES[0].name
        )
    }
    var selectedMethod by remember { mutableStateOf("DuitNow QR") }
    var dateStr by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(Date())) }
    var showNewCatSheet by remember { mutableStateOf(false) }

    val isTolongBeliCat = selectedCat.contains("Tolong Beli", ignoreCase = true)
    var isHelpBuy by remember(isTolongBeliCat) { mutableStateOf(isTolongBeliCat) }
    var claimRecipient by remember { mutableStateOf("") }
    var isSettled by remember { mutableStateOf(false) }

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

            // Quick Preset Amount Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(5, 10, 20, 50, 100).forEach { preset ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.6f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                val currentVal = amountStr.toDoubleOrNull() ?: 0.0
                                amountStr = String.format(Locale.US, "%.2f", currentVal + preset)
                            }
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            Text(
                                "+$preset",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Description (OPTIONAL)
            OutlinedTextField(
                value = titleStr,
                onValueChange = { titleStr = it },
                label = { Text("Description (Optional)") },
                placeholder = { Text("e.g. $selectedCat") },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )

            // Tolong Beli Toggle & Recipient Card
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = if (isHelpBuy) Color(0xFF38BDF8).copy(alpha = 0.12f) else glassSurfaceColor(MaterialTheme.colorScheme.surfaceContainer),
                border = BorderStroke(1.dp, if (isHelpBuy) Color(0xFF38BDF8).copy(alpha = 0.45f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.VolunteerActivism, contentDescription = null, tint = if (isHelpBuy) Color(0xFF38BDF8) else MaterialTheme.colorScheme.onSurfaceVariant)
                            Column {
                                Text("Tolong Beli (Bought for someone)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                Text("Tag friend & share claim easily", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Switch(
                            checked = isHelpBuy,
                            onCheckedChange = {
                                isHelpBuy = it
                                if (it && !isTolongBeliCat) {
                                    selectedCat = "Tolong Beli (Help Buy)"
                                }
                            }
                        )
                    }

                    if (isHelpBuy) {
                        OutlinedTextField(
                            value = claimRecipient,
                            onValueChange = { claimRecipient = it },
                            label = { Text("Who did you help buy for? (Friend's Name)") },
                            placeholder = { Text("e.g. Ah Ming, Sarah, Mom") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF38BDF8)) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        if (recentRecipients.isNotEmpty()) {
                            Text("Recent friends:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Row(
                                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                recentRecipients.forEach { name ->
                                    SuggestionChip(
                                        onClick = { claimRecipient = name },
                                        label = { Text(name, fontSize = 11.sp) },
                                        shape = CircleShape
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Payment Status:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                            FilterChip(
                                selected = isSettled,
                                onClick = { isSettled = !isSettled },
                                leadingIcon = {
                                    Icon(
                                        if (isSettled) Icons.Default.CheckCircle else Icons.Default.Schedule,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = if (isSettled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                label = { Text(if (isSettled) "Already Paid" else "Pending Claim", fontWeight = FontWeight.Bold) },
                                shape = CircleShape
                            )
                        }
                    }
                }
            }

            // Category Picker
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Category", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    TextButton(
                        onClick = { showNewCatSheet = true },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("New Custom", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }
                    TextButton(
                        onClick = onManageCategories,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text("Manage", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AssistChip(
                    onClick = { showNewCatSheet = true },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    label = { Text("Custom", fontWeight = FontWeight.Bold) },
                    shape = CircleShape
                )
                allCategories.forEach { cat ->
                    FilterChip(
                        selected = selectedCat == cat.name,
                        onClick = {
                            selectedCat = cat.name
                            if (cat.name.contains("Tolong Beli", ignoreCase = true)) {
                                isHelpBuy = true
                            }
                        },
                        leadingIcon = {
                            Icon(
                                getCategoryIcon(cat.name),
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = cat.color
                            )
                        },
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
                        val finalTitle = titleStr.trim().ifEmpty {
                            if (isHelpBuy && claimRecipient.isNotBlank()) "Bought for $claimRecipient" else selectedCat
                        }
                        onAdd(
                            Transaction(
                                date = dateStr,
                                title = finalTitle,
                                amount = amt,
                                category = selectedCat,
                                method = selectedMethod,
                                isHelpBuy = isHelpBuy,
                                claimRecipient = if (isHelpBuy) claimRecipient.trim() else "",
                                isSettled = isSettled
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

    if (showNewCatSheet) {
        CustomCategorySheet(
            onDismiss = { showNewCatSheet = false },
            onSave = { newCat ->
                onAddCustomCategory(newCat)
                selectedCat = newCat.name
                showNewCatSheet = false
            }
        )
    }
}

// -------------------------------------------------------------
// SHARE CLAIM DIALOG (WHATSAPP & APP INTENT SHARE)
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareClaimDialog(
    recipientName: String,
    transactionsToClaim: List<Transaction>,
    savedDuitNow: String,
    onDismiss: () -> Unit,
    onMarkAllSettled: () -> Unit,
    onSaveDuitNow: (String) -> Unit
) {
    val context = LocalContext.current
    var customDuitNow by remember(savedDuitNow) { mutableStateOf(savedDuitNow) }
    var showDuitNowEdit by remember { mutableStateOf(savedDuitNow.isBlank()) }
    var includeDuitNow by remember { mutableStateOf(true) }
    var customNote by remember { mutableStateOf("") }

    val totalAmount = remember(transactionsToClaim) {
        transactionsToClaim.sumOf { it.amount }
    }

    val displayRecipient = if (recipientName.isNotBlank() && recipientName != "All People") recipientName else "friend"

    // Construct the formatted WhatsApp / Text claim
    val claimMessage = remember(recipientName, transactionsToClaim, customDuitNow, includeDuitNow, customNote) {
        val sb = StringBuilder()
        sb.append("🧾 *RinggitWise Claim • Tolong Beli*\n\n")
        sb.append("Hey $displayRecipient! 👋\n")
        if (transactionsToClaim.size == 1) {
            val tx = transactionsToClaim.first()
            sb.append("Here's the receipt for what I helped you buy:\n\n")
            sb.append("📅 *Date:* ${tx.date}\n")
            sb.append("🛍️ *Item:* ${tx.title}\n")
            sb.append("💰 *Amount:* ${formatRM(tx.amount)}\n")
            if (tx.note.isNotBlank()) {
                sb.append("📝 *Note:* ${tx.note}\n")
            }
        } else {
            sb.append("Here is the summary of items I helped you buy (${transactionsToClaim.size} items):\n\n")
            transactionsToClaim.forEach { tx ->
                sb.append("• ${tx.date}: ${tx.title} — ${formatRM(tx.amount)}\n")
            }
        }

        if (customNote.isNotBlank()) {
            sb.append("\n💬 $customNote\n")
        }

        sb.append("\n────────────────────────\n")
        sb.append("👉 *Total to transfer:* *${formatRM(totalAmount)}*\n")
        sb.append("────────────────────────\n")

        if (includeDuitNow && customDuitNow.isNotBlank()) {
            sb.append("\n📲 *DuitNow / Transfer Details:*\n$customDuitNow\n")
        }
        sb.append("\nThanks bro! Let me know once transferred 🙌")
        sb.toString()
    }

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Share Claim Receipt", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("For $displayRecipient • Total ${formatRM(totalAmount)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF38BDF8).copy(alpha = 0.2f),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = Color(0xFF38BDF8))
                    }
                }
            }

            // Receipt Preview Card
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = glassSurfaceColor(MaterialTheme.colorScheme.surfaceContainerHighest),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Preview Message", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
                    Text(
                        text = claimMessage,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Custom note input
            OutlinedTextField(
                value = customNote,
                onValueChange = { customNote = it },
                label = { Text("Add Custom Note (Optional)") },
                placeholder = { Text("e.g. Bro pay me before Friday ya") },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // DuitNow toggle and edit
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = glassSurfaceColor(MaterialTheme.colorScheme.surfaceContainer),
                border = glassCardBorder(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.SendToMobile, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Text("Include DuitNow / Payment Details", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                        }
                        Switch(checked = includeDuitNow, onCheckedChange = { includeDuitNow = it })
                    }

                    if (includeDuitNow) {
                        if (customDuitNow.isBlank() || showDuitNowEdit) {
                            OutlinedTextField(
                                value = customDuitNow,
                                onValueChange = {
                                    customDuitNow = it
                                    onSaveDuitNow(it)
                                },
                                label = { Text("DuitNow / Account Details") },
                                placeholder = { Text("e.g. 012-3456789 (Maybank - Tan)") },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(customDuitNow, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                TextButton(onClick = { showDuitNowEdit = true }) {
                                    Text("Edit")
                                }
                            }
                        }
                    }
                }
            }

            // Primary Actions: WhatsApp / Apps Share & Copy
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, claimMessage)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Share Claim with $displayRecipient via"))
                    },
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share (WhatsApp)", fontWeight = FontWeight.Bold)
                }

                FilledTonalButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("RinggitWise Claim", claimMessage))
                        Toast.makeText(context, "Claim receipt copied to clipboard! 📋", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.height(50.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Copy")
                }
            }

            // Settle button
            OutlinedButton(
                onClick = {
                    onMarkAllSettled()
                    Toast.makeText(context, "Marked as settled! 🎉", Toast.LENGTH_SHORT).show()
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Mark as Settled (Paid Back)")
            }

            Spacer(modifier = Modifier.height(16.dp))
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
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(
                                Icons.Default.AllInclusive,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = if (currentMonth == "all") MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.primary
                            )
                            Text(
                                "All Time (Everything)",
                                fontWeight = if (currentMonth == "all") FontWeight.Bold else FontWeight.Medium,
                                color = if (currentMonth == "all") MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                        }
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
                                "Drag to pan • Pinch to zoom",
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
    geminiApiKey: String,
    geminiModel: String,
    onSetGeminiApiKey: (String) -> Unit,
    onSetGeminiModel: (String) -> Unit,
    onTestGeminiKey: suspend (String, String) -> Result<String>,
    userDuitNowInfo: String = "",
    onSetDuitNowInfo: (String) -> Unit = {},
    monthlyBudget: Double,
    onEditBudget: () -> Unit,
    onClearAllData: () -> Unit,
    autoCheckUpdates: Boolean = true,
    onSetAutoCheckUpdates: (Boolean) -> Unit = {},
    githubRepoSlug: String = UpdateManager.DEFAULT_REPO,
    onSetGithubRepoSlug: (String) -> Unit = {},
    availableUpdate: AppUpdateInfo? = null,
    onCheckForUpdates: suspend () -> Result<AppUpdateInfo> = { Result.failure(Exception()) },
    onOpenUpdateModal: (AppUpdateInfo) -> Unit = {}
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

        // Section 3: Google Gemini AI Configuration
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = glassCardColors(MaterialTheme.colorScheme.surfaceContainerHigh),
            border = glassCardBorder()
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text("Google Gemini AI Assistant", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }

                Text(
                    "Intelligent statement & receipt vision parser (PDF & images) and natural language financial tools. Uses Google's free API key.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                var localKey by remember(geminiApiKey) { mutableStateOf(geminiApiKey) }
                var showPassword by remember { mutableStateOf(false) }
                var localModel by remember(geminiModel) {
                    val initial = if (geminiModel.isBlank() || geminiModel == "gemini-2.5-flash-lite" || geminiModel == "gemini-2.5-flash") "gemini-3.5-flash-lite" else geminiModel
                    if (initial != geminiModel) onSetGeminiModel(initial)
                    mutableStateOf(initial)
                }
                val scope = rememberCoroutineScope()
                var isTesting by remember { mutableStateOf(false) }
                var testResult by remember { mutableStateOf<String?>(null) }
                var isTestSuccess by remember { mutableStateOf(false) }

                OutlinedTextField(
                    value = localKey,
                    onValueChange = {
                        localKey = it
                        onSetGeminiApiKey(it)
                        testResult = null
                    },
                    label = { Text("Gemini API Key") },
                    placeholder = { Text("Paste AIzaSy... key") },
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Toggle Visibility"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = glassSurfaceColor(MaterialTheme.colorScheme.surfaceContainer),
                    border = glassCardBorder(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                        Text(
                            "Get your 100% free API key from aistudio.google.com (zero credit card required).",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                OutlinedTextField(
                    value = localModel,
                    onValueChange = {
                        localModel = it
                        onSetGeminiModel(it)
                        testResult = null
                    },
                    label = { Text("Gemini Model") },
                    placeholder = { Text("gemini-3.5-flash-lite") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )

                Text("Quick Select Model:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                val availableModels = listOf("gemini-3.5-flash-lite", "gemini-3.5-flash", "gemini-2.0-flash")
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    availableModels.forEach { m ->
                        val isSelected = localModel == m
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                localModel = m
                                onSetGeminiModel(m)
                                testResult = null
                            },
                            label = { Text(m, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            shape = CircleShape
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            scope.launch {
                                isTesting = true
                                testResult = null
                                val res = onTestGeminiKey(localKey, localModel)
                                isTesting = false
                                res.onSuccess {
                                    isTestSuccess = true
                                    testResult = it
                                }.onFailure { err ->
                                    isTestSuccess = false
                                    testResult = err.message ?: "Connection test failed"
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        enabled = !isTesting && localKey.isNotBlank()
                    ) {
                        if (isTesting) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Testing...")
                        } else {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Test Key")
                        }
                    }
                }

                if (testResult != null) {
                    if (isTestSuccess) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Column {
                                    Text(
                                        "Gemini AI Connected!",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        "Model $localModel is ready for receipt scanning & AI ledger actions.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                                    )
                                }
                            }
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.errorContainer,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Cancel,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Text(
                                        "Connection Issue",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }

                                val err = testResult ?: "Unknown error"
                                val isModelRetired = err.contains("404") || err.contains("no longer available") || err.contains("not found")
                                val isKeyInvalid = err.contains("400") || err.contains("API_KEY_INVALID") || err.contains("key not valid")

                                Text(
                                    text = when {
                                        isModelRetired -> "This model is retired or not supported by Google. Please switch to gemini-3.5-flash-lite."
                                        isKeyInvalid -> "API key is invalid. Please make sure you copied the exact key from aistudio.google.com without spaces."
                                        else -> err.take(200)
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )

                                if (isModelRetired && localModel != "gemini-3.5-flash-lite") {
                                    FilledTonalButton(
                                        onClick = {
                                            localModel = "gemini-3.5-flash-lite"
                                            onSetGeminiModel("gemini-3.5-flash-lite")
                                            testResult = null
                                        },
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("Switch to gemini-3.5-flash-lite", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section 4: Budget Ceiling Card
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

        // Section 4: Tolong Beli & DuitNow Payment Info
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
                    Icon(Icons.Default.SendToMobile, contentDescription = null, tint = Color(0xFF38BDF8))
                    Text("Tolong Beli • Claim Payment Info", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }

                Text(
                    "Set your default DuitNow ID, TNG eWallet number, or Bank Account. When you share claims with friends via WhatsApp, this is automatically attached for easy transfer.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                var localDuitNow by remember(userDuitNowInfo) { mutableStateOf(userDuitNowInfo) }
                OutlinedTextField(
                    value = localDuitNow,
                    onValueChange = {
                        localDuitNow = it
                        onSetDuitNowInfo(it)
                    },
                    label = { Text("My DuitNow / Bank Info") },
                    placeholder = { Text("e.g. 012-3456789 (Maybank / TNG - Tan)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )
            }
        }

        // Section 5: App Updates & GitHub Releases
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = glassCardColors(MaterialTheme.colorScheme.surfaceContainerHigh),
            border = glassCardBorder()
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.CloudSync, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text("App Updates & Releases", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }

                Text(
                    "Syncs with GitHub Releases. Seamlessly checks for new versions, downloads the APK in the background, and triggers one-tap install.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Version status row
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = glassSurfaceColor(MaterialTheme.colorScheme.surfaceContainer),
                    border = glassCardBorder(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Installed Version", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("v${BuildConfig.VERSION_NAME}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }

                        if (availableUpdate != null && availableUpdate.hasUpdate) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.errorContainer
                            ) {
                                Text(
                                    "Update: ${availableUpdate.latestVersion}",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        } else {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    "Latest Version",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }

                // Auto-check Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Auto-check for updates", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Text("Silently checks GitHub Releases at app launch", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = autoCheckUpdates,
                        onCheckedChange = onSetAutoCheckUpdates
                    )
                }

                // GitHub Repository Slug
                var localSlug by remember(githubRepoSlug) { mutableStateOf(githubRepoSlug) }
                OutlinedTextField(
                    value = localSlug,
                    onValueChange = {
                        localSlug = it
                        onSetGithubRepoSlug(it)
                    },
                    label = { Text("GitHub Repository (owner/repo)") },
                    placeholder = { Text("Kwan-desu/RinggitWise") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )

                // Check Button & Status
                val scope = rememberCoroutineScope()
                var isChecking by remember { mutableStateOf(false) }
                var checkMessage by remember { mutableStateOf<String?>(null) }
                var isCheckSuccess by remember { mutableStateOf(true) }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            isChecking = true
                            checkMessage = null
                            scope.launch {
                                val res = onCheckForUpdates()
                                isChecking = false
                                res.onSuccess { info ->
                                    isCheckSuccess = true
                                    if (info.hasUpdate) {
                                        checkMessage = "Found ${info.latestVersion}! Tap View Update."
                                        onOpenUpdateModal(info)
                                    } else {
                                        checkMessage = "You're already on the latest version (${info.currentVersion})."
                                    }
                                }.onFailure { err ->
                                    isCheckSuccess = false
                                    checkMessage = "Check failed: ${err.message}"
                                }
                            }
                        },
                        enabled = !isChecking,
                        modifier = Modifier.weight(1.2f),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        if (isChecking) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Checking...")
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Check Updates")
                        }
                    }

                    if (availableUpdate != null && availableUpdate.hasUpdate) {
                        FilledTonalButton(
                            onClick = { onOpenUpdateModal(availableUpdate) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.ArrowOutward, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("View Update")
                        }
                    }
                }

                if (checkMessage != null) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isCheckSuccess) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            checkMessage!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isCheckSuccess) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        }

        // Section 6: Data Management & Reset
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
            "RinggitWise Native v${BuildConfig.VERSION_NAME} • Material You • Made for Malaysia",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(24.dp))
    }
}

// -------------------------------------------------------------
// APP UPDATE MODAL DIALOG
// -------------------------------------------------------------
@Composable
fun AppUpdateDialog(
    updateInfo: AppUpdateInfo,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isDownloading by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableFloatStateOf(0f) }
    var downloadError by remember { mutableStateOf<String?>(null) }
    var downloadedFile by remember { mutableStateOf<File?>(null) }

    Dialog(
        onDismissRequest = {
            if (!isDownloading) onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.SystemUpdate,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            "New Version Available",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "${updateInfo.currentVersion} ➔ ${updateInfo.latestVersion}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                // Release Info & Notes
                if (updateInfo.releaseTitle.isNotBlank()) {
                    Text(
                        updateInfo.releaseTitle,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(14.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            updateInfo.releaseNotes.ifBlank { "Minor bug fixes and performance improvements." },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (updateInfo.apkFileSize > 0) {
                    val sizeMb = String.format(Locale.US, "%.1f MB", updateInfo.apkFileSize / (1024.0 * 1024.0))
                    Text(
                        "Package size: $sizeMb",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Download Progress
                if (isDownloading) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Downloading update...",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                "${(downloadProgress * 100).toInt()}%",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        LinearProgressIndicator(
                            progress = { downloadProgress.coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(CircleShape),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                        )
                    }
                }

                if (downloadError != null) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = downloadError!!,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (!isDownloading) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("Later")
                        }
                    }

                    if (downloadedFile != null && downloadedFile!!.exists()) {
                        Button(
                            onClick = {
                                UpdateManager.installApk(context, downloadedFile!!)
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.DownloadDone, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Install Now")
                        }
                    } else if (updateInfo.apkDownloadUrl != null) {
                        Button(
                            onClick = {
                                isDownloading = true
                                downloadError = null
                                downloadProgress = 0f
                                scope.launch {
                                    val result = UpdateManager.downloadApk(
                                        context = context,
                                        downloadUrl = updateInfo.apkDownloadUrl,
                                        fileName = updateInfo.apkFileName
                                    ) { progress ->
                                        downloadProgress = progress
                                    }
                                    isDownloading = false
                                    result.onSuccess { file ->
                                        downloadedFile = file
                                        UpdateManager.installApk(context, file)
                                    }.onFailure { err ->
                                        downloadError = "Download failed: ${err.message}"
                                    }
                                }
                            },
                            enabled = !isDownloading,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            if (isDownloading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Downloading...")
                            } else {
                                Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Download APK")
                            }
                        }
                    } else {
                        Button(
                            onClick = {
                                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(updateInfo.htmlUrl)).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(browserIntent)
                                onDismiss()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Open in GitHub")
                        }
                    }
                }
            }
        }
    }
}
