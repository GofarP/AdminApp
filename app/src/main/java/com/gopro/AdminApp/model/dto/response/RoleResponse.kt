package com.gopro.AdminApp.model.dto.response

import com.gopro.AdminApp.model.entity.Pagination
import com.gopro.AdminApp.model.entity.Role

data class RoleResponse(
    val data:List<Role>,
    val pagination: Pagination
)