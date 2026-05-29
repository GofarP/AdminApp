package com.gopro.AdminApp.model.dto.response.permissioncategories

import com.gopro.AdminApp.model.entity.Pagination
import com.gopro.AdminApp.model.entity.PermissionCategories

data class PermissionCategoryResponse(
    val data:List<PermissionCategories>,
    val pagination: Pagination
)