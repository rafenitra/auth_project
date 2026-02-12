package com.authbackend.authback.service;

import com.authbackend.authback.entity.RefreshToken;
import com.authbackend.authback.entity.User;
import com.authbackend.authback.exception.TokenExpiredException;
import com.authbackend.authback.exception.TokenNotFoundException;
import com.authbackend.authback.exception.TokenRevokedException;
import com.authbackend.authback.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    //validité 2 min
    private final long refreshTokenDurationMs =  2 * 60 * 1000;

    public RefreshToken createRefreshToken(User user){
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expireDate(Instant.now().plusMillis(refreshTokenDurationMs))
                .revoked(false)
                .build();
        return refreshTokenRepository.save(refreshToken);
    }

    public RefreshToken validateRefreshToken(String token){
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new TokenNotFoundException("RefreshToken Introuvable"));
        if(refreshToken.isRevoked()){
            throw new TokenRevokedException("RefresToken revoqué");
        }

        if(refreshToken.getExpireDate().isBefore(Instant.now())){
            throw  new TokenExpiredException("RefreshToken expiré");
        }
        return  refreshToken;
    }

    public void revokeToken(RefreshToken refreshToken){
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);
    }

    public RefreshToken rotateRefreshToken(RefreshToken oldToken){
        //Révoquer l'ancien
        revokeToken(oldToken);

        //générer un nouveau
        return createRefreshToken(oldToken.getUser());

    }




}
