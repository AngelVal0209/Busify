# Documentación Completa de Busify

## Índice
1. [Estructura del Proyecto](#1-estructura-del-proyecto)
2. [Modelo de Datos](#2-modelo-de-datos)
3. [Repositorios](#3-repositorios)
4. [Autenticación](#4-autenticación)
5. [Panel de Administración](#5-panel-de-administración)
6. [Flujo de Compra](#6-flujo-de-compra)
7. [Pantallas de Visualización](#7-pantallas-de-visualización)
8. [Notificaciones Push (FCM)](#8-notificaciones-push-fcm)
9. [Perfil de Usuario](#9-perfil-de-usuario)
10. [Pantalla de Conductor](#10-pantalla-de-conductor)
11. [Validación de Formularios](#11-validación-de-formularios)
12. [Firebase Cloud Functions](#12-firebase-cloud-functions)
13. [Dependencias](#13-dependencias)

---

## 1. Estructura del Proyecto

```
app/src/main/java/com/example/busify/
├── MainActivity.kt                  ← Splash screen + Firestore offline persistence
├── core/
│   ├── components/
│   │   ├── Buttons.kt
│   │   └── TextFields.kt
│   ├── fcm/
│   │   └── BusifyMessagingService.kt ← FCM token auto-save to Firestore
│   ├── navigation/
│   │   ├── NavGraph.kt              ← Driver route added, logs removed
│   │   └── Screen.kt                ← Driver screen + SteeringWheel icon
│   ├── theme/
│   │   ├── Color.kt
│   │   ├── Theme.kt
│   │   └── Type.kt
│   └── util/
│       ├── Resource.kt
│       └── Validation.kt            ← NUEVO: validación de email, password, nombre
├── data/
│   └── repository/
│       ├── AuthRepository.kt        ← resetPassword, sendEmailVerification, updateEmail, updatePassword, deleteAccount, reauthenticate
│       ├── PaymentRepository.kt
│       ├── RouteRepository.kt       ← getPaginatedRoutes
│       └── TicketRepository.kt      ← getTicketsByRoute, updateTicketStatus
├── domain/
│   └── model/
│       ├── Payment.kt
│       ├── Route.kt
│       ├── Seat.kt
│       ├── Ticket.kt
│       └── User.kt                  ← photoUrl, fcmToken fields
├── features/
│   ├── admin/
│   │   ├── AdminScreen.kt           ← Logs removed
│   │   └── AdminViewModel.kt
│   ├── auth/
│   │   ├── AuthViewModel.kt         ← resetPassword, isEmailVerified, deleteAccount, reauthenticate
│   │   ├── LoginScreen.kt           ← Password reset dialog, email validation, verify button
│   │   └── RegisterScreen.kt        ← Password confirm, validation, auto send verification
│   ├── buses/
│   │   └── BusesScreen.kt
│   ├── driver/
│   │   ├── DriverScreen.kt          ← NUEVO: ver rutas asignadas, pasajeros, marcar tickets
│   │   └── DriverViewModel.kt       ← NUEVO
│   ├── home/
│   │   └── HomeScreen.kt
│   ├── profile/
│   │   ├── ProfileScreen.kt         ← Foto de perfil, cambiar email/password, eliminar cuenta
│   │   └── ProfileViewModel.kt      ← Firebase Storage upload, re-authentication
│   └── viajes/
│       ├── PaymentScreen.kt
│       ├── SeatSelectionScreen.kt
│       ├── TicketScreen.kt
│       └── ViajesScreen.kt

functions/
├── package.json                     ← Firebase Functions config
└── index.js                         ← Cloud Functions: onTicketCreated, onRouteUpdated, onRouteCreated
```

---

## 2. Modelo de Datos

### User
| Campo | Tipo | Descripción |
|-------|------|-------------|
| uid | String | ID único (Firebase Auth UID) |
| name | String | Nombre completo |
| email | String | Correo electrónico |
| photoUrl | String? | URL de foto de perfil (Firebase Storage) |
| fcmToken | String? | Token FCM para notificaciones push |
| role | Long | 1=Usuario, 2=Administrador, 3=Chofer |

### Route
| Campo | Tipo | Descripción |
|-------|------|-------------|
| id | String | ID único |
| origin | String | Lugar de salida |
| destination | String | Lugar de llegada |
| departureTime | String | Hora de salida (HH:MM) |
| arrivalTime | String | Hora de llegada (HH:MM) |
| departureDate | Long | Timestamp fecha de salida |
| arrivalDate | Long | Timestamp fecha de llegada |
| price | Double | Precio del pasaje |
| busType | String | Tipo de bus |
| duration | String | Duración del viaje |
| status | String | Pendiente/A tiempo/Demorado |
| capacity | Long | Capacidad total |
| company | String | Empresa de transporte |
| driverId | String | UID del conductor asignado |
| driverName | String | Nombre del conductor |

### Ticket
| Campo | Tipo | Descripción |
|-------|------|-------------|
| id | String | ID único |
| userId | String | UID del usuario comprador |
| routeId | String | ID de la ruta |
| company | String | Empresa |
| origin | String | Origen |
| destination | String | Destino |
| departureTime | String | Hora de salida |
| seatNumbers | List\<Long\> | Asientos seleccionados |
| totalPrice | Double | Precio total |
| paymentMethod | String | Yape/Visa/Plin |
| status | String | confirmado/usado |
| createdAt | Long | Timestamp de creación |

---

## 3. Repositorios

### AuthRepository
- `login(email, password)` → Inicia sesión con Firebase Auth
- `register(name, email, password)` → Registra usuario + guarda en Firestore
- `resetPassword(email)` → Envía correo de restablecimiento
- `sendEmailVerification()` → Envía verificación de email
- `isEmailVerified()` → Verifica si el email está confirmado
- `updatePassword(newPassword)` → Actualiza contraseña
- `updateEmail(newEmail)` → Actualiza email (envía verificación)
- `reauthenticate(email, password)` → Re-autentica al usuario
- `deleteAccount()` → Elimina cuenta de Auth + Firestore
- `updateUser(user)` → Actualiza datos del usuario en Firestore
- `getUserDetails(uid)` → Obtiene datos de un usuario
- `getAllUsers()` → Lista todos los usuarios
- `updateUserRole(uid, role)` → Cambia el rol de un usuario
- `listenToUserDetails(uid)` → Flow con snapshot listener en tiempo real

### RouteRepository
- `createRoute(route)` → Crea ruta en Firestore
- `getRoutes()` → Obtiene todas las rutas
- `updateRoute(route)` → Actualiza ruta
- `deleteRoute(routeId)` → Elimina ruta
- `decrementCapacity(routeId, count)` → Reduce capacidad atómicamente
- `getPaginatedRoutes(lastVisibleId, pageSize)` → Carga paginada

### TicketRepository
- `saveTicket(ticket)` → Guarda ticket
- `getBookedSeatsForRoute(routeId)` → Asientos ocupados de una ruta
- `getUserTickets(userId)` → Tickets del usuario
- `getTicketsByRoute(routeId)` → Tickets de una ruta (para conductor)
- `updateTicketStatus(ticketId, status)` → Marcar ticket como usado

---

## 4. Autenticación

### Flujo de Login
1. Usuario ingresa email y contraseña
2. Validación de formato de email con `Validation.isValidEmail()`
3. `AuthRepository.login()` autentica con Firebase Auth
4. Verifica si el email está confirmado (`isEmailVerified()`)
5. Si no está verificado, muestra botón para reenviar verificación
6. Si está verificado, navega al Home

### Flujo de Registro
1. Validación de nombre (mín 2 caracteres)
2. Validación de email (formato regex)
3. Validación de contraseña (mín 8 chars, 1 letra, 1 número)
4. Confirmación de contraseña (debe coincidir)
5. `AuthRepository.register()` crea usuario + guarda en Firestore
6. Autoenvía email de verificación
7. Muestra mensaje para verificar email antes de iniciar sesión

### Recuperación de Contraseña
1. Diálogo con campo de email
2. `AuthRepository.resetPassword()` envía email con Firebase Auth
3. Snackbar de confirmación

### Eliminación de Cuenta
1. Re-autenticación con contraseña actual
2. Elimina documento en Firestore
3. Elimina usuario de Firebase Auth
4. Redirige al login

---

## 5. Panel de Administración

3 tabs: Crear/Editar Ruta | Gestionar Rutas | Usuarios

### Crear/Editar Ruta
- Campos: Empresa, Tipo de Bus, Origen, Destino, Fecha Salida/Llegada, Hora Salida/Llegada, Precio, Capacidad, Estado, Conductor
- Validación en tiempo real por campo
- DatePicker y TimePicker Material3
- Edición: formulario pre-poblado + indicador visual

### Gestionar Rutas
- Búsqueda/filtro por origen, destino, empresa
- Lista con tarjetas
- Editar y Eliminar (con confirmación)

### Usuarios
- Lista de todos los usuarios con roles
- Promover/Degradar entre Admin y Usuario
- Indicador visual de rol por color

---

## 6. Flujo de Compra

1. BusesScreen / ViajesScreen → lista de rutas disponibles
2. SeatSelectionScreen → grid 4 columnas (1-40 asientos), máx 5 por compra
   - Colores: gris=disponible, rojo=ocupado, primary=seleccionado
3. PaymentScreen → resumen + método de pago (Yape/Visa/Plin)
4. Guarda ticket en Firestore + decrementa capacidad atómicamente
5. TicketScreen → confirmación con QR (ZXing)

---

## 7. Pantallas de Visualización

### HomeScreen
- Stats de rutas disponibles
- Rol del usuario (Usuario/Admin/Chofer)
- Pull-to-refresh

### BusesScreen
- Lista de rutas con Pull-to-Refresh
- Tarjetas con: origen, destino, empresa, horarios, precio, capacidad, estado

### ViajesScreen
- Filtros de búsqueda por origen y destino
- Pull-to-Refresh

---

## 8. Notificaciones Push (FCM)

### BusifyMessagingService
- `onNewToken(token)`: Guarda automáticamente el token FCM en Firestore (`users/{uid}/fcmToken`)
- `onMessageReceived(message)`: Muestra notificación con título y cuerpo
- Canal de notificación "busify_notifications" para Android 8+

### Firebase Cloud Functions (functions/index.js)
- `onTicketCreated`: Notifica al usuario cuando compra un ticket
- `onRouteUpdated`: Notifica a todos los pasajeros si cambia el estado de una ruta
- `onRouteCreated`: Notifica a todos los usuarios cuando se crea una nueva ruta

---

## 9. Perfil de Usuario

### Funcionalidades
- **Foto de perfil**: Clic en avatar → selector de galería → Firebase Storage
- **Editar nombre**: Modo inline
- **Cambiar email**: Diálogo con re-autenticación + envío de verificación
- **Cambiar contraseña**: Diálogo con re-autenticación
- **Mis Viajes**: Expandible con historial de tickets + QR
- **Eliminar cuenta**: Con re-autenticación y confirmación
- **Cerrar sesión**

### Firebase Storage
- Ruta: `profile_photos/{uid}.jpg`
- Load con Coil AsyncImage
- Actualización automática de `User.photoUrl` en Firestore

---

## 10. Pantalla de Conductor

### Funcionalidades
- Lista de rutas asignadas (filtradas por `driverId`)
- Al seleccionar una ruta: lista de pasajeros con sus tickets
- Cada ticket muestra: origen/destino, asientos, método de pago, estado
- Botón "Usado": marca el ticket como usado (cambia a verde)
- Bottom nav item "Conducir" visible solo para role=3 (Chofer)

---

## 11. Validación de Formularios

### Validation.kt
- `isValidEmail(email)`: Usa `android.util.Patterns.EMAIL_ADDRESS`
- `isValidPassword(password)`: Mín 8 chars, 1 letra, 1 número
- `isValidName(name)`: Mín 2 caracteres, no blank

### Aplicado en
- LoginScreen: email format validation
- RegisterScreen: nombre, email, password strength, confirm match
- AdminScreen: validación por campo en tiempo real (FieldError)

---

## 12. Firebase Cloud Functions

### Configuración
- Node.js 18
- firebase-admin v12, firebase-functions v5
- Desplegar con: `firebase deploy --only functions`

### Funciones
| Función | Trigger | Comportamiento |
|---------|---------|----------------|
| onTicketCreated | tickets.onCreate | Notifica al usuario comprador |
| onRouteUpdated | routes.onUpdate | Notifica a pasajeros si cambia el estado |
| onRouteCreated | routes.onCreate | Notifica a todos los usuarios |

---

## 13. Dependencias

### build.gradle.kts (app)
```
Firebase BoM 34.13.0 → auth, firestore, messaging, storage
Coroutines Play Services 1.7.3
Coil Compose 2.6.0 (imágenes)
ZXing Core 3.5.3 (QR)
Splash Screen 1.2.0
Lottie Compose 6.6.4 (animaciones)
Compose Shimmer 1.3.1 (skeletons)
Compose BOM 2024.09.00 → Material3, Navigation, Icons Extended
```

### functions/package.json
```
firebase-admin ^12.0.0
firebase-functions ^5.0.0
```
