# 🔐 Proyecto JWT con Spring Boot

Este repositorio implementa un sistema de **autenticación y autorización** en APIs REST usando **JSON Web Tokens (JWT)** con **Spring Boot 3** y **Spring Security**, incorporando:
  - **Access Tokens** de vida corta para autenticar peticiones.
  - **Refresh Tokens** de vida larga para renovar accesos sin re-login.
  - Persistencia del refresh token en la base de datos.
  - Endpoints de `login`, `register`, `refresh` y `logout`.
El objetivo es ser una guía clara y práctica de cómo manejar JWTs de manera **segura y escalable** en aplicaciones Java modernas.

---

## 🚀 Características principales

- Registro y login de usuarios con contraseña encriptada.
- Emisión de **access + refresh tokens**.
- Endpoint `/api/auth/refresh` para renovar access tokens usando refresh.
- Endpoint `/api/auth/logout` para invalidar refresh tokens en servidor.
- Limpieza automática de tokens revocados mediante un job programado con @Scheduled.
- Integración completa con **Spring Security**.
- **MySQL** como base de datos relacional.
- **Docker Compose** para levantar app + MySQL.
- Código ampliamente comentado para aprendizaje.

---

## 🛠️ Tecnologías utilizadas

- **Java 21**
- **Spring Boot 3**
- **Spring Security**
- **Spring Data JPA**
- **JJWT** (`io.jsonwebtoken`)
- **MySQL**
- **Maven**
- **Docker & Docker Compose**

---

## 📦 Instalación y ejecución

### **1. Clonar el repositorio**

```bash
git clone https://github.com/LucaLzt/jwt-ejemplo.git
cd jwt-ejemplo
```

### **2. Levantar con Docker**

Con el `docker-compose.yml` incluido:

```bash
docker compose up --build
```
Esto levanta:
- `mysql` en el puerto `3307`
- `spring-jwt-app` en el puerto `8080` 

### **3. Variables de entorno**

En `docker-compose.yml` ya están seteadas:
```yaml
JWT_SECRET: clave_secreta_super_segura_1234567890123456
ACCESS_EXPIRATION: 900000        # 15 minutos
REFRESH_EXPIRATION: 1209600000   # 14 días
```
Y el `application.properties` las usa así:
```properties
jwt.secret=${JWT_SECRET}
jwt.accessExpiration=${ACCESS_EXPIRATION}
jwt.refreshExpiration=${REFRESH_EXPIRATION}
```

### **4. Compilar localmente (opcional)**

```bash
mvn clean package -DskipTests
java -jar target/app.jar
```
---

## 🔑 Flujo de autenticación

1. **Login / Register** → Devuelve `accessToken` + `refreshToken` en un `AuthDTO`.
2. **Acceso a endpoints protegidos** → Se envía el `accessToken` en el header:
```makefile
Authorization: Bearer <accessToken>
```
3. **Token expirado** → El cliente usa el `refreshToken` en `/api/auth/refresh` para obtener un nuevo par de tokens.
4. **Logout** → El servidor invalida el token guardado en la base de datos y agrega el access token a la blacklist, cerrando la sesión inmediatamente aunque el token no haya expirado.

---

## 🧹 Limpieza automática de tokens

El sistema incluye un job programado (`TokenCleanupJob`) que se ejecuta diariamente y elimina
de la base de datos todos los tokens revocados cuya fecha de expiración (`expires_at`) ya pasó.
Esto evita que la tabla `revoked_tokens` crezca indefinidamente y mantiene la base optimizada.

---

## 📚 Estructura del proyecto

```
src/
  main/java/com/ejemplos/jwt/
    controllers/      # Endpoints REST (AuthController, etc.)
    models/           # Entidades JPA (User)
    repositories/     # UserRepository
    services/         # Lógica de negocio (AuthServiceImpl)
    utils/            # JwtUtil, filtros de seguridad
    jobs/             # Limpieza de tokens revocados (TokenCleanupJob)
  resources/
    application.properties
docker-compose.yml
Dockerfile
```

---

## 💡 Recursos recomendados

- [Documentación oficial Spring Security](https://docs.spring.io/spring-security/reference/)
- [Documentación JJWT](https://github.com/jwtk/jjwt)
- [Artículo sobre JWT](https://jwt.io/introduction/)

---

## 📝 Autor

**LucaLzt**  
[LinkedIn](https://www.linkedin.com/in/luca-lazarte)  
[GitHub](https://github.com/LucaLzt)
