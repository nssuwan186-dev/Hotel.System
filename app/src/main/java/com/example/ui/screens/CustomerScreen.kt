package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Customer
import com.example.ui.theme.*
import com.example.ui.viewmodel.HotelViewModel

@Composable
fun CustomerScreen(
    viewModel: HotelViewModel,
    modifier: Modifier = Modifier
) {
    val customersList by viewModel.customers.collectAsStateWithLifecycle()
    var searchPhoneQuery by remember { mutableStateOf("") }

    val filteredCustomers = customersList.filter {
        searchPhoneQuery.isEmpty() ||
                it.name.contains(searchPhoneQuery, ignoreCase = true) ||
                it.contactPhone.contains(searchPhoneQuery, ignoreCase = true) ||
                it.cmId.contains(searchPhoneQuery, ignoreCase = true)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Search & Sync Header Info
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SoftCardBg),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "ระบบ Customer Sync",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = HotelGold,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "ตรวจสอบรายชื่อลูกค้าเก่าโดยอัตโนมัติผ่านเบอร์โทรศัพท์ เพื่อเชื่อมโยงประวัติและรหัสลูกค้า CM ID ในระบบ Single Source of Truth",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(0.6f)
                )

                OutlinedTextField(
                    value = searchPhoneQuery,
                    onValueChange = { searchPhoneQuery = it },
                    placeholder = { Text("ค้นหารายชื่อ, เบอร์มือถือ, CM ID...", color = Color.White.copy(0.4f)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = HotelGold,
                        unfocusedBorderColor = Color.White.copy(0.2f)
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        }

        if (filteredCustomers.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "ไม่พบข้อมูลสอดคล้องในฐานระบบลูกค้า",
                    color = Color.White.copy(0.4f),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(filteredCustomers) { cust ->
                    CustomerSyncRow(cust)
                }
            }
        }
    }
}

@Composable
fun CustomerSyncRow(customer: Customer) {
    Surface(
        color = SoftCardBg,
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder(true).copy(
            brush = Brush.linearGradient(listOf(Color.White.copy(0.04f), HotelGold.copy(0.12f)))
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(45.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(HotelGold.copy(0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "person profile",
                        tint = HotelGold,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Column {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = customer.name,
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(
                            color = ActiveBlue.copy(0.15f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = customer.cmId,
                                color = ActiveBlue,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "phone",
                            tint = SoftGold,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "เข้าพักเรียบร้อย: ${customer.totalStayCount} ครั้ง",
                            color = Color.White.copy(0.5f),
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.ContactPhone,
                            contentDescription = "phone",
                            tint = Color.White.copy(0.4f),
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = customer.contactPhone.ifEmpty { "ไม่แสดงเบอร์" },
                            color = Color.White.copy(0.5f),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    if (customer.address.isNotEmpty()) {
                        Text(
                            text = "ที่ส่งเอกสาร: ${customer.address}",
                            color = Color.White.copy(0.4f),
                            fontSize = 11.sp,
                            maxLines = 1,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }

            // Customer cumulative booking expenses
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "ยอดใช้จ่ายสะสม",
                    color = Color.White.copy(0.4f),
                    fontSize = 11.sp
                )
                Text(
                    text = "฿${String.format("%,.0f", customer.totalSpend)}",
                    color = HotelGold,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "ล่าสุด: ${customer.lastBookingDate}",
                    color = Color.White.copy(0.3f),
                    fontSize = 10.sp
                )
            }
        }
    }
}
