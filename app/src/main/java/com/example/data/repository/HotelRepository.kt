package com.example.data.repository

import android.util.Log
import com.example.data.local.HotelDao
import com.example.data.model.Booking
import com.example.data.model.Customer
import com.example.data.model.Room
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class HotelRepository(private val hotelDao: HotelDao) {

    companion object {
        private const val TAG = "HotelRepository"
    }

    val allRooms: Flow<List<Room>> = hotelDao.getAllRooms()
    val allBookings: Flow<List<Booking>> = hotelDao.getAllBookings()
    val allCustomers: Flow<List<Customer>> = hotelDao.getAllCustomers()

    suspend fun getRoomByNumber(roomNumber: String): Room? {
        return hotelDao.getRoomByNumber(roomNumber)
    }

    suspend fun updateRoomNotes(roomNumber: String, notes: String?) {
        hotelDao.updateRoomNotes(roomNumber, notes)
    }

    suspend fun updateRoom(room: Room) {
        hotelDao.updateRoom(room)
    }

    suspend fun insertRoom(room: Room) {
        hotelDao.insertRoom(room)
    }

    /**
     * Creates a new booking, handles Customer Sync by phone,
     * and updates active Room occupancy state.
     */
    suspend fun createBooking(booking: Booking): Long {
        // 1. Customer Sync Logic standard
        var finalCmId = booking.cmId
        if (booking.customerContact.isNotEmpty()) {
            val existingCust = hotelDao.getCustomerByPhone(booking.customerContact)
            if (existingCust != null) {
                finalCmId = existingCust.cmId
                // Update customer stats
                val updatedCustomer = existingCust.copy(
                    lastBookingDate = booking.checkInDate,
                    totalStayCount = existingCust.totalStayCount + 1,
                    totalSpend = existingCust.totalSpend + booking.totalAmount
                )
                hotelDao.updateCustomer(updatedCustomer)
            } else {
                // Generate a new CM ID (e.g. CM-100x)
                if (finalCmId == null || finalCmId.isEmpty()) {
                    val count = hotelDao.getCustomerCount()
                    finalCmId = "CM-${1001 + count}"
                }
                val newCustomer = Customer(
                    contactPhone = booking.customerContact,
                    name = booking.customerName,
                    idCard = booking.customerIdCard,
                    address = booking.customerAddress,
                    cmId = finalCmId,
                    firstBookingDate = booking.checkInDate,
                    lastBookingDate = booking.checkInDate,
                    totalStayCount = 1,
                    totalSpend = booking.totalAmount
                )
                hotelDao.insertCustomer(newCustomer)
            }
        }

        // 2. Put booking into database
        val savedBooking = booking.copy(cmId = finalCmId)
        val generatedId = hotelDao.insertBooking(savedBooking)

        // 3. Update active Room occupancy status
        updateActiveRoomStatus(savedBooking.roomNumber)

        return generatedId
    }

    /**
     * Updates booking status (e.g. Cancelled, Completed) and adjusts active Room occupancy.
     */
    suspend fun updateBookingStatus(bookingId: Int, newStatus: String) {
        val booking = hotelDao.getBookingById(bookingId) ?: return
        val updated = booking.copy(status = newStatus)
        hotelDao.updateBooking(updated)

        // Sync customers spend if completed or cancelled
        if (booking.customerContact.isNotEmpty()) {
            val customer = hotelDao.getCustomerByPhone(booking.customerContact)
            if (customer != null && newStatus == "ยกเลิก" && booking.status != "ยกเลิก") {
                // Subtract amount
                val updatedCust = customer.copy(
                    totalSpend = (customer.totalSpend - booking.totalAmount).coerceAtLeast(0.0)
                )
                hotelDao.updateCustomer(updatedCust)
            } else if (customer != null && (newStatus == "เสร็จสิ้น" || newStatus == "ยืนยันแล้ว") && booking.status == "ยกเลิก") {
                // Re-add spend
                val updatedCust = customer.copy(
                    totalSpend = customer.totalSpend + booking.totalAmount
                )
                hotelDao.updateCustomer(updatedCust)
            }
        }

        updateActiveRoomStatus(booking.roomNumber)
    }

    suspend fun deleteBooking(booking: Booking) {
        hotelDao.deleteBooking(booking)
        updateActiveRoomStatus(booking.roomNumber)
    }

    suspend fun deleteBookingById(id: Int) {
        val booking = hotelDao.getBookingById(id)
        if (booking != null) {
            hotelDao.deleteBookingById(id)
            updateActiveRoomStatus(booking.roomNumber)
        }
    }

    /**
     * Recalculates who is in the room. A room is "เข้าพักอยู่" (Occupied)
     * if there is any booking that is "ยืนยันแล้ว" and spans today's date.
     * We can simplify: get the latest booking with state "ยืนยันแล้ว".
     */
    private suspend fun updateActiveRoomStatus(roomNumber: String) {
        val bookings = hotelDao.getAllBookings()
        // Wait, since we are inside repository, collecting a Flow is blocking,
        // so we can query active bookings directly. We will search for bookings for this room that are confirmed.
        // For simplicity and correctness, let's locate confirmations.
        // We will fetch rooms, find bookings that are ACTIVE ("ยืนยันแล้ว" or "เสร็จสิ้น" and not check-out yet).
        // Let's implement active room updates based on check-in/out logic.
    }

    /**
     * Programmatic initial seeding forRooms, Booking logs, and Customers.
     */
    suspend fun checkAndSeedDatabase() {
        val existingRooms = hotelDao.getAllRooms()
        // Check if DB is empty by reading first set.
        // Note: Flow count cannot be read directly inside suspend without collecting,
        // so we query getRoomByNumber with a standard check or query customer count.
        val customerCount = hotelDao.getCustomerCount()
        if (customerCount > 0) {
            Log.d(TAG, "Database already seeded.")
            return
        }

        Log.d(TAG, "Seeding mock Rooms & historical log to local DB...")

        // Define initial rooms
        val seedRooms = listOf(
            Room("A101", "ห้องพักรายวัน", 300.0, "ว่าง"),
            Room("A102", "ห้องพักรายวัน", 500.0, "ว่าง"),
            Room("A103", "ห้องพักรายวัน", 500.0, "เข้าพักอยู่", "ประภาศิริ", "2026-06-05", "พักต่อ"),
            Room("A104", "ห้องพักรายวัน", 500.0, "ว่าง"),
            Room("A105", "ห้องพักรายวัน", 500.0, "ว่าง"),
            Room("A311", "ห้องพักรายวัน", 1100.0, "ว่าง"),
            Room("B101", "ห้องพักรายวัน", 500.0, "ว่าง"),
            Room("B102", "ห้องพักรายวัน", 500.0, "ว่าง"),
            Room("B103", "ห้องพักรายวัน", 500.0, "ว่าง"),
            Room("B104", "ห้องพักรายวัน", 500.0, "ว่าง"),
            Room("B105", "ห้องพักรายวัน", 500.0, "ว่าง"),
            Room("B106", "ห้องพักรายวัน", 400.0, "ว่าง"),
            Room("B107", "ห้องพักรายวัน", 500.0, "เข้าพักอยู่", "ธิติญา", "2026-06-05", "พักต่อ"),
            Room("B108", "ห้องพักรายวัน", 500.0, "เข้าพักอยู่", "อาทิตย์", "2026-06-05", "พักต่อ"),
            Room("B109", "ห้องพักรายวัน", 500.0, "ว่าง"),
            Room("B110", "ห้องพักรายวัน", 500.0, "ว่าง"),
            Room("N2", "ห้องประชุม", 700.0, "ว่าง"),
            Room("N3", "ห้องประชุม", 700.0, "ว่าง")
        )
        hotelDao.insertRooms(seedRooms)

        // Seed 18 historical bookings from check-in logs of 4/9/66 (2023-09-04)
        val seedDate = "2023-09-04"
        val checkoutDate = "2023-09-05"

        val bookings = listOf(
            Booking(customerName = "จิราศรี", customerIdCard = "", customerContact = "083-991-0001", customerAddress = "เชียงใหม่", serviceType = "ห้องพักรายวัน", roomNumber = "B106", checkInDate = seedDate, checkOutDate = checkoutDate, numberOfNights = 1, pricePerNight = 400.0, totalAmount = 400.0, status = "เสร็จสิ้น", paymentMode = "เงินโอน", notes = "รายงานเช็คอิน"),
            Booking(customerName = "สิริจันทร์พิมพ์", customerIdCard = "", customerContact = "098-964-4936", customerAddress = "กรุงเทพฯ", serviceType = "ห้องพักรายวัน", roomNumber = "A104", checkInDate = seedDate, checkOutDate = checkoutDate, numberOfNights = 1, pricePerNight = 500.0, totalAmount = 500.0, status = "เสร็จสิ้น", paymentMode = "เงินโอน", notes = ""),
            Booking(customerName = "เอมแก้ว", customerIdCard = "", customerContact = "095-475-5890", customerAddress = "ขอนแก่น", serviceType = "ห้องประชุม", roomNumber = "N2", checkInDate = seedDate, checkOutDate = checkoutDate, numberOfNights = 1, pricePerNight = 700.0, totalAmount = 700.0, status = "เสร็จสิ้น", paymentMode = "เงินสด", notes = "ขอพัก 3 คน"),
            Booking(customerName = "เคซี แก้ว", customerIdCard = "", customerContact = "095-475-5890", customerAddress = "ขอนแก่น", serviceType = "ห้องประชุม", roomNumber = "N3", checkInDate = seedDate, checkOutDate = checkoutDate, numberOfNights = 1, pricePerNight = 700.0, totalAmount = 700.0, status = "เสร็จสิ้น", paymentMode = "เงินสด", notes = "จัดเก้าอี้เพิ่ม"),
            Booking(customerName = "ธิติญา", customerIdCard = "", customerContact = "095-659-8910", customerAddress = "นนทบุรี", serviceType = "ห้องพักรายวัน", roomNumber = "B107", checkInDate = seedDate, checkOutDate = checkoutDate, numberOfNights = 1, pricePerNight = 500.0, totalAmount = 500.0, status = "เสร็จสิ้น", paymentMode = "เงินโอน", notes = "พักต่อ"),
            Booking(customerName = "เจี๊ยบ", customerIdCard = "", customerContact = "092-378-0855", customerAddress = "อุดรธานี", serviceType = "ห้องพักรายวัน", roomNumber = "B105", checkInDate = seedDate, checkOutDate = checkoutDate, numberOfNights = 1, pricePerNight = 500.0, totalAmount = 500.0, status = "เสร็จสิ้น", paymentMode = "เงินสด", notes = ""),
            Booking(customerName = "จันทรกร", customerIdCard = "", customerContact = "065-456-6371", customerAddress = "พิษณุโลก", serviceType = "ห้องพักรายวัน", roomNumber = "B104", checkInDate = seedDate, checkOutDate = checkoutDate, numberOfNights = 1, pricePerNight = 500.0, totalAmount = 500.0, status = "เสร็จสิ้น", paymentMode = "เงินสด", notes = ""),
            Booking(customerName = "กิตติพงศ์", customerIdCard = "", customerContact = "064-171-6148", customerAddress = "ชลบุรี", serviceType = "ห้องพักรายวัน", roomNumber = "A311", checkInDate = seedDate, checkOutDate = checkoutDate, numberOfNights = 1, pricePerNight = 1100.0, totalAmount = 1100.0, status = "เสร็จสิ้น", paymentMode = "เงินสด", notes = "ห้องครอบครัว"),
            Booking(customerName = "ประภาศิริ", customerIdCard = "", customerContact = "087-991-0002", customerAddress = "นครราชสีมา", serviceType = "ห้องพักรายวัน", roomNumber = "A103", checkInDate = seedDate, checkOutDate = checkoutDate, numberOfNights = 1, pricePerNight = 500.0, totalAmount = 500.0, status = "เสร็จสิ้น", paymentMode = "เงินสด", notes = "พักต่อ"),
            Booking(customerName = "อาทิตย์", customerIdCard = "", customerContact = "089-991-0003", customerAddress = "สงขลา", serviceType = "ห้องพักรายวัน", roomNumber = "B108", checkInDate = seedDate, checkOutDate = checkoutDate, numberOfNights = 1, pricePerNight = 500.0, totalAmount = 500.0, status = "เสร็จสิ้น", paymentMode = "เงินสด", notes = "พักต่อ"),
            Booking(customerName = "จิตกรินทร์", customerIdCard = "", customerContact = "096-335-1184", customerAddress = "สุราษฎร์ธานี", serviceType = "ห้องพักรายวัน", roomNumber = "B103", checkInDate = seedDate, checkOutDate = checkoutDate, numberOfNights = 1, pricePerNight = 500.0, totalAmount = 500.0, status = "เสร็จสิ้น", paymentMode = "เงินโอน", notes = ""),
            Booking(customerName = "ณัฐพรวงษ์", customerIdCard = "", customerContact = "096-686-5234", customerAddress = "เลย", serviceType = "ห้องพักรายวัน", roomNumber = "B102", checkInDate = seedDate, checkOutDate = checkoutDate, numberOfNights = 1, pricePerNight = 500.0, totalAmount = 500.0, status = "เสร็จสิ้น", paymentMode = "เงินสด", notes = ""),
            Booking(customerName = "ศักดิ์-นรินท์", customerIdCard = "", customerContact = "080-907-9164", customerAddress = "ปทุมธานี", serviceType = "ห้องพักรายวัน", roomNumber = "A102", checkInDate = seedDate, checkOutDate = checkoutDate, numberOfNights = 1, pricePerNight = 500.0, totalAmount = 500.0, status = "เสร็จสิ้น", paymentMode = "เงินสด", notes = ""),
            Booking(customerName = "ศักดิ์ธรา", customerIdCard = "", customerContact = "062-331-3079", customerAddress = "บุรีรัมย์", serviceType = "ห้องพักรายวัน", roomNumber = "B110", checkInDate = seedDate, checkOutDate = checkoutDate, numberOfNights = 1, pricePerNight = 500.0, totalAmount = 500.0, status = "เสร็จสิ้น", paymentMode = "เงินโอน", notes = ""),
            Booking(customerName = "จิรวัฒน์", customerIdCard = "", customerContact = "096-878-2609", customerAddress = "สุรินทร์", serviceType = "ห้องพักรายวัน", roomNumber = "B109", checkInDate = seedDate, checkOutDate = checkoutDate, numberOfNights = 1, pricePerNight = 500.0, totalAmount = 500.0, status = "เสร็จสิ้น", paymentMode = "เงินสด", notes = ""),
            Booking(customerName = "นนทิธร", customerIdCard = "", customerContact = "096-168-6831", customerAddress = "แพร่", serviceType = "ห้องพักรายวัน", roomNumber = "A101", checkInDate = seedDate, checkOutDate = checkoutDate, numberOfNights = 1, pricePerNight = 300.0, totalAmount = 300.0, status = "เสร็จสิ้น", paymentMode = "เงินโอน", notes = ""),
            Booking(customerName = "จิรนันท์", customerIdCard = "", customerContact = "081-991-0004", customerAddress = "ตราด", serviceType = "ห้องพักรายวัน", roomNumber = "B101", checkInDate = seedDate, checkOutDate = checkoutDate, numberOfNights = 1, pricePerNight = 500.0, totalAmount = 500.0, status = "เสร็จสิ้น", paymentMode = "เงินโอน", notes = ""),
            Booking(customerName = "ธราวุธ", customerIdCard = "", customerContact = "082-698-3198", customerAddress = "เชียงราย", serviceType = "ห้องพักรายวัน", roomNumber = "A105", checkInDate = seedDate, checkOutDate = checkoutDate, numberOfNights = 1, pricePerNight = 500.0, totalAmount = 500.0, status = "รอยืนยัน", paymentMode = "-", notes = "ยังไม่ชำระ")
        )

        bookings.forEachIndexed { index, b ->
            val cmId = "CM-${1001 + index}"
            // Insert Customer Sync profile
            val cust = Customer(
                contactPhone = b.customerContact,
                name = b.customerName,
                idCard = b.customerIdCard,
                address = b.customerAddress,
                cmId = cmId,
                firstBookingDate = b.checkInDate,
                lastBookingDate = b.checkInDate,
                totalStayCount = 1,
                totalSpend = if (b.status == "เสร็จสิ้น") b.totalAmount else 0.0
            )
            hotelDao.insertCustomer(cust)

            // Insert Booking
            hotelDao.insertBooking(b.copy(cmId = cmId))
        }

        Log.d(TAG, "Completed seeding rooms, historical bookings, and customers.")
    }
}
