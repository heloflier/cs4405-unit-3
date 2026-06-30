package com.example.bugtracker.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class Priority {
    LOW, MEDIUM, HIGH
}

enum class Status {
    OPEN, IN_PROGRESS, CLOSED
}

@Entity(tableName = "bugs")
data class Bug(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val description: String,
    val priority: Priority,
    val status: Status,
    val createdAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)