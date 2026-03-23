package com.carlos.payflowguard.auth.controller;

import com.carlos.payflowguard.auth.dto.AuthRequest;
import com.carlos.payflowguard.auth.dto.AuthResponse;
import com.carlos.payflowguard.auth.dto.UserResponse;
import com.carlos.payflowguard.auth.service.AuthService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public AuthResponse register(@RequestBody AuthRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody AuthRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public UserResponse getCurrentUser() {
        return authService.getCurrentUser();
    }
}