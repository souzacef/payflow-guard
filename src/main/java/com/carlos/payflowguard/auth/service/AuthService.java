package com.carlos.payflowguard.auth.service;

import com.carlos.payflowguard.auth.dto.AuthRequest;
import com.carlos.payflowguard.auth.dto.AuthResponse;
import com.carlos.payflowguard.auth.dto.UserResponse;
import com.carlos.payflowguard.common.exception.UnauthorizedException;
import com.carlos.payflowguard.security.JwtService;
import com.carlos.payflowguard.user.entity.User;
import com.carlos.payflowguard.user.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            UserRepository userRepository,
            JwtService jwtService,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthResponse register(AuthRequest request) {
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        userRepository.save(user);

        String token = jwtService.generateToken(user.getEmail());

        return new AuthResponse(token);
    }

    public AuthResponse login(AuthRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Invalid credentials");
        }

        String token = jwtService.generateToken(user.getEmail());

        return new AuthResponse(token);
    }

    public UserResponse getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (!(principal instanceof String email)) {
            throw new UnauthorizedException("Unauthorized");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Unauthorized"));

        return new UserResponse(user.getId(), user.getEmail());
    }
}