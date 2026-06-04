package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.sp
import com.example.ui.screens.BookingScreen
import com.example.ui.screens.CustomerScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.RoomsScreen
import com.example.ui.theme.HotelGold
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SoftCardBg
import com.example.ui.viewmodel.HotelViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: HotelViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                var currentTab by remember { mutableStateOf(0) }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        NavigationBar(
                            containerColor = SoftCardBg,
                            contentColor = Color.White,
                            modifier = Modifier.testTag("main_bottom_nav")
                        ) {
                            NavigationBarItem(
                                selected = currentTab == 0,
                                onClick = { currentTab = 0 },
                                icon = { Icon(imageVector = Icons.Default.Dashboard, contentDescription = "Dashboard") },
                                label = { Text("แดชบอร์ด", fontSize = 11.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = SoftCardBg,
                                    selectedTextColor = HotelGold,
                                    indicatorColor = HotelGold,
                                    unselectedIconColor = Color.White.copy(0.4f),
                                    unselectedTextColor = Color.White.copy(0.4f)
                                ),
                                modifier = Modifier.testTag("nav_item_dashboard")
                            )
                            NavigationBarItem(
                                selected = currentTab == 1,
                                onClick = { currentTab = 1 },
                                icon = { Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = "Bookings") },
                                label = { Text("การจอง", fontSize = 11.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = SoftCardBg,
                                    selectedTextColor = HotelGold,
                                    indicatorColor = HotelGold,
                                    unselectedIconColor = Color.White.copy(0.4f),
                                    unselectedTextColor = Color.White.copy(0.4f)
                                ),
                                modifier = Modifier.testTag("nav_item_bookings")
                            )
                            NavigationBarItem(
                                selected = currentTab == 2,
                                onClick = { currentTab = 2 },
                                icon = { Icon(imageVector = Icons.Default.Hotel, contentDescription = "Rooms") },
                                label = { Text("หน้าห้องพัก", fontSize = 11.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = SoftCardBg,
                                    selectedTextColor = HotelGold,
                                    indicatorColor = HotelGold,
                                    unselectedIconColor = Color.White.copy(0.4f),
                                    unselectedTextColor = Color.White.copy(0.4f)
                                ),
                                modifier = Modifier.testTag("nav_item_rooms")
                            )
                            NavigationBarItem(
                                selected = currentTab == 3,
                                onClick = { currentTab = 3 },
                                icon = { Icon(imageVector = Icons.Default.People, contentDescription = "Customers") },
                                label = { Text("บริหารลูกค้า", fontSize = 11.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = SoftCardBg,
                                    selectedTextColor = HotelGold,
                                    indicatorColor = HotelGold,
                                    unselectedIconColor = Color.White.copy(0.4f),
                                    unselectedTextColor = Color.White.copy(0.4f)
                                ),
                                modifier = Modifier.testTag("nav_item_customers")
                            )
                        }
                    }
                ) { innerPadding ->
                    when (currentTab) {
                        0 -> DashboardScreen(
                            viewModel = viewModel,
                            modifier = Modifier.padding(innerPadding)
                        )
                        1 -> BookingScreen(
                            viewModel = viewModel,
                            modifier = Modifier.padding(innerPadding)
                        )
                        2 -> RoomsScreen(
                            viewModel = viewModel,
                            modifier = Modifier.padding(innerPadding)
                        )
                        3 -> CustomerScreen(
                            viewModel = viewModel,
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}

