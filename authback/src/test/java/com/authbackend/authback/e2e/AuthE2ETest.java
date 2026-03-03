package com.authbackend.authback.e2e;

import com.authbackend.authback.dto.AuthResponse;
import com.authbackend.authback.repository.RefreshTokenRepository;
import com.authbackend.authback.repository.UserRepository;
import com.authbackend.authback.service.JwtService;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
@ActiveProfiles("test")
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
public class AuthE2ETest {
    @LocalServerPort
    private int port;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private JwtService jwtService;

    private String accessToken;
    private String refreshToken;

    @BeforeEach
    void setup(){
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
    }

    @Test
    void fullAuthentificationFlow(){
        //Register
        given()
                .contentType("application/json")
                .body("""
                        {
                            "email": "ary@gmail.com",
                            "password": "pass123",
                            "username": "Ary"
                        }
                        """)
                .when()
                    .post("/auth/register")
                .then()
                .statusCode(200);
        assertTrue(userRepository.findByEmail("ary@gmail.com").isPresent());

        //Login
        String refresTokenJson = given()
                .contentType("application/json")
                .body("""
                        {
                            "email": "ary@gmail.com",
                            "password": "pass123"
                        }
                        """)
                .when()
                .post("/auth/login")
                .then()
                .statusCode(200)
                .body("accessToken",notNullValue())
                .body("refreshToken", notNullValue())
                .extract().asString();

        ObjectMapper objectMapper = new ObjectMapper();
        AuthResponse authResponse = objectMapper.readValue(refresTokenJson, AuthResponse.class);

        accessToken = authResponse.accessToken();
        refreshToken = authResponse.refreshToken();

        assertEquals(1,refreshTokenRepository.count());

        System.out.println("**********************************************");

        //Me avec JWT
        given()
                .header("Authorization", "Bearer " + accessToken)
                .log().all()
                .when()
                .get("/auth/me")
                .then()
                .log().all()
                .statusCode(200)
                .body("email", equalTo("ary@gmail.com"));

        //Refresh
        String refreshJson =
                given()
                        .contentType("application/json")
                        .body("""
                                {
                                    "refreshToken" : "%s"
                                }
                                """.formatted(refreshToken))
                        .when()
                        .post("/auth/refresh")
                        .then()
                        .statusCode(200)
                        .body("accessToken", notNullValue())
                        .body("refreshToken", notNullValue())
                        .extract().asString();
        AuthResponse refreshResponse = objectMapper.readValue(refreshJson, AuthResponse.class);

        assertNotEquals(refreshToken,refreshResponse.refreshToken());

        //LogOut
        given()
                .contentType("application/json")
                .body("""
                        {
                            "refreshToken" : "%s"
                        }
                        """.formatted(refreshResponse.refreshToken()))
                .header("Authorization", "Bearer "+ refreshResponse.accessToken())
                .when()
                .post("/auth/logout")
                .then()
                .statusCode(200)
                .body(equalTo("Vous êtes déconnecté"));
    }

}
