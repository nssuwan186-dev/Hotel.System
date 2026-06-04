package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Room
import com.example.ui.theme.*
import com.example.ui.viewmodel.HotelViewModel

@Composable
fun RoomsScreen(
    viewModel: HotelViewModel,
    modifier: Modifier = Modifier
) {
    val roomsState by viewModel.rooms.collectAsStateWithLifecycle()
    var selectedRoomForEdit by remember { mutableStateOf<Room?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- Legend Row ---
        RoomLegendRow(roomsState)

        // --- Visual Room Grid ---
        if (roomsState.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = HotelGold)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 100.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(roomsState) { room ->
                    RoomVisualCard(
                        room = room,
                        onClick = { selectedRoomForEdit = room }
                    )
                }
            }
        }
    }

    // --- Quick Room Detail / Management Dialog ---
    selectedRoomForEdit?.let { room ->
        RoomManagementDialog(
            room = room,
            onDismiss = { selectedRoomForEdit = null },
            onUpdateNotes = { notes ->
                viewModel.updateRoomNotes(room.roomNumber, notes)
                selectedRoomForEdit = null
            },
            onQuickCheckout = {
                viewModel.makeRoomAvailable(room.roomNumber)
                selectedRoomForEdit = null
            },
            onQuickOccupied = { guest, checkoutDate, notes ->
                viewModel.makeRoomQuickStay(room.roomNumber, guest, checkoutDate, notes)
                selectedRoomForEdit = null
            }
        )
    }
}

@Composable
fun RoomLegendRow(rooms: List<Room>) {
    val vacant = rooms.count { it.status == "ว่าง" }
    val occupied = rooms.count { it.status == "เข้าพักอยู่" }
    val repair = rooms.count { it.status == "ปรับปรุง" }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SoftCardBg)
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        LegendItem("ว่าง ($vacant)", EmeraldGreen)
        LegendItem("เข้าพักอยู่ ($occupied)", AmberPending)
        LegendItem("ปรับปรุง ($repair)", CrimsonCancel)
    }
}

@Composable
fun LegendItem(label: String, color: Color) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(color)
        )
        Text(text = label, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun RoomVisualCard(
    room: Room,
    onClick: () -> Unit
) {
    val statusColor = when (room.status) {
        "เข้าพักอยู่" -> AmberPending
        "ปรับปรุง" -> CrimsonCancel
        else -> EmeraldGreen
    }

    Card(
        modifier = Modifier
            .aspectRatio(1.1f)
            .testTag("room_card_${room.roomNumber}")
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = SoftCardBg),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder(true).copy(
            brush = Brush.linearGradient(listOf(Color.White.copy(0.04f), statusColor.copy(0.35f)))
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Room Type abbreviation
                Text(
                    text = when (room.roomType) {
                        "ห้องประชุม" -> "CONF"
                        "ห้องพักรายเดือน" -> "MTH"
                        else -> "DLY"
                    },
                    color = Color.White.copy(0.4f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                // Small indicator dot
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(statusColor)
                )
            }

            Text(
                text = room.roomNumber,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color.White
                )
            )

            if (room.currentGuestName != null) {
                Text(
                    text = room.currentGuestName,
                    color = statusColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Text(
                    text = "฿${room.pricePerNight.toInt()}",
                    color = HotelGold,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun RoomManagementDialog(
    room: Room,
    onDismiss: () -> Unit,
    onUpdateNotes: (String?) -> Unit,
    onQuickCheckout: () -> Unit,
    onQuickOccupied: (guest: String, checkoutDate: String, notes: String) -> Unit
) {
    var notesText by remember { mutableStateOf(room.notes ?: "") }
    var actionGuestName by remember { mutableStateOf("") }
    var actionCheckoutDate by remember { mutableStateOf("2026-06-05") }

    Dialog(onDismissRequest = { onDismiss() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = SoftCardBg),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ห้อง ${room.roomNumber} (${room.roomType})",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = HotelGold
                        )
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "close", tint = Color.White)
                    }
                }

                Divider(color = Color.White.copy(0.1f))

                if (room.status == "เข้าพักอยู่") {
                    // Display Current occupant information
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(0.04f))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "ข้อมูลผู้เข้าพักปัจจุบัน (Stay Status)",
                            color = HotelGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(text = "ชื่อลูกค้า: ${room.currentGuestName ?: "-"}", color = Color.White)
                        Text(text = "วันที่นัดออก: ${room.checkOutDate ?: "-"}", color = Color.White.copy(0.8f))
                        if (room.notes != null) {
                            Text(text = "เงื่อนไขเข้าพัก: ${room.notes}", color = SoftGold, fontSize = 13.sp)
                        }
                    }

                    // Edit Notes (e.g. extension, room status memo)
                    OutlinedTextField(
                        value = notesText,
                        onValueChange = { notesText = it },
                        label = { Text("บันทึกเหตุการณ์ห้องพัก (Notes)", color = Color.White.copy(0.6f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = HotelGold,
                            unfocusedBorderColor = Color.White.copy(0.2f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Safe and modern Checkout buttons with ripples
                        Button(
                            onClick = { onQuickCheckout() },
                            colors = ButtonDefaults.buttonColors(containerColor = CrimsonCancel),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.Logout, contentDescription = "checkout")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("เช็คเอ้าท์ออก")
                        }

                        Button(
                            onClick = { onUpdateNotes(notesText.ifEmpty { null }) },
                            colors = ButtonDefaults.buttonColors(containerColor = HotelGold),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.Save, contentDescription = "save")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("บันทึกเงื่อนไข")
                        }
                    }
                } else {
                    // Room is VACANT. Provide Quick Occupy Option
                    Text(
                        text = "ทำรายการจองด่วน (Quick Stay Check-In)",
                        color = HotelGold,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    OutlinedTextField(
                        value = actionGuestName,
                        onValueChange = { actionGuestName = it },
                        label = { Text("ชื่อ-นามสกุลผู้จอง / แขก", color = Color.White.copy(0.6f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = HotelGold,
                            unfocusedBorderColor = Color.White.copy(0.2f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = actionCheckoutDate,
                        onValueChange = { actionCheckoutDate = it },
                        label = { Text("วันที่ Checkout (YYYY-MM-DD)", color = Color.White.copy(0.6f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = HotelGold,
                            unfocusedBorderColor = Color.White.copy(0.2f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = notesText,
                        onValueChange = { notesText = it },
                        label = { Text("ข้อความ (เช่น พักต่อ, พัก 3 คน)", color = Color.White.copy(0.6f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = HotelGold,
                            unfocusedBorderColor = Color.White.copy(0.2f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            if (actionGuestName.isNotEmpty()) {
                                onQuickOccupied(actionGuestName, actionCheckoutDate, notesText)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                        enabled = actionGuestName.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Login, contentDescription = "quick occupy")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("เช็คอินด่วน")
                    }
                }
            }
        }
    }
}
