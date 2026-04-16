package com.example.auth.controller;

import com.example.auth.dto.*;
import com.example.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;


@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public AuthResponse register(@RequestBody RegisterRequest req) {
        return authService.register(req);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest req) {
        return authService.login(req);
    }

    @GetMapping("/me")
    public String me() {
        return "You are authorized";
    }
}