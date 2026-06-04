package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Booking
import com.example.data.model.Room
import com.example.ui.theme.*
import com.example.ui.viewmodel.HotelViewModel

@Composable
fun DashboardScreen(
    viewModel: HotelViewModel,
    modifier: Modifier = Modifier
) {
    val roomsState by viewModel.rooms.collectAsStateWithLifecycle()
    val bookingsState by viewModel.bookings.collectAsStateWithLifecycle()
    val totalRevenue by viewModel.totalRevenue.collectAsStateWithLifecycle()
    val typeBreakdown by viewModel.bookingCountByRoomType.collectAsStateWithLifecycle()

    val totalBookings = bookingsState.size
    val pendingBookings = bookingsState.count { it.status == "รอยืนยัน" }
    val confirmedBookings = bookingsState.count { it.status == "ยืนยันแล้ว" }
    val completedBookings = bookingsState.count { it.status == "เสร็จสิ้น" }
    val cancelledBookings = bookingsState.count { it.status == "ยกเลิก" }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // --- Header Banner ---
        item {
            HotelHeaderBanner(totalBookings)
        }

        // --- Performance Dashboard Cards ---
        item {
            Text(
                text = "แดชบอร์ดสรุปข้อมูล",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = HotelGold,
                    letterSpacing = 1.sp
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            val cards = listOf(
                DashboardCardInfo("การจองทั้งหมด", totalBookings.toString(), Icons.Default.CalendarMonth, HotelGold),
                DashboardCardInfo("รอยืนยัน", pendingBookings.toString(), Icons.Default.PendingActions, AmberPending),
                DashboardCardInfo("ยืนยันแล้ว", confirmedBookings.toString(), Icons.Default.CheckCircle, ActiveBlue),
                DashboardCardInfo("เสร็จสิ้นหมด", completedBookings.toString(), Icons.Default.AssignmentTurnedIn, EmeraldGreen),
                DashboardCardInfo("ยกเลิกการจอง", cancelledBookings.toString(), Icons.Default.Cancel, CrimsonCancel)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                cards.take(2).forEach { card ->
                    Box(modifier = Modifier.weight(1f)) {
                        DashboardCard(card)
                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                cards.drop(2).take(3).forEach { card ->
                    Box(modifier = Modifier.weight(1f)) {
                        DashboardCard(card)
                    }
                }
            }
        }

        // --- Revenue & Custom Sales Chart ---
        item {
            RevenueCard(totalRevenue, typeBreakdown)
        }

        // --- Current Stay Status Summary ---
        item {
            CurrentStayBanner(roomsState)
        }
    }
}

data class DashboardCardInfo(
    val title: String,
    val value: String,
    val icon: ImageVector,
    val tint: Color
)

@Composable
fun DashboardCard(info: DashboardCardInfo) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("dashboard_card_${info.title}"),
        colors = CardDefaults.cardColors(containerColor = SoftCardBg),
        border = CardDefaults.outlinedCardBorder(true).copy(
            brush = Brush.linearGradient(listOf(Color.White.copy(0.04f), info.tint.copy(0.2f)))
        ),
        shape = RoundedCornerShape(12.dp),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = info.icon,
                    contentDescription = info.title,
                    tint = info.tint,
                    modifier = Modifier.size(24.dp)
                )
            }
            Column {
                Text(
                    text = info.value,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = info.title,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.White.copy(0.6f)
                    )
                )
            }
        }
    }
}

@Composable
fun HotelHeaderBanner(totalBookings: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        DeepSlateBg.copy(0.5f),
                        PrimaryLightNavy.copy(0.8f)
                    )
                )
            )
            .drawBehind {
                drawCircle(
                    brush = Brush.radialGradient(listOf(HotelGold.copy(0.12f), Color.Transparent)),
                    radius = 280f,
                    center = Offset(size.width * 0.9f, size.height * 0.2f)
                )
            }
            .padding(20.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "วิพัฒน์โฮเทล (Wipat Hotel)",
                style = MaterialTheme.typography.titleLarge.copy(
                    color = HotelGold,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            )
            Text(
                text = "ระบบบริหารจัดการการจองห้องพักและข้อมูลผู้ใช้แบบอัจฉริยะ (Premium Office Manager)",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color.White.copy(0.7f)
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
                color = HotelGold.copy(0.15f),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    text = "ประวัติเก็บข้อมูล: $totalBookings รายการ",
                    color = SoftGold,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun RevenueCard(totalRevenue: Double, typeBreakdown: Map<String, Int>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SoftCardBg),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder(true).copy(
            brush = Brush.linearGradient(listOf(Color.White.copy(0.04f), HotelGold.copy(0.15f)))
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "รายได้รวม (ยืนยัน/เสร็จสิ้น)",
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = Color.White.copy(0.6f)
                        )
                    )
                    Text(
                        text = "฿${String.format("%,.2f", totalRevenue)}",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            color = HotelGold,
                            fontWeight = FontWeight.ExtraBold
                        )
                    )
                }
                Icon(
                    imageVector = Icons.Default.CurrencyExchange,
                    contentDescription = "finance icon",
                    tint = HotelGold,
                    modifier = Modifier.size(36.dp)
                )
            }

            Divider(color = Color.White.copy(0.1f))

            Text(
                text = "การจองตามประเภทห้องพัก (ยอดสัดส่วน)",
                style = MaterialTheme.typography.titleSmall.copy(
                    color = Color.White.copy(0.8f),
                    fontWeight = FontWeight.Bold
                )
            )

            // Dynamic Chart Bars using Box weights
            val dailyCount = typeBreakdown["ห้องพักรายวัน"] ?: 15
            val monthlyCount = typeBreakdown["ห้องพักรายเดือน"] ?: 0
            val meetingCount = typeBreakdown["ห้องประชุม"] ?: 3
            val total = (dailyCount + monthlyCount + meetingCount).coerceAtLeast(1)

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Line 1: Daily Room
                ChartBar(
                    label = "ห้องพักรายวัน",
                    count = dailyCount,
                    percent = dailyCount.toFloat() / total.toFloat(),
                    barColor = HotelGold
                )
                // Line 2: Monthly Room
                ChartBar(
                    label = "ห้องพักรายเดือน",
                    count = monthlyCount,
                    percent = monthlyCount.toFloat() / total.toFloat(),
                    barColor = ActiveBlue
                )
                // Line 3: Meeting Room
                ChartBar(
                    label = "ห้องประชุม",
                    count = meetingCount,
                    percent = meetingCount.toFloat() / total.toFloat(),
                    barColor = InfoIndigo
                )
            }
        }
    }
}

@Composable
fun ChartBar(label: String, count: Int, percent: Float, barColor: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(0.8f))
            Text(
                text = "$count รายการ (${String.format("%.0f", percent * 100)}%)",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = barColor
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(Color.White.copy(0.06f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(percent.coerceIn(0.05f..1f))
                    .clip(RoundedCornerShape(5.dp))
                    .background(barColor)
            )
        }
    }
}

@Composable
fun CurrentStayBanner(rooms: List<Room>) {
    val occupiedRooms = rooms.filter { it.status == "เข้าพักอยู่" }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("current_stays_banner"),
        colors = CardDefaults.cardColors(containerColor = SoftCardBg),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder(true).copy(
            brush = Brush.linearGradient(listOf(Color.White.copy(0.04f), AmberPending.copy(0.12f)))
        )
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "สถานะห้องพักในคณะนี้พักต่อ (${occupiedRooms.size} ห้อง)",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
                Icon(
                    imageVector = Icons.Default.LocalHotel,
                    contentDescription = "",
                    tint = AmberPending
                )
            }

            if (occupiedRooms.isEmpty()) {
                Surface(
                    color = Color.White.copy(0.04f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "ไม่มีลูกค้าลงทะเบียนพักค้างคืนในขณะนี้",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(0.5f),
                        modifier = Modifier.padding(14.dp)
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    occupiedRooms.forEach { room ->
                        Surface(
                            color = Color.White.copy(0.05f),
                            shape = RoundedCornerShape(10.dp),
                            border = CardDefaults.outlinedCardBorder(true).copy(
                                brush = Brush.linearGradient(listOf(Color.White.copy(0.03f), Color.White.copy(0.08f)))
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(AmberPending.copy(0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = room.roomNumber,
                                            color = AmberPending,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = room.currentGuestName ?: "ไม่ระบุชื่อ",
                                            color = Color.White,
                                            fontWeight = FontWeight.SemiBold,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Text(
                                            text = room.roomType,
                                            color = Color.White.copy(0.5f),
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Surface(
                                        color = EmeraldGreen.copy(0.12f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "คืนออก ${room.checkOutDate ?: "-"}",
                                            color = EmeraldGreen,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    if (room.notes != null) {
                                        Text(
                                            text = room.notes,
                                            color = SoftGold,
                                            fontSize = 11.sp,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
