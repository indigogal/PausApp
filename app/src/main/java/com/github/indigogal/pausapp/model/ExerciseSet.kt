package com.github.indigogal.pausapp.model

data class ExerciseSet(
    val exercises: List<Exercise>,
    val amountCompleted: Int = 0
)