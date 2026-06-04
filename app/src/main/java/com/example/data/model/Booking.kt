package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookings")
data class Booking(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val customerName: String,
    val customerIdCard: String,
    val customerContact: String,
    val customerAddress: String,
    val serviceType: String,       // e.g. "ห้องพักรายวัน", "ห้องพักรายเดือน", "ห้องประชุม"
    val roomNumber: String,
    val checkInDate: String,       // "YYYY-MM-DD"
    val checkOutDate: String,      // "YYYY-MM-DD"
    val numberOfNights: Int,
    val pricePerNight: Double,
    val totalAmount: Double,
    val status: String,            // "รอยืนยัน", "ยืนยันแล้ว", "ยกเลิก", "เสร็จสิ้น"
    val paymentMode: String,       // "เงินโอน", "เงินสด", "-"
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val cmId: String? = null       // Synced Customer ID
)
