package com.example.auth.service;

import com.example.auth.dto.*;
import com.example.auth.entity.User;
import com.example.auth.repository.UserRepository;
import com.example.auth.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    private final Pattern EMAIL_REGEX =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    public AuthResponse register(RegisterRequest req) {

        if (!EMAIL_REGEX.matcher(req.getEmail()).matches()) {
            throw new RuntimeException("Invalid email");
        }

        if (req.getPassword().length() < 8) {
            throw new RuntimeException("Password too short");
        }

        if (!req.getPassword().equals(req.getVerifyPassword())) {
            throw new RuntimeException("Passwords do not match");
        }

        if (userRepository.existsByEmail(req.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = User.builder()
                .name(req.getName())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .build();

        userRepository.save(user);

        String token = jwtService.generateToken(user.getEmail());

        return AuthResponse.builder()
                .accessToken(token)
                .refreshToken(token)
                .tokenType("Bearer")
                .build();
    }

    public AuthResponse login(LoginRequest req) {

        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new RuntimeException("Wrong password");
        }

        String token = jwtService.generateToken(user.getEmail());

        return AuthResponse.builder()
                .accessToken(token)
                .refreshToken(token)
                .tokenType("Bearer")
                .build();
    }
}