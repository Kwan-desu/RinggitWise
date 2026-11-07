package com.ringgitwise.app.data

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** CSV and PDF report generation for transactions. */
object ExportManager {

    private fun esc(v: String): String {
        val needsQuotes = v.contains(',') || v.contains('"') || v.contains('\n')
        val inner = v.replace("\"", "\"\"")
        return if (needsQuotes) "\"$inner\"" else inner
    }

    fun buildCsv(transactions: List<Transaction>): String {
        val sb = StringBuilder()
        sb.append("Date,Time,Title,Category,Method,Type,Amount (RM),Note,Tolong Beli,Recipient,Settled\n")
        transactions.sortedWith(compareBy({ it.date }, { it.time })).forEach { t ->
            sb.append(listOf(
                t.date, t.time, t.title, t.category, t.method,
                if (t.type == "credit") "Income/Top up" else "Expense",
                String.format(Locale.US, "%.2f", t.amount),
                t.note,
                if (t.isHelpBuy) "Yes" else "No",
                t.claimRecipient,
                if (t.isSettled) "Yes" else "No"
            ).joinToString(",") { esc(it) })
            sb.append('\n')
        }
        return sb.toString()
    }

    /**
     * Writes an A4 PDF report: summary, category breakdown with bars, and a transaction table
     * that flows onto additional pages as needed.
     */
    fun writePdfReport(
        out: OutputStream,
        periodLabel: String,
        transactions: List<Transaction>,
        monthlyBudget: Double?
    ) {
        val pageW = 595
        val pageH = 842
        val margin = 40f
        val doc = PdfDocument()

        val ink = Color.parseColor("#0F172A")
        val muted = Color.parseColor("#64748B")
        val line = Color.parseColor("#E2E8F0")
        val accent = Color.parseColor("#047857")

        val title = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = ink; textSize = 22f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) }
        val h2 = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = ink; textSize = 13f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) }
        val body = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = ink; textSize = 10f }
        val small = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = muted; textSize = 9f }
        val right = Paint(body).apply { textAlign = Paint.Align.RIGHT }
        val rightBold = Paint(right).apply { typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) }
        val rule = Paint().apply { color = line; strokeWidth = 1f }
        val barBg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#F1F5F9") }

        val expenses = transactions.filter { it.type != "credit" && it.amount > 0 }
        val income = transactions.filter { it.type == "credit" }.sumOf { it.amount }
        val spent = expenses.sumOf { it.amount }
        val byCat = expenses.groupBy { it.category.ifBlank { "Other" } }
            .mapValues { e -> e.value.sumOf { it.amount } }
            .toList().sortedByDescending { it.second }

        var pageNo = 0
        var page: PdfDocument.Page? = null
        var y = 0f

        fun newPage() {
            page?.let { doc.finishPage(it) }
            pageNo++
            page = doc.startPage(PdfDocument.PageInfo.Builder(pageW, pageH, pageNo).create())
            y = margin
            page!!.canvas.drawText("RinggitWise • $periodLabel", margin, pageH - 24f, small)
            page!!.canvas.drawText("Page $pageNo", pageW - margin, pageH - 24f, Paint(small).apply { textAlign = Paint.Align.RIGHT })
        }

        fun ensure(space: Float) { if (y + space > pageH - 50f) newPage() }

        newPage()
        val c = { page!!.canvas }

        c().drawText("Spending report", margin, y + 22f, title); y += 34f
        c().drawText(periodLabel + "  •  generated " + SimpleDateFormat("d MMM yyyy, HH:mm", Locale.ENGLISH).format(Date()), margin, y, small); y += 24f

        // Summary boxes
        val boxW = (pageW - margin * 2 - 16f) / 3f
        val summary = listOf(
            "Total spent" to rm(spent),
            (if (monthlyBudget != null) "Budget" else "Income / top ups") to rm(monthlyBudget ?: income),
            "Transactions" to "${transactions.size}"
        )
        summary.forEachIndexed { i, (label, value) ->
            val x = margin + i * (boxW + 8f)
            c().drawRoundRect(x, y, x + boxW, y + 54f, 10f, 10f, barBg)
            c().drawText(label, x + 12f, y + 20f, small)
            c().drawText(value, x + 12f, y + 40f, h2)
        }
        y += 76f

        // Category breakdown
        c().drawText("By category", margin, y, h2); y += 16f
        val maxCat = byCat.maxOfOrNull { it.second } ?: 1.0
        val barX = margin + 150f
        val barW = pageW - margin - 90f - barX
        byCat.forEach { (cat, amt) ->
            ensure(20f)
            c().drawText(cat.take(26), margin, y + 10f, body)
            c().drawRoundRect(barX, y + 2f, barX + barW, y + 12f, 5f, 5f, barBg)
            val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = try { Color.parseColor(CategoryRegistry.resolveColorHex(cat)) } catch (_: Exception) { accent }
            }
            c().drawRoundRect(barX, y + 2f, barX + (barW * (amt / maxCat)).toFloat().coerceAtLeast(6f), y + 12f, 5f, 5f, fill)
            val pct = if (spent > 0) (amt / spent * 100).toInt() else 0
            c().drawText("${rm(amt)}  ($pct%)", pageW - margin, y + 10f, right)
            y += 18f
        }
        if (byCat.isEmpty()) { c().drawText("No expenses in this period.", margin, y + 10f, small); y += 18f }
        y += 14f

        // Transactions table
        ensure(40f)
        c().drawText("Transactions", margin, y, h2); y += 14f
        fun header() {
            c().drawText("Date", margin, y + 10f, small)
            c().drawText("Title", margin + 70f, y + 10f, small)
            c().drawText("Category", margin + 280f, y + 10f, small)
            c().drawText("Amount", pageW - margin, y + 10f, Paint(small).apply { textAlign = Paint.Align.RIGHT })
            y += 16f
            c().drawLine(margin, y, pageW - margin, y, rule); y += 4f
        }
        header()
        transactions.sortedWith(compareByDescending<Transaction> { it.date }.thenByDescending { it.time }).forEach { t ->
            if (y + 18f > pageH - 50f) { newPage(); header() }
            c().drawText(t.date, margin, y + 10f, body)
            c().drawText(ellipsize(t.title, body, 200f), margin + 70f, y + 10f, body)
            c().drawText(ellipsize(t.category, small, 130f), margin + 280f, y + 10f, small)
            val sign = if (t.type == "credit") "+" else ""
            c().drawText(sign + rm(t.amount), pageW - margin, y + 10f, if (t.type == "credit") Paint(rightBold).apply { color = accent } else right)
            y += 16f
            c().drawLine(margin, y, pageW - margin, y, rule); y += 2f
        }

        page?.let { doc.finishPage(it) }
        doc.writeTo(out)
        doc.close()
    }

    private fun ellipsize(text: String, paint: Paint, maxW: Float): String {
        if (paint.measureText(text) <= maxW) return text
        var t = text
        while (t.isNotEmpty() && paint.measureText("$t…") > maxW) t = t.dropLast(1)
        return "$t…"
    }

    private fun rm(v: Double) = String.format(Locale.US, "RM %,.2f", v)
}
