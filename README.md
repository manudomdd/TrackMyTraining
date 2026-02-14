# 🏋️ TrackMyTraining

> Aplicación Android para registrar y llevar el control de tus entrenamientos de gimnasio — series, repeticiones, peso y RIR.

---

## ✨ Características

### 📅 Calendario de Entrenamientos
- Selecciona cualquier fecha para registrar o consultar el entreno de ese día.
- Interfaz limpia con el calendario integrado en una tarjeta redondeada.

### 📝 Registro de Series
Registra cada serie de tu entrenamiento con los siguientes campos:

| Campo | Descripción |
|---|---|
| **Ejercicio** | Nombre del ejercicio (ej. Press Banca, Sentadilla) |
| **Serie #** | Número de serie (auto-incremento automático) |
| **Reps** | Repeticiones realizadas |
| **Peso (kg)** | Peso utilizado en la serie |
| **RIR** | Reps In Reserve — cuántas repeticiones te quedan "en la recámara" |

### 📋 Historial del Día
- Visualiza todas las series registradas del día en tarjetas elegantes.
- Cada tarjeta muestra: nombre del ejercicio, peso × reps, RIR, y número de serie.
- Ordenadas cronológicamente (de arriba a abajo, según las vas añadiendo).

### 🗑️ Borrado Flexible
- **Borrado individual**: Pulsación larga sobre una tarjeta para eliminar esa serie.
- **Borrado múltiple**: Checkboxes en cada serie para seleccionar varias y eliminarlas de golpe con un solo botón.

---

## 🎨 Diseño

- **Material Design 3** con tema oscuro premium.
- Paleta de colores violeta/púrpura sobre fondo negro profundo.
- Componentes Material: `MaterialCardView`, `TextInputLayout` (OutlinedBox), `MaterialButton`.
- Tipografía clara con jerarquía visual definida.
- Animaciones de layout al añadir/eliminar series.

---

## 🛠️ Stack Tecnológico

| Tecnología | Uso |
|---|---|
| **Java** | Lenguaje principal |
| **Android SDK** | Framework de desarrollo |
| **Material Components** | Componentes UI modernos |
| **SQLite** | Base de datos local para persistencia |
| **Gradle (Kotlin DSL)** | Sistema de build |

---

## 📁 Estructura del Proyecto

```
app/src/main/
├── java/com/example/trackmytraining/
│   ├── MainActivity.java          # Pantalla principal con calendario
│   ├── RegistroActivity.java      # Pantalla de registro de series
│   └── AdminSQLiteOpenHelper.java # Base de datos SQLite
└── res/
    ├── layout/
    │   ├── activity_main.xml      # Layout del calendario
    │   └── activity_registro.xml  # Layout del registro
    ├── drawable/                   # Shapes y fondos personalizados
    └── values/
        ├── colors.xml             # Paleta de colores M3
        ├── themes.xml             # Tema Material Design 3
        └── strings.xml            # Recursos de texto
```

---

## 📱 Capturas

> *Próximamente*

---

## 🚀 Cómo ejecutar

1. Clona el repositorio:
   ```bash
   git clone https://github.com/tu-usuario/TrackMyTraining.git
   ```
2. Abre el proyecto en **Android Studio**.
3. Sincroniza Gradle y ejecuta en un emulador o dispositivo físico (API 24+).

---

## 📄 Licencia

Este proyecto es de uso personal. Siéntete libre de usarlo como referencia o inspiración.

---

<p align="center">
  Hecho con 💪 para no olvidar ni una serie.
</p>
