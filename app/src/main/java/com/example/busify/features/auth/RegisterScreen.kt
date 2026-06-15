package com.example.busify.features.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

    LaunchedEffect(registerState) {
        when (registerState) {
            is Resource.Success -> {
                snackbarHostState.showSnackbar("Cuenta creada correctamente")
                onNavigateBack()
            }
            is Resource.Error -> snackbarHostState.showSnackbar(registerState?.message ?: "Error")
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
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
            Spacer(modifier = Modifier.height(8.dp))

            Text(text = "Crear cuenta", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(text = "Únete a Busify y viaja seguro.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))

            Spacer(modifier = Modifier.height(32.dp))

            BusifyTextField(value = name, onValueChange = { name = it }, label = "Nombre completo", leadingIcon = Icons.Default.Person)
            Spacer(modifier = Modifier.height(16.dp))
            BusifyTextField(value = email, onValueChange = { email = it }, label = "Correo electrónico", leadingIcon = Icons.Default.Email)
            Spacer(modifier = Modifier.height(16.dp))
            BusifyTextField(value = password, onValueChange = { password = it }, label = "Contraseña", leadingIcon = Icons.Default.Lock, visualTransformation = PasswordVisualTransformation())
            Spacer(modifier = Modifier.height(16.dp))
            BusifyTextField(value = confirmPassword, onValueChange = { confirmPassword = it }, label = "Confirmar contraseña", leadingIcon = Icons.Default.Lock, visualTransformation = PasswordVisualTransformation())

            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "La contraseña debe tener al menos 8 caracteres, una mayúscula y un número.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))

            Spacer(modifier = Modifier.height(32.dp))

            BusifyButton(
                text = "Crear Cuenta",
                onClick = {
                    if (name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty()) {
                        if (!Validation.isValidName(name)) { scope.launch { snackbarHostState.showSnackbar("El nombre debe tener al menos 2 caracteres") }; return@BusifyButton }
                        if (!Validation.isValidEmail(email)) { scope.launch { snackbarHostState.showSnackbar("Correo inválido") }; return@BusifyButton }
                        val passwordValidation = Validation.isValidPassword(password)
                        if (!passwordValidation.isValid) { scope.launch { snackbarHostState.showSnackbar(passwordValidation.errorMessage ?: "Contraseña inválida") }; return@BusifyButton }
                        if (password != confirmPassword) { scope.launch { snackbarHostState.showSnackbar("Las contraseñas no coinciden") }; return@BusifyButton }
                        viewModel.register(name, email, password)
                    } else { scope.launch { snackbarHostState.showSnackbar("Llena todos los campos") } }
                },
                loading = registerState is Resource.Loading
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
