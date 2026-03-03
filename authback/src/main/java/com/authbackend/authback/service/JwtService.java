package com.authbackend.authback.service;

import java.security.Key;
import java.util.Date;
import java.util.function.Function;

import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    //Clé secrète pour la signture du token
    @Value("${app.security.jwt-secret}")
    private String secret_key;

    public void setSecret_key(String secret_key) {
        this.secret_key = secret_key;
    }

    //générer la clé de signature utilisable par JJWT
    private Key getSigninKey(){
        return Keys.hmacShaKeyFor(secret_key.getBytes());
    }

    //extraction de mail du token
    public String extractEmail(String token){
        return extractClaim(token, Claims::getSubject);
    }

    //extraction du claim définissé dans le pramaètre de la méthode 
    public <T> T extractClaim(String token, Function <Claims, T> claimsResolver){
        Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // extraction de tous les claims de token 
    private Claims extractAllClaims(String token){
        return Jwts
            .parserBuilder()
            .setSigningKey(getSigninKey())
            .build()
            .parseClaimsJws(token)
            .getBody();
    }

    //création de token
    public String generateToken(String email){
        return Jwts
            .builder()
            .setSubject(email)
            .setIssuedAt(new Date(System.currentTimeMillis()))
            .setExpiration(new Date(System.currentTimeMillis() + 60 * 1000 * 1)) //1 minutes
            .signWith(getSigninKey(), SignatureAlgorithm.HS256)
            .compact();
    }

    //validation du token
    public boolean isTokenValid(String token, String email){
        String extractedMail = extractEmail(token);
        if(extractedMail.equals(email) && !isTokenExpired(token))
            return true;
        return false;
    }

    //expiration du token
    public boolean isTokenExpired(String token){
        try{
            Date expiration = extractClaim(token, Claims::getExpiration);
            return expiration.before(new Date());
        }
        catch (ExpiredJwtException e){
            return  true;
        }
    }

}
