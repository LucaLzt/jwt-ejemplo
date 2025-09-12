# Proyecto JWT con Spring Boot

Este repositorio es un **ejemplo práctico** de implementación de autenticación y autorización en APIs REST usando **JWT (Json Web Token)** con **Spring Boot** y **Spring Security**. El objetivo es servir como guía y material de estudio para quienes deseen aprender buenas prácticas de seguridad en aplicaciones Java.

---

## 🚀 Características principales

- Autenticación de usuarios vía JWT
- Autorización de endpoints protegidos
- Integración con Spring Security
- Uso de MySQL como base de datos
- Configuración flexible mediante variables de entorno o `application.properties`
- Código comentado para facilitar el aprendizaje

---

## 🛠️ Tecnologías utilizadas

- **Java 21**
- **Spring Boot 3**
- **Spring Security**
- **Spring Data JPA**
- **JJWT** (`io.jsonwebtoken`)
- **MySQL**
- **Maven**

---

## 📦 Instalación y ejecución

### **1. Clonar el repositorio**

```bash
git clone https://github.com/LucaLzt/jwt-ejemplo.git
cd jwt-ejemplo
```

### **2. Configurar la base de datos**

Asegúrate de tener MySQL corriendo y una base de datos creada. Puedes usar Docker con el archivo `docker-compose.yml` incluido:

```bash
docker compose up
```

### **3. Configurar variables de entorno**

Crea un archivo `.env` o edita el `application.properties` con tus datos de conexión y la clave secreta JWT:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/jwt_db?useSSL=false&serverTimezone=UTC
spring.datasource.username=jwt_user
spring.datasource.password=jwt_password
jwt.secret=clave_secreta_super_segura_1234567890123456
jwt.expiration=86400000
```

### **4. Compilar y ejecutar la app**

```bash
./mvnw spring-boot:run
```
o usando Docker:

```bash
docker build -t jwt-app .
docker run -p 8080:8080 --env-file .env jwt-app
```

---

## 🔑 ¿Cómo funciona la autenticación JWT?

1. El usuario envía sus credenciales al endpoint `/authenticate`.
2. Si son válidas, el backend genera y retorna un JWT.
3. El cliente utiliza ese token en el header `Authorization: Bearer <token>` en cada request a endpoints protegidos.
4. El backend valida el JWT en cada petición y autoriza el acceso según los datos del token.

---

## 📚 Estructura del proyecto

```
src/
  main/
    java/com/ejemplos/jwt/
      controllers/        # Controladores REST
      models/             # Entidades JPA
      repositories/       # Repositorios JPA
      services/           # Lógica de negocio
      utils/              # Utilidades (JwtUtil, filtros, etc)
    resources/
      application.properties # Configuración
  test/                    # Pruebas unitarias
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

---

## 🖤 ¿Te resultó útil?

¡No dudes en dar una estrella ⭐ al repositorio, dejar tus sugerencias o abrir issues para mejorar el proyecto!
