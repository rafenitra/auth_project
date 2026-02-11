package com.authbackend.authback.mapper;

import com.authbackend.authback.dto.MeResponse;
import com.authbackend.authback.entity.User;

public class UserMapper {
    public static MeResponse toMeResponse(User user){
        var roles = user.getRoles().stream()
                .map(role -> role.getName())
                .toList();
        return  new MeResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                roles
        );
    }
}
