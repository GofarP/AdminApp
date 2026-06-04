package com.gopro.AdminApp.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gopro.AdminApp.model.entity.Department
import com.gopro.AdminApp.presentation.components.ConfirmDeleteDialog
import com.gopro.AdminApp.presentation.components.FormBottomSheet
import com.gopro.AdminApp.presentation.state.UiState
import com.gopro.AdminApp.ui.components.SearchableDataList
import com.gopro.AdminApp.ui.theme.Background
import com.gopro.AdminApp.ui.theme.Error
import com.gopro.AdminApp.ui.theme.components.BaseMasterCard
import com.gopro.AdminApp.ui.theme.components.BaseSkeletonCard
import com.gopro.AdminApp.ui.theme.components.ButtonColorType
import com.gopro.AdminApp.ui.theme.components.CustomButton
import com.gopro.AdminApp.ui.theme.components.CustomSnackBar
import com.gopro.AdminApp.ui.theme.components.CustomSnackbarVisuals
import com.gopro.AdminApp.ui.theme.components.CustomTextField
import com.gopro.AdminApp.ui.theme.components.FloatingButton
import com.gopro.AdminApp.ui.theme.components.SnackbarType
import com.gopro.AdminApp.viewmodel.DepartmentViewModel
import kotlinx.coroutines.launch

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

    var currentAction by remember { mutableStateOf("") }

    var inputName by remember { mutableStateOf("") }
    var inputDesc by remember { mutableStateOf("") }

    val nameErrorText = state.fieldErrors["Name"] ?: ""
    val descErrorText = state.fieldErrors["Description"] ?: ""

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(state.actionSuccess) {
        if (state.actionSuccess) {
            val message = when (currentAction) {
                "insert" -> "Data Department berhasil ditambahkan"
                "update" -> "Data Department berhasil diubah"
                "delete" -> "Data Department berhasil dihapus"
                else -> ""
            }

            when (currentAction) {
                "insert", "update" -> {
                    showBottomSheet = false
                    inputName = ""
                    inputDesc = ""
                }
                "delete" -> {
                    departmentToDelete = null
                }
            }

            viewModel.resetActionState()
            currentAction = ""

            if (message.isNotEmpty()) {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        CustomSnackbarVisuals(
                            message = message,
                            type = SnackbarType.SUCCESS,
                            duration = SnackbarDuration.Short
                        )
                    )
                }
            }
        }
    }

    LaunchedEffect(state.actionErrorMessage) {
        state.actionErrorMessage?.let { msg ->

            viewModel.clearGeneralError()

            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    CustomSnackbarVisuals(
                        message = msg,
                        type = SnackbarType.ERROR,
                        duration = SnackbarDuration.Short
                    )
                )
            }
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                CustomSnackBar(snackbarData = data)
            }
        },
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
                        skeletonItem = { BaseSkeletonCard() },
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
                        skeletonItem = { BaseSkeletonCard() },
                        onRefresh = {viewModel.fetchDepartments()},
                        listItem = { dept ->

                            var editName by remember(dept) { mutableStateOf(dept.name) }
                            var editDesc by remember(dept) { mutableStateOf(dept.description) }

                            BaseMasterCard(
                                title = dept.name,
                                description = dept.description,
                                formTitle = "Edit Department",
                                isActionLoading = state.isActionLoading,
                                actionSuccess = state.actionSuccess,
                                onResetActionState = { viewModel.resetActionState() },
                                onDeleteClick = {
                                    currentAction = "delete"
                                    departmentToDelete = dept
                                },
                                onSaveEdit = {
                                    currentAction = "update"
                                    viewModel.updateDepartment(
                                        id = dept.id,
                                        name = editName,
                                        description = editDesc
                                    )
                                }
                            ) {
                                CustomTextField(
                                    value = editName,
                                    onValueChange = {
                                        editName = it
                                        viewModel.clearNameError()
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
                                        viewModel.clearDescError()
                                    },
                                    placeholder = "Deskripsi",
                                    modifier = Modifier.fillMaxWidth(),
                                    minLines = 3,
                                    enabled = !state.isActionLoading,
                                    isError = descErrorText.isNotEmpty(),
                                    errorText = descErrorText
                                )
                            }
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
            }
        )
    }
}