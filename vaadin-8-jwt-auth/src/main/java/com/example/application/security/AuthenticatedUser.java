package com.example.application.security;


import com.example.application.data.User;
import com.example.application.data.UserRepository;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.ui.UI;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@SpringComponent
public class AuthenticatedUser {

    private final UserRepository userRepository;
    private final SecurityService securityService;

    public AuthenticatedUser(UserRepository userRepository, SecurityService securityService) {
        this.userRepository = userRepository;
        this.securityService = securityService;
    }

    public boolean isAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated();
    }

    public String getUsername() {
        if (isAuthenticated()) {
            return SecurityContextHolder.getContext().getAuthentication().getName();
        }
        return "";
    }

    public Optional<User> getUser() {
        if (isAuthenticated()) {
            return userRepository.findByUsername(getUsername());
        }
        return Optional.empty();
    }

    public void logout() {
        securityService.logout();
        UI.getCurrent().getPage().reload();
    }

}
