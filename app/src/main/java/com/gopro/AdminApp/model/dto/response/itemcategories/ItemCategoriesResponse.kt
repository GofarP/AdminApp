package com.gopro.AdminApp.model.dto.response.itemcategories

import com.gopro.AdminApp.model.entity.ItemCategories
import com.gopro.AdminApp.model.entity.Pagination

data class ItemCategoriesResponse(
    val data:List<ItemCategories>,
    val pagination: Pagination
)