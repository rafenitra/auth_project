package com.authbackend.authback.unit;


import com.authbackend.authback.config.JwtAuthenticationFilter;
import com.authbackend.authback.config.SecurityConfig;
import com.authbackend.authback.controller.AuthController;
import com.authbackend.authback.dto.*;
import com.authbackend.authback.repository.UserRepository;
import com.authbackend.authback.service.AuthService;
import com.authbackend.authback.service.JwtService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;

import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.actuate.web.servlet.ManagementWebSecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;


import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;

import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(
        controllers = AuthController.class
)
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    void login_return200AndTokens() throws Exception {
        LoginRequest loginRequest = new LoginRequest("test@gmail.com","pass123");
        AuthResponse authResponse = new AuthResponse("access_token","refresh_token");

        Mockito.when(authService.login(loginRequest)).thenReturn(authResponse);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""    
                        {
                            "email":"test@gmail.com",
                            "password":"pass123"
                        }
                        """

                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access_token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh_token"));
    }

    @Test
    void register_returnIsOkAndMessage() throws Exception{
        RegisterRequest registerRequest = new RegisterRequest("Test","test@gmail.com","pass123");
        Mockito.when(authService.register(any(RegisterRequest.class))).thenReturn("Utilisateur enregistré avec succès");

        ObjectMapper objectMapper = new ObjectMapper();

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest))
        )
                .andExpect(status().isOk())
                .andExpect(content().string("Utilisateur enregistré avec succès"));
    }

    @Test
    void refreshToken_returnNewTokens() throws Exception {

        // Arrange
        AuthResponse response = new AuthResponse(
                "new-access-token",
                "new-refresh-token"
        );

        Mockito.when(authService.refreshToken(any(RefreshRequest.class)))
                .thenReturn(response);

        // Act + Assert
        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "refreshToken": "old-refresh-token"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access-token"))
                .andExpect(jsonPath("$.refreshToken").value("new-refresh-token"));
    }

    @Test
    void logout_shouldReturn200AndMessage() throws Exception {

        // Arrange
        Mockito.doNothing().when(authService).logout(any(LogoutRequest.class));

        // Act + Assert
        mockMvc.perform(post("/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "refreshToken": "some-refresh-token"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(content().string("Vous êtes déconnecté"));

        // Vérifie que le service a bien été appelé
        verify(authService, times(1)).logout(any(LogoutRequest.class));
    }
}

