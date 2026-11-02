package com.ringgitwise.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.ringgitwise.app.data.FinanceRepository
import com.ringgitwise.app.ui.RinggitWiseApp
import com.ringgitwise.app.ui.theme.RinggitWiseTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = FinanceRepository(applicationContext)

        setContent {
            RinggitWiseTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    RinggitWiseApp(repository = repository)
                }
            }
        }
    }
}
