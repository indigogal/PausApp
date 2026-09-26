package com.github.indigogal.pausapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.github.indigogal.pausapp.data.UserDAO

class UserViewModelFactory(private val userDAO: UserDAO): ViewModelProvider.Factory {
    override fun<T: ViewModel> create(modelClass: Class<T>): T{
        if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UserViewModel(userDAO) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
    }