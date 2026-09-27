package com.github.indigogal.pausapp.data

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.OnConflictStrategy
import java.time.LocalDate
import java.time.LocalTime

@Entity
data class User(
    @PrimaryKey val uid: Int,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name="streakStart") val streakStart: LocalDate,
    @ColumnInfo(name = "streakEnd") val streakEnd: LocalDate,
    @ColumnInfo(name="reminderTime") val reminderTime: LocalTime
) {
    val isRegistered: Boolean
        get() = uid != 0
}

@Dao
interface UserDAO{
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(vararg user: User)

    @Query("SELECT * FROM user WHERE uid != 0 LIMIT 1")
    suspend fun getUser() : User?

    // INFO: may not end up using this, but if we implement a button-
    // to reset user it may come in handy
    @Query("DELETE FROM user")
    suspend fun flushUsers()

    // The temp user is created when instantiating the user db for the first time,
    // neeeds to be removed after registration
    @Query("DELETE FROM user WHERE uid = 0")
    suspend fun deleteTempUser()

}