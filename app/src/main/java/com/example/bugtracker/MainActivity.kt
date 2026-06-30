package com.example.bugtracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.bugtracker.ui.theme.BugTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BugTrackerTheme {
                BugTrackerApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BugTrackerApp() {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Bug Tracker") })
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Text(
            text = "Welcome to Bug Tracker!",
            modifier = Modifier.padding(innerPadding)
        )
    }
}
