<br />
<div align="center">
<h3 align="center">PRAGMA POWER-UP - FOOD COURT MICROSERVICE</h3>
  <p align="center">
    Microservicio principal de gestión de plazoleta de comidas. Administra restaurantes, platos, pedidos y métricas de eficiencia.
  </p>
</div>

### Built With

* ![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=java&logoColor=white)
* ![Spring](https://img.shields.io/badge/Spring-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
* ![Spring Security](https://img.shields.io/badge/Spring_Security-6DB33F?style=for-the-badge&logo=Spring-Security&logoColor=white)
* ![JWT](https://img.shields.io/badge/JWT-black?style=for-the-badge&logo=JSON%20web%20tokens)
* ![Gradle](https://img.shields.io/badge/Gradle-02303A.svg?style=for-the-badge&logo=Gradle&logoColor=white)
* ![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white)
* ![Feign](https://img.shields.io/badge/Feign-Client-green?style=for-the-badge)

## Descripción General

Este es el microservicio **central** del sistema de plazoleta de comidas. Gestiona toda la lógica de negocio relacionada con:

- **Restaurantes**: Creación y consulta de restaurantes (solo ADMINISTRADORES)
- **Platos**: Gestión completa del menú (crear, modificar, habilitar/deshabilitar) por PROPIETARIOS
- **Pedidos**: Ciclo completo de pedidos desde creación hasta entrega
- **Trazabilidad**: Integración con microservicio de auditoría para registrar cambios
- **Notificaciones**: Envío de SMS cuando pedidos están listos
- **Métricas**: Consulta de eficiencia de pedidos por restaurante y empleado

**Puerto:** 8082  
**Base de datos:** PostgreSQL (powerup_foodcourt)

### Arquitectura

El proyecto sigue **Arquitectura Hexagonal (Puertos y Adaptadores)** con **API-First Design**:

```
src/
├── domain/              # Lógica de negocio pura
│   ├── model/          # Modelos de dominio
│   ├── usecase/        # Casos de uso
│   ├── api/            # Puertos de entrada
│   └── spi/            # Puertos de salida
├── application/         # Capa de aplicación
│   ├── handler/        # Handlers
│   └── mapper/         # Mappers (MapStruct)
└── infrastructure/      # Adaptadores
    ├── input/rest/     # Controladores REST
    ├── out/jpa/        # Persistencia JPA
    ├── out/http/       # Clientes HTTP (Feign)
    └── security/       # Seguridad JWT
```

---

### Comunicación entre Microservicios

```
foodcourt ──────> foodCourt-users (validar roles y usuarios)
          └─────> trazability-audit (registrar auditorías)
          └─────> message-sms (enviar notificaciones)
                           └─────> Twilio API (SMS)
```

---

## 📊 Historias de Usuario Implementadas

El proyecto cubre **18 Historias de Usuario** completas:

### 🔐 Autenticación y Usuarios (4 HU)

| ID | Historia | Microservicio | Rol |
|----|----------|---------------|-----|
| HU-1 | Crear Propietario | foodCourt-users | Administrador |
| HU-5 | Autenticación al sistema | foodCourt-users | Todos |
| HU-6 | Crear cuenta empleado | foodCourt-users | Propietario |
| HU-8 | Crear cuenta Cliente | foodCourt-users | - |

### 🍽️ Restaurantes y Platos (5 HU)

| ID | Historia | Microservicio | Rol |
|----|----------|---------------|-----|
| HU-2 | Crear Restaurante | foodcourt | Administrador |
| HU-3 | Crear Plato | foodcourt | Propietario |
| HU-4 | Modificar Plato | foodcourt | Propietario |
| HU-7 | Habilitar/Deshabilitar Plato | foodcourt | Propietario |
| HU-9 | Listar los restaurantes | foodcourt | Cliente |
| HU-10 | Listar los platos de un restaurante | foodcourt | Cliente |

### 📦 Gestión de Pedidos (7 HU)

| ID | Historia | Microservicio | Rol |
|----|----------|---------------|-----|
| HU-11 | Realizar pedido | foodcourt | Cliente |
| HU-12 | Obtener lista de pedidos | foodcourt | Empleado |
| HU-13 | Asignarse a un pedido | foodcourt | Empleado |
| HU-14 | Notificar pedido listo | foodcourt + message-sms | Empleado |
| HU-15 | Entregar pedido | foodcourt | Empleado |
| HU-16 | Cancelar pedido | foodcourt | Cliente |

### 📈 Trazabilidad y Métricas (2 HU)

| ID | Historia | Microservicio | Rol |
|----|----------|---------------|-----|
| HU-17 | Consultar trazabilidad | trazability-audit | Cliente |
| HU-18 | Consultar eficiencia | foodcourt | Propietario |

---

## Endpoints Implementados

La colleccion de **Postman** con todos los endpoints está disponible [aquí](MicroserviciosPragmaFoodCourt.postman_collection).

### Restaurantes

#### `POST /restaurants`
Crear un nuevo restaurante (solo ADMINISTRADOR).

**Headers:**
```
Authorization: Bearer <token>
```

**Request Body:**
```json
{
  "name": "Restaurante Ejemplo",
  "nit": "900123456",
  "address": "Calle 123 #45-67",
  "phoneNumber": "+573001234567",
  "logoUrl": "https://example.com/logo.png",
  "ownerId": 5
}
```

**Response (201 Created):**
```json
{
  "data": {
    "id": 1,
    "name": "Restaurante Ejemplo",
    "nit": "900123456",
    "address": "Calle 123 #45-67",
    "phoneNumber": "+573001234567",
    "logoUrl": "https://example.com/logo.png",
    "ownerId": 5
  }
}
```

---

#### `GET /restaurants`
Listar restaurantes paginado (orden alfabético).

**Query Parameters:**
- `page`: Número de página (default: 0)
- `size`: Tamaño de página (default: 10)

**Response (200 OK):**
```json
{
  "data": [
    {
      "name": "Restaurante Ejemplo",
      "logoUrl": "https://example.com/logo.png"
    }
  ],
  "meta": {
    "page": 0,
    "size": 10,
    "totalElements": 1,
    "totalPages": 1
  }
}
```

---

### Platos

#### `POST /dishes`
Crear un plato (solo PROPIETARIO del restaurante).

**Headers:**
```
Authorization: Bearer <token>
```

**Request Body:**
```json
{
  "name": "Hamburguesa Especial",
  "price": 25000,
  "description": "Deliciosa hamburguesa con queso y tocino",
  "urlImage": "https://example.com/burger.png",
  "category": "COMIDA_RAPIDA", 
  "restaurantId": 1
}
```

**Response (201 Created):**
```json
{
  "data": {
    "id": 1,
    "name": "Hamburguesa Especial",
    "price": 25000,
    "description": "Deliciosa hamburguesa con queso y tocino",
    "urlImage": "https://example.com/burger.png",
    "category": "COMIDA_RAPIDA",
    "active": true,
    "restaurantId": 1
  }
}
```

---

#### `PUT /dishes/{id}`
Actualizar precio y descripción de un plato (solo PROPIETARIO).

**Request Body:**
```json
{
  "price": 28000,
  "description": "Nueva descripción actualizada"
}
```

---

#### `PATCH /dishes/{id}/status`
Habilitar/Deshabilitar un plato (solo PROPIETARIO).

**Request Body:**
```json
{
  "active": false
}
```

---

#### `GET /restaurants/{restaurantId}/dishes`
Listar platos de un restaurante (con filtros opcionales).

**Query Parameters:**
- `restaurantId`: ID del restaurante (obligatorio)
- `category`: Categoría del plato (opcional)
- `page`: Número de página (default: 0)
- `size`: Tamaño de página (default: 10)

**Response (200 OK):**
```json
{
  "data": [
    {
      "id": 1,
      "name": "Hamburguesa Especial",
      "price": 25000,
      "description": "Deliciosa hamburguesa",
      "imageUrl": "https://example.com/burger.png",
      "category": "COMIDA_RAPIDA",
      "active": true
    }
  ],
  "meta": {
    "page": 0,
    "size": 10,
    "totalElements": 1,
    "totalPages": 1
  }
}
```

---

#### `PATCH /dishes/{dishId}/toggle`
Toggle para habilitar o deshabilitar un plato.

---

### Pedidos

#### `POST /orders`
Crear un nuevo pedido (solo CLIENTE).

**Headers:**
```
Authorization: Bearer <token>
```

**Request Body:**
```json
{
  "restaurantId": 1,
  "dishes": [
    {
      "dishId": 1,
      "quantity": 2
    },
    {
      "dishId": 3,
      "quantity": 1
    }
  ]
}
```

**Response (201 Created):**
```json
{
  "data": {
    "id": 1,
    "restaurantId": 1,
    "clientId": 10,
    "status": "PENDIENTE",
    "createdAt": "2025-12-07T10:30:00",
    "dishes": [
      {
        "dishId": 1,
        "dishName": "Hamburguesa Especial",
        "quantity": 2,
        "unitPrice": 25000
      }
    ],
    "total": 50000
  }
}
```

---

#### `GET /orders`
Listar pedidos con filtros (EMPLEADO ve solo de su restaurante).

**Query Parameters:**
- `status`: Estado del pedido (opcional)
- `page`: Número de página (default: 0)
- `size`: Tamaño de página (default: 10)

**Response (200 OK):**
```json
{
  "data": [
    {
      "id": 1,
      "restaurantId": 1,
      "clientId": 10,
      "employeeId": null,
      "status": "PENDIENTE",
      "createdAt": "2025-12-07T10:30:00"
    }
  ],
  "meta": {
    "page": 0,
    "size": 10,
    "totalElements": 1,
    "totalPages": 1
  }
}
```

---

#### `PATCH /orders/{id}/assign`
Asignarse a un pedido y cambiar estado a EN_PREPARACION (solo EMPLEADO).

**Response (200 OK):**
```json
{
  "data": {
    "id": 1,
    "status": "EN_PREPARACION",
    "employeeId": 7
  }
}
```

---

#### `PATCH /orders/{id}/ready`
Marcar pedido como listo y enviar SMS con PIN (solo EMPLEADO).

**Response (200 OK):**
```json
{
  "data": {
    "id": 1,
    "status": "LISTO",
    "securityPin": "1234",
    "smsStatus": "SENT"
  }
}
```

---

#### `PATCH /orders/{id}/deliver`
Entregar pedido validando PIN (solo EMPLEADO).

**Request Body:**
```json
{
  "securityPin": "1234"
}
```

**Response (200 OK):**
```json
{
  "data": {
    "id": 1,
    "status": "ENTREGADO",
    "deliveredAt": "2025-12-07T11:00:00"
  }
}
```

---

#### `PATCH /orders/{id}/cancel`
Cancelar pedido (solo CLIENTE, solo si está PENDIENTE).

**Response (200 OK):**
```json
{
  "data": {
    "id": 1,
    "status": "CANCELADO"
  }
}
```

---

### Métricas

#### `GET /metrics/orders-duration?restaurantId={restaurantId}`
Consultar eficiencia(tiempos) de pedidos por restaurante (solo PROPIETARIO).

**Response (200 OK):**
```json
{
  "data": {
    "orders": [
      {
        "orderId": 1,
        "clientId": 10,
        "employeeId": 7,
        "startedAt": "2025-12-02T10:00:00Z",
        "completedAt": "2025-12-02T10:45:00Z",
        "finalStatus": "ENTREGADO",
        "durationMinutes": 45
      }
    ],
    "summary": {
      "totalOrders": 150,
      "averageDurationMinutes": 30.5,
      "minDurationMinutes": 15,
      "maxDurationMinutes": 120,
      "medianDurationMinutes": 28.0,
      "deliveredCount": 140,
      "cancelledCount": 10
    }
  },
  "meta": {
    "page": 0,
    "size": 10,
    "totalElements": 100,
    "totalPages": 10
  }
}
```

---

#### `GET /metrics/employee-efficiency?restaurantId={restaurantId}`
Consultar eficiencia de empleado en un restaurante (solo PROPIETARIO).

**Response (200 OK):**
```json
{
  "data": {
    "ranking": [
      {
        "rank": 1,
        "employeeId": 7,
        "totalOrdersCompleted": 45,
        "totalOrdersDelivered": 42,
        "totalOrdersCancelled": 3,
        "averageDurationMinutes": 25.5,
        "minDurationMinutes": 15,
        "maxDurationMinutes": 60,
        "medianDurationMinutes": 24.0
      }
    ],
    "summary": {
      "totalEmployees": 15,
      "restaurantAverageDurationMinutes": 30.5,
      "bestEmployeeAverageDurationMinutes": 22.0,
      "worstEmployeeAverageDurationMinutes": 45.0,
      "totalOrdersProcessed": 450
    }
  },
  "meta": {
    "page": 0,
    "size": 10,
    "totalElements": 15,
    "totalPages": 2
  }
}
```

---

### Auditoría de Pedidos

#### `GET /orders/audit/history`
Consultar historial de auditoría de pedidos (solo CLIENTE).

**Response (200 OK):**
```json
{
  "data": [
    {
      "id": "507f1f77bcf86cd799439011",
      "orderId": 1,
      "restaurantId": 5,
      "clientId": 10,
      "previousStatus": "PENDIENT",
      "newStatus": "IN_PREPARE",
      "changedByUserId": 7,
      "changedByRole": "EMPLEADO",
      "changedAt": "2025-12-02T10:30:00",
      "actionType": "ASSIGNMENT",
      "employeeId": 7,
      "ipAddress": "192.168.1.100",
      "userAgent": "Mozilla/5.0",
      "notes": "Pedido asignado al empleado",
      "timeInPreviousStatusMinutes": 15
    }
  ],
  "meta": {
    "page": 0,
    "size": 10,
    "totalElements": 100,
    "totalPages": 10
  }
}
```


---

## Cómo Ejecutar Localmente

### 1. Prerequisitos

- ✅ JDK 17
- ✅ Gradle
- ✅ PostgreSQL 14+
- ✅ **Microservicio foodCourt-users ejecutándose en puerto 8081**
- ✅ **Microservicio trazability-audit ejecutándose en puerto 8083**
- ✅ **Microservicio message-sms ejecutándose en puerto 8084**

### 2. Instalación

1. **Clonar el repositorio**
   ```bash
   git clone <repository-url>
   cd foodcourt
   ```

2. **Crear base de datos en PostgreSQL**
   ```sql
   CREATE DATABASE foodcourt;
   ```

3. **Configurar conexión a base de datos**
   
   Editar `src/main/resources/application-dev.yml`:
   ```yaml
   spring:
     datasource:
       url: jdbc:postgresql://localhost:5432/foodcourt
       username: postgres
       password: tu_contraseña
   ```

4. **Configurar URLs de microservicios**
   
   Editar `src/main/resources/application-dev.yml`:
   ```yaml
   users:
     service:
       url: http://localhost:8081
   audit:
      service:
       url: http://localhost:8083
   ```

### 3. Compilar el Proyecto

```bash
# Generar DTOs desde OpenAPI spec
./gradlew openApiGenerate

# Compilar todo el proyecto
./gradlew clean build
```

### 4. Ejecutar la Aplicación

⚠️ **IMPORTANTE**: Antes de ejecutar este microservicio, asegúrate de que los siguientes estén corriendo:
1. `foodCourt-users` (puerto 8081)
2. `trazability-audit` (puerto 8083)
3. `message-sms` (puerto 8084)

**Opción 1: Desde terminal**
```bash
./gradlew bootRun
```

**Opción 2: Desde IntelliJ IDEA**
- Right-click `PowerUpApplication.java` → Run

---

## Cómo Correr las Pruebas

### Ejecutar todas las pruebas con cobertura

```bash
./gradlew test jacocoTestReport
```

### Ver reportes

```bash
# Reporte de tests
start build/reports/tests/test/index.html

# Reporte de cobertura
start build/reports/jacoco/test/html/index.html
```

### Ejecutar tests específicos

```bash
# RestaurantUseCaseTest (HU-2, HU-9)
./gradlew test --tests "RestaurantUseCaseTest"

# DishUseCaseTest (HU-3, HU-4, HU-7, HU-10)
./gradlew test --tests "DishUseCaseTest"

# OrderUseCaseTest (HU-11, HU-12, HU-13, HU-14, HU-15, HU-16)
./gradlew test --tests "OrderUseCaseTest"

# MetricsUseCaseTest (HU-18)
./gradlew test --tests "MetricsUseCaseTest"

# UserValidationHttpAdapterTest (WireMock)
./gradlew test --tests "UserValidationHttpAdapterTest"
```

### Cobertura de Historias de Usuario

Este microservicio cubre **13 Historias de Usuario** con más de **90 pruebas unitarias**:

| Historia | Clase de Test | Pruebas |
|----------|---------------|---------|
| HU-2: Crear Restaurante | `RestaurantUseCaseTest` | ✅ Validación NIT único<br>✅ Validación propietario existe<br>✅ Validación nombre |
| HU-3: Crear Plato | `DishUseCaseTest` | ✅ Validación propietario<br>✅ Precio > 0<br>✅ Plato activo por defecto |
| HU-4: Modificar Plato | `DishUseCaseTest` | ✅ Solo precio y descripción<br>✅ Validación ownership |
| HU-7: Habilitar/Deshabilitar | `DishUseCaseTest` | ✅ Solo propietario<br>✅ Toggle status |
| HU-9: Listar Restaurantes | `RestaurantUseCaseTest` | ✅ Orden alfabético<br>✅ Paginación |
| HU-10: Listar Platos | `DishUseCaseTest` | ✅ Filtro por categoría<br>✅ Paginación |
| HU-11: Realizar Pedido | `OrderUseCaseTest` | ✅ Estado inicial PENDIENTE<br>✅ Sin pedidos activos |
| HU-12: Listar Pedidos | `OrderUseCaseTest` | ✅ Filtro por estado<br>✅ Solo restaurante empleado |
| HU-13: Asignar Pedido | `OrderUseCaseTest` | ✅ Cambio a EN_PREPARACION<br>✅ Asignación empleado |
| HU-14: Pedido Listo | `OrderUseCaseTest` | ✅ Generación PIN<br>✅ Envío SMS |
| HU-15: Entregar Pedido | `OrderUseCaseTest` | ✅ Validación PIN<br>✅ Solo desde LISTO |
| HU-16: Cancelar Pedido | `OrderUseCaseTest` | ✅ Solo PENDIENTE<br>✅ Mensaje error |
| HU-18: Eficiencia | `MetricsUseCaseTest` | ✅ Tiempo promedio<br>✅ Ranking empleados |

---

## Notas Adicionales

### Seguridad y Autenticación

#### JWT (JSON Web Tokens)

El microservicio valida tokens JWT generados por el microservicio de Users:

**Extracción del usuario autenticado:**
- El `SecurityContextUtil` decodifica el token JWT del header `Authorization`
- Extrae `userId` y `role` del payload del token
- Esta información se usa para validar permisos

**Flujo de autorización:**
1. Cliente incluye token en header: `Authorization: Bearer <token>`
2. Sistema extrae `userId` y `role` del token
3. Controller valida rol básico con anotaciones de seguridad
4. UseCase valida permisos específicos (ej: propiedad del restaurante)

### Integración con otros Microservicios

#### foodCourt-users (Puerto 8081)
- **Validación de roles**: Verifica que usuarios tengan roles específicos
- **Consulta de usuarios**: Obtiene información de empleados y clientes
- **Tecnología**: RestTemplate

#### trazability-audit (Puerto 8083)
- **Registro de auditoría**: Cada cambio de estado de pedido se registra
- **Consulta de trazabilidad**: Los clientes pueden ver el historial de sus pedidos
- **Tecnología**: Feign Client

#### message-sms (Puerto 8084)
- **Notificaciones SMS**: Envía PIN de seguridad cuando pedido está listo
- **Tecnología**: RestTemplate → Twilio API

---

## Autor

**Brayan Barco**

## Licencia

Este proyecto es parte de la prueba técnica de Pragma.
