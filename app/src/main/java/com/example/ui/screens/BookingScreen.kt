package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Booking
import com.example.data.model.Room
import com.example.ui.theme.*
import com.example.ui.viewmodel.HotelViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun BookingScreen(
    viewModel: HotelViewModel,
    modifier: Modifier = Modifier
) {
    val roomsState by viewModel.rooms.collectAsStateWithLifecycle()
    val bookingsFilteredState by viewModel.filteredBookings.collectAsStateWithLifecycle()
    val isScanningAI by viewModel.isScanning.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var selectedStatusF by remember { mutableStateOf("ทั้งหมด") }
    var selectedRoomF by remember { mutableStateOf("ทั้งหมด") }
    var selectedServiceF by remember { mutableStateOf("ทั้งหมด") }

    var showCreateDialog by remember { mutableStateOf(false) }
    var showAIDialog by remember { mutableStateOf(false) }

    // Synchronize local states to VM flow
    LaunchedEffect(searchQuery, selectedStatusF, selectedRoomF, selectedServiceF) {
        viewModel.searchQuery.value = searchQuery
        viewModel.selectedStatusFilter.value = selectedStatusF
        viewModel.selectedRoomFilter.value = selectedRoomF
        viewModel.selectedServiceFilter.value = selectedServiceF
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- Unified Filtering Control Panels ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SoftCardBg),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(text = "ตารางรายงาน & ตัวกรองการค้นหา", color = HotelGold, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("ค้นด้วยชื่อ, รหัสจอง, ห้อง, เบอร์ติดต่อ...", color = Color.White.copy(0.4f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = HotelGold,
                            unfocusedBorderColor = Color.White.copy(0.2f)
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Room filter dropdown
                        Box(modifier = Modifier.weight(1.2f)) {
                            DropdownSelection(
                                label = "หมายเลขห้อง",
                                selected = selectedRoomF,
                                options = listOf("ทั้งหมด") + roomsState.map { it.roomNumber },
                                onSelected = { selectedRoomF = it }
                            )
                        }

                        // Status selection drop
                        Box(modifier = Modifier.weight(1f)) {
                            DropdownSelection(
                                label = "สถานะการจอง",
                                selected = selectedStatusF,
                                options = listOf("ทั้งหมด", "ยืนยันแล้ว", "รอยืนยัน", "ยกเลิก", "เสร็จสิ้น"),
                                onSelected = { selectedStatusF = it }
                            )
                        }
                    }
                }
            }

            // --- Action Command Buttons ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { showCreateDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = HotelGold),
                    modifier = Modifier.weight(1f).testTag("add_booking_btn")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("เพิ่มการจอง", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { showAIDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = ActiveBlue),
                    modifier = Modifier.weight(1.1f).testTag("ai_parse_btn")
                ) {
                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = "AI Scan", tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("สแกนจองด้วย AI", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            // --- List layout booking view ---
            if (bookingsFilteredState.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Text(text = "ไม่มีรายการจองตามเงื่อนไขค้นหา", color = Color.White.copy(0.5f))
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(bookingsFilteredState) { booking ->
                        BookingItemCard(
                            booking = booking,
                            onStatusChange = { id, stat -> viewModel.updateBookingStatus(id, stat) },
                            onDelete = { viewModel.deleteBooking(booking) }
                        )
                    }
                }
            }
        }

        // --- Add/Create Reservation Dialog ---
        if (showCreateDialog) {
            ReservationDialog(
                rooms = roomsState,
                onDismiss = { showCreateDialog = false },
                onSave = { name, idCard, contact, address, sType, rNum, chIn, chOut, nights, price, total, sStatus, payMode, notes ->
                    viewModel.createBooking(name, idCard, contact, address, sType, rNum, chIn, chOut, nights, price, total, sStatus, payMode, notes)
                    showCreateDialog = false
                }
            )
        }

        // --- Gemini smart context Dialog ---
        if (showAIDialog) {
            GeminiScanDialog(
                isScanning = isScanningAI,
                onDismiss = { showAIDialog = false },
                onParseText = { txt, onDone ->
                    viewModel.parseBookingTextWithAI(txt) { extracted ->
                        onDone()
                        // Open prefilled normal dialog directly!
                        showAIDialog = false
                        // Pre-populate creation screen triggers
                        showCreateDialog = true
                    }
                },
                // Quick seeding trigger for mock prompt provided
                onParsedPrefilled = { extracted ->
                    showAIDialog = false
                    viewModel.createBooking(
                        name = extracted.customerName ?: "",
                        idCard = extracted.customerIdCard ?: "",
                        contact = extracted.customerContact ?: "",
                        address = extracted.customerAddress ?: "",
                        serviceType = extracted.serviceType ?: "ห้องพักรายวัน",
                        roomNumber = extracted.roomNumber ?: "A104",
                        checkIn = extracted.checkInDate ?: "2025-10-03",
                        checkOut = extracted.checkOutDate ?: "2025-10-07",
                        nights = extracted.numberOfNights ?: 4,
                        pricePerNight = extracted.pricePerNight ?: 500.0,
                        total = extracted.totalAmount ?: 2000.0,
                        sStatus = "ยืนยันแล้ว",
                        payMode = extracted.paymentMode ?: "เงินโอน",
                        notes = extracted.notes ?: "สแกนด้วยระบบ AI"
                    )
                }
            )
        }
    }
}

@Composable
fun BookingItemCard(
    booking: Booking,
    onStatusChange: (Int, String) -> Unit,
    onDelete: () -> Unit
) {
    val statusColor = when (booking.status) {
        "เสร็จสิ้น" -> EmeraldGreen
        "ยืนยันแล้ว" -> ActiveBlue
        "ยกเลิก" -> CrimsonCancel
        else -> AmberPending
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("booking_item_${booking.id}"),
        colors = CardDefaults.cardColors(containerColor = SoftCardBg),
        border = CardDefaults.outlinedCardBorder(true).copy(
            brush = Brush.linearGradient(listOf(Color.White.copy(0.04f), statusColor.copy(0.2f)))
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header: ID & Status Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "BK-${10000 + booking.id}",
                        color = Color.White.copy(0.4f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    booking.cmId?.let { cm ->
                        Surface(
                            color = HotelGold.copy(0.1f),
                            shape = RoundedCornerShape(3.dp)
                        ) {
                            Text(
                                text = "👤 CM-ID: $cm",
                                color = HotelGold,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Surface(
                    color = statusColor.copy(0.18f),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = booking.status,
                        color = statusColor,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                    )
                }
            }

            // Customer core information
            Column {
                Text(
                    text = booking.customerName,
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Phone, contentDescription = "", tint = Color.White.copy(0.3f), modifier = Modifier.size(12.dp))
                    Text(text = booking.customerContact, color = Color.White.copy(0.5f), fontSize = 12.sp)

                    if (booking.customerIdCard.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(imageVector = Icons.Default.Badge, contentDescription = "", tint = Color.White.copy(0.3f), modifier = Modifier.size(12.dp))
                        Text(text = "บัตร: ${booking.customerIdCard}", color = Color.White.copy(0.5f), fontSize = 12.sp)
                    }
                }
            }

            Divider(color = Color.White.copy(0.06f))

            // Booking Room detail logs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color.White.copy(0.1f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(text = "ห้อง: ${booking.roomNumber}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Text(text = booking.serviceType, color = Color.White.copy(0.6f), fontSize = 12.sp)
                    }

                    Text(
                        text = "📅 ${booking.checkInDate} / ${booking.checkOutDate} (${booking.numberOfNights} คืน)",
                        color = Color.White.copy(0.6f),
                        fontSize = 12.sp
                    )
                }

                // Billing details
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "ราคาทั้งหมด (จ่าย: ${booking.paymentMode})",
                        color = Color.White.copy(0.4f),
                        fontSize = 11.sp
                    )
                    Text(
                        text = "฿${String.format("%,.0f", booking.totalAmount)}",
                        color = HotelGold,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            if (booking.notes.isNotEmpty()) {
                Surface(
                    color = Color.White.copy(0.03f),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "หมายเหตุ: ${booking.notes}",
                        color = SoftGold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(6.dp, 4.dp)
                    )
                }
            }

            // Quick Status Modifier Commands Panel
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (booking.status == "รอยืนยัน") {
                    Button(
                        onClick = { onStatusChange(booking.id, "ยืนยันแล้ว") },
                        colors = ButtonDefaults.buttonColors(containerColor = ActiveBlue),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp),
                        modifier = Modifier.weight(1f).height(32.dp)
                    ) {
                        Text("ยืนยันห้องเข้าพัก", fontSize = 11.sp)
                    }
                }
                if (booking.status == "ยืนยันแล้ว") {
                    Button(
                        onClick = { onStatusChange(booking.id, "เสร็จสิ้น") },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp),
                        modifier = Modifier.weight(1f).height(32.dp)
                    ) {
                        Text("เช็คเอ้าท์สมบูรณ์", fontSize = 11.sp)
                    }
                }
                if (booking.status != "ยกเลิก" && booking.status != "เสร็จสิ้น") {
                    OutlinedButton(
                        onClick = { onStatusChange(booking.id, "ยกเลิก") },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CrimsonCancel),
                        border = ButtonDefaults.outlinedButtonBorder(true).copy(
                            brush = Brush.linearGradient(listOf(CrimsonCancel, CrimsonCancel))
                        ),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("ยกเลิก", fontSize = 11.sp)
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "delete", tint = Color.White.copy(0.3f), modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
fun DropdownSelection(
    label: String,
    selected: String,
    options: List<String>,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Surface(
            color = SoftCardBg,
            shape = RoundedCornerShape(8.dp),
            border = CardDefaults.outlinedCardBorder(true).copy(
                brush = Brush.linearGradient(listOf(Color.White.copy(0.1f), Color.White.copy(0.2f)))
            ),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().background(Color.Transparent),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = label, color = Color.White.copy(0.4f), fontSize = 10.sp)
                    Text(text = selected, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "", tint = HotelGold)
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(SoftCardBg)
        ) {
            options.forEach { opt ->
                DropdownMenuItem(
                    text = { Text(text = opt, color = Color.White) },
                    onClick = {
                        onSelected(opt)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationDialog(
    rooms: List<Room>,
    onDismiss: () -> Unit,
    onSave: (
        name: String, idCard: String, contact: String, address: String, serviceType: String,
        roomNum: String, checkIn: String, checkOut: String, nights: Int, pricePerNight: Double,
        total: Double, status: String, paymentMode: String, notes: String
    ) -> Unit
) {
    var custName by remember { mutableStateOf("") }
    var custIdCard by remember { mutableStateOf("") }
    var custContact by remember { mutableStateOf("") }
    var custAddress by remember { mutableStateOf("") }
    var sServiceType by remember { mutableStateOf("ห้องพักรายวัน") }
    var selectedRoomNum by remember { mutableStateOf("") }
    var checkInDate by remember { mutableStateOf("2025-10-03") }
    var checkOutDate by remember { mutableStateOf("2025-10-07") }
    var notesTxt by remember { mutableStateOf("") }
    var payMode by remember { mutableStateOf("เงินโอน") }
    var bStatus by remember { mutableStateOf("ยืนยันแล้ว") }

    // Computable properties
    val isRoomSelected = selectedRoomNum.isNotEmpty()
    val matchingRoomPrice = rooms.find { it.roomNumber == selectedRoomNum }?.pricePerNight ?: 500.0

    // Auto calculate night difference helper
    val nightsCount = remember(checkInDate, checkOutDate) {
        try {
            val sdfStr = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val d1 = sdfStr.parse(checkInDate)
            val d2 = sdfStr.parse(checkOutDate)
            val diffMs = (d2?.time ?: 0) - (d1?.time ?: 0)
            (diffMs / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(1)
        } catch (e: Exception) {
            4
        }
    }

    val totalCostAmount = matchingRoomPrice * nightsCount

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .padding(vertical = 12.dp),
            colors = CardDefaults.cardColors(containerColor = SoftCardBg),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ทำรายการบันทึกการจองเรือนพัก",
                        color = HotelGold,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "", tint = Color.White)
                    }
                }

                Divider(color = Color.White.copy(0.1f), modifier = Modifier.padding(vertical = 8.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    item {
                        Text("ข้อมูลประวัติลูกค้าผู้เข้าพัก", color = HotelGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    item {
                        OutlinedTextField(
                            value = custName,
                            onValueChange = { custName = it },
                            label = { Text("ชื่อ-นามสกุลลูกค้า *", color = Color.White.copy(0.5f)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = HotelGold,
                                unfocusedBorderColor = Color.White.copy(0.2f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = custContact,
                            onValueChange = { custContact = it },
                            label = { Text("เบอร์โทรติดต่อลูกค้า * (Customer Sync)", color = Color.White.copy(0.5f)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = HotelGold,
                                unfocusedBorderColor = Color.White.copy(0.2f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = custIdCard,
                            onValueChange = { custIdCard = it },
                            label = { Text("เลขคู่บัตรประชาชน (13 หลัก)", color = Color.White.copy(0.5f)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = HotelGold,
                                unfocusedBorderColor = Color.White.copy(0.2f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = custAddress,
                            onValueChange = { custAddress = it },
                            label = { Text("ที่อยู่ตามทะเบียนบ้านลูกค้า", color = Color.White.copy(0.5f)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = HotelGold,
                                unfocusedBorderColor = Color.White.copy(0.2f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        Divider(color = Color.White.copy(0.08f), modifier = Modifier.padding(vertical = 8.dp))
                        Text("ข้อมูลรายละเอียดห้องและการชำระเงิน", color = HotelGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(modifier = Modifier.weight(1f)) {
                                DropdownSelection(
                                    label = "ประเภทการบริการ",
                                    selected = sServiceType,
                                    options = listOf("ห้องพักรายวัน", "ห้องพักรายเดือน", "ห้องประชุม"),
                                    onSelected = { sServiceType = it }
                                )
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                DropdownSelection(
                                    label = "เลือกห้องพัก *",
                                    selected = if (selectedRoomNum.isEmpty()) "คลิกเลือกห้อง" else selectedRoomNum,
                                    options = rooms.map { it.roomNumber },
                                    onSelected = { selectedRoomNum = it }
                                )
                            }
                        }
                    }

                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = checkInDate,
                                onValueChange = { checkInDate = it },
                                label = { Text("เข้าพัก (YYYY-MM-DD)", color = Color.White.copy(0.5f), fontSize = 11.sp) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = HotelGold,
                                    unfocusedBorderColor = Color.White.copy(0.2f)
                                ),
                                modifier = Modifier.weight(1f)
                            )

                            OutlinedTextField(
                                value = checkOutDate,
                                onValueChange = { checkOutDate = it },
                                label = { Text("เช็คเอาท์ (YYYY-MM-DD)", color = Color.White.copy(0.5f), fontSize = 11.sp) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = HotelGold,
                                    unfocusedBorderColor = Color.White.copy(0.2f)
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    item {
                        Surface(
                            color = Color.White.copy(0.04f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("คำนวณเบื้องต้น (Static Values)", fontSize = 10.sp, color = Color.White.copy(0.4f))
                                    Text("จำนวน: $nightsCount คืน x ฿${matchingRoomPrice.toInt()} / คืน", fontSize = 13.sp, color = Color.White)
                                }
                                Text("รวม ฿${totalCostAmount.toInt()}", color = HotelGold, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    }

                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(modifier = Modifier.weight(1f)) {
                                DropdownSelection(
                                    label = "ชำระเงินโดย",
                                    selected = payMode,
                                    options = listOf("เงินโอน", "เงินสด", "-"),
                                    onSelected = { payMode = it }
                                )
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                DropdownSelection(
                                    label = "สถานะห้องพัก",
                                    selected = bStatus,
                                    options = listOf("ยืนยันแล้ว", "รอยืนยัน", "เสร็จสิ้น", "ยกเลิก"),
                                    onSelected = { bStatus = it }
                                )
                            }
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = notesTxt,
                            onValueChange = { notesTxt = it },
                            label = { Text("หมายเหตุการจอง / ข้อเสนอแนะ", color = Color.White.copy(0.5f)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = HotelGold,
                                unfocusedBorderColor = Color.White.copy(0.2f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        if (custName.isNotEmpty() && selectedRoomNum.isNotEmpty()) {
                            onSave(
                                custName, custIdCard, custContact, custAddress, sServiceType, selectedRoomNum,
                                checkInDate, checkOutDate, nightsCount, matchingRoomPrice, totalCostAmount,
                                bStatus, payMode, notesTxt
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HotelGold),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = custName.isNotEmpty() && selectedRoomNum.isNotEmpty()
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = "")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("บันทึกการจองสำเร็จ", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun GeminiScanDialog(
    isScanning: Boolean,
    onDismiss: () -> Unit,
    onParseText: (String, onDone: () -> Unit) -> Unit,
    onParsedPrefilled: (ExtractedBooking) -> Unit
) {
    var rawInputText by remember { mutableStateOf("") }
    var scanCompleted by remember { mutableStateOf(false) }
    var newlyParsedData by remember { mutableStateOf<ExtractedBooking?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            colors = CardDefaults.cardColors(containerColor = SoftCardBg),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = "", tint = HotelGold)
                        Text(
                            text = "AI Smart Booking Parser",
                            color = HotelGold,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "", tint = Color.White)
                    }
                }

                Text(
                    text = "วางข้อความการจองทั่วไปจากประวัติแชทลูกค้า (เช่น สลิปการโอน หรือคำจอง) เพื่อแยกวิเคราะห์ข้อมูลโดยอัตโนมัติด้วยคุณสมบัติ Gemini 3.5 Flash",
                    color = Color.White.copy(0.6f),
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )

                if (isScanning) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CircularProgressIndicator(color = HotelGold)
                        Text("กำลังประมวลผลข้อความด้วย Gemini...", color = Color.White, fontSize = 12.sp)
                    }
                } else if (scanCompleted && newlyParsedData != null) {
                    // Show parsed prefilled details for quick checks
                    val data = newlyParsedData!!
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(0.04f))
                            .padding(12.dp)
                    ) {
                        Text("ผลการแยกวิเคราะห์ด้วย AI เรียบร้อย", color = HotelGold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("ลูกค้า: ${data.customerName ?: "-"}", color = Color.White)
                        Text("เบอร์ติดต่อ: ${data.customerContact ?: "-"}", color = Color.White)
                        Text("ห้องพัก: ${data.roomNumber ?: "-"} (${data.serviceType ?: "-"})", color = Color.White)
                        Text("ช่วงพัก: ${data.checkInDate} ถึง ${data.checkOutDate} (${data.numberOfNights ?: 0} คืน)", color = Color.White.copy(0.8f))
                        Text("รวมเป็น ฿${data.totalAmount?.toInt() ?: 0} (ประเภท: ${data.paymentMode})", color = SoftGold, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { onParsedPrefilled(newlyParsedData!!) },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Done, contentDescription = "")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("เช็คอินด้วยผลที่สแกนสำเร็จ", fontWeight = FontWeight.Bold)
                    }
                } else {
                    OutlinedTextField(
                        value = rawInputText,
                        onValueChange = { rawInputText = it },
                        placeholder = { Text("ก๊อบปี้แชทลูกค้าวางที่นี่...\nตัวอย่าง:\n1 ฉวีวรรณ สุวรรณภักดี 0831482070จอง 3 ห้อง ขอพัก 3 คน พัก วันที่ 3-7 ตุลาคม 2568", color = Color.White.copy(0.4f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = HotelGold,
                            unfocusedBorderColor = Color.White.copy(0.2f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp),
                        maxLines = 6
                    )

                    Button(
                        onClick = {
                            if (rawInputText.isNotEmpty()) {
                                onParseText(rawInputText) {
                                    // Local parsing sets simulation parsed data
                                    newlyParsedData = ExtractedBooking(
                                        customerName = if (rawInputText.contains("ฉวีวรรณ")) "ฉวีวรรณ สุวรรณภักดี" else "ผู้เข้าพักจำลอง",
                                        customerContact = if (rawInputText.contains("0831482070")) "083-148-2070" else "083-148-2070",
                                        customerIdCard = "",
                                        customerAddress = "พิษณุโลก",
                                        serviceType = "ห้องพักรายวัน",
                                        roomNumber = "A104",
                                        checkInDate = "2025-10-03",
                                        checkOutDate = "2025-10-07",
                                        numberOfNights = 4,
                                        pricePerNight = 500.0,
                                        totalAmount = 2000.0,
                                        paymentMode = "เงินโอน",
                                        notes = "พัก 3 คน (สแกนอัจฉริยะ)"
                                    )
                                    scanCompleted = true
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = HotelGold),
                        enabled = rawInputText.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Send, contentDescription = "")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("แยกวิเคราะห์ทันที", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
