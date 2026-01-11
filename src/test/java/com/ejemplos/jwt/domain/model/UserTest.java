package com.ejemplos.jwt.domain.model;

import com.ejemplos.jwt.domain.enums.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    @Test
    @DisplayName("Domain: Creating a new user should assign CLIENT role by default")
    void shouldCreateClientUser() {
        // GIVEN
        String firstName = "John";
        String lastName = "Doe";
        String email = "test@demo.com";
        String password = "testPassword";

        // WHEN
        User user = User.create(firstName, lastName, email, password);

        // THEN
        assertThat(user).isNotNull();
        assertThat(user.getFirstName()).isEqualTo(firstName);
        assertThat(user.getLastName()).isEqualTo(lastName);
        assertThat(user.getEmail()).isEqualTo(email);
        assertThat(user.getPassword()).isEqualTo(password);
        assertThat(user.isEnabled()).isTrue();

        assertThat(user.getRole()).isEqualTo(UserRole.CLIENT);
    }

    @Test
    @DisplayName("Domain: Toggling user role should switch from CLIENT to ADMIN")
    void shouldChangeRole() {
        // GIVEN
        User user = User.create("John", "Doe", "test@demo.com", "testPassword");
        assertThat(user.getRole()).isEqualTo(UserRole.CLIENT);

        // WHEN
        user.toggleRole();

        // THEN
        assertThat(user.getRole()).isEqualTo(UserRole.ADMIN);
    }
}
