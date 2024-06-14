## Keycloak

![Escenario](https://res.cloudinary.com/dilrruxyx/image/upload/v1718310860/keycloak_pdryin.jpg)


Para instalar Keycloak en tu máquina local, sigue estos pasos: [DOCUMENTATION](./Doc/DOCUMENTATION.md)

## Obtener Token

Para obtener el token de autenticación, debes hacer una petición POST a la siguiente ruta:
```
   http://.../keycloak/token/
```

La petición debe incluir en el cuerpo un JSON con el siguiente formato:
```json
{
    "username": "usuario",
    "password": "password"
}
```

> [!NOTE]
> Asegúrate de completar la url y reemplazar los valores de username y password con las credenciales correctas proporcionadas por el administrador del sistema.

Si la autenticación es exitosa, recibirás un token en la respuesta que podrás utilizar para acceder a los recursos protegidos del sistema.

```json
{
    "access_token": "eyJhbGciOiJSUzI1N...",
    "expires_in": 1200,
    "refresh_expires_in": 1800,
    "refresh_token": "eyJhbGciOiJIUzUx...",
    "token_type": "Bearer",
    "not-before-policy": 0,
    "session_state": "64b3ac98...",
    "scope": "profile email"
}
```

> [!TIP]
> Puedes usar la herramienta curl para realizar la petición desde la línea de comandos. A continuación, se muestra un ejemplo de cómo hacer la petición:

```
curl -X POST http://.../keycloak/token/ \
-H "Content-Type: application/json" \
-d '{
    "username": "usuario",
    "password": "password"
}'
```

# API del Controlador de Keycloak
Permite la gestión de usuarios de Keycloak, incluyendo la creación, actualización, recuperación y eliminación de usuarios.

## Endpoints

### Crear un Usuario
**POST** `/keycloak/create`

***Cuerpo de la Solicitud***
```json
{
  "id": "string",
  "username": "usuarioEjemplo",
  "email": "usuario@ejemplo.com",
  "firstName": "Juan",
  "lastName": "Pérez",
  "password": "ContraseñaSegura123",
  "roles": [
    "user_realm",
    "admin_realm",
    "super_realm"
  ]
}
```

### Actualizar un Usuario

**PUT** `/keycloak/update/{userId}`

***Parámetro de Ruta***

- userId (string): El ID del usuario a actualizar.
```json
{
  "id": "string",
  "username": "usuarioActualizado",
  "email": "usuarioActualizado@ejemplo.com",
  "firstName": "Ana",
  "lastName": "López",
  "password": "NuevaContraseñaSegura123!",
  "roles": [
    "user_realm",
    "admin_realm"
  ]
}
```

### Obtener y Eliminar Usuarios

| Método   | Endpoint                           | Descripción                                              |
|----------|------------------------------------|----------------------------------------------------------|
| **GET**  | `/keycloak/users`                  | Recupera una lista de todos los usuarios en Keycloak.    |
| **GET**  | `/keycloak/users/{username}`       | Recupera una de usuarios que coincidan con el username   | 
| **GET**  | `/keycloak/user/{userId}`          | Recupera los detalles de un usuario por su ID de usuario.|
| **GET**  | `/keycloak/getCurrentUser`         | Recupera los detalles del usuario actualmente autenticado. |
| **DELETE**| `/keycloak/delete/{userId}`       | Elimina un usuario de Keycloak.                          |

### Swagger

Para acceder a Swagger, use el siguiente enlace: [http://localhost:8081/swagger-ui/index.html#/](http://localhost:8081/swagger-ui/index.html#/). 

Las peticiones requieren un token, por lo que debe agregarlo en la sección correspondiente.
