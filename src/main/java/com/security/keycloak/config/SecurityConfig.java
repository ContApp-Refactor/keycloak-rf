package com.security.keycloak.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

import lombok.RequiredArgsConstructor;

/**
 * Configuración principal de seguridad para la aplicación.
 * <p>
 * Define las reglas de autorización, manejo de sesiones y configuración
 * de JWT como proveedor de autenticación con Keycloak.
 * </p>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    /**
     * Conversor personalizado de JWT a {@link org.springframework.security.core.Authentication},
     * encargado de extraer roles y atributos del token.
     */
    @Autowired
    private JwtAuthConverter jwtAuthConverter;

    /**
     * Define la cadena de filtros de seguridad de Spring Security.
     * <p>
     * Configura:
     * <ul>
     *   <li>Deshabilitación de CSRF (apropiado para APIs REST).</li>
     *   <li>Rutas públicas (ej: Swagger, actuator, endpoint de token).</li>
     *   <li>Protección con autenticación para todas las demás rutas.</li>
     *   <li>Validación de tokens JWT como método de autenticación.</li>
     *   <li>Gestión de sesiones sin estado (stateless).</li>
     * </ul>
     * </p>
     *
     * @param httpSecurity objeto de configuración de seguridad HTTP.
     * @return la configuración de la cadena de filtros de seguridad.
     * @throws Exception en caso de error en la configuración.
     */
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(http -> http
                    .requestMatchers("/api/keycloak/token/", "/swagger-ui/**", "/v3/api-docs/**", "/actuator/**").permitAll()
                    .anyRequest()
                    .authenticated())
                .oauth2ResourceServer(oauth -> {
                    oauth.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter));
                })
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .build();
    }
}
