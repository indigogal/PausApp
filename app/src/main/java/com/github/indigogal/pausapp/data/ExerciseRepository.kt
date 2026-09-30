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
            val exerciseName = filename.removeSuffix(".mp4")
            val durationSeconds = getVideoDuration(context, path)
            Exercise(
                id = index,
                name = exerciseName,
                description = getExerciseDescription(context, exerciseName),
                assetPath = path,
                durationSeconds = if (durationSeconds <= 0) 10 else durationSeconds
            )
        }.sortedBy { it.name }

        return exercises!!
    }

    /**
     * Looks up the TalkBack description for an exercise from
     * res/values/exercise_descriptions.xml. The string resource names mirror
     * the exercise names with spaces replaced by underscores
     * (e.g. "Rotación de cuello" -> "Rotación_de_cuello").
     * Returns an empty string when no description exists for the exercise.
     */
    @Suppress("DiscouragedApi") // dynamic lookup keyed by asset filename
    private fun getExerciseDescription(context: Context, exerciseName: String): String {
        val resName = exerciseName.replace(' ', '_')
        val resId = context.resources.getIdentifier(resName, "string", context.packageName)
        return if (resId != 0) context.getString(resId) else ""
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