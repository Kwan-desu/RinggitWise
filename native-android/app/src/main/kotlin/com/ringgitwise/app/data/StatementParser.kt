package com.ringgitwise.app.data

import java.text.SimpleDateFormat
import java.util.Locale
import java.util.regex.Pattern

object StatementParser {

    fun autoCategorize(title: String, rawLine: String): String {
        val lower = "$title $rawLine".lowercase(Locale.ROOT)

        // 1. Parking & Tolls
        if (lower.contains(Regex("""\b(parking|parkir|valet|autopay|toll|plazatoll|rfid|smarttag)\b|touch\s*['’]?n\s*go|tng\s*parking|plus\s*toll|mbpj"""))) {
            return "Parking & Tolls"
        }

        // 2. Main Meals & Dining
        if (lower.contains(Regex("""\b(rice|katsu|curry|chicken|mee|nasi|kandar|roti|burger|lunch|dinner|breakfast|food|dining|makan|restaurant|bistro|cafeteria|ayam|daging|ikan|soup|noodle|pasta|spaghetti|pizza|mcdonald|mcd|kfc|subway|marrybrown|texas|domino)\b"""))) {
            return "Mamak & Dining"
        }

        // 3. Drinks & Cafe
        if (lower.contains(Regex("""\b(americano|latte|cappuccino|coffee|kopi|teh|tea|tealive|zus|starbucks|chagee|boba|ice|drink|drinks|beverage|mocha|espresso)\b"""))) {
            return "Drinks & Cafe"
        }

        // 4. Snacks & Desserts
        if (lower.contains(Regex("""\b(waffle|cake|dessert|donut|doughnut|croissant|snack|cookie|bakery|aiskrim|pastry|pie)\b|ice\s*cream"""))) {
            return "Snacks & Desserts"
        }

        // 5. Fuel & Transport
        if (lower.contains(Regex("""\b(petronas|shell|petron|caltex|fuel|petrol|diesel|rapid|mrt|lrt|bus|bas|ktm|monorail)\b|grab\s*ride"""))) {
            return "Fuel & Transport"
        }

        // 6. Groceries & Supermarkets
        if (lower.contains(Regex("""\b(grocer|groceries|speedmart|lotus|jaya|village|supermarket|pasar|mydin|econsave)\b|family\s*mart|7-eleven|cu\s*mart"""))) {
            return "Groceries"
        }

        // 7. Shopping & Retail
        if (lower.contains(Regex("""\b(shopee|lazada|tiktok|uniqlo|padini|h&m|zara|boutique)\b|mr\s*diy"""))) {
            return "Shopping & Online"
        }

        // 8. Utilities & Monthly Bills
        if (lower.contains(Regex("""\b(unifi|maxis|celcom|digi|tnb|astro|bill|bills|utility)\b|air\s*selangor|indah\s*water"""))) {
            return "Utilities & Bills"
        }

        // 9. Healthcare & Pharmacy
        if (lower.contains(Regex("""\b(clinic|klinik|hospital|pharmacy|farmasi|guardian|watsons|caring)\b"""))) {
            return "Healthcare & Pharmacy"
        }

        // 10. Top Up
        if (lower.contains(Regex("""\b(top\s*up|reload|deposit|credit)\b""")) && !lower.contains(Regex("""\b(payment|bayaran)\b"""))) {
            return "Top Up (Credit)"
        }

        // 11. Tolong Beli (Help Buy / Claims)
        if (lower.contains(Regex("""\b(tolong|help\s*buy|bought\s*for|claim|split|iou)\b"""))) {
            return "Tolong Beli (Help Buy)"
        }

        return "Mamak & Dining"
    }

    fun normalizeDate(rawDate: String): String {
        val trimmed = rawDate.trim()
        if (trimmed.matches(Regex("""^\d{4}-\d{2}-\d{2}$"""))) {
            return trimmed
        }

        // DD/MM/YYYY or DD-MM-YYYY
        if (trimmed.matches(Regex("""^\d{1,2}[/-]\d{1,2}[/-]\d{2,4}$"""))) {
            val parts = trimmed.split(Regex("""[/-]"""))
            if (parts.size == 3) {
                if (parts[0].length == 4) {
                    return String.format("%s-%02d-%02d", parts[0], parts[1].toInt(), parts[2].toInt())
                } else {
                    val y = if (parts[2].length == 2) "20" + parts[2] else parts[2]
                    return String.format("%s-%02d-%02d", y, parts[1].toInt(), parts[0].toInt())
                }
            }
        }

        // DD Mon YYYY e.g. 20 Oct 2026
        try {
            val parser = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
            val date = parser.parse(trimmed)
            if (date != null) {
                val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
                return formatter.format(date)
            }
        } catch (_: Exception) {}

        return SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(java.util.Date())
    }

    fun parseLine(line: String): Transaction? {
        val cleanLine = line.trim()
        if (cleanLine.length < 5) return null

        // 1. Date at start (direct or indexed like "18 2026-10-16")
        val dateRegex = Regex("""^(?:#?\s*\d+[\s|]+)?(\d{1,2}[/-]\d{1,2}[/-]\d{2,4}|\d{1,2}\s+[A-Za-z]{3,9}\s+\d{2,4}|\d{4}-\d{2}-\d{2})\b""")
        val dateMatch = dateRegex.find(cleanLine) ?: return null

        val rawDate = dateMatch.groupValues[1]
        var rest = cleanLine.substring(dateMatch.range.last + 1).trim()
        if (rest.startsWith("|")) rest = rest.substring(1).trim()

        // 2. Time
        val timeRegex = Regex("""^(\d{1,2}:\d{2}(?::\d{2})?\s*(?:AM|PM|am|pm)?)\b""", RegexOption.IGNORE_CASE)
        val timeMatch = timeRegex.find(rest)
        var time = ""
        if (timeMatch != null) {
            time = timeMatch.groupValues[1].trim()
            rest = rest.substring(timeMatch.range.last + 1).trim()
            if (rest.startsWith("|")) rest = rest.substring(1).trim()
        }

        // 3. Amount and trailing balance
        val amountRegex = Regex("""([+-]?(?:RM|MYR)?\s*[0-9]{1,5}(?:,[0-9]{3})*(?:\.[0-9]{1,2})?)\s*(?:(?:RM|MYR)?\s*[0-9]{1,5}(?:,[0-9]{3})*(?:\.[0-9]{1,2})?)?$""", RegexOption.IGNORE_CASE)
        val amtMatch = amountRegex.find(rest) ?: return null

        val rawAmtStr = amtMatch.groupValues[1].replace(Regex("""RM|MYR|\s""", RegexOption.IGNORE_CASE), "")
        val parsedAmt = kotlin.math.abs(rawAmtStr.toDoubleOrNull() ?: return null)
        if (parsedAmt <= 0) return null

        val isCredit = rawAmtStr.startsWith("+") ||
                (cleanLine.contains(Regex("""\b(reload|top\s*up|received|cash\s*in|deposit)\b""", RegexOption.IGNORE_CASE)) &&
                        !cleanLine.contains(Regex("""\b(payment|bayaran)\b""", RegexOption.IGNORE_CASE)))

        // 4. Title / Description
        var rawTitle = rest.substring(0, amtMatch.range.first).trim()
        rawTitle = rawTitle
            .replace(Regex("""[|#]"""), " ")
            .replace(Regex("""\b(Successful|Success|Completed|Failed|Berjaya|Pending)\b""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""\b(TR\d{8,26}|TNG\d{6,26}|\d{16,26})\b""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""\b(DuitNow\s*QR\s*Payment|Quick\s*Reload\s*Payment|QR\s*Payment|Direct\s*Charge|Payment)\b""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""^[\s\-:|]+|[\s\-:|]+$"""), "")
            .trim()

        if (rawTitle.isEmpty()) {
            rawTitle = when {
                cleanLine.contains("rfid", true) || cleanLine.contains("toll", true) -> "RFID Toll Charge"
                cleanLine.contains("parking", true) || cleanLine.contains("mbpj", true) -> "Parking Fee"
                cleanLine.contains("reload", true) || cleanLine.contains("top up", true) -> "Wallet Reload"
                else -> "E-Wallet Expense"
            }
        }

        val method = when {
            cleanLine.contains("apspace", true) || cleanLine.contains("campus", true) -> "Campus Card / APCard"
            cleanLine.contains("maybank", true) || cleanLine.contains("mae", true) -> "MAE by Maybank"
            cleanLine.contains("cimb", true) -> "CIMB OCTO"
            cleanLine.contains("grab", true) -> "GrabPay"
            else -> "Touch 'n Go eWallet"
        }

        val category = autoCategorize(rawTitle, cleanLine)

        return Transaction(
            date = normalizeDate(rawDate),
            time = time,
            title = rawTitle,
            amount = parsedAmt,
            type = if (isCredit) "credit" else "debit",
            category = category,
            method = method,
            note = if (time.isNotEmpty()) "Time: $time" else ""
        )
    }

    fun parseFullText(rawText: String): List<Transaction> {
        val lines = rawText.lines()
        val list = mutableListOf<Transaction>()
        for (line in lines) {
            val tx = parseLine(line)
            if (tx != null) {
                list.add(tx)
            }
        }
        return list
    }

    fun parseFinancialText(rawText: String): List<Transaction> {
        val cleanText = rawText.replace("\r", "").trim()
        if (cleanText.isEmpty()) return emptyList()

        // 1. Try parsing line-by-line statement table
        val statementResults = parseFullText(cleanText)
        if (statementResults.isNotEmpty()) {
            return statementResults
        }

        // 2. Try parsing single receipt / DuitNow slip
        val singleTx = parseSingleReceipt(cleanText)
        if (singleTx != null) {
            return listOf(singleTx)
        }

        return emptyList()
    }

    fun parseSingleReceipt(rawText: String): Transaction? {
        val lines = rawText.lines().map { it.trim() }.filter { it.isNotEmpty() }
        if (lines.isEmpty()) return null

        // 1. Amount Extraction
        val amountRegexes = listOf(
            Regex("""(?:Total|Jumlah|Grand\s*Total|Amount|Bayaran|Nilai)\s*[:=]?\s*(?:RM|MYR)?\s*([0-9]{1,4}(?:,[0-9]{3})*\.[0-9]{2})""", RegexOption.IGNORE_CASE),
            Regex("""(?:RM|MYR)\s*([0-9]{1,4}(?:,[0-9]{3})*\.[0-9]{2})""", RegexOption.IGNORE_CASE),
            Regex("""([0-9]{1,4}\.[0-9]{2})\s*(?:RM|MYR)""", RegexOption.IGNORE_CASE),
            Regex("""\b([0-9]{1,4}\.[0-9]{2})\b""")
        )

        var amount: Double? = null
        for (regex in amountRegexes) {
            for (line in lines) {
                val match = regex.find(line)
                if (match != null) {
                    val rawVal = match.groupValues[1].replace(",", "")
                    val parsed = rawVal.toDoubleOrNull()
                    if (parsed != null && parsed > 0) {
                        amount = parsed
                        break
                    }
                }
            }
            if (amount != null) break
        }

        if (amount == null || amount <= 0) return null

        // 2. Date Extraction
        var dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(java.util.Date())
        val dateRegexes = listOf(
            Regex("""\b(\d{1,2}[/-]\d{1,2}[/-]\d{2,4})\b"""),
            Regex("""\b(\d{1,2}\s+(?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)[a-z]*\s+\d{2,4})\b""", RegexOption.IGNORE_CASE),
            Regex("""\b(\d{4}-\d{2}-\d{2})\b""")
        )

        for (regex in dateRegexes) {
            val match = regex.find(rawText)
            if (match != null) {
                dateStr = normalizeDate(match.groupValues[1])
                break
            }
        }

        // 3. Time Extraction
        var timeStr = ""
        val timeMatch = Regex("""\b(\d{1,2}:\d{2}(?::\d{2})?\s*(?:AM|PM|am|pm)?)\b""").find(rawText)
        if (timeMatch != null) {
            timeStr = timeMatch.groupValues[1]
        }

        // 4. Merchant / Recipient Extraction
        var merchant = ""
        val recipientMatch = Regex("""(?:To|Recipient|Penerima|Merchant|Paid\s*to)\s*[:]?\s*(.+)""", RegexOption.IGNORE_CASE).find(rawText)
        if (recipientMatch != null) {
            merchant = recipientMatch.groupValues[1].trim()
        }

        if (merchant.isEmpty()) {
            val ignoreKeywords = listOf("receipt", "tax", "invoice", "successful", "completed", "duitnow", "transfer", "payment", "bank", "welcome")
            for (line in lines.take(6)) {
                val lower = line.lowercase(Locale.ROOT)
                if (line.length in 3..40 && !ignoreKeywords.any { lower.contains(it) } && !line.matches(Regex("""^[\d\s:./-]+$"""))) {
                    merchant = line
                    break
                }
            }
        }

        if (merchant.isEmpty()) {
            merchant = "Scanned Receipt"
        }

        val category = autoCategorize(merchant, rawText)
        val method = when {
            rawText.contains("duitnow", true) -> "DuitNow QR"
            rawText.contains("touch", true) || rawText.contains("tng", true) -> "Touch 'n Go eWallet"
            rawText.contains("maybank", true) || rawText.contains("mae", true) -> "MAE by Maybank"
            rawText.contains("grab", true) -> "GrabPay"
            rawText.contains("card", true) || rawText.contains("visa", true) || rawText.contains("mastercard", true) -> "Card Payment"
            else -> "Receipt Scan"
        }

        return Transaction(
            date = dateStr,
            time = timeStr,
            title = merchant,
            amount = amount,
            category = category,
            method = method,
            note = if (timeStr.isNotEmpty()) "OCR Time: $timeStr" else "Scanned via OCR"
        )
    }
}
