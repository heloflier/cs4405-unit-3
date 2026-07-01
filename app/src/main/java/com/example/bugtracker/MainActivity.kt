package com.example.bugtracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bugtracker.model.Bug
import com.example.bugtracker.model.Priority
import com.example.bugtracker.model.Status
import com.example.bugtracker.ui.theme.BugTrackerTheme
import com.example.bugtracker.viewmodel.BugViewModel
import java.text.SimpleDateFormat
import java.util.*

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
fun BugTrackerApp(viewModel: BugViewModel = viewModel()) {
    val bugs by viewModel.bugs.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Bug Tracker") })
        },
        floatingActionButton = {
            // FAB opens the add bug dialog
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Bug")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(bugs) { bug ->
                BugItem(
                    bug = bug,
                    onStatusChange = { newStatus ->
                        viewModel.updateBugStatus(bug.id, newStatus)
                    },
                    onDelete = {
                        viewModel.deleteBug(bug)
                    }
                )
            }
        }

        if (showAddDialog) {
            AddBugDialog(
                onDismiss = { showAddDialog = false },
                onAdd = { title, description, priority ->
                    viewModel.addBug(title, description, priority)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun BugItem(bug: Bug, onStatusChange: (Status) -> Unit, onDelete: () -> Unit) {
    // Format the creation timestamp for display
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = bug.title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = bug.description, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Created: ${dateFormat.format(Date(bug.createdAt))}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Priority: ${bug.priority}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "Status: ${bug.status}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Row(
                modifier = Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Only show Start if not already in progress
                Button(
                    onClick = { onStatusChange(Status.IN_PROGRESS) },
                    enabled = bug.status != Status.IN_PROGRESS
                ) {
                    Text("Start")
                }
                // Only show Close if not already closed
                Button(
                    onClick = { onStatusChange(Status.CLOSED) },
                    enabled = bug.status != Status.CLOSED
                ) {
                    Text("Close")
                }
                // Delete removes the bug entirely
                OutlinedButton(onClick = onDelete) {
                    Text("Delete")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBugDialog(onDismiss: () -> Unit, onAdd: (String, String, Priority) -> Unit) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf(Priority.MEDIUM) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Bug") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Priority:")
                // Radio buttons for priority selection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Priority.values().forEach { p ->
                        Column(
                            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                        ) {
                            RadioButton(
                                selected = priority == p,
                                onClick = { priority = p }
                            )
                            Text(p.name, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onAdd(title, description, priority) },
                enabled = title.isNotBlank() // prevent adding bugs with empty titles
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}