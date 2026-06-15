package com.example.busify.features.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
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
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val loginState by viewModel.loginState
    val resetPasswordState by viewModel.resetPasswordState
    val emailVerificationState by viewModel.emailVerificationState

    LaunchedEffect(loginState) {
        when (loginState) {
            is Resource.Success -> onLoginSuccess()
            is Resource.Error -> snackbarHostState.showSnackbar(loginState?.message ?: "Error")
            else -> {}
        }
    }

    LaunchedEffect(resetPasswordState) {
        when (resetPasswordState) {
            is Resource.Success -> { showResetDialog = false; snackbarHostState.showSnackbar("Correo enviado. Revisa tu bandeja."); viewModel.clearResetPasswordState() }
            is Resource.Error -> { snackbarHostState.showSnackbar(resetPasswordState?.message ?: "Error"); viewModel.clearResetPasswordState() }
            else -> {}
        }
    }

    LaunchedEffect(emailVerificationState) {
        when (emailVerificationState) {
            is Resource.Success -> { snackbarHostState.showSnackbar("Correo de verificación enviado"); viewModel.clearEmailVerificationState() }
            is Resource.Error -> { snackbarHostState.showSnackbar(emailVerificationState?.message ?: "Error"); viewModel.clearEmailVerificationState() }
            else -> {}
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Restablecer contraseña") },
            text = {
                Column {
                    Text("Ingresa tu correo para recibir el enlace de restablecimiento.", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                    BusifyTextField(value = resetEmail, onValueChange = { resetEmail = it }, label = "Correo electrónico", leadingIcon = Icons.Default.Email)
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (resetEmail.isNotEmpty()) viewModel.resetPassword(resetEmail)
                        else scope.launch { snackbarHostState.showSnackbar("Ingresa un correo") }
                    },
                    enabled = resetPasswordState !is Resource.Loading
                ) {
                    if (resetPasswordState is Resource.Loading) CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    else Text("Enviar")
                }
            },
            dismissButton = { TextButton(onClick = { showResetDialog = false }) { Text("Cancelar") } }
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
            Spacer(modifier = Modifier.height(48.dp))

            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "B",
                    style = MaterialTheme.typography.displayLarge.copy(
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 44.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Bienvenido de vuelta",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Inicia sesión para continuar",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.height(40.dp))

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
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (passwordVisible) "Ocultar" else "Mostrar",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            )

            TextButton(
                onClick = { showResetDialog = true },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("¿Olvidaste tu contraseña?", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            }

            Spacer(modifier = Modifier.height(32.dp))

            BusifyButton(
                text = "Iniciar Sesión",
                onClick = {
                    if (email.isNotEmpty() && password.isNotEmpty()) {
                        if (!Validation.isValidEmail(email)) {
                            scope.launch { snackbarHostState.showSnackbar("Correo inválido") }; return@BusifyButton
                        }
                        viewModel.login(email, password)
                    } else {
                        scope.launch { snackbarHostState.showSnackbar("Llena todos los campos") }
                    }
                },
                loading = loginState is Resource.Loading
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "¿No tienes cuenta?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                TextButton(onClick = onNavigateToRegister) {
                    Text("Regístrate", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
