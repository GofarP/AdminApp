package com.gopro.AdminApp.model.dto.response

import com.gopro.AdminApp.model.entity.Pagination
import com.gopro.AdminApp.model.entity.Permission

data class  PermissionResponse(
    val data:List<Permission>,
    val pagination: Pagination
)