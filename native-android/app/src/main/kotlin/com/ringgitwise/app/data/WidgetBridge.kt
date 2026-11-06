package com.ringgitwise.app.data

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/** Snapshot of the numbers shown on the home-screen widget and in reminders. */
data class SpendSnapshot(
    val todaySpent: Double,
    val monthSpent: Double,
    val budget: Double,
    val remaining: Double,
    val dailySafe: Double,
    val nextBillName: String?,
    val nextBillDays: Int?
)

/**
 * Lightweight bridge used by the widget and background workers. Reads the same
 * SharedPreferences the repository writes to, so it works without the app running.
 */
object WidgetBridge {
    const val PREFS = "ringgitwise_native_prefs"
    private val json = Json { ignoreUnknownKeys = true }

    fun loadTransactions(context: Context): List<Transaction> = try {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString("transactions_json", null)
            ?.let { json.decodeFromString<List<Transaction>>(it) } ?: emptyList()
    } catch (_: Exception) { emptyList() }

    fun loadBills(context: Context): List<RecurringBill> = try {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString("bills_json", null)
            ?.let { json.decodeFromString<List<RecurringBill>>(it) } ?: emptyList()
    } catch (_: Exception) { emptyList() }

    fun snapshot(context: Context): SpendSnapshot {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val budget = prefs.getFloat("monthly_budget", 3500f).toDouble()
        val now = Date()
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(now)
        val month = SimpleDateFormat("yyyy-MM", Locale.ENGLISH).format(now)
        val expenses = loadTransactions(context).filter { it.amount > 0 && it.type != "credit" }
        val todaySpent = expenses.filter { it.date == today }.sumOf { it.amount }
        val monthSpent = expenses.filter { it.date.startsWith(month) }.sumOf { it.amount }
        val remaining = (budget - monthSpent).coerceAtLeast(0.0)
        val cal = Calendar.getInstance()
        val daysLeft = (cal.getActualMaximum(Calendar.DAY_OF_MONTH) - cal.get(Calendar.DAY_OF_MONTH) + 1).coerceAtLeast(1)
        val nextBill = loadBills(context).filter { it.isActive }.minByOrNull { it.daysUntilDue(cal) }
        return SpendSnapshot(
            todaySpent = todaySpent,
            monthSpent = monthSpent,
            budget = budget,
            remaining = remaining,
            dailySafe = remaining / daysLeft,
            nextBillName = nextBill?.name,
            nextBillDays = nextBill?.daysUntilDue(cal)
        )
    }

    /** Asks every placed RinggitWise widget to redraw. Safe to call often. */
    fun requestRefresh(context: Context) {
        try {
            val mgr = AppWidgetManager.getInstance(context) ?: return
            val component = ComponentName(context, "com.ringgitwise.app.widget.SpendWidgetProvider")
            val ids = mgr.getAppWidgetIds(component)
            if (ids.isEmpty()) return
            val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE).apply {
                setComponent(component)
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            }
            context.sendBroadcast(intent)
        } catch (_: Exception) {}
    }
}
