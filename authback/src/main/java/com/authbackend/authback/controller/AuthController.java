package com.authbackend.authback.controller;

import com.authbackend.authback.dto.MeResponse;
import com.authbackend.authback.entity.User;
import com.authbackend.authback.mapper.UserMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.authbackend.authback.dto.LoginRequest;
import com.authbackend.authback.dto.AuthResponse;
import com.authbackend.authback.dto.RegisterRequest;
import com.authbackend.authback.service.AuthService;

import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    //appelé pour la création de compte
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest registerRequest) {
        String message = authService.register(registerRequest);
        return ResponseEntity.ok(message);
    }

    //appelé pour la connexion 
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login (@RequestBody LoginRequest loginRequest){
        AuthResponse response = authService.login(loginRequest);
        return ResponseEntity.ok(response);
    }

    //Pour avoir l'information de celui qui est connecté
    @GetMapping("/me")
    public ResponseEntity<MeResponse> me(){
        var auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();
        return ResponseEntity.ok(UserMapper.toMeResponse(user));
    }

}
