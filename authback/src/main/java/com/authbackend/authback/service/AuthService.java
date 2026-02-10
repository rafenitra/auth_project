package com.authbackend.authback.service;

import java.util.Set;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.authbackend.authback.dto.LoginRequest;
import com.authbackend.authback.dto.AuthResponse;
import com.authbackend.authback.dto.RegisterRequest;
import com.authbackend.authback.entity.Role;
import com.authbackend.authback.entity.User;
import com.authbackend.authback.repository.RoleRepository;
import com.authbackend.authback.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;


    //Méthode pour la création de compte
    public String register(RegisterRequest registerRequest){
        if(userRepository.existsByEmail(registerRequest.email())){
            throw new RuntimeException("Email déjà utilisé");
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
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        if(!passwordEncoder.matches(loginRequest.password() , user.getPassword())){
            throw new RuntimeException("Mot de passe incorrect");
        }
        
        //envoyer le token
        return new AuthResponse("TEMP_TOKEN");
    }
}
