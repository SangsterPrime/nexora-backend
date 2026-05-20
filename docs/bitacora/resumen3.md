# Resumen 3 - Soporte Docker/Render y configuracion PostgreSQL local

Este documento detalla la tercera iteracion realizada sobre Nexora Backend, enfocada en preparar soporte de despliegue Docker para Render sin convertir Docker en requisito para desarrollo local.

## Objetivo de esta iteracion

El objetivo fue dejar el backend Spring Boot listo para dos escenarios:

- Ejecucion local directa usando PostgreSQL instalado en Windows.
- Despliegue opcional mediante Docker/Render usando variables de entorno.

Restricciones y criterios respetados:

- No agregar Spring Security.
- No agregar login.
- No subir secretos reales.
- No hacer Docker obligatorio para desarrollo local.
- Mantener `compose.yaml` solo como alternativa de desarrollo local.
- Usar PostgreSQL local 17.6 en Windows como base esperada para desarrollo actual.
- Mantener Java 21.
- Mantener Maven.
- Mantener package base `cl.duoc.nexora.backend`.

## Configuracion local esperada

Se dejo el proyecto preparado para conectarse por defecto a PostgreSQL local con estos datos:

- Host: `localhost`
- Puerto: `5432`
- Database: `nexora_db`
- Username: `nexora_user`
- Password: `nexora_password`
- Version local indicada: PostgreSQL `17.6` en Windows

No se requiere Docker local para correr la aplicacion en este escenario.

## Cambios en application.properties

Archivo modificado:

- `src/main/resources/application.properties`

### Puerto compatible con Render

Antes se usaba:

```properties
server.port=${SERVER_PORT:8080}
```

Se cambio a:

```properties
server.port=${PORT:8080}
```

Motivo:

- Render entrega el puerto a traves de la variable de entorno `PORT`.
- Si `PORT` no existe, se usa `8080` como fallback local.

### Bind address para contenedores

Se agrego:

```properties
server.address=0.0.0.0
```

Motivo:

- En entornos Docker/Render, la aplicacion debe escuchar en todas las interfaces del contenedor.
- Si solo escucha en `localhost`, Render o el proxy externo no podrian enrutar correctamente hacia la app.

### Datasource con variables y fallback local

Se dejo configurado exactamente con variables de entorno y fallback local:

```properties
spring.datasource.url=${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5432/nexora_db}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME:nexora_user}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD:nexora_password}
spring.datasource.driver-class-name=org.postgresql.Driver
```

Motivo:

- En local, si no se define ninguna variable, la app usa PostgreSQL local en Windows.
- En Render, se pueden definir las variables `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME` y `SPRING_DATASOURCE_PASSWORD` desde el dashboard del servicio.
- No se versionan secretos reales.

### JPA solicitado

Se dejo JPA con los valores pedidos explicitamente:

```properties
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.open-in-view=false
```

Ademas se mantiene:

```properties
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
```

Motivo:

- `ddl-auto=update` permite que Hibernate actualice el esquema durante desarrollo.
- `show-sql=true` imprime SQL en consola para diagnostico durante MVP.
- `format_sql=true` mejora la lectura del SQL generado.
- `open-in-view=false` evita mantener la sesion JPA abierta durante la serializacion HTTP.
- `PostgreSQLDialect` deja claro el dialecto esperado para runtime.

### Estado final relevante de application.properties

El archivo quedo con esta configuracion base:

```properties
spring.application.name=nexora-backend

server.port=${PORT:8080}
server.address=0.0.0.0

spring.datasource.url=${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5432/nexora_db}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME:nexora_user}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD:nexora_password}
spring.datasource.driver-class-name=org.postgresql.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.open-in-view=false
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect

management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.health.show-details=always
management.info.env.enabled=true
```

## Creacion de backend/Dockerfile

Archivo creado:

- `backend/Dockerfile`

Contenido:

```dockerfile
FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn -B -DskipTests package

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Diseno del Dockerfile

Se uso un build multi-stage:

#### Stage 1: build

Base:

```dockerfile
FROM maven:3.9.9-eclipse-temurin-21 AS build
```

Responsabilidades:

- Usar Maven con Java 21.
- Copiar `pom.xml` y `src`.
- Construir el JAR con `mvn -B -DskipTests package`.

#### Stage 2: runtime

Base:

```dockerfile
FROM eclipse-temurin:21-jre
```

Responsabilidades:

- Ejecutar solamente el JAR generado.
- Usar una imagen runtime mas liviana que la imagen Maven.
- Exponer puerto `8080` como documentacion del puerto interno.
- Ejecutar la app con `java -jar app.jar`.

### Por que se ubico en backend/

La tarea pedia crear:

- `backend/Dockerfile`

El proyecto Maven real esta en la raiz del repo, por eso el Dockerfile esta bajo `backend/`, pero debe construirse usando la raiz como contexto.

Comando esperado si se usara Docker:

```bash
docker build -f backend/Dockerfile .
```

Importante:

- No es necesario ejecutar ese comando localmente ahora.
- El archivo queda como soporte para Render o despliegues futuros.

## Creacion de backend/.dockerignore

Archivo creado:

- `backend/.dockerignore`

Contenido:

```dockerignore
target/
.mvn/
.git/
.gitignore
.gitattributes
.env
.env.*
!.env.example
.qodo
.idea/
.vscode/
*.iml
*.log
README.md
resumen.md
resumen2.md
```

Proposito:

- Evitar enviar archivos innecesarios al contexto de build Docker.
- Evitar incluir `.env` o archivos con posibles secretos.
- Evitar incluir metadata de Git o IDE.
- Evitar incluir artefactos `target/` generados localmente.
- Mantener la imagen enfocada solo en lo necesario para construir y ejecutar la app.

Nota importante:

- El `.dockerignore` esta en `backend/` porque fue solicitado asi.
- Si Docker se ejecuta con contexto raiz (`docker build -f backend/Dockerfile .`), Docker usa por defecto `.dockerignore` de la raiz si existe. Como no se creo `.dockerignore` en raiz, este archivo queda como soporte/documentacion dentro de `backend/` segun la tarea solicitada.
- Si se quisiera que Docker aplique estas reglas automaticamente con contexto raiz, en una mejora posterior convendria agregar un `.dockerignore` tambien en la raiz.

## Actualizacion de .env.example

Archivo modificado:

- `.env.example`

Antes se usaba:

```env
SERVER_PORT=8080
```

Se cambio a:

```env
PORT=8080
```

Motivo:

- Alinear la variable de puerto con Render y con `application.properties`.

Se mantuvieron variables de datasource:

```env
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/nexora_db
SPRING_DATASOURCE_USERNAME=nexora_user
SPRING_DATASOURCE_PASSWORD=nexora_password
```

Se eliminaron del ejemplo:

```env
SPRING_JPA_HIBERNATE_DDL_AUTO=update
SPRING_JPA_SHOW_SQL=true
```

Motivo:

- La instruccion pidio dejar esos valores directamente definidos en `application.properties`.
- Se simplifica el `.env.example` y se evita hacer configurable algo que por ahora quedo definido como convencion del MVP.

Estado final de `.env.example`:

```env
PORT=8080

SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/nexora_db
SPRING_DATASOURCE_USERNAME=nexora_user
SPRING_DATASOURCE_PASSWORD=nexora_password
```

## Actualizacion de compose.yaml

Archivo modificado:

- `compose.yaml`

Cambio realizado:

```yaml
image: postgres:17.6
```

Antes estaba como:

```yaml
image: postgres:16
```

Motivo:

- El usuario indico que usa PostgreSQL local version `17.6` en Windows.
- Aunque no usara Docker local por ahora, se dejo `compose.yaml` alineado como alternativa de desarrollo local.

Estado importante:

- `compose.yaml` no es requisito para correr el proyecto localmente.
- Es solo soporte alternativo para desarrollo o pruebas futuras.
- La app local se conecta por fallback a `localhost:5432`, que corresponde al PostgreSQL instalado en Windows si esta activo.

## Render: variables esperadas

Para desplegar en Render, se espera configurar variables de entorno en el servicio.

Variables principales:

```env
PORT=<lo entrega Render automaticamente>
SPRING_DATASOURCE_URL=<jdbc url de PostgreSQL en Render>
SPRING_DATASOURCE_USERNAME=<usuario de PostgreSQL>
SPRING_DATASOURCE_PASSWORD=<password de PostgreSQL>
```

No se agregaron secretos reales al repositorio.

## Uso local sin Docker

El flujo local recomendado tras esta iteracion es:

1. Tener PostgreSQL 17.6 corriendo en Windows.
2. Crear la base `nexora_db`.
3. Crear el usuario `nexora_user` con password `nexora_password`.
4. Asegurar que PostgreSQL escuche en `localhost:5432`.
5. Ejecutar la app con Maven.

Comando para ejecutar:

```bash
mvn spring-boot:run
```

La app usara automaticamente:

```text
jdbc:postgresql://localhost:5432/nexora_db
```

si no hay variables de entorno que sobrescriban esa configuracion.

## Uso Docker opcional

Aunque no se usara Docker local por ahora, quedo preparado.

Comando de build con contexto raiz:

```bash
docker build -f backend/Dockerfile .
```

Ejemplo de ejecucion opcional:

```bash
docker run --rm -p 8080:8080 \
  -e PORT=8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/nexora_db \
  -e SPRING_DATASOURCE_USERNAME=nexora_user \
  -e SPRING_DATASOURCE_PASSWORD=nexora_password \
  nexora-backend
```

Nota:

- En Docker Desktop sobre Windows, `host.docker.internal` permite que el contenedor acceda a servicios del host.
- Este flujo es opcional y no fue ejecutado.

## Verificaciones ejecutadas

### Compilacion

Comando ejecutado:

```bash
mvn compile
```

Resultado:

```text
BUILD SUCCESS
```

Detalle:

- El proyecto compilo correctamente despues de los cambios en `application.properties`, Dockerfile, `.dockerignore`, `.env.example` y `compose.yaml`.
- No hubo errores de Java ni configuracion que impidieran compilar.

### Tests

Comando ejecutado:

```bash
mvn test
```

Resultado:

```text
BUILD SUCCESS
```

Detalle:

- Se ejecuto el test de contexto existente.
- Uso H2 mediante `src/test/resources/application.properties`.
- No dependio del PostgreSQL local ni de Docker.
- Resultado reportado: `Tests run: 1, Failures: 0, Errors: 0, Skipped: 0`.

## Archivos creados en esta iteracion

- `backend/Dockerfile`
- `backend/.dockerignore`
- `resumen3.md`

## Archivos modificados en esta iteracion

- `src/main/resources/application.properties`
- `.env.example`
- `compose.yaml`

## Estado final

El proyecto queda en este estado:

- Compila correctamente.
- Tests pasan correctamente.
- Puede correr localmente contra PostgreSQL 17.6 en Windows sin Docker.
- Tiene soporte Docker opcional para Render o despliegue futuro.
- Usa `PORT` para compatibilidad con Render.
- Escucha en `0.0.0.0` para ambientes contenerizados.
- Usa datasource por variables de entorno con fallback local.
- No contiene secretos reales.
- No se agrego Spring Security.
- `compose.yaml` queda como alternativa de desarrollo local, no como requisito.

## Pendiente recomendado

Si se decide usar Docker local o Render con build Docker desde raiz, convendria evaluar agregar un `.dockerignore` tambien en la raiz del repositorio, porque Docker aplica el `.dockerignore` del contexto de build, no necesariamente el ubicado junto al Dockerfile cuando el contexto es `.`.
