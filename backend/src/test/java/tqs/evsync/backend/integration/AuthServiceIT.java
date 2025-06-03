package tqs.evsync.backend.integration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import tqs.evsync.backend.model.User;
import tqs.evsync.backend.repository.UserRepository;
import tqs.evsync.backend.service.AuthService;

@ExtendWith(MockitoExtension.class)
class AuthServiceIT {
    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @InjectMocks private AuthService authService;

    @Test
    void whenRegisterUser_thenPasswordIsEncoded() {
        String rawPassword = "password123";
        String encodedPassword = "encoded123";
        User mockUser = new User();
        mockUser.setPassword(encodedPassword);
        
        // Mock the behavior
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenReturn(mockUser);
        
        // Test
        User result = authService.registerUser("test@example.com", rawPassword);
        
        // Verify
        assertNotNull(result);
        assertEquals(encodedPassword, result.getPassword());
        verify(passwordEncoder).encode(rawPassword);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void whenValidLogin_thenAuthenticate() {
        User user = new User();
        user.setPassword("encoded123");
        
        when(userRepository.findByEmail("test@example.com"))
            .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "encoded123"))
            .thenReturn(true);
        
        String result = authService.authenticate("test@example.com", "password123");
        assertTrue(result.contains("Successfully authenticated"));
    }

    @Test
    void whenInvalidPassword_thenThrowException() {
        User user = new User();
        user.setPassword("encoded123");
        
        when(userRepository.findByEmail("test@example.com"))
            .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongpass", "encoded123"))
            .thenReturn(false);
        
        assertThrows(RuntimeException.class, () -> {
            authService.authenticate("test@example.com", "wrongpass");
        });
    }
}