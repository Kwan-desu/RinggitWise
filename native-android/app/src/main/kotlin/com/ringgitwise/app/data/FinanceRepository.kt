package com.ringgitwise.app.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FinanceRepository(context: Context) {

    private val prefs = context.getSharedPreferences("ringgitwise_native_prefs", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }

    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()

    private val _monthlyBudget = MutableStateFlow(3500.0)
    val monthlyBudget: StateFlow<Double> = _monthlyBudget.asStateFlow()

    private val _selectedMonth = MutableStateFlow(currentMonthString())
    val selectedMonth: StateFlow<String> = _selectedMonth.asStateFlow()

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
