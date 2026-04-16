package com.example.auth.security;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {

    private final int MAX_ATTEMPT = 5;
    private final int BLOCK_MINUTES = 10;

    private final ConcurrentHashMap<String, Integer> attempts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, LocalDateTime> blocked = new ConcurrentHashMap<>();

    public void loginFailed(String email) {
        attempts.put(email, attempts.getOrDefault(email, 0) + 1);

        if (attempts.get(email) >= MAX_ATTEMPT) {
            blocked.put(email, LocalDateTime.now().plusMinutes(BLOCK_MINUTES));
        }
    }

    public void loginSuccess(String email) {
        attempts.remove(email);
        blocked.remove(email);
    }

    public boolean isBlocked(String email) {
        if (!blocked.containsKey(email)) return false;

        if (blocked.get(email).isBefore(LocalDateTime.now())) {
            blocked.remove(email);
            attempts.remove(email);
            return false;
        }

        return true;
    }
}