package com.example.TT.Backend.service;

import com.example.TT.Backend.dto.AuthRequest;
import com.example.TT.Backend.dto.AuthResponse;
import com.example.TT.Backend.dto.RegisterRequest;
import com.example.TT.Backend.model.User;
import com.example.TT.Backend.repository.UserRepository;
import com.example.TT.Backend.security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    public AuthResponse register(RegisterRequest request) {
        if (request.getUsername() == null || request.getUsername().isBlank()) {
            throw new IllegalArgumentException("Username is required.");
        }

        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email is required.");
        }

        if (request.getPassword() == null || request.getPassword().length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters.");
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists.");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists.");
        }

        User user = new User(
            request.getUsername().trim(),
            request.getEmail().trim(),
            passwordEncoder.encode(request.getPassword()),
            new BigDecimal("50000.00")
        );

        userRepository.save(user);
        String token = jwtUtil.generateToken(
            org.springframework.security.core.userdetails.User.withUsername(user.getUsername())
                .password(user.getPasswordHash())
                .authorities("USER")
                .build()
        );

        return new AuthResponse(token, user.getId(), user.getUsername(), user.getEmail(), user.getBalance());
    }

    public AuthResponse login(AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        org.springframework.security.core.userdetails.User principal =
            (org.springframework.security.core.userdetails.User) authentication.getPrincipal();

        User user = userRepository.findByUsername(principal.getUsername())
            .orElseThrow(() -> new IllegalArgumentException("User not found."));

        String token = jwtUtil.generateToken(principal);
        return new AuthResponse(token, user.getId(), user.getUsername(), user.getEmail(), user.getBalance());
    }
}
