# Proyecto JWT con Spring Boot - Autenticación, Refresh Tokens, Password Recovery y RabbitMQ

Este repositorio implementa un sistema completo de autenticación y autorización basada en JWT, diseñado
con una arquitectura Vertical Slicing + Hexagonal Architecture (Ports & Adapters pragmático), integrando:

### Autenticación
- Access tokens de vida corta.
- Refresh tokens persistidos en base de datos.
- Logout seguro (blacklist + revocación).
- Spring Security 6 + filtros personalizados.

### Recuperación de contraseña
- Token temporal de recuperación (15 minutos).
- Email asincrónico enviado mediante RabbitMQ.
- Procesamiento desacoplado vía `EmailProducer` y `EmailConsumer`.

### Arquitectura
- Vertical Slicing por feature: `auth`, `recoverypassword`.
- Hexagonal Architecture pragmática: separación clara de _web / application / domain / infra / shared_.
- Envío de correo desacoplado (`EmailService`), implementado en infraestructura.
- Adaptadores para email y mensajería RabbitMQ.

### Deploy con Docker Compose
- App Spring Boot
- MySQL 8.3
- RabbitMQ + Management UI
- Variables desde `.env`.

---

## Características principales

### Autenticación y registro
- `/api/auth/register`
- `/api/auth/login`
- `/api/auth/refresh`
- `/api/auth/logout`

### Seguridad
- Implementación de `JwtAuthenticationFilter` propia.
- Token revocado guardado en base + limpieza periódica.
- Password encriptado con BCrypt.
- Refresh Token persistido en `User`.

### Refresh Token seguro
- `refreshToken` guardado en DB.
- Cada refresh genera un nuevo par (access + refresh).
- El refresh token previo queda invalidado.

### Recuperación de contraseña
- `POST /api/recovery/forgot-password`
  - Genera token UUID, persistido por 15 minutos.
  - Publica mensaje a RabbitMQ -> `EmailProducer`.
- `EmailConsumer` recibe el mensaje y usa `EmailService` para enviar email real.
- `POST /api/recovery/reset-password`
  - Valida token + exp. + coincidencia de passwords -> actualiza contraseña.

### Email desacoplado
- `EmailService` (interface en `shared.email`)
- `EmailServiceImpl` (infraestructura usando JavaMailSender)
- Config Mailtrap (SMTP Sandbox)

### Limpieza automática de tokens
- `TokenCleanupJob` programado con `@Scheduled`.
- Borra tokens expirados (revoked + recovery).

---

## Tecnologías utilizadas
- Java 21
- Spring Boot 3.3+
- Spring Security 6
- Spring Data JPA
- JJWT (io.jsonwebtoken)
- JavaMailSender
- RabbitMQ + Management UI
- MySQL 8.3
- Maven
- Docker & Docker Compose
- Lombok

---

## Instalación y ejecución

### 1. Clonar el repositorio

```bash
git clone https://github.com/LucaLzt/jwt-ejemplo.git
cd jwt-ejemplo
```

---

### 2. Configurar variables de entorno

```bash
co .env.example .env
```

Completar con tus valores:

```env
# MySQL
MYSQL_ROOT_PASSWORD=supersecret
MYSQL_DATABASE=jwt_app
MYSQL_USER=jwt_user
MYSQL_PASSWORD=jwt_pass

# JWT
JWT_SECRET=clave_super_segura_32_chars_minimo
ACCESS_EXPIRATION=900000     # 15 minutos
REFRESH_EXPIRATION=1209600000 # 14 días

# Mailtrap SMTP
MAIL_USER=xxxx
MAIL_PASS=xxxx

# RabbitMQ (para recuperación de contraseña)
RABBITMQ_USER=guest
RABBITMQ_PASS=guest
RABBITMQ_EMAIL_RECOVERY_PASSWORD_QUEUE=email.recovery.queue
RABBITMQ_EMAIL_RECOVERY_PASSWORD_EXCHANGE=email.recovery.exchange
RABBITMQ_EMAIL_RECOVERY_PASSWORD_ROUTING_KEY=email.recovery.routing
RABBITMQ_EMAIL_RECOVERY_PASSWORD_DLQ=email.recovery.dlq
RABBITMQ_EMAIL_RECOVERY_PASSWORD_DLX=email.recovery.dlx
```

---

### 3. Ejecutar con Docker Compose

```bash
docker-compose up --build
```

Esto levanta:
- MySQL -> puerto 3307
- RabbitMQ -> puertos 5672 / 15672
- Spring Boot -> puerto 8080

UI de RabbitMQ:
```txt
http://localhost:15672
```

---

## Flujo de autenticación

1. Usuario hace login -> se devuelven `accessToken` + `refreshToken`.
2. Cada request a endpoints protegidos requiere:
```makefile
Authorization: Bearer <accessToken>
```
3. Si expira -> `POST /api/auth/refresh`.
4. Logout -> invalida refresh + agrega access a blacklist.

---

## Flujo de recuperación de contraseña

1. Usuario solicita recuperación
```bash
POST /api/recovery/forgot-password

{
  "email": "usuario@demo.com"
}
```

El servicio:
* Genera token UUID (15 minutos).
* Lo persiste.
* Llama a `EmailProducer`.
* Producer publica mensaje -> RabbitMQ.

2. RabbitMQ -> Envío del correo
`EmailConsumer`:
    - Recibe el DTO.
    - Llama a `EmailService.sendPasswordReset(...)`.
    - Email llega a Mailtrap.

3. Usuario cambia contraseña
```bash
POST /api/recovery/reset-password
```
Valida token -> actualiza contraseña -> borra token usado.

---

## Arquitectura del proyecto

### Vertical Slicing + Hexagonal Architecture (pragmática)

El código está organizado por features y en capas internas inspiradas en Hexagonal Architecture:

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

- [Documentación Spring Security](https://docs.spring.io/spring-security/reference/)
- [Mailtrap](https://mailtrap.io/)
- [JWT Introduction](https://jwt.io/introduction/)
- [RabbitMQ Docs](https://www.rabbitmq.com/documentation.html)

---

## Autor

**LucaLzt**  
[LinkedIn](https://www.linkedin.com/in/luca-lazarte)  
[GitHub](https://github.com/LucaLzt)
