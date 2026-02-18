package com.authbackend.authback.unit;

import com.authbackend.authback.dto.*;
import com.authbackend.authback.entity.RefreshToken;
import com.authbackend.authback.entity.Role;
import com.authbackend.authback.entity.User;
import com.authbackend.authback.repository.RefreshTokenRepository;
import com.authbackend.authback.repository.RoleRepository;
import com.authbackend.authback.repository.UserRepository;
import com.authbackend.authback.service.AuthService;
import com.authbackend.authback.service.JwtService;
import com.authbackend.authback.service.RefreshTokenService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.sql.Ref;
import java.util.Optional;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_returnStringOfSuccess(){
        RegisterRequest registerRequest = new RegisterRequest("Test","test@gmail.com","pass123");
        User user = User.builder()
                .username("Test")
                .email("test@gmail.com")
                .build();
        Role role = Role.builder().name("USER").build();

        Mockito.when(userRepository.existsByEmail(registerRequest.email())).thenReturn(false);
        Mockito.when(roleRepository.findByName("USER")).thenReturn(Optional.of(role));
        Mockito.when(userRepository.save(Mockito.any(User.class))).thenReturn(user);
        Mockito.when(passwordEncoder.encode("pass123")).thenReturn("pass123_encoded");

        String response = authService.register(registerRequest);

        Assertions.assertThat(response).isNotNull().isEqualTo("Utilisateur enregistré avec succès");
    }

    @Test
    void login_returnAuthResponse(){
        User user = User.builder()
                .username("Test")
                .email("test@gmail.com")
                .password("pass123_encoded")
                .build();

        RefreshToken refreshToken = RefreshToken.builder().token("refresh_token_created").user(user).revoked(false).build();
        LoginRequest loginRequest = new LoginRequest("test@gmail.com","pass123");

        Mockito.when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        Mockito.when(passwordEncoder.matches(loginRequest.password(),user.getPassword())).thenReturn(true);
        Mockito.when(jwtService.generateToken(user.getEmail())).thenReturn("access_token_created");
        Mockito.when(refreshTokenService.createRefreshToken(user)).thenReturn(refreshToken);

        AuthResponse authResponse = authService.login(loginRequest);
    }

    @Test
    void refresshToken(){
        User user = User.builder()
                .username("Test")
                .email("test@gmail.com")
                .password("pass123_encoded")
                .build();

        RefreshToken refreshToken = RefreshToken.builder().token("refresh_token_created").user(user).revoked(false).build();
        RefreshRequest refreshRequest = new RefreshRequest("refresh_token_created");
        RefreshToken newrefreshToken = RefreshToken.builder().token("new_refresh_token_created").user(user).revoked(false).build();
        Mockito.when(refreshTokenService.validateRefreshToken(refreshRequest.refreshToken())).thenReturn(refreshToken);
        Mockito.when(jwtService.generateToken(user.getEmail())).thenReturn("new_accessToken_created");
        Mockito.when(refreshTokenService.rotateRefreshToken(refreshToken)).thenReturn(newrefreshToken);

        AuthResponse authResponse = authService.refreshToken(refreshRequest);

        Assertions.assertThat(authResponse.accessToken()).isNotNull().isEqualTo("new_accessToken_created");
        Assertions.assertThat(authResponse.refreshToken()).isNotNull().isEqualTo(newrefreshToken.getToken());
    }


    @Test
    void logout_returnRefreshTokenRevoked(){
        User user = User.builder()
                .username("Test")
                .email("test@gmail.com")
                .password("pass123_encoded")
                .build();

        RefreshToken refreshToken = RefreshToken.builder().token("refresh_token_created").user(user).revoked(false).build();

        LogoutRequest logoutRequest = new LogoutRequest(refreshToken.getToken());

        Mockito.when(refreshTokenService.validateRefreshToken(logoutRequest.refreshToken())).thenReturn(refreshToken);

        authService.logout(logoutRequest);

        verify(refreshTokenService,times(1)).validateRefreshToken(logoutRequest.refreshToken());
        verify(refreshTokenService,times(1)).revokeToken(refreshToken);
    }

}
