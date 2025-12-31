package com.alfarays.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


@Component
public class JwtTokenService {

    @Value("${application.security.token.secret_key}")
    private String SECRET_KEY;

    @Value("${application.security.token.expiration}")
    private long EXPIRATION_TIME;

    public String generateToken(final UserDetails userDetails, Map<String, Object> claims) {
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + (EXPIRATION_TIME * 1000)))
                .issuer("Alfarays Tech Solutions Pvt. Ltd.")
                .audience().add("Alfarays APIs.")
                .and()
                .claim("user", claims)
                .claim("authorities",
                        userDetails.getAuthorities()
                                .stream()
                                .map(GrantedAuthority::getAuthority)
                                .collect(Collectors.toSet()))
                .signWith(signKey())
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaims(token, Claims::getSubject);
    }

    public <T> T extractClaims(String token, Function<Claims, T> claimsResolver) {
        return claimsResolver.apply(
                Jwts.parser()
                        .verifyWith(signKey())
                        .build()
                        .parseSignedClaims(token)
                        .getPayload()
        );
    }

    public boolean isTokenValid(String token, String username) {
        return username.equals(extractUsername(token)) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractClaims(token, Claims::getExpiration).before(new Date());
    }

    private SecretKey signKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET_KEY));
    }

}
