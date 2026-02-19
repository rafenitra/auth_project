package com.authbackend.authback.integration;

import com.authbackend.authback.dto.AuthResponse;
import com.authbackend.authback.dto.RefreshRequest;
import com.authbackend.authback.entity.RefreshToken;
import com.authbackend.authback.entity.User;
import com.authbackend.authback.repository.RefreshTokenRepository;
import com.authbackend.authback.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
public class AuthControllerIT extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void register_createAndReturnMessage() throws  Exception{
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                            {
                                "username" : "Test_User",
                                "email" : "test@gmail.com",
                                "password" : "pass123"
                            }
                        """
                        ))
                .andExpect(status().isOk())
                .andExpect(content().string("Utilisateur enregistré avec succès"));
        assertTrue(userRepository.findByEmail("test@gmail.com").isPresent());
    }

    @Test
    void login_returnAccessandRefreshToken() throws Exception{
        User user = new User();
        user.setEmail("test@test.com");
        user.setUsername("Test_Username");
        user.setPassword(passwordEncoder.encode("pass123")); // mot de passe hashé
        userRepository.save(user);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "email": "test@test.com",
                            "password": "pass123"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists());
    }

    @Test
    void refresh_returnAuthResponseWIthNewTokens() throws Exception{
        // Arrange : créer un user + refresh token
        User user = new User();
        user.setEmail("test@test.com");
        user.setUsername("Test_Username");
        user.setPassword("pass");
        user = userRepository.save(user);

        RefreshToken oldToken = new RefreshToken();
        oldToken.setToken("old-token");
        oldToken.setUser(user);
        oldToken.setExpireDate(Instant.now().plusSeconds(3600));
        refreshTokenRepository.save(oldToken);

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "refreshToken": "old-token"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists());
        assertNotEquals("old-token", refreshTokenRepository.findAll().get(0).getToken());

    }

}
