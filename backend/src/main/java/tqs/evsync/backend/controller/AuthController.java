package tqs.evsync.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tqs.evsync.backend.service.AuthService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<String> register(
            @RequestBody RegistrationRequest request) {
        authService.registerUser(request.email(), request.password());
        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(
            @RequestBody LoginRequest request) {
        String authResult = authService.authenticate(
            request.email(), 
            request.password()
        );
        return ResponseEntity.ok(authResult);
    }

    public record RegistrationRequest(String email, String password) {}
    public record LoginRequest(String email, String password) {}
}