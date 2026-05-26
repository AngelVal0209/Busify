# Guía de Desarrollo y Modificación de Busify

> **Propósito:** Este documento explica cómo funciona el código internamente, cómo hacer cambios comunes, y qué necesitas saber para tu examen.

---

## Índice
1. [Estructura General y Arquitectura](#1-estructura-general-y-arquitectura)
2. [Navegación entre Pantallas](#2-navegación-entre-pantallas)
3. [Agregar una Nueva Pantalla/Vista](#3-agregar-una-nueva-pantallavista)
4. [Firebase: Firestore y Autenticación](#4-firebase-firestore-y-autenticación)
5. [Mantener Sesión Iniciada (No cerrar al salir)](#5-mantener-sesión-iniciada-no-cerrar-al-salir)
6. [Cambiar Colores y Tema](#6-cambiar-colores-y-tema)
7. [Agregar Nuevos Campos a un Modelo](#7-agregar-nuevos-campos-a-un-modelo)
8. [CRUD Completo: Crear, Leer, Actualizar, Eliminar](#8-crud-completo-crear-leer-actualizar-eliminar)
9. [State Management en Compose](#9-state-management-en-compose)
10. [Navegación con Parámetros](#10-navegación-con-parámetros)
11. [Snackbar vs Toast](#11-snackbar-vs-toast)
12. [Pull-to-Refresh](#12-pull-to-refresh)
13. [ExposedDropdownMenu (Dropdowns)](#13-exposeddropdownmenu-dropdowns)
14. [DatePicker y TimePicker](#14-datepicker-y-timepicker)
15. [Código QR](#15-código-qr)
16. [Notificaciones Push FCM](#16-notificaciones-push-fcm)
17. [Optimización de UI para Mejor Rendimiento](#17-optimización-de-ui-para-mejor-rendimiento)
18. [Preguntas Típicas de Examen (Avanzadas)](#18-preguntas-típicas-de-examen-avanzadas)
19. [Errores Comunes y Soluciones](#errores-comunes-y-soluciones)
20. [Glosario Rápido (Ampliado)](#glosario-rápido-ampliado)

---

## 1. Estructura General y Arquitectura

Busify usa **Arquitectura MVVM** (Model-View-ViewModel) con **Jetpack Compose** para UI y **Firebase** como backend.

```
Capa       │ Componentes                     │ Rol
───────────┼─────────────────────────────────┼────────────────────────────
View       │ *Screen.kt (Composables)        │ Interfaz de usuario
ViewModel  │ *ViewModel.kt                   │ Estado y lógica de negocio
Model      │ domain/model/*.kt               │ Datos (Route, Ticket, etc.)
Repository │ data/repository/*.kt            │ Comunicación con Firebase
```

### Flujo típico:

```
Usuario toca botón
  → Screen llama a ViewModel.metodo()
    → ViewModel lanza corrutina
      → Repository llama a Firebase
        → Firestore devuelve datos
      → ViewModel actualiza State
    → Compose re-renderiza la UI automáticamente
```

### Ejemplo concreto (crear ruta):

```
AdminScreen: usuario llena formulario y toca "Crear Ruta"
  → AdminScreen llama a viewModel.submitRoute()
    → AdminViewModel.submitRoute():
      → Valida campos (validateAll())
      → Crea objeto Route
      → repository.createRoute(route)
        → RouteRepository.createRoute():
          → firestore.collection("routes").document().set(route)
      → Actualiza _createRouteState
  → AdminScreen observa createRouteState con LaunchedEffect
    → Muestra Snackbar de éxito/error
    → Limpia formulario
```

---

## 2. Navegación entre Pantallas

### Archivos clave:
- `core/navigation/Screen.kt` → Define las rutas (URLs de navegación)
- `core/navigation/NavGraph.kt` → Conecta rutas con composables

### Sistema de navegación:

**Screen.kt** define rutas con parámetros:
```kotlin
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Buses : Screen("buses")
    object Admin : Screen("admin")
    
    // Rutas con parámetros:
    object Seats : Screen("seats/{routeId}/{company}/{origin}/{destination}/{price}/{departureTime}")
    object Payment : Screen("payment/{routeId}/{company}/{origin}/{destination}/{seats}/{price}/{departureTime}")
}
```

**NavGraph.kt** conecta rutas a composables:
```kotlin
composable(route = Screen.Seats.route) {
    SeatSelectionScreen(
        navController = navController,
        routeId = it.arguments?.getString("routeId") ?: "",
        company = it.arguments?.getString("company") ?: "",
        // ...
    )
}
```

**Navegar a una pantalla:**
```kotlin
navController.navigate("seats/abc123/CruzDelSur/Lima/Arequipa/70.0/14:30")
```

**Navegar y limpiar backstack:**
```kotlin
navController.navigate("home") {
    popUpTo("login") { inclusive = true }  // Elimina login del stack
    launchSingleTop = true                  // No duplica pantallas
}
```

### Bottom Navigation:

En `MainScaffold` (NavGraph.kt), se construye la barra inferior:
```kotlin
val items = mutableListOf(
    BottomNavItem.Home,
    BottomNavItem.Buses
).apply {
    if (userData?.role == 2) add(BottomNavItem.Admin)  // Solo admins
    add(BottomNavItem.Viajes)
    add(BottomNavItem.Profile)
}
```

---

## 3. Agregar una Nueva Pantalla/Vista

### Paso a paso:

**1. Crear el Screen (ruta)** en `core/navigation/Screen.kt`:
```kotlin
object NuevaPantalla : Screen("nueva-pantalla")
// Con parámetros:
object NuevaPantalla : Screen("nueva-pantalla/{param1}/{param2}")
```

**2. Crear el archivo de la pantalla** en `features/mimodulo/MiPantalla.kt`:
```kotlin
@Composable
fun MiPantalla(
    navController: NavController,
    param1: String,
    viewModel: MiViewModel = viewModel()
) {
    // UI aquí
}
```

**3. (Opcional) Crear ViewModel** en `features/mimodulo/MiViewModel.kt`:
```kotlin
class MiViewModel : ViewModel() {
    private val _state = mutableStateOf(...)
    val state: State<...> = _state
    
    fun hacerAlgo() {
        viewModelScope.launch { ... }
    }
}
```

**4. Registrar en NavGraph.kt**:
```kotlin
// En el NavHost:
composable(route = Screen.NuevaPantalla.route) {
    MiPantalla(
        navController = navController,
        param1 = it.arguments?.getString("param1") ?: ""
    )
}
```

**5. Navegar a la nueva pantalla**:
```kotlin
navController.navigate("nueva-pantalla/valor1/valor2")
```

### Ejemplo: Agregar pantalla "Acerca de"

```kotlin
// 1. Screen.kt
object About : Screen("about")

// 2. features/about/AboutScreen.kt
@Composable
fun AboutScreen(navController: NavController) {
    Scaffold(topBar = {
        TopAppBar(title = { Text("Acerca de") },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                }
            })
    }) { padding ->
        Column(Modifier.padding(padding).padding(24.dp)) {
            Text("Busify v1.0", style = MaterialTheme.typography.headlineMedium)
            Text("App de transporte interprovincial")
        }
    }
}

// 3. NavGraph.kt
composable(Screen.About.route) {
    AboutScreen(navController)
}

// 4. Navegar desde cualquier lado:
navController.navigate("about")
```

---

## 4. Firebase: Firestore y Autenticación

### Inicialización

Firebase se inicializa automáticamente con `google-services.json`. No se necesita código.

### Leer de Firestore (RouteRepository.kt):

```kotlin
suspend fun getRoutes(): Resource<List<Route>> {
    val snapshot = firestore.collection("routes").get().await()
    val routes = snapshot.toObjects(Route::class.java)
    return Resource.Success(routes)
}
```

### Escribir en Firestore:

```kotlin
suspend fun createRoute(route: Route): Resource<String> {
    val documentRef = firestore.collection("routes").document()
    val routeWithId = route.copy(id = documentRef.id)
    documentRef.set(routeWithId).await()
    return Resource.Success(documentRef.id)
}
```

### Actualizar un campo específico (sin sobrescribir todo):

```kotlin
firestore.collection("routes").document(routeId)
    .update("capacity", FieldValue.increment(-count.toLong()))
    .await()
```

### Eliminar documento:

```kotlin
firestore.collection("routes").document(routeId).delete().await()
```

### Autenticación (AuthRepository.kt):

```kotlin
// Registrar usuario
auth.createUserWithEmailAndPassword(email, password).await()

// Iniciar sesión
auth.signInWithEmailAndPassword(email, password).await()

// Cerrar sesión
auth.signOut()

// Obtener usuario actual
auth.currentUser
```

---

## 5. Mantener Sesión Iniciada (No cerrar al salir)

### ¿Cómo funciona actualmente?

En `NavGraph.kt`, el `startDestination` se calcula así:

```kotlin
val startDestination = remember(currentUser) {
    if (currentUser != null) Screen.Home.route
    else Screen.Login.route
}
```

Firebase Auth **ya mantiene la sesión por defecto**. Cuando cierras la app y la vuelves a abrir, `FirebaseAuth.getInstance().currentUser` sigue siendo el mismo usuario. No es necesario iniciar sesión de nuevo.

### ¿Por qué NO se cierra la sesión?

- Firebase Auth guarda las credenciales en el disco del dispositivo
- No hay un `signOut()` automático al cerrar la app
- Solo se cierra sesión si el usuario toca "Cerrar Sesión"

### Si quisieras cerrar sesión al salir de la app:

```kotlin
// En AuthViewModel:
fun logout() {
    auth.signOut()  // Firebase borra las credenciales
    // La próxima vez que abra la app, pedirá login
}
```

### Si quisieras que la sesión se cierre al minimizar la app:

```kotlin
// En MainActivity.kt:
override fun onStop() {
    super.onStop()
    // No hacer signOut() aquí para mantener sesión
}
```

### Para manejo avanzado (recordar usuario aunque no haya internet):

Firebase Auth ya hace caching automático. El `currentUser` persiste incluso offline.

---

## 6. Cambiar Colores y Tema

### Archivo principal: `core/theme/Color.kt`

```kotlin
// Colores principales
val md_theme_light_primary = Color(0xFF1A6B52)       // ← Cambia este
val md_theme_light_onPrimary = Color(0xFFFFFFFF)
val md_theme_light_primaryContainer = Color(0xFFA5F2D5)
// ... más colores
```

### Tema claro y oscuro: `core/theme/Theme.kt`

```kotlin
@Composable
fun BusifyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) darkColorScheme(...) 
                      else lightColorScheme(...)
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
```

### Para cambiar colores:

1. Abre `core/theme/Color.kt`
2. Cambia los valores hexadecimales:
   - `0xFF1A6B52` → formato `0xAARRGGBB` (Alpha, Rojo, Verde, Azul)
   - Ej: `0xFFE91E63` es rosa, `0xFF2196F3` es azul
3. Los cambios se reflejan en TODA la app automáticamente

### Para cambiar el color de un elemento específico:

```kotlin
Button(
    onClick = { },
    colors = ButtonDefaults.buttonColors(
        containerColor = Color(0xFFFF5722),     // Naranja
        contentColor = Color.White
    )
)
```

### Colores predefinidos en Compose:

| Color | Código |
|-------|--------|
| Rojo | `Color(0xFFFF0000)` o `Color.Red` |
| Verde | `Color(0xFF4CAF50)` |
| Azul | `Color(0xFF2196F3)` |
| Blanco | `Color.White` |
| Negro | `Color.Black` |
| Gris | `Color.Gray` |

---

## 7. Agregar Nuevos Campos a un Modelo

### Ejemplo: Agregar "descuento" a Route

**1. Modificar el modelo** (`domain/model/Route.kt`):
```kotlin
data class Route(
    val id: String = "",
    val origin: String = "",
    // ... campos existentes
    val discount: Double = 0.0,  // ← NUEVO: descuento en porcentaje (0-100)
)
```

**2. Actualizar el formulario** (`features/admin/AdminScreen.kt`):
```kotlin
// Agregar estado
val formState = viewModel.formState.value

// En el formulario, agregar el campo:
OutlinedTextField(
    value = formState.discount,
    onValueChange = { if (it.isNotEmpty() && it.all { c -> c.isDigit() || c == '.' }) 
        onFieldChange("discount", it) },
    label = { Text("Descuento (%)") },
    modifier = Modifier.fillMaxWidth(),
    shape = MaterialTheme.shapes.medium,
    leadingIcon = { Icon(Icons.Default.Sale, contentDescription = null) }
)
```

**3. Actualizar el AdminViewModel**:
```kotlin
// En AdminFormState:
data class AdminFormState(
    // ... campos existentes
    val discount: String = "0"
)

// En AdminViewModel.updateField():
"discount" -> _formState.value = _formState.value.copy(discount = value)

// En submitRoute() agregar al objeto Route:
discount = form.discount.toDoubleOrNull() ?: 0.0
```

**4. Mostrar en pantallas** (`features/buses/BusesScreen.kt`):
```kotlin
// En BusCard:
if (route.discount > 0) {
    InfoItem(Icons.Default.Sale, "Descuento: ${route.discount}%")
}
```

---

## 8. CRUD Completo: Crear, Leer, Actualizar, Eliminar

### Crear (Create):

```kotlin
// RouteRepository
suspend fun createRoute(route: Route): Resource<String> {
    val docRef = firestore.collection("routes").document()
    val routeWithId = route.copy(id = docRef.id)
    docRef.set(routeWithId).await()
    return Resource.Success(docRef.id)
}
```

### Leer (Read):

```kotlin
// RouteRepository
suspend fun getRoutes(): Resource<List<Route>> {
    val snapshot = firestore.collection("routes").get().await()
    val routes = snapshot.toObjects(Route::class.java)
    return Resource.Success(routes)
}

// Con filtro:
firestore.collection("routes")
    .whereEqualTo("status", "A tiempo")
    .get().await()
```

### Actualizar (Update):

```kotlin
// RouteRepository - Sobrescribe TODO el documento:
suspend fun updateRoute(route: Route): Resource<Boolean> {
    firestore.collection("routes").document(route.id).set(route).await()
    return Resource.Success(true)
}

// O actualizar solo un campo:
firestore.collection("routes").document(routeId)
    .update("price", 85.0).await()
```

### Eliminar (Delete):

```kotlin
// RouteRepository
suspend fun deleteRoute(routeId: String): Resource<Boolean> {
    firestore.collection("routes").document(routeId).delete().await()
    return Resource.Success(true)
}
```

---

## 9. State Management en Compose

### Tipos de estado:

```kotlin
// Estado que COMPOSE observa para re-renderizar:
val textState = remember { mutableStateOf("") }          // Simple
var text by remember { mutableStateOf("") }              // Con delegado

// Estado en ViewModel (sobrevive rotaciones):
private val _data = mutableStateOf<List<Route>>(emptyList())
val data: State<List<Route>> = _data
```

### Cómo actualizar la UI automáticamente:

```kotlin
// En ViewModel:
class MiViewModel : ViewModel() {
    private val _mensaje = mutableStateOf("Hola")
    val mensaje: State<String> = _mensaje
    
    fun cambiarMensaje() {
        _mensaje.value = "Nuevo mensaje"  // ← Esto actualiza la UI automáticamente
    }
}

// En Screen:
@Composable
fun MiScreen(viewModel: MiViewModel = viewModel()) {
    Text(viewModel.mensaje.value)  // ← Se re-renderiza cuando cambia
}
```

### LaunchedEffect (ejecutar código cuando cambia un estado):

```kotlin
LaunchedEffect(createRouteState) {
    when (createRouteState) {
        is Resource.Success -> {
            snackbarHostState.showSnackbar("Éxito")
            viewModel.resetForm()
        }
        is Resource.Error -> {
            snackbarHostState.showSnackbar(createRouteState.message ?: "Error")
        }
        else -> {}
    }
}
```

### remember vs mutableStateOf:

```kotlin
// remember: solo sobrevive recomposiciones, NO rotaciones
val texto = remember { mutableStateOf("") }

// ViewModel: sobrevive rotaciones y cambios de configuración
// (usar para datos que vienen de Firebase)
```

---

## 10. Navegación con Parámetros

### Definir ruta con parámetros:

```kotlin
// Screen.kt
object Detalle : Screen("detalle/{rutaId}/{nombre}")
```

### Extraer parámetros:

```kotlin
// NavGraph.kt
composable(route = Screen.Detalle.route) { backStackEntry ->
    val rutaId = backStackEntry.arguments?.getString("rutaId") ?: ""
    val nombre = backStackEntry.arguments?.getString("nombre") ?: ""
    DetalleScreen(rutaId = rutaId, nombre = nombre)
}
```

### Navegar con parámetros:

```kotlin
navController.navigate("detalle/abc123/MiRuta")
```

### Parámetros opcionales:

```kotlin
// Screen.kt
object Detalle : Screen("detalle?rutaId={rutaId}&nombre={nombre}") {
    fun createRoute(rutaId: String, nombre: String = "Default"): String {
        return "detalle?rutaId=$rutaId&nombre=$nombre"
    }
}

// Uso:
navController.navigate(Screen.Detalle.createRoute("abc123"))
```

---

## 11. Snackbar vs Toast

### Antes (Toast - NO USAR):
```kotlin
Toast.makeText(context, "Mensaje", Toast.LENGTH_SHORT).show()
```

### Ahora (Snackbar - USAR):
```kotlin
// 1. Declarar estado
val snackbarHostState = remember { SnackbarHostState() }
val scope = rememberCoroutineScope()

// 2. Agregar al Scaffold
Scaffold(
    snackbarHost = { SnackbarHost(snackbarHostState) }
) { ... }

// 3. Mostrar mensaje
scope.launch {
    snackbarHostState.showSnackbar("Mensaje")
}

// También desde LaunchedEffect (no necesita scope):
LaunchedEffect(estado) {
    if (estado is Resource.Success) {
        snackbarHostState.showSnackbar("Operación exitosa")
    }
}
```

---

## 12. Pull-to-Refresh

### Implementación:

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MiPantalla() {
    val pullToRefreshState = rememberPullToRefreshState()
    var isRefreshing by remember { mutableStateOf(false) }
    
    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            viewModel.cargarDatos()
            isRefreshing = false
        }
    }
    
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { isRefreshing = true },
        state = pullToRefreshState
    ) {
        LazyColumn { ... }
    }
}
```

---

## 13. ExposedDropdownMenu (Dropdowns)

### Código completo:

```kotlin
var expanded by remember { mutableStateOf(false) }
val opciones = listOf("Opción 1", "Opción 2", "Opción 3")
var seleccion by remember { mutableStateOf(opciones[0]) }

ExposedDropdownMenuBox(
    expanded = expanded,
    onExpandedChange = { expanded = !expanded }
) {
    OutlinedTextField(
        value = seleccion,
        onValueChange = {},
        readOnly = true,                    // ← Importante: no editable
        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
        modifier = Modifier.fillMaxWidth().menuAnchor(),  // ← El anchor es crucial
        label = { Text("Selecciona") }
    )
    ExposedDropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false }
    ) {
        opciones.forEach { opcion ->
            DropdownMenuItem(
                text = { Text(opcion) },
                onClick = {
                    seleccion = opcion
                    expanded = false
                }
            )
        }
    }
}
```

### Notas importantes:
- El campo debe ser `readOnly = true` para que no se abra el teclado
- `menuAnchor()` es necesario para que el dropdown se posicione correctamente
- `@OptIn(ExperimentalMaterial3Api::class)` es necesario

---

## 14. DatePicker y TimePicker

### DatePicker (calendario):

```kotlin
var showDatePicker by remember { mutableStateOf(false) }

if (showDatePicker) {
    val datePickerState = rememberDatePickerState()
    
    androidx.compose.material3.DatePickerDialog(
        onDismissRequest = { showDatePicker = false },
        confirmButton = {
            TextButton(onClick = {
                datePickerState.selectedDateMillis?.let { millis ->
                    // millis es Long (timestamp Unix)
                    onDateSelected(millis)
                }
                showDatePicker = false
            }) { Text("Aceptar") }
        },
        dismissButton = {
            TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}
```

### Formatear fecha para mostrar:

```kotlin
val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale("es", "PE")) }
val fechaStr = if (fechaMillis > 0) dateFormat.format(Date(fechaMillis)) else ""
```

### TimePicker (selector de hora):

Se implementó un diálogo custom con botones ▲▼. Para usar el TimePicker nativo de Android:

```kotlin
// Alternativa con TimePicker nativo (requiere contexto de Activity):
val context = LocalContext.current
val activity = context as Activity

val timePickerDialog = android.app.TimePickerDialog(
    context,
    { _, hour, minute ->
        onTimeSelected(hour, minute)
    },
    initialHour, initialMinute, true  // true = formato 24h
)
timePickerDialog.show()
```

---

## 15. Código QR

### Generar QR con zxing:

```kotlin
// En TicketScreen.kt
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

private fun generateQrCode(content: String): Bitmap? {
    val writer = QRCodeWriter()
    val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, 512, 512)
    val bitmap = Bitmap.createBitmap(512, 512, Bitmap.Config.RGB_565)
    for (x in 0 until 512) {
        for (y in 0 until 512) {
            bitmap.setPixel(x, y, if (bitMatrix[x, y]) 
                android.graphics.Color.BLACK 
            else 
                android.graphics.Color.WHITE)
        }
    }
    return bitmap
}
```

### Mostrar el QR:

```kotlin
val qrBitmap = remember(qrContent) { generateQrCode(qrContent) }

if (qrBitmap != null) {
    Image(
        bitmap = qrBitmap.asImageBitmap(),
        contentDescription = "QR",
        modifier = Modifier.size(160.dp)
    )
}
```

### Dependencia necesaria (build.gradle.kts):
```kotlin
implementation("com.google.zxing:core:3.5.3")
```

---

## 16. Notificaciones Push FCM

### Servicio (BusifyMessagingService.kt):

```kotlin
class BusifyMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        // Se llama cuando el token cambia (primera vez o renovación)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val title = message.notification?.title ?: message.data["title"]
        val body = message.notification?.body ?: message.data["body"]
        showNotification(title ?: "Busify", body ?: "")
    }
}
```

### Para enviar notificaciones desde Firebase Console:
1. Ir a [Firebase Console](https://console.firebase.google.com)
2. Seleccionar proyecto Busify
3. Ir a "Cloud Messaging"
4. Crear campaña → "Notificación de Firebase"
5. Escribir título y cuerpo
6. En "Segmento de usuarios" elegir "Todos los usuarios" o "Android"
7. Enviar

### Para enviar desde código (Cloud Function o servidor):

```bash
# POST request a Firebase API
curl -X POST -H "Authorization: key=YOUR_SERVER_KEY" \
  -H "Content-Type: application/json" \
  -d '{
    "to": "/topics/all",
    "notification": {
      "title": "Nueva ruta",
      "body": "Se agregó una nueva ruta Lima → Cusco"
    }
  }' \
  https://fcm.googleapis.com/fcm/send
```

---

## Preguntas Típicas de Examen

### 1. "Agrega un botón en la pantalla de inicio"

```kotlin
// En HomeScreen.kt, dentro del Column:
Button(
    onClick = { /* tu acción */ },
    modifier = Modifier.fillMaxWidth(),
    shape = MaterialTheme.shapes.medium
) {
    Text("Nuevo Botón")
}
```

### 2. "Cambia el color del tema a azul"

```kotlin
// En core/theme/Color.kt:
val md_theme_light_primary = Color(0xFF1976D2)  // Azul Material
```

### 3. "Agrega un campo 'teléfono' al perfil"

```kotlin
// 1. User.kt: val phone: String = ""
// 2. ProfileScreen.kt: Agregar OutlinedTextField
// 3. ProfileViewModel.kt: Incluir phone en updateUser()
```

### 4. "Haz que las rutas se ordenen por precio"

```kotlin
// En RouteRepository.kt:
firestore.collection("routes")
    .orderBy("price", Query.Direction.ASCENDING)  // Menor a mayor
    // .orderBy("price", Query.Direction.DESCENDING) // Mayor a menor
    .get().await()
```

### 5. "Muestra solo rutas con estado 'A tiempo'"

```kotlin
// En BusesViewModel o repository:
firestore.collection("routes")
    .whereEqualTo("status", "A tiempo")
    .get().await()
```

### 6. "No permitir comprar más asientos de los disponibles"

```kotlin
// En SeatSelectionScreen:
val routeCapacity = 40
val occupiedCount = bookedSeats.size
val availableCount = routeCapacity - occupiedCount
// Luego limitar selección:
val maxSeats = minOf(5, availableCount)
```

---

## Glosario Rápido

| Término | Significado |
|---------|-------------|
| `@Composable` | Función que renderiza UI en Compose |
| `remember` | Guarda valor entre recomposiciones |
| `mutableStateOf` | Crea estado observable por Compose |
| `viewModelScope` | Corrutina ligada al ciclo de vida del ViewModel |
| `LaunchedEffect` | Ejecuta efecto secundario cuando cambian sus parámetros |
| `Scaffold` | Layout base con soporte para TopBar, BottomBar, Snackbar |
| `NavController` | Controla la navegación entre pantallas |
| `Resource<T>` | Envoltorio: Success, Error, Loading |
| `FieldValue.increment()` | Operación atómica de Firestore para sumar/restar |

---

## 17. Optimización de UI para Mejor Rendimiento

### Problemas comunes de rendimiento en Compose:

```kotlin
// ❌ MAL: Crear colores en cada recomposición
Text(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))

// ✅ BIEN: Usar constante pre-computada
Text(color = TextSecondaryAlpha60)
```

### Reglas de oro para UI rápida:

1. **Prefiere `Column` sobre `LazyColumn`** para listas pequeñas (< 20 items)
2. **Evita `verticalScroll` + `LazyColumn` anidados** → usa un solo scroll
3. **Extrae colores con alpha** a constantes en `Color.kt`
4. **Reduce tamaño de QR** de 512×512 a 256×256
5. **Usa `remember`** para objetos pesados (DateFormatter, QR bitmaps)
6. **Evita emojis en UI** → usa Material Icons (vectoriales, más ligeros)
7. **Reduce elevaciones** de tarjetas (2dp → 1dp)
8. **Reduce padding** de 24dp → 16dp donde sea posible

### Ejemplo de optimización de perfil (antes y después):

**Antes (lento):**
```kotlin
Column { // No scrollable
    // ...
    LazyColumn { // Nested LazyColumn, consume mucha memoria
        items(tickets) { ... }
    }
    Spacer(modifier = Modifier.weight(1f))
    BusifyButton(text = "Cerrar Sesión")
}
```

**Después (rápido):**
```kotlin
Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
    // ...
    Column { // Simple Column en lugar de LazyColumn
        tickets.forEach { ticket ->
            TicketHistoryCard(ticket = ticket, ...)
        }
    }
    Spacer(modifier = Modifier.height(16.dp))
    BusifyButton(text = "Cerrar Sesión")
}
```

---

## 18. Preguntas Típicas de Examen (Avanzadas)

### 7. "Haz que la app sea más rápida"
```kotlin
// En Color.kt - Pre-computar colores alpha
val TextSecondaryAlpha60 = TextSecondary.copy(alpha = 0.6f)

// En Buttons.kt - Reducir altura y radio
.height(48.dp) // en lugar de 56.dp
RoundedCornerShape(12.dp) // en lugar de 16.dp

// En TicketScreen - Reducir QR
writer.encode(content, BarcodeFormat.QR_CODE, 256, 256) // en lugar de 512
```

### 8. "Agrega un icono en lugar de emoji en AdminScreen"
```kotlin
// ❌ Antes:
Tab(text = { Text("➕ Crear Ruta") })

// ✅ Después:
Tab(
    icon = { Icon(Icons.Default.Add, null) },
    text = { Text("Crear") }
)
```

### 9. "Cambia el color del texto secundario en todo el proyecto"
```kotlin
// En Color.kt:
val TextSecondaryAlpha60 = TextSecondary.copy(alpha = 0.6f)
// Luego usarlo en todas las pantallas en lugar de:
// MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
```

### 10. "Muestra un mensaje de error si no hay conexión a Firebase"
```kotlin
// En RouteRepository:
try {
    val snapshot = firestore.collection("routes").get().await()
    Resource.Success(snapshot.toObjects(Route::class.java))
} catch (e: com.google.firebase.firestore.FirebaseFirestoreException) {
    Resource.Error("Sin conexión a internet. Verifica tu red.")
} catch (e: Exception) {
    Resource.Error(e.message ?: "Error desconocido")
}
```

### 11. "Agrega un botón de ayuda en la pantalla de login"
```kotlin
// En LoginScreen.kt, dentro del Column:
Spacer(modifier = Modifier.height(8.dp))
BusifyOutlinedButton(
    text = "¿Necesitas ayuda?",
    onClick = { /* navegar a pantalla de ayuda */ }
)
```

### 12. "Filtra las rutas por empresa específica"
```kotlin
// En BusesViewModel o repository:
suspend fun getRoutesByCompany(company: String): Resource<List<Route>> {
    val snapshot = firestore.collection("routes")
        .whereEqualTo("company", company)
        .get().await()
    return Resource.Success(snapshot.toObjects(Route::class.java))
}
```

### 13. "Haz que el botón de pago se deshabilite si no hay asientos"
```kotlin
// En PaymentScreen:
val isDisabled = seatList.isEmpty() || isPaying

Button(
    onClick = { /* pagar */ },
    enabled = !isDisabled,
    modifier = Modifier.fillMaxWidth().height(48.dp),
    shape = MaterialTheme.shapes.medium
) {
    Text("Pagar S/ ${"%.2f".format(totalPrice)}")
}
```

### 14. "Agrega un confirmación antes de cerrar sesión"
```kotlin
// En ProfileScreen:
var showLogoutDialog by remember { mutableStateOf(false) }

if (showLogoutDialog) {
    AlertDialog(
        onDismissRequest = { showLogoutDialog = false },
        title = { Text("Cerrar Sesión") },
        text = { Text("¿Estás seguro de que deseas cerrar sesión?") },
        confirmButton = {
            Button(onClick = { showLogoutDialog = false; onLogout() }) {
                Text("Sí, cerrar sesión")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = { showLogoutDialog = false }) {
                Text("Cancelar")
            }
        }
    )
}
// Cambiar onClick del botón de cerrar sesión:
BusifyButton(
    text = "Cerrar Sesión",
    onClick = { showLogoutDialog = true },
    containerColor = MaterialTheme.colorScheme.error
)
```

### 15. "Agrega un campo de descuento a las rutas"
```kotlin
// 1. Route.kt: val discount: Double = 0.0
// 2. AdminFormState: val discount: String = "0"
// 3. AdminViewModel.updateField(): "discount" -> copy(discount = value)
// 4. AdminScreen: Agregar OutlinedTextField para descuento
// 5. Mostrar en BusCard: if (route.discount > 0) { ... }
```

### 16. "Muestra un indicador de carga mientras se procesa el pago"
```kotlin
// En PaymentScreen:
var isPaying by remember { mutableStateOf(false) }

Button(
    onClick = {
        isPaying = true
        scope.launch {
            // ... lógica de pago
            isPaying = false
        }
    },
    enabled = !isPaying
) {
    if (isPaying) {
        CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            color = MaterialTheme.colorScheme.onPrimary,
            strokeWidth = 2.dp
        )
    } else {
        Text("Pagar S/ ${"%.2f".format(totalPrice)}")
    }
}
```

### 17. "Agrega un campo de búsqueda por empresa en BusesScreen"
```kotlin
// En BusesScreen:
var companyFilter by remember { mutableStateOf("") }

OutlinedTextField(
    value = companyFilter,
    onValueChange = { companyFilter = it },
    label = { Text("Buscar por empresa") },
    modifier = Modifier.fillMaxWidth(),
    singleLine = true
)

// Filtrar:
val filteredRoutes = routes.filter {
    it.company.contains(companyFilter, ignoreCase = true) ||
    it.origin.contains(companyFilter, ignoreCase = true)
}
```

### 18. "Usa un StateFlow en lugar de mutableStateOf en ViewModel"
```kotlin
// Alternativa más profesional:
class MiViewModel : ViewModel() {
    private val _state = MutableStateFlow<Resource<List<Route>>>(Resource.Loading())
    val state: StateFlow<Resource<List<Route>>> = _state.asStateFlow()
    
    fun loadData() {
        viewModelScope.launch {
            _state.value = Resource.Loading()
            _state.value = repository.getRoutes()
        }
    }
}

// En el Screen:
val state by viewModel.state.collectAsState()
```

---

## Errores Comunes y Soluciones

| Error | Causa | Solución |
|-------|-------|----------|
| `java.lang.ClassCastException: java.lang.Long cannot be cast to java.lang.Integer` | Firestore guarda números como `Long` | Usar `Long` en modelos, comparar con `2L` |
| `FAILED_PRECONDITION: The query requires an index` | `orderBy()` sin índice compuesto | Eliminar `orderBy()` y ordenar en memoria con `.sortedBy()` |
| App lenta al scrollear | Demasiadas tarjetas con sombras | Reducir `elevation`, usar `shape = MaterialTheme.shapes.medium` |
| `CircularProgressIndicator` nunca desaparece | Error silencioso en corrutina | Envolver en `try/catch` y actualizar estado en ambos casos |
| Los tickets no aparecen en perfil | `orderBy()` sin índice en Firestore | Usar `.sortedByDescending { it.createdAt }` en memoria |
| El rol de admin no se refleja | ViewModel no compartido entre pantallas | Crear única instancia en NavGraph y pasar como parámetro |

---

## Glosario Rápido (Ampliado)

| Término | Significado |
|---------|-------------|
| `@Composable` | Función que renderiza UI en Compose |
| `remember` | Guarda valor entre recomposiciones |
| `mutableStateOf` | Crea estado observable por Compose |
| `viewModelScope` | Corrutina ligada al ciclo de vida del ViewModel |
| `LaunchedEffect` | Ejecuta efecto secundario cuando cambian sus parámetros |
| `Scaffold` | Layout base con soporte para TopBar, BottomBar, Snackbar |
| `NavController` | Controla la navegación entre pantallas |
| `Resource<T>` | Envoltorio: Success, Error, Loading |
| `FieldValue.increment()` | Operación atómica de Firestore para sumar/restar |
| `SideEffect` | Ejecuta código después de cada recomposición exitosa |
| `disposableEffect` | Efecto con cleanup al salir de la composición |
| `derivedStateOf` | Estado derivado de otros estados (evita recomposiciones innecesarias) |
| `snapshotFlow` | Convierte estado de Compose en Flow de Kotlin |
| `callbackFlow` | Crea Flow basado en callbacks (útil para Firestore listeners) |
| `CompositionLocal` | Provee datos implícitamente a través del árbol de composición |

---

> **Consejo para el examen:** Entiende el flujo MVVM (Screen → ViewModel → Repository → Firebase). Si te piden agregar algo nuevo, sigue la misma estructura de los archivos existentes. Busca el archivo más parecido a lo que necesitas y cópialo como plantilla.
> 
> **Para rendimiento:** Recuerda que los cambios más efectivos son: reducir tamaño de QR, pre-computar colores alpha, evitar nested scrolling, y reducir elevaciones/sombras de tarjetas.
