package com.github.indigogal.pausapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.github.indigogal.pausapp.data.User
import com.github.indigogal.pausapp.data.UserDAO
import kotlinx.coroutines.Dispatchers
import java.time.LocalDate
import java.time.LocalTime

class UserViewModel(private val userDao: UserDAO) : ViewModel() {

    private val _users = MutableStateFlow<User>(User(
        // Temp user: no streak until the first routine is completed
        uid = 0,
        name = "User",
        streakStart = null,
        streakEnd = null,
        reminderTime = LocalTime.parse("17:38"),
    ))

    val user: StateFlow<User> = _users.asStateFlow()

    init {
        fetchUser()
    }

    private fun fetchUser() {
        viewModelScope.launch(Dispatchers.IO) {
            userDao.getUser()?.let { user ->
                _users.value = user
            }
        }
    }

    fun addUser(user: User) {
        viewModelScope.launch(Dispatchers.IO) {
            userDao.insertUser(user)
            userDao.deleteTempUser()
            fetchUser()
        }
    }

    fun completeRoutine(today: LocalDate = LocalDate.now()) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentUser = userDao.getUser() ?: _users.value
            // Null until the user completes their first routine
            val newStreakStart: LocalDate?
            val newStreakEnd = today

            when {
                // Already completed today: keep streakStart, streakEnd is today
                currentUser.streakEnd == today -> {
                    newStreakStart = currentUser.streakStart
                }
                // Streak consecutive (yesterday was streakEnd): keep streakStart, streakEnd is today
                currentUser.streakEnd?.plusDays(1) == today -> {
                    newStreakStart = currentUser.streakStart
                }
                // First completion, streak broken (missed day) or invalid: start fresh today
                else -> {
                    newStreakStart = today
                }
            }

            val updatedUser = currentUser.copy(
                streakStart = newStreakStart,
                streakEnd = newStreakEnd
            )
            userDao.insertUser(updatedUser)
            fetchUser()
        }
    }
}