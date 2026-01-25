package com.example.myapplication.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class WorkoutSession(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val sportId: String = "",
    val sportName: String = "",
    val date: Timestamp = Timestamp.now(),
    val duration: Int = 0, // в минутах
    val distance: Double = 0.0, // в км, для бега/маршрутов
    val caloriesBurned: Int = 0,
    val notes: String = "",
    val emotion: String = "", // "happy", "sad", "neutral"
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
)