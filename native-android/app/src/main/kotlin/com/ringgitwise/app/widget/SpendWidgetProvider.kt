package com.ringgitwise.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.ringgitwise.app.MainActivity
import com.ringgitwise.app.R
import com.ringgitwise.app.data.WidgetBridge
import com.ringgitwise.app.data.formatDueLabel
import java.util.Locale

/** Home-screen widget: today's spend, budget left this month, and the next bill due. */
class SpendWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val snap = WidgetBridge.snapshot(context)
        val pct = if (snap.budget > 0) ((snap.monthSpent / snap.budget) * 100).toInt().coerceIn(0, 100) else 0

        appWidgetIds.forEach { id ->
            val views = RemoteViews(context.packageName, R.layout.widget_spend)
            views.setTextViewText(R.id.widget_today_value, rm(snap.todaySpent))
            views.setTextViewText(R.id.widget_left_value, rm(snap.remaining))
            views.setTextViewText(R.id.widget_left_caption, "left of ${rm(snap.budget)} • $pct% used")
            views.setProgressBar(R.id.widget_progress, 100, pct, false)
            views.setTextViewText(R.id.widget_safe_value, "Safe today: ${rm(snap.dailySafe)}")
            val billText = if (snap.nextBillName != null && snap.nextBillDays != null) {
                "${snap.nextBillName} • ${formatDueLabel(snap.nextBillDays)}"
            } else "No upcoming bills"
            views.setTextViewText(R.id.widget_bill_value, billText)

            val openApp = PendingIntent.getActivity(
                context, 0,
                Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, openApp)

            val quickAdd = PendingIntent.getActivity(
                context, 1,
                Intent(context, MainActivity::class.java)
                    .putExtra(MainActivity.EXTRA_OPEN_ADD, true)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_add, quickAdd)

            appWidgetManager.updateAppWidget(id, views)
        }
    }

    private fun rm(v: Double) = String.format(Locale.US, "RM %,.2f", v)
}
