# 📘 README — App de Subastas  
TPO Subastas, Desarrollo de aplicaciones 1 - Grupo 16

**Proyecto:** Blackwood Fine Sales   

---

# 👥 1. Miembros del equipo

- Montaño Paz Ana Maria Laura
- Farran Pedro
- Huayta Romay Ezequiel Lautaro
- Ferreiro Rodrigo Martin
- Di Giacinti Nicolas Martin

---

# 🧭 2. Flujos principales y wireframes

La aplicación fue diseñada en base a tres roles principales:

- 👤 Postor (comprador)  
- 📦 Vendedor (dueño del bien)  
- 🧑‍💼 Empleado (administrador)  

Los wireframes fueron desarrollados en alta fidelidad utilizando Figma, organizados en páginas para facilitar su navegación y comprensión.

📍 **Link Figma:**  
https://www.figma.com/design/HrQNrdT0icluVfLEuWOSu6/TPO---desarrollo-de-apps-I?node-id=0-2059&t=KgxuzhadGDd5Aw84-1

---

## 🎨 Organización en Figma

### 🟡 Page 1 — Wireframes + Paleta de colores

**Pantallas implementadas:**
- Subasta en vivo (postor)  
- Resultado de subasta (ganador / perdedor)  
- Seguimiento del bien (vendedor)  
- Dashboard de monitoreo (empleado)  
- Perfil y métricas (postor)  

**Paleta de colores:**
- Verde oscuro `#1C2A21`  
- Fondo claro `#F4F1EA`  
- Dorado `#C6A75E`  
- Blanco `#FFFFFF`  
- Gris `#6B6B6B`  

---

### 🎬 Page 2 — Splash

Incluye:
- Pantalla inicial de la aplicación  
- Fondo oscuro  
- Logo centrado  
- Identidad visual de la app  

---

## 🔄 Flujos principales

### 👤 Postor
- Participación en subasta en vivo  
- Visualización de resultados (ganador / perdedor)  
- Gestión de pagos  
- Consulta de métricas  

### 📦 Vendedor
- Seguimiento del estado del bien  
- Aceptación / rechazo de condiciones  
- Visualización de resultados de venta  

### 🧑‍💼 Empleado
- Monitoreo de subastas activas  
- Control de pujas  
- Visualización de logs  

---

# 📡 3. API REST

El diseño completo de la API se documentó utilizando Swagger.

📍 **Archivo en el repositorio:**  
https://github.com/rmferreiro/DA1-TPO_Subastas-G16/blob/dev/swagger.yaml

📍 **Ver documentación interactiva Swagger UI:**  
https://editor.swagger.io/?url=https://raw.githubusercontent.com/rmferreiro/DA1-TPO_Subastas-G16/blob/dev/swagger.yaml

---

## 🔹 Ejemplo: Realizar una puja

### POST `/subastas/{id}/ofertar`

**Request:**
```json
{
  "monto": 1000
}
```
# 💻 4. Código implementado en la repo

En el repositorio se incluyen implementaciones iniciales orientadas a validar la navegación y estructura de la aplicación.

## 📱 Pantallas / funcionalidades implementadas

- Login de usuario (navegación básica entre pantallas)  
- Listado de subastas  
- Estructura inicial de actividades  
- Navegación entre vistas principales  

## 🎯 Objetivo

Estas implementaciones permiten validar:
- el flujo de navegación de la app  
- la estructura base del proyecto  
- la interacción inicial del usuario  

## 🚧 Estado actual

- Implementación parcial (prototipo funcional)  
- Sin integración con backend real  
- Datos simulados en interfaz  

## 🔜 Próximos pasos

- Integración con la API REST definida  
- Manejo de estados y validaciones  
- Conexión con datos dinámicos  