package com.gopro.AdminApp.model.presentation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.gopro.AdminApp.model.presentation.screens.ItemCategoriesScreen
import com.gopro.AdminApp.model.presentation.screens.PermissionCategoriesScreen
import com.gopro.AdminApp.presentation.screens.DepartmentScreen
import com.gopro.AdminApp.presentation.screens.VendingMachineScreen

fun NavGraphBuilder.masterDataGraph(navController: NavController){

    composable("department") {
        DepartmentScreen(
            onNavigateBack = {
                navController.popBackStack()
            }
        )
    }

    composable("permission_categories") {
        PermissionCategoriesScreen(
            onNavigateBack = {
                navController.popBackStack()
            }
        )
    }

    composable("item_categories"){
        ItemCategoriesScreen(
            onNavigateBack = {
                navController.popBackStack()
            }
        )
    }

    composable("vending_machine"){
        VendingMachineScreen(
            onNavigateBack = {
                navController.popBackStack()
            }
        )
    }
}