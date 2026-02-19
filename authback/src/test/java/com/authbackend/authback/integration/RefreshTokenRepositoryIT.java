package com.authbackend.authback.integration;

import com.authbackend.authback.entity.RefreshToken;
import com.authbackend.authback.entity.User;
import com.authbackend.authback.repository.RefreshTokenRepository;
import com.authbackend.authback.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
public class RefreshTokenRepositoryIT {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void saveAndFindByToken_returnRefreshToken(){
        User user = new User();
        user.setEmail("test@test.com");
        user.setUsername("Test_User");
        user.setPassword("password");
        User userSaved = userRepository.save(user);


        // créer un refresh token
        RefreshToken token = new RefreshToken();
        token.setToken("abc123");
        token.setUser(userSaved);
        token.setExpireDate(Instant.now().plusSeconds(3600));

        refreshTokenRepository.save(token);

        Optional<RefreshToken> result = refreshTokenRepository.findByToken("abc123");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("abc123", result.get().getToken());
        assertEquals("test@test.com", result.get().getUser().getEmail());

    }
}
