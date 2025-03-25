package com.example.application.security;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.stream.Collectors;

import static com.example.application.security.JwtDevLogger.log;

/**
 * Request filter that locates and verifies JWT token, which is used to authenticates user,
 * on every request.
 *
 * There are 3 main functions of this filter:
 * - automatically login a user, via spring security, if a valid auth jwt exists (if not already logged in)
 * - refresh the auth jwt if the refresh jwt is valid
 * - automatically trigger a "logout" if the auth/refresh jwts are invalid and/or does not exist
 *   - this includes clearing the jwts as well as clearing spring authentication
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final UserDetailsService userDetailsService;
    private final SecurityService securityService;
    private final JwtAuthenticationService jwtAuthenticationService;

    public JwtAuthenticationFilter(UserDetailsService userDetailsService,
                                   SecurityService securityService,
                                   JwtAuthenticationService jwtAuthenticationService) {
        this.userDetailsService = userDetailsService;
        this.securityService = securityService;
        this.jwtAuthenticationService = jwtAuthenticationService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // find the auth token
        log("Looking for auth token");
        String token = jwtAuthenticationService.getAuthTokenFromRequest(request).orElse(null);
        log("Auth token found = " + (token != null));

        // try the happy path first to optimize majority of requests (user is authenticated)
        if (jwtAuthenticationService.validateAuthToken(token) && tokenUserMatchesAuthenticatedUser(token)) {
            log("User already authenticated - exiting filter");
        } else if (jwtAuthenticationService.isAuthTokenParsable(token)) {
            // check if auth token is expired and we need to refresh it
            if (jwtAuthenticationService.isAuthTokenExpired(token)) {
                log("Auth token has expired - attempting to refresh token");
                token = refreshAuthToken(request, response, token);
                log("Refresh successful");
            }
            // continue if valid, otherwise clear security context
            if (jwtAuthenticationService.validateAuthToken(token)) {
                log("Auth token validated");
                log("Attempting to extract username from token");
                log("Username found (" + jwtAuthenticationService.getUsernameFromAuthToken(token) + ")");
                log("Updating spring security if needed");
                updateAuthentication(request, token);
            } else {
                log("Token is invalid - clearing current authentication (if any exists)");
                clearAuthentication(request, response);
            }
        } else {
            log("Token is invalid - clearing current authentication (if any exists)");
            clearAuthentication(request, response);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Clears current authentication if a current authenticated user exists.
     * @param request
     * @param response
     */
    private void clearAuthentication(HttpServletRequest request, HttpServletResponse response) {
        if (SecurityContextHolder.getContext() != null
                && SecurityContextHolder.getContext().getAuthentication() != null
                && SecurityContextHolder.getContext().getAuthentication().isAuthenticated()) {
            securityService.logout(request, response);
        }
    }

    /**
     * Updates current authentication with the details from the auth jwt.
     * @param request
     * @param authToken
     */
    private void updateAuthentication(HttpServletRequest request, String authToken) {
        // set the authenticated user if we need to
        if (!tokenUserMatchesAuthenticatedUser(authToken)) {
            log("Authentication needs to be updated");

            String username = jwtAuthenticationService.getUsernameFromAuthToken(authToken);
            UserDetails user = userDetailsService.loadUserByUsername(username);
            log("Authorities=" + user.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.joining(", ")));

            log("Attempting to set authentication via spring");
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(auth);
            log("Authentication set");
        } else {
            // authentication has already been set
            log("Authentication already set - skipping security context update");
        }
    }

    /**
     * Returns true if username in authentication and auth jwt match.
     * @param authToken
     * @return
     */
    private boolean tokenUserMatchesAuthenticatedUser(String authToken) {
        String username = jwtAuthenticationService.getUsernameFromAuthToken(authToken);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && StringUtils.equals(username, authentication.getName());
    }

    /**
     * Refreshes the auth token.
     * @param request
     * @param response
     * @param expiredAuthToken
     * @return the new auth token
     */
    private String refreshAuthToken(HttpServletRequest request, HttpServletResponse response, String expiredAuthToken) {
        String authToken = null;

        // find the refresh token
        log("Looking for refresh token");
        String refreshToken = jwtAuthenticationService.getRefreshTokenFromRequest(request).orElse(null);
        log("Refresh token found = " + (refreshToken != null));

        if (jwtAuthenticationService.validateRefreshToken(refreshToken)) {
            // regenerate auth token if usernames from both tokens match
            if (tokenUsernamesMatch(expiredAuthToken, refreshToken)) {
                String username = jwtAuthenticationService.getUsernameFromAuthToken(expiredAuthToken);

                // generate new refresh token
                log("Generating new refresh token");
                jwtAuthenticationService.generateAndSetRefreshTokenCookie(username, response);
                log("Refresh token generated");

                // generate new auth token
                log("Generating new auth token");
                authToken = jwtAuthenticationService.generateAndSetAuthTokenCookie(username, response);
                log("Auth token generated");
            } else {
                log("Auth and refresh token username values do not match - auth token will not be refreshed");
            }
        } else {
            log("Refresh token is not valid - auth token will not be refreshed");
        }

        return authToken;
    }

    /**
     * Returns true if the username in the auth and refresh tokens match.
     * @param authToken
     * @param refreshToken
     * @return
     */
    private boolean tokenUsernamesMatch(String authToken, String refreshToken) {
        log("Extracting username from both auth and refresh tokens to compare");

        String authUsername = jwtAuthenticationService.getUsernameFromAuthToken(authToken);
        String refreshUsername = jwtAuthenticationService.getUsernameFromRefreshToken(refreshToken);

        log("AuthTokenUsername=" + StringUtils.defaultString(authUsername) + " | refreshTokenUsername=" + StringUtils.defaultString(refreshUsername));

        return StringUtils.equals(authUsername, refreshUsername);
    }
}