package com.dlp.controller;

import com.dlp.model.entity.User;
import com.dlp.security.JwtService;
import com.dlp.security.RefreshTokenService;
import com.dlp.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public AuthController(AuthService authService,
                          AuthenticationManager authenticationManager,
                          JwtService jwtService,
                          RefreshTokenService refreshTokenService) {
        this.authService = authService;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        String name = body.get("name");
        String email = body.get("email");
        String password = body.get("password");
        if (name == null || email == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "name, email and password are required"));
        }
        User user = authService.register(name, email, password);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "id", user.getId(),
                "email", user.getEmail(),
                "name", user.getName()));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String password = body.get("password");
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password));
        var userDetails = (UserDetails) auth.getPrincipal();
        String accessToken = jwtService.generateToken(userDetails);
        String refreshToken = refreshTokenService.generateRefreshToken(email);
        return ResponseEntity.ok(Map.of(
                "accessToken", accessToken,
                "refreshToken", refreshToken,
                "email", email));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String refreshToken = body.get("refreshToken");
        if (email == null || refreshToken == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "email and refreshToken are required"));
        }
        if (!refreshTokenService.validateRefreshToken(email, refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid or expired refresh token"));
        }
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(email)
                .password("")
                .authorities("ROLE_USER")
                .build();
        String newAccessToken = jwtService.generateToken(userDetails);
        refreshTokenService.revokeRefreshToken(email);
        String newRefreshToken = refreshTokenService.generateRefreshToken(email);
        return ResponseEntity.ok(Map.of(
                "accessToken", newAccessToken,
                "refreshToken", newRefreshToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        if (email != null) {
            refreshTokenService.revokeRefreshToken(email);
        }
        return ResponseEntity.ok(Map.of("message", "Logged out"));
    }
}
