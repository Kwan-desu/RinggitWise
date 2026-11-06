package com.ringgitwise.app.data

import kotlinx.serialization.Serializable
import java.util.Calendar
import java.util.Locale

/**
 * A recurring bill or subscription (Netflix, phone plan, rent, insurance...).
 * frequency: "monthly" or "yearly". For yearly bills, [dueMonth] (1-12) is used.
 * [lastPaidPeriod] stores the period key that was last paid ("yyyy-MM" or "yyyy").
 */
@Serializable
data class RecurringBill(
    val id: String = "bill_${System.currentTimeMillis()}_${(1000..9999).random()}",
    val name: String,
    val amount: Double,
    val category: String = "Utilities & Bills",
    val method: String = "Online Banking",
    val frequency: String = "monthly",
    val dueDay: Int = 1,
    val dueMonth: Int = 1,
    val remindDaysBefore: Int = 2,
    val lastPaidPeriod: String = "",
    val isActive: Boolean = true
)

/** A savings goal shown with a progress ring. */
@Serializable
data class SavingsGoal(
    val id: String = "goal_${System.currentTimeMillis()}_${(1000..9999).random()}",
    val name: String,
    val target: Double,
    val saved: Double = 0.0,
    val colorHex: String = "#10B981",
    val iconKey: String = "savings",
    val deadline: String = "" // yyyy-MM-dd, optional
)

/** Full portable backup of the user's data. */
@Serializable
data class BackupBundle(
    val version: Int = 1,
    val exportedAt: Long = System.currentTimeMillis(),
    val monthlyBudget: Double = 3500.0,
    val transactions: List<Transaction> = emptyList(),
    val bills: List<RecurringBill> = emptyList(),
    val goals: List<SavingsGoal> = emptyList()
)

val GOAL_COLORS = listOf("#10B981", "#38BDF8", "#A78BFA", "#F59E0B", "#F43F5E", "#EC4899", "#14B8A6", "#84CC16")
val GOAL_ICON_KEYS = listOf("savings", "phone", "travel", "car", "home", "school", "gift", "health", "laptop", "emergency")

// ---------------------------------------------------------------
// Bill date helpers (Calendar based so they work on minSdk 24)
// ---------------------------------------------------------------

private fun Calendar.startOfDay(): Calendar = (clone() as Calendar).apply {
    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
}

fun RecurringBill.periodKey(cal: Calendar): String {
    val y = cal.get(Calendar.YEAR)
    val m = cal.get(Calendar.MONTH) + 1
    return if (frequency == "yearly") "$y" else String.format(Locale.US, "%04d-%02d", y, m)
}

fun RecurringBill.isPaidThisPeriod(today: Calendar = Calendar.getInstance()): Boolean =
    lastPaidPeriod == periodKey(today)

/** Due date inside the period that contains [ref]. */
private fun RecurringBill.dueInPeriodOf(ref: Calendar): Calendar {
    val c = ref.startOfDay()
    if (frequency == "yearly") c.set(Calendar.MONTH, (dueMonth - 1).coerceIn(0, 11))
    c.set(Calendar.DAY_OF_MONTH, 1)
    val maxDay = c.getActualMaximum(Calendar.DAY_OF_MONTH)
    c.set(Calendar.DAY_OF_MONTH, dueDay.coerceIn(1, maxDay))
    return c
}

/**
 * The next date the user has to pay. If this period is already paid, returns the due date of
 * the next period. If unpaid and the date has passed, returns the (overdue) date of this period.
 */
fun RecurringBill.nextDueDate(today: Calendar = Calendar.getInstance()): Calendar {
    val thisPeriodDue = dueInPeriodOf(today)
    if (!isPaidThisPeriod(today)) return thisPeriodDue
    val next = today.startOfDay()
    next.set(Calendar.DAY_OF_MONTH, 1)
    if (frequency == "yearly") next.add(Calendar.YEAR, 1) else next.add(Calendar.MONTH, 1)
    return dueInPeriodOf(next)
}

/** Days until the next due date. Negative = overdue. */
fun RecurringBill.daysUntilDue(today: Calendar = Calendar.getInstance()): Int {
    val t = today.startOfDay().timeInMillis
    val d = nextDueDate(today).timeInMillis
    return ((d - t) / 86_400_000L).toInt()
}

/** Monthly-equivalent cost, used for "fixed costs per month" totals. */
fun RecurringBill.monthlyEquivalent(): Double = if (frequency == "yearly") amount / 12.0 else amount

fun formatDueLabel(days: Int): String = when {
    days < 0 -> "Overdue ${-days}d"
    days == 0 -> "Due today"
    days == 1 -> "Due tomorrow"
    else -> "In $days days"
}
