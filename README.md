# Cash Manager

Cash Manager es una aplicación Android para gestionar finanzas personales y tareas académicas de estudiantes universitarios. La app permite controlar gastos, presupuestos, ingresos, métodos de pago, recordatorios y agenda.

## Características principales

- Registro e inicio de sesión de usuarios
- Gestión de ingresos y gastos por método de pago
- Presupuestos mensuales con desglose por cuenta
- Seguimiento de recordatorios y entregas académicas
- Dashboard con indicadores financieros clave
- Persistencia local con Room
- Interfaz construida con Jetpack Compose

## Requisitos

- JDK 17 o superior
- Android Studio Ladybug o superior
- Android SDK 36 (o el requerido por el proyecto)
- Git

## Configuración inicial

1. Clona el repositorio.
2. Abre el proyecto en Android Studio.
3. Asegúrate de que el SDK de Android esté instalado.
4. Si no existe, genera la configuración del wrapper de Gradle con:

```bash
./gradlew wrapper
```

5. Crea o edita `local.properties` si Android Studio no lo genera automáticamente:

```properties
sdk.dir=C:\Users\TU_USUARIO\AppData\Local\Android\Sdk
```

6. Si usas secretos de Firebase o Gemini, configura el archivo `.env` basado en `.env.example`.

## Ejecutar la aplicación

```bash
./gradlew assembleDebug
```

O desde Android Studio, selecciona "Run app".

## Estructura general

```text
app/
  src/main/java/com/example/      # Código principal de la app
  src/main/res/                  # Recursos de Android
  src/test/                      # Pruebas unitarias
  src/androidTest/               # Pruebas de instrumentación
build.gradle.kts                # Configuración del módulo app
settings.gradle.kts             # Configuración del proyecto
```

## Seguridad

Las contraseñas no se almacenan en texto plano. La app usa un hash PBKDF2 con sal aleatoria para verificar credenciales de forma segura.

## Estado del proyecto

Este repositorio aún se encuentra en desarrollo. La prioridad actual es consolidar la base del proyecto, mejorar la seguridad y preparar la capa de gestión de usuarios antes de continuar con más funcionalidades.
