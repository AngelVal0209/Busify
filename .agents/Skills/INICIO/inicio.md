# Jetpack Compose — Skill UI/UX (Solo Diseño)

## Objetivo

Desarrollar únicamente el diseño visual de una aplicación móvil usando Jetpack Compose.

La aplicación NO debe incluir:

* backend
* autenticación real
* base de datos
* API
* lógica de negocio
* Firebase
* sockets
* almacenamiento
* consumo de servicios
* arquitectura compleja innecesaria

Todo será únicamente visual/UI.

---

# Pantallas Requeridas

Crear únicamente estas pantallas:

1. Login
2. Registro
3. Inicio (Home)
4. Lista de Buses Disponibles

---

# Importante

## Solo UI/UX

La aplicación debe enfocarse únicamente en:

* diseño moderno
* navegación visual
* animaciones
* experiencia móvil
* prototipado profesional

NO implementar funcionalidad real.

---

# Login Mock Visual

Mostrar únicamente como placeholder:

```txt id="t4n8zp"
Usuario: 123
Contraseña: 123
```

No validar credenciales reales.

---

# Navegación

Usar navegación únicamente para mover entre pantallas visualmente.

## Recomendado

```kotlin id="u2m7xy"
Navigation Compose
```

---

# Navbar Inferior

Implementar un `Bottom Navigation Bar` moderno.

## Tabs

* Inicio
* Buses
* Perfil

---

# UX Mobile

La app debe sentirse moderna y nativa.

## Incluir

* animaciones suaves
* navegación fluida
* scroll fluido
* transiciones modernas
* soporte dark mode preparado
* diseño responsive
* safe areas

---

# Gestos

La navegación debe permitir:

* swipe back
* historial visual
* experiencia táctil natural

---

# Diseño Visual

## Estilo esperado

La interfaz debe ser:

* minimalista
* limpia
* moderna
* profesional
* consistente

---

# Componentes Reutilizables

Crear componentes UI reutilizables para:

* botones
* text fields
* cards
* navbar
* headers
* tarjetas de buses

---

# Lista de Buses

Mostrar únicamente datos mock visuales.

## Cada tarjeta debe contener

* nombre del bus
* código
* horario
* estado
* capacidad

---

# Home Screen

Debe contener:

* bienvenida visual
* resumen rápido
* acceso a buses
* navbar inferior

---

# Registro

Pantalla visual únicamente.

Campos:

* nombre
* correo
* contraseña
* confirmar contraseña

---

# Arquitectura UI Recomendada

Mantener estructura limpia para escalabilidad visual.

```txt id="l5r9dn"
app/
│
├── core/
│   ├── theme/
│   ├── navigation/
│   └── components/
│
├── features/
│   ├── auth/
│   ├── home/
│   └── buses/
│
└── MainActivity.kt
```

---

# Material Design

Usar:

* Material 3
* tipografía moderna
* espaciado uniforme
* colores consistentes
* componentes modernos de Compose

---

# Dependencias Recomendadas

```gradle id="g3w7pk"
implementation("androidx.compose.material3:material3")
implementation("androidx.navigation:navigation-compose")
implementation("io.coil-kt:coil-compose")
```

---

# Resultado Esperado

La aplicación debe parecer una app real moderna enfocada únicamente en:

* UI
* UX
* navegación visual
* experiencia móvil
* prototipado profesional

Sin backend ni lógica funcional real.
