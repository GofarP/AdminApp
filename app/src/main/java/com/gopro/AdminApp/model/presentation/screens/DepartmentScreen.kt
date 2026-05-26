package com.gopro.AdminApp.presentation.screens

import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

import com.gopro.AdminApp.viewmodel.department.DepartmentViewModel
import com.gopro.AdminApp.presentation.state.UiState
import com.gopro.AdminApp.presentation.state.ScreenState

import com.gopro.AdminApp.model.entity.Department
import com.gopro.AdminApp.ui.theme.Background
import com.gopro.AdminApp.ui.theme.CardBg
import com.gopro.AdminApp.ui.theme.Error
import com.gopro.AdminApp.ui.theme.TextPri
import com.gopro.AdminApp.ui.theme.TextSec
import com.gopro.AdminApp.ui.theme.Warning

import com.gopro.AdminApp.ui.theme.components.FloatingButton
import com.gopro.AdminApp.ui.theme.components.CustomTextField
import com.gopro.AdminApp.presentation.components.FormBottomSheet
import com.gopro.AdminApp.presentation.components.ConfirmDeleteDialog
import com.gopro.AdminApp.ui.components.SearchableDataList
import com.gopro.AdminApp.ui.theme.components.ButtonColorType
import com.gopro.AdminApp.ui.theme.components.CustomButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DepartmentScreen(
    onNavigateBack: () -> Unit,
    viewModel: DepartmentViewModel = viewModel()
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showBottomSheet by remember { mutableStateOf(false) }
    var departmentToDelete by remember { mutableStateOf<Department?>(null) }

    // Menyimpan status aksi yang sedang berjalan agar pesan Toast lebih spesifik
    var currentAction by remember { mutableStateOf("") }

    var inputName by remember { mutableStateOf("") }
    var inputDesc by remember { mutableStateOf("") }

    val nameErrorText = state.fieldErrors["Name"] ?: ""
    val descErrorText = state.fieldErrors["Description"] ?: ""

    LaunchedEffect(state.actionErrorMessage) {
        state.actionErrorMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearGeneralError()
        }
    }

    LaunchedEffect(state.actionSuccess) {
        if (state.actionSuccess) {
            when (currentAction) {
                "insert" -> {
                    Toast.makeText(context, "Berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                    showBottomSheet = false
                    inputName = ""
                    inputDesc = ""
                }
                "delete" -> {
                    Toast.makeText(context, "Berhasil dihapus", Toast.LENGTH_SHORT).show()
                    departmentToDelete = null
                }
            }
            viewModel.resetActionState()
            currentAction = "" // Reset action
        }
    }

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text("Data Department", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        },
        floatingActionButton = {
            FloatingButton(
                onClick = {
                    currentAction = "insert"
                    showBottomSheet = true
                }
            )
        }
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            when (val dataStatus = state.dataState) {

                is UiState.Idle, is UiState.Loading -> {
                    SearchableDataList(
                        items = emptyList<Department>(),
                        isLoading = true,
                        searchQuery = searchQuery,
                        onSearchQueryChange = { searchQuery = it },
                        searchPlaceholder = "Cari department...",
                        skeletonItem = { SkeletonDepartmentCard() },
                        listItem = {}
                    )
                }

                is UiState.Success -> {
                    val filteredDepartments = dataStatus.data.filter {
                        it.name.contains(searchQuery, ignoreCase = true) ||
                                it.description.contains(searchQuery, ignoreCase = true)
                    }

                    SearchableDataList(
                        items = filteredDepartments,
                        isLoading = false,
                        searchQuery = searchQuery,
                        onSearchQueryChange = { searchQuery = it },
                        searchPlaceholder = "Cari department...",
                        emptyMessage = "Tidak ada department yang cocok.",
                        skeletonItem = { SkeletonDepartmentCard() },
                        listItem = { dept ->
                            DepartmentCard(
                                department = dept,
                                state = state,
                                onClearNameError = { viewModel.clearNameError() },
                                onClearDescError = { viewModel.clearDescError() },
                                onResetActionState = { viewModel.resetActionState() },
                                onEditClick = { selectedDept ->
                                    viewModel.updateDepartment(
                                        id = selectedDept.id,
                                        name = selectedDept.name,
                                        description = selectedDept.description
                                    )
                                },
                                onDeleteClick = { selectedDept ->
                                    currentAction = "delete"
                                    departmentToDelete = selectedDept
                                }
                            )
                        }
                    )
                }

                is UiState.Error -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(text = dataStatus.message, color = Error)
                        Spacer(modifier = Modifier.height(8.dp))
                        CustomButton(
                            colorType = ButtonColorType.PRIMARY,
                            text = "Coba Lagi",
                            modifier = Modifier
                                .width(150.dp)
                                .padding(top = 14.dp),
                            onClick = { viewModel.fetchDepartments() }
                        )
                    }
                }
            }
        }
    }

    if (showBottomSheet) {
        FormBottomSheet(
            title = "Tambah Department",
            isLoading = state.isActionLoading,
            onDismiss = {
                showBottomSheet = false
                viewModel.resetActionState()
            },
            onSave = {
                viewModel.insertDepartment(inputName, inputDesc)
            }
        ) {
            CustomTextField(
                value = inputName,
                onValueChange = {
                    inputName = it
                    viewModel.clearNameError()
                },
                placeholder = "Nama Department",
                singleLine = true,
                enabled = !state.isActionLoading,
                isError = nameErrorText.isNotEmpty(),
                errorText = nameErrorText
            )

            Spacer(modifier = Modifier.height(16.dp))

            CustomTextField(
                value = inputDesc,
                onValueChange = {
                    inputDesc = it
                    viewModel.clearDescError()
                },
                placeholder = "Deskripsi",
                singleLine = false,
                minLines = 3,
                enabled = !state.isActionLoading,
                isError = descErrorText.isNotEmpty(),
                errorText = descErrorText
            )
        }
    }

    departmentToDelete?.let { dept ->
        ConfirmDeleteDialog(
            itemName = dept.name,
            onDismiss = { departmentToDelete = null },
            onConfirm = {
                viewModel.deleteDepartment(dept.id)
                // departmentToDelete akan diset null di dalam LaunchedEffect saat sukses
            }
        )
    }
}


@Composable
fun DepartmentCard(
    department: Department,
    state: ScreenState<List<Department>>,
    onClearNameError: () -> Unit,
    onClearDescError: () -> Unit,
    onResetActionState: () -> Unit,
    onEditClick: (Department) -> Unit,
    onDeleteClick: (Department) -> Unit
) {
    var showEditForm by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(CardBg)
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(text = department.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPri)
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = department.description, fontSize = 13.sp, color = TextSec, maxLines = 2, overflow = TextOverflow.Ellipsis)

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = { showEditForm = true },
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp),
                    border = BorderStroke(1.dp, Warning)
                ) {
                    Text("Edit", fontSize = 12.sp, color = Warning)
                }

                Spacer(modifier = Modifier.width(8.dp))

                OutlinedButton(
                    onClick = { onDeleteClick(department) },
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp),
                    border = BorderStroke(1.dp, Error)
                ) {
                    Text("Hapus", fontSize = 12.sp, color = Error)
                }
            }
        }
    }

    if (showEditForm) {
        val context = LocalContext.current
        var editName by remember { mutableStateOf(department.name) }
        var editDesc by remember { mutableStateOf(department.description) }

        val nameErrorText = state.fieldErrors["Name"] ?: ""
        val descErrorText = state.fieldErrors["Description"] ?: ""

        // Efek spesifik untuk Edit agar tidak bertabrakan dengan efek Insert/Delete di layar utama
        LaunchedEffect(state.actionSuccess) {
            if (state.actionSuccess) {
                Toast.makeText(context, "Perubahan disimpan", Toast.LENGTH_SHORT).show()
                showEditForm = false
                onResetActionState()
            }
        }

        FormBottomSheet(
            title = "Edit Department",
            buttonText = "Simpan",
            isLoading = state.isActionLoading,
            onDismiss = {
                showEditForm = false
                onResetActionState()
            },
            onSave = {
                val updatedDepartment = department.copy(
                    name = editName,
                    description = editDesc
                )
                onEditClick(updatedDepartment)
            }
        ) {
            CustomTextField(
                value = editName,
                onValueChange = {
                    editName = it
                    onClearNameError()
                },
                placeholder = "Nama Department",
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isActionLoading,
                isError = nameErrorText.isNotEmpty(),
                errorText = nameErrorText
            )

            Spacer(modifier = Modifier.height(16.dp))

            CustomTextField(
                value = editDesc,
                onValueChange = {
                    editDesc = it
                    onClearDescError()
                },
                placeholder = "Deskripsi",
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isActionLoading,
                isError = descErrorText.isNotEmpty(),
                errorText = descErrorText
            )
        }
    }
}

@Composable
fun SkeletonDepartmentCard() {
    val infiniteTransition = rememberInfiniteTransition(label = "skeleton")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(tween(800, easing = LinearEasing), RepeatMode.Reverse),
        label = "alpha"
    )
    val skeletonColor = Color.LightGray.copy(alpha = alpha)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(CardBg)
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.fillMaxWidth(0.6f).height(20.dp).clip(RoundedCornerShape(4.dp)).background(skeletonColor))
            Spacer(modifier = Modifier.height(8.dp))
            Box(modifier = Modifier.fillMaxWidth(0.9f).height(14.dp).clip(RoundedCornerShape(4.dp)).background(skeletonColor))
            Spacer(modifier = Modifier.height(4.dp))
            Box(modifier = Modifier.fillMaxWidth(0.5f).height(14.dp).clip(RoundedCornerShape(4.dp)).background(skeletonColor))

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Box(modifier = Modifier.width(50.dp).height(28.dp).clip(RoundedCornerShape(12.dp)).background(skeletonColor))
                Spacer(modifier = Modifier.width(8.dp))
                Box(modifier = Modifier.width(60.dp).height(28.dp).clip(RoundedCornerShape(12.dp)).background(skeletonColor))
            }
        }
    }
}