package com.example.application.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;

import java.util.Base64;

/**
 * Utility class for generic JWT token operations.
 */
public class JwtUtils {

    public static boolean validateToken(String token, byte[] decodedSecret) {
        if (token == null)
            return false;

        try {
            JwtUtils.parseClaims(token, decodedSecret);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    public static boolean isTokenExpired(String token, byte[] decodedSecret) {
        try {
            JwtUtils.parseClaims(token, decodedSecret);
            return false;
        } catch (ExpiredJwtException e) {
            return true;
        }
    }

    public static boolean isParsable(String token, byte[] decodedSecret) {
        try {
            parseClaims(token, decodedSecret);
            return true;
        } catch (ExpiredJwtException expiredJwtException) {
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static Jws<Claims> parseClaims(String token, byte[] decodedSecret) {
        return Jwts.parserBuilder()
                .setSigningKey(decodedSecret)
                .build()
                .parseClaimsJws(token);
    }

    public static byte[] decode(String encodedSecret) {
        return Base64.getDecoder().decode(encodedSecret);
    }
}