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
import com.gopro.AdminApp.model.entity.Employee
import com.gopro.AdminApp.model.entity.Role
import com.gopro.AdminApp.presentation.components.ConfirmDeleteDialog
import com.gopro.AdminApp.presentation.components.FormBottomSheet
import com.gopro.AdminApp.presentation.state.UiState
import com.gopro.AdminApp.ui.components.SearchableDataList
import com.gopro.AdminApp.ui.theme.Background
import com.gopro.AdminApp.ui.theme.Error
import com.gopro.AdminApp.ui.theme.components.AsyncSelectField
import com.gopro.AdminApp.ui.theme.components.BaseMasterCard
import com.gopro.AdminApp.ui.theme.components.BaseSkeletonCard
import com.gopro.AdminApp.ui.theme.components.ButtonColorType
import com.gopro.AdminApp.ui.theme.components.CustomButton
import com.gopro.AdminApp.ui.theme.components.CustomSnackBar
import com.gopro.AdminApp.ui.theme.components.CustomSnackbarVisuals
import com.gopro.AdminApp.ui.theme.components.CustomTextField
import com.gopro.AdminApp.ui.theme.components.FloatingButton
import com.gopro.AdminApp.ui.theme.components.ImagePickerField
import com.gopro.AdminApp.ui.theme.components.SnackbarType
import com.gopro.AdminApp.viewmodel.EmployeeViewModel
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeScreen(
    onNavigateBack: () -> Unit,
    viewModel: EmployeeViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    val roles by viewModel.roles.collectAsState()
    val isRoleLoading by viewModel.isRoleLoading.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showBottomSheet by remember { mutableStateOf(false) }
    var employeeToDelete by remember { mutableStateOf<Employee?>(null) }

    var currentAction by remember { mutableStateOf("") }

    var inputFullName by remember { mutableStateOf("") }
    var inputEmail by remember { mutableStateOf("") }
    var inputPassword by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf<Role?>(null) }
    var inputPhotoFile by remember { mutableStateOf<File?>(null) }

    val fullNameErrorText = state.fieldErrors["FullName"] ?: ""
    val emailErrorText = state.fieldErrors["Email"] ?: ""
    val passwordErrorText = state.fieldErrors["Password"] ?: ""
    val roleIdErrorText = state.fieldErrors["RoleId"] ?: ""

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(state.actionSuccess) {
        if (state.actionSuccess) {
            val message = when (currentAction) {
                "insert" -> "Data Employee berhasil ditambahkan"
                "update" -> "Data Employee berhasil diubah"
                "delete" -> "Data Employee berhasil dihapus"
                else -> ""
            }

            when (currentAction) {
                "insert", "update" -> {
                    showBottomSheet = false
                    inputFullName = ""
                    inputEmail = ""
                    inputPassword = ""
                    selectedRole = null
                    inputPhotoFile = null
                }
                "delete" -> {
                    employeeToDelete = null
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

    // DISAMAKAN DENGAN DEPARTMENT SCREEN
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
                title = { Text("Data Employee", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
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
                        items = emptyList<Employee>(),
                        isLoading = true,
                        searchQuery = searchQuery,
                        onSearchQueryChange = { searchQuery = it },
                        searchPlaceholder = "Cari employee...",
                        skeletonItem = { BaseSkeletonCard() },
                        listItem = {}
                    )
                }

                is UiState.Success -> {
                    val filteredEmployees = dataStatus.data.filter {
                        it.fullName.contains(searchQuery, ignoreCase = true) ||
                                it.email.contains(searchQuery, ignoreCase = true)
                    }

                    SearchableDataList(
                        items = filteredEmployees,
                        isLoading = false,
                        searchQuery = searchQuery,
                        onSearchQueryChange = { searchQuery = it },
                        searchPlaceholder = "Cari employee...",
                        emptyMessage = "Tidak ada employee yang cocok.",
                        skeletonItem = { BaseSkeletonCard() },
                        onRefresh = {viewModel.fetchEmployees()},
                        listItem = { employee ->

                            var editFullName by remember(employee) { mutableStateOf(employee.fullName) }
                            var editEmail by remember(employee) { mutableStateOf(employee.email) }
                            var editPassword by remember { mutableStateOf("") }
                            var editPhotoFile by remember { mutableStateOf<File?>(null) }

                            var editSelectedRole by remember(employee, roles) {
                                val currentRole = roles.find { it.id == employee.role?.id }
                                val fallbackRole = employee.role
                                mutableStateOf<Role?>(currentRole ?: fallbackRole)
                            }

                            BaseMasterCard(
                                title = employee.fullName,
                                description = "Email: ${employee.email} | Role: ${employee.role?.name ?: "Tidak ada role"}",
                                formTitle = "Edit Employee",
                                isActionLoading = state.isActionLoading,
                                actionSuccess = state.actionSuccess,
                                onResetActionState = { viewModel.resetActionState() },
                                onDeleteClick = {
                                    currentAction = "delete"
                                    employeeToDelete = employee
                                },
                                onSaveEdit = {
                                    currentAction = "update"
                                    viewModel.updateEmployee(
                                        id = employee.id,
                                        fullName = editFullName,
                                        email = editEmail,
                                        password = editPassword.takeIf { it.isNotBlank() },
                                        roleId = editSelectedRole?.id ?: "",
                                        photoFile = editPhotoFile
                                    )
                                }
                            ) {
                                CustomTextField(
                                    value = editFullName,
                                    onValueChange = {
                                        editFullName = it
                                        viewModel.clearFullNameError()
                                    },
                                    placeholder = "Nama Lengkap",
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = !state.isActionLoading,
                                    isError = fullNameErrorText.isNotEmpty(),
                                    errorText = fullNameErrorText
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                CustomTextField(
                                    value = editEmail,
                                    onValueChange = {
                                        editEmail = it
                                        viewModel.clearEmailError()
                                    },
                                    placeholder = "Email",
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = !state.isActionLoading,
                                    isError = emailErrorText.isNotEmpty(),
                                    errorText = emailErrorText
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                CustomTextField(
                                    value = editPassword,
                                    onValueChange = {
                                        editPassword = it
                                        viewModel.clearPasswordError()
                                    },
                                    placeholder = "Password Baru (Biarkan kosong jika tetap)",
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = !state.isActionLoading,
                                    isPassword = true,
                                    isError = passwordErrorText.isNotEmpty(),
                                    errorText = passwordErrorText,
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                AsyncSelectField(
                                    selectedOption = editSelectedRole,
                                    options = roles,
                                    isLoading = isRoleLoading,
                                    placeholder = "Pilih Role",
                                    displayMapper = { it.name },
                                    onSearchQueryChanged = { query -> viewModel.searchRoles(query) },
                                    onOptionSelected = {
                                        editSelectedRole = it
                                        viewModel.clearRoleIdError()
                                    },
                                    enabled = !state.isActionLoading,
                                    isError = roleIdErrorText.isNotEmpty(),
                                    errorText = roleIdErrorText
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                ImagePickerField(
                                    selectedFile = editPhotoFile,
                                    onFileSelected = { editPhotoFile = it },
                                    placeholder = "Pilih Foto Baru (Opsional)",
                                    enabled = !state.isActionLoading
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
                            onClick = { viewModel.fetchEmployees() }
                        )
                    }
                }
            }
        }
    }

    if (showBottomSheet) {
        FormBottomSheet(
            title = "Tambah Employee",
            isLoading = state.isActionLoading,
            onDismiss = {
                showBottomSheet = false
                viewModel.resetActionState()
            },
            onSave = {
                viewModel.insertEmployee(
                    fullName = inputFullName,
                    email = inputEmail,
                    password = inputPassword,
                    roleId = selectedRole?.id ?: "",
                    photoFile = inputPhotoFile
                )
            }
        ) {
            CustomTextField(
                value = inputFullName,
                onValueChange = {
                    inputFullName = it
                    viewModel.clearFullNameError()
                },
                placeholder = "Nama Lengkap",
                singleLine = true,
                enabled = !state.isActionLoading,
                isError = fullNameErrorText.isNotEmpty(),
                errorText = fullNameErrorText
            )

            Spacer(modifier = Modifier.height(16.dp))

            CustomTextField(
                value = inputEmail,
                onValueChange = {
                    inputEmail = it
                    viewModel.clearEmailError()
                },
                placeholder = "Email",
                singleLine = true,
                enabled = !state.isActionLoading,
                isError = emailErrorText.isNotEmpty(),
                errorText = emailErrorText
            )

            Spacer(modifier = Modifier.height(16.dp))

            CustomTextField(
                value = inputPassword,
                onValueChange = {
                    inputPassword = it
                    viewModel.clearPasswordError()
                },
                placeholder = "Password",
                singleLine = true,
                isPassword = true,
                enabled = !state.isActionLoading,
                isError = passwordErrorText.isNotEmpty(),
                errorText = passwordErrorText
            )

            Spacer(modifier = Modifier.height(16.dp))

            AsyncSelectField(
                selectedOption = selectedRole,
                options = roles,
                isLoading = isRoleLoading,
                placeholder = "Pilih Role",
                displayMapper = { it.name },
                onSearchQueryChanged = { query -> viewModel.searchRoles(query) },
                onOptionSelected = {
                    selectedRole = it
                    viewModel.clearRoleIdError()
                },
                enabled = !state.isActionLoading,
                isError = roleIdErrorText.isNotEmpty(),
                errorText = roleIdErrorText
            )

            Spacer(modifier = Modifier.height(16.dp))

            ImagePickerField(
                selectedFile = inputPhotoFile,
                onFileSelected = { inputPhotoFile = it },
                placeholder = "Upload Foto (Opsional)",
                enabled = !state.isActionLoading
            )
        }
    }

    employeeToDelete?.let { employee ->
        ConfirmDeleteDialog(
            itemName = employee.fullName,
            onDismiss = { employeeToDelete = null },
            onConfirm = {
                viewModel.deleteEmployee(employee.id)
            }
        )
    }
}