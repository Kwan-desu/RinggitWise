package com.ringgitwise.app.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ringgitwise.app.data.*
import com.ringgitwise.app.ui.theme.Danger
import com.ringgitwise.app.ui.theme.Info
import com.ringgitwise.app.ui.theme.Warning
import java.util.Calendar

/** Colour for budget usage: calm under 75%, amber up to 100%, red when over. */
@Composable
fun budgetColor(fraction: Float): Color = when {
    fraction > 1f -> Danger
    fraction >= 0.75f -> Warning
    else -> MaterialTheme.colorScheme.primary
}

@Composable
fun HomeScreen(
    selectedMonth: String,
    totalSpent: Double,
    monthlyBudget: Double,
    todaySpent: Double,
    dailySafeSpend: Double,
    daysRemaining: Int,
    recentTransactions: List<Transaction>,
    bills: List<RecurringBill>,
    goals: List<SavingsGoal>,
    pendingClaimsTotal: Double,
    pendingClaimsCount: Int,
    availableUpdate: AppUpdateInfo?,
    onOpenUpdate: () -> Unit,
    onDismissUpdate: () -> Unit,
    onEditBudget: () -> Unit,
    onAddExpense: () -> Unit,
    onScan: () -> Unit,
    onOpenBills: () -> Unit,
    onOpenGoals: () -> Unit,
    onPayBill: (RecurringBill) -> Unit,
    onOpenGoal: (SavingsGoal) -> Unit,
    onOpenActivity: () -> Unit,
    onOpenClaims: () -> Unit
) {
    val fraction = if (monthlyBudget > 0) (totalSpent / monthlyBudget).toFloat() else 0f
    val accent = budgetColor(fraction)
    val upcoming = remember(bills) {
        val cal = Calendar.getInstance()
        bills.filter { it.isActive && !it.isPaidThisPeriod(cal) }
            .sortedBy { it.daysUntilDue(cal) }
            .take(3)
    }
    val fixedMonthly = remember(bills) { bills.filter { it.isActive }.sumOf { it.monthlyEquivalent() } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Space.lg),
        verticalArrangement = Arrangement.spacedBy(Space.lg)
    ) {
        Spacer(Modifier.height(Space.xs))

        if (availableUpdate != null) {
            UpdateBanner(availableUpdate.latestVersion, onOpenUpdate, onDismissUpdate)
        }

        // ---------------- Hero: month spend vs budget ----------------
        AppCard(container = MaterialTheme.colorScheme.surfaceContainerHigh, contentPadding = PaddingValues(Space.xl)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Spent in ${formatDisplayMonth(selectedMonth)}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                StatusPill(
                    text = when {
                        fraction > 1f -> "Over budget"
                        fraction >= 0.75f -> "Slow down"
                        else -> "On track"
                    },
                    color = accent
                )
            }
            Spacer(Modifier.height(Space.sm))
            Text(formatRMExact(totalSpent), style = MaterialTheme.typography.displayMedium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (totalSpent <= monthlyBudget) "${formatRMExact(monthlyBudget - totalSpent)} left of ${formatRMWhole(monthlyBudget)}"
                    else "${formatRMExact(totalSpent - monthlyBudget)} over ${formatRMWhole(monthlyBudget)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onEditBudget) {
                    Icon(Icons.Outlined.Edit, contentDescription = "Edit monthly budget", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(IconSize.md))
                }
            }
            Spacer(Modifier.height(Space.xs))
            SlimProgress(fraction, accent)
            Spacer(Modifier.height(Space.lg))
            Row(horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
                MiniStat("Today", formatRMExact(todaySpent), Modifier.weight(1f))
                MiniStat("Safe / day", formatRMExact(dailySafeSpend), Modifier.weight(1f))
                MiniStat("Days left", "$daysRemaining", Modifier.weight(1f))
            }
        }

        // ---------------- Quick actions ----------------
        Row(horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
            QuickTile(Icons.Default.Add, "Add", MaterialTheme.colorScheme.primary, Modifier.weight(1f), onAddExpense)
            QuickTile(Icons.Default.DocumentScanner, "Scan", Info, Modifier.weight(1f), onScan)
            QuickTile(Icons.Default.EventRepeat, "Bills", Warning, Modifier.weight(1f), onOpenBills)
            QuickTile(Icons.Default.Savings, "Goals", Color(0xFFA78BFA), Modifier.weight(1f), onOpenGoals)
        }

        // ---------------- Pending Tolong Beli ----------------
        if (pendingClaimsCount > 0) {
            AppCard(onClick = onOpenClaims) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Space.md)) {
                    IconBadge(Icons.Default.VolunteerActivism, Info)
                    Column(Modifier.weight(1f)) {
                        Text("${formatRMExact(pendingClaimsTotal)} to collect", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text(
                            "$pendingClaimsCount Tolong Beli ${if (pendingClaimsCount == 1) "claim" else "claims"} pending",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        // ---------------- Upcoming bills ----------------
        SectionHeader("Upcoming bills", actionLabel = "All bills", onAction = onOpenBills)
        AppCard(contentPadding = PaddingValues(horizontal = Space.lg, vertical = Space.sm)) {
            if (upcoming.isEmpty()) {
                ListRow(
                    icon = Icons.Default.EventRepeat,
                    tint = Warning,
                    title = if (bills.isEmpty()) "Track your bills & subscriptions" else "All bills paid this period",
                    subtitle = if (bills.isEmpty()) "Get reminded before Netflix, phone or rent is due" else "Fixed costs: ${formatRMExact(fixedMonthly)} / month",
                    onClick = onOpenBills
                )
            } else {
                upcoming.forEachIndexed { i, bill ->
                    val days = bill.daysUntilDue()
                    Row(
                        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).padding(vertical = Space.sm),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Space.md)
                    ) {
                        IconBadge(getCategoryIcon(bill.category), if (days < 0) Danger else Warning)
                        Column(Modifier.weight(1f)) {
                            Text(bill.name, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(
                                "${formatRMExact(bill.amount)} • ${formatDueLabel(days)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (days < 0) Danger else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        FilledTonalButton(
                            onClick = { onPayBill(bill) },
                            shape = CircleShape,
                            contentPadding = PaddingValues(horizontal = Space.lg),
                            modifier = Modifier.heightIn(min = 40.dp)
                        ) { Text("Paid") }
                    }
                    if (i < upcoming.lastIndex) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
        }

        // ---------------- Goals ----------------
        SectionHeader("Savings goals", actionLabel = "All goals", onAction = onOpenGoals)
        if (goals.isEmpty()) {
            AppCard(contentPadding = PaddingValues(horizontal = Space.lg, vertical = Space.sm)) {
                ListRow(
                    icon = Icons.Default.Savings,
                    tint = Color(0xFFA78BFA),
                    title = "Start a savings goal",
                    subtitle = "New phone, raya trip, emergency fund...",
                    onClick = onOpenGoals
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(Space.md)
            ) {
                goals.forEach { goal -> GoalMiniCard(goal) { onOpenGoal(goal) } }
            }
        }

        // ---------------- Recent activity ----------------
        SectionHeader("Recent activity", actionLabel = "See all", onAction = onOpenActivity)
        AppCard(contentPadding = PaddingValues(horizontal = Space.lg, vertical = Space.sm)) {
            if (recentTransactions.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.ReceiptLong,
                    title = "No expenses yet",
                    message = "Tap + to log your first expense or scan a receipt.",
                    actionLabel = "Add expense",
                    onAction = onAddExpense
                )
            } else {
                recentTransactions.forEachIndexed { i, tx ->
                    CompactTransactionRow(tx)
                    if (i < recentTransactions.lastIndex) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
        }

        // Clearance for the bottom bar / centre button
        Spacer(Modifier.height(96.dp))
    }
}

@Composable
private fun UpdateBanner(version: String, onOpen: () -> Unit, onDismiss: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(Radius.card),
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(start = Space.lg, end = Space.xs, top = Space.sm, bottom = Space.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Space.md)
        ) {
            Icon(Icons.Default.SystemUpdate, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
            Column(Modifier.weight(1f)) {
                Text("Update available", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                Text("Version $version is ready", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
            Button(onClick = onOpen, shape = CircleShape) { Text("Update") }
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "Dismiss update", tint = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }
    }
}

@Composable
fun StatusPill(text: String, color: Color) {
    Surface(shape = CircleShape, color = color.copy(alpha = 0.16f)) {
        Text(
            text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = color,
            modifier = Modifier.padding(horizontal = Space.md, vertical = Space.xs)
        )
    }
}

@Composable
private fun MiniStat(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.6f),
        modifier = modifier
    ) {
        Column(Modifier.padding(horizontal = Space.md, vertical = Space.md)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(2.dp))
            Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun QuickTile(icon: ImageVector, label: String, tint: Color, modifier: Modifier, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = modifier.heightIn(min = 76.dp),
        shape = RoundedCornerShape(20.dp),
        colors = glassCardColors(MaterialTheme.colorScheme.surfaceContainer),
        border = appCardBorder()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = Space.md),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            IconBadge(icon, tint, size = 36.dp, iconSize = IconSize.md)
            Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun GoalMiniCard(goal: SavingsGoal, onClick: () -> Unit) {
    val color = parseHex(goal.colorHex)
    val progress = if (goal.target > 0) (goal.saved / goal.target).toFloat() else 0f
    Card(
        onClick = onClick,
        modifier = Modifier.width(156.dp),
        shape = RoundedCornerShape(Radius.card),
        colors = glassCardColors(MaterialTheme.colorScheme.surfaceContainer),
        border = appCardBorder()
    ) {
        Column(Modifier.padding(Space.lg), verticalArrangement = Arrangement.spacedBy(Space.sm)) {
            ProgressRing(progress, color, size = 56.dp, stroke = 6.dp) {
                Icon(goalIcon(goal.iconKey), contentDescription = null, tint = color, modifier = Modifier.size(IconSize.md))
            }
            Text(goal.name, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                "${formatRMWhole(goal.saved)} / ${formatRMWhole(goal.target)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
            Text("${(progress * 100).toInt().coerceAtMost(999)}%", style = MaterialTheme.typography.labelLarge, color = color)
        }
    }
}

@Composable
fun CompactTransactionRow(tx: Transaction) {
    val color = CategoryRegistry.resolveColor(tx.category)
    val isCredit = tx.type == "credit"
    val cleanTitle = tx.title.replace(Regex("\\s+"), " ").trim().ifEmpty { tx.category }
    val cleanMethod = tx.method.replace(Regex("\\s+"), " ").trim()
    Row(
        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).padding(vertical = Space.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Space.md)
    ) {
        IconBadge(getCategoryIcon(tx.category), color)
        Column(Modifier.weight(1f)) {
            Text(cleanTitle, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                buildString {
                    append(tx.category)
                    if (cleanMethod.isNotEmpty()) append(" • ").append(cleanMethod)
                    append(" • ").append(tx.date.takeLast(5))
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text(
            (if (isCredit) "+" else "-") + formatRMExact(tx.amount),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            softWrap = false,
            maxLines = 1,
            color = if (isCredit) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}
