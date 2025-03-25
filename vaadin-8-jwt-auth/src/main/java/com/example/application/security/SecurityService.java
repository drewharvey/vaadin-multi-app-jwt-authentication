package com.example.application.security;

import com.vaadin.server.VaadinServletRequest;
import com.vaadin.server.VaadinServletResponse;
import com.vaadin.spring.annotation.SpringComponent;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SpringComponent
public class SecurityService
{
    private JwtAuthenticationService jwtAuthenticationService;

    public SecurityService(JwtAuthenticationService jwtAuthenticationService) {
        this.jwtAuthenticationService = jwtAuthenticationService;
    }

    public void logout() {
        if (VaadinServletRequest.getCurrent() == null) {
            System.out.println("Cannot logout because VaadinServletRequest.getCurrent() is returning null");
            return;
        }

        if (VaadinServletResponse.getCurrent() == null) {
            System.out.println("Cannot logout because VaadinServletResponse.getCurrent() is returning null");
            return;
        }

        var request = VaadinServletRequest.getCurrent().getHttpServletRequest();
        var response = VaadinServletResponse.getCurrent().getHttpServletResponse();
        logout(request, response);
    }

    public void logout(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("SSO: executing jwt logout...");
        var logoutHandler = new JwtLogoutHandler(jwtAuthenticationService);
        logoutHandler.logout(request, response, SecurityContextHolder.getContext().getAuthentication());
        System.out.println("SSO: finished jwt logout");
    }
}
