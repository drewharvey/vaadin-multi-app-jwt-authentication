/*
 * Copyright (c) 2025.  Ohio Department of Education. - All Rights Reserved.
 * Unauthorized copying of this file, in any medium, is strictly prohibited.
 * Written by the State Software Development Team (http://ssdt.oecn.k12.oh.us/)
 *
 */

package com.example.application.security;

import com.vaadin.spring.annotation.SpringComponent;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

/**
 * Service for handling JWT authentication.
 */
@SpringComponent
public class JwtAuthenticationService
{
    private final JwtProperties jwtProperties;

    public JwtAuthenticationService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    public JwtProperties getJwtProperties() {
        return jwtProperties;
    }

    /**
     * Get the auth token from the request if it exists.
     * @param request
     * @return
     */
    public Optional<String> getAuthTokenFromRequest(HttpServletRequest request) {
        Cookie cookie = CookieUtils.findCookieByName(request, jwtProperties.getAuthTokenName());
        String token = cookie != null ? cookie.getValue() : null;
        return Optional.ofNullable(token);
    }

    /**
     * Get the refresh token from the request if it exists.
     * @param request
     * @return
     */
    public Optional<String> getRefreshTokenFromRequest(HttpServletRequest request) {
        Cookie cookie = CookieUtils.findCookieByName(request, jwtProperties.getRefreshTokenName());
        String token = cookie != null ? cookie.getValue() : null;
        return Optional.ofNullable(token);
    }

    /**
     * Generate a new auth token and set it as a cookie in the response.
     * @param authentication
     * @param response
     * @return the new auth token
     */
    public String generateAndSetAuthTokenCookie(Authentication authentication, HttpServletResponse response) {
        String authToken = generateAuthToken(authentication);
        ResponseCookie authTokenCookie = generateAuthTokenCookie(authToken);
        response.addHeader("Set-Cookie", authTokenCookie.toString());
        return authToken;
    }

    /**
     * Generate a new auth token and set it as a cookie in the response.
     * @param username
     * @param response
     * @return the new auth token
     */
    public String generateAndSetAuthTokenCookie(String username, HttpServletResponse response) {
        String authToken = generateAuthToken(username);
        ResponseCookie authTokenCookie = generateAuthTokenCookie(authToken);
        response.addHeader("Set-Cookie", authTokenCookie.toString());
        return authToken;
    }

    /**
     * Generate a new refresh token and set it as a cookie in the response.
     * @param authentication
     * @param response
     * @return the new refresh token
     */
    public String generateAndSetRefreshTokenCookie(Authentication authentication, HttpServletResponse response) {
        return generateAndSetRefreshTokenCookie(authentication.getName(), response);
    }

    /**
     * Generate a new refresh token and set it as a cookie in the response.
     * @param response
     * @return the new refresh token
     */
    public String generateAndSetRefreshTokenCookie(String username, HttpServletResponse response) {
        String refreshToken = generateRefreshToken(username);
        ResponseCookie refreshTokenCookie = generateRefreshTokenCookie(refreshToken);
        response.addHeader("Set-Cookie", refreshTokenCookie.toString());
        return refreshToken;
    }

    public ResponseCookie generateAuthTokenCookie(String authToken) {
        return CookieUtils.createSecureResponseCookie(
                jwtProperties.getAuthTokenName(),
                jwtProperties.getCookiePath(),
                authToken);
    }

    public ResponseCookie generateRefreshTokenCookie(String refreshToken) {
        return CookieUtils.createSecureResponseCookie(
                jwtProperties.getRefreshTokenName(),
                jwtProperties.getCookiePath(),
                refreshToken);
    }

    /**
     * Generate an JWT auth token. This token is used to authenticate the current user.
     * @param authentication
     * @return
     */
    public String generateAuthToken(Authentication authentication) {
        return generateAuthToken(authentication.getName());
    }

    /**
     * Generate an JWT auth token. This token is used to authenticate the current user.
     * @param username
     * @return
     */
    public String generateAuthToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtProperties.getAuthTokenExpirationInMilliseconds()))
                .signWith(SignatureAlgorithm.HS256, getDecodedAuthSecret())
                .compact();
    }

    /**
     * Generate JWT refresh token.  This token is used to refresh an expired auth token.
     * @param authentication
     * @return
     */
    public String generateRefreshToken(Authentication authentication) {
        return generateRefreshToken(authentication.getName());
    }

    /**
     * Generate JWT refresh token.  This token is used to refresh an expired auth token.
     * @param username
     * @return
     */
    public String generateRefreshToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtProperties.getRefreshTokenExpirationInMilliseconds()))
                .signWith(SignatureAlgorithm.HS256, getDecodedRefreshSecret())
                .compact();
    }

    /**
     * Checks if an auth token is valid. A token will be considered invalid if it is expired. Use
     * {@link #isAuthTokenExpired(String)} if you want to check.
     * @param token
     * @return
     */
    public boolean validateAuthToken(String token) {
        return JwtUtils.validateToken(token, getDecodedAuthSecret());
    }

    /**
     * Checks if a JWT token is valid. A token will be considered invalid if it is expired. Use
     * {@link #isAuthTokenExpired(String)} if you want to check.
     * @param token
     * @return
     */
    public boolean validateRefreshToken(String token) {
        return JwtUtils.validateToken(token, getDecodedRefreshSecret());
    }

    /**
     * Checks if an auth token is expired.
     * @param token
     * @return
     */
    public boolean isAuthTokenExpired(String token) {
        return JwtUtils.isTokenExpired(token, getDecodedAuthSecret());
    }

    /**
     * Checks if a refresh token is expired.
     * @param token
     * @return
     */
    public boolean isRefreshTokenExpired(String token) {
        return JwtUtils.isTokenExpired(token, getDecodedRefreshSecret());
    }

    /**
     * Gets username from token claims. Username is still returned if token is expired.
     * @param token
     * @return
     */
    public String getUsernameFromAuthToken(String token) {
        return getUsernameFromToken(token, getDecodedAuthSecret());
    }

    /**
     * Gets username from token claims. Username is still returned if token is expired.
     * @param token
     * @return
     */
    public String getUsernameFromRefreshToken(String token) {
        return getUsernameFromToken(token, getDecodedRefreshSecret());
    }

    public String getUsernameFromToken(String token, byte[] decodedSecret) {
        Claims claims;
        try {
            claims = JwtUtils.parseClaims(token, decodedSecret).getBody();
        } catch (ExpiredJwtException e) {
            claims = e.getClaims();
        }
        return getUsernameFromClaims(claims);
    }

    private String getUsernameFromClaims(Claims claims) {
        return claims.getSubject();
    }

    // todo: probably don't need
    public List<GrantedAuthority> getAuthoritiesFromAuthToken(String token) {
        Claims claims;
        try {
            claims = parseAuthClaims(token).getBody();
        } catch (ExpiredJwtException e) {
            claims = e.getClaims();
        }

        ArrayList<HashMap<String, String>> authMapList = (ArrayList<HashMap<String, String>>) claims.get("authorities");
        String[] authNameList = authMapList.stream()
                .flatMap(authMap -> authMap.values().stream())
                .toArray(String[]::new);
        return AuthorityUtils.createAuthorityList(authNameList);
    }

    private Jws<Claims> parseAuthClaims(String token) {
        return JwtUtils.parseClaims(token, getDecodedAuthSecret());
    }

    private Jws<Claims> parseRefreshClaims(String token) {
        return JwtUtils.parseClaims(token, getDecodedRefreshSecret());
    }

    /**
     * Returns true if the auth token can be parsed with the secret key. This DOES NOT mean the token
     * is validated, just that it is readable with the secret key.
     * @param token
     * @return
     */
    public boolean isAuthTokenParsable(String token) {
        return JwtUtils.isParsable(token, getDecodedAuthSecret());
    }

    /**
     * Returns true if the refresh token can be parsed with the secret key. This DOES NOT mean the token
     * is validated, just that it is readable with the secret key.
     * @param token
     * @return
     */
    public boolean isRefreshTokenParsable(String token) {
        return JwtUtils.isParsable(token, getDecodedRefreshSecret());
    }

    /**
     * Remove the JWT auth/refresh cookies from the response.
     * @param request
     * @param response
     */
    public void removeJwtCookies(HttpServletRequest request, HttpServletResponse response) {
        removeCookie(request, response, jwtProperties.getAuthTokenName());
        removeCookie(request, response, jwtProperties.getRefreshTokenName());
    }

    private void removeCookie(HttpServletRequest request, HttpServletResponse response, String cookieName) {
        ResponseCookie deleteCookie = CookieUtils.createSecureResponseCookie(cookieName, jwtProperties.getCookiePath(), "", 0);
        response.addHeader("Set-Cookie", deleteCookie.toString());
    }

    private byte[] getDecodedAuthSecret() {
        return JwtUtils.decode(jwtProperties.getAuthTokenSecret());
    }

    private byte[] getDecodedRefreshSecret() {
        return JwtUtils.decode(jwtProperties.getRefreshTokenSecret());
    }
}