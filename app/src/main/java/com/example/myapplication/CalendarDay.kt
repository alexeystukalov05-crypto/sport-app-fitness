package com.example.myapplication.data

import java.text.SimpleDateFormat
import java.util.*

data class CalendarDay(
    val date: String, // формат "yyyy-MM-dd"
    val workoutSessions: List<WorkoutSession> = emptyList(),
    val totalDuration: Int = 0,
    val emotions: List<String> = emptyList()
) {
    companion object {
        fun fromDate(date: Date): String {
            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            return formatter.format(date)
        }

        fun fromTimestamp(timestamp: com.google.firebase.Timestamp): String {
            return fromDate(timestamp.toDate())
        }
    }
}