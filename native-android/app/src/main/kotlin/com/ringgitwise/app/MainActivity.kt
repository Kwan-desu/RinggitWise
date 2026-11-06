package com.ringgitwise.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import com.ringgitwise.app.data.FinanceRepository
import com.ringgitwise.app.data.WidgetBridge
import com.ringgitwise.app.ui.LaunchAction
import com.ringgitwise.app.ui.RinggitWiseApp
import com.ringgitwise.app.ui.theme.RinggitWiseTheme
import com.ringgitwise.app.widget.BillReminderWorker

class MainActivity : ComponentActivity() {

    /** Pending one-shot action coming from the widget or a notification. */
    private val launchAction = mutableStateOf(LaunchAction.NONE)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = FinanceRepository(applicationContext)
        BillReminderWorker.schedule(applicationContext)
        WidgetBridge.requestRefresh(applicationContext)
        handleIntent(intent)

        setContent {
            val themeMode by repository.themeMode.collectAsState()
            val dark = when (themeMode) {
                "light" -> false
                "system" -> isSystemInDarkTheme()
                else -> true
            }
            RinggitWiseTheme(darkTheme = dark) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    RinggitWiseApp(
                        repository = repository,
                        launchAction = launchAction.value,
                        onLaunchActionConsumed = { launchAction.value = LaunchAction.NONE }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        when {
            intent?.getBooleanExtra(EXTRA_OPEN_ADD, false) == true -> launchAction.value = LaunchAction.ADD_EXPENSE
            intent?.getBooleanExtra(EXTRA_OPEN_BILLS, false) == true -> launchAction.value = LaunchAction.OPEN_BILLS
        }
    }

    companion object {
        const val EXTRA_OPEN_ADD = "open_add_expense"
        const val EXTRA_OPEN_BILLS = "open_bills"
    }
}
