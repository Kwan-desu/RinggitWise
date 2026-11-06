package com.ringgitwise.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ringgitwise.app.data.GOAL_COLORS
import com.ringgitwise.app.data.GOAL_ICON_KEYS
import com.ringgitwise.app.data.SavingsGoal
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun GoalsScreen(
    goals: List<SavingsGoal>,
    initialOpenGoalId: String?,
    onInitialConsumed: () -> Unit,
    onUpsert: (SavingsGoal) -> Unit,
    onDelete: (String) -> Unit,
    onContribute: (String, Double) -> Unit
) {
    var detailId by remember { mutableStateOf<String?>(null) }
    var editing by remember { mutableStateOf<SavingsGoal?>(null) }
    var creating by remember { mutableStateOf(false) }

    LaunchedEffect(initialOpenGoalId) {
        if (initialOpenGoalId != null) {
            detailId = initialOpenGoalId
            onInitialConsumed()
        }
    }

    val totalSaved = goals.sumOf { it.saved }
    val totalTarget = goals.sumOf { it.target }

    Box(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Space.lg)
                .padding(top = Space.sm, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(Space.md)
        ) {
            AppCard(container = MaterialTheme.colorScheme.surfaceContainerHigh, contentPadding = PaddingValues(Space.xl)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Space.lg)) {
                    val p = if (totalTarget > 0) (totalSaved / totalTarget).toFloat() else 0f
                    ProgressRing(p, MaterialTheme.colorScheme.primary, size = 88.dp, stroke = 10.dp) {
                        Text("${(p * 100).toInt()}%", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Column {
                        Text("Total saved", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(formatRMExact(totalSaved), style = MaterialTheme.typography.headlineMedium)
                        Text(
                            "of ${formatRMWhole(totalTarget)} across ${goals.size} ${if (goals.size == 1) "goal" else "goals"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (goals.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.Savings,
                    title = "No goals yet",
                    message = "Set a target like a new phone or raya trip and watch the ring fill up as you save.",
                    actionLabel = "Create a goal",
                    onAction = { creating = true }
                )
            } else {
                goals.chunked(2).forEach { pair ->
                    Row(horizontalArrangement = Arrangement.spacedBy(Space.md)) {
                        pair.forEach { goal ->
                            Box(Modifier.weight(1f)) { GoalGridCard(goal) { detailId = goal.id } }
                        }
                        if (pair.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }
        }

        ExtendedFloatingActionButton(
            onClick = { creating = true },
            icon = { Icon(Icons.Default.Add, contentDescription = null) },
            text = { Text("New goal") },
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.align(Alignment.BottomEnd).padding(Space.lg).navigationBarsPadding()
        )
    }

    val detailGoal = goals.find { it.id == detailId }
    if (detailGoal != null) {
        GoalDetailSheet(
            goal = detailGoal,
            onDismiss = { detailId = null },
            onContribute = { amt -> onContribute(detailGoal.id, amt) },
            onEdit = { editing = detailGoal; detailId = null },
            onDelete = { onDelete(detailGoal.id); detailId = null }
        )
    }

    if (creating || editing != null) {
        GoalEditorSheet(
            initial = editing,
            onDismiss = { creating = false; editing = null },
            onSave = { onUpsert(it); creating = false; editing = null }
        )
    }
}

@Composable
private fun GoalGridCard(goal: SavingsGoal, onClick: () -> Unit) {
    val color = parseHex(goal.colorHex)
    val p = if (goal.target > 0) (goal.saved / goal.target).toFloat() else 0f
    AppCard(onClick = onClick, contentPadding = PaddingValues(Space.lg)) {
        ProgressRing(p, color, size = 72.dp, stroke = 8.dp) {
            Icon(goalIcon(goal.iconKey), contentDescription = null, tint = color, modifier = Modifier.size(IconSize.lg))
        }
        Spacer(Modifier.height(Space.md))
        Text(goal.name, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(formatRMExact(goal.saved), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(
            if (p >= 1f) "Goal reached" else "${formatRMWhole((goal.target - goal.saved).coerceAtLeast(0.0))} to go",
            style = MaterialTheme.typography.bodySmall,
            color = if (p >= 1f) color else MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (goal.deadline.isNotBlank()) {
            Spacer(Modifier.height(Space.xs))
            Text("By ${prettyDate(goal.deadline)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private fun prettyDate(iso: String): String = try {
    val d = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse(iso)
    SimpleDateFormat("d MMM yyyy", Locale.ENGLISH).format(d!!)
} catch (_: Exception) { iso }

/** Months between today and the deadline (at least 1), or null when no deadline. */
private fun monthsUntil(iso: String): Int? = try {
    val d = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse(iso)!!
    val now = Calendar.getInstance()
    val end = Calendar.getInstance().apply { time = d }
    val months = (end.get(Calendar.YEAR) - now.get(Calendar.YEAR)) * 12 + (end.get(Calendar.MONTH) - now.get(Calendar.MONTH))
    months.coerceAtLeast(1)
} catch (_: Exception) { null }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GoalDetailSheet(
    goal: SavingsGoal,
    onDismiss: () -> Unit,
    onContribute: (Double) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val color = parseHex(goal.colorHex)
    val p = if (goal.target > 0) (goal.saved / goal.target).toFloat() else 0f
    val remaining = (goal.target - goal.saved).coerceAtLeast(0.0)
    var custom by remember { mutableStateOf("") }
    var confirmDelete by remember { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = onDismiss, shape = RoundedCornerShape(topStart = Radius.hero, topEnd = Radius.hero)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Space.xl)
                .padding(bottom = Space.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Space.lg)
        ) {
            ProgressRing(p, color, size = 140.dp, stroke = 14.dp) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(goalIcon(goal.iconKey), contentDescription = null, tint = color)
                    Text("${(p * 100).toInt()}%", style = MaterialTheme.typography.headlineSmall)
                }
            }
            Text(goal.name, style = MaterialTheme.typography.headlineSmall)
            Text(
                "${formatRMExact(goal.saved)} of ${formatRMExact(goal.target)}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            val months = if (goal.deadline.isNotBlank()) monthsUntil(goal.deadline) else null
            if (remaining > 0 && months != null) {
                StatusPill("Save ${formatRMExact(remaining / months)} / month to hit ${prettyDate(goal.deadline)}", color)
            } else if (remaining <= 0) {
                StatusPill("Goal reached. Nice one!", color)
            }

            Text("Add money", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.fillMaxWidth())
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
                listOf(10.0, 50.0, 100.0, 500.0).forEach { amt ->
                    FilledTonalButton(
                        onClick = { onContribute(amt) },
                        shape = CircleShape,
                        contentPadding = PaddingValues(horizontal = 0.dp),
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp)
                    ) { Text("+${amt.toInt()}") }
                }
            }
            OutlinedTextField(
                value = custom,
                onValueChange = { custom = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("Custom amount (RM)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
                val v = custom.toDoubleOrNull()
                OutlinedButton(
                    onClick = { if (v != null && v > 0) { onContribute(-v); custom = "" } },
                    enabled = v != null && v > 0 && goal.saved > 0,
                    shape = CircleShape,
                    modifier = Modifier.weight(1f).heightIn(min = 48.dp)
                ) { Text("Withdraw") }
                Button(
                    onClick = { if (v != null && v > 0) { onContribute(v); custom = "" } },
                    enabled = v != null && v > 0,
                    shape = CircleShape,
                    modifier = Modifier.weight(1f).heightIn(min = 48.dp)
                ) { Text("Deposit") }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
                OutlinedButton(onClick = onEdit, shape = CircleShape, modifier = Modifier.weight(1f).heightIn(min = 48.dp)) {
                    Icon(Icons.Outlined.Edit, contentDescription = null, modifier = Modifier.size(IconSize.md))
                    Spacer(Modifier.width(Space.sm)); Text("Edit")
                }
                OutlinedButton(
                    onClick = { confirmDelete = true },
                    shape = CircleShape,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.weight(1f).heightIn(min = 48.dp)
                ) {
                    Icon(Icons.Outlined.DeleteOutline, contentDescription = null, modifier = Modifier.size(IconSize.md))
                    Spacer(Modifier.width(Space.sm)); Text("Delete")
                }
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete \"${goal.name}\"?") },
            text = { Text("The goal and its saved amount will be removed. Your expenses are not affected.") },
            confirmButton = {
                TextButton(onClick = { confirmDelete = false; onDelete() }, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancel") } }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun GoalEditorSheet(
    initial: SavingsGoal?,
    onDismiss: () -> Unit,
    onSave: (SavingsGoal) -> Unit
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var target by remember { mutableStateOf(initial?.target?.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() } ?: "") }
    var saved by remember { mutableStateOf(initial?.saved?.let { if (it == 0.0) "" else if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() } ?: "") }
    var colorHex by remember { mutableStateOf(initial?.colorHex ?: GOAL_COLORS.first()) }
    var iconKey by remember { mutableStateOf(initial?.iconKey ?: "savings") }
    var deadline by remember { mutableStateOf(initial?.deadline ?: "") }
    var showDatePicker by remember { mutableStateOf(false) }
    var triedSave by remember { mutableStateOf(false) }

    val targetVal = target.toDoubleOrNull()
    val nameError = name.isBlank()
    val targetError = targetVal == null || targetVal <= 0

    ModalBottomSheet(onDismissRequest = onDismiss, shape = RoundedCornerShape(topStart = Radius.hero, topEnd = Radius.hero)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Space.xl)
                .padding(bottom = Space.xl),
            verticalArrangement = Arrangement.spacedBy(Space.lg)
        ) {
            Text(if (initial == null) "New savings goal" else "Edit goal", style = MaterialTheme.typography.headlineSmall)
            OutlinedTextField(
                value = name, onValueChange = { name = it },
                label = { Text("Goal name") }, placeholder = { Text("e.g. New phone") },
                isError = triedSave && nameError,
                supportingText = if (triedSave && nameError) {{ Text("Give your goal a name") }} else null,
                singleLine = true, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()
            )
            Row(horizontalArrangement = Arrangement.spacedBy(Space.md)) {
                OutlinedTextField(
                    value = target, onValueChange = { target = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Target (RM)") },
                    isError = triedSave && targetError,
                    supportingText = if (triedSave && targetError) {{ Text("Above 0") }} else null,
                    singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(16.dp), modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = saved, onValueChange = { saved = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Saved so far") },
                    singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(16.dp), modifier = Modifier.weight(1f)
                )
            }

            Text("Icon", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            val accent = parseHex(colorHex)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(Space.sm), verticalArrangement = Arrangement.spacedBy(Space.sm)) {
                GOAL_ICON_KEYS.forEach { key ->
                    val sel = key == iconKey
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (sel) accent.copy(alpha = 0.22f) else MaterialTheme.colorScheme.surfaceContainerHighest)
                            .border(if (sel) 2.dp else 0.dp, if (sel) accent else androidx.compose.ui.graphics.Color.Transparent, RoundedCornerShape(14.dp))
                            .clickable(role = Role.RadioButton) { iconKey = key }
                            .semantics { contentDescription = "Icon $key"; selected = sel },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(goalIcon(key), contentDescription = null, tint = if (sel) accent else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Text("Colour", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
                GOAL_COLORS.forEachIndexed { i, hex ->
                    val sel = hex == colorHex
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .clickable(role = Role.RadioButton) { colorHex = hex }
                            .semantics { contentDescription = "Colour ${i + 1}"; selected = sel },
                        contentAlignment = Alignment.Center
                    ) {
                        Box(Modifier.size(36.dp).clip(CircleShape).background(parseHex(hex)), contentAlignment = Alignment.Center) {
                            if (sel) Icon(Icons.Default.Check, contentDescription = null, tint = androidx.compose.ui.graphics.Color.White, modifier = Modifier.size(IconSize.md))
                        }
                    }
                }
            }

            Text("Target date (optional)", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
                OutlinedButton(onClick = { showDatePicker = true }, shape = CircleShape, modifier = Modifier.heightIn(min = 48.dp)) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(IconSize.md))
                    Spacer(Modifier.width(Space.sm))
                    Text(if (deadline.isBlank()) "Pick a date" else prettyDate(deadline))
                }
                if (deadline.isNotBlank()) {
                    TextButton(onClick = { deadline = "" }) { Text("Clear") }
                }
            }

            Button(
                onClick = {
                    triedSave = true
                    if (!nameError && !targetError) {
                        val base = initial ?: SavingsGoal(name = name.trim(), target = targetVal!!)
                        onSave(
                            base.copy(
                                name = name.trim(),
                                target = targetVal!!,
                                saved = saved.toDoubleOrNull()?.coerceAtLeast(0.0) ?: 0.0,
                                colorHex = colorHex,
                                iconKey = iconKey,
                                deadline = deadline
                            )
                        )
                    }
                },
                shape = CircleShape,
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
            ) { Text(if (initial == null) "Create goal" else "Save changes") }
        }
    }

    if (showDatePicker) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = try {
                if (deadline.isNotBlank()) SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).apply { timeZone = TimeZone.getTimeZone("UTC") }.parse(deadline)?.time else null
            } catch (_: Exception) { null }
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let {
                        deadline = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).apply { timeZone = TimeZone.getTimeZone("UTC") }.format(Date(it))
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = state) }
    }
}
