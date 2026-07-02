package com.example.bugtracker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bugtracker.data.local.AppDatabase
import com.example.bugtracker.data.remote.RetrofitClient
import com.example.bugtracker.model.Bug
import com.example.bugtracker.model.Priority
import com.example.bugtracker.model.Status
import com.example.bugtracker.repository.BugRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Bridges the UI and the BugRepository.
 * Extends AndroidViewModel to access the Application context needed for the database.
 */
class BugViewModel(application: Application) : AndroidViewModel(application) {

    // Repository is now the single point of contact for all data operations
    private val repository: BugRepository

    // _bugs is mutable internally; bugs is the read-only version exposed to the UI
    private val _bugs = MutableStateFlow<List<Bug>>(emptyList())
    val bugs: StateFlow<List<Bug>> = _bugs

    init {
        val database = AppDatabase.getDatabase(application)
        val dao = database.bugDao()
        val api = RetrofitClient.bugApiService
        repository = BugRepository(dao, api)

        // viewModelScope is tied to this ViewModel's lifecycle — coroutines launched here
        // are automatically cancelled when the ViewModel is destroyed, preventing memory leaks
        viewModelScope.launch {
            repository.allBugs.collect { listOfBugs ->
                _bugs.value = listOfBugs
            }
        }

        // attempt to sync with the server on startup
        refreshBugs()
    }

    fun addBug(title: String, description: String, priority: Priority) {
        viewModelScope.launch {
            repository.addBug(title, description, priority)
        }
    }

    fun updateBugStatus(id: Int, newStatus: Status) {
        viewModelScope.launch {
            repository.updateBugStatus(id, newStatus)
        }
    }

    fun deleteBug(bug: Bug) {
        viewModelScope.launch {
            repository.deleteBug(bug)
        }
    }

    // pulls latest from server — also pushes any unsynced local changes first
    fun refreshBugs() {
        viewModelScope.launch {
            repository.refreshBugs()
        }
    }
}
