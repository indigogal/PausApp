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

        val exercisesInSubfolder = context.assets.list("exercises")
            ?.filter { it.endsWith(".mp4") && it != "PLACEHOLDER" }
            ?.map { "exercises/$it" } ?: emptyList()

        val assetPaths = if (exercisesInSubfolder.isNotEmpty()) {
            exercisesInSubfolder
        } else {
            context.assets.list("")
                ?.filter { it.endsWith(".mp4") && it != "PLACEHOLDER" }
                ?: emptyList()
        }

        exercises = assetPaths.mapIndexed { index, path ->
            val filename = path.substringAfterLast("/")
            val durationSeconds = getVideoDuration(context, path)
            Exercise(
                id = index,
                name = filename.removeSuffix(".mp4"),
                assetPath = path,
                durationSeconds = if (durationSeconds <= 0) 10 else durationSeconds
            )
        }.sortedBy { it.name }

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
            10 // fallback default
        }
    }
}