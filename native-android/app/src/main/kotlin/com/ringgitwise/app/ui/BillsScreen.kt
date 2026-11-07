package com.ringgitwise.app.ui

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
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ringgitwise.app.data.*
import com.ringgitwise.app.ui.theme.Danger
import com.ringgitwise.app.ui.theme.Warning
import java.util.Calendar

private data class BillTemplate(val name: String, val amount: Double, val category: String, val dueDay: Int = 1)

private val BILL_TEMPLATES = listOf(
    BillTemplate("Netflix", 55.0, "Shopping & Online"),
    BillTemplate("Spotify", 15.90, "Shopping & Online"),
    BillTemplate("Phone plan", 58.0, "Utilities & Bills"),
    BillTemplate("Unifi / Internet", 129.0, "Utilities & Bills"),
    BillTemplate("TNB Electricity", 120.0, "Utilities & Bills"),
    BillTemplate("Water", 20.0, "Utilities & Bills"),
    BillTemplate("Rent", 900.0, "Utilities & Bills"),
    BillTemplate("Car loan", 650.0, "Fuel & Transport"),
    BillTemplate("Insurance", 200.0, "Healthcare & Pharmacy"),
    BillTemplate("PTPTN", 150.0, "Utilities & Bills")
)

private val MONTHS = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

@Composable
fun BillsScreen(
    bills: List<RecurringBill>,
    customCategories: List<CustomCategory> = emptyList(),
    remindersEnabled: Boolean,
    onToggleReminders: (Boolean) -> Unit,
    onUpsert: (RecurringBill) -> Unit,
    onDelete: (String) -> Unit,
    onMarkPaid: (RecurringBill) -> Unit,
    onUnmarkPaid: (RecurringBill) -> Unit,
    onAddCustomCategory: (CustomCategory) -> Unit = {}
) {
    var editing by remember { mutableStateOf<RecurringBill?>(null) }
    var creating by remember { mutableStateOf(false) }

    val cal = remember(bills) { Calendar.getInstance() }
    val active = bills.filter { it.isActive }
    val unpaid = active.filter { !it.isPaidThisPeriod(cal) }.sortedBy { it.daysUntilDue(cal) }
    val dueSoon = unpaid.filter { it.daysUntilDue(cal) <= 7 }
    val later = unpaid.filter { it.daysUntilDue(cal) > 7 }
    val paid = active.filter { it.isPaidThisPeriod(cal) }.sortedBy { it.daysUntilDue(cal) }
    val fixedMonthly = active.sumOf { it.monthlyEquivalent() }
    val dueSoonTotal = dueSoon.sumOf { it.amount }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = Space.lg, end = Space.lg, top = Space.sm, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(Space.md)
        ) {
            item {
                AppCard(container = MaterialTheme.colorScheme.surfaceContainerHigh, contentPadding = PaddingValues(Space.xl)) {
                    Text("Fixed costs per month", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(formatRMExact(fixedMonthly), style = MaterialTheme.typography.displaySmall)
                    Spacer(Modifier.height(Space.md))
                    Row(horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
                        StatusPill("${dueSoon.size} due in 7 days", if (dueSoon.isEmpty()) MaterialTheme.colorScheme.primary else Warning)
                        if (dueSoon.isNotEmpty()) StatusPill(formatRMExact(dueSoonTotal), Warning)
                    }
                }
            }
            item {
                AppCard(contentPadding = PaddingValues(horizontal = Space.lg, vertical = Space.xs)) {
                    ListRow(
                        icon = Icons.Outlined.NotificationsActive,
                        tint = MaterialTheme.colorScheme.primary,
                        title = "Due-date reminders",
                        subtitle = "Notify me before a bill is due",
                        onClick = { onToggleReminders(!remindersEnabled) },
                        trailing = { Switch(checked = remindersEnabled, onCheckedChange = onToggleReminders) }
                    )
                }
            }

            if (bills.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Default.EventRepeat,
                        title = "No bills yet",
                        message = "Add subscriptions and monthly bills to see what's due and get reminded on time.",
                        actionLabel = "Add a bill",
                        onAction = { creating = true }
                    )
                }
            }

            if (dueSoon.isNotEmpty()) {
                item { SectionHeader("Due soon") }
                items(dueSoon, key = { it.id }) { bill -> BillRow(bill, cal, false, { editing = bill }, { onMarkPaid(bill) }) }
            }
            if (later.isNotEmpty()) {
                item { SectionHeader("Later") }
                items(later, key = { it.id }) { bill -> BillRow(bill, cal, false, { editing = bill }, { onMarkPaid(bill) }) }
            }
            if (paid.isNotEmpty()) {
                item { SectionHeader("Paid this period") }
                items(paid, key = { it.id }) { bill -> BillRow(bill, cal, true, { editing = bill }, { onUnmarkPaid(bill) }) }
            }
        }

        ExtendedFloatingActionButton(
            onClick = { creating = true },
            icon = { Icon(Icons.Default.Add, contentDescription = null) },
            text = { Text("Add bill") },
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.align(Alignment.BottomEnd).padding(Space.lg).navigationBarsPadding()
        )
    }

    if (creating || editing != null) {
        BillEditorSheet(
            initial = editing,
            customCategories = customCategories,
            onAddCustomCategory = onAddCustomCategory,
            onDismiss = { creating = false; editing = null },
            onSave = { onUpsert(it); creating = false; editing = null },
            onDelete = { id -> onDelete(id); creating = false; editing = null }
        )
    }
}

@Composable
private fun BillRow(bill: RecurringBill, cal: Calendar, isPaid: Boolean, onEdit: () -> Unit, onAction: () -> Unit) {
    val days = bill.daysUntilDue(cal)
    val tint = when {
        isPaid -> MaterialTheme.colorScheme.primary
        days < 0 -> Danger
        days <= 3 -> Warning
        else -> MaterialTheme.colorScheme.secondary
    }
    AppCard(onClick = onEdit, contentPadding = PaddingValues(horizontal = Space.lg, vertical = Space.md)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Space.md)) {
            IconBadge(if (isPaid) Icons.Default.CheckCircle else getCategoryIcon(bill.category), tint)
            Column(Modifier.weight(1f)) {
                Text(bill.name, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                val freq = if (bill.frequency == "yearly") "Yearly • ${MONTHS[(bill.dueMonth - 1).coerceIn(0, 11)]} ${bill.dueDay}" else "Monthly • day ${bill.dueDay}"
                Text(
                    if (isPaid) "$freq • next ${formatDueLabel(days).lowercase()}" else "$freq • ${formatDueLabel(days)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (!isPaid && days < 0) Danger else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(formatRMExact(bill.amount), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                TextButton(onClick = onAction, contentPadding = PaddingValues(horizontal = Space.sm), modifier = Modifier.heightIn(min = 40.dp)) {
                    Text(if (isPaid) "Undo" else "Mark paid")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun BillEditorSheet(
    initial: RecurringBill?,
    customCategories: List<CustomCategory> = emptyList(),
    onAddCustomCategory: (CustomCategory) -> Unit = {},
    onDismiss: () -> Unit,
    onSave: (RecurringBill) -> Unit,
    onDelete: (String) -> Unit
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var amount by remember { mutableStateOf(initial?.amount?.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() } ?: "") }
    var frequency by remember { mutableStateOf(initial?.frequency ?: "monthly") }
    var dueDay by remember { mutableStateOf((initial?.dueDay ?: Calendar.getInstance().get(Calendar.DAY_OF_MONTH)).toString()) }
    var dueMonth by remember { mutableIntStateOf(initial?.dueMonth ?: (Calendar.getInstance().get(Calendar.MONTH) + 1)) }
    var category by remember { mutableStateOf(initial?.category ?: "Utilities & Bills") }
    var remind by remember { mutableIntStateOf(initial?.remindDaysBefore ?: 2) }
    var showNewCatSheet by remember { mutableStateOf(false) }

    val amountVal = amount.toDoubleOrNull()
    val dayVal = dueDay.toIntOrNull()
    val nameError = name.isBlank()
    val amountError = amountVal == null || amountVal <= 0
    val dayError = dayVal == null || dayVal !in 1..31
    var triedSave by remember { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = onDismiss, shape = RoundedCornerShape(topStart = Radius.hero, topEnd = Radius.hero)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Space.xl)
                .padding(bottom = Space.xl),
            verticalArrangement = Arrangement.spacedBy(Space.lg)
        ) {
            Text(if (initial == null) "New bill" else "Edit bill", style = MaterialTheme.typography.headlineSmall)

            if (initial == null) {
                Text("Quick templates", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
                    BILL_TEMPLATES.forEach { t ->
                        SuggestionChip(
                            onClick = {
                                name = t.name
                                amount = if (t.amount % 1.0 == 0.0) t.amount.toInt().toString() else t.amount.toString()
                                category = t.category
                            },
                            label = { Text(t.name) },
                            shape = CircleShape
                        )
                    }
                }
            }

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                placeholder = { Text("e.g. Netflix") },
                isError = triedSave && nameError,
                supportingText = if (triedSave && nameError) {{ Text("Enter a name") }} else null,
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("Amount (RM)") },
                isError = triedSave && amountError,
                supportingText = if (triedSave && amountError) {{ Text("Enter an amount above 0") }} else null,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Text("Repeats", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                listOf("monthly" to "Monthly", "yearly" to "Yearly").forEachIndexed { i, (key, label) ->
                    SegmentedButton(
                        selected = frequency == key,
                        onClick = { frequency = key },
                        shape = SegmentedButtonDefaults.itemShape(i, 2)
                    ) { Text(label) }
                }
            }

            if (frequency == "yearly") {
                Text("Due month", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(Space.sm), verticalArrangement = Arrangement.spacedBy(Space.sm)) {
                    MONTHS.forEachIndexed { i, m ->
                        FilterChip(selected = dueMonth == i + 1, onClick = { dueMonth = i + 1 }, label = { Text(m) }, shape = CircleShape)
                    }
                }
            }

            OutlinedTextField(
                value = dueDay,
                onValueChange = { dueDay = it.filter { c -> c.isDigit() }.take(2) },
                label = { Text("Due day of month (1-31)") },
                isError = triedSave && dayError,
                supportingText = { Text(if (triedSave && dayError) "Pick a day from 1 to 31" else "Short months use their last day") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Text("Remind me", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
                listOf(0 to "On the day", 1 to "1 day before", 2 to "2 days before", 3 to "3 days before", 7 to "1 week before").forEach { (d, label) ->
                    FilterChip(selected = remind == d, onClick = { remind = d }, label = { Text(label) }, shape = CircleShape)
                }
            }

            Text("Category", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
                AssistChip(
                    onClick = { showNewCatSheet = true },
                    leadingIcon = { Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(IconSize.sm)) },
                    label = { Text("Custom") },
                    shape = CircleShape
                )
                CategoryRegistry.allCategories(customCategories)
                    .filter { !it.name.contains("Top Up") && !it.name.contains("Tolong") }
                    .forEach { c ->
                        FilterChip(
                            selected = category == c.name,
                            onClick = { category = c.name },
                            leadingIcon = { Icon(getCategoryIcon(c.name), contentDescription = null, tint = c.color, modifier = Modifier.size(IconSize.sm)) },
                            label = { Text(c.name) },
                            shape = CircleShape
                        )
                    }
            }

            Button(
                onClick = {
                    triedSave = true
                    if (!nameError && !amountError && !dayError) {
                        val base = initial ?: RecurringBill(name = name.trim(), amount = amountVal!!)
                        onSave(
                            base.copy(
                                name = name.trim(),
                                amount = amountVal!!,
                                frequency = frequency,
                                dueDay = dayVal!!,
                                dueMonth = dueMonth,
                                category = category,
                                remindDaysBefore = remind
                            )
                        )
                    }
                },
                shape = CircleShape,
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
            ) { Text(if (initial == null) "Add bill" else "Save changes") }

            if (initial != null) {
                OutlinedButton(
                    onClick = { onDelete(initial.id) },
                    shape = CircleShape,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
                ) {
                    Icon(Icons.Outlined.DeleteOutline, contentDescription = null)
                    Spacer(Modifier.width(Space.sm))
                    Text("Delete bill")
                }
            }
        }
    }

    if (showNewCatSheet) {
        CustomCategorySheet(
            onDismiss = { showNewCatSheet = false },
            onSave = { newCat ->
                onAddCustomCategory(newCat)
                category = newCat.name
                showNewCatSheet = false
            }
        )
    }
}
