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
    val note: String = ""
)

data class Category(
    val id: String,
    val name: String,
    val color: Color,
    val colorHex: String
)

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
    val explanation: String = ""
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
    Category("c10", "Top Up (Credit)", Color(0xFF85F8CE), "#85F8CE")
)

fun formatRM(amount: Double): String {
    return String.format("RM %.2f", amount)
}
