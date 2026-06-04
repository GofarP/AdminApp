package com.gopro.AdminApp.model.dto.response

import com.gopro.AdminApp.model.entity.ItemsByMachine
import com.gopro.AdminApp.model.entity.Pagination

data class ItemsByMachineResponse(
    val data:List<ItemsByMachine>,
    val pagination: Pagination
)