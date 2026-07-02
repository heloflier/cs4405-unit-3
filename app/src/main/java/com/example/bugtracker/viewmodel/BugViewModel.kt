package com.example.bugtracker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
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
 * Uses SavedStateHandle to preserve in-progress form data across rotation and process death.
 */
class BugViewModel(
    application: Application,
    private val savedState: SavedStateHandle
) : AndroidViewModel(application) {

    private val repository: BugRepository

    private val _bugs = MutableStateFlow<List<Bug>>(emptyList())
    val bugs: StateFlow<List<Bug>> = _bugs

    // Form state preserved across rotation via SavedStateHandle
    val draftTitle: StateFlow<String> = savedState.getStateFlow("draft_title", "")
    val draftDescription: StateFlow<String> = savedState.getStateFlow("draft_description", "")
    val draftPriority: StateFlow<Priority> = savedState.getStateFlow("draft_priority", Priority.MEDIUM)

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

        refreshBugs()
    }

    // Update draft state as the user types
    fun updateDraftTitle(value: String) { savedState["draft_title"] = value }
    fun updateDraftDescription(value: String) { savedState["draft_description"] = value }
    fun updateDraftPriority(value: Priority) { savedState["draft_priority"] = value }

    // Clear draft state after a bug is successfully added
    fun clearDraft() {
        savedState["draft_title"] = ""
        savedState["draft_description"] = ""
        savedState["draft_priority"] = Priority.MEDIUM
    }

    fun addBug(title: String, description: String, priority: Priority) {
        viewModelScope.launch {
            repository.addBug(title, description, priority)
            clearDraft()
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

    fun refreshBugs() {
        viewModelScope.launch {
            repository.refreshBugs()
        }
    }
}
