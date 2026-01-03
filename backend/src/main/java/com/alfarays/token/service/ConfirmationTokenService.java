package com.alfarays.token.service;


import com.alfarays.exceptions.AuthorizationException;
import com.alfarays.token.entity.Token;
import com.alfarays.token.enums.DurationUnit;
import com.alfarays.token.enums.TokenType;
import com.alfarays.token.repository.TokenRepository;
import com.alfarays.token.util.TokenGenerator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConfirmationTokenService implements ITokenService {
    private final TokenRepository tokenRepository;

    @Transactional
    @Override
    public String create(String username, TokenType type, int duration, DurationUnit unit) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expireAt = "SECOND".equals(unit.name()) ? now.plusSeconds(duration)
                : "MINUTE".equals(unit.name()) ? now.plusMinutes(duration)
                : "HOUR".equals(unit.name()) ? now.plusHours(duration)
                : now.plusDays(duration);

        var token = new Token();
        token.setToken(TokenGenerator.generate());
        token.setType(type);
        token.setExpiresAt(expireAt);
        token.setUsername(username);
        token = tokenRepository.save(token);
        return token.getToken();
    }

    @Override
    public void validate(String username, String value, TokenType type) {

        // Check if the token exists
        var token = tokenRepository.findToken(username, value, type)
                .orElseThrow(() -> new AuthorizationException("Invalid token !"));

        // Check for the expiration
        if(token.getExpiresAt().isBefore(LocalDateTime.now()))
            throw new AuthorizationException("Token is expired !");

    }

    @Transactional
    @Override
    public void invalidate(String username, String value, TokenType type) {
        // Check if the token exists
        var token = tokenRepository.findToken(username, value, type)
                .orElseThrow(() -> new AuthorizationException("Invalid token !"));

        tokenRepository.delete(token);
    }

}
