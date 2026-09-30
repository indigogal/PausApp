package com.github.indigogal.pausapp.model

data class Exercise(
    val id: Number,
    val name: String,
    val description: String = "",
    val assetPath: String,
    val durationSeconds: Number,
)