# Proyecto JWT con Spring Boot

Este repositorio implementa un sistema de **autenticación y autorización** en API REST usando **JSON Web Tokens (JWT)** con **Spring Boot 3** y **Spring Security**, incorporando:
  - **Access Tokens** de vida corta para autenticar peticiones.
  - **Refresh Tokens** de vida larga para renovar accesos sin re-login.
  - Persistencia del refresh token en la base de datos.
  - Endpoints de `login`, `register`, `refresh` y `logout`.
  - Flujo de recuperación de contraseña con token temporal (15 min) enviado por mail. 

El objetivo es ser una guía clara y práctica de cómo manejar JWT de manera **segura y escalable** en aplicaciones Java modernas.

---

## Características principales

- Registro y login de usuarios con contraseña encriptada.
- Emisión de **access + refresh tokens**.
- Endpoint `/api/auth/refresh` para renovar access tokens usando refresh.
- Endpoint `/api/auth/logout` para invalidar tokens en servidor y cerrar sesión inmediatamente.
- Flujo de **recuperación de contraseña**:
  - `/api/auth/forgot-password` genera un token UUID válido por 15 minutos y lo envía por correo.
  - `/api/auth/reset-password` permite actualizar la contraseña validando ducho token.
- Servicio de emails desacoplado (`EmailService`) que encapsula `JavaMailSender`.
- Limpieza automática de tokens (revocados y de recuperación) mediante un job programado con `@Scheduled`.
- Integración completa con **Spring Security**.
- **MySQL** como base de datos relacional.
- **Docker Compose** para levantar app + MySQL + configuración con `.env`.
- Código ampliamente comentado para aprendizaje.

---

## Tecnologías utilizadas

- **Java 21**
- **Spring Boot 3**
- **Spring Security**
- **Spring Data JPA**
- **JJWT** (`io.jsonwebtoken`)
- **JavaMailSender** (Spring Mail)
- **MySQL**
- **Maven**
- **Docker & Docker Compose**

---

## Instalación y ejecución

### 1. Clonar el repositorio

```bash
git clone https://github.com/LucaLzt/jwt-ejemplo.git
cd jwt-ejemplo
```
### 2. Configuración de variables de entorno

El proyecto utiliza un archivo `.env` para credenciales y configuraciones sensibles.
En el repositorio vas a encontrar un `.env.example`. Copialo y completalo con tus valores:

```bash
cp .env.example .env
```

Ejemplo de variables:

```env
# MySQL
MYSQL_ROOT_PASSWORD=supersecret
MYSQL_DATABASE=jwt_app
MYSQL_USER=jwt_user
MYSQL_PASSWORD=jwt_pass

# JWT
JWT_SECRET=clave_secreta_super_segura_1234567890123456
ACCESS_EXPIRATION=900000         # 15 minutos
REFRESH_EXPIRATION=1209600000    # 14 días

# Mailtrap (SMTP para pruebas)
MAIL_USER=tu_usuario_mailtrap
MAIL_PASS=tu_password_mailtrap
```

### Configuración de correo con Mailtrap

Este proyecto usa [Mailtrap](https://mailtrap.io/) para simular el envío de correos en desarrollo.
Mailtrap provee un servidor SMTP de sandbox que captura los correos enviados y los muestra en su web, sin
entragarlos a destinatarios reales.

Para configurarlo:

1. Crea una cuenta gratuita en [Mailtrap](https://mailtrap.io/).
2. En el dashboard, abre una **Inbox de Sandbox** (Mailtrap crea una por defecto).
3. Ve a la pestaña **SMTP Settings** y copia las credenciales.
4. Pega esas credenciales en tu archivo `.env` en las variables `MAIL_USER` y `MAIL_PASS`.

> Los correos enviados por la aplicación aparecerán en tu Inbox de Mailtrap y no se enviarán a destinatarios reales.

### 3. Levantar con Docker

Con el `docker-compose.yml` incluido:

```bash
docker compose up --build
```
Esto levanta:
- `mysql` en el puerto `3307` (con healthcheck para esperar a que esté listo).
- `spring-jwt-app` en el puerto `8080`.

### **4. Compilar localmente (opcional)**

```bash
mvn clean package -DskipTests
java -jar target/app.jar
```
---

## Flujo de autenticación

1. **Login / Register** → Devuelve `accessToken` + `refreshToken` en un `AuthDTO`.
2. **Acceso a endpoints protegidos** → Se envía el `accessToken` en el header:
```makefile
Authorization: Bearer <accessToken>
```
3. **Token expirado** → El cliente usa el `refreshToken` en `/api/auth/refresh` para obtener un nuevo par de tokens.
4. **Logout** → El servidor invalida el token guardado en la base de datos y agrega el access token a la blacklist, cerrando la sesión inmediatamente aunque el token no haya expirado.

---

## Flujo de recuperación de contraseña

1. **Solicitud de recuperación**:
   - Endpoint: `POST /api/auth/forgot-password`
   - Body: `{ "email": "usuario@demo.com" }`
   - Genera un token UUID con duración de 15 minutos y envía un link al correo configurado.
   
2. **Restablecimiento de contraseña**:
   - Endpoint: `POST /api/auth/reset-password`
   - Body: 
   ```json
    {
      "token": "<uuid>",
      "newPassword": "nueva_contraseña",
      "repeatNewPassword": "nueva_contraseña"
    }
    ```
   - Si el token es válido y no está expirado ni usado, actualiza la contraseña del usuario.

---

## Limpieza automática de tokens

El sistema incluye un job programado (`TokenCleanupJob`) que se ejecuta diariamente y elimina
de la base de datos todos los tokens revocados y de recuperación de contraseña cuya fecha de expiración 
(`expires_at`) ya pasó.
Esto evita que las tablas crezcan indefinidamente y mantiene la base optimizada.

---

## Estructura del proyecto

El proyecto está organizado siguiendo una arquitectura híbrida Vertical Slicing + Hexagonal Architecture donde:

- Cada feature (caso de uso principal) tiene sus propias capas internas (``web``, ``application``, ``domain``).
- La infraestructura técnica (mensajería, mail, scheduler, etc.) vive fuera de los features.
- La seguridad y otros aspectos transversales se encuentran en ``shared/``

Esto permite un código modular, escalable y fácilmente testable, manteniendo aisladas las reglas de negocio de la infraestructura.

```
src/
  main/java/com/ejemplos/jwt/
    shared/                         # Componentes transversales a toda la app.
        config/                     # Beans globales (ApplicationConfig)
        security/                   # Seguridad, JWT, filtros, utilidades.
    infra/                          # Infraestructura técnica (ajena al dominio).
        mail/                       # Implementación genérica de envío de emails.
        messaging/                  # Integración con RabbitMQ.
            email/                  # Productores, consumidores y topologías.
    features/                       # Slices verticales (cada uno con su mini-arquitectura).
        auth/
            web/                    # Controladores HTTP de autenticación.
            application/            # Casos de uso (login, register, refresh, logout).
                dto/                # Objetos de transferencia específicos del feature.
                service/            # Servicios de aplicación.
            domain/                 # Modelo de dominio de Auth.
                entity/             # User, RevokedToken.
                enums/              # UserRole.
                repository/         # Repositorios JPA de Auth.
        recoverypassword/
            web/                    # Controlador de recuperación de contraseña.
            application/            # Caso de uso (forgot-password, reset-password).
                dto/                # ForgotDTO, ResetDTO, RecoveryEmailDTO.
                service/            # Lógica del proceso de recuperación.
            domain/              
                entity/             # PasswordResetToken.
                repository/         # PasswordResetTokenRepository.
    JwtApplication.java             # Punto de entrada a Spring Boot.
  resources/
    application.properties
docker-compose.yml                  # MySQL + RabbitMQ + App Spring Boot.
Dockerfile                          # Imagen Docker multi-stage.
.env.example                        # Variables de entorno del proyecto.
```

---

## Recursos recomendados

- [Documentación oficial Spring Security](https://docs.spring.io/spring-security/reference/)
- [Documentación JJWT](https://github.com/jwtk/jjwt)
- [Artículo sobre JWT](https://jwt.io/introduction/)
- [Spring Mail Reference](https://docs.spring.io/spring-framework/reference/integration/email.html)
- [Mailtrap](https://mailtrap.io/) (SMTP para pruebas en desarrollo)

---

## Autor

**LucaLzt**  
[LinkedIn](https://www.linkedin.com/in/luca-lazarte)  
[GitHub](https://github.com/LucaLzt)
