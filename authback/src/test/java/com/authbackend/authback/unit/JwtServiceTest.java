package com.authbackend.authback.unit;

import com.authbackend.authback.entity.User;
import com.authbackend.authback.service.JwtService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;


@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class JwtServiceTest {


    @InjectMocks
    private JwtService jwtService;

    @Test
    void generateToken_returnNotNullToken(){
        User user = new User();
        user.setEmail("test@gmail.com");

        String token = jwtService.generateToken(user.getEmail());
        assertNotNull(token);
    }


    @Test
    void extractMail_returnCorrectMail(){
        User user = User.builder().email("test@gmail.com").build();

        String token = jwtService.generateToken(user.getEmail());

        String mailExtracted = jwtService.extractEmail(token);

        assertEquals(user.getEmail(),mailExtracted);
    }

    @Test
    void isTokenValid_returnTrue(){
        User user = User.builder().email("test@gmail.com").build();

        String token = jwtService.generateToken(user.getEmail());

        boolean isValid = jwtService.isTokenValid(token, user.getEmail());

        assertTrue(isValid);
    }

}
