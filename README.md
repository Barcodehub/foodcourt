<br />
<div align="center">
<h3 align="center">PRAGMA POWER-UP - FOOD COURT MICROSERVICE</h3>
  <p align="center">
    Microservicio de gestión de restaurantes y platos. Permite a administradores crear restaurantes y a propietarios gestionar los platos de sus restaurantes.
  </p>
</div>

### Built With

* ![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=java&logoColor=white)
* ![Spring](https://img.shields.io/badge/Spring-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
* ![Spring Security](https://img.shields.io/badge/Spring_Security-6DB33F?style=for-the-badge&logo=Spring-Security&logoColor=white)
* ![JWT](https://img.shields.io/badge/JWT-black?style=for-the-badge&logo=JSON%20web%20tokens)
* ![Gradle](https://img.shields.io/badge/Gradle-02303A.svg?style=for-the-badge&logo=Gradle&logoColor=white)
* ![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white)

## Descripción

Este microservicio es responsable de:
- **Gestión de restaurantes**: Creación de restaurantes (solo ADMINISTRADORES)
- **Gestión de platos**: Crear y modificar platos (solo el PROPIETARIO del restaurante)
- **Validación de permisos**: Garantiza que solo los propietarios puedan gestionar sus restaurantes
- **Integración**: Se comunica con el microservicio de usuarios para validar roles

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
    ├── out/feign/      # Cliente Feign (Users API)
    └── security/       # Seguridad JWT
```


<!-- GETTING STARTED -->
## Getting Started

To get a local copy up and running follow these steps.

### Prerequisites

* JDK 17 [https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
* Gradle [https://gradle.org/install/](https://gradle.org/install/)
* PostgreSQL 14+ [https://www.postgresql.org/download/](https://www.postgresql.org/download/)
* Microservicio de Users ejecutándose en puerto 8081

### Recommended Tools
* IntelliJ IDEA [https://www.jetbrains.com/idea/download/](https://www.jetbrains.com/idea/download/)
* Postman [https://www.postman.com/downloads/](https://www.postman.com/downloads/)
* DBeaver [https://dbeaver.io/download/](https://dbeaver.io/download/)

### Installation

1. Clone the repo
   ```sh
   git clone <repository-url>
   cd foodCourt
   ```

2. Create a new database in PostgreSQL
   ```sql
   CREATE DATABASE powerup_foodcourt;
   ```

3. Update the database connection settings
   ```yml
   # src/main/resources/application-dev.yml
   spring:
     datasource:
       url: jdbc:postgresql://localhost:5432/powerup_foodcourt
       username: postgres
       password: your_password
   ```

4. Configure .env file (copiar de .env.example)
   ```sh
   # URL del microservicio de usuarios
   USERS_API_URL=http://localhost:8081
   ```

<!-- USAGE -->
## Usage

### Compilar y generar código

Genera los DTOs e interfaces desde OpenAPI:
```sh
./gradlew openApiGenerate
```

Compila todo el proyecto:
```sh
./gradlew clean build
```

### Ejecutar la aplicación

⚠️ **Importante**: Primero debe estar ejecutándose el microservicio de Users en el puerto 8081

Desde terminal:
```sh
./gradlew bootRun
```

O desde IntelliJ: Right-click PowerUpApplication → Run

La aplicación estará disponible en:
- API: [http://localhost:8082](http://localhost:8082)
- Swagger UI: [http://localhost:8082/swagger-ui.html](http://localhost:8082/swagger-ui.html)
- OpenAPI spec: [http://localhost:8082/api-docs](http://localhost:8082/api-docs)

### Endpoints Principales

#### Restaurantes
- `POST /restaurants` - Crear restaurante (requiere rol ADMINISTRADOR)
- `GET /restaurants` - Listar restaurantes
- `GET /restaurants/{id}` - Obtener restaurante por ID

#### Platos
- `POST /dishes` - Crear plato (requiere ser PROPIETARIO del restaurante)
- `PUT /dishes/{id}` - Actualizar plato (requiere ser PROPIETARIO del restaurante)
- `GET /dishes` - Listar platos

### Reglas de Negocio

#### Creación de Restaurantes
- Solo usuarios con rol **ADMINISTRADOR** pueden crear restaurantes
- El `ownerId` proporcionado debe existir y tener rol PROPIETARIO
- El NIT del restaurante debe ser único
- El nombre no puede contener solo números

#### Gestión de Platos
- Solo usuarios con rol **PROPIETARIO** pueden crear/modificar platos
- El propietario debe ser dueño del restaurante al que pertenece el plato
- Si un propietario intenta crear/modificar un plato de otro restaurante → HTTP 403 Forbidden

#### Validaciones
- Nombre del restaurante requerido y no puede ser solo números
- NIT único
- Precio del plato debe ser mayor a 0
- El restaurante debe existir antes de crear platos

### Desarrollo API-First

1. **Edita** el schema en `src/main/resources/static/open-api.yaml`
2. **Genera** los DTOs: `./gradlew openApiGenerate`
3. **Implementa** las interfaces generadas en los controladores
4. **Mapea** entre DTOs y modelos de dominio con MapStruct

Flujo de una petición:
```
Request → Controller (valida @RequireRole) → Handler → UseCase 
  ↓
Valida permisos específicos → Persiste → Response
```

<!-- ROADMAP -->
## Tests

Run tests with coverage:
```sh
./gradlew test jacocoTestReport
```

O desde IntelliJ: Right-click test folder → Run tests with coverage

### Seguridad

#### JWT (JSON Web Tokens)
El microservicio valida tokens JWT generados por el microservicio de Users:

**Extracción del usuario autenticado:**
- El `SecurityContextUtil` decodifica el token JWT del header `Authorization`
- Extrae `userId` y `role` del payload del token
- Esta información se usa para validar permisos

**Flujo de autorización:**
1. Cliente incluye token en header: `Authorization: Bearer <token>`
2. Sistema extrae `userId` y `role` del token
3. Controller valida rol básico con `@RequireRole`
4. UseCase valida permisos específicos (ej: propiedad del restaurante)

#### Roles y Permisos

| Rol | Puede hacer | Validación |
|-----|------------|-----------|
| ADMINISTRADOR | Crear restaurantes | `@RequireRole` en controller |
| PROPIETARIO | Crear/modificar platos | `@RequireRole` + validación de propiedad en UseCase |
| EMPLEADO | - | - |
| CLIENTE | - | - |

### Validaciones de Propiedad

**Crear/Modificar Plato:**
1. Se verifica que el usuario tenga rol PROPIETARIO (controller)
2. Se obtiene el restaurante asociado al plato
3. Se compara `restaurant.ownerId` con `currentUserId`
4. Si no coinciden → `UnauthorizedDishOperationException` (HTTP 403)

**Ejemplo:**
- Usuario ID 5 (PROPIETARIO) intenta crear plato en restaurante con ownerId=7
- Resultado: ❌ HTTP 403 "Solo el propietario del restaurante puede crear o modificar platos"

### Arquitectura Hexagonal

**Capas:**
- **Domain**: Lógica de negocio pura
- **Application**: Orquestación y mapeo
- **Infrastructure**: Adaptadores externos

**Puertos:**
- **API (Entrada)**: `IRestaurantServicePort`, `IDishServicePort`
- **SPI (Salida)**: `IRestaurantPersistencePort`, `IDishPersistencePort`, `ISecurityContextPort`, `IUserValidationPort`

**Comunicación entre microservicios:**
- Feign Client para consumir API de Users
- Validación de existencia de usuarios y roles
- Manejo de errores de comunicación

### Documentación Adicional

- [VALIDACIONES_SEGURIDAD_IMPLEMENTADAS.md](VALIDACIONES_SEGURIDAD_IMPLEMENTADAS.md) - Detalles de validaciones implementadas

### Troubleshooting

**Error: "Cannot connect to Users API"**
- Verifica que el microservicio de Users esté ejecutándose en puerto 8081
- Revisa la variable `USERS_API_URL` en el archivo `.env`

**Error: "Unauthorized Dish Operation"**
- Verifica que el token JWT sea del propietario correcto del restaurante
- Confirma que el `ownerId` del restaurante coincida con tu `userId`

**Error: "Restaurant Not Found"**
- Verifica que el `restaurantId` exista en la base de datos
- Confirma que estés apuntando a la base de datos correcta


