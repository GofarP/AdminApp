package com.gopro.AdminApp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gopro.AdminApp.model.LoginRequest
import com.gopro.AdminApp.ui.theme.AdminAppTheme
import com.gopro.AdminApp.ui.theme.components.CustomButton
import com.gopro.AdminApp.ui.theme.components.CustomTextField
import com.gopro.AdminApp.viewmodel.AuthViewModel

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AdminAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    LoginScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val context = LocalContext.current

    LaunchedEffect(viewModel.loginSuccess) {
        if (viewModel.loginSuccess) {
            val intent = Intent(context, MainActivity::class.java)
            context.startActivity(intent)
            (context as? ComponentActivity)?.finish()
        }
    }

    LaunchedEffect(viewModel.errorMessage) {
        viewModel.errorMessage?.let { msg ->
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

                Text(
                    text = "Vendinglot",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF111827)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "SECURE ACCESS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F62FE),
                    letterSpacing = 1.5.sp
                )

                Spacer(modifier = Modifier.height(40.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "EMAIL ADDRESS",
                        color = Color(0xFF6B7280),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    CustomTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            viewModel.clearEmailError()
                        },
                        placeholder = "your@mail.com",
                        keyboardType = KeyboardType.Email,
                        enabled = !viewModel.isLoading,
                        isError = viewModel.emailError.isNotEmpty(), // Menyala merah jika ada error
                        errorText = viewModel.emailError // Menerima teks pesan gagal validasi dari server
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "PASSWORD",
                        color = Color(0xFF6B7280),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    CustomTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            viewModel.clearPasswordError() // Menghapus pesan error spesifik saat mulai ngetik ulang
                        },
                        placeholder = "Masukkan Password Anda",
                        isPassword = true,
                        keyboardType = KeyboardType.Password,
                        enabled = !viewModel.isLoading,
                        isError = viewModel.passwordError.isNotEmpty(),
                        errorText = viewModel.passwordError // Menampilkan "Password wajib diisi." dari server
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))

                CustomButton(
                    text = "Masuk Sekarang",
                    isLoading = viewModel.isLoading,
                    onClick = {
                        val request = LoginRequest(email, password)
                        viewModel.login(request)
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    AdminAppTheme {
        LoginScreen()
    }
}