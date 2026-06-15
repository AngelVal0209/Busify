package com.example.busify.features.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.busify.core.components.BusifyButton
import com.example.busify.core.components.BusifyTextField
import com.example.busify.core.util.Resource
import com.example.busify.core.util.Validation
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onNavigateBack: () -> Unit,
    viewModel: AuthViewModel
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val registerState by viewModel.registerState
    val emailVerificationState by viewModel.emailVerificationState
    var verificationSent by remember { mutableStateOf(false) }

    LaunchedEffect(registerState) {
        when (registerState) {
            is Resource.Success -> {
                verificationSent = true
                snackbarHostState.showSnackbar("Cuenta creada. Revisa tu correo para verificar tu cuenta antes de iniciar sesión.")
            }
            is Resource.Error -> {
                snackbarHostState.showSnackbar(registerState?.message ?: "Error")
            }
            else -> {}
        }
    }

    LaunchedEffect(emailVerificationState) {
        when (emailVerificationState) {
            is Resource.Success -> {
                if (verificationSent) {
                    onNavigateBack()
                }
                viewModel.clearEmailVerificationState()
            }
            is Resource.Error -> {
                snackbarHostState.showSnackbar(emailVerificationState?.message ?: "Error al enviar verificación")
                viewModel.clearEmailVerificationState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Crea tu cuenta",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Únete a la comunidad de Busify hoy mismo.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(40.dp))

            BusifyTextField(
                value = name,
                onValueChange = { name = it },
                label = "Nombre completo",
                leadingIcon = Icons.Default.Person
            )

            Spacer(modifier = Modifier.height(16.dp))

            BusifyTextField(
                value = email,
                onValueChange = { email = it },
                label = "Correo electrónico",
                leadingIcon = Icons.Default.Email
            )

            Spacer(modifier = Modifier.height(16.dp))

            BusifyTextField(
                value = password,
                onValueChange = { password = it },
                label = "Contraseña",
                leadingIcon = Icons.Default.Lock,
                visualTransformation = PasswordVisualTransformation()
            )

            Spacer(modifier = Modifier.height(16.dp))

            BusifyTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = "Confirmar contraseña",
                leadingIcon = Icons.Default.Lock,
                visualTransformation = PasswordVisualTransformation()
            )

            Spacer(modifier = Modifier.height(40.dp))

            if (registerState is Resource.Loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                BusifyButton(
                    text = "Crear Cuenta",
                    onClick = {
                        if (name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty()) {
                            if (!Validation.isValidName(name)) {
                                scope.launch {
                                    snackbarHostState.showSnackbar("El nombre debe tener al menos 2 caracteres")
                                }
                                return@BusifyButton
                            }
                            if (!Validation.isValidEmail(email)) {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Formato de correo electrónico inválido")
                                }
                                return@BusifyButton
                            }
                            val passwordValidation = Validation.isValidPassword(password)
                            if (!passwordValidation.isValid) {
                                scope.launch {
                                    snackbarHostState.showSnackbar(passwordValidation.errorMessage ?: "Contraseña inválida")
                                }
                                return@BusifyButton
                            }
                            if (password != confirmPassword) {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Las contraseñas no coinciden")
                                }
                                return@BusifyButton
                            }
                            viewModel.register(name, email, password)
                        } else {
                            scope.launch {
                                snackbarHostState.showSnackbar("Por favor llena todos los campos")
                            }
                        }
                    }
                )
            }

            if (verificationSent) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Se ha enviado un correo de verificación a $email. Por favor verifica tu cuenta antes de iniciar sesión.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
