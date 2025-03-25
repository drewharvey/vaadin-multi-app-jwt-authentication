package com.example.application.security;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinServlet;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.server.VaadinServletResponse;
import com.vaadin.flow.spring.annotation.SpringComponent;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import static com.example.application.security.JwtDevLogger.log;

@SpringComponent
public class SecurityService {

    private static final String LOGOUT_SUCCESS_URL = "/";

    private final JwtAuthenticationService jwtAuthenticationService;

    public SecurityService(JwtAuthenticationService jwtAuthenticationService) {
        this.jwtAuthenticationService = jwtAuthenticationService;
    }

    public void logout() {
        if (VaadinServletRequest.getCurrent() == null) {
            log("Cannot logout because VaadinServletRequest.getCurrent() is returning null");
            return;
        }

        if (VaadinServletResponse.getCurrent() == null) {
            log("Cannot logout because VaadinServletResponse.getCurrent() is returning null");
            return;
        }

        var request = VaadinServletRequest.getCurrent().getHttpServletRequest();
        var response = VaadinServletResponse.getCurrent().getHttpServletResponse();
        logout(request, response);
    }

    public void logout(HttpServletRequest request, HttpServletResponse response) {
        // schedule a redirect to the logout success page if we have an active UI
        if (UI.getCurrent() != null) {
            UI.getCurrent().getPage().setLocation(VaadinServlet.getCurrent().getServletContext() + LOGOUT_SUCCESS_URL);
        }

        // execute the logout
        log("Executing JWT + Spring logout...");
        var logoutHandler = new JwtLogoutHandler(jwtAuthenticationService);
        logoutHandler.logout(request, response, SecurityContextHolder.getContext().getAuthentication());
        log("Finished logout");
    }
}
