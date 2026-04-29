# App de Subastas  
**Proyecto:** Blackwood Fine Sales  
**Entrega:** Primera instancia  


# 1. Miembros del equipo

- Montaño Paz , Ana Maria Laura 
- Ferreiro, Rodrigo Martín
- Farran, Pedro
- Huayta Romay, Ezequiel Lautaro
- Di Giacinti, Nicolas Martin 


# 2. Flujos principales y wireframes

La aplicación fue diseñada en base a tres roles principales:

- 👤 Postor (cliente comprador)  
- 📦 Vendedor (cliente que aporta bienes)  
- 🧑‍💼 Empleado (backoffice / administración)  

Los wireframes fueron desarrollados en alta fidelidad en Figma, contemplando los flujos completos del sistema y diferenciando claramente las vistas de cliente y de backoffice.

📍 [**Link Figma**  ](https://www.figma.com/design/HrQNrdT0icluVfLEuWOSu6/TPO---desarrollo-de-apps-I?node-id=0-1&t=815KvgWRPN9Gisty-1)


## Organización en Figma

### 🟡 Page 1 — Wireframes + Paleta + Logo

Incluye:
 
🔐 Acceso y registro
- Login  
- Registro de postor  
- Registro en proceso  
- Completar registro  

###  Postor (cliente)
 🏠 Exploración y subastas
- Listado de subastas  
- Detalle de subasta (vista cliente):
  - No iniciada  
  - En proceso  
  - Finalizada  

🔴 Participación
- Subasta en vivo (activa)  
- Detalle de lote (incluye variante obra de arte)  

💰 Post-subasta
- Detalle notificación ganador  
- Detalle notificación perdedor  
- Permanecer en subasta (post-ganador)  

📊 Perfil
- Perfil  
- Métricas del usuario  
- Historial de participación  

### Vendedor
📤 Gestión de artículos
- Registro nuevo lote  
- Gestión de artículos  

📩 Proceso de evaluación
- Subasta asignada  
- Rechazar propuesta  
- Post-aprobación  

📍 Seguimiento
- Seguimiento del artículo (aseguradora)  
- Seguimiento del artículo (soporte)  

### Empleado (Backoffice)

🏛️ Gestión de subastas
- Gestión de subastas (creación, edición, configuración)  

📊 Monitoreo
- Panel de control  
- Seguimiento de subastas activas  
- Control de pujas  

## 🎨 Identidad visual
- Paleta de colores:
  - Verde oscuro `#1C2A21`  
  - Fondo claro `#F4F1EA`  
  - Dorado `#C6A75E`  
  - Blanco `#FFFFFF`  
  - Gris `#6B6B6B`  

- Logo integrado en las pantallas  

### Page 2 — Splash

Incluye:
- Pantalla inicial de carga  
- Fondo oscuro  
- Logo centrado  
- Refuerzo de identidad visual  


## Flujos principales

### 👤 Postor
- Registro en dos etapas con validación  
- Exploración de subastas  
- Participación en subasta en vivo  
- Recepción de resultados  
- Gestión de pago  
- Consulta de métricas  

---

### 📦 Vendedor
- Carga de artículos para consignación  
- Evaluación por parte de la empresa  
- Aceptación o rechazo de propuesta  
- Seguimiento del bien  
- Resultado de venta  

---

### 🧑‍💼 Empleado
- Administración de subastas (backoffice)  
- Monitoreo en tiempo real  
- Control de actividad de usuarios  
- Evaluación de artículos consignados  

# 3. API REST

La aplicación cuenta con una API REST documentada mediante Swagger (OpenAPI 3.0), la cual define todos los endpoints necesarios para gestionar el flujo completo de subastas.

📍 **Archivo Swagger** : ``swagger/blackwood_api.yaml  ``


## 🧩 Estructura general

La API está organizada en módulos funcionales (tags), que representan las principales áreas del sistema:

- **Auth** → autenticación y registro de usuarios  
- **Usuarios** → perfil, métricas y actividad  
- **Medios de Pago** → gestión de métodos de pago del postor  
- **Subastas** → consulta y administración de subastas  
- **Lotes** → catálogo y detalle de lotes  
- **Pujas** → participación en subastas en vivo  
- **Artículos para subasta** → consignación de bienes por parte del vendedor  
- **Notificaciones** → mensajes y eventos del sistema  

---

### 🔐 Autenticación y registro

El sistema utiliza autenticación basada en **JWT (Bearer Token)**.

El flujo de registro está dividido en dos etapas:
1. **Registro inicial** con datos personales y documentación (requiere validación por parte de la empresa)  
2. **Activación de cuenta** mediante la creación de contraseña  

Esto asegura control y validación de identidad antes de permitir la participación en subastas.


### 👤 Gestión de usuarios

Permite:
- Obtener y actualizar perfil  
- Consultar métricas (subastas ganadas, montos ofertados/pagados)  
- Ver historial de pujas  

También contempla:
- multas por incumplimiento de pago  
- validación de estado del usuario  


### 💳 Medios de pago

Los usuarios pueden:
- registrar múltiples medios de pago  
- eliminarlos (con restricciones si están en uso)  

Tipos soportados:
- cuenta bancaria  
- tarjeta de crédito  
- cheque certificado  

Todos los medios requieren verificación antes de ser utilizados en subastas.


### 🏛️ Subastas

Permite:
- listar subastas (con filtros por estado, categoría y moneda)  
- ver detalle completo  
- unirse a subastas en vivo  

Restricciones importantes:
- se requiere medio de pago verificado  
- un usuario solo puede estar en una subasta a la vez  


### 📦 Lotes

Permite:
- consultar catálogo de una subasta  
- ver detalle de cada lote  

El catálogo es público, pero:
- usuarios no autenticados no pueden ver el precio base  


### 🔴 Pujas (core del sistema)

Permite realizar ofertas en tiempo real con validaciones de negocio:

- la oferta debe ser mayor a la anterior  
- debe respetar un rango dinámico (mínimo y máximo)  
- no se puede ofertar sin confirmar la puja anterior  

También se contempla:
- historial de pujas (para empleados)  
- estado de cada oferta (vigente, superada, ganadora)  


### 💰 Checkout (post-subasta)

El ganador de un lote debe:

- confirmar el medio de pago  
- seleccionar modalidad de entrega:
  - envío a domicilio  
  - retiro personal  

Se genera una **factura** con:
- monto ofertado  
- comisión  
- costo de envío  
- total  


### 📤 Consignación de artículos (vendedor)

El vendedor puede:

1. Enviar artículos para evaluación (con fotos y descripción)  
2. Recibir una propuesta de:
   - valor base  
   - comisión  
3. Aceptar o rechazar la propuesta  

El sistema también contempla:
- inspección por parte de empleados  
- motivos de rechazo  
- asignación a subastas  


### 🔔 Notificaciones

Centraliza eventos del sistema:

- aprobación/rechazo de registro  
- resultados de subastas  
- aceptación/rechazo de artículos  
- multas  

Permite marcar notificaciones como leídas.


## ⚠️ Manejo de estados y errores

La API utiliza códigos HTTP estándar, tales como:

- `200 OK` → operación exitosa  
- `201 Created` → recurso creado  
- `204 No Content` → operación sin contenido de retorno  
- `400 Bad Request` → datos inválidos  
- `401 Unauthorized` → usuario no autenticado  
- `403 Forbidden` → sin permisos  
- `404 Not Found` → recurso inexistente  
- `409 Conflict` → conflicto de estado (ej: puja simultánea)  
