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
import com.gopro.AdminApp.model.entity.Role
import com.gopro.AdminApp.presentation.components.ConfirmDeleteDialog
import com.gopro.AdminApp.presentation.components.FormBottomSheet
import com.gopro.AdminApp.presentation.state.UiState
import com.gopro.AdminApp.ui.components.SearchableDataList
import com.gopro.AdminApp.ui.theme.Background
import com.gopro.AdminApp.ui.theme.Error
import com.gopro.AdminApp.ui.theme.components.*
import com.gopro.AdminApp.viewmodel.RoleViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoleScreen(
    onNavigateBack: () -> Unit,
    viewModel: RoleViewModel = viewModel()
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()

    val groupedPermissions by viewModel.groupedPermissions.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showBottomSheet by remember { mutableStateOf(false) }
    var roleToDelete by remember { mutableStateOf<Role?>(null) }

    var currentAction by remember { mutableStateOf("") }

    var inputName by remember { mutableStateOf("") }
    var selectedPermissionIds by remember { mutableStateOf<List<Int>>(emptyList()) }

    val nameErrorText = state.fieldErrors["Name"] ?: ""
    val permissionErrorText = state.fieldErrors["PermissionIds"] ?: ""

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(state.actionSuccess) {
        if (state.actionSuccess) {
            val message = when (currentAction) {
                "insert" -> "Data Role berhasil ditambahkan"
                "update" -> "Data Role berhasil diubah"
                "delete" -> "Data Role berhasil dihapus"
                else -> ""
            }

            when (currentAction) {
                "insert", "update" -> {
                    showBottomSheet = false
                    inputName = ""
                    selectedPermissionIds = emptyList()
                }
                "delete" -> {
                    roleToDelete = null
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
                title = { Text("Data Role", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
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
                        items = emptyList<Role>(),
                        isLoading = true,
                        searchQuery = searchQuery,
                        onSearchQueryChange = { searchQuery = it },
                        searchPlaceholder = "Cari role...",
                        skeletonItem = { BaseSkeletonCard() },
                        listItem = {}
                    )
                }

                is UiState.Success -> {
                    val filteredRoles = dataStatus.data.filter {
                        it.name.contains(searchQuery, ignoreCase = true)
                    }

                    SearchableDataList(
                        items = filteredRoles,
                        isLoading = false,
                        searchQuery = searchQuery,
                        onSearchQueryChange = { searchQuery = it },
                        searchPlaceholder = "Cari role...",
                        emptyMessage = "Tidak ada role yang cocok.",
                        skeletonItem = { BaseSkeletonCard() },
                        onRefresh = {viewModel.fetchRoles()},
                        listItem = { role ->

                            var editName by remember(role) { mutableStateOf(role.name) }


                            var editSelectedPermissionIds by remember(role) {
                                mutableStateOf(role.permissionIds ?: emptyList())
                            }

                            BaseMasterCard(
                                title = role.name,
                                description = "Total Permissions: ${role.permissionIds?.size ?: 0}",
                                formTitle = "Edit Role",
                                isActionLoading = state.isActionLoading,
                                actionSuccess = state.actionSuccess,
                                onResetActionState = { viewModel.resetActionState() },
                                onDeleteClick = {
                                    currentAction = "delete"
                                    roleToDelete = role
                                },
                                onSaveEdit = {
                                    currentAction = "update"
                                    viewModel.updateRole(
                                        id = role.id,
                                        name = editName,
                                        permissionIds = editSelectedPermissionIds
                                    )
                                }
                            ) {
                                CustomTextField(
                                    value = editName,
                                    onValueChange = {
                                        editName = it
                                        viewModel.clearNameError()
                                    },
                                    placeholder = "Nama Role",
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = !state.isActionLoading,
                                    isError = nameErrorText.isNotEmpty(),
                                    errorText = nameErrorText
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // Menampilkan komponen PermissionSelector yang sudah dikelompokkan
                                PermissionSelector(
                                    groupedPermissions = groupedPermissions,
                                    selectedPermissionIds = editSelectedPermissionIds,
                                    onPermissionToggled = { permissionId, isChecked ->
                                        editSelectedPermissionIds = if (isChecked) {
                                            editSelectedPermissionIds + permissionId
                                        } else {
                                            editSelectedPermissionIds - permissionId
                                        }
                                        viewModel.clearPermissionError()
                                    }
                                )

                                // Teks Error khusus untuk Permission jika kosong/salah
                                if (permissionErrorText.isNotEmpty()) {
                                    Text(
                                        text = permissionErrorText,
                                        color = MaterialTheme.colorScheme.error,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(top = 4.dp, start = 16.dp)
                                    )
                                }
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
                            onClick = { viewModel.fetchRoles() }
                        )
                    }
                }
            }
        }
    }

    if (showBottomSheet) {
        FormBottomSheet(
            title = "Tambah Role",
            isLoading = state.isActionLoading,
            onDismiss = {
                showBottomSheet = false
                viewModel.resetActionState()
            },
            onSave = {
                viewModel.insertRole(
                    name = inputName,
                    permissionIds = selectedPermissionIds
                )
            }
        ) {
            CustomTextField(
                value = inputName,
                onValueChange = {
                    inputName = it
                    viewModel.clearNameError()
                },
                placeholder = "Nama Role",
                singleLine = true,
                enabled = !state.isActionLoading,
                isError = nameErrorText.isNotEmpty(),
                errorText = nameErrorText
            )

            Spacer(modifier = Modifier.height(16.dp))

            PermissionSelector(
                groupedPermissions = groupedPermissions,
                selectedPermissionIds = selectedPermissionIds,
                onPermissionToggled = { permissionId, isChecked ->
                    selectedPermissionIds = if (isChecked) {
                        selectedPermissionIds + permissionId
                    } else {
                        selectedPermissionIds - permissionId
                    }
                    viewModel.clearPermissionError()
                }
            )

            if (permissionErrorText.isNotEmpty()) {
                Text(
                    text = permissionErrorText,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp, start = 16.dp)
                )
            }
        }
    }

    roleToDelete?.let { role ->
        ConfirmDeleteDialog(
            itemName = role.name,
            onDismiss = { roleToDelete = null },
            onConfirm = {
                viewModel.deleteRole(role.id)
            }
        )
    }
}