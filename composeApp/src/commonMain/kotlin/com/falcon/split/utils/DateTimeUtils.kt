package com.falcon.split.util

import kotlinx.datetime.*

object DateTimeUtil {
    // Get current date in local timezone
    fun now(): LocalDateTime {
        return Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    }

    // Convert timestamp to LocalDateTime
    fun timestampToLocalDateTime(timestamp: Long): LocalDateTime {
        return Instant.fromEpochMilliseconds(timestamp).toLocalDateTime(TimeZone.currentSystemDefault())
    }

    // Format as relative date (Today, Yesterday, etc.)
    fun formatRelativeDate(timestamp: Long): String {
        val date = timestampToLocalDateTime(timestamp).date
        val today = now().date

        return when {
            date == today -> "Today"
            date == today.minus(DatePeriod(days = 1)) -> "Yesterday"
            isWithinDays(date, today, 7) -> "This Week"
            isWithinDays(date, today, 30) -> "This Month"
            isWithinCurrentYear(date, today) -> formatDateWithinYear(date)
            else -> formatFullDate(date)
        }
    }

    // Format as complete date without year if in current year
    fun formatStandardDate(timestamp: Long): String {
        val date = timestampToLocalDateTime(timestamp).date
        val today = now().date

        return if (isWithinCurrentYear(date, today)) {
            formatDateWithinYear(date)
        } else {
            formatFullDate(date)
        }
    }

    // Format as standard time (HH:MM AM/PM)
    fun formatTime(timestamp: Long): String {
        val ldt = timestampToLocalDateTime(timestamp)
        val hour = when {
            ldt.hour == 0 -> 12
            ldt.hour > 12 -> ldt.hour - 12
            else -> ldt.hour
        }
        val amPm = if (ldt.hour < 12) "AM" else "PM"
        return "$hour:${ldt.minute.toString().padStart(2, '0')} $amPm"
    }

    // Format as combined date and time
    fun formatDateTime(timestamp: Long): String {
        return "${formatStandardDate(timestamp)} at ${formatTime(timestamp)}"
    }

    // Helper to check if date is within X days of another date
    private fun isWithinDays(date: LocalDate, referenceDate: LocalDate, days: Int): Boolean {
        val diff = referenceDate.toEpochDays() - date.toEpochDays()
        return diff in 0..days
    }

    // Helper to check if date is in the same year
    private fun isWithinCurrentYear(date: LocalDate, referenceDate: LocalDate): Boolean {
        return date.year == referenceDate.year
    }

    // Format date without year (e.g., "Mar 15")
    private fun formatDateWithinYear(date: LocalDate): String {
        val month = when (date.monthNumber) {
            1 -> "Jan"
            2 -> "Feb"
            3 -> "Mar"
            4 -> "Apr"
            5 -> "May"
            6 -> "Jun"
            7 -> "Jul"
            8 -> "Aug"
            9 -> "Sep"
            10 -> "Oct"
            11 -> "Nov"
            12 -> "Dec"
            else -> "Unknown"
        }
        return "$month ${date.dayOfMonth}"
    }

    // Format full date with year (e.g., "Mar 15, 2023")
    private fun formatFullDate(date: LocalDate): String {
        return "${formatDateWithinYear(date)}, ${date.year}"
    }
}