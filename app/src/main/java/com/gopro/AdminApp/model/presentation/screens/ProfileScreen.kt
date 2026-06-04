package com.gopro.AdminApp.presentation.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.gopro.AdminApp.BuildConfig
import com.gopro.AdminApp.model.dto.request.PasswordUpdateRequest
import com.gopro.AdminApp.presentation.components.FormBottomSheet
import com.gopro.AdminApp.presentation.state.UiState
import com.gopro.AdminApp.ui.theme.Background
import com.gopro.AdminApp.ui.theme.components.*
import com.gopro.AdminApp.viewmodel.ProfileViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val context = LocalContext.current

    val state by viewModel.state.collectAsState()
    val photoUrlFromStore by viewModel.userPhoto.collectAsState()

    val profileResponseData = (state.dataState as? UiState.Success)?.data

    val rawPhotoPath = profileResponseData?.photoUrl ?: photoUrlFromStore

    val currentPhotoUrl = rawPhotoPath?.let { path ->
        if (path.startsWith("http")) {
            path
        } else {
            val cleanPath = path.removePrefix("/")
            "${BuildConfig.BASE_URL}uploads/users/$cleanPath"
        }
    } ?: ""

    var editFullName by remember(profileResponseData) { mutableStateOf(profileResponseData?.fullName ?: "") }
    var editEmail by remember(profileResponseData) { mutableStateOf(profileResponseData?.email ?: "") }
    var editPhotoFile by remember { mutableStateOf<File?>(null) }

    var showPasswordSheet by remember { mutableStateOf(false) }
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordMismatchError by remember { mutableStateOf(false) }

    var isRefreshing by remember { mutableStateOf(false) }

    val fullNameError = state.fieldErrors["FullName"] ?: ""
    val emailError = state.fieldErrors["Email"] ?: ""
    val oldPasswordError = state.fieldErrors["OldPassword"] ?: ""
    val newPasswordError = state.fieldErrors["NewPassword"] ?: ""

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            editPhotoFile = uriToFile(context, it)
        }
    }

    LaunchedEffect(state.dataState) {
        if (state.dataState !is UiState.Loading) {
            isRefreshing = false
        }
    }

    LaunchedEffect(state.actionSuccess) {
        if (state.actionSuccess) {
            showPasswordSheet = false
            oldPassword = ""
            newPassword = ""
            confirmPassword = ""
            editPhotoFile = null

            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    CustomSnackbarVisuals(
                        message = "Profil berhasil diperbarui",
                        type = SnackbarType.SUCCESS,
                        duration = SnackbarDuration.Short
                    )
                )
            }
            viewModel.resetActionState()
        }
    }

    LaunchedEffect(state.actionErrorMessage) {
        state.actionErrorMessage?.let { msg ->
            viewModel.clearGeneralError()
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    CustomSnackbarVisuals(
                        message = msg,
                        type = SnackbarType.ERROR,
                        duration = SnackbarDuration.Short
                    )
                )
            }
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                CustomSnackBar(snackbarData = data)
            }
        },
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text("Profil Saya", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        }
    ) { innerPadding ->

        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                viewModel.fetchProfile()
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray)
                        .clickable(enabled = !state.isActionLoading) {
                            photoPickerLauncher.launch("image/*")
                        },
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        editPhotoFile != null -> {
                            AsyncImage(
                                model = editPhotoFile,
                                contentDescription = "Selected Photo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        currentPhotoUrl.isNotEmpty() -> {
                            AsyncImage(
                                model = currentPhotoUrl,
                                contentDescription = "Profile Photo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        else -> {
                            Icon(
                                imageVector = Icons.Outlined.Person,
                                contentDescription = "Placeholder",
                                modifier = Modifier.size(60.dp),
                                tint = Color.Gray
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Ketuk untuk ganti foto",
                    fontSize = 12.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(32.dp))

                CustomTextField(
                    value = editFullName,
                    onValueChange = {
                        editFullName = it
                        viewModel.clearFullNameError()
                    },
                    placeholder = "Nama Lengkap",
                    enabled = !state.isActionLoading,
                    isError = fullNameError.isNotEmpty(),
                    errorText = fullNameError
                )

                Spacer(modifier = Modifier.height(16.dp))

                CustomTextField(
                    value = editEmail,
                    onValueChange = {
                        editEmail = it
                        viewModel.clearEmailError()
                    },
                    placeholder = "Email",
                    enabled = !state.isActionLoading,
                    isError = emailError.isNotEmpty(),
                    errorText = emailError
                )

                Spacer(modifier = Modifier.height(32.dp))

                CustomButton(
                    text = if (state.isActionLoading) "Menyimpan..." else "Simpan Profil",
                    colorType = ButtonColorType.PRIMARY,
                    onClick = {
                        viewModel.updateProfile(
                            fullName = editFullName,
                            email = editEmail,
                            photoFile = editPhotoFile
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isActionLoading
                )

                Spacer(modifier = Modifier.height(32.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showPasswordSheet = true },
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Lock,
                            contentDescription = "Lock",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Ubah Password", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("Perbarui kata sandi untuk keamanan akun Anda", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }
            }
        }
    }

    if (showPasswordSheet) {
        FormBottomSheet(
            title = "Ubah Password",
            isLoading = state.isActionLoading,
            onDismiss = {
                showPasswordSheet = false
                viewModel.resetActionState()
                oldPassword = ""
                newPassword = ""
                confirmPassword = ""
                passwordMismatchError = false
            },
            onSave = {
                if (newPassword != confirmPassword) {
                    passwordMismatchError = true
                } else {
                    val request = PasswordUpdateRequest(
                        oldPassword = oldPassword,
                        newPassword = newPassword,
                        confirmPassword = confirmPassword
                    )
                    viewModel.updatePassword(request)
                }
            }
        ) {
            CustomTextField(
                value = oldPassword,
                onValueChange = {
                    oldPassword = it
                    viewModel.clearOldPasswordError()
                },
                keyboardType = KeyboardType.Password,
                placeholder = "Password Lama",
                singleLine = true,
                isPassword = true,
                enabled = !state.isActionLoading,
                isError = oldPasswordError.isNotEmpty(),
                errorText = oldPasswordError
            )

            Spacer(modifier = Modifier.height(16.dp))

            CustomTextField(
                value = newPassword,
                keyboardType = KeyboardType.Password,

                onValueChange = {
                    newPassword = it
                    viewModel.clearNewPasswordError()
                    passwordMismatchError = false
                },
                placeholder = "Password Baru",
                singleLine = true,
                isPassword = true,
                enabled = !state.isActionLoading,
                isError = newPasswordError.isNotEmpty(),
                errorText = newPasswordError
            )

            Spacer(modifier = Modifier.height(16.dp))

            CustomTextField(
                value = confirmPassword,
                keyboardType = KeyboardType.Password,
                isPassword = true,
                onValueChange = {
                    confirmPassword = it
                    passwordMismatchError = false
                },
                placeholder = "Konfirmasi Password Baru",
                singleLine = true,
                enabled = !state.isActionLoading,
                isError = passwordMismatchError,
                errorText = if (passwordMismatchError) "Password tidak cocok" else ""
            )
        }
    }
}

private fun uriToFile(context: Context, uri: Uri): File? {
    val contentResolver = context.contentResolver
    val tempFile = File.createTempFile("profile_", ".jpg", context.cacheDir)
    return try {
        contentResolver.openInputStream(uri)?.use { inputStream: InputStream ->
            FileOutputStream(tempFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        tempFile
    } catch (e: Exception) {
        null
    }
}