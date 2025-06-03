package tqs.evsync.backend.integration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import tqs.evsync.backend.controller.AuthController;
import tqs.evsync.backend.model.User;
import tqs.evsync.backend.service.AuthService;

@ExtendWith(MockitoExtension.class)
class AuthControllerIT {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private AuthController.RegistrationRequest registrationRequest;
    private AuthController.LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        registrationRequest = new AuthController.RegistrationRequest("test@example.com", "password");
        loginRequest = new AuthController.LoginRequest("test@example.com", "password");
    }

    @Test
    void testRegister() {
        User mockUser = new User();
        mockUser.setEmail(registrationRequest.email());
        mockUser.setPassword(registrationRequest.password());
        when(authService.registerUser(registrationRequest.email(), registrationRequest.password()))
            .thenReturn(mockUser);

        ResponseEntity<String> response = authController.register(registrationRequest);

        assertEquals("User registered successfully", response.getBody());
        verify(authService, times(1)).registerUser(registrationRequest.email(), registrationRequest.password());
    }

    @Test
    void testLogin() {
        when(authService.authenticate(loginRequest.email(), loginRequest.password())).thenReturn("token");

        ResponseEntity<String> response = authController.login(loginRequest);

        assertEquals("token", response.getBody());
        verify(authService, times(1)).authenticate(loginRequest.email(), loginRequest.password());
    }

    @Test
    void testRegistrationRequestRecord() {
        AuthController.RegistrationRequest request = new AuthController.RegistrationRequest("user@test.com", "pass123");
        assertEquals("user@test.com", request.email());
        assertEquals("pass123", request.password());
    }

    @Test
    void testLoginRequestRecord() {
        AuthController.LoginRequest request = new AuthController.LoginRequest("user@test.com", "pass123");
        assertEquals("user@test.com", request.email());
        assertEquals("pass123", request.password());
    }
}