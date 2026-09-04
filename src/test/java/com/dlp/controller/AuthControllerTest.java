package com.dlp.controller;

import com.dlp.model.entity.User;
import com.dlp.security.JwtService;
import com.dlp.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
@Import(com.dlp.config.TestSecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private com.dlp.security.RefreshTokenService refreshTokenService;

    @MockBean
    private com.dlp.security.CustomUserDetailsService customUserDetailsService;

    private User savedUser;

    @BeforeEach
    void setUp() {
        savedUser = new User();
        savedUser.setId(1L);
        savedUser.setName("Alice");
        savedUser.setEmail("alice@test.com");
        savedUser.setPassword("hashed");
        savedUser.setRole("USER");
    }

    @Test
    void registerReturnsCreatedUser() throws Exception {
        when(authService.register("Alice", "alice@test.com", "secret123")).thenReturn(savedUser);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Alice\",\"email\":\"alice@test.com\",\"password\":\"secret123\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("alice@test.com"))
                .andExpect(jsonPath("$.name").value("Alice"));

        verify(authService).register("Alice", "alice@test.com", "secret123");
    }

    @Test
    void registerRejectsMissingFields() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Bob\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());

        verifyNoInteractions(authService);
    }

    @Test
    void loginReturnsTokenOnValidCredentials() throws Exception {
        UserDetails principal = org.springframework.security.core.userdetails.User
                .withUsername("alice@test.com")
                .password("secret123")
                .authorities("ROLE_USER")
                .build();
        Authentication auth = new UsernamePasswordAuthenticationToken(principal, "secret123");
        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn("mock-jwt-token");
        when(refreshTokenService.generateRefreshToken("alice@test.com")).thenReturn("mock-refresh-token");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"alice@test.com\",\"password\":\"secret123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("mock-jwt-token"))
                .andExpect(jsonPath("$.email").value("alice@test.com"));

        verify(authenticationManager).authenticate(any());
        verify(jwtService).generateToken(any(UserDetails.class));
    }

    @Test
    void refreshReturnsNewAccessTokenWhenTokenIsValid() throws Exception {
        when(refreshTokenService.validateRefreshToken("alice@test.com", "valid-refresh-token")).thenReturn(true);
        when(refreshTokenService.generateRefreshToken("alice@test.com")).thenReturn("new-refresh-token");
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn("new-access-token");

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"alice@test.com\",\"refreshToken\":\"valid-refresh-token\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access-token"))
                .andExpect(jsonPath("$.refreshToken").value("new-refresh-token"));

        verify(refreshTokenService).revokeRefreshToken("alice@test.com");
    }

    @Test
    void refreshRejectsInvalidToken() throws Exception {
        when(refreshTokenService.validateRefreshToken("alice@test.com", "bad-token")).thenReturn(false);

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"alice@test.com\",\"refreshToken\":\"bad-token\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid or expired refresh token"));
    }

    @Test
    void logoutRevokesRefreshToken() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"alice@test.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Logged out"));

        verify(refreshTokenService).revokeRefreshToken("alice@test.com");
    }
}
