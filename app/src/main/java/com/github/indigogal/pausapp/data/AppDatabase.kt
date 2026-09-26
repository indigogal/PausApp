package com.github.indigogal.pausapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [User::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun getUserDAO(): UserDAO

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            // Return the existing instance if it exists.
            // If not, enter the synchronized block.
            return instance ?: synchronized(this) {
                // Check again inside the synchronized block (Double-checked locking)
                instance ?: Room.databaseBuilder(
                    context.applicationContext, // Prevents memory leaks
                    AppDatabase::class.java,
                    "userDB"
                ).build().also {
                    instance = it
                }
            }
        }
    }
}