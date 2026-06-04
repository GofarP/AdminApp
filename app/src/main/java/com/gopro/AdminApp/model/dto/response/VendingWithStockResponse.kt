package com.gopro.AdminApp.model.dto.response

import com.gopro.AdminApp.model.entity.Pagination
import com.gopro.AdminApp.model.entity.VendingWithStock

data class VendingWithStockResponse(
    val data:List<VendingWithStock>,
    val pagination: Pagination
)