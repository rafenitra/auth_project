package com.authbackend.authback.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component
public class StartupLogger {
    private static final Logger logger = LoggerFactory.getLogger(StartupLogger.class);

    @Value("${websocket.allowed-origins}")
    private String allowedOrigin;

    @PostConstruct
    public void logStartupConfig() {
        logger.info("==================================================");
        logger.info("DÉMARRAGE DE L'APPLICATION");
        logger.info("Profil actif : {}",allowedOrigin );
        logger.info("==================================================");
    }
}
