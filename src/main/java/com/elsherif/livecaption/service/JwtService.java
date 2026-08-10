package com.elsherif.livecaption.service;

import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
public class JwtService {
    @Value("${jwt.secret}")
    private String secretKey;
    @Value("${jwt.expiration}")
    private long jwtExpiration;

    public String extractUsername(String token) {
        return extractClaim(token , Claims::getSubject);
    }
    public <T> T extractClaim(String token, Function<Claims, T> function) {
        final Claims claims = extractAllClaims(token);


    }
}
