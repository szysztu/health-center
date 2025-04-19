package org.example.bookingsystemapp.security;

import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/swagger-ui/**").permitAll()
                        .requestMatchers("/v3/**").permitAll()

                        .requestMatchers(HttpMethod.POST, "/patient").hasRole("PATIENT")
                        .requestMatchers(HttpMethod.GET, "/patient").hasRole("DOCTOR")
                        .requestMatchers(HttpMethod.PUT, "/patient").hasRole("PATIENT")
                        .requestMatchers(HttpMethod.DELETE, "/patient").hasRole("PATIENT")

                        .requestMatchers(HttpMethod.POST, "/doctor").hasRole("DOCTOR")
                        .requestMatchers(HttpMethod.GET, "/doctor").hasRole("DOCTOR")
                        .requestMatchers(HttpMethod.PUT, "/doctor").hasRole("DOCTOR")
                        .requestMatchers(HttpMethod.DELETE, "/doctor").hasRole("DOCTOR")

                        .requestMatchers(HttpMethod.POST, "/schedule").hasRole("DOCTOR")
                        .requestMatchers(HttpMethod.POST, "/schedule/book").hasRole("PATIENT")
                        .requestMatchers(HttpMethod.GET, "/schedule").hasRole("PATIENT")
                        .requestMatchers(HttpMethod.PUT, "/schedule").hasRole("DOCTOR")
                        .requestMatchers(HttpMethod.DELETE, "/schedule").hasRole("DOCTOR")
                        .requestMatchers("/schedule/free/**").hasRole("PATIENT")
                        .requestMatchers("/schedule/criteria").hasRole("PATIENT")

                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));

        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthoritiesClaimName("realm_access.roles");
        grantedAuthoritiesConverter.setAuthorityPrefix("");

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }

    @Bean
    public Keycloak keycloak(@Value("${keycloak.admin-username}") String username,
                             @Value("${keycloak.admin-password}") String password,
                             @Value("${bookingapp.sso.client-secret}") String clientSecret) {

        return KeycloakBuilder.builder()
                .serverUrl("http://keycloak:8080")
                .realm("booking-system")
                .clientId("booking-app")
                .clientSecret(clientSecret)
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .username(username)
                .password(password)
                .build();
    }

}
