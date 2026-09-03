package com.dlp.service;

import com.dlp.model.entity.User;
import com.dlp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    private AuthService service;

    @BeforeEach
    void setUp() {
        service = new AuthService(userRepository, passwordEncoder);
    }

    @Test
    void registerHashesPasswordAndDefaultsRole() {
        when(userRepository.existsByEmail("a@b.com")).thenReturn(false);
        when(passwordEncoder.encode("secret")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User user = service.register("Ada", "a@b.com", "secret");

        assertEquals("Ada", user.getName());
        assertEquals("a@b.com", user.getEmail());
        assertEquals("hashed", user.getPassword());
        assertEquals("USER", user.getRole());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerRejectsDuplicateEmail() {
        when(userRepository.existsByEmail("dup@b.com")).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> service.register("Dup", "dup@b.com", "x"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void findByEmailDelegatesToRepository() {
        User u = new User();
        when(userRepository.findByEmail(eq("a@b.com"))).thenReturn(Optional.of(u));

        assertSame(u, service.findByEmail("a@b.com").orElseThrow());
    }
}
