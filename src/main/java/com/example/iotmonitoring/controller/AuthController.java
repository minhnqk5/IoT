package com.example.iotmonitoring.controller;

import java.util.Map;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import com.example.iotmonitoring.dto.LoginRequest;
import com.example.iotmonitoring.dto.LoginResponse;
import com.example.iotmonitoring.entity.User;
import com.example.iotmonitoring.repository.UserRepository;
import jakarta.servlet.http.HttpSession;


@RestController
@RequestMapping("/api/auth")
@CrossOrigin
public class AuthController {

    private final UserRepository users;
    private final BCryptPasswordEncoder encoder;

    public AuthController(
            UserRepository users,
            BCryptPasswordEncoder encoder
    ) {
        this.users = users;
        this.encoder = encoder;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request, HttpSession session) {
        User user = users.findByUsername(request.getUsername())
                .orElseThrow(() ->
                        new IllegalArgumentException("Username hoặc password không đúng")
                );

        if (!encoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Username hoặc password không đúng");
        }

        session.setAttribute("user", user);
        session.setAttribute("username", user.getUsername());
        session.setAttribute("role", user.getRole());

        return new LoginResponse(
                user.getId(),
                user.getUsername(),
                user.getRole()
        );
    }

    @PostMapping("/logout")
    public Map<String, String> logout(HttpSession session) {
        session.invalidate();
        return Map.of("message", "Logout successful");
    }
}
