package com.example.TT.Backend.dto;

import java.math.BigDecimal;

public class AuthResponse {
    private String token;
    private Long userId;
    private String username;
    private String email;
    private BigDecimal balance;

    public AuthResponse() {
    }

    public AuthResponse(String token, Long userId, String username, String email, BigDecimal balance) {
        this.token = token;
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.balance = balance;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
}
