package com.example.fittrackr.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_stats")
data class DailyStat(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val date: String,
    val steps: Int,
    val calories: Int,
    val workoutMinutes: Int
)
