package com.gopro.AdminApp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.gopro.AdminApp.model.dto.request.AssignItemToMachine
import com.gopro.AdminApp.model.dto.request.RestockRequest
import com.gopro.AdminApp.model.dto.response.ErrorResponse
import com.gopro.AdminApp.model.entity.Item
import com.gopro.AdminApp.model.entity.ItemsByMachine
import com.gopro.AdminApp.model.entity.VendingWithStock
import com.gopro.AdminApp.network.ItemApi
import com.gopro.AdminApp.network.RetrofitClient
import com.gopro.AdminApp.network.VendingItemApi
import com.gopro.AdminApp.presentation.state.ScreenState
import com.gopro.AdminApp.presentation.state.UiState
import com.gopro.AdminApp.utils.ApiErrorHandler
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class VendingItemViewModel : ViewModel() {
    private val api = RetrofitClient.createService(VendingItemApi::class.java)
    private val itemApi = RetrofitClient.createService(ItemApi::class.java) // TAMBAHAN: Instance untuk Item API

    // State utama untuk daftar Item di dalam Mesin
    private val _state = MutableStateFlow(ScreenState<List<ItemsByMachine>>())
    val state: StateFlow<ScreenState<List<ItemsByMachine>>> = _state.asStateFlow()

    // State untuk daftar Mesin awal
    private val _vendingMachines = MutableStateFlow<UiState<List<VendingWithStock>>>(UiState.Idle)
    val vendingMachines: StateFlow<UiState<List<VendingWithStock>>> = _vendingMachines.asStateFlow()

    // TAMBAHAN: State untuk menampung master Item yang akan dipilih di AsyncSelectField
    private val _items = MutableStateFlow<List<Item>>(emptyList())
    val items: StateFlow<List<Item>> = _items.asStateFlow()

    private val _isItemLoading = MutableStateFlow(false)
    val isItemLoading: StateFlow<Boolean> = _isItemLoading.asStateFlow()

    private var searchItemJob: Job? = null
    private var currentMachineId: Int? = null

    init {
        fetchVendingWithStock()
        searchItems("") // TAMBAHAN: Load awal master item kosong/default
    }

    // ==========================================
    // FUNGSI GET DATA
    // ==========================================

    fun fetchVendingWithStock(search: String = "", page: Int = 1) {
        viewModelScope.launch {
            _vendingMachines.value = UiState.Loading
            try {
                val response = api.getVendingWithStock(search = search, page = page)
                if (response.isSuccessful && response.body() != null) {
                    _vendingMachines.value = UiState.Success(response.body()!!.data)
                } else {
                    _vendingMachines.value = UiState.Error("Gagal mengambil data: ${response.code()}")
                }
            } catch (e: Exception) {
                _vendingMachines.value = UiState.Error("Failed to connect to the server")
            }
        }
    }

    fun fetchItemsByMachine(machineId: Int, search: String = "", page: Int = 1) {
        currentMachineId = machineId
        viewModelScope.launch {
            _state.update { it.copy(dataState = UiState.Loading) }
            try {
                val response = api.getItemsByMachine(id = machineId, search = search, page = page)
                if (response.isSuccessful && response.body() != null) {
                    val itemsList = response.body()!!.data
                    _state.update { it.copy(dataState = UiState.Success(itemsList)) }
                } else {
                    _state.update { it.copy(dataState = UiState.Error("Gagal mengambil data: ${response.code()}")) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(dataState = UiState.Error("Failed to connect to the server")) }
            }
        }
    }

    // TAMBAHAN: Fungsi untuk mencari master item (menggunakan Debounce 300ms agar hemat API)
    fun searchItems(query: String) {
        searchItemJob?.cancel()
        searchItemJob = viewModelScope.launch {
            _isItemLoading.value = true
            delay(300)
            try {
                // Sesuaikan nama fungsi 'getItem' atau 'getItems' sesuai dengan yang ada di ItemApi-mu
                val response = itemApi.getItem(search = query.ifBlank { null })
                if (response.isSuccessful && response.body() != null) {
                    _items.value = response.body()!!.data
                } else {
                    _items.value = emptyList()
                }
            } catch (e: Exception) {
                _items.value = emptyList()
                println("Error search item: ${e.message}")
            } finally {
                _isItemLoading.value = false
            }
        }
    }

    // ==========================================
    // FUNGSI AKSI (ASSIGN, RESTOCK, DELETE)
    // ==========================================

    fun assignItemToMachine(vendingMachineId: Int, itemId: Int, quantity: Int, capacity: Int, price: Double) {
        viewModelScope.launch {
            _state.update {
                it.copy(isActionLoading = true, actionSuccess = false, actionErrorMessage = null, fieldErrors = emptyMap())
            }
            try {
                val request = AssignItemToMachine(
                    vendingMachineId = vendingMachineId,
                    itemId = itemId,
                    quantity = quantity,
                    capacity = capacity,
                    price = price
                )

                val response = api.assignItemToMachine(request)
                if (response.isSuccessful) {
                    _state.update { it.copy(isActionLoading = false, actionSuccess = true) }
                    currentMachineId?.let { fetchItemsByMachine(it) }
                } else {
                    val errorResult = ApiErrorHandler.parseError(response, "Gagal assign item ke mesin")
                    _state.update {
                        it.copy(
                            isActionLoading = false,
                            actionErrorMessage = errorResult.generalErrorMessage,
                            fieldErrors = errorResult.fieldErrors
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(isActionLoading = false, actionErrorMessage = "Gagal terhubung ke server: ${e.localizedMessage}")
                }
            }
        }
    }

    fun restock(id: Int, quantity: Int, capacity: Int) {
        viewModelScope.launch {
            _state.update {
                it.copy(isActionLoading = true, actionSuccess = false, actionErrorMessage = null, fieldErrors = emptyMap())
            }
            try {
                val request = RestockRequest(quantity = quantity, capacity = capacity)
                val response = api.restock(id, request)

                if (response.isSuccessful) {
                    _state.update { it.copy(isActionLoading = false, actionSuccess = true) }
                    currentMachineId?.let { fetchItemsByMachine(it) }
                } else {
                    val errorResult = ApiErrorHandler.parseError(response, "Gagal melakukan restock")
                    _state.update {
                        it.copy(
                            isActionLoading = false,
                            actionErrorMessage = errorResult.generalErrorMessage,
                            fieldErrors = errorResult.fieldErrors
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(isActionLoading = false, actionErrorMessage = "Gagal terhubung ke server: ${e.localizedMessage}")
                }
            }
        }
    }

    fun removeItemFromMachine(id: String) {
        viewModelScope.launch {
            _state.update {
                it.copy(isActionLoading = true, actionSuccess = false, actionErrorMessage = null, fieldErrors = emptyMap())
            }
            try {
                val response = api.removeItemFromMachine(id)
                if (response.isSuccessful) {
                    _state.update { it.copy(isActionLoading = false, actionSuccess = true) }
                    currentMachineId?.let { fetchItemsByMachine(it) }
                } else {
                    val errorJson = response.errorBody()?.string()
                    if (errorJson != null) {
                        try {
                            val errorResponse = Gson().fromJson(errorJson, ErrorResponse::class.java)
                            _state.update {
                                it.copy(isActionLoading = false, actionErrorMessage = errorResponse.message ?: "Gagal menghapus data")
                            }
                        } catch (e: Exception) {
                            _state.update { it.copy(isActionLoading = false, actionErrorMessage = "Terjadi kesalahan membaca error.") }
                        }
                    } else {
                        _state.update { it.copy(isActionLoading = false, actionErrorMessage = "Gagal menghapus data: Format tidak valid") }
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(isActionLoading = false, actionErrorMessage = "Gagal terhubung ke server: ${e.localizedMessage}")
                }
            }
        }
    }

    // ==========================================
    // FUNGSI CLEAR ERROR
    // ==========================================

    fun clearItemIdError() {
        _state.update { it.copy(fieldErrors = it.fieldErrors - "ItemId") }
    }

    fun clearVendingMachineIdError() {
        _state.update { it.copy(fieldErrors = it.fieldErrors - "VendingMachineId") }
    }

    fun clearQuantityError() {
        _state.update { it.copy(fieldErrors = it.fieldErrors - "Quantity") }
    }

    fun clearCapacityError() {
        _state.update { it.copy(fieldErrors = it.fieldErrors - "Capacity") }
    }

    fun clearPriceError() {
        _state.update { it.copy(fieldErrors = it.fieldErrors - "Price") }
    }

    fun clearGeneralError() {
        _state.update { it.copy(actionErrorMessage = null) }
    }

    fun resetActionState() {
        _state.update { it.copy(actionSuccess = false, actionErrorMessage = null, fieldErrors = emptyMap()) }
    }
}