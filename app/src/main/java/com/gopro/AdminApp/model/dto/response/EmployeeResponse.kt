package com.gopro.AdminApp.model.dto.response

import com.gopro.AdminApp.model.entity.Employee
import com.gopro.AdminApp.model.entity.Pagination

data class EmployeeResponse(
    val data:List<Employee>,
    val pagination: Pagination
)