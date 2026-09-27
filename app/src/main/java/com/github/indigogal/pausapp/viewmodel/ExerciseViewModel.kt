package com.github.indigogal.pausapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.github.indigogal.pausapp.data.ExerciseRepository
import com.github.indigogal.pausapp.model.ExerciseSet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ExerciseViewModel(application: Application) : AndroidViewModel(application) {
    private val _currentExerciseSet = MutableStateFlow<ExerciseSet?>(null)
    val currentExerciseSet = _currentExerciseSet.asStateFlow()

    fun setExerciseSet(exerciseSet: ExerciseSet) {
        _currentExerciseSet.value = exerciseSet
    }

    fun loadRandomExerciseSet() {
        viewModelScope.launch(Dispatchers.IO){
            _currentExerciseSet.value = ExerciseRepository.createRandomExerciseSet(getApplication())
        }
    }
}