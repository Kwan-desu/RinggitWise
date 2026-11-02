package com.ringgitwise.app.data

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FinanceRepository(private val context: Context) {

    private val prefs = context.getSharedPreferences("ringgitwise_native_prefs", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }

    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()

    private val _monthlyBudget = MutableStateFlow(3500.0)
    val monthlyBudget: StateFlow<Double> = _monthlyBudget.asStateFlow()

    private val _selectedMonth = MutableStateFlow(currentMonthString())
    val selectedMonth: StateFlow<String> = _selectedMonth.asStateFlow()

    // Profile Customizations (Avatar / Emoji / Logo)
    private val _userProfileEmoji = MutableStateFlow(prefs.getString("profile_emoji", "RM") ?: "RM")
    val userProfileEmoji: StateFlow<String> = _userProfileEmoji.asStateFlow()

    private val _avatarUpdateTrigger = MutableStateFlow(System.currentTimeMillis())
    val avatarUpdateTrigger: StateFlow<Long> = _avatarUpdateTrigger.asStateFlow()

    private val _hasCustomProfileImage = MutableStateFlow(File(context.filesDir, "user_avatar.jpg").exists())
    val hasCustomProfileImage: StateFlow<Boolean> = _hasCustomProfileImage.asStateFlow()

    // Custom Background Image
    private val _hasCustomBackground = MutableStateFlow(File(context.filesDir, "user_bg.jpg").exists())
    val hasCustomBackground: StateFlow<Boolean> = _hasCustomBackground.asStateFlow()

    private val _bgUpdateTrigger = MutableStateFlow(System.currentTimeMillis())
    val bgUpdateTrigger: StateFlow<Long> = _bgUpdateTrigger.asStateFlow()

    private val _customBgDim = MutableStateFlow(prefs.getFloat("custom_bg_dim", 0.78f))
    val customBgDim: StateFlow<Float> = _customBgDim.asStateFlow()

    // Chart Display Mode ("half_pie", "donut", "bar", "timeline")
    private val _chartMode = MutableStateFlow(prefs.getString("chart_mode", "half_pie") ?: "half_pie")
    val chartMode: StateFlow<String> = _chartMode.asStateFlow()

    init {
        loadData()
    }

    private fun currentMonthString(): String {
        return SimpleDateFormat("yyyy-MM", Locale.ENGLISH).format(Date())
    }

    private fun loadData() {
        val budgetVal = prefs.getFloat("monthly_budget", 3500f).toDouble()
        _monthlyBudget.value = budgetVal

        val monthVal = prefs.getString("selected_month", null) ?: currentMonthString()
        _selectedMonth.value = monthVal

        val txJson = prefs.getString("transactions_json", null)
        if (!txJson.isNullOrEmpty()) {
            try {
                val list = json.decodeFromString<List<Transaction>>(txJson)
                _transactions.value = list
            } catch (_: Exception) {
                _transactions.value = emptyList()
            }
        } else {
            _transactions.value = emptyList()
        }
    }

    fun addTransaction(tx: Transaction) {
        val current = _transactions.value.toMutableList()
        current.add(0, tx)
        _transactions.value = current
        saveTransactions(current)
    }

    fun addMultipleTransactions(txs: List<Transaction>) {
        val current = _transactions.value.toMutableList()
        current.addAll(0, txs)
        _transactions.value = current
        saveTransactions(current)
    }

    fun deleteTransaction(id: String) {
        val current = _transactions.value.filter { it.id != id }
        _transactions.value = current
        saveTransactions(current)
    }

    fun updateBudget(newBudget: Double) {
        _monthlyBudget.value = newBudget
        prefs.edit().putFloat("monthly_budget", newBudget.toFloat()).apply()
    }

    fun selectMonth(month: String) {
        _selectedMonth.value = month
        prefs.edit().putString("selected_month", month).apply()
    }

    fun setChartMode(mode: String) {
        _chartMode.value = mode
        prefs.edit().putString("chart_mode", mode).apply()
    }

    fun setProfileEmoji(emoji: String) {
        _userProfileEmoji.value = emoji
        prefs.edit().putString("profile_emoji", emoji).apply()
        // If an emoji is selected, remove the custom photo so emoji displays
        removeProfileImage()
    }

    fun getProfileAvatarFile(): File = File(context.filesDir, "user_avatar.jpg")

    fun getBackgroundFile(): File = File(context.filesDir, "user_bg.jpg")

    fun saveProfileImage(uri: Uri) {
        try {
            val destFile = getProfileAvatarFile()
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }
            _hasCustomProfileImage.value = true
            _avatarUpdateTrigger.value = System.currentTimeMillis()
        } catch (_: Exception) {}
    }

    fun removeProfileImage() {
        val file = getProfileAvatarFile()
        if (file.exists()) file.delete()
        _hasCustomProfileImage.value = false
        _avatarUpdateTrigger.value = System.currentTimeMillis()
    }

    fun saveBackgroundImage(uri: Uri) {
        try {
            val destFile = getBackgroundFile()
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }
            _hasCustomBackground.value = true
            _bgUpdateTrigger.value = System.currentTimeMillis()
        } catch (_: Exception) {}
    }

    fun removeBackgroundImage() {
        val file = getBackgroundFile()
        if (file.exists()) file.delete()
        _hasCustomBackground.value = false
        _bgUpdateTrigger.value = System.currentTimeMillis()
    }

    fun setCustomBgDim(dim: Float) {
        _customBgDim.value = dim
        prefs.edit().putFloat("custom_bg_dim", dim).apply()
    }

    fun clearAllData() {
        _transactions.value = emptyList()
        prefs.edit().remove("transactions_json").apply()
    }

    private fun saveTransactions(list: List<Transaction>) {
        try {
            val encoded = json.encodeToString(list)
            prefs.edit().putString("transactions_json", encoded).apply()
        } catch (_: Exception) {}
    }
}
