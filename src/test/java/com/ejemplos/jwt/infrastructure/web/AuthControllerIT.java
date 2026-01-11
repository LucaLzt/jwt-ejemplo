package com.ejemplos.jwt.infrastructure.web;

import com.ejemplos.jwt.AbstractIT;
import com.ejemplos.jwt.infrastructure.web.dto.LoginRequest;
import com.ejemplos.jwt.infrastructure.web.dto.LogoutRequest;
import com.ejemplos.jwt.infrastructure.web.dto.RefreshRequest;
import com.ejemplos.jwt.infrastructure.web.dto.RegisterRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
class AuthControllerIT extends AbstractIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Successful Registration: Should return 201 Created")
    void shouldRegisterUserSuccessfully() throws Exception {
        RegisterRequest request = new RegisterRequest("Test", "Demo", "test@demo.com", "testPassword");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Business Error: Should return 409 Conflict if the email already exists")
    void shouldFailWhenEmailAlreadyExists() throws Exception {
        createUserInDatabase("duplicado@test.com", "123456");

        RegisterRequest duplicateRequest = new RegisterRequest("Impostor", "User", "duplicado@test.com", "testPassword");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateRequest)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Validation: Should return 400 Bad Request if the email format is invalid")
    void shouldFailWhenEmailIsMalformed() throws Exception {
        RegisterRequest invalidRequest = new RegisterRequest("Test", "Demo", "malformed-email", "testPassword");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Successful Login: Should return 200 OK and issue Access/Refresh tokens")
    void shouldLoginSuccessfully() throws Exception {
        String email = "test@demo.com";
        String password = "testPassword";

        createUserInDatabase(email, password);

        LoginRequest loginRequest = new LoginRequest(email, password);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty());
    }

    @Test
    @DisplayName("Validation: Should return 400 Bad Request if the login email is malformed")
    void shouldFailLoginWhenEmailIsMalformed() throws Exception {
        LoginRequest invalidLoginRequest = new LoginRequest("malformed-email", "testPassword");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidLoginRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Validation: Should return 400 Bad Request if the password is empty")
    void shouldFailLoginWhenPasswordIsEmpty() throws Exception {
        LoginRequest emptyPassRequest = new LoginRequest("test@demo.com", "");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emptyPassRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Security: Should return 404 Not Found if the user does not exist (No User Enumeration)")
    void shouldFailLoginWhenUserDoesNotExist() throws Exception {
        // No user is created in the database

        LoginRequest ghostRequest = new LoginRequest("test@demo.com", "testPassword");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ghostRequest)))
                .andExpect(status().isNotFound());  // Todo: We need to change this to 401 Unauthorized for security reasons
    }

    @Test
    @DisplayName("Security: Should return 401 Unauthorized if the password is incorrect")
    void shouldFailLoginWithWrongPassword() throws Exception {
        createUserInDatabase("test@demo.com", "testPassword");

        LoginRequest badRequest = new LoginRequest("test@demo.com", "wrongPassword");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Successful Token Refresh: Should return 200 OK and rotate tokens")
    void shouldRefreshToken() throws Exception {
        String email = "test@demo.com";
        String password = "testPassword";

        createUserInDatabase(email, password);
        Tokens tokens = loginAndGetTokens(email, password);

        RefreshRequest request = new RefreshRequest(tokens.refreshToken);

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty());
    }

    @Test
    @DisplayName("Validation: Should return 400 Bad Request if the refresh token is missing")
    void shouldFailRefreshWhenTokenIsMissing() throws Exception {
        RefreshRequest emptyRequest = new RefreshRequest("");

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emptyRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Validation: Should return 400 Bad Request if the refresh token format is invalid")
    void shouldFailRefreshWhenTokenIsInvalid() throws Exception {
        RefreshRequest fakeRequest = new RefreshRequest("invalid-refresh-token-uuid");

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(fakeRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Security: Should return 403 Forbidden if a refresh token is reused (Reuse Detection)")
    void shouldFailRefreshWhenTokenIsReused() throws Exception {
        String email = "test@demo.com";
        String password = "testPassword";

        createUserInDatabase(email, password);
        Tokens tokensV1 = loginAndGetTokens(email, password);

        RefreshRequest requestV1 = new RefreshRequest(tokensV1.refreshToken());
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestV1)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestV1)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Successful Logout: Should invalidate session and return 204 No Content")
    void shouldLogout() throws Exception {
        String email = "test@demo.com";
        String password = "testPassword";

        createUserInDatabase(email, password);
        Tokens tokens = loginAndGetTokens(email, password);

        LogoutRequest request = new LogoutRequest(tokens.refreshToken);

        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer " + tokens.accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Validation: Should return 400 Bad Request if the refresh token is missing in body")
    void shouldFailLogoutWhenTokenIsMissing() throws Exception {
        String email = "test@demo.com";
        String password = "testPassword";

        createUserInDatabase(email, password);
        Tokens tokens = loginAndGetTokens(email, password);

        LogoutRequest invalidRequest = new LogoutRequest("");

        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer " + tokens.accessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Security: Should return 401 Unauthorized if Authorization header is missing")
    void shouldFailLogoutWhenAccessTokenIsMissing() throws Exception {
        LogoutRequest logoutRequest = new LogoutRequest("some-refresh-token");

        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(logoutRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Security: Should return 401 Unauthorized if Authorization header contains invalid token")
    void shouldFailLogoutWhenAccessTokenIsInvalid() throws Exception {
        LogoutRequest logoutRequest = new LogoutRequest("some-refresh-token");

        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer wrong-access-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(logoutRequest)))
                .andExpect(status().isUnauthorized());
    }

    private void createUserInDatabase(String email, String password) throws Exception {
        RegisterRequest request = new RegisterRequest("Aux", "Aux", email, password);
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    private Tokens loginAndGetTokens(String email, String password) throws Exception {
        LoginRequest loginRequest = new LoginRequest(email, password);
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return new Tokens(
                json.get("accessToken").asText(),
                json.get("refreshToken").asText()
        );
    }

    // Record class for move both tokens together
    record Tokens(String accessToken, String refreshToken) {
    }
}
