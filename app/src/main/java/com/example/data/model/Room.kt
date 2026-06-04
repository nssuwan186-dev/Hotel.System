package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rooms")
data class Room(
    @PrimaryKey val roomNumber: String,  // e.g. "A101", "B102", "N2"
    val roomType: String,                // e.g. "ห้องพักรายวัน", "ห้องพักรายเดือน", "ห้องประชุม"
    val pricePerNight: Double,           // e.g. 500.0, 700.0, etc.
    val status: String,                  // "ว่าง" (Available), "เข้าพักอยู่" (Occupied), "ปรับปรุง" (Maintenance)
    val currentGuestName: String? = null,
    val checkOutDate: String? = null,    // "YYYY-MM-DD"
    val notes: String? = null
)
