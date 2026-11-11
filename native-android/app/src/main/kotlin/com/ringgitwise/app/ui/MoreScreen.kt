package com.ringgitwise.app.ui

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.graphics.Bitmap
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ringgitwise.app.BuildConfig
import com.ringgitwise.app.data.*
import com.ringgitwise.app.ui.theme.Danger
import com.ringgitwise.app.ui.theme.Info
import com.ringgitwise.app.ui.theme.Warning
import com.ringgitwise.app.widget.SpendWidgetProvider
import java.text.SimpleDateFormat
import java.util.*

// -------------------------------------------------------------
// MORE hub
// -------------------------------------------------------------
@Composable
fun MoreScreen(
    avatarBitmap: Bitmap?,
    profileEmoji: String,
    bills: List<RecurringBill>,
    goals: List<SavingsGoal>,
    customCategoriesCount: Int = 0,
    pendingClaimsCount: Int,
    themeMode: String,
    onSetThemeMode: (String) -> Unit,
    onOpenBills: () -> Unit,
    onOpenGoals: () -> Unit,
    onOpenCategories: () -> Unit = {},
    onOpenClaims: () -> Unit,
    onOpenExport: () -> Unit,
    onOpenScan: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val context = LocalContext.current
    val fixed = bills.filter { it.isActive }.sumOf { it.monthlyEquivalent() }
    val saved = goals.sumOf { it.saved }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Space.lg)
            .padding(top = Space.sm, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(Space.md)
    ) {
        // Profile header
        AppCard(onClick = onOpenSettings, container = MaterialTheme.colorScheme.surfaceContainerHigh) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Space.lg)) {
                Box(
                    Modifier.size(56.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    if (avatarBitmap != null) {
                        Image(avatarBitmap.asImageBitmap(), contentDescription = "Profile photo", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    } else if (profileEmoji.isNotBlank() && profileEmoji != "RM") {
                        Text(profileEmoji, fontSize = 24.sp)
                    } else {
                        Text("RM", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
                Column(Modifier.weight(1f)) {
                    Text("RinggitWise", style = MaterialTheme.typography.titleLarge)
                    Text("Profile, AI, wallpaper & updates", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        SectionHeader("Money")
        AppCard(contentPadding = PaddingValues(horizontal = Space.lg, vertical = Space.xs)) {
            ListRow(Icons.Default.EventRepeat, Warning, "Bills & subscriptions",
                if (bills.isEmpty()) "Add recurring payments and get reminders" else "${bills.size} active • ${formatRMExact(fixed)} / month",
                onClick = onOpenBills)
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            ListRow(Icons.Default.Savings, Color(0xFFA78BFA), "Savings goals",
                if (goals.isEmpty()) "Save towards something you want" else "${goals.size} goals • ${formatRMExact(saved)} saved",
                onClick = onOpenGoals)
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            ListRow(Icons.Default.Category, Color(0xFF06B6D4), "Categories",
                if (customCategoriesCount == 0) "Create your own custom categories, icons & colours"
                else "$customCategoriesCount custom • ${DEFAULT_CATEGORIES.size} built-in",
                onClick = onOpenCategories)
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            ListRow(Icons.Default.VolunteerActivism, Info, "Tolong Beli claims",
                if (pendingClaimsCount == 0) "No pending claims" else "$pendingClaimsCount pending to collect",
                onClick = onOpenClaims)
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            ListRow(Icons.Default.DocumentScanner, MaterialTheme.colorScheme.primary, "Scan receipt or statement",
                "On-device OCR or Gemini AI for PDFs", onClick = onOpenScan)
        }

        SectionHeader("Data")
        AppCard(contentPadding = PaddingValues(horizontal = Space.lg, vertical = Space.xs)) {
            ListRow(Icons.Default.IosShare, MaterialTheme.colorScheme.primary, "Export & backup", "PDF report, CSV, save or restore a backup", onClick = onOpenExport)
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            ListRow(Icons.Default.Widgets, Info, "Home-screen widget", "Today's spend and budget left at a glance", onClick = {
                val mgr = AppWidgetManager.getInstance(context)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && mgr.isRequestPinAppWidgetSupported) {
                    mgr.requestPinAppWidget(ComponentName(context, SpendWidgetProvider::class.java), null, null)
                } else {
                    Toast.makeText(context, "Long-press your home screen → Widgets → RinggitWise", Toast.LENGTH_LONG).show()
                }
            })
        }

        SectionHeader("Appearance")
        AppCard {
            ThemeModeSelector(
                themeMode = themeMode,
                onSelect = onSetThemeMode
            )
        }

        AppCard(contentPadding = PaddingValues(horizontal = Space.lg, vertical = Space.xs)) {
            ListRow(Icons.Outlined.Settings, MaterialTheme.colorScheme.secondary, "Settings", "Budget, Gemini AI key, wallpaper, DuitNow, updates", onClick = onOpenSettings)
        }

        Text(
            "Version ${BuildConfig.VERSION_NAME}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth().padding(top = Space.sm),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

private data class ThemeChoice(
    val key: String,
    val label: String,
    val subtitle: String,
    val icon: ImageVector,
    val swatch: Color,
    val swatchBorder: Color
)

/** Shared 2x2 selector for Dark, Light, Light Grey, and System themes. */
@Composable
fun ThemeModeSelector(
    themeMode: String,
    onSelect: (String) -> Unit
) {
    val choices = listOf(
        ThemeChoice("dark", "Dark", "OLED black", Icons.Outlined.DarkMode, Color(0xFF09090B), Color(0xFF3F3F46)),
        ThemeChoice("light", "Light", "Crisp white", Icons.Outlined.LightMode, Color(0xFFFFFFFF), Color(0xFFD4D4D8)),
        ThemeChoice("light_grey", "Light Grey", "Soft neutral", Icons.Outlined.Contrast, Color(0xFFDADCE2), Color(0xFF9CA3AF)),
        ThemeChoice("system", "System", "Match phone", Icons.Outlined.SettingsBrightness, Color(0xFF71717A), Color(0xFF52525B))
    )

    Column(verticalArrangement = Arrangement.spacedBy(Space.sm)) {
        choices.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Space.sm)
            ) {
                rowItems.forEach { item ->
                    val selected = themeMode == item.key
                    Surface(
                        onClick = { onSelect(item.key) },
                        shape = RoundedCornerShape(16.dp),
                        color = if (selected) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceContainerHigh
                        },
                        border = BorderStroke(
                            width = if (selected) 1.5.dp else 1.dp,
                            color = if (selected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.outlineVariant
                            }
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = Space.md, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(Space.sm)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(item.swatch)
                            ) {
                                Surface(
                                    modifier = Modifier.fillMaxSize(),
                                    shape = CircleShape,
                                    color = Color.Transparent,
                                    border = BorderStroke(1.dp, item.swatchBorder)
                                ) {}
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    item.label,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    item.subtitle,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            if (selected) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Centre "+" action sheet
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddActionSheet(
    onDismiss: () -> Unit,
    onAddExpense: () -> Unit,
    onScan: () -> Unit,
    onAddBill: () -> Unit,
    onAddToGoal: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss, shape = RoundedCornerShape(topStart = Radius.hero, topEnd = Radius.hero)) {
        Column(
            Modifier.fillMaxWidth().padding(horizontal = Space.xl).padding(bottom = Space.xl),
            verticalArrangement = Arrangement.spacedBy(Space.md)
        ) {
            Text("Quick add", style = MaterialTheme.typography.headlineSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(Space.md)) {
                ActionTile(Icons.Default.Add, "Expense", "Log what you spent", MaterialTheme.colorScheme.primary, Modifier.weight(1f)) { onDismiss(); onAddExpense() }
                ActionTile(Icons.Default.DocumentScanner, "Scan", "Receipt or PDF", Info, Modifier.weight(1f)) { onDismiss(); onScan() }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(Space.md)) {
                ActionTile(Icons.Default.EventRepeat, "Bill", "Recurring payment", Warning, Modifier.weight(1f)) { onDismiss(); onAddBill() }
                ActionTile(Icons.Default.Savings, "Goal", "Save money", Color(0xFFA78BFA), Modifier.weight(1f)) { onDismiss(); onAddToGoal() }
            }
        }
    }
}

@Composable
private fun ActionTile(icon: ImageVector, title: String, subtitle: String, tint: Color, modifier: Modifier, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = modifier.heightIn(min = 112.dp),
        shape = RoundedCornerShape(Radius.card),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        border = appCardBorder()
    ) {
        Column(Modifier.padding(Space.lg), verticalArrangement = Arrangement.spacedBy(Space.sm)) {
            IconBadge(icon, tint, size = 44.dp, iconSize = IconSize.lg)
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

// -------------------------------------------------------------
// Insights cards shown above the charts
// -------------------------------------------------------------
@Composable
fun InsightsHeader(allTransactions: List<Transaction>, monthExpenses: List<Transaction>) {
    val fmt = remember { SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH) }
    val insights = remember(allTransactions, monthExpenses) {
        val expenses = allTransactions.filter { it.type != "credit" && it.amount > 0 }
        val cal = Calendar.getInstance()
        val today = fmt.format(cal.time)
        cal.add(Calendar.DAY_OF_YEAR, -6)
        val weekStart = fmt.format(cal.time)
        cal.add(Calendar.DAY_OF_YEAR, -7)
        val prevStart = fmt.format(cal.time)
        cal.add(Calendar.DAY_OF_YEAR, 6)
        val prevEnd = fmt.format(cal.time)

        val thisWeek = expenses.filter { it.date in weekStart..today }.sumOf { it.amount }
        val lastWeek = expenses.filter { it.date in prevStart..prevEnd }.sumOf { it.amount }
        val change = if (lastWeek > 0) ((thisWeek - lastWeek) / lastWeek * 100).toInt() else null

        val topCat = monthExpenses.groupBy { it.category }.mapValues { e -> e.value.sumOf { it.amount } }.maxByOrNull { it.value }
        val topMerchant = monthExpenses.groupBy { it.title.trim().lowercase() }
            .maxByOrNull { e -> e.value.sumOf { it.amount } }
            ?.let { e -> e.value.first().title to e.value.sumOf { it.amount } }
        val activeDays = monthExpenses.map { it.date }.distinct().size.coerceAtLeast(1)
        val avg = monthExpenses.sumOf { it.amount } / activeDays
        val biggest = monthExpenses.maxByOrNull { it.amount }

        // Logging streak: consecutive days (ending today or yesterday) with at least one entry
        val days = allTransactions.map { it.date }.toSet()
        var streak = 0
        val s = Calendar.getInstance()
        if (!days.contains(fmt.format(s.time))) s.add(Calendar.DAY_OF_YEAR, -1)
        while (days.contains(fmt.format(s.time))) { streak++; s.add(Calendar.DAY_OF_YEAR, -1) }

        Insights(thisWeek, lastWeek, change, topCat?.key, topCat?.value, topMerchant, avg, biggest, streak)
    }

    Column(verticalArrangement = Arrangement.spacedBy(Space.md), modifier = Modifier.fillMaxWidth()) {
        // Week comparison
        AppCard(container = MaterialTheme.colorScheme.surfaceContainerHigh, contentPadding = PaddingValues(Space.xl)) {
            Text("Last 7 days", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(verticalAlignment = Alignment.Bottom) {
                Text(formatRMExact(insights.thisWeek), style = MaterialTheme.typography.displaySmall, modifier = Modifier.weight(1f))
                if (insights.change != null) {
                    val up = insights.change > 0
                    StatusPill(
                        "${if (up) "+" else ""}${insights.change}% vs prior week",
                        if (up) Danger else MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(Modifier.height(Space.xs))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Space.xs)) {
                val up = (insights.change ?: 0) > 0
                Icon(
                    if (up) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                    contentDescription = null,
                    tint = if (up) Danger else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(IconSize.sm)
                )
                Text(
                    if (insights.lastWeek > 0) "Prior week: ${formatRMExact(insights.lastWeek)}" else "Not enough history for a comparison yet",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(Space.md)) {
            val topCatIcon = insights.topCat?.let { getCategoryIcon(it) } ?: Icons.Default.Category
            val topCatTint = insights.topCat?.let { CategoryRegistry.resolveColor(it) } ?: Warning
            InsightTile(topCatIcon, "Top category", insights.topCat ?: "—",
                insights.topCatAmount?.let { formatRMExact(it) } ?: "No data", topCatTint, Modifier.weight(1f))
            InsightTile(Icons.Default.Storefront, "Top merchant", insights.topMerchant?.first ?: "—",
                insights.topMerchant?.second?.let { formatRMExact(it) } ?: "No data", Info, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(Space.md)) {
            InsightTile(Icons.Default.CalendarToday, "Avg per active day", formatRMExact(insights.avgPerDay),
                "this month", MaterialTheme.colorScheme.primary, Modifier.weight(1f))
            InsightTile(Icons.Default.LocalFireDepartment, "Logging streak", "${insights.streak} ${if (insights.streak == 1) "day" else "days"}",
                insights.biggest?.let { "Biggest: ${formatRMWhole(it.amount)}" } ?: "Keep it going", Color(0xFFF97316), Modifier.weight(1f))
        }
    }
}

private data class Insights(
    val thisWeek: Double,
    val lastWeek: Double,
    val change: Int?,
    val topCat: String?,
    val topCatAmount: Double?,
    val topMerchant: Pair<String, Double>?,
    val avgPerDay: Double,
    val biggest: Transaction?,
    val streak: Int
)

@Composable
private fun InsightTile(icon: ImageVector, label: String, value: String, caption: String, tint: Color, modifier: Modifier) {
    AppCard(modifier = modifier) {
        IconBadge(icon, tint, size = 36.dp)
        Spacer(Modifier.height(Space.md))
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(caption, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}
