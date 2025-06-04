package tqs.evsync.backend.integration;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.SecurityFilterChain;

import tqs.evsync.backend.authConfig.SecurityConfig;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SecurityConfigTest {

    @Test
    void testSecurityFilterChain() throws Exception {
        SecurityConfig securityConfig = new SecurityConfig();
        HttpSecurity http = mock(HttpSecurity.class);
        
        // Mock the CSRF configurer
        CsrfConfigurer<HttpSecurity> csrfConfigurer = mock(CsrfConfigurer.class);
        
        // Create a mock DefaultSecurityFilterChain (the actual implementation)
        DefaultSecurityFilterChain mockFilterChain = mock(DefaultSecurityFilterChain.class);
        
        // Mock the method chain
        when(http.csrf()).thenReturn(csrfConfigurer);
        when(csrfConfigurer.disable()).thenReturn(http);
        when(http.authorizeHttpRequests(any())).thenReturn(http);
        when(http.build()).thenReturn(mockFilterChain);
        
        SecurityFilterChain filterChain = securityConfig.securityFilterChain(http);
        
        assertNotNull(filterChain);
        verify(http).csrf();
        verify(csrfConfigurer).disable();
        verify(http).authorizeHttpRequests(any());
        verify(http).build();
    }

    @Test
    void testPasswordEncoder() {
        SecurityConfig securityConfig = new SecurityConfig();
        PasswordEncoder encoder = securityConfig.passwordEncoder();
        
        assertNotNull(encoder);
        String rawPassword = "password";
        String encodedPassword = encoder.encode(rawPassword);
        
        assertNotEquals(rawPassword, encodedPassword);
        assertTrue(encoder.matches(rawPassword, encodedPassword));
    }
}