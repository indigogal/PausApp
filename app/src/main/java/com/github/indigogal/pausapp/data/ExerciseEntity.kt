package com.github.indigogal.pausapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exercises")
data class ExerciseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val description: String,
    val durationSeconds: Int,
    val videoUri: String,
    val audioUri: String
)
