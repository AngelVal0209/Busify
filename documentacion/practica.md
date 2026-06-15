# Guía de Desarrollo y Modificación de Busify

> **Propósito:** Este documento explica cómo funciona el código internamente, cómo hacer cambios comunes, y qué necesitas saber para tu examen.

---

## Índice
1. [Estructura General y Arquitectura](#1-estructura-general-y-arquitectura)
2. [Navegación entre Pantallas](#2-navegación-entre-pantallas)
3. [Agregar una Nueva Pantalla/Vista](#3-agregar-una-nueva-pantallavista)
4. [Firebase: Firestore y Autenticación](#4-firebase-firestore-y-autenticación)
5. [Mantener Sesión Iniciada](#5-mantener-sesión-iniciada)
6. [Cambiar Colores y Tema](#6-cambiar-colores-y-tema)
7. [Agregar Nuevos Campos a un Modelo](#7-agregar-nuevos-campos-a-un-modelo)
8. [CRUD Completo](#8-crud-completo)
9. [State Management en Compose](#9-state-management-en-compose)
10. [Navegación con Parámetros](#10-navegación-con-parámetros)
11. [Snackbar vs Toast](#11-snackbar-vs-toast)
12. [Pull-to-Refresh](#12-pull-to-refresh)
13. [ExposedDropdownMenu](#13-exposeddropdownmenu)
14. [DatePicker y TimePicker](#14-datepicker-y-timepicker)
15. [Código QR](#15-código-qr)
16. [Notificaciones Push FCM](#16-notificaciones-push-fcm)
17. [Validación de Formularios](#17-validación-de-formularios)
18. [Subida de Fotos (Firebase Storage)](#18-subida-de-fotos-firebase-storage)
19. [Pantalla de Conductor](#19-pantalla-de-conductor)
20. [Firebase Cloud Functions](#20-firebase-cloud-functions)
21. [Roles y Navegación Condicional](#21-roles-y-navegación-condicional)
22. [Offline Persistence](#22-offline-persistence)

---

## 1. Estructura General y Arquitectura

Busify usa **Arquitectura MVVM** (Model-View-ViewModel) con **Jetpack Compose** para UI y **Firebase** como backend.

```
View (Composable) ← observa → ViewModel (StateFlow/State) ← llama → Repository ← Firebase
```

### Capas:
- **Model** (`domain/model/`): Data classes (User, Route, Ticket, Payment)
- **Repository** (`data/repository/`): Lógica de acceso a Firebase
- **ViewModel** (`features/*/`): Estado de la UI + lógica de negocio
- **View** (`features/*/`): Composables de Jetpack Compose

### Core:
- `core/components/`: Componentes reutilizables (BusifyButton, BusifyTextField, RouteCard, EmptyState, ErrorState, ShimmerEffect, LoadingOverlay, NetworkBanner)
- `core/navigation/`: NavGraph y Screen definitions (login reactivo en tiempo real)
- `core/theme/`: Colores, tipografía, tema (light/dark)
- `core/util/`: Resource (Success/Error/Loading), Validation

---

## 2. Navegación entre Pantallas

### Screen.kt
Define todas las rutas usando `sealed class Screen`:
```kotlin
sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Driver : Screen("driver")
    object Seats : Screen("seats/{routeId}/{company}/{origin}/{destination}/{price}/{departureTime}")
    // ...
}
```

### NavGraph.kt
Usa `NavHost` con `composable()` para cada ruta:
```kotlin
NavHost(navController, startDestination) {
    composable(Screen.Login.route) { LoginScreen(...) }
    composable(Screen.Driver.route) { MainScaffold(...) { DriverScreen() } }
}
```

### Login reactivo
El estado `currentUser` se lee directamente del ViewModel (`viewModel.currentUserData.value`) sin `remember` estático, para que la navegación reaccione al login/logout en tiempo real.

### Navegación condicional por rol
```kotlin
mutableListOf(BottomNavItem.Home, BottomNavItem.Buses).apply {
    if (userData?.role == 2L) add(BottomNavItem.Admin)
    if (userData?.role == 3L) add(BottomNavItem.Driver)
    add(BottomNavItem.Viajes)
    add(BottomNavItem.Profile)
}
```

---

## 3. Agregar una Nueva Pantalla/Vista

1. Crear `Screen.objeto` en `Screen.kt`
2. Crear el Composable en `features/mi-pantalla/`
3. Agregar `composable(Screen.MiPantalla.route) { ... }` en `NavGraph.kt`
4. Si va en bottom nav: agregar `BottomNavItem` y aplicar filtro por rol

---

## 4. Firebase: Firestore y Autenticación

### Autenticación
```kotlin
// Login
auth.signInWithEmailAndPassword(email, password).await()
// Registro
auth.createUserWithEmailAndPassword(email, password).await()
// Reset password
auth.sendPasswordResetEmail(email).await()
// Verificación
auth.currentUser?.sendEmailVerification()?.await()
// Re-autenticación
val credential = EmailAuthProvider.getCredential(email, password)
auth.currentUser?.reauthenticate(credential)?.await()
```

### Firestore
```kotlin
// Guardar
firestore.collection("users").document(uid).set(user).await()
// Leer
firestore.collection("routes").get().await()
// Actualizar campo
firestore.collection("routes").document(id).update("status", "A tiempo").await()
// Eliminar
firestore.collection("routes").document(id).delete().await()
// Operación atómica
FieldValue.increment(-count.toLong())
```

### Snapshot Listener (tiempo real)
```kotlin
firestore.collection("users").document(uid)
    .addSnapshotListener { snapshot, error -> ... }
```

---

## 5. Mantener Sesión Iniciada

Firebase Auth maneja la persistencia automáticamente. En `AuthViewModel.init()`:
```kotlin
val fbUser = repository.getCurrentUser() // No nulo si hay sesión activa
if (fbUser != null) loadUserData(fbUser.uid)
```

El `startDestination` del NavHost depende de `currentUser`:
```kotlin
val startDestination = if (currentUser != null) Screen.Home.route else Screen.Login.route
```

---

## 6. Cambiar Colores y Tema

### Color.kt
```kotlin
val Primary = Color(0xFF6366F1)    // Indigo 500
val Secondary = Color(0xFF10B981)  // Emerald 500
val Background = Color(0xFFF8FAFC) // Slate 50
```

### Theme.kt
Dos esquemas: `lightColorScheme` y `darkColorScheme`. Se seleccionan según `isSystemInDarkTheme()`.

---

## 7. Agregar Nuevos Campos a un Modelo

1. Agregar campo a la data class (ej: `val phone: String = ""`)
2. Firestore lo guarda automáticamente (NoSQL schema-less)
3. Usar `copy()` para actualizar: `user.copy(phone = "999888777")`

---

## 8. CRUD Completo

```kotlin
// CREATE
val docRef = firestore.collection("routes").document()
val routeWithId = route.copy(id = docRef.id)
docRef.set(routeWithId).await()

// READ
val snapshot = firestore.collection("routes").get().await()
val routes = snapshot.toObjects(Route::class.java)

// UPDATE
firestore.collection("routes").document(route.id).set(route).await()
// o update parcial:
firestore.collection("routes").document(id).update("status", "Nuevo").await()

// DELETE
firestore.collection("routes").document(routeId).delete().await()
```

---

## 9. State Management en Compose

### ViewModel
```kotlin
private val _state = mutableStateOf<Resource<List<Route>>>(Resource.Loading())
val state: State<Resource<List<Route>>> = _state
```

### Resource sealed class
```kotlin
sealed class Resource<T> {
    class Success<T>(data: T) : Resource<T>(data)
    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)
    class Loading<T>(data: T? = null) : Resource<T>(data)
}
```

### En el Composable
```kotlin
when (val s = viewModel.state) {
    is Resource.Loading -> CircularProgressIndicator()
    is Resource.Success -> MostrarDatos(s.data!!)
    is Resource.Error -> Text("Error: ${s.message}")
}
```

---

## 10. Navegación con Parámetros

```kotlin
// Definición
object Seats : Screen("seats/{routeId}/{company}/{origin}/{destination}/{price}/{departureTime}")

// Navegar
navController.navigate("seats/$routeId/$company/$origin/$destination/$price/$departureTime")

// Recibir
composable(route = Screen.Seats.route) {
    val routeId = it.arguments?.getString("routeId") ?: ""
    SeatSelectionScreen(routeId = routeId, ...)
}
```

---

## 11. Snackbar vs Toast

Todos los mensajes usan `SnackbarHost` (Material3):

```kotlin
val snackbarHostState = remember { SnackbarHostState() }
val scope = rememberCoroutineScope()

Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { ... }

// Mostrar mensaje
scope.launch { snackbarHostState.showSnackbar("Mensaje") }
```

No se usa `Toast` en ninguna parte.

---

## 12. Pull-to-Refresh

```kotlin
val pullRefreshState = rememberPullToRefreshState()
PullToRefreshBox(
    isRefreshing = isRefreshing,
    onRefresh = { viewModel.loadData() },
    state = pullRefreshState
) {
    LazyColumn { ... }
}
```

---

## 13. ExposedDropdownMenu

```kotlin
var expanded by remember { mutableStateOf(false) }
ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
    OutlinedTextField(
        value = selectedValue,
        onValueChange = {},
        readOnly = true,
        modifier = Modifier.menuAnchor(),
        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
    )
    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
        options.forEach { option ->
            DropdownMenuItem(text = { Text(option) }, onClick = { selectedValue = option; expanded = false })
        }
    }
}
```

---

## 14. DatePicker y TimePicker

```kotlin
// DatePicker
val dateState = rememberDatePickerState()
DatePickerDialog(
    onDismissRequest = { },
    confirmButton = { TextButton(onClick = { dateState.selectedDateMillis?.let { ... } }) { Text("OK") } },
    dismissButton = { TextButton(onClick = { }) { Text("Cancelar") } }
) { DatePicker(state = dateState) }

// TimePicker
val timeState = rememberTimePickerState(initialHour = 8, initialMinute = 0, is24Hour = true)
AlertDialog(
    text = { TimePicker(state = timeState) },
    confirmButton = { TextButton(onClick = { onTimeSelected(timeState.hour, timeState.minute) }) { Text("OK") } }
)
```

---

## 15. Código QR

Generación con ZXing:
```kotlin
val writer = QRCodeWriter()
val bitMatrix = writer.encode("Contenido del QR", BarcodeFormat.QR_CODE, 512, 512)
val bitmap = Bitmap.createBitmap(512, 512, Bitmap.Config.RGB_565)
// pixel por pixel...
```

---

## 16. Notificaciones Push FCM

### BusifyMessagingService
```kotlin
class BusifyMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        // Auto-guarda token en Firestore: users/{uid}/fcmToken
    }
    override fun onMessageReceived(message: RemoteMessage) {
        showNotification(title, body)
    }
}
```

### Manifest
```xml
<service android:name=".core.fcm.BusifyMessagingService" android:exported="false">
    <intent-filter><action android:name="com.google.firebase.MESSAGING_EVENT" /></intent-filter>
</service>
```

---

## 17. Validación de Formularios

```kotlin
object Validation {
    fun isValidEmail(email: String): Boolean
    fun isValidPassword(password: String): ValidationResult
    fun isValidName(name: String): Boolean
}
data class ValidationResult(val isValid: Boolean, val errorMessage: String? = null)
```

Usado en LoginScreen y RegisterScreen para validar antes de enviar.

---

## 18. Subida de Fotos (Firebase Storage)

```kotlin
val storageRef = FirebaseStorage.getInstance()
    .reference.child("profile_photos/${uid}.jpg")
storageRef.putFile(uri).await()
val downloadUrl = storageRef.downloadUrl.await()
// Guardar downloadUrl en Firestore: users/{uid}/photoUrl
```

Mostrar con Coil:
```kotlin
AsyncImage(model = user.photoUrl, contentDescription = "Foto")
```

---

## 19. Pantalla de Conductor

- Acceso: role == 3L
- Bottom nav item "Conducir" con icono SteeringWheel
- Muestra rutas donde `driverId == currentUser.uid`
- Al seleccionar ruta: lista de pasajeros con sus tickets
- Botón "Usado" → cambia `ticket.status` a "usado"

---

## 20. Firebase Cloud Functions

### functions/index.js
```javascript
exports.onTicketCreated = functions.firestore
    .document("tickets/{ticketId}").onCreate(async (snap) => { ... });

exports.onRouteUpdated = functions.firestore
    .document("routes/{routeId}").onUpdate(async (change) => { ... });

exports.onRouteCreated = functions.firestore
    .document("routes/{routeId}").onCreate(async (snap) => { ... });
```

### Despliegue
```bash
cd functions
npm install
firebase deploy --only functions
```

---

## 21. Roles y Navegación Condicional

Tres roles:
| Role | Valor | Acceso |
|------|-------|--------|
| Usuario | 1L | Home, Buses, Viajes, Perfil |
| Administrador | 2L | + Admin tab |
| Chofer | 3L | + Conducir tab |

En `MainScaffold` se filtran los items del bottom nav según el rol.

---

## 22. Offline Persistence

```kotlin
FirebaseFirestore.getInstance().firestoreSettings = FirebaseFirestoreSettings.Builder()
    .setPersistenceEnabled(true)
    .build()
```

Habilitado en `MainActivity.onCreate()`. Permite que Firestore funcione sin conexión a internet (caché local).
