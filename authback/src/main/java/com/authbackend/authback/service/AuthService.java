package com.authbackend.authback.service;

import java.util.Set;

import com.authbackend.authback.dto.*;
import com.authbackend.authback.entity.RefreshToken;
import com.authbackend.authback.repository.RefreshTokenRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.authbackend.authback.entity.Role;
import com.authbackend.authback.entity.User;
import com.authbackend.authback.exception.EmailAlreadyUsedException;
import com.authbackend.authback.exception.InvalidPasswordException;
import com.authbackend.authback.exception.EmailNotFoundException;
import com.authbackend.authback.repository.RoleRepository;
import com.authbackend.authback.repository.UserRepository;


import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;


    //Méthode pour la création de compte
    public String register(RegisterRequest registerRequest){
        if(userRepository.existsByEmail(registerRequest.email())){
            throw new EmailAlreadyUsedException("Email déjà utilisé");
        }

        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new RuntimeException("Role USER non trouvé"));
        User user = User.builder()
                .username(registerRequest.username())
                .email(registerRequest.email())
                .password(passwordEncoder.encode(registerRequest.password()))
                .roles(Set.of(userRole))
                .build();
        userRepository.save(user);
        return "Utilisateur enregistré avec succès";
    }

    //Méthode pour la connexion 
    public AuthResponse login(LoginRequest loginRequest){
        User user = userRepository.findByEmail(loginRequest.email())
                .orElseThrow(() -> new EmailNotFoundException("Email non trouvé"));
        if(!passwordEncoder.matches(loginRequest.password() , user.getPassword())){
            throw new InvalidPasswordException("Mot de passe incorrect");
        }

        //générer l'access token
        String accessToken = jwtService.generateToken(user.getEmail());

        //générer le refreshToken
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return new AuthResponse(accessToken, refreshToken.getToken());
    }

    //méthode de rafraîchissement des deux token
    public AuthResponse refreshToken(RefreshRequest refreshRequest){
        RefreshToken oldRefreshToken = refreshTokenService.validateRefreshToken(refreshRequest.refreshToken());

        User user = oldRefreshToken.getUser();

        String newAccessToken = jwtService.generateToken(user.getEmail());

        RefreshToken newRefreshToken = refreshTokenService.rotateRefreshToken(oldRefreshToken);

        return new AuthResponse(newAccessToken, newRefreshToken.getToken());
    }

    //logout
    public void logout(LogoutRequest logoutRequest){
        //vérification que c'est encore valide
        RefreshToken refreshToken = refreshTokenService.validateRefreshToken(logoutRequest.refreshToken());
        refreshTokenService.revokeToken(refreshToken);
    }
}
