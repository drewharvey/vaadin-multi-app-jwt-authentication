package com.example.application.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.example.application.security.JwtDevLogger.log;

/**
 * This is where the JWT tokens are generated after a user successfully logs in.
 */
public class JwtAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private final JwtAuthenticationService jwtAuthenticationService;

    public JwtAuthenticationSuccessHandler(JwtAuthenticationService jwtAuthenticationService) {
        this.jwtAuthenticationService = jwtAuthenticationService;
        setDefaultTargetUrl("/");
        setAlwaysUseDefaultTargetUrl(true);
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException
    {

        log("Basic auth success - attempting to create auth tokens");

        // todo: do we want max age on cookies?
        // - we want JWT cookies to stick around while the user is logged in, even if they expire
        // - JWTs have their own internal expiration dates

        // generate the JWT token using details from the Authentication object.
        jwtAuthenticationService.generateAndSetAuthTokenCookie(authentication, response);
        log("Auth token created");

        // generate the refresh token
        jwtAuthenticationService.generateAndSetRefreshTokenCookie(authentication, response);
        log("Refresh token created");

        super.onAuthenticationSuccess(request, response, authentication);
    }

}
