package com.gopro.AdminApp.model.dto.response.vendingmachine

import com.gopro.AdminApp.model.entity.Pagination
import com.gopro.AdminApp.model.entity.VendingMachine

data class VendingMachineResponse(
    val data:List<VendingMachine>,
    val pagination: Pagination
)