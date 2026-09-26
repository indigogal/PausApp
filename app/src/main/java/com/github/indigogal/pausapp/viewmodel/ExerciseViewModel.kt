package com.github.indigogal.pausapp.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.indigogal.pausapp.data.ExerciseRepository
import com.github.indigogal.pausapp.model.ExerciseSet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ExerciseViewModel(private val context: Context) : ViewModel() {
    private val _currentExerciseSet = MutableStateFlow<ExerciseSet?>(null)
    val currentExerciseSet = _currentExerciseSet.asStateFlow()

    fun setExerciseSet(exerciseSet: ExerciseSet) {
        _currentExerciseSet.value = exerciseSet
    }

    fun loadRandomExerciseSet() {
        viewModelScope.launch(Dispatchers.IO){
            _currentExerciseSet.value = ExerciseRepository.createRandomExerciseSet(context)
        }
    }
}

// TODO: