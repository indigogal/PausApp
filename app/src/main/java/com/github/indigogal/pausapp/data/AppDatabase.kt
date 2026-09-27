package com.github.indigogal.pausapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [User::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun getUserDAO(): UserDAO

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        /**
         * v1 -> v2 makes streakStart/streakEnd nullable: a user has no streak
         * until they complete their first routine. SQLite cannot alter columns,
         * so we rebuild the table and copy the existing data over, preserving
         * the user's name, reminder time and any legitimate streak.
         */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `user_new` (
                        `uid` INTEGER NOT NULL,
                        `name` TEXT NOT NULL,
                        `streakStart` TEXT,
                        `streakEnd` TEXT,
                        `reminderTime` TEXT NOT NULL,
                        PRIMARY KEY(`uid`)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO `user_new` (uid, name, streakStart, streakEnd, reminderTime)
                    SELECT uid, name, streakStart, streakEnd, reminderTime FROM `user`
                    """.trimIndent()
                )
                db.execSQL("DROP TABLE `user`")
                db.execSQL("ALTER TABLE `user_new` RENAME TO `user`")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            // Return the existing instance if it exists.
            // If not, enter the synchronized block.
            return instance ?: synchronized(this) {
                // Check again inside the synchronized block (Double-checked locking)
                instance ?: Room.databaseBuilder(
                    context.applicationContext, // Prevents memory leaks
                    AppDatabase::class.java,
                    "userDB"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build().also {
                    instance = it
                }
            }
        }
    }
}