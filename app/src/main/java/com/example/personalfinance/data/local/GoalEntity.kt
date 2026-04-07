package com.example.personalfinance.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "goal_config")
data class GoalEntity(
    @PrimaryKey val id: Int = 1,
    val monthlyTarget: Double
)
