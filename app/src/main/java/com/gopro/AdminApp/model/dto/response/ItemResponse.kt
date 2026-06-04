package com.gopro.AdminApp.model.dto.response

import com.gopro.AdminApp.model.entity.Item
import com.gopro.AdminApp.model.entity.Pagination

data class ItemResponse(
    val data:List<Item>,
    val pagination: Pagination
)