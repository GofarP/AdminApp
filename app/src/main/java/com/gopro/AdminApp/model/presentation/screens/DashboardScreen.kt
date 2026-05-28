package com.gopro.AdminApp.presentation.screens

import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gopro.AdminApp.model.presentation.MenuGroup
import com.gopro.AdminApp.model.presentation.MenuItem
import com.gopro.AdminApp.model.presentation.StatItem
import com.gopro.AdminApp.presentation.state.UiState
import com.gopro.AdminApp.ui.theme.*
import com.gopro.AdminApp.ui.theme.components.*
import com.gopro.AdminApp.viewmodel.dashboard.DashboardViewModel

@Composable
fun DashboardScreen(
    onNavigate: (route: String) -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: DashboardViewModel = viewModel()
) {
    var selectedMenu by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    val state by viewModel.state.collectAsState()

    val userName by viewModel.userName.collectAsState()
    val userPhoto by viewModel.userPhoto.collectAsState()

    val menuGroups = listOf(
        MenuGroup("Utama", listOf(
            MenuItem("nav_dashboard", "Dashboard", Icons.Outlined.Dashboard, accentColor = Brand)
        )),
        MenuGroup("Data Master", listOf(
            MenuItem("nav_employee", "Employee", Icons.Outlined.People, accentColor = BrandLight),
            MenuItem("nav_department", "Department", Icons.Outlined.AccountTree, accentColor = Purple),
            MenuItem("nav_permission_categories", "Permission Cat.", Icons.Outlined.Category, accentColor = Teal),
            MenuItem("nav_permission", "Permission", Icons.Outlined.AdminPanelSettings, accentColor = Teal),
            MenuItem("nav_role", "Role", Icons.Outlined.ManageAccounts, accentColor = Amber),
            MenuItem("nav_item_cat", "Item Category", Icons.Outlined.Inventory2, accentColor = Coral),
        )),
        MenuGroup("Item", listOf(
            MenuItem("nav_item", "Item", Icons.Outlined.LocalCafe, accentColor = Amber, badge = "12")
        )),
        MenuGroup("Vending Machine", listOf(
            MenuItem("nav_vending_machine", "Vending Machine", Icons.Outlined.SmartScreen, accentColor = BrandLight, badge = "3"),
            MenuItem("nav_vending_item", "Vending Item", Icons.Outlined.BreakfastDining, accentColor = Coral),
        )),
        MenuGroup("Logout", listOf(
            MenuItem("action_logout", "Logout", Icons.Outlined.Logout, accentColor = Error)
        )),
    )

    LaunchedEffect(Unit) {
        viewModel.fetchStats()
    }

    val infiniteTransition = rememberInfiniteTransition(label = "orb")
    val orbPulse by infiniteTransition.animateFloat(
        initialValue = 0.9f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(3500, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .safeDrawingPadding()
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawOrb(Offset(size.width * 0.85f, size.height * 0.06f), 200f * orbPulse, Brand.copy(.05f))
            drawOrb(Offset(size.width * 0.1f,  size.height * 0.12f), 140f, Accent.copy(.03f))
            drawOrb(Offset(size.width * 0.5f,  size.height * 0.9f),  180f, Brand.copy(.04f))
        }

        Column(modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
        ) {
            TopBar(
                userName = userName,
                photoUrl = userPhoto
            )
            HeroCard()
            Spacer(Modifier.height(20.dp))
            SectionLabel("Ringkasan Statistik")
            Spacer(Modifier.height(10.dp))

            when (val dataStatus = state.dataState) {
                is UiState.Success -> {
                    val data = dataStatus.data
                    val statsList = listOf(
                        StatItem("Total Users", data.totalUsers.toString(), "Pengguna terdaftar", Icons.Outlined.People, BrandLight),
                        StatItem("Total Departments", data.totalDepartments.toString(), "Entitas departemen", Icons.Outlined.AccountTree, Purple),
                        StatItem("Total Machines", data.totalMachines.toString(), "Mesin aktif", Icons.Outlined.SmartScreen, Amber)
                    )

                    StatsGrids(statsList)
                }

                is UiState.Idle, is UiState.Loading -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.heightIn(max = 400.dp)
                    ) {
                        items(4) {
                            StatSkeletonCard()
                        }
                    }
                }

                is UiState.Error -> {
                    val errorStatsList = listOf(
                        StatItem("Total Users", "-", "Gagal memuat", Icons.Outlined.People, BrandLight),
                        StatItem("Total Departments", "-", "Gagal memuat", Icons.Outlined.AccountTree, Purple),
                        StatItem("Total Machines", "-", "Gagal memuat", Icons.Outlined.SmartScreen, Amber)
                    )

                    StatsGrids(errorStatsList)
                }
            }

            menuGroups.forEach { group ->
                Spacer(Modifier.height(20.dp))
                SectionLabel(group.groupLabel)
                Spacer(Modifier.height(10.dp))
                MenuGrid(
                    items = group.items,
                    selectedMenu = selectedMenu,
                    onSelect = { clickedItem ->
                        selectedMenu = clickedItem.id

                        when (clickedItem.id) {
                            "action_logout" -> {
                                viewModel.logout(onLogoutComplete = {
                                    onNavigateToLogin()
                                })
                            }
                            else -> {
                                onNavigate(clickedItem.id)
                            }
                        }
                    }
                )
                Spacer(Modifier.height(40.dp))
            }
        }
    }
}