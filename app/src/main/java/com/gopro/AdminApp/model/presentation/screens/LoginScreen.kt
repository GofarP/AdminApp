package com.gopro.AdminApp.presentation.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gopro.AdminApp.model.dto.request.auth.LoginRequest
import com.gopro.AdminApp.ui.theme.components.CustomButton
import com.gopro.AdminApp.ui.theme.components.CustomTextField
import com.gopro.AdminApp.viewmodel.auth.AuthViewModel

import androidx.compose.runtime.collectAsState

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = viewModel(),
    onLoginSuccess: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current

    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.actionSuccess) {
        if (state.actionSuccess) {
            onLoginSuccess()
        }
    }

    LaunchedEffect(state.actionErrorMessage) {
        state.actionErrorMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearGeneralError()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF4F6F9)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(75.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF0F62FE)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "V",
                        color = Color.White,
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Vendinglot", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF111827))
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "SECURE ACCESS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F62FE), letterSpacing = 1.5.sp)
                Spacer(modifier = Modifier.height(40.dp))

                val emailErrorText = state.fieldErrors["Email"] ?: ""
                val passwordErrorText = state.fieldErrors["Password"] ?: ""

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(text = "EMAIL ADDRESS", color = Color(0xFF6B7280), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    CustomTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            viewModel.clearEmailError()
                        },
                        placeholder = "your@mail.com",
                        keyboardType = KeyboardType.Email,
                        enabled = !state.isActionLoading,
                        isError = emailErrorText.isNotEmpty(),
                        errorText = emailErrorText
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(text = "PASSWORD", color = Color(0xFF6B7280), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    CustomTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            viewModel.clearPasswordError()
                        },
                        placeholder = "Masukkan Password Anda",
                        isPassword = true,
                        keyboardType = KeyboardType.Password,
                        enabled = !state.isActionLoading,
                        isError = passwordErrorText.isNotEmpty(),
                        errorText = passwordErrorText
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))

                CustomButton(
                    text = "Masuk Sekarang",
                    isLoading = state.isActionLoading,
                    onClick = {
                        val request = LoginRequest(email, password)
                        viewModel.login(request)
                    }
                )
            }
        }
    }
}