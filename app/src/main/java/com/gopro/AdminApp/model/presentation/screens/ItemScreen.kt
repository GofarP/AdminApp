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
import com.gopro.AdminApp.model.entity.Item
import com.gopro.AdminApp.model.entity.ItemCategories
import com.gopro.AdminApp.presentation.components.ConfirmDeleteDialog
import com.gopro.AdminApp.presentation.components.FormBottomSheet
import com.gopro.AdminApp.presentation.state.UiState
import com.gopro.AdminApp.ui.components.SearchableDataList
import com.gopro.AdminApp.ui.theme.Background
import com.gopro.AdminApp.ui.theme.Error
import com.gopro.AdminApp.ui.theme.components.*
import com.gopro.AdminApp.viewmodel.ItemViewModel // Asumsi nama ViewModel-mu
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemScreen(
    onNavigateBack: () -> Unit,
    viewModel: ItemViewModel = viewModel()
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()

    val categories by viewModel.itemCategories.collectAsState()
    val isCategoryLoading by viewModel.isCategoryLoading.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showBottomSheet by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<Item?>(null) }

    var currentAction by remember { mutableStateOf("") }

    var inputName by remember { mutableStateOf("") }
    var inputPrice by remember { mutableStateOf("") }
    var inputQuantity by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<ItemCategories?>(null) }

    val nameErrorText = state.fieldErrors["Name"] ?: ""
    val categoryIdErrorText = state.fieldErrors["ItemCategoryId"] ?: ""
    val priceErrorText = state.fieldErrors["Price"] ?: ""
    val quantityErrorText = state.fieldErrors["Quantity"] ?: ""

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(state.actionSuccess) {
        if (state.actionSuccess) {
            val message = when (currentAction) {
                "insert" -> "Data Item berhasil ditambahkan"
                "update" -> "Data Item berhasil diubah"
                "delete" -> "Data Item berhasil dihapus"
                else -> ""
            }

            when (currentAction) {
                "insert", "update" -> {
                    showBottomSheet = false
                    inputName = ""
                    inputPrice = ""
                    inputQuantity = ""
                    selectedCategory = null
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
                title = { Text("Data Item", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
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
                        items = emptyList<Item>(),
                        isLoading = true,
                        searchQuery = searchQuery,
                        onSearchQueryChange = { searchQuery = it },
                        searchPlaceholder = "Cari item...",
                        onRefresh = {viewModel.fetchItems()},
                        skeletonItem = { BaseSkeletonCard() },
                        listItem = {}
                    )
                }

                is UiState.Success -> {
                    val filteredItems = dataStatus.data.filter {
                        it.name.contains(searchQuery, ignoreCase = true)
                    }

                    SearchableDataList(
                        items = filteredItems,
                        isLoading = false,
                        searchQuery = searchQuery,
                        onSearchQueryChange = { searchQuery = it },
                        searchPlaceholder = "Cari item...",
                        emptyMessage = "Tidak ada item yang cocok.",
                        skeletonItem = { BaseSkeletonCard() },
                        listItem = { item ->

                            var editName by remember(item) { mutableStateOf(item.name) }
                            var editPrice by remember(item) { mutableStateOf(item.price.toString()) }
                            var editQuantity by remember(item) { mutableStateOf(item.quantity.toString()) }

                            var editSelectedCategory by remember(item, categories) {

                                val categoryFromList = categories.find { it.id == item.itemCategoryId }

                                val fallbackCategory = if (categoryFromList == null && item.itemCategoryId != 0) {
                                    ItemCategories(
                                        id = item.itemCategoryId,
                                        name = item.itemCategoryName ?: "Kategori ID: ${item.itemCategoryId}",
                                        description = ""
                                    )
                                } else null

                                mutableStateOf<ItemCategories?>(categoryFromList ?: fallbackCategory)
                            }


                            BaseMasterCard(
                                title = item.name,
                                description = "Kategori: ${item.itemCategoryName ?: "Unknown"} | Harga: Rp${item.price} | Stok: ${item.quantity}",
                                formTitle = "Edit Item",
                                isActionLoading = state.isActionLoading,
                                actionSuccess = state.actionSuccess,
                                onResetActionState = { viewModel.resetActionState() },
                                onDeleteClick = {
                                    currentAction = "delete"
                                    itemToDelete = item
                                },
                                onSaveEdit = {
                                    currentAction = "update"
                                    viewModel.updateItem(
                                        id = item.id,
                                        name = editName,
                                        itemCategoriesId = editSelectedCategory?.id ?: 0,
                                        price = editPrice.toDoubleOrNull() ?: 0.0,
                                        quantity = editQuantity.toIntOrNull() ?: 0
                                    )
                                }
                            ) {
                                CustomTextField(
                                    value = editName,
                                    onValueChange = {
                                        editName = it
                                        viewModel.clearNameError()
                                    },
                                    placeholder = "Nama Item",
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
                                        viewModel.searchItemCategories(query)
                                    },
                                    onOptionSelected = { category ->
                                        editSelectedCategory = category
                                        viewModel.clearItemCategoryIdError()
                                    },
                                    enabled = !state.isActionLoading,
                                    isError = categoryIdErrorText.isNotEmpty(),
                                    errorText = categoryIdErrorText
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    CustomTextField(
                                        value = editPrice,
                                        onValueChange = { editPrice = it },
                                        placeholder = "Harga",
                                        modifier = Modifier.weight(1f),
                                        enabled = !state.isActionLoading,
                                        isError = priceErrorText.isNotEmpty(),
                                        errorText = priceErrorText
                                        // Idealnya CustomTextField milikmu mendukung parameter KeyboardOptions
                                        // keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                    )

                                    CustomTextField(
                                        value = editQuantity,
                                        onValueChange = { editQuantity = it },
                                        placeholder = "Stok",
                                        modifier = Modifier.weight(1f),
                                        enabled = !state.isActionLoading,
                                        isError = quantityErrorText.isNotEmpty(),
                                        errorText = quantityErrorText
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
                            onClick = { viewModel.fetchItems() }
                        )
                    }
                }
            }
        }
    }

    if (showBottomSheet) {
        FormBottomSheet(
            title = "Tambah Item",
            isLoading = state.isActionLoading,
            onDismiss = {
                showBottomSheet = false
                viewModel.resetActionState()
            },
            onSave = {
                viewModel.insertItem(
                    name = inputName,
                    itemCategoryId = selectedCategory?.id ?: 0,
                    price = inputPrice.toDoubleOrNull() ?: 0.0,
                    quantity = inputQuantity.toIntOrNull() ?: 0
                )
            }
        ) {
            CustomTextField(
                value = inputName,
                onValueChange = {
                    inputName = it
                    viewModel.clearNameError()
                },
                placeholder = "Nama Item",
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
                    viewModel.searchItemCategories(query)
                },
                onOptionSelected = { category ->
                    selectedCategory = category
                    viewModel.clearItemCategoryIdError()
                },
                enabled = !state.isActionLoading,
                isError = categoryIdErrorText.isNotEmpty(),
                errorText = categoryIdErrorText
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CustomTextField(
                    value = inputPrice,
                    onValueChange = { inputPrice = it },
                    placeholder = "Harga (Rp)",
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    enabled = !state.isActionLoading,
                    isError = priceErrorText.isNotEmpty(),
                    errorText = priceErrorText
                )

                CustomTextField(
                    value = inputQuantity,
                    onValueChange = { inputQuantity = it },
                    placeholder = "Stok",
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    enabled = !state.isActionLoading,
                    isError = quantityErrorText.isNotEmpty(),
                    errorText = quantityErrorText
                )
            }
        }
    }

    itemToDelete?.let { item ->
        ConfirmDeleteDialog(
            itemName = item.name,
            onDismiss = { itemToDelete = null },
            onConfirm = {
                viewModel.deleteItem(item.id)
            }
        )
    }
}