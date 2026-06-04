package com.gopro.AdminApp.model.presentation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.gopro.AdminApp.model.presentation.screens.ItemCategoriesScreen
import com.gopro.AdminApp.model.presentation.screens.PermissionCategoriesScreen
import com.gopro.AdminApp.presentation.screens.DepartmentScreen
import com.gopro.AdminApp.presentation.screens.EmployeeScreen
import com.gopro.AdminApp.presentation.screens.ItemScreen
import com.gopro.AdminApp.presentation.screens.PermissionScreen
import com.gopro.AdminApp.presentation.screens.ProfileScreen
import com.gopro.AdminApp.presentation.screens.RoleScreen
import com.gopro.AdminApp.presentation.screens.VendingItemScreen
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

    composable("permission"){
        PermissionScreen(
            onNavigateBack = {
                navController.popBackStack()
            }
        )
    }

    composable("role"){
        RoleScreen(
            onNavigateBack = {
                navController.popBackStack()
            }
        )
    }

    composable("item"){
        ItemScreen(
            onNavigateBack = {
                navController.popBackStack()
            }
        )
    }

    composable("employee"){
        EmployeeScreen(
            onNavigateBack = {
                navController.popBackStack()
            }
        )
    }

    composable("vending_item"){
        VendingItemScreen(
            onNavigateBack = {
                navController.popBackStack()
            }
        )
    }

    composable("setting"){
        ProfileScreen(
            onNavigateBack = {
                navController.popBackStack()
            }
        )
    }


}