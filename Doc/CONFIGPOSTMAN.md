## Configuración de Postman

En este video se explica cómo configurar colecciones de Postman para facilitar las peticiones utilizando el token.

[Video](https://drive.google.com/file/d/1DMnx5_tMszpVmXpSXt_ge6WLY3Mp3Ll0/view)

## Información que se utiliza en el video.

***POST***

```
http://contables.unicauca.edu.co/keycloak/token/
```

***Cuerpo de la Solicitud***

```json
{
    "username": "contables_admin",
    "password": "12345"
}
```

***Respuesta***

```json
{
    "access_token": "eyJhbGciOiJSUzI1...."
    "refresh_expires_in": 36000,
    "expires_in": 1200
}
```

***Script***

```javascript
var data = JSON.parse(responseBody);
postman.setGlobalVariable("tokenKeycloak",data.access_token)
```

***Peticion de prueba***

***GET***

```
http://contables.unicauca.edu.co/api/enterprises/
```

