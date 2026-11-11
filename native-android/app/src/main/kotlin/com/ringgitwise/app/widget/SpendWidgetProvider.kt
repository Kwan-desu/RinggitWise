package com.ringgitwise.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.SizeF
import android.view.View
import android.widget.RemoteViews
import com.ringgitwise.app.MainActivity
import com.ringgitwise.app.R
import com.ringgitwise.app.data.SpendSnapshot
import com.ringgitwise.app.data.WidgetBridge
import com.ringgitwise.app.data.formatDueLabel
import java.util.Locale
import kotlin.math.abs

/**
 * Home-screen widget: today's spend, budget left this month, and the next bill due.
 * Adapts across 4x2, 2x2, 2x1/4x1, and 1x2 grid cells.
 */
class SpendWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val snap = WidgetBridge.snapshot(context)
        appWidgetIds.forEach { id ->
            val options = appWidgetManager.getAppWidgetOptions(id)
            updateWidget(context, appWidgetManager, id, options, snap)
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        val snap = WidgetBridge.snapshot(context)
        updateWidget(context, appWidgetManager, appWidgetId, newOptions, snap)
    }

    private fun updateWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        options: Bundle,
        snap: SpendSnapshot
    ) {
        val pct = if (snap.budget > 0) ((snap.monthSpent / snap.budget) * 100).toInt().coerceIn(0, 100) else 0
        val openApp = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val quickAdd = PendingIntent.getActivity(
            context, 1,
            Intent(context, MainActivity::class.java)
                .putExtra(MainActivity.EXTRA_OPEN_ADD, true)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val views = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            RemoteViews(
                mapOf(
                    SizeF(40f, 85f) to build1x2Views(context, snap, pct, openApp, quickAdd, compactHeight = true),
                    SizeF(40f, 120f) to build1x2Views(context, snap, pct, openApp, quickAdd, compactHeight = false),
                    SizeF(125f, 50f) to buildRegularViews(context, snap, pct, openApp, quickAdd, widthDp = 150, heightDp = 75),
                    SizeF(125f, 115f) to buildRegularViews(context, snap, pct, openApp, quickAdd, widthDp = 160, heightDp = 130),
                    SizeF(215f, 50f) to buildRegularViews(context, snap, pct, openApp, quickAdd, widthDp = 250, heightDp = 75),
                    SizeF(215f, 115f) to buildRegularViews(context, snap, pct, openApp, quickAdd, widthDp = 250, heightDp = 135)
                )
            )
        } else {
            val minWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 250)
            val minHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 130)
            if (minWidth < 125) {
                build1x2Views(context, snap, pct, openApp, quickAdd, compactHeight = minHeight < 115)
            } else {
                buildRegularViews(context, snap, pct, openApp, quickAdd, minWidth, minHeight)
            }
        }

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun build1x2Views(
        context: Context,
        snap: SpendSnapshot,
        pct: Int,
        openApp: PendingIntent,
        quickAdd: PendingIntent,
        compactHeight: Boolean
    ): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_spend_1x2)
        views.setTextViewText(R.id.widget_1x2_today, compactRm(snap.todaySpent))
        views.setProgressBar(R.id.widget_1x2_progress, 100, pct, false)
        views.setTextViewText(R.id.widget_1x2_pct, "$pct%")

        val leftLabel = if (snap.remaining >= 0) {
            "${compactRm(snap.remaining)} left"
        } else {
            "${compactRm(abs(snap.remaining))} over"
        }
        views.setTextViewText(R.id.widget_1x2_left, leftLabel)
        views.setViewVisibility(R.id.widget_1x2_left, if (compactHeight) View.GONE else View.VISIBLE)

        views.setOnClickPendingIntent(R.id.widget_root, openApp)
        views.setOnClickPendingIntent(R.id.widget_add, quickAdd)
        return views
    }

    private fun buildRegularViews(
        context: Context,
        snap: SpendSnapshot,
        pct: Int,
        openApp: PendingIntent,
        quickAdd: PendingIntent,
        widthDp: Int,
        heightDp: Int
    ): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_spend)
        val narrow = widthDp < 200
        views.setTextViewText(
            R.id.widget_today_value,
            if (narrow) compactRm(snap.todaySpent) else rm(snap.todaySpent)
        )
        views.setTextViewText(
            R.id.widget_left_value,
            if (narrow) compactRm(snap.remaining) else rm(snap.remaining)
        )
        views.setTextViewText(
            R.id.widget_left_caption,
            if (narrow) "$pct% used" else "left of ${rm(snap.budget)} • $pct% used"
        )
        views.setProgressBar(R.id.widget_progress, 100, pct, false)
        views.setTextViewText(
            R.id.widget_safe_value,
            if (narrow) "Safe: ${compactRm(snap.dailySafe)}" else "Safe today: ${rm(snap.dailySafe)}"
        )

        val billText = if (snap.nextBillName != null && snap.nextBillDays != null) {
            "${snap.nextBillName} • ${formatDueLabel(snap.nextBillDays)}"
        } else "No upcoming bills"
        views.setTextViewText(R.id.widget_bill_value, billText)

        views.setViewVisibility(R.id.widget_bill_value, if (heightDp < 110) View.GONE else View.VISIBLE)
        views.setViewVisibility(R.id.widget_safe_value, if (heightDp < 88) View.GONE else View.VISIBLE)

        views.setOnClickPendingIntent(R.id.widget_root, openApp)
        views.setOnClickPendingIntent(R.id.widget_add, quickAdd)
        return views
    }

    private fun rm(v: Double) = String.format(Locale.US, "RM %,.2f", v)

    private fun compactRm(v: Double): String {
        val sign = if (v < 0) "-" else ""
        val a = abs(v)
        return when {
            a >= 1000.0 -> String.format(Locale.US, "%sRM %.1fk", sign, a / 1000.0)
            a >= 100.0 -> String.format(Locale.US, "%sRM %.0f", sign, a)
            a == a.toLong().toDouble() -> String.format(Locale.US, "%sRM %d", sign, a.toLong())
            else -> String.format(Locale.US, "%sRM %.1f", sign, a)
        }
    }
}
