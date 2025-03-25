package com.example.application.security;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseCookie;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

/**
 * Cookie-related utilities.
 */
public class CookieUtils
{

    /**
     * Find a cookie by name. Returns null if no matching cookie is found.
     * @param request
     * @param cookieName
     * @return
     */
    public static Cookie findCookieByName(HttpServletRequest request, String cookieName) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (cookieName.equals(cookie.getName())) {
                    return cookie;
                }
            }
        }
        return null;
    }

    /**
     * Create a cookie that can be set via 'Set-Cookie' response header.
     * @param name      cookie name
     * @param contents  cookie contents
     * @return
     */
    public static ResponseCookie createSecureResponseCookie(String name, String path, String contents) {
        return ResponseCookie.from(name, contents)
                .httpOnly(true)
                .secure(true)
                .path(StringUtils.defaultString(path, "/"))
                .domain("localhost")
                // .maxAge(jwtUtils.getJwtProperties().getRefreshTokenExpiration())
                .build();
    }

    /**
     * Create a cookie that can be set via 'Set-Cookie' response header.
     * @param name      cookie name
     * @param contents  cookie contents
     * @param maxAge    max age in milliseconds
     * @return
     */
    public static ResponseCookie createSecureResponseCookie(String name, String path, String contents, long maxAge) {
        return ResponseCookie.from(name, contents)
                .httpOnly(true)
                .secure(true)
                .path(StringUtils.defaultString(path, "/"))
                .domain("localhost")
                .maxAge(maxAge)
                .build();
    }

}
