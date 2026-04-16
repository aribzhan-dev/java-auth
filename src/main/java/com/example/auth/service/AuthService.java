package com.example.auth.service;

import com.example.auth.dto.*;
import com.example.auth.entity.User;
import com.example.auth.repository.UserRepository;
import com.example.auth.security.JwtService;
import com.example.auth.security.LoginAttemptService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final LoginAttemptService loginAttemptService;
    private final JwtService jwtService;

    public AuthResponse register(RegisterRequest req) {

        if (!req.getPassword().trim().equals(req.getVerifyPassword().trim())) {
            throw new RuntimeException("Passwords do not match");
        }

        if (userRepository.existsByEmail(req.getEmail().trim().toLowerCase())) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setName(req.getName().trim());
        user.setEmail(req.getEmail().trim().toLowerCase());
        user.setPassword(passwordEncoder.encode(req.getPassword().trim()));

        userRepository.save(user);

        String accessToken = jwtService.generateAccessToken(user.getEmail());
        String refreshToken = jwtService.generateRefreshToken(user.getEmail());

        return new AuthResponse(
                accessToken,
                refreshToken,
                "registered successfully"
        );
    }

    public AuthResponse login(LoginRequest req) {

        String email = req.getEmail().trim().toLowerCase();

        if (loginAttemptService.isBlocked(email)) {
            throw new RuntimeException("Too many attempts. Try again in 10 minutes");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(req.getPassword().trim(), user.getPassword())) {
            loginAttemptService.loginFailed(email);
            throw new RuntimeException("Wrong password");
        }

        loginAttemptService.loginSuccess(email);

        String accessToken = jwtService.generateAccessToken(user.getEmail());
        String refreshToken = jwtService.generateRefreshToken(user.getEmail());

        return new AuthResponse(
                accessToken,
                refreshToken,
                "logged in successfully"
        );
    }

    public String me(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return "Hello, " + user.getName() + " (" + user.getEmail() + ")";
    }
}