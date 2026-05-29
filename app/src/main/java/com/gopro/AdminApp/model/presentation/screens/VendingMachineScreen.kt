package com.gopro.AdminApp.presentation.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gopro.AdminApp.model.entity.VendingMachine
import com.gopro.AdminApp.presentation.components.ConfirmDeleteDialog
import com.gopro.AdminApp.presentation.components.FormBottomSheet
import com.gopro.AdminApp.presentation.state.UiState
import com.gopro.AdminApp.ui.components.SearchableDataList
import com.gopro.AdminApp.ui.theme.Background
import com.gopro.AdminApp.ui.theme.Error
import com.gopro.AdminApp.ui.theme.components.*
import com.gopro.AdminApp.viewmodel.vendingmachine.VendingMachineViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VendingMachineScreen(
    onNavigateBack: () -> Unit,
    viewModel: VendingMachineViewModel = viewModel()
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showBottomSheet by remember { mutableStateOf(false) }
    var machineToDelete by remember { mutableStateOf<VendingMachine?>(null) }

    var currentAction by remember { mutableStateOf("") }

    var inputMachineCode by remember { mutableStateOf("") }
    var inputName by remember { mutableStateOf("") }
    var inputLocation by remember { mutableStateOf("") }
    var selectIsActive by remember { mutableStateOf(true) }
    var inputLastRestock by remember { mutableStateOf("") }

    var showInsertDatePicker by remember { mutableStateOf(false) }
    val insertDatePickerState = rememberDatePickerState()

    val codeErrorText = state.fieldErrors["MachineCode"] ?: ""
    val nameErrorText = state.fieldErrors["Name"] ?: ""
    val locationErrorText = state.fieldErrors["Location"] ?: ""
    val isActiveErrorText = state.fieldErrors["IsActive"] ?: ""
    val restockErrorText = state.fieldErrors["LastRestock"] ?: ""

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(state.actionSuccess) {
        if (state.actionSuccess) {
            val message = when (currentAction) {
                "insert" -> "Data Vending Machine berhasil ditambahkan"
                "update" -> "Data Vending Machine berhasil diubah"
                "delete" -> "Data Vending Machine berhasil dihapus"
                else -> ""
            }

            when (currentAction) {
                "insert", "update" -> {
                    showBottomSheet = false
                    inputMachineCode = ""
                    inputName = ""
                    inputLocation = ""
                    selectIsActive = true
                    inputLastRestock = ""
                }
                "delete" -> {
                    machineToDelete = null
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
                title = { Text("Data Vending Machine", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
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
                        items = emptyList<VendingMachine>(),
                        isLoading = true,
                        searchQuery = searchQuery,
                        onSearchQueryChange = { searchQuery = it },
                        searchPlaceholder = "Cari mesin...",
                        skeletonItem = { BaseSkeletonCard() },
                        listItem = {}
                    )
                }

                is UiState.Success -> {
                    val filteredMachines = dataStatus.data.filter {
                        it.name.contains(searchQuery, ignoreCase = true) ||
                                it.machineCode.contains(searchQuery, ignoreCase = true) ||
                                it.location.contains(searchQuery, ignoreCase = true)
                    }

                    SearchableDataList(
                        items = filteredMachines,
                        isLoading = false,
                        searchQuery = searchQuery,
                        onSearchQueryChange = { searchQuery = it },
                        searchPlaceholder = "Cari mesin...",
                        emptyMessage = "Tidak ada mesin yang cocok.",
                        skeletonItem = { BaseSkeletonCard() },
                        listItem = { machine ->

                            var editMachineCode by remember(machine) { mutableStateOf(machine.machineCode) }
                            var editName by remember(machine) { mutableStateOf(machine.name) }
                            var editLocation by remember(machine) { mutableStateOf(machine.location) }
                            var editIsActive by remember(machine) { mutableStateOf(machine.isActive) }
                            var editLastRestock by remember(machine) { mutableStateOf(machine.lastRestock) }

                            var editDropdownExpanded by remember { mutableStateOf(false) }
                            var showEditDatePicker by remember { mutableStateOf(false) }
                            val editDatePickerState = rememberDatePickerState()

                            val statusText = if (machine.isActive) "Aktif" else "Tidak Aktif"

                            BaseMasterCard(
                                title = machine.name,
                                description = "Lokasi: ${machine.location} | Kode: ${machine.machineCode} | Status: $statusText",
                                formTitle = "Edit Vending Machine",
                                isActionLoading = state.isActionLoading,
                                actionSuccess = state.actionSuccess,
                                onResetActionState = { viewModel.resetActionState() },
                                onDeleteClick = {
                                    currentAction = "delete"
                                    machineToDelete = machine
                                },
                                onSaveEdit = {
                                    currentAction = "update"
                                    viewModel.updateVendingMachine(
                                        id = machine.id,
                                        machineCode = editMachineCode,
                                        name = editName,
                                        location = editLocation,
                                        isActive = editIsActive,
                                        lastRestock = editLastRestock
                                    )
                                }
                            ) {
                                CustomTextField(
                                    value = editMachineCode,
                                    onValueChange = {
                                        editMachineCode = it
                                        viewModel.clearMachineCodeError()
                                    },
                                    placeholder = "Kode Mesin",
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = !state.isActionLoading,
                                    isError = codeErrorText.isNotEmpty(),
                                    errorText = codeErrorText
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                CustomTextField(
                                    value = editName,
                                    onValueChange = {
                                        editName = it
                                        viewModel.clearName()
                                    },
                                    placeholder = "Nama Mesin",
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = !state.isActionLoading,
                                    isError = nameErrorText.isNotEmpty(),
                                    errorText = nameErrorText
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                CustomTextField(
                                    value = editLocation,
                                    onValueChange = {
                                        editLocation = it
                                        viewModel.clearLocation()
                                    },
                                    placeholder = "Lokasi",
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = !state.isActionLoading,
                                    isError = locationErrorText.isNotEmpty(),
                                    errorText = locationErrorText
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                ExposedDropdownMenuBox(
                                    expanded = editDropdownExpanded,
                                    onExpandedChange = { if (!state.isActionLoading) editDropdownExpanded = !editDropdownExpanded }
                                ) {
                                    OutlinedTextField(
                                        value = if (editIsActive) "Aktif" else "Tidak Aktif",
                                        onValueChange = {},
                                        readOnly = true,
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = editDropdownExpanded) },
                                        modifier = Modifier
                                            .menuAnchor()
                                            .fillMaxWidth(),
                                        label = { Text("Status Mesin") },
                                        enabled = !state.isActionLoading,
                                        isError = isActiveErrorText.isNotEmpty(),
                                        supportingText = if (isActiveErrorText.isNotEmpty()) { { Text(isActiveErrorText) } } else null
                                    )
                                    ExposedDropdownMenu(
                                        expanded = editDropdownExpanded,
                                        onDismissRequest = { editDropdownExpanded = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("Aktif") },
                                            onClick = {
                                                editIsActive = true
                                                editDropdownExpanded = false
                                                viewModel.clearIsActive()
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Tidak Aktif") },
                                            onClick = {
                                                editIsActive = false
                                                editDropdownExpanded = false
                                                viewModel.clearIsActive()
                                            }
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                OutlinedTextField(
                                    value = editLastRestock,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Last Restock") },
                                    trailingIcon = { Icon(Icons.Outlined.CalendarToday, contentDescription = "Pilih Tanggal") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null
                                        ) {
                                            if (!state.isActionLoading) showEditDatePicker = true
                                        },
                                    enabled = false,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                                        disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    isError = restockErrorText.isNotEmpty(),
                                    supportingText = if (restockErrorText.isNotEmpty()) { { Text(restockErrorText) } } else null
                                )

                                if (showEditDatePicker) {
                                    DatePickerDialog(
                                        onDismissRequest = { showEditDatePicker = false },
                                        confirmButton = {
                                            TextButton(onClick = {
                                                editDatePickerState.selectedDateMillis?.let { millis ->
                                                    editLastRestock = formatMillisToDate(millis)
                                                    viewModel.clearLastRestock()
                                                }
                                                showEditDatePicker = false
                                            }) { Text("OK") }
                                        },
                                        dismissButton = {
                                            TextButton(onClick = { showEditDatePicker = false }) { Text("Batal") }
                                        }
                                    ) {
                                        DatePicker(state = editDatePickerState)
                                    }
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
                            onClick = { viewModel.fetchVendingMachine() }
                        )
                    }
                }
            }
        }
    }

    if (showBottomSheet) {
        FormBottomSheet(
            title = "Tambah Vending Machine",
            isLoading = state.isActionLoading,
            onDismiss = {
                showBottomSheet = false
                viewModel.resetActionState()
            },
            onSave = {
                viewModel.insertVendingMachine(
                    machineCode = inputMachineCode,
                    name = inputName,
                    location = inputLocation,
                    isActive = selectIsActive,
                    lastRestock = inputLastRestock
                )
            }
        ) {
            CustomTextField(
                value = inputMachineCode,
                onValueChange = {
                    inputMachineCode = it
                    viewModel.clearMachineCodeError()
                },
                placeholder = "Kode Mesin",
                singleLine = true,
                enabled = !state.isActionLoading,
                isError = codeErrorText.isNotEmpty(),
                errorText = codeErrorText
            )

            Spacer(modifier = Modifier.height(16.dp))

            CustomTextField(
                value = inputName,
                onValueChange = {
                    inputName = it
                    viewModel.clearName()
                },
                placeholder = "Nama Mesin",
                singleLine = true,
                enabled = !state.isActionLoading,
                isError = nameErrorText.isNotEmpty(),
                errorText = nameErrorText
            )

            Spacer(modifier = Modifier.height(16.dp))

            CustomTextField(
                value = inputLocation,
                onValueChange = {
                    inputLocation = it
                    viewModel.clearLocation()
                },
                placeholder = "Lokasi",
                singleLine = false,
                minLines = 2,
                enabled = !state.isActionLoading,
                isError = locationErrorText.isNotEmpty(),
                errorText = locationErrorText
            )

            Spacer(modifier = Modifier.height(16.dp))

            var insertDropdownExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = insertDropdownExpanded,
                onExpandedChange = { if (!state.isActionLoading) insertDropdownExpanded = !insertDropdownExpanded }
            ) {
                OutlinedTextField(
                    value = if (selectIsActive) "Aktif" else "Tidak Aktif",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = insertDropdownExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    label = { Text("Status Mesin") },
                    enabled = !state.isActionLoading,
                    isError = isActiveErrorText.isNotEmpty(),
                    supportingText = if (isActiveErrorText.isNotEmpty()) { { Text(isActiveErrorText) } } else null
                )
                ExposedDropdownMenu(
                    expanded = insertDropdownExpanded,
                    onDismissRequest = { insertDropdownExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Aktif") },
                        onClick = {
                            selectIsActive = true
                            insertDropdownExpanded = false
                            viewModel.clearIsActive()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Tidak Aktif") },
                        onClick = {
                            selectIsActive = false
                            insertDropdownExpanded = false
                            viewModel.clearIsActive()
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = inputLastRestock,
                onValueChange = {},
                readOnly = true,
                label = { Text("Last Restock") },
                placeholder = { Text("Pilih Tanggal") },
                trailingIcon = { Icon(Icons.Outlined.CalendarToday, contentDescription = "Pilih Tanggal") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        if (!state.isActionLoading) showInsertDatePicker = true
                    },
                enabled = false,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                isError = restockErrorText.isNotEmpty(),
                supportingText = if (restockErrorText.isNotEmpty()) { { Text(restockErrorText) } } else null
            )

            if (showInsertDatePicker) {
                DatePickerDialog(
                    onDismissRequest = { showInsertDatePicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            insertDatePickerState.selectedDateMillis?.let { millis ->
                                inputLastRestock = formatMillisToDate(millis)
                                viewModel.clearLastRestock()
                            }
                            showInsertDatePicker = false
                        }) { Text("OK") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showInsertDatePicker = false }) { Text("Batal") }
                    }
                ) {
                    DatePicker(state = insertDatePickerState)
                }
            }
        }
    }

    machineToDelete?.let { machine ->
        ConfirmDeleteDialog(
            itemName = machine.name,
            onDismiss = { machineToDelete = null },
            onConfirm = {
                viewModel.deleteVendingMachine(machine.id)
            }
        )
    }
}

private fun formatMillisToDate(millis: Long): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return formatter.format(Date(millis))
}