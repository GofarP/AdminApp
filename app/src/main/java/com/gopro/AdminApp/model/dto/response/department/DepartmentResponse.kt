package com.gopro.AdminApp.model.dto.response.department

import com.gopro.AdminApp.model.entity.Department
import com.gopro.AdminApp.model.entity.Pagination

data class DepartmentResponse(
    val data:List<Department>,
    val pagination: Pagination
)