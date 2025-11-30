# Validaciones de Seguridad en Microservicio FoodCourt

## Resumen de Implementación

Se han implementado las siguientes validaciones de seguridad en el microservicio de FoodCourt:

### 1. Validación para crear Restaurante
- **Requisito**: Solo usuarios con rol **ADMINISTRADOR** pueden crear restaurantes
- **Ubicación**: `RestaurantUseCase.createRestaurant()`
- **Comportamiento**: Si un usuario que NO es ADMINISTRADOR intenta crear un restaurante, se lanza una excepción `UnauthorizedRestaurantCreationException` (HTTP 403 Forbidden)

### 2. Validación para crear Plato
- **Requisito**: Solo usuarios con rol **PROPIETARIO** pueden crear platos
- **Requisito adicional**: El propietario debe ser el dueño del restaurante al cual se le asignará el plato
- **Ubicación**: `DishUseCase.createDish()`
- **Comportamiento**: 
  - Si el usuario autenticado NO es el propietario del restaurante, se lanza `UnauthorizedDishOperationException` (HTTP 403 Forbidden)
  - La validación compara el `userId` del usuario autenticado con el `ownerId` del restaurante

### 3. Validación para modificar Plato
- **Requisito**: Solo usuarios con rol **PROPIETARIO** pueden modificar platos
- **Requisito adicional**: El propietario debe ser el dueño del restaurante al cual pertenece el plato
- **Ubicación**: `DishUseCase.updateDish()`
- **Comportamiento**: 
  - Se obtiene el restaurante asociado al plato
  - Si el usuario autenticado NO es el propietario del restaurante, se lanza `UnauthorizedDishOperationException` (HTTP 403 Forbidden)

## Archivos Creados

### 1. **ISecurityContextPort.java** (NUEVO)
- Puerto SPI para acceder al contexto de seguridad desde la capa de dominio
- Métodos:
  - `getCurrentUserId()`: Obtiene el ID del usuario autenticado
  - `getCurrentUserRole()`: Obtiene el rol del usuario autenticado
  - `hasRole(String roleName)`: Verifica si el usuario tiene un rol específico

### 2. **SecurityContextAdapter.java** (NUEVO)
- Adaptador que implementa `ISecurityContextPort`
- Utiliza `SecurityContextUtil` para extraer información del token JWT
- Componente de infraestructura que permite al dominio acceder al contexto de seguridad

### 3. **UnauthorizedRestaurantCreationException.java** (NUEVO)
- Excepción personalizada para cuando un usuario sin rol ADMINISTRADOR intenta crear un restaurante
- Se lanza en `RestaurantUseCase.validateAdministratorRole()`

### 4. **UnauthorizedDishOperationException.java** (NUEVO)
- Excepción personalizada para cuando un propietario intenta crear/modificar un plato de un restaurante que no le pertenece
- Se lanza en `DishUseCase.validateRestaurantOwnership()`

## Archivos Modificados

### 1. **RestaurantUseCase.java**
- Se inyectó `ISecurityContextPort` como dependencia
- Se agregó validación `validateAdministratorRole()` al inicio de `createRestaurant()`
- Método privado `validateAdministratorRole()`:
  - Obtiene el rol del usuario autenticado
  - Verifica que sea ADMINISTRADOR
  - Lanza `UnauthorizedRestaurantCreationException` si no lo es

### 2. **DishUseCase.java**
- Se inyectó `ISecurityContextPort` como dependencia
- Se agregó validación `validateRestaurantOwnership()` en `createDish()`
- Se agregó validación `validateRestaurantOwnership()` en `updateDish()`
- Método privado `validateRestaurantOwnership(RestaurantModel restaurant)`:
  - Obtiene el ID del usuario autenticado
  - Compara con el `ownerId` del restaurante
  - Lanza `UnauthorizedDishOperationException` si no coinciden

### 3. **BeanConfiguration.java**
- Se actualizó el bean `restaurantServicePort` para inyectar `ISecurityContextPort`
- Se actualizó el bean `dishServicePort` para inyectar `ISecurityContextPort`

### 4. **ControllerAdvisor.java**
- Se agregó handler para `UnauthorizedRestaurantCreationException` → HTTP 403
- Se agregó handler para `UnauthorizedDishOperationException` → HTTP 403

## Flujo de Validación

### Crear Restaurante
1. Usuario autenticado (con token JWT) envía POST a `/restaurants`
2. `RestaurantController` valida que el usuario tenga rol ADMINISTRADOR (nivel básico con `@RequireRole`)
3. `RestaurantUseCase`:
   - **Valida** que el usuario autenticado sea ADMINISTRADOR
   - Si no lo es → `UnauthorizedRestaurantCreationException` (HTTP 403)
   - Valida el nombre del restaurante
   - Verifica que no exista otro restaurante con el mismo NIT
   - Valida que el `ownerId` proporcionado exista y tenga rol PROPIETARIO
   - Guarda el restaurante

### Crear Plato
1. Usuario autenticado (PROPIETARIO) envía POST a `/dishes`
2. `DishController` valida que el usuario tenga rol PROPIETARIO (con `@RequireRole`)
3. `DishUseCase`:
   - Verifica que el `restaurantId` exista
   - **Valida** que el usuario autenticado sea el propietario del restaurante
   - Si no es el propietario → `UnauthorizedDishOperationException` (HTTP 403)
   - Guarda el plato

### Modificar Plato
1. Usuario autenticado (PROPIETARIO) envía PUT a `/dishes/{id}`
2. `DishController` valida que el usuario tenga rol PROPIETARIO (con `@RequireRole`)
3. `DishUseCase`:
   - Verifica que el plato exista
   - Obtiene el restaurante asociado al plato
   - **Valida** que el usuario autenticado sea el propietario del restaurante
   - Si no es el propietario → `UnauthorizedDishOperationException` (HTTP 403)
   - Actualiza el plato

## Ejemplos de Uso

### Crear un Restaurante (requiere ser ADMINISTRADOR)
```json
POST /restaurants
Authorization: Bearer {token_de_administrador}
Content-Type: application/json

{
  "name": "Restaurante El Buen Sabor",
  "nit": "900123456",
  "address": "Calle 123 #45-67",
  "phoneNumber": "+573001234567",
  "urlLogo": "https://example.com/logo.png",
  "ownerId": 5
}
```
**Resultado**: ✅ Restaurante creado exitosamente

---

### Error: PROPIETARIO intenta crear un Restaurante
```json
POST /restaurants
Authorization: Bearer {token_de_propietario}
Content-Type: application/json

{
  "name": "Mi Restaurante",
  "nit": "900999888",
  "address": "Avenida 10 #20-30",
  "phoneNumber": "+573009876543",
  "urlLogo": "https://example.com/mi-logo.png",
  "ownerId": 8
}
```
**Resultado**: ❌ HTTP 403 Forbidden
```json
{
  "timestamp": "2025-11-30T...",
  "status": 403,
  "error": "Unauthorized Restaurant Creation",
  "message": "Solo los usuarios con rol ADMINISTRADOR pueden crear restaurantes"
}
```

---

### Crear un Plato (requiere ser PROPIETARIO del restaurante)
```json
POST /dishes
Authorization: Bearer {token_de_propietario_restaurante_1}
Content-Type: application/json

{
  "name": "Bandeja Paisa",
  "price": 25000,
  "description": "Plato típico colombiano",
  "urlImage": "https://example.com/bandeja.jpg",
  "category": "Plato fuerte",
  "active": true,
  "restaurantId": 1
}
```
**Resultado**: ✅ Plato creado exitosamente (si el usuario autenticado es propietario del restaurante ID 1)

---

### Error: PROPIETARIO intenta crear plato en restaurante de otro propietario
```json
POST /dishes
Authorization: Bearer {token_de_propietario_A}
Content-Type: application/json

{
  "name": "Pizza Margarita",
  "price": 30000,
  "description": "Pizza italiana tradicional",
  "urlImage": "https://example.com/pizza.jpg",
  "category": "Pizza",
  "active": true,
  "restaurantId": 2
}
```
**Resultado**: ❌ HTTP 403 Forbidden (si el restaurante 2 pertenece a otro propietario)
```json
{
  "timestamp": "2025-11-30T...",
  "status": 403,
  "error": "Unauthorized Dish Operation",
  "message": "Solo el propietario del restaurante puede crear o modificar platos. Restaurante pertenece al propietario con ID: 7"
}
```

---

### Modificar un Plato (requiere ser PROPIETARIO del restaurante)
```json
PUT /dishes/10
Authorization: Bearer {token_de_propietario_restaurante}
Content-Type: application/json

{
  "price": 28000,
  "description": "Bandeja Paisa con chorizo adicional"
}
```
**Resultado**: ✅ Plato actualizado exitosamente (si el usuario autenticado es propietario del restaurante al que pertenece el plato)

---

### Error: PROPIETARIO intenta modificar plato de otro restaurante
```json
PUT /dishes/15
Authorization: Bearer {token_de_propietario_B}
Content-Type: application/json

{
  "price": 35000
}
```
**Resultado**: ❌ HTTP 403 Forbidden (si el plato 15 pertenece a un restaurante de otro propietario)
```json
{
  "timestamp": "2025-11-30T...",
  "status": 403,
  "error": "Unauthorized Dish Operation",
  "message": "Solo el propietario del restaurante puede crear o modificar platos. Restaurante pertenece al propietario con ID: 12"
}
```

## Seguridad Implementada

### Extracción del Usuario Autenticado
El `SecurityContextUtil` existente decodifica el token JWT del header `Authorization` y extrae:
- **userId**: ID del usuario autenticado
- **role**: Rol del usuario autenticado

Esta información se utiliza para:
1. Validar que el usuario sea ADMINISTRADOR (crear restaurante)
2. Validar que el usuario sea el propietario del restaurante (crear/modificar plato)

### Validaciones en Cascada
1. **Nivel Controller**: `@RequireRole` valida el rol básico requerido
2. **Nivel Dominio**: Validaciones adicionales de pertenencia y autorización específica

Esta arquitectura garantiza:
- ✅ Solo ADMINISTRADORES pueden crear restaurantes
- ✅ Solo PROPIETARIOS pueden crear/modificar platos
- ✅ Los propietarios solo pueden crear/modificar platos de SUS restaurantes
- ✅ No se puede crear/modificar platos de restaurantes ajenos

## Notas Importantes

- Las validaciones están en la **capa de dominio** (casos de uso), respetando la arquitectura limpia
- Se utilizan **puertos y adaptadores** para acceder al contexto de seguridad sin acoplar el dominio a Spring Security
- Los mensajes de error son **descriptivos** e indican claramente el problema y el ID del propietario correcto
- La seguridad es **robusta** ya que combina validaciones a nivel de controller y dominio

