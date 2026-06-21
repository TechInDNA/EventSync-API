package com.techindna.eventsyncapi.config;

import com.techindna.eventsyncapi.exception.ErrorResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${cors.allowed-origins}")
    private String allowedOrigins;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(allowedOrigins.split(",")));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
        configuration.setAllowedHeaders(List.of("Authorization", "Cache-Control", "Content-Type", "X-Requested-With"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, TokenProvider tokenProvider) {
        http
            .cors(Customizer.withDefaults())
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.POST, "/events/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/events/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/events/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/events/**").permitAll()

                .requestMatchers(HttpMethod.POST, "/rooms/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/rooms/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/rooms/**").permitAll()
                .requestMatchers(HttpMethod.PUT, "/rooms/**").hasRole("ADMIN")

                .requestMatchers(HttpMethod.POST, "/speakers/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/speakers/**").permitAll()
                .requestMatchers(HttpMethod.PUT, "/speakers/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/speakers/**").hasRole("ADMIN")

                .requestMatchers(HttpMethod.POST, "/sessions/*/questions").authenticated()
                .requestMatchers(HttpMethod.POST, "/sessions/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/sessions/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/sessions/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/sessions/**").permitAll()

                .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                .requestMatchers(HttpMethod.POST, "/auth/participant").permitAll()
                .requestMatchers(HttpMethod.POST, "/ai/conversations/**").hasRole("ADMIN")
                .requestMatchers("/mcp/**").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(new JwtAuthenticationFilter(tokenProvider), UsernamePasswordAuthenticationFilter.class)
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint((request, response, authException) ->
                    ErrorResponse.send(response, HttpStatus.UNAUTHORIZED, "Authentication required.")
                )
                .accessDeniedHandler((request, response, accessDeniedException) ->
                    ErrorResponse.send(response, HttpStatus.FORBIDDEN, "Insufficient privileges.")
                )
            );
        return http.build();
    }
}
