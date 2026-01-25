package com.example.myapplication.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sports")
data class Sport(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String,
    val iconRes: String, // название ресурса иконки
    val category: String, // "cardio", "strength", "flexibility"
    val isActive: Boolean = true
)