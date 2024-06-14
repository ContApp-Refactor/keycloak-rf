## Keycloak Docker

#### Requisitos previos
Antes de empezar, asegúrate de tener instalado Docker en tu máquina.


### Configuración del archivo .env
Para configurar el entorno, necesitarás crear un archivo .env en la raíz del proyecto. Puedes usar el archivo de ejemplo .example.env como referencia. El contenido del archivo .env debe incluir las siguientes variables:

```
POSTGRES_DB=
POSTGRES_USER=
POSTGRES_PASSWORD=

KEYCLOAK_ADMIN=
KEYCLOAK_ADMIN_PASSWORD=
```
`Servicios en Docker Compose`

El archivo docker-compose.yml utiliza las variables definidas en el archivo .env y configura dos servicios:

PostgreSQL: Utiliza las variables de entorno para configurar la base de datos.
Keycloak: Configura un servidor Keycloak con un volumen en PostgreSQL.

### Instrucciones para ejecutar el proyecto

Sigue estos pasos para construir y levantar los servicios:

#### Construir los servicios:

```
docker-compose build
```

#### Levantar los servicios:

```
docker-compose up
```

Visualización en Docker Dashboard
Una vez que los servicios estén en funcionamiento, puedes verlos en el Docker Dashboard. A continuación, se muestra una imagen de ejemplo del dashboard:

![](https://res.cloudinary.com/dilrruxyx/image/upload/v1718328020/Captura_desde_2024-06-13_20-20-03_ccacz7.png)

