package com.github.indigogal.pausapp.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalTime

@Entity
data class User(
    @PrimaryKey val uid: Int,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name="streakStart") val streakStart: LocalDate,
    @ColumnInfo(name = "streakEnd") val streakEnd: LocalDate,
    @ColumnInfo(name="reminderTime") val reminderTime: LocalTime
)