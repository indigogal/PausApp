package com.github.indigogal.pausapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [ExerciseEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun exerciseDao(): ExerciseDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pausapp_database"
                )
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialData(database.exerciseDao())
                    }
                }
            }
        }

        suspend fun populateInitialData(dao: ExerciseDao) {
            dao.insertExercise(
                ExerciseEntity(
                    title = "Respiración Profunda",
                    description = "Ejercicio de respiración guiada para relajar el cuerpo y la mente.",
                    durationSeconds = 45,
                    videoUri = "",
                    audioUri = ""
                )
            )
            dao.insertExercise(
                ExerciseEntity(
                    title = "Estiramiento de Cuello",
                    description = "Estiramiento suave para liberar tensión en el cuello y hombros.",
                    durationSeconds = 60,
                    videoUri = "",
                    audioUri = ""
                )
            )
            dao.insertExercise(
                ExerciseEntity(
                    title = "Pausa Activa Postural",
                    description = "Movimientos de columna y hombros para mejorar la postura.",
                    durationSeconds = 90,
                    videoUri = "",
                    audioUri = ""
                )
            )
        }
    }
}
