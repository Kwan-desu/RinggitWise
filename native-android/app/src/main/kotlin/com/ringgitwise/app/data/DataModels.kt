package com.ringgitwise.app.data

import androidx.compose.ui.graphics.Color
import kotlinx.serialization.Serializable

@Serializable
data class Transaction(
    val id: String = "tx_${System.currentTimeMillis()}_${(1000..9999).random()}",
    val date: String, // YYYY-MM-DD
    val time: String = "",
    val title: String,
    val amount: Double,
    val type: String = "debit", // "debit" (expense) or "credit" (topup/income)
    val category: String = "Mamak & Dining",
    val method: String = "DuitNow QR",
    val note: String = "",
    val isHelpBuy: Boolean = false, // True if bought on behalf of someone else
    val claimRecipient: String = "", // Recipient / Friend's name (e.g. "Ah Ming", "Sarah")
    val isSettled: Boolean = false // false = Pending Claim / Unpaid, true = Settled
)

data class Category(
    val id: String,
    val name: String,
    val color: Color,
    val colorHex: String,
    val iconKey: String = "",
    val isCustom: Boolean = false
)

@Serializable
data class CustomCategory(
    val id: String = "cat_${System.currentTimeMillis()}_${(1000..9999).random()}",
    val name: String,
    val colorHex: String = "#10B981",
    val iconKey: String = "tag"
) {
    fun toCategory(): Category {
        val parsedColor = try {
            Color(android.graphics.Color.parseColor(colorHex))
        } catch (_: Exception) {
            Color(0xFF10B981)
        }
        return Category(
            id = id,
            name = name,
            color = parsedColor,
            colorHex = colorHex,
            iconKey = iconKey,
            isCustom = true
        )
    }
}

val CATEGORY_COLORS = listOf(
    "#10B981", "#38BDF8", "#A856EA", "#F59E0B",
    "#F43F5E", "#EC4899", "#06B6D4", "#84CC16",
    "#F97316", "#6366F1", "#14B8A6", "#EAB308"
)

val CATEGORY_ICON_KEYS = listOf(
    "tag", "restaurant", "cafe", "fastfood",
    "groceries", "shopping", "car", "fuel",
    "flight", "home", "bills", "health",
    "fitness", "gaming", "movie", "school",
    "work", "pets", "gift", "beauty",
    "tech", "savings", "charity", "payments"
)

object CategoryRegistry {
    @Volatile
    var customCategories: List<CustomCategory> = emptyList()

    fun allCategories(custom: List<CustomCategory> = customCategories): List<Category> {
        val customMapped = custom.map { it.toCategory() }
        val defaultFiltered = DEFAULT_CATEGORIES.filter { def ->
            customMapped.none { it.name.equals(def.name, ignoreCase = true) }
        }
        return customMapped + defaultFiltered
    }

    fun findCategory(name: String, custom: List<CustomCategory> = customCategories): Category? {
        val trimmed = name.trim()
        custom.find { it.name.equals(trimmed, ignoreCase = true) }?.let { return it.toCategory() }
        DEFAULT_CATEGORIES.find { it.name.equals(trimmed, ignoreCase = true) }?.let { return it }
        return null
    }

    fun resolveColor(name: String, custom: List<CustomCategory> = customCategories): Color {
        findCategory(name, custom)?.let { return it.color }
        val palette = CATEGORY_COLORS.mapNotNull {
            try { Color(android.graphics.Color.parseColor(it)) } catch (_: Exception) { null }
        }
        if (palette.isEmpty()) return Color(0xFF10B981)
        val idx = (name.trim().lowercase().hashCode() and 0x7FFFFFFF) % palette.size
        return palette[idx]
    }

    fun resolveColorHex(name: String, custom: List<CustomCategory> = customCategories): String {
        findCategory(name, custom)?.let { return it.colorHex }
        val idx = (name.trim().lowercase().hashCode() and 0x7FFFFFFF) % CATEGORY_COLORS.size
        return CATEGORY_COLORS[idx]
    }
}

@Serializable
data class GeminiProposedAction(
    val actionType: String, // "ADD", "DELETE", "UPDATE", "MESSAGE"
    val title: String = "",
    val amount: Double = 0.0,
    val type: String = "debit", // "debit" or "credit"
    val category: String = "Mamak & Dining",
    val method: String = "DuitNow QR",
    val date: String = "",
    val time: String = "",
    val note: String = "",
    val targetId: String = "", // for delete/update
    val explanation: String = "",
    val isHelpBuy: Boolean = false,
    val claimRecipient: String = ""
)

@Serializable
data class GeminiExecutionResult(
    val success: Boolean,
    val summary: String,
    val actions: List<GeminiProposedAction> = emptyList(),
    val rawResponse: String = "",
    val errorMessage: String? = null
)

val DEFAULT_CATEGORIES = listOf(
    Category("c1", "Parking & Tolls", Color(0xFFF43F5E), "#F43F5E"),
    Category("c2", "Mamak & Dining", Color(0xFFF59E0B), "#F59E0B"),
    Category("c3", "Drinks & Cafe", Color(0xFF06B6D4), "#06B6D4"),
    Category("c4", "Groceries", Color(0xFF10B981), "#10B981"),
    Category("c5", "Fuel & Transport", Color(0xFF54B3D6), "#54B3D6"),
    Category("c6", "Shopping & Online", Color(0xFFA856EA), "#A856EA"),
    Category("c7", "Utilities & Bills", Color(0xFF64748B), "#64748B"),
    Category("c8", "Healthcare & Pharmacy", Color(0xFFEC4899), "#EC4899"),
    Category("c9", "Snacks & Desserts", Color(0xFFC856DB), "#C856DB"),
    Category("c10", "Top Up (Credit)", Color(0xFF85F8CE), "#85F8CE"),
    Category("c11", "Tolong Beli (Help Buy)", Color(0xFF38BDF8), "#38BDF8")
)

fun formatRM(amount: Double): String {
    return String.format("RM %.2f", amount)
}
