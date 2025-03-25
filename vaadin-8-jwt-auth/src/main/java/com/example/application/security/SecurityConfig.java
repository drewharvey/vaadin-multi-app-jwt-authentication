package com.example.application.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@EnableWebSecurity
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	private final UserDetailsService userDetailsService;
	private final JwtAuthenticationService jwtAuthenticationService;
	private final SecurityService securityService;

	public SecurityConfig(UserDetailsService userDetailsService,
						  JwtAuthenticationService jwtAuthenticationService,
						  SecurityService securityService) {
		this.userDetailsService = userDetailsService;
		this.jwtAuthenticationService = jwtAuthenticationService;
        this.securityService = securityService;
    }

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
				.csrf().disable()
				.authorizeRequests()
				.antMatchers("/login", "public/**", "/images/*.png").permitAll()
				.antMatchers("/VAADIN/**").permitAll()
				.antMatchers("/line-awesome/**/*.svg").permitAll()
				.anyRequest().authenticated()
				.and()
				.formLogin()
				.successHandler(new JwtAuthenticationSuccessHandler(jwtAuthenticationService))
				.and()
				.logout()
				.invalidateHttpSession(true)
				.addLogoutHandler(new JwtLogoutHandler(jwtAuthenticationService))
				.logoutUrl("/logout");

		// Run the JWT filter before the username/password authentication filter
		 http.addFilterBefore(new JwtAuthenticationFilter(userDetailsService, securityService, jwtAuthenticationService), UsernamePasswordAuthenticationFilter.class);
	}

	@Override
	public UserDetailsService userDetailsService() {
		return userDetailsService;
	}

}
