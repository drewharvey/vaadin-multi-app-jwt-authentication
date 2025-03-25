package com.example.application.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Contains JWT authentication related properties.
 */
@Configuration
public class JwtProperties {

    @Value("${jwt.auth.name}")
    private String authTokenName;

    @Value("${jwt.refresh.name}")
    private String refreshTokenName;

    @Value("${jwt.auth.expiration}")
    private int authTokenExpiration;

    @Value("${jwt.refresh.expiration}")
    private int refreshTokenExpiration;

    @Value("${jwt.auth.secret}")
    private String authTokenSecret;

    @Value("${jwt.refresh.secret}")
    private String refreshTokenSecret;

    @Value("${jwt.cookie.path}")
    private String cookiePath;

    public String getAuthTokenName() {
        return authTokenName;
    }

    public String getRefreshTokenName() {
        return refreshTokenName;
    }

    /**
     * Auth token expiration in minutes.
     * @return
     */
    public int getAuthTokenExpiration() {
        return authTokenExpiration;
    }

    public long getAuthTokenExpirationInMilliseconds() {
        return authTokenExpiration * 60 * 1000;
    }

    /**
     * Refresh token expiration in minutes.
     * @return
     */
    public int getRefreshTokenExpiration() {
        return refreshTokenExpiration;
    }

    public int getRefreshTokenExpirationInMilliseconds() {
        return refreshTokenExpiration * 60 * 1000;
    }

    public String getAuthTokenSecret() {
        return authTokenSecret;
    }

    public String getRefreshTokenSecret() {
        return refreshTokenSecret;
    }

    public String getCookiePath() {
        return cookiePath;
    }
}