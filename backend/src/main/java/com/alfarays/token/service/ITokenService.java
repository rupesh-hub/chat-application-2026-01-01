package com.alfarays.token.service;

import com.alfarays.token.enums.DurationUnit;
import com.alfarays.token.enums.TokenType;

public interface ITokenService {

    String create(String username, TokenType type, int duration, DurationUnit unit);

    void validate(String username, String token, TokenType type);

    void invalidate(String username, String token, TokenType type);

}
