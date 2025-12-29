# 📱 Configuración del Mobile (Kotlin Multiplatform)

## ✅ Estado Actual

- **Gradle wrapper configurado**: Gradle 8.5
- **Targets iOS desactivados**: Solo se compila Android (iOS requiere Xcode)
- **Código 100% implementado**: Todas las pantallas, ViewModels y repositories están completos

---

## 🚀 Cómo ejecutar en Android Studio

### 1. Abrir el proyecto

```bash
# Desde Android Studio:
File → Open → seleccionar carpeta: codigo/mobile/
```

### 2. Esperar sincronización

- Android Studio sincronizará automáticamente el proyecto con Gradle
- Esto puede tardar 5-10 minutos la primera vez
- Se descargarán todas las dependencias (Kotlin, Compose, Ktor, etc.)

### 3. Configurar URL del backend

**Antes de ejecutar, actualizar la URL del backend en:**

`shared/src/commonMain/kotlin/com/eventos/app/data/remote/ApiClient.kt`

```kotlin
// Para emulador Android:
private const val BASE_URL = "http://10.0.2.2:8080"

// Para dispositivo físico en la misma red:
private const val BASE_URL = "http://<TU_IP_LOCAL>:8080"
```

### 4. Ejecutar la app

```
Run → androidApp
```

O desde la línea de comandos:

```bash
./gradlew :androidApp:installDebug
```

---

## 🏗️ Arquitectura Implementada (MVVM)

### **View**: Screens (Compose UI)
- `LoginScreen.kt` - Pantalla de login
- `EventListScreen.kt` - Lista de eventos
- `EventDetailScreen.kt` - Detalle del evento
- `SeatMapScreen.kt` - Mapa de asientos
- `PersonDataScreen.kt` - Datos de personas
- `ConfirmationScreen.kt` - Confirmación y compra
- `SalesHistoryScreen.kt` - Historial de compras

### **ViewModel**: ScreenModels (Voyager + StateFlow)
- `LoginScreenModel.kt` - Maneja lógica de login
- `EventListScreenModel.kt` - Maneja lista de eventos
- `SeatMapScreenModel.kt` - Maneja selección de asientos
- Etc.

**Características:**
- ✅ `StateFlow` para estado reactivo
- ✅ `screenModelScope` para coroutines
- ✅ Separación de lógica de negocio de UI

### **Model**: Repositories + Remote
- `ApiClient.kt` - Cliente HTTP (Ktor)
- `AuthRepository.kt` - Autenticación
- `EventoRepository.kt` - Consulta de eventos
- `SesionRepository.kt` - Gestión de sesiones
- `AsientoRepository.kt` - Bloqueo de asientos
- `VentaRepository.kt` - Compras

---

## 📦 Dependencias Principales

```kotlin
// Compose Multiplatform
implementation(compose.runtime)
implementation(compose.foundation)
implementation(compose.material3)

// Ktor Client (HTTP)
implementation("io.ktor:ktor-client-core:2.3.7")
implementation("io.ktor:ktor-client-content-negotiation:2.3.7")
implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.7")

// Navigation
implementation("cafe.adriel.voyager:voyager-navigator:1.0.0")

// ViewModel
implementation("cafe.adriel.voyager:voyager-screenmodel:1.0.0")
```

---

## 🎯 Para la Presentación

### **Enfoque recomendado:**

1. **Mostrar estructura de carpetas** en Android Studio
   - Explicar arquitectura MVVM
   - Mostrar separación domain/data/ui

2. **Abrir un ViewModel** (ej: `LoginScreenModel.kt`)
   - Mostrar `ScreenModel` (equivalente a Android ViewModel)
   - Mostrar `StateFlow<LoginUiState>`
   - Explicar manejo de estado reactivo

3. **Abrir un Repository** (ej: `AuthRepository.kt`)
   - Mostrar uso de `ApiClient`
   - Mostrar llamadas HTTP con Ktor
   - Explicar separación de responsabilidades

4. **Abrir una Screen** (ej: `EventListScreen.kt`)
   - Mostrar Compose UI
   - Mostrar `collectAsState()` del StateFlow
   - Explicar UI reactiva

5. **Mencionar que:**
   - El código está 100% implementado
   - No se alcanzó a probar por priorizar la arquitectura hexagonal del backend
   - Es un proyecto Kotlin Multiplatform (puede compilar para iOS)

---

## ❓ Troubleshooting

### Si Android Studio no sincroniza:

```bash
# Limpiar y regenerar
cd codigo/mobile
./gradlew clean
./gradlew --refresh-dependencies
```

### Si hay problemas con Gradle:

```bash
# Regenerar wrapper
./gradlew wrapper --gradle-version 8.5
```

### Si aparecen errores de Xcode/iOS:

Los targets de iOS ya están desactivados en `shared/build.gradle.kts`.
Solo se debe compilar la app de Android.

---

## 📝 Notas Adicionales

- **Kotlin**: 1.9.20
- **Compose**: 1.5.11
- **Android minSdk**: 24
- **Android targetSdk**: 34
- **JVM target**: 17

---

**Creado para**: Presentación Final - Programación II
**Fecha**: Diciembre 2024


