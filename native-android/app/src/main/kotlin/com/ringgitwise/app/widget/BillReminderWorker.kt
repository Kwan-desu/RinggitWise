package com.ringgitwise.app.widget

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.ringgitwise.app.MainActivity
import com.ringgitwise.app.R
import com.ringgitwise.app.data.WidgetBridge
import com.ringgitwise.app.data.daysUntilDue
import com.ringgitwise.app.data.formatDueLabel
import com.ringgitwise.app.data.isPaidThisPeriod
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Runs about twice a day. Posts one notification per day listing unpaid bills that are
 * due within their reminder window (or overdue), and refreshes the home-screen widget.
 */
class BillReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val ctx = applicationContext
        WidgetBridge.requestRefresh(ctx)

        val prefs = ctx.getSharedPreferences(WidgetBridge.PREFS, Context.MODE_PRIVATE)
        if (!prefs.getBoolean("bill_reminders_enabled", true)) return Result.success()

        val todayKey = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(Date())
        if (prefs.getString("last_bill_reminder_day", "") == todayKey) return Result.success()

        val cal = Calendar.getInstance()
        val due = WidgetBridge.loadBills(ctx)
            .filter { it.isActive }
            .map { it to it.daysUntilDue(cal) }
            .filter { (bill, days) -> days <= bill.remindDaysBefore && !bill.isPaidThisPeriod(cal) }
            .sortedBy { it.second }

        if (due.isEmpty()) return Result.success()
        if (!canNotify(ctx)) return Result.success()

        ensureChannel(ctx)
        val total = due.sumOf { it.first.amount }
        val title = if (due.size == 1) "${due[0].first.name} • ${formatDueLabel(due[0].second)}"
        else "${due.size} bills due soon • ${String.format(Locale.US, "RM %,.2f", total)}"
        val body = due.joinToString("\n") { (b, d) ->
            "${b.name}: ${String.format(Locale.US, "RM %,.2f", b.amount)} (${formatDueLabel(d)})"
        }

        val tap = PendingIntent.getActivity(
            ctx, 2,
            Intent(ctx, MainActivity::class.java)
                .putExtra(MainActivity.EXTRA_OPEN_BILLS, true)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(ctx, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_bill)
            .setContentTitle(title)
            .setContentText(body.lineSequence().first())
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(tap)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        try {
            NotificationManagerCompat.from(ctx).notify(NOTIFICATION_ID, notification)
            prefs.edit().putString("last_bill_reminder_day", todayKey).apply()
        } catch (_: SecurityException) {}

        return Result.success()
    }

    companion object {
        const val CHANNEL_ID = "bill_reminders"
        private const val NOTIFICATION_ID = 4101
        private const val WORK_NAME = "ringgitwise_bill_reminders"

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<BillReminderWorker>(12, TimeUnit.HOURS).build()
            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
        }

        fun canNotify(context: Context): Boolean =
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

        private fun ensureChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val mgr = context.getSystemService(NotificationManager::class.java)
                if (mgr.getNotificationChannel(CHANNEL_ID) == null) {
                    mgr.createNotificationChannel(
                        NotificationChannel(CHANNEL_ID, "Bill reminders", NotificationManager.IMPORTANCE_DEFAULT).apply {
                            description = "Reminds you before recurring bills and subscriptions are due"
                        }
                    )
                }
            }
        }
    }
}
