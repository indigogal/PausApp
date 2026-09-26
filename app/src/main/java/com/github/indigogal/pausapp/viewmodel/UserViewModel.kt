package com.github.indigogal.pausapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.github.indigogal.pausapp.data.User
import com.github.indigogal.pausapp.data.UserDAO
import java.time.LocalDate
import java.time.LocalTime

class UserViewModel(private val userDao: UserDAO) : ViewModel() {

    private val _users = MutableStateFlow<User>(User(
        // Temp user
        uid = 0,
        name = "User",
        streakStart = LocalDate.now(),
        streakEnd = LocalDate.now(),
        reminderTime = LocalTime.parse("17:38"),
    ))

    val user: StateFlow<User> = _users.asStateFlow()

    init {
        fetchUser()
    }

    private fun fetchUser() {
        viewModelScope.launch {
            _users.value = userDao.getUser()
        }
    }

    fun addUser(user: User) {
        viewModelScope.launch {
            userDao.insertUser(user)
            fetchUser()
        }
    }
}