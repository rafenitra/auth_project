package com.authbackend.authback.config;

import org.springframework.stereotype.Component;

import com.authbackend.authback.entity.Role;
import com.authbackend.authback.repository.RoleRepository;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DataInitializer {

    private final RoleRepository roleRepository;

    //initialisation de la table rôles avec les rôles USER et ADMIN arpès le démarrage de l'application
    @PostConstruct
    public void init(){
        if(roleRepository.findByName("USER").isEmpty()){
            Role role = Role.builder()
                    .name("USER")
                    .build();
            roleRepository.save(role);
        }

        if(roleRepository.findByName("ADMIN").isEmpty()){
            Role role = Role.builder()
                    .name("ADMIN")
                    .build();
            roleRepository.save(role);
        }
    }
}
