package tqs.evsync.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tqs.evsync.backend.repository.UserRepository;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    public boolean authenticate(String email, String rawPassword) {
        return userRepository.findByEmail(email)
            .map(user -> rawPassword.equals(user.getPassword())) 
            .orElse(false);
    }
}
