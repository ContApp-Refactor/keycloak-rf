## Obtención del Token

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
