package com.authbackend.authback.dto;

import java.util.List;

public record MeResponse(Long id, String username, String email, List<String> roles) {
}
