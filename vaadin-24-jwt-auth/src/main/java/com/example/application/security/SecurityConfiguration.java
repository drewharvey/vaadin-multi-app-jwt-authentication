package com.example.application.security;

import com.example.application.views.LoginView;
import com.vaadin.flow.spring.security.VaadinWebSecurity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@EnableWebSecurity
@Configuration
public class SecurityConfiguration extends VaadinWebSecurity {

    private final UserDetailsService userDetailsService;
    private final SecurityService securityService;
    private final JwtAuthenticationService jwtAuthenticationService;

    private final String loginUrl;
    private final String logoutUrl;

    @Autowired
    public SecurityConfiguration(@Value("${app.login.url}") String loginUrl,
                                 @Value("${app.logout.url}") String logoutUrl,
                                 UserDetailsService userDetailsService,
                                 SecurityService securityService,
                                 JwtAuthenticationService jwtAuthenticationService) {
        this.loginUrl = loginUrl;
        this.logoutUrl = logoutUrl;
        this.userDetailsService = userDetailsService;
        this.securityService = securityService;
        this.jwtAuthenticationService = jwtAuthenticationService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        // Configure your static resources with public access before calling
        // super.configure(HttpSecurity) as it adds final anyRequest matcher
        http.authorizeHttpRequests(auth -> auth.requestMatchers(new AntPathRequestMatcher("/public/**"))
                .permitAll());

        http.authorizeHttpRequests(auth -> auth.requestMatchers(new AntPathRequestMatcher("**/login"))
                .permitAll());

        http.authorizeHttpRequests(authorize -> authorize
                .requestMatchers(new AntPathRequestMatcher("/images/*.png")).permitAll());

        // Icons from the line-awesome addon
        http.authorizeHttpRequests(authorize -> authorize
                .requestMatchers(new AntPathRequestMatcher("/line-awesome/**/*.svg")).permitAll());

        super.configure(http);

        setLoginView(http, LoginView.class);

        // on login, we need to setup the JWTs
        http.formLogin(login -> login
                .loginPage(loginUrl)
                .successHandler(new JwtAuthenticationSuccessHandler(jwtAuthenticationService))
                .permitAll()
        );

        // on logout, we need to clear the JWTs
        http.logout(logout -> logout
                .invalidateHttpSession(false)
                .logoutUrl(logoutUrl)
                .addLogoutHandler(new JwtLogoutHandler(jwtAuthenticationService))
                .permitAll()
        );

        // run the jwt filter before standard username password filter
        http.addFilterAt(new JwtAuthenticationFilter(userDetailsService, securityService, jwtAuthenticationService), UsernamePasswordAuthenticationFilter.class);

    }
}
