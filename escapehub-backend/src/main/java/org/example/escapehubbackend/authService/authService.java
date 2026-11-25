package org.example.escapehubbackend.authService;

import org.example.escapehubbackend.dtoRegister.authResponse;
import org.example.escapehubbackend.dtoRegister.loginRequest;
import org.example.escapehubbackend.dtoRegister.registerRequest;
import org.example.escapehubbackend.model.User;
import org.example.escapehubbackend.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class authService {
    private final UserRepository userRepository;

    public authService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public authResponse register(registerRequest request) {
        String username = request.getName() != null ? request.getName().trim() : "";
        String password = request.getPass() != null ? request.getPass() : "";

        if (username.isEmpty() || password.isEmpty()) {
            throw new IllegalArgumentException("Username and password are required");
        }

        String email = (request.getEmail() != null && !request.getEmail().isBlank())
                ? request.getEmail().trim()
                : username + "@escapehub.local";

        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username is already taken");
        }
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email is already registered");
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);

        User saved = userRepository.save(user);

        return new authResponse(
                "User registered successfully",
                "200",
                saved.getId(),
                saved.getUsername(),
                saved.getEmail()
        );
    }

    public authResponse logIn(loginRequest loginRequest) {
        String identifier = loginRequest.getEmail() != null ? loginRequest.getEmail().trim() : "";
        String password = loginRequest.getPass() != null ? loginRequest.getPass() : "";

        User user = userRepository.findByUsername(identifier)
                .or(() -> userRepository.findByEmail(identifier))
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));

        if (!user.getPassword().equals(password)) {
            throw new IllegalArgumentException("Invalid username or password");
        }

        return new authResponse(
                "Login successful",
                "200",
                user.getId(),
                user.getUsername(),
                user.getEmail()
        );
    }
}