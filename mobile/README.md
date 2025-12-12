# 📱 Eventos App - Cliente Móvil

Aplicación móvil multiplataforma desarrollada con **Kotlin Multiplatform (KMP)** y **Compose Multiplatform**.

## 🏗️ Arquitectura

- **Shared Module**: Código compartido entre Android e iOS
  - UI con Compose Multiplatform
  - Lógica de negocio
  - Cliente HTTP (Ktor)
  - Modelos de datos
  - Navegación
  
- **Android App**: Aplicación nativa Android
- **iOS App**: Aplicación nativa iOS

## 🛠️ Tecnologías

### Framework Principal
- **Kotlin Multiplatform (KMP)** - Código compartido
- **Compose Multiplatform** - UI multiplataforma

### Dependencias Principales
- **Ktor Client** - Cliente HTTP para APIs REST
- **Kotlinx Serialization** - Serialización JSON
- **Kotlinx Coroutines** - Programación asíncrona
- **Voyager** - Navegación multiplataforma
- **Kotlinx DateTime** - Manejo de fechas

## 📁 Estructura del Proyecto

```
mobile/
├── shared/                      # Módulo compartido
│   └── src/
│       ├── commonMain/         # Código común
│       │   └── kotlin/
│       │       └── com/eventos/app/
│       │           ├── data/
│       │           │   ├── models/      # Modelos de datos
│       │           │   └── remote/      # Cliente API
│       │           ├── domain/          # Lógica de negocio
│       │           └── ui/              # Pantallas y componentes
│       ├── androidMain/        # Código específico Android
│       └── iosMain/            # Código específico iOS
│
├── androidApp/                 # Aplicación Android
│   └── src/main/
│       ├── kotlin/
│       └── AndroidManifest.xml
│
└── iosApp/                     # Proyecto Xcode (iOS)
```

## 🚀 Requisitos

### Para Android
- JDK 17 o superior
- Android Studio Hedgehog o superior
- Android SDK (API 24+)

### Para iOS
- macOS con Xcode 15+
- CocoaPods
- iOS 14.0+

## 🔧 Configuración

### 1. Clonar el repositorio
```bash
cd codigo/mobile
```

### 2. Sincronizar Gradle
```bash
./gradlew build
```

### 3. Ejecutar en Android
```bash
./gradlew :androidApp:installDebug
```

O abrir el proyecto en Android Studio y ejecutar.

### 4. Ejecutar en iOS
```bash
cd iosApp
pod install
open iosApp.xcworkspace
```

O abrir `iosApp.xcworkspace` en Xcode y ejecutar.

## 🌐 Configuración del Backend

El cliente se conecta al backend en:
- **Desarrollo**: `http://localhost:8080`
- **Producción**: Configurar en `ApiClient.kt`

## 📱 Funcionalidades

- ✅ Login y registro de usuarios
- ✅ Listado de eventos con paginación
- ✅ Detalle de eventos
- ✅ Mapa interactivo de asientos
- ✅ Selección de asientos (máximo 4)
- ✅ Carga de datos de asistentes
- ✅ Proceso de compra
- ✅ Historial de ventas
- ✅ Manejo de sesiones concurrentes

## 🧪 Testing

```bash
# Tests compartidos
./gradlew :shared:test

# Tests Android
./gradlew :androidApp:test
```

## 📦 Build para Producción

### Android (APK/AAB)
```bash
./gradlew :androidApp:assembleRelease
# O para App Bundle
./gradlew :androidApp:bundleRelease
```

### iOS (Archive)
Desde Xcode: Product → Archive

## 🎨 UI/UX

- **Material Design 3** (Android)
- **Cupertino** (iOS adaptado)
- **Tema responsive** con soporte para modo oscuro

## 📝 Estado del Proyecto

✅ **ISSUE-031**: Inicialización del proyecto móvil - **Completado**
- ✅ Proyecto KMP configurado
- ✅ Estructura de directorios
- ✅ Dependencias base instaladas
- ✅ Cliente HTTP configurado
- ✅ Modelos de datos básicos
- ✅ Aplicación Android funcional

⏳ **Siguientes pasos**: 
- ISSUE-032: Configuración de navegación
- ISSUE-033: Cliente HTTP y gestión de estado
- ISSUE-034+: Implementación de pantallas

## 👥 Desarrolladores

- Agustin Benavidez - Universidad de Mendoza

## 📄 Licencia

MIT License
