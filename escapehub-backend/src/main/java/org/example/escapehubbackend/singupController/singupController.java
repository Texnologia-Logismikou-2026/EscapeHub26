package org.example.escapehubbackend.singupController;

import org.example.escapehubbackend.authService.authService;
import org.example.escapehubbackend.dtoRegister.authResponse;
import org.example.escapehubbackend.dtoRegister.loginRequest;
import org.example.escapehubbackend.dtoRegister.registerRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class singupController {
    private final authService authService;

    public singupController(authService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<authResponse> register(@RequestBody registerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<authResponse> login(@RequestBody loginRequest request) {
        return ResponseEntity.ok(authService.logIn(request));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<authResponse> handleAuthError(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new authResponse(ex.getMessage(), "400"));
    }
}