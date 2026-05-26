package com.gopro.AdminApp.model.entity

data class Pagination(
    val totalCount: Int,
    val pageSize: Int,
    val currentPage: Int,
    val totalPages: Int
)