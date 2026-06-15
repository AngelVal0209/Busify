package com.example.busify.features.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.busify.core.components.BusifyButton
import com.example.busify.core.components.BusifyTextField
import com.example.busify.core.util.Resource
import com.example.busify.core.util.Validation
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    viewModel: AuthViewModel
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var resetEmail by remember { mutableStateOf("") }
    var showVerifyButton by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val loginState by viewModel.loginState
    val resetPasswordState by viewModel.resetPasswordState
    val emailVerificationState by viewModel.emailVerificationState

    LaunchedEffect(loginState) {
        when (loginState) {
            is Resource.Success -> {
                if (viewModel.isEmailVerified()) {
                    onLoginSuccess()
                } else {
                    showVerifyButton = true
                    snackbarHostState.showSnackbar("Por favor verifica tu correo electrónico antes de continuar")
                }
            }
            is Resource.Error -> {
                snackbarHostState.showSnackbar(loginState?.message ?: "Error")
            }
            else -> {}
        }
    }

    LaunchedEffect(resetPasswordState) {
        when (resetPasswordState) {
            is Resource.Success -> {
                showResetDialog = false
                snackbarHostState.showSnackbar("Correo de restablecimiento enviado. Revisa tu bandeja de entrada.")
                viewModel.clearResetPasswordState()
            }
            is Resource.Error -> {
                snackbarHostState.showSnackbar(resetPasswordState?.message ?: "Error al enviar correo")
                viewModel.clearResetPasswordState()
            }
            else -> {}
        }
    }

    LaunchedEffect(emailVerificationState) {
        when (emailVerificationState) {
            is Resource.Success -> {
                snackbarHostState.showSnackbar("Correo de verificación enviado. Revisa tu bandeja de entrada.")
                viewModel.clearEmailVerificationState()
            }
            is Resource.Error -> {
                snackbarHostState.showSnackbar(emailVerificationState?.message ?: "Error al enviar verificación")
                viewModel.clearEmailVerificationState()
            }
            else -> {}
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Restablecer contraseña") },
            text = {
                Column {
                    Text("Ingresa tu correo electrónico para recibir un enlace de restablecimiento.")
                    Spacer(modifier = Modifier.height(16.dp))
                    BusifyTextField(
                        value = resetEmail,
                        onValueChange = { resetEmail = it },
                        label = "Correo electrónico",
                        leadingIcon = Icons.Default.Email
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (resetEmail.isNotEmpty()) {
                            viewModel.resetPassword(resetEmail)
                        } else {
                            scope.launch {
                                snackbarHostState.showSnackbar("Ingresa un correo electrónico")
                            }
                        }
                    },
                    enabled = resetPasswordState !is Resource.Loading
                ) {
                    if (resetPasswordState is Resource.Loading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    } else {
                        Text("Enviar")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(MaterialTheme.colorScheme.primary, shape = MaterialTheme.shapes.large),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "B",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        color = Color.White,
                        fontSize = 48.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Bienvenido a Busify",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = "Tu transporte, simplificado",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(48.dp))

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
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = null)
                    }
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = { showResetDialog = true },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("¿Olvidaste tu contraseña?", style = MaterialTheme.typography.labelMedium)
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (loginState is Resource.Loading) {
                CircularProgressIndicator()
            } else {
                BusifyButton(
                    text = "Iniciar Sesión",
                    onClick = {
                        if (email.isNotEmpty() && password.isNotEmpty()) {
                            if (!Validation.isValidEmail(email)) {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Formato de correo electrónico inválido")
                                }
                                return@BusifyButton
                            }
                            showVerifyButton = false
                            viewModel.login(email, password)
                        } else {
                            scope.launch {
                                snackbarHostState.showSnackbar("Por favor llena todos los campos")
                            }
                        }
                    }
                )
            }

            if (showVerifyButton) {
                Spacer(modifier = Modifier.height(12.dp))
                BusifyButton(
                    text = "Verificar Email",
                    onClick = { viewModel.sendEmailVerification() },
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "¿No tienes una cuenta?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
                TextButton(onClick = onNavigateToRegister) {
                    Text(
                        text = "Regístrate",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
        }
    }
}
