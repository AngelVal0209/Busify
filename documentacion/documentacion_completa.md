# Documentación Completa de Busify

## Índice
1. [Estructura del Proyecto](#1-estructura-del-proyecto)
2. [Modelo de Datos (Route)](#2-modelo-de-datos-route)
3. [Repositorio de Rutas (RouteRepository)](#3-repositorio-de-rutas-routerepository)
4. [Panel de Administración](#4-panel-de-administración)
5. [Flujo de Compra y Capacidad](#5-flujo-de-compra-y-capacidad)
6. [Pantallas de Visualización](#6-pantallas-de-visualización)
7. [Notificaciones Push (FCM)](#7-notificaciones-push-fcm)
8. [Historial de Viajes en Perfil](#8-historial-de-viajes-en-perfil)
9. [Código QR en Ticket](#9-código-qr-en-ticket)
10. [Asignación de Conductores](#10-asignación-de-conductores)
11. [Snackbars vs Toasts](#11-snackbars-vs-toasts)
12. [Pull-to-Refresh](#12-pull-to-refresh)
13. [Paginación de Rutas](#13-paginación-de-rutas)
14. [Dependencias Agregadas](#14-dependencias-agregadas)

---

## 1. Estructura del Proyecto

```
app/src/main/java/com/example/busify/
├── MainActivity.kt
├── core/
│   ├── components/
│   │   ├── Buttons.kt
│   │   └── TextFields.kt
│   ├── fcm/
│   │   └── BusifyMessagingService.kt   ← NUEVO
│   ├── navigation/
│   │   ├── NavGraph.kt
│   │   └── Screen.kt
│   ├── theme/
│   │   ├── Color.kt
│   │   ├── Theme.kt
│   │   └── Type.kt
│   └── util/
│       └── Resource.kt
├── data/
│   └── repository/
│       ├── AuthRepository.kt
│       ├── PaymentRepository.kt
│       ├── RouteRepository.kt
│       └── TicketRepository.kt          ← MODIFICADO (sin orderBy, sort en memoria)
├── domain/
│   └── model/
│       ├── Payment.kt
│       ├── Route.kt                     ← MODIFICADO (capacity: Long)
│       ├── Seat.kt
│       ├── Ticket.kt                    ← MODIFICADO (seatNumbers: List<Long>)
│       └── User.kt                      ← MODIFICADO (role: Long)
└── features/
    ├── admin/
    │   ├── AdminScreen.kt               ← REESCRITO + gestión de usuarios
    │   └── AdminViewModel.kt            ← REESCRITO + fix submitRoute
    ├── auth/
    │   ├── AuthViewModel.kt
    │   ├── LoginScreen.kt               ← MODIFICADO (viewModel compartido)
    │   └── RegisterScreen.kt            ← MODIFICADO (viewModel compartido)
    ├── buses/
    │   ├── BusesScreen.kt
    │   └── BusesViewModel.kt
    ├── home/
    │   └── HomeScreen.kt                ← MODIFICADO (viewModel compartido)
    ├── profile/
    │   ├── ProfileScreen.kt             ← MODIFICADO (detalle con QR + click)
    │   └── ProfileViewModel.kt
    └── viajes/
        ├── PaymentScreen.kt
        ├── SeatSelectionScreen.kt       ← MODIFICADO (seats: Long)
        ├── TicketScreen.kt
        └── ViajesScreen.kt
```

---

## 2. Modelo de Datos (Route)

**Archivo:** `domain/model/Route.kt`

Campos agregados:
| Campo | Tipo | Descripción |
|-------|------|-------------|
| `departureDate` | `Long` | Timestamp Unix en milisegundos de la fecha de salida |
| `arrivalDate` | `Long` | Timestamp Unix en milisegundos de la fecha de llegada |
| `driverId` | `String` | UID del conductor asignado (Firebase Auth) |
| `driverName` | `String` | Nombre del conductor asignado |

```kotlin
data class Route(
    val id: String = "",
    val origin: String = "",
    val destination: String = "",
    val departureTime: String = "",       // "HH:MM"
    val arrivalTime: String = "",         // "HH:MM"
    val departureDate: Long = 0L,          // timestamp millis
    val arrivalDate: Long = 0L,            // timestamp millis
    val price: Double = 0.0,
    val busType: String = "",
    val duration: String = "",
    val status: String = "Pendiente",
    val capacity: Long = 0L,              // Long para compatibilidad con Firestore
    val company: String = "",
    val driverId: String = "",
    val driverName: String = ""
)
```

---

## 2.1 Modelo de Datos (User)

**Archivo:** `domain/model/User.kt`

```kotlin
data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val photoUrl: String? = null,
    val role: Long = 1L // 1L = Usuario, 2L = Administrador, 3L = Chofer
)
```

**Nota:** El campo `role` usa `Long` en lugar de `Int` porque Firestore almacena todos los números como `Long` internamente. Esto evita errores de deserialización. Todas las comparaciones se hacen con `== 2L`, `== 1L`, etc.

---

## 2.2 Modelo de Datos (Payment)

**Archivo:** `domain/model/Payment.kt`

```kotlin
data class Payment(
    val id: String = "",
    val ticketId: String = "",
    val amount: Double = 0.0,
    val method: String = "",
    val status: String = "completado",
    val timestamp: Long = System.currentTimeMillis()
)
```

Se usa en `PaymentRepository.recordPayment()` para registrar transacciones en la colección `payments` de Firestore.

---

## 2.3 Modelo de Datos (Seat)

**Archivo:** `domain/model/Seat.kt`

```kotlin
data class Seat(
    val number: Int = 0,
    val isAvailable: Boolean = true
)
```

Usado internamente en `SeatSelectionScreen` para representar el estado visual de cada asiento (1-40). La disponibilidad real se obtiene desde Firestore mediante `TicketRepository.getBookedSeatsForRoute()`.

---

## 2.4 Modelo de Datos (Ticket)

**Archivo:** `domain/model/Ticket.kt`

```kotlin
data class Ticket(
    val id: String = "",
    val userId: String = "",
    val routeId: String = "",
    val company: String = "",
    val origin: String = "",
    val destination: String = "",
    val departureTime: String = "",
    val seatNumbers: List<Long> = emptyList(),  // Long por Firestore
    val totalPrice: Double = 0.0,
    val paymentMethod: String = "",
    val status: String = "confirmado",
    val createdAt: Long = System.currentTimeMillis()
)
```

**Nota:** `seatNumbers` usa `List<Long>` por la misma razón de compatibilidad con Firestore. Los asientos 1-40 se representan como `Long`.

---

## 3. Repositorio de Rutas (RouteRepository)

**Archivo:** `data/repository/RouteRepository.kt`

Métodos agregados:

### `updateRoute(route: Route): Resource<Boolean>`
Actualiza un documento completo en la colección `routes` sobreescribiendo con el objeto `Route`.

### `deleteRoute(routeId: String): Resource<Boolean>`
Elimina un documento de `routes` por su ID.

### `decrementCapacity(routeId: String, count: Int): Resource<Boolean>`
Usa `FieldValue.increment(-count.toLong())` de Firestore para restar asientos de forma **atómica** sin necesidad de leer primero el documento.

```kotlin
firestore.collection("routes").document(routeId)
    .update("capacity", FieldValue.increment(-count.toLong()))
```

### `getPaginatedRoutes(lastVisibleId: String?, pageSize: Long): Resource<Pair<List<Route>, String?>>`
Obtiene rutas paginadas ordenadas por `departureDate` ascendente. Devuelve un par con la lista de rutas y el ID del último documento (para usarse como cursor en la siguiente página).

---

## 4. Panel de Administración

### AdminViewModel

**Archivo:** `features/admin/AdminViewModel.kt`

**Estados:**

| Estado | Tipo | Descripción |
|--------|------|-------------|
| `formState` | `AdminFormState` | Estado del formulario con todos los campos |
| `fieldErrors` | `FieldError` | Errores de validación por campo |
| `createRouteState` | `Resource<String>?` | Resultado de crear/editar |
| `routesState` | `Resource<List<Route>>?` | Lista de rutas cargadas |
| `deleteState` | `Resource<Boolean>?` | Resultado de eliminar |
| `editingRoute` | `Route?` | Ruta que se está editando (null = modo crear) |
| `searchQuery` | `String` | Texto de búsqueda |
| `selectedTab` | `Int` | Tab seleccionada (0=Crear, 1=Gestionar, 2=Usuarios) |
| `usersState` | `Resource<List<User>>?` | Lista de usuarios del sistema |
| `roleUpdateState` | `Resource<Boolean>?` | Resultado de actualizar rol |

**Funciones clave:**

| Función | Descripción |
|---------|-------------|
| `submitRoute()` | Valida todo el formulario y crea o actualiza la ruta |
| `loadRoutes()` | Carga todas las rutas desde Firestore |
| `deleteRoute(id)` | Elimina una ruta y recarga la lista |
| `startEdit(route)` | Pre-puebla el formulario con datos de la ruta a editar |
| `cancelEdit()` | Limpia el modo edición y resetea el formulario |
| `updateField(field, value)` | Actualiza un campo y ejecuta validación en tiempo real |
| `getFilteredRoutes()` | Filtra rutas por origen, destino o empresa |
| `loadUsers()` | Carga todos los usuarios desde Firestore |
| `setUserRole(uid, role)` | Actualiza el rol de un usuario (promover/degradar) |

### AdminScreen

**Archivo:** `features/admin/AdminScreen.kt`

**Características:**
- **Tabs**: "Crear Ruta" / "Gestionar Rutas" / "Usuarios"
- **Formulario** con:
  - `ExposedDropdownMenuBox` para Tipo de Bus (Semi cama, VIP, Económico, Cama)
  - `ExposedDropdownMenuBox` para Estado (Pendiente, A tiempo, Demorado)
  - DatePicker de Material3 para fechas
  - TimePickerDialog custom (sube/baja horas y minutos)
  - Validación en tiempo real con `supportingText`
  - Iconos leading en todos los campos
- **Lista de rutas** con:
  - Barra de búsqueda por origen/destino/empresa
  - Tarjetas con iconos de editar (📝) y eliminar (🗑️)
  - `AlertDialog` de confirmación antes de eliminar
- **Modo edición**: Banner indicando "Editando: Origen → Destino" con botón de cancelar
- **Gestión de usuarios**:
  - Lista de todos los usuarios con su rol actual
  - Botón para promover a administrador (icono `AdminPanelSettings`)
  - Botón para degradar a usuario normal (icono `Person`)
  - Snackbar de feedback al cambiar rol
- **Snackbar** para feedback de éxito/error

**Fix importante:** `submitRoute()` anteriormente tenía un bug con `.let {}` que ocultaba errores de actualización. Ahora maneja correctamente los casos de error en `updateRoute()`.

---

## 5. Flujo de Compra y Capacidad

**Archivo:** `features/viajes/PaymentScreen.kt`

Cuando el usuario completa un pago exitosamente:

1. Se guarda el ticket en Firestore (`ticketRepository.saveTicket()`)
2. Se decrementa la capacidad de la ruta (`routeRepository.decrementCapacity(routeId, seatList.size)`)
3. Se muestra Snackbar de éxito
4. Se navega a la pantalla de Ticket

```kotlin
val result = ticketRepository.saveTicket(ticket)
when (result) {
    is Resource.Success -> {
        routeRepository.decrementCapacity(routeId, seatList.size)
        snackbarHostState.showSnackbar("Pago realizado con éxito")
        navController.navigate("ticket/...")
    }
    is Resource.Error -> {
        snackbarHostState.showSnackbar(result.message ?: "Error")
    }
}
```

---

## 6. Componentes Reutilizables

### Buttons.kt

**Archivo:** `core/components/Buttons.kt`

Dos componentes reutilizables:
- `BusifyButton` — Botón principal con ancho completo, 56dp de alto, bordes redondeados de 16dp. Acepta `containerColor` personalizable (default = primary).
- `BusifyOutlinedButton` — Botón secundario "outlined" con las mismas dimensiones.

### TextFields.kt

**Archivo:** `core/components/TextFields.kt`

- `BusifyTextField` — Campo de texto estilizado con borde redondeado de 16dp, soporte para `leadingIcon`, `trailingIcon`, `visualTransformation`, y estado de error con `supportingText`.

Ambos se usan en `LoginScreen`, `RegisterScreen` y `ProfileScreen`.

---

## 7. AuthViewModel y Listener en Tiempo Real

**Archivo:** `features/auth/AuthViewModel.kt`

`AuthViewModel` es el **ViewModel compartido** en toda la app. Se crea una única instancia en `BusifyNavigation` y se pasa a todas las pantallas que lo necesitan.

**Funcionalidades clave:**

- **Persistencia de sesión:** En `init`, verifica si hay un usuario autenticado via `repository.getCurrentUser()`. Si existe, carga sus datos y activa un **listener en tiempo real** de Firestore.
- **Realtime listener:** `repository.listenToUserDetails(uid)` usa `callbackFlow` y `addSnapshotListener` para mantener `currentUserData` sincronizado automáticamente. Si un admin cambia el rol de un usuario, el cambio se refleja instantáneamente sin necesidad de recargar la app.
- **Métodos:** `login()`, `register()`, `logout()` — actualizan `currentUserData` y los estados respectivos.

---

## 8. Seat Selection Screen

**Archivo:** `features/viajes/SeatSelectionScreen.kt`

- Muestra una cuadrícula de 40 asientos (4 columnas) usando `LazyVerticalGrid`
- Los asientos ocupados se cargan desde Firestore con `TicketRepository.getBookedSeatsForRoute(routeId)`
- Colores: rojo (ocupado), gris (disponible), primary (seleccionado)
- Límite máximo de 5 asientos por compra
- Al continuar, navega a `PaymentScreen` con los parámetros de la ruta y los asientos seleccionados (separados por `_`)

---

## 9. ProfileViewModel

**Archivo:** `features/profile/ProfileViewModel.kt`

- Carga el perfil del usuario y sus tickets simultáneamente mediante `repository.getUserDetails()` y `ticketRepository.getUserTickets()`
- `updateUser(name)` actualiza el nombre y refleja el cambio localmente
- `resetUpdateState()` limpia el estado de actualización después del Snackbar

---

## 10. Pantallas de Visualización

Todas las pantallas que muestran rutas (`BusesScreen`, `ViajesScreen`, `HomeScreen`, `TicketScreen`) fueron actualizadas para mostrar la fecha formateada cuando `departureDate > 0`:

```kotlin
val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale("es", "PE")) }
val departureStr = if (route.departureDate > 0) {
    "${dateFormat.format(Date(route.departureDate))} ${route.departureTime}"
} else {
    route.departureTime
}
```

### BusesScreen
- **Pull-to-refresh** mediante `PullToRefreshBox`
- Muestra empresa, origen → destino, estado, fecha+ hora salida, hora llegada, precio, capacidad, conductor

### ViajesScreen
- **Pull-to-refresh** mediante `PullToRefreshBox`
- Búsqueda por origen/destino
- Fecha+ hora formateada, info del conductor si aplica

---

## 11. Notificaciones Push (FCM)

**Archivo nuevo:** `core/fcm/BusifyMessagingService.kt`

Servicio que extiende `FirebaseMessagingService`:

- **`onNewToken(token)`**: Maneja tokens nuevos de FCM
- **`onMessageReceived(message)`**: Muestra notificación cuando llega un mensaje
- **`showNotification(title, body)`**: Crea canal de notificación (Android 8+) y muestra la notificación con:
  - Título y cuerpo
  - PendingIntent para abrir la app
  - Auto-cancel al tocar

**Registro en AndroidManifest.xml:**
```xml
<service
    android:name=".core.fcm.BusifyMessagingService"
    android:exported="false">
    <intent-filter>
        <action android:name="com.google.firebase.MESSAGING_EVENT" />
    </intent-filter>
</service>
```

---

## 12. Historial de Viajes en Perfil

### ProfileViewModel
- Se agregó `ticketsState` que carga los tickets del usuario autenticado mediante `ticketRepository.getUserTickets(uid)`
- Se carga automáticamente al inicializar junto con el perfil

### ProfileScreen
- Nueva opción **"Mis Viajes"** con icono `History`
- Al tocar, se expande/colapsa una sección que muestra:
  - Cantidad de viajes
  - Lista de tarjetas clickeables con: origen → destino, empresa, hora, asientos, total pagado
- **Detalle del viaje con QR:** Al tocar una tarjeta de viaje, se abre un diálogo `TicketDetailDialog` con:
  - Información completa: empresa, ruta, salida, asientos, total, método de pago, estado
  - **Código QR** generado con ZXing (misma librería que `TicketScreen`)
  - El QR contiene: ruta, empresa, salida, asientos, ID de ruta
- **Snackbar** para feedback de actualización de perfil (reemplaza Toast)

---

## 13. Código QR en Ticket

**Archivo:** `features/viajes/TicketScreen.kt`

Se agregó generación de código QR usando la librería **zxing**:

```kotlin
private fun generateQrCode(content: String): Bitmap? {
    val writer = QRCodeWriter()
    val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, 512, 512)
    val bitmap = Bitmap.createBitmap(512, 512, Bitmap.Config.RGB_565)
    for (x in 0 until 512) {
        for (y in 0 until 512) {
            bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
        }
    }
    bitmap
}
```

El QR contiene:
```
Busify Ticket
Ruta: Lima → Arequipa
Empresa: Cruz del Sur
Salida: 14:30
Asientos: 5, 6, 7
ID: abc123
```

Se muestra como un `Image` de 160dp debajo del resumen del ticket.

---

## 13.1 Historial de Viajes con QR en Perfil (Detalle)

**Archivo:** `features/profile/ProfileScreen.kt`

Desde el perfil, al tocar **"Mis Viajes"** se expande la lista de tickets. Cada ticket es clickeable y abre un `Dialog` con:

- Información completa del viaje (empresa, ruta, salida, asientos, total, pago, estado)
- Código QR generado con ZXing (512x512px)
- El contenido del QR incluye: ruta, empresa, hora de salida, asientos seleccionados y ID de la ruta

```kotlin
// Contenido del QR:
"Busify Ticket\nRuta: Lima → Arequipa\nEmpresa: Cruz del Sur\nSalida: 14:30\nAsientos: 5, 6, 7\nID: abc123"
```

---

## 9.2 Fix: ViewModel Compartido (AuthViewModel)

**Problema original:** Cada pantalla (`LoginScreen`, `RegisterScreen`, `HomeScreen`) creaba su propia instancia de `AuthViewModel` mediante `viewModel()` con valor por defecto. Cuando el usuario iniciaba sesión, los datos se guardaban en la instancia del LoginScreen, pero el bottom nav bar usaba la instancia del NavGraph, que nunca recibía los datos → `role == 2L` siempre era falso.

**Solución:** 
- `BusifyNavigation` crea una única instancia de `AuthViewModel` y la pasa como parámetro a todas las pantallas que la necesitan
- Se eliminaron los `= viewModel()` por defecto de `LoginScreen`, `RegisterScreen` y `HomeScreen`
- Ahora reciben `viewModel: AuthViewModel` como parámetro requerido

```kotlin
// NavGraph.kt
fun BusifyNavigation(
    viewModel: AuthViewModel = viewModel()  // ← Única instancia
) {
    // Pasar a cada pantalla:
    LoginScreen(viewModel = viewModel, ...)
    RegisterScreen(viewModel = viewModel, ...)
    HomeScreen(authViewModel = viewModel)
}
```

---

## 9.3 Fix: Tipos Long para Firestore

Firestore almacena todos los números como `Long` internamente. Para evitar errores de deserialización con `toObject()` y `toObjects()`, se cambiaron los siguientes campos:

| Modelo | Campo | Cambio |
|--------|-------|--------|
| `User` | `role` | `Int` → `Long` (comparar con `== 2L`) |
| `Route` | `capacity` | `Int` → `Long` |
| `Ticket` | `seatNumbers` | `List<Int>` → `List<Long>` |

Todas las comparaciones y conversiones se actualizaron para usar los nuevos tipos (`== 2L`, `.toLongOrNull()`, etc.).

---

## 9.4 Fix: getUserTickets sin orderBy

**Problema:** `TicketRepository.getUserTickets()` usaba `.whereEqualTo("userId", userId).orderBy("createdAt", DESCENDING)`, lo que requiere un **índice compuesto** en Firestore. Si el índice no existía, la consulta fallaba y los tickets no se mostraban en el perfil.

**Solución:** Se eliminó `.orderBy()` de la consulta a Firestore. Ahora los tickets se ordenan en memoria con `.sortedByDescending { it.createdAt }`.

---

## 10. Asignación de Conductores

El formulario de Admin incluye un campo **"Conductor"** (junto al dropdown de Estado en la misma fila).

- Se almacena en `Route.driverName` y `Route.driverId`
- Se muestra en:
  - `BusesScreen` (si el conductor está asignado)
  - `ViajesScreen` (texto secundario)
  - `AdminScreen` lista de rutas

**Uso futuro:** El rol `3` en `User.role` corresponde a **Chofer**. Cuando un conductor inicie sesión, podría ver solo sus rutas asignadas filtrando por `driverId`.

---

## 11. Snackbars vs Toasts

Se reemplazaron todos los `Toast.makeText()` por `Snackbar` de Material3 para una experiencia más moderna.

### Archivos actualizados:

| Archivo | Cambio |
|---------|--------|
| `LoginScreen.kt` | Scaffold con SnackbarHost |
| `RegisterScreen.kt` | SnackbarHost en Scaffold existente |
| `PaymentScreen.kt` | SnackbarHost en Scaffold existente |
| `ProfileScreen.kt` | SnackbarHost en Scaffold |

**Código típico:**
```kotlin
val snackbarHostState = remember { SnackbarHostState() }
val scope = rememberCoroutineScope()

Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { ... }

// Para mostrar:
scope.launch { snackbarHostState.showSnackbar("Mensaje") }
```

---

## 12. Pull-to-Refresh

**Archivos:** `BusesScreen.kt`, `ViajesScreen.kt`

Implementado con `PullToRefreshBox` de Material3 (disponible desde 1.3.0):

```kotlin
val pullToRefreshState = rememberPullToRefreshState()
var isRefreshing by remember { mutableStateOf(false) }

PullToRefreshBox(
    isRefreshing = isRefreshing,
    onRefresh = { isRefreshing = true },
    state = pullToRefreshState
) {
    LazyColumn { ... }
}

LaunchedEffect(isRefreshing) {
    if (isRefreshing) {
        viewModel.getRoutes()
        isRefreshing = false
    }
}
```

---

## 13. Paginación de Rutas

**Archivo:** `RouteRepository.kt`

Método `getPaginatedRoutes(lastVisibleId: String?, pageSize: Long)`:
- Ordena por `departureDate` ascendente
- Usa `startAfter()` con el último documento visible como cursor
- Devuelve `Pair<List<Route>, lastId>` donde `lastId` es `null` cuando no hay más páginas

**Uso previsto:** Para cuando haya muchas rutas, se puede cargar por páginas de 20 en lugar de todas simultáneamente.

---

## 14. Dependencias Agregadas

**Archivo:** `app/build.gradle.kts`

```kotlin
// Notificaciones push Firebase Cloud Messaging
implementation("com.google.firebase:firebase-messaging")

// Generación de códigos QR (zxing)
implementation("com.google.zxing:core:3.5.3")
```

Ambas están dentro del scope de `Firebase BoM 34.13.0` (FCM) y se agrega zxing para QR.

---

## Resumen de Archivos Modificados

| Archivo | Estado | Cambios |
|---------|--------|---------|
| `domain/model/Route.kt` | ✅ Modificado | +departureDate, +arrivalDate, +driverId, +driverName |
| `data/repository/RouteRepository.kt` | ✅ Modificado | +updateRoute, +deleteRoute, +decrementCapacity, +getPaginatedRoutes |
| `features/admin/AdminViewModel.kt` | ✅ Reescrito | Form state, validación, CRUD, búsqueda, tabs |
| `features/admin/AdminScreen.kt` | ✅ Reescrito | Tabs, pickers, dropdowns, defaults, lista CRUD, delete dialog |
| `features/viajes/PaymentScreen.kt` | ✅ Modificado | +decrementCapacity, Snackbar, +RouteRepository |
| `features/viajes/TicketScreen.kt` | ✅ Modificado | +QR code generation |
| `features/viajes/ViajesScreen.kt` | ✅ Modificado | +PullToRefresh, datetime formato |
| `features/buses/BusesScreen.kt` | ✅ Modificado | +PullToRefresh, datetime formato, info extra |
| `features/home/HomeScreen.kt` | ✅ Modificado | Datetime formato, company, price |
| `features/profile/ProfileViewModel.kt` | ✅ Modificado | +ticketsState, carga de tickets |
| `features/profile/ProfileScreen.kt` | ✅ Modificado | +Mis Viajes expandible, Snackbar |
| `features/auth/LoginScreen.kt` | ✅ Modificado | Toast → Snackbar |
| `features/auth/RegisterScreen.kt` | ✅ Modificado | Toast → Snackbar |
| `core/fcm/BusifyMessagingService.kt` | 🆕 Nuevo | Servicio FCM |
| `app/build.gradle.kts` | ✅ Modificado | +zxing, +firebase-messaging |
| `AndroidManifest.xml` | ✅ Modificado | +FCM service registration |
