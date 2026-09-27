package com.github.indigogal.pausapp

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

/**
 * Unit tests for the streak calculation checks used by [RachaScreen].
 *
 * These verify the guards that prevent routines from being attributed
 * incorrectly or streaks being ended prematurely:
 *  - A streak stays valid on the day right after the last completion
 *    (the user still has that day to complete the routine).
 *  - A streak shows 0 once more than one day has been missed.
 *  - Invalid ranges never count as a streak.
 */
class RachaStreakLogicTest {

    @Test
    fun `streak counts the inclusive range when active`() {
        val start = LocalDate.of(2026, 9, 20)
        val end = LocalDate.of(2026, 9, 22)
        assertEquals(3, calculateStreakDays(start, end, LocalDate.of(2026, 9, 22)))
    }

    @Test
    fun `streak remains valid on the day after the last completion`() {
        // User completed yesterday; today is not counted yet, but the
        // streak must not be shown as broken until the day is over.
        val start = LocalDate.of(2026, 9, 20)
        val end = LocalDate.of(2026, 9, 25)
        assertEquals(6, calculateStreakDays(start, end, LocalDate.of(2026, 9, 26)))
    }

    @Test
    fun `broken streak returns zero after more than one missed day`() {
        // Last completed two days ago -> streak is broken.
        val start = LocalDate.of(2026, 9, 20)
        val end = LocalDate.of(2026, 9, 24)
        assertEquals(0, calculateStreakDays(start, end, LocalDate.of(2026, 9, 26)))
    }

    @Test
    fun `invalid range where end is before start returns zero`() {
        val start = LocalDate.of(2026, 9, 22)
        val end = LocalDate.of(2026, 9, 20)
        assertEquals(0, calculateStreakDays(start, end, LocalDate.of(2026, 9, 26)))
    }
}