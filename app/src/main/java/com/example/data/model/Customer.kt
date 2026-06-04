package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey val contactPhone: String, // Serving as key for matching Sync
    val name: String,
    val idCard: String,
    val address: String,
    val cmId: String,                    // Customer Unique ID, e.g., "CM-1001"
    val firstBookingDate: String,
    val lastBookingDate: String,
    val totalStayCount: Int = 1,
    val totalSpend: Double = 0.0
)
