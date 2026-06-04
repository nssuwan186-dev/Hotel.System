package com.example.data.local

import androidx.room.*
import com.example.data.model.Booking
import com.example.data.model.Customer
import com.example.data.model.Room
import kotlinx.coroutines.flow.Flow

@Dao
interface HotelDao {

    // --- Rooms ---
    @Query("SELECT * FROM rooms ORDER BY roomNumber ASC")
    fun getAllRooms(): Flow<List<Room>>

    @Query("SELECT * FROM rooms WHERE roomNumber = :roomNumber LIMIT 1")
    suspend fun getRoomByNumber(roomNumber: String): Room?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRooms(rooms: List<Room>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoom(room: Room)

    @Update
    suspend fun updateRoom(room: Room)

    @Query("UPDATE rooms SET status = :status, currentGuestName = :guestName, checkOutDate = :checkOutDate WHERE roomNumber = :roomNumber")
    suspend fun updateRoomOccupyStatus(roomNumber: String, status: String, guestName: String?, checkOutDate: String?)

    @Query("UPDATE rooms SET notes = :notes WHERE roomNumber = :roomNumber")
    suspend fun updateRoomNotes(roomNumber: String, notes: String?)


    // --- Bookings ---
    @Query("SELECT * FROM bookings ORDER BY timestamp DESC")
    fun getAllBookings(): Flow<List<Booking>>

    @Query("SELECT * FROM bookings WHERE id = :id LIMIT 1")
    suspend fun getBookingById(id: Int): Booking?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooking(booking: Booking): Long

    @Update
    suspend fun updateBooking(booking: Booking)

    @Delete
    suspend fun deleteBooking(booking: Booking)

    @Query("DELETE FROM bookings WHERE id = :id")
    suspend fun deleteBookingById(id: Int)


    // --- Customers ---
    @Query("SELECT * FROM customers ORDER BY cmId DESC")
    fun getAllCustomers(): Flow<List<Customer>>

    @Query("SELECT * FROM customers WHERE contactPhone = :phone LIMIT 1")
    suspend fun getCustomerByPhone(phone: String): Customer?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: Customer)

    @Update
    suspend fun updateCustomer(customer: Customer)

    @Query("SELECT COUNT(*) FROM customers")
    suspend fun getCustomerCount(): Int
}
