package com.authbackend.authback.unit;


import com.authbackend.authback.entity.RefreshToken;
import com.authbackend.authback.entity.User;
import com.authbackend.authback.exception.TokenExpiredException;
import com.authbackend.authback.exception.TokenNotFoundException;
import com.authbackend.authback.exception.TokenRevokedException;
import com.authbackend.authback.repository.RefreshTokenRepository;
import com.authbackend.authback.repository.UserRepository;
import com.authbackend.authback.service.RefreshTokenService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;


@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    @Test
    void createRefreshToken_returnRefreshToken(){
        User user = User.builder().id(1L).username("MrTest").email("test@gmail.com").build();

        RefreshToken refreshToken = RefreshToken.builder().token(UUID.randomUUID().toString()).revoked(false).user(user).build();

        Mockito.when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(refreshToken);

        RefreshToken result = refreshTokenService.createRefreshToken(user);

        assertNotNull(result);
        assertEquals(user, result.getUser());
    }

    @Test
    void validateRefreshToken_returnNotNullToken_whenNotExpired(){
        User user = User.builder().id(1L).username("MrTest").email("test@gmail.com").build();

        RefreshToken refreshToken = RefreshToken.builder().token(UUID.randomUUID().toString()).revoked(false).user(user).build();
        refreshToken.setExpireDate(Instant.now().plusSeconds(3600));
        Mockito.when(refreshTokenRepository.findByToken(refreshToken.getToken())).thenReturn(Optional.of(refreshToken));

        RefreshToken result = refreshTokenService.validateRefreshToken(refreshToken.getToken());

        assertAll( () -> {
            assertNotNull(result);
            assertEquals(refreshToken,result);
            }
        );

    }

    @Test
    void validateRefreshToken_ThrowException_whenTokenExpired(){
        User user = User.builder().id(1L).username("MrTest").email("test@gmail.com").build();

        RefreshToken refreshToken = RefreshToken.builder().token(UUID.randomUUID().toString()).revoked(false).user(user).build();
        refreshToken.setExpireDate(Instant.now().minusSeconds(10));

        Mockito.when(refreshTokenRepository.findByToken(refreshToken.getToken())).thenReturn(Optional.of(refreshToken));

        assertThrows(TokenExpiredException.class, () -> {
            refreshTokenService.validateRefreshToken(refreshToken.getToken());
        });
    }

    @Test
    void validateRefreshToken_ThrowException_whenTokenIsRevoked(){
        User user = User.builder().id(1L).username("MrTest").email("test@gmail.com").build();

        RefreshToken refreshToken = RefreshToken.builder().token(UUID.randomUUID().toString()).revoked(true).user(user).build();

        Mockito.when(refreshTokenRepository.findByToken(refreshToken.getToken())).thenReturn(Optional.of(refreshToken));

        assertThrows(TokenRevokedException.class, () -> {
            refreshTokenService.validateRefreshToken(refreshToken.getToken());
        });
    }

    @Test
    void revokeToken_returnRefresTokenRevoked(){
        User user = User.builder().id(1L).username("MrTest").email("test@gmail.com").build();
        RefreshToken refreshToken = RefreshToken.builder().token(UUID.randomUUID().toString()).revoked(false).user(user).build();

        Mockito.when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(refreshToken);

        refreshTokenService.revokeToken(refreshToken);

        Assertions.assertThat(refreshToken.isRevoked()).isTrue();
    }

    @Test
    void rotateRefreshToken_returnNewToken(){
        User user = User.builder().id(1L).username("MrTest").email("test@gmail.com").build();
        RefreshToken refreshToken = RefreshToken.builder().token(UUID.randomUUID().toString()).revoked(false).user(user).build();
        RefreshToken refreshToken2 = RefreshToken.builder().token(UUID.randomUUID().toString()).revoked(false).user(user).build();

        Mockito.when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(refreshToken).thenReturn(refreshToken2);

        RefreshToken newRefreshToken = refreshTokenService.rotateRefreshToken(refreshToken);

        Assertions.assertThat(newRefreshToken).isNotNull().isNotEqualTo(refreshToken);
    }
}
