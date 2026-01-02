package com.ejemplos.jwt.domain.model;

import com.ejemplos.jwt.domain.enums.UserRole;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Builder
public class User {

    private final Long id;
    private String firstName;
    private String lastName;
    private String email;
    @Setter
    private String password;
    private UserRole role;
    private boolean enabled;
    private Instant createdAt;
    private Instant updatedAt;

    public User(Long id, String firstName, String lastName, String email, String password, UserRole role, boolean enabled, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.role = role;
        this.enabled = enabled;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static User createAdmin(String firstName, String lastName, String email, String password) {
        return new User(
                null,
                firstName,
                lastName,
                email,
                password,
                UserRole.ADMIN,
                true,
                Instant.now(),
                Instant.now()
        );
    }

    public static User createClient(String firstName, String lastName, String email, String password) {
        return new User(
                null,
                firstName,
                lastName,
                email,
                password,
                UserRole.CLIENT,
                true,
                Instant.now(),
                Instant.now()
        );
    }

    public void toggleRole() {
        if (this.role == UserRole.ADMIN) {
            this.role = UserRole.CLIENT;
        } else {
            this.role = UserRole.ADMIN;
        }
        this.updatedAt = Instant.now();
    }
}