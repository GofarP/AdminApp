package com.gopro.AdminApp.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gopro.AdminApp.model.entity.Permission
import com.gopro.AdminApp.model.entity.PermissionCategories
import com.gopro.AdminApp.presentation.components.ConfirmDeleteDialog
import com.gopro.AdminApp.presentation.components.FormBottomSheet
import com.gopro.AdminApp.presentation.state.UiState
import com.gopro.AdminApp.ui.components.SearchableDataList
import com.gopro.AdminApp.ui.theme.Background
import com.gopro.AdminApp.ui.theme.Error
import com.gopro.AdminApp.ui.theme.components.*
import com.gopro.AdminApp.viewmodel.PermissionViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionScreen(
    onNavigateBack: () -> Unit,
    viewModel: PermissionViewModel = viewModel()
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()

    val categories by viewModel.permissionCategories.collectAsState()
    val isCategoryLoading by viewModel.isCategoryLoading.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showBottomSheet by remember { mutableStateOf(false) }
    var permissionToDelete by remember { mutableStateOf<Permission?>(null) }

    var currentAction by remember { mutableStateOf("") }

    var inputName by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<PermissionCategories?>(null) }

    val nameErrorText = state.fieldErrors["Name"] ?: ""
    val categoryIdErrorText = state.fieldErrors["PermissionCategoryId"] ?: ""

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(state.actionSuccess) {
        if (state.actionSuccess) {
            val message = when (currentAction) {
                "insert" -> "Data Permission berhasil ditambahkan"
                "update" -> "Data Permission berhasil diubah"
                "delete" -> "Data Permission berhasil dihapus"
                else -> ""
            }

            when (currentAction) {
                "insert", "update" -> {
                    showBottomSheet = false
                    inputName = ""
                    selectedCategory = null
                }
                "delete" -> {
                    permissionToDelete = null
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
                title = { Text("Data Permission", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
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
                        items = emptyList<Permission>(),
                        isLoading = true,
                        searchQuery = searchQuery,
                        onSearchQueryChange = { searchQuery = it },
                        searchPlaceholder = "Cari permission...",
                        skeletonItem = { BaseSkeletonCard() },
                        listItem = {}
                    )
                }

                is UiState.Success -> {
                    val filteredPermissions = dataStatus.data.filter {
                        it.name.contains(searchQuery, ignoreCase = true)
                    }

                    SearchableDataList(
                        items = filteredPermissions,
                        isLoading = false,
                        searchQuery = searchQuery,
                        onSearchQueryChange = { searchQuery = it },
                        searchPlaceholder = "Cari permission...",
                        emptyMessage = "Tidak ada permission yang cocok.",
                        skeletonItem = { BaseSkeletonCard() },
                        onRefresh = {viewModel.fetchPermissions()},
                        listItem = { permission ->

                            var editName by remember(permission) { mutableStateOf(permission.name) }

                            var editSelectedCategory by remember(permission) {
                                val categoryFromBackend = permission.category
                                val categoryFromList = categories.find { it.id == permission.permissionCategoryId }
                                mutableStateOf<PermissionCategories?>(categoryFromBackend ?: categoryFromList)
                            }

                            BaseMasterCard(
                                title = permission.name,
                                description = "Category: ${permission.category?.name ?: "Tidak diketahui"}",
                                formTitle = "Edit Permission",
                                isActionLoading = state.isActionLoading,
                                actionSuccess = state.actionSuccess,
                                onResetActionState = { viewModel.resetActionState() },
                                onDeleteClick = {
                                    currentAction = "delete"
                                    permissionToDelete = permission
                                },
                                onSaveEdit = {
                                    currentAction = "update"
                                    viewModel.updatePermission(
                                        id = permission.id,
                                        name = editName,
                                        permissionCategoryId = editSelectedCategory?.id ?: 0
                                    )
                                }
                            ) {
                                CustomTextField(
                                    value = editName,
                                    onValueChange = {
                                        editName = it
                                        viewModel.clearNameError()
                                    },
                                    placeholder = "Nama Permission",
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = !state.isActionLoading,
                                    isError = nameErrorText.isNotEmpty(),
                                    errorText = nameErrorText
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                AsyncSelectField(
                                    selectedOption = editSelectedCategory,
                                    options = categories,
                                    isLoading = isCategoryLoading,
                                    placeholder = "Pilih Kategori",
                                    displayMapper = { it.name },
                                    onSearchQueryChanged = { query ->
                                        viewModel.searchCategories(query)
                                    },
                                    onOptionSelected = { category ->
                                        editSelectedCategory = category
                                        viewModel.clearPermissionCategoryIdError()
                                    },
                                    enabled = !state.isActionLoading,
                                    isError = categoryIdErrorText.isNotEmpty(),
                                    errorText = categoryIdErrorText
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
                            onClick = { viewModel.fetchPermissions() }
                        )
                    }
                }
            }
        }
    }

    if (showBottomSheet) {
        FormBottomSheet(
            title = "Tambah Permission",
            isLoading = state.isActionLoading,
            onDismiss = {
                showBottomSheet = false
                viewModel.resetActionState()
            },
            onSave = {
                viewModel.insertPermission(
                    name = inputName,
                    permissionCategoryId = selectedCategory?.id ?: 0
                )
            }
        ) {
            CustomTextField(
                value = inputName,
                onValueChange = {
                    inputName = it
                    viewModel.clearNameError()
                },
                placeholder = "Nama Permission",
                singleLine = true,
                enabled = !state.isActionLoading,
                isError = nameErrorText.isNotEmpty(),
                errorText = nameErrorText
            )

            Spacer(modifier = Modifier.height(16.dp))

            AsyncSelectField(
                selectedOption = selectedCategory,
                options = categories,
                isLoading = isCategoryLoading,
                placeholder = "Pilih Kategori",
                displayMapper = { it.name },
                onSearchQueryChanged = { query ->
                    viewModel.searchCategories(query)
                },
                onOptionSelected = { category ->
                    selectedCategory = category
                    viewModel.clearPermissionCategoryIdError()
                },
                enabled = !state.isActionLoading,
                isError = categoryIdErrorText.isNotEmpty(),
                errorText = categoryIdErrorText
            )
        }
    }

    permissionToDelete?.let { permission ->
        ConfirmDeleteDialog(
            itemName = permission.name,
            onDismiss = { permissionToDelete = null },
            onConfirm = {
                viewModel.deletePermission(permission.id)
            }
        )
    }
}