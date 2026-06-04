package com.gopro.AdminApp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.gopro.AdminApp.model.presentation.screens.NoInternetScreen
import com.gopro.AdminApp.network.RetrofitClient
import com.gopro.AdminApp.presentation.AppNavigation
import com.gopro.AdminApp.ui.theme.AdminAppTheme
import com.gopro.AdminApp.ui.theme.Background
import com.gopro.AdminApp.utils.NetworkObserver
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        RetrofitClient.init(applicationContext)
        setContent {
            AdminAppTheme{
                val context= LocalContext.current
                val networkObserver = remember { NetworkObserver(context) }
                val isConnected by networkObserver.isConnected.collectAsState(initial=true)

                val snackbarHostState=remember { SnackbarHostState() }

                val coroutineScope = rememberCoroutineScope()

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Background
                ) {
                    Box(modifier= Modifier.fillMaxSize()){
                        if(!isConnected){
                            NoInternetScreen(
                                modifier = Modifier.fillMaxSize(),
                                onRetry = {
                                    coroutineScope.launch {
                                        snackbarHostState.currentSnackbarData?.dismiss()

                                        snackbarHostState.showSnackbar(
                                            message = "Tidak ada koneksi internet. Mengecek ulang...",
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                }
                            )
                        }else{
                            AppNavigation()
                        }

                        SnackbarHost(
                            hostState = snackbarHostState,
                            modifier = Modifier.align(Alignment.BottomCenter)
                        )
                    }

                }



            }
        }
    }
}