# Guía Maestra para el Examen: UI y Componentes - Busify 🚀

Esta sección está diseñada para que puedas realizar cambios rápidos durante el examen. Aquí tienes el "Cómo se hace" de las tareas más comunes.

---

## 🛠 Tarea 1: Modificar Pantallas Existentes (Layouts)

Las pantallas (como `LoginScreen.kt` o `HomeScreen.kt`) usan **Column** y **Row** para organizar todo.

### Cómo agregar elementos:
*   **Column**: Apila cosas una debajo de otra (Vertical).
*   **Row**: Pone cosas una al lado de la otra (Horizontal).
*   **Spacer**: Crea espacio vacío entre elementos. Use `Modifier.height(24.dp)` para vertical o `width(16.dp)` para horizontal.

**Ejemplo: Unir dos botones horizontalmente (Row)**
Si te piden poner un botón de "Cancelar" al lado de "Aceptar":
```kotlin
Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(16.dp) // Espacio entre botones
) {
    BusifyOutlinedButton(
        text = "Cancelar",
        onClick = { /* Acción */ },
        modifier = Modifier.weight(1f) // Esto hace que ocupen el mismo tamaño
    )
    BusifyButton(
        text = "Aceptar",
        onClick = { /* Acción */ },
        modifier = Modifier.weight(1f)
    )
}
```

---

## 🎨 Tarea 2: Cambiar el Color de un Solo Botón

Si te piden que **SOLO UN** botón sea rojo (por ejemplo, "Eliminar"), no cambies el tema global. Usa el parámetro `containerColor`.

**En la pantalla donde esté el botón:**
```kotlin
BusifyButton(
    text = "Eliminar Cuenta",
    onClick = { /* ... */ },
    containerColor = Color.Red // Forzamos el color rojo solo aquí
)
```

---

## 📝 Tarea 3: Modificar Formularios (TextFields)

Los formularios están en `LoginScreen.kt` o `RegisterScreen.kt`.

*   **Cambiar el Icono**: Busca `leadingIcon = Icons.Default.Email` y cámbialo por `Icons.Default.Person`, `Icons.Default.Lock`, etc.
*   **Hacerlo tipo Contraseña**: Asegúrate de tener `visualTransformation = PasswordVisualTransformation()`.
*   **Capturar el Texto**: El texto vive en una variable `remember { mutableStateOf("") }`. Lo que el usuario escribe llega a través de `onValueChange`.

---

## 🧩 Tarea 4: Editar el "Corazón" de los Componentes (`core`)

Si te piden cambiar algo que afecte a **TODA** la app, ve a la carpeta `core`.

### En `Buttons.kt`:
*   **Cambiar el borde**: Busca `shape = RoundedCornerShape(16.dp)`. Si pones `0.dp`, serán cuadrados.
*   **Cambiar el grosor del texto**: Busca `FontWeight.Bold` y cámbialo a `FontWeight.Normal`.

### En `TextFields.kt`:
*   **Cambiar el color de la letra**: Busca `colors = OutlinedTextFieldDefaults.colors(...)` y añade `focusedTextColor = Color.Black`.

---

## 🚀 Tarea 5: El "Machete" de Estilos Rápidos

| Si te piden... | Haz esto... |
| :--- | :--- |
| **Separar elementos** | Añade `Spacer(modifier = Modifier.height(20.dp))` |
| **Centrar algo** | En el `Column`, pon `horizontalAlignment = Alignment.CenterHorizontally` |
| **Cambiar texto del Home** | Ve a `HomeScreen.kt` y busca el `Text("Hola, Nicolás")` |
| **Cambiar el nombre de la App** | Ve a `strings.xml` o directamente en el `MainActivity.kt` / `NavGraph.kt` |
| **Navegar a otra pantalla** | Usa `navController.navigate(Screen.Nombre.route)` |

---

## 💡 Pro-Tip para el Examen:
Si algo se rompe o se ve mal alineado, revisa los `Modifier`.
*   `.fillMaxWidth()`: Ocupa todo el ancho.
*   `.padding(16.dp)`: Margen interno para que no toque los bordes.
*   `.weight(1f)`: En un `Row` o `Column`, reparte el espacio de forma equitativa.

> [!CAUTION]
> **¡Cuidado!** Si modificas `BusifyButton` directamente en la carpeta `core`, cambiarás TODOS los botones de la app. Si solo quieres cambiar uno, hazlo desde la pantalla donde se encuentra usando los parámetros.
