package com.example.personalfinance.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "auth")
data class AuthEntity(
    @PrimaryKey val id: Int = 1,
    val email: String,
    val password: String
)
