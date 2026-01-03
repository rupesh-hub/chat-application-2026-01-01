package com.alfarays.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.password.CompromisedPasswordChecker;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.password.HaveIBeenPwnedRestApiPasswordChecker;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, prePostEnabled = true)
@RequiredArgsConstructor
@Slf4j
public class CustomSecurityConfiguration {

    private final TokenValidationFilterBefore tokenValidationFilter;
    private final AuthenticationProvider authenticationProvider;

    private static final String[] PUBLIC_URIS = {
            "/authentication",
            "/login",
            "/login/**",
            "/authentication/**",
            "/roles",
            "/roles/**",
            "/permissions",
            "/permissions/**",
            "/auth/**",
            "/favicon.ico",
            "/uploads/**",
            "/actuator/**",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/swagger-ui.html",
            "/system/**",
            "/h2-console/**",
            "/h2-console",
            "/ws",
            "/ws/**"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http.cors(Customizer.withDefaults());

        // Disable CSRF globally (you use JWT)
        http.csrf(AbstractHttpConfigurer::disable);

        // Allow rendering H2 console in a frame
        http.headers(headers -> headers
                .frameOptions(HeadersConfigurer.FrameOptionsConfig::disable)
        );

        // Stateless session
        http.sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // Authorization rules
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(PUBLIC_URIS).permitAll()
                .anyRequest().authenticated()
        );

        // Exception handling
        http.exceptionHandling(ex ->
                ex.accessDeniedHandler(new CustomAccessDeniedHandler()));

        // Filters
        http.addFilterBefore(tokenValidationFilter, UsernamePasswordAuthenticationFilter.class);

        http.authenticationProvider(authenticationProvider);

        return http.build();
    }


    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public CompromisedPasswordChecker compromisedPasswordChecker() {
        return new HaveIBeenPwnedRestApiPasswordChecker();
    }

}
