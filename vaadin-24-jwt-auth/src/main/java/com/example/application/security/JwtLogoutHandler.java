package com.example.application.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

import static com.example.application.security.JwtDevLogger.log;

/**
 * This is where the JWT tokens are cleared after a user successfully logs out.
 */
public class JwtLogoutHandler extends SecurityContextLogoutHandler {

    private final JwtAuthenticationService jwtAuthenticationService;

    public JwtLogoutHandler(JwtAuthenticationService jwtAuthenticationService) {
        this.jwtAuthenticationService = jwtAuthenticationService;
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        log("JwtLogoutHandler::logout triggered via spring logout"); log("Clearing cookies...");
        jwtAuthenticationService.removeJwtCookies(request, response);
        log("Cookies cleared");

        // call spring default logout
        super.logout(request, response, authentication);
    }
}
