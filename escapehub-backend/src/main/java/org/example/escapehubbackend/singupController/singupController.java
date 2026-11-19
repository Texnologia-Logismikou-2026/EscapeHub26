package org.example.escapehubbackend.singupController;


import org.example.escapehubbackend.authService.authService;
import org.example.escapehubbackend.dtoRegister.authResponse;
import org.example.escapehubbackend.dtoRegister.loginRequest;
import org.example.escapehubbackend.dtoRegister.registerRequest;
import org.example.escapehubbackend.repository.UserRepository;
import org.springframework.http.HttpEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
    public ResponseEntity<authResponse> register(@RequestBody registerRequest request, HttpEntity<Object> httpEntity){
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }
    @PostMapping("/login")
    public ResponseEntity<authResponse> login(@RequestBody loginRequest request){
        return ResponseEntity.ok(authService.logIn(request));
    }
}
