package com.authbackend.authback.dto;

public record AuthResponse(String accessToken, String refreshToken) {
}
