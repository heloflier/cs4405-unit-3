package com.example.bugtracker.repository

import android.util.Log
import com.example.bugtracker.data.local.BugDao
import com.example.bugtracker.data.remote.BugApiService
import com.example.bugtracker.model.Bug
import com.example.bugtracker.model.Priority
import com.example.bugtracker.model.Status
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

/**
 * Single source of truth for bug data.
 * Manages the flow between the local Room database and the remote API.
 */
class BugRepository(
    private val bugDao: BugDao,
    private val bugApiService: BugApiService
) {
    // UI observes this — always reflects the current local database state
    val allBugs: Flow<List<Bug>> = bugDao.getAllBugs()

    /**
     * Saves a new bug locally first for immediate UI feedback,
     * then attempts to sync it with the server.
     */
    suspend fun addBug(title: String, description: String, priority: Priority) {
        val newBug = Bug(
            title = title,
            description = description,
            priority = priority,
            status = Status.OPEN,
            isSynced = false
        )
        // insertBug returns the new row ID assigned by Room
        val assignedId = bugDao.insertBug(newBug)
        val bugWithId = newBug.copy(id = assignedId.toInt())

        try {
            val response = bugApiService.createBug(bugWithId)
            if (response.isSuccessful) {
                bugDao.updateBug(bugWithId.copy(isSynced = true))
            }
        } catch (e: Exception) {
            Log.d("BugRepository", "Initial sync failed: ${e.message}")
        }
    }

    /**
     * Updates bug status locally and notifies the server.
     */
    suspend fun updateBugStatus(bugId: Int, newStatus: Status) {
        val current = allBugs.first().find { it.id == bugId } ?: return
        val updated = current.copy(status = newStatus, isSynced = false)
        bugDao.updateBug(updated)

        try {
            val response = bugApiService.updateBug(bugId, updated)
            if (response.isSuccessful) {
                bugDao.updateBug(updated.copy(isSynced = true))
            }
        } catch (e: Exception) {
            Log.d("BugRepository", "Update sync failed: ${e.message}")
        }
    }

    /**
     * Retries syncing any bugs that failed to sync previously.
     */
    suspend fun syncUnsyncedBugs() {
        val unsynced = bugDao.getUnsyncedBugs().first()
        if (unsynced.isEmpty()) {
            Log.d("BugRepository", " ----- No unsynced bugs found -----")
            return
        }

        Log.d("BugRepository", " ----- Retrying sync for ${unsynced.size} unsynced bug(s) -----")

        for (bug in unsynced) {
            try {
                val response = if (bug.id == 0) {
                    bugApiService.createBug(bug)
                } else {
                    bugApiService.updateBug(bug.id, bug)
                }
                if (response.isSuccessful) {
                    bugDao.updateBug(bug.copy(isSynced = true))
                    Log.d("BugRepository", " ----- Bug ${bug.id} synced successfully -----")
                } else {
                    Log.d("BugRepository", " ----- Sync failed for bug ${bug.id}: server returned ${response.code()} -----")
                }
            } catch (e: Exception) {
                Log.d("BugRepository", " ----- Retry failed for bug ${bug.id}: ${e.message} ----- ")
            }
        }
    }

    /**
     * Pushes unsynced local changes first, then pulls latest from server.
     */
    suspend fun refreshBugs() {
        syncUnsyncedBugs()
        try {
            val remoteBugs = bugApiService.getBugs()
            // clear local data first to avoid duplicates from ID mismatches
            bugDao.deleteAllBugs()
            for (bug in remoteBugs) {
                bugDao.insertBug(bug.copy(isSynced = true))
            }
        } catch (e: Exception) {
            Log.d("BugRepository", "Refresh failed: ${e.message}")
        }
    }

    /**
     * Deletes a bug locally and removes it from the server.
     */
    suspend fun deleteBug(bug: Bug) {
        bugDao.deleteBug(bug)
        try {
            bugApiService.deleteBug(bug.id)
        } catch (e: Exception) {
            Log.d("BugRepository", "Delete sync failed: ${e.message}")
        }
    }
}
