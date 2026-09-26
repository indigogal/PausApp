package com.github.indigogal.pausapp.data

import java.time.LocalDate
import java.time.LocalTime

class FakeUserDAO : UserDAO {

    override suspend fun insertUser(vararg user: User) {
        // Do nothing in preview
    }

    // Make the user configurable for UI testing via args
    override suspend fun getUser(): User {
        return User(
            uid = 0,
            name = "User",
            streakStart = LocalDate.now(),
            streakEnd = LocalDate.now(),
            reminderTime = LocalTime.parse("17:38")
        )
    }

    override suspend fun flushUsers() {
        // Do nothing in preview
    }
}