package com.gopro.AdminApp.presentation.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.gopro.AdminApp.model.entity.Item
import com.gopro.AdminApp.model.entity.ItemsByMachine
import com.gopro.AdminApp.model.entity.VendingWithStock
import com.gopro.AdminApp.presentation.components.ConfirmDeleteDialog
import com.gopro.AdminApp.presentation.components.FormBottomSheet
import com.gopro.AdminApp.presentation.state.UiState
import com.gopro.AdminApp.ui.components.SearchableDataList
import com.gopro.AdminApp.ui.theme.Background
import com.gopro.AdminApp.ui.theme.Error
import com.gopro.AdminApp.ui.theme.components.*
import com.gopro.AdminApp.viewmodel.VendingItemViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VendingItemScreen(
    onNavigateBack: () -> Unit,
    viewModel: VendingItemViewModel = viewModel()
) {
    val context = LocalContext.current
    val itemsState by viewModel.state.collectAsState()
    val machinesState by viewModel.vendingMachines.collectAsState()

    val itemList by viewModel.items.collectAsState()
    val isItemLoading by viewModel.isItemLoading.collectAsState()

    var selectedMachine by remember { mutableStateOf<VendingWithStock?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var showBottomSheet by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<ItemsByMachine?>(null) }
    var currentAction by remember { mutableStateOf("") }

    var selectedItem by remember { mutableStateOf<Item?>(null) }
    var inputQuantity by remember { mutableStateOf("") }
    var inputCapacity by remember { mutableStateOf("") }

    val itemIdErrorText = itemsState.fieldErrors["ItemId"] ?: ""
    val quantityErrorText = itemsState.fieldErrors["Quantity"] ?: ""
    val capacityErrorText = itemsState.fieldErrors["Capacity"] ?: ""

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.fetchVendingWithStock()
        viewModel.searchItems("")
    }

    LaunchedEffect(itemsState.actionSuccess) {
        if (itemsState.actionSuccess) {
            val message = when (currentAction) {
                "insert" -> "Item berhasil ditambahkan ke mesin"
                "restock" -> "Restock berhasil"
                "delete" -> "Item berhasil dihapus dari mesin"
                else -> ""
            }

            when (currentAction) {
                "insert", "restock" -> {
                    showBottomSheet = false
                    selectedItem = null
                    inputQuantity = ""
                    inputCapacity = ""
                }
                "delete" -> {
                    itemToDelete = null
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

    LaunchedEffect(itemsState.actionErrorMessage) {
        itemsState.actionErrorMessage?.let { msg ->
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
                title = {
                    val titleText = if (selectedMachine == null) "Vending Machines" else "Isi Mesin: ${selectedMachine?.name}"
                    Text(titleText, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (selectedMachine != null) {
                            selectedMachine = null
                            searchQuery = ""
                        } else {
                            onNavigateBack()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        },
        floatingActionButton = {
            if (selectedMachine != null) {
                FloatingButton(
                    onClick = {
                        currentAction = "insert"
                        showBottomSheet = true
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            if (selectedMachine == null) {
                when (val dataStatus = machinesState) {
                    is UiState.Idle, is UiState.Loading -> {
                        SearchableDataList(
                            items = emptyList<VendingWithStock>(),
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
                            searchPlaceholder = "Cari nama, kode, atau lokasi...",
                            emptyMessage = "Tidak ada mesin yang cocok.",
                            skeletonItem = { BaseSkeletonCard() },
                            onRefresh = {viewModel.fetchVendingWithStock()},
                            listItem = { machine ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 12.dp)
                                        .clickable {
                                            selectedMachine = machine
                                            searchQuery = ""
                                            viewModel.fetchItemsByMachine(machine.id)
                                        },
                                    colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.White),
                                    shape = RoundedCornerShape(12.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            text = "${machine.name} (${machine.machineCode})",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "Lokasi: ${machine.location}",
                                            fontSize = 14.sp,
                                            color = androidx.compose.ui.graphics.Color.Gray
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "Stok: ${machine.totalStock} | Jenis: ${machine.totalItemTypes} | Kategori: ${machine.totalCategories}",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.primary
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
                                modifier = Modifier.width(150.dp).padding(top = 14.dp),
                                onClick = { viewModel.fetchVendingWithStock() }
                            )
                        }
                    }
                }
            } else {
                when (val dataStatus = itemsState.dataState) {
                    is UiState.Idle, is UiState.Loading -> {
                        SearchableDataList(
                            items = emptyList<ItemsByMachine>(),
                            isLoading = true,
                            searchQuery = searchQuery,
                            onSearchQueryChange = { searchQuery = it },
                            searchPlaceholder = "Cari item...",
                            skeletonItem = { BaseSkeletonCard() },
                            listItem = {}
                        )
                    }

                    is UiState.Success -> {
                        val filteredItems = dataStatus.data.filter {
                            it.itemName.contains(searchQuery, ignoreCase = true) ||
                                    it.categoryName.contains(searchQuery, ignoreCase = true)
                        }

                        SearchableDataList(
                            items = filteredItems,
                            isLoading = false,
                            searchQuery = searchQuery,
                            onSearchQueryChange = { searchQuery = it },
                            searchPlaceholder = "Cari nama item...",
                            emptyMessage = "Mesin ini masih kosong.",
                            skeletonItem = { BaseSkeletonCard() },
                            onRefresh = {selectedMachine?.let { machine->
                                viewModel.fetchItemsByMachine(machine.id)
                            }},
                            listItem = { item ->
                                var editQuantity by remember(item) { mutableStateOf(item.quantity.toString()) }
                                var editCapacity by remember(item) { mutableStateOf(item.capacity.toString()) }

                                BaseMasterCard(
                                    title = item.itemName,
                                    description = "Qty: ${item.quantity} / ${item.capacity} | Harga: Rp ${item.price}",
                                    formTitle = "Restock Item",
                                    isActionLoading = itemsState.isActionLoading,
                                    actionSuccess = itemsState.actionSuccess,
                                    onResetActionState = { viewModel.resetActionState() },
                                    onDeleteClick = {
                                        currentAction = "delete"
                                        itemToDelete = item
                                    },
                                    onSaveEdit = {
                                        currentAction = "restock"
                                        viewModel.restock(
                                            id = item.id,
                                            quantity = editQuantity.toIntOrNull() ?: 0,
                                            capacity = editCapacity.toIntOrNull() ?: 0
                                        )
                                    }
                                ) {
                                    CustomTextField(
                                        value = editQuantity,
                                        onValueChange = {
                                            editQuantity = it
                                            viewModel.clearQuantityError()
                                        },
                                        placeholder = "Quantity",
                                        modifier = Modifier.fillMaxWidth(),
                                        enabled = !itemsState.isActionLoading,
                                        isError = quantityErrorText.isNotEmpty(),
                                        errorText = quantityErrorText
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    CustomTextField(
                                        value = editCapacity,
                                        onValueChange = {
                                            editCapacity = it
                                            viewModel.clearCapacityError()
                                        },
                                        placeholder = "Capacity",
                                        modifier = Modifier.fillMaxWidth(),
                                        enabled = !itemsState.isActionLoading,
                                        isError = capacityErrorText.isNotEmpty(),
                                        errorText = capacityErrorText
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
                                modifier = Modifier.width(150.dp).padding(top = 14.dp),
                                onClick = { selectedMachine?.let { viewModel.fetchItemsByMachine(it.id) } }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showBottomSheet) {
        FormBottomSheet(
            title = "Assign Item ke Mesin",
            isLoading = itemsState.isActionLoading,
            onDismiss = {
                showBottomSheet = false
                viewModel.resetActionState()
                selectedItem = null
                inputQuantity = ""
                inputCapacity = ""
            },
            onSave = {
                selectedMachine?.let { machine ->
                    viewModel.assignItemToMachine(
                        vendingMachineId = machine.id,
                        itemId = selectedItem?.id ?: 0,
                        quantity = inputQuantity.toIntOrNull() ?: 0,
                        capacity = inputCapacity.toIntOrNull() ?: 0,
                        price = selectedItem?.price ?: 0.0
                    )
                }
            }
        ) {
            AsyncSelectField(
                selectedOption = selectedItem,
                options = itemList,
                isLoading = isItemLoading,
                placeholder = "Pilih Item",
                displayMapper = { it.name },
                onSearchQueryChanged = { query -> viewModel.searchItems(query) },
                onOptionSelected = {
                    selectedItem = it
                    viewModel.clearItemIdError()
                },
                enabled = !itemsState.isActionLoading,
                isError = itemIdErrorText.isNotEmpty(),
                errorText = itemIdErrorText
            )

            Spacer(modifier = Modifier.height(16.dp))

            CustomTextField(
                value = inputQuantity,
                onValueChange = {
                    inputQuantity = it
                    viewModel.clearQuantityError()
                },
                placeholder = "Quantity",
                singleLine = true,
                enabled = !itemsState.isActionLoading,
                isError = quantityErrorText.isNotEmpty(),
                errorText = quantityErrorText
            )

            Spacer(modifier = Modifier.height(16.dp))

            CustomTextField(
                value = inputCapacity,
                onValueChange = {
                    inputCapacity = it
                    viewModel.clearCapacityError()
                },
                placeholder = "Capacity",
                singleLine = true,
                enabled = !itemsState.isActionLoading,
                isError = capacityErrorText.isNotEmpty(),
                errorText = capacityErrorText
            )
        }
    }

    itemToDelete?.let { item ->
        ConfirmDeleteDialog(
            itemName = item.itemName,
            onDismiss = { itemToDelete = null },
            onConfirm = {
                viewModel.removeItemFromMachine(item.id.toString())
            }
        )
    }
}