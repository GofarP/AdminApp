package com.gopro.AdminApp.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.gopro.AdminApp.data.local.TokenManager
import com.gopro.AdminApp.presentation.screens.DashboardScreen
import com.gopro.AdminApp.presentation.screens.DepartmentScreen
import com.gopro.AdminApp.presentation.screens.LoginScreen

@Composable
fun AppNavigation(){
    val context=LocalContext.current
    val navController=rememberNavController()
    val tokenManager=remember { TokenManager(context) }

    val forceLogout by TokenManager.forceLogoutEvent.collectAsState()

    LaunchedEffect(forceLogout) {
        if (forceLogout) {
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }
            }
            TokenManager.forceLogoutEvent.value = false
        }
    }

    val startScreen = if (tokenManager.getAccessToken() != null) {
        "dashboard"
    } else {
        "login"
    }

    NavHost(navController=navController, startDestination = startScreen){
        composable("login"){
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("dashboard"){
                        popUpTo("login"){inclusive=true}
                    }
                }
            )
        }

        composable("dashboard"){
            DashboardScreen(
                onNavigateToDepartment = {
                    navController.navigate("department")
                },
                onNavigateToLogin = {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable("department"){
            DepartmentScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}