package com.example.bugtracker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bugtracker.data.local.AppDatabase
import com.example.bugtracker.model.Bug
import com.example.bugtracker.model.Priority
import com.example.bugtracker.model.Status
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Bridges the UI and the local Room database.
 * Extends AndroidViewModel to access the Application context needed for the database.
 */
class BugViewModel(application: Application) : AndroidViewModel(application) {

    // Direct reference to the DAO — replaced by a repository when Retrofit is added
    private val dao = AppDatabase.getDatabase(application).bugDao()

    // _bugs is mutable internally; bugs is the read-only version exposed to the UI
    private val _bugs = MutableStateFlow<List<Bug>>(emptyList())
    val bugs: StateFlow<List<Bug>> = _bugs

    init {
        // viewModelScope is a coroutine scope tied to this ViewModel's lifecycle —
        // any coroutine launched here is automatically canceled when the ViewModel
        // is destroyed, preventing memory leaks
        viewModelScope.launch {
            // collect() listens to the Flow from Room and updates _bugs
            // every time the database changes, keeping the UI in sync automatically
            dao.getAllBugs().collect { listOfBugs ->
                _bugs.value = listOfBugs
            }
        }
    }

    // Database operations run in viewModelScope to avoid blocking the UI thread

    fun addBug(title: String, description: String, priority: Priority) {
        viewModelScope.launch {
            dao.insertBug(
                Bug(
                    title = title,
                    description = description,
                    priority = priority,
                    status = Status.OPEN
                )
            )
        }
    }

    fun updateBugStatus(id: Int, newStatus: Status) {
        viewModelScope.launch {
            // find the bug in the current list, exit silently if not found
            val current = _bugs.value.find { it.id == id } ?: return@launch
            // copy() creates a new Bug object with only the status changed
            dao.updateBug(current.copy(status = newStatus))
        }
    }

    fun deleteBug(bug: Bug) {
        viewModelScope.launch {
            dao.deleteBug(bug)
        }
    }
}
