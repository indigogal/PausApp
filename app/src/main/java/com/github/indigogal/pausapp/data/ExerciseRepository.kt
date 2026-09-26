package com.github.indigogal.pausapp.data

import android.content.Context
import android.media.MediaMetadataRetriever
import com.github.indigogal.pausapp.model.Exercise
import com.github.indigogal.pausapp.model.ExerciseSet

object ExerciseRepository {
    private var exercises: List<Exercise>? = null

    fun createRandomExerciseSet(context: Context): ExerciseSet {
        val exercises = getExercises(context)
        val randomExercises = exercises.shuffled().take(3)
        return ExerciseSet(exercises = randomExercises)
    }

    fun getExercises(context: Context): List<Exercise> {
        if (exercises != null) return exercises!!

        exercises = context.assets.list("exercises")
            ?.filterNot { filename -> filename == "PLACEHOLDER" }
            ?.mapIndexed { index, filename ->
                val durationSeconds = getVideoDuration(context, "exercises/$filename")
                    Exercise(
                        id = index,
                        name = filename.removeSuffix(".mp4"),
                        assetPath = "exercises/$filename",
                        durationSeconds = durationSeconds
                    )
            }
            ?.sortedBy { it.name }
            ?: emptyList()

        return exercises!!
    }

    private fun getVideoDuration(context: Context, assetPath: String): Int {
        return try {
            val retriever = MediaMetadataRetriever()
            val fd = context.assets.openFd(assetPath)

            retriever.setDataSource(fd.fileDescriptor, fd.startOffset, fd.length)
            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                ?.toLong() ?: 0

            retriever.release()
            (duration / 1000).toInt() // convert ms to seconds
        } catch (e: Exception) {
            6 // fallback default
        }
    }
}