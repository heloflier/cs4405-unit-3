package com.example.bugtracker.data.local

import androidx.room.*
import com.example.bugtracker.model.Bug
import kotlinx.coroutines.flow.Flow

@Dao
interface BugDao {
    @Query("SELECT * FROM bugs ORDER BY createdAt DESC")
    fun getAllBugs(): Flow<List<Bug>>

    @Query("SELECT * FROM bugs WHERE isSynced = 0")
    fun getUnsyncedBugs(): Flow<List<Bug>>

    @Query("DELETE FROM bugs")
    suspend fun deleteAllBugs()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBug(bug: Bug): Long

    @Update
    suspend fun updateBug(bug: Bug)

    @Delete
    suspend fun deleteBug(bug: Bug)
}
