package com.security.keycloak.service.impl;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.security.keycloak.controller.exception.UserException;
import com.security.keycloak.dtos.AuthDTO;
import com.security.keycloak.dtos.UserDTO;
import com.security.keycloak.event.UserLoggedInEvent;
import com.security.keycloak.service.IAuthKeycloakService;
import com.security.keycloak.util.JwtUtils;
import com.security.keycloak.util.KeycloakProvider;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthKeycloakServiceImpl implements IAuthKeycloakService{

    private static final Logger logger = LoggerFactory.getLogger(AuthKeycloakServiceImpl.class);

    private final StringRedisTemplate redis;
    private final KeycloakProvider keycloakProvider;

    private final ApplicationEventPublisher eventPublisher;
    
    @Value("${app.jwt.blacklist-prefix:jwt:black:}")
    private String blacklistPrefix;

    @Value("${keycloak.client.secret}")
    private String CLIENT_SECRET;

    @Value("${jwt.token.url}")
    private String tokenUrl;

    @Value("${jwt.auth.converter.resource-id}")
    private String CLIENT_ID;

    @Value("${jwt.public.key}")
    private  String publicKeyString;

    @Value("${app.jwt.refresh-prefix:jwt:refresh:}")
    private String refreshPrefix;

    @Value("${app.jwt.refresh-ttl-days:7}")
    private long refreshTtlDays;

    @Override
    public UserDTO getCurrentUser() {

        Authentication auth = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (!(auth instanceof JwtAuthenticationToken jwtAuth)) {
            throw new UserException("No autorizado", 401);
        }

        Jwt jwt = jwtAuth.getToken();

        @SuppressWarnings("unchecked")
        List<String> roles =
            (List<String>) ((Map<String, Object>)
                jwt.getClaims().get("realm_access")).get("roles");

        return UserDTO.builder()
                .id(jwt.getSubject())
                .username(jwt.getClaimAsString("preferred_username"))
                .email("** email **")
                .firstName(jwt.getClaimAsString("given_name"))
                .lastName(jwt.getClaimAsString("family_name"))
                .roles(roles)
                .build();
    }

    
    private PublicKey getPublicKey(String publicKeyString) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyString);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(keySpec);
    }

    @Override
    public void logoutAndBlacklist(HttpServletRequest request) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (!(auth instanceof JwtAuthenticationToken jwtAuth)) {
            return;
        }

        Jwt jwt = jwtAuth.getToken();

        String jti = jwt.getId();
        Instant exp = jwt.getExpiresAt();

        if (jti != null && exp != null) {
            long ttl = Duration.between(Instant.now(), exp).getSeconds();
            if (ttl > 0) {
                redis.opsForValue()
                    .set(blacklistPrefix + jti, "1", Duration.ofSeconds(ttl));
            }
        }

        String refreshToken = request.getHeader("X-Refresh-Token");
        if (refreshToken != null) {
            redis.delete(refreshPrefix + refreshToken);
        }

    }

    @SuppressWarnings("unchecked")
    @Override
    public String getToken(AuthDTO authDTO, HttpServletRequest request) throws JsonMappingException, JsonProcessingException {

        try {
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("client_id", CLIENT_ID);
            formData.add("grant_type", "password");
            formData.add("username", authDTO.getUsername());
            formData.add("password", authDTO.getPassword());
            formData.add("client_secret", CLIENT_SECRET);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(formData, headers);

            ResponseEntity<String> response = new RestTemplate().postForEntity(tokenUrl, requestEntity, String.class);

            String responseBody = response.getBody();

            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> responseMap = mapper.readValue(responseBody, Map.class);
            String accessToken = (String) responseMap.get("access_token");

            
            String userId = JwtUtils.getSub(accessToken);
            if (userId != null) {
                var userRep = keycloakProvider.getUserResource().get(userId).toRepresentation();
                if (!userRep.isEnabled()) {
                    throw new UserException("Usuario inactivo", 401);
                }
            }

            String rptToken = getTokenRPT(accessToken);
            String jti = JwtUtils.getJti(rptToken);
            userId = JwtUtils.getSub(rptToken);

            String refreshToken = createRefreshToken(userId, jti);

            Object expiresInObject = responseMap.get("expires_in");
            Object refreshExpires = responseMap.get("refresh_expires_in");

            Map<String, Object> accessTokenInfo = new HashMap<>();
            accessTokenInfo.put("access_token", rptToken);
            accessTokenInfo.put("refresh_token", refreshToken);
            accessTokenInfo.put("expires_in", expiresInObject);

            //Publico evento de token para micro de auditoria
            eventPublisher.publishEvent(new UserLoggedInEvent(rptToken, request));

            return mapper.writeValueAsString(accessTokenInfo);
        } catch (RestClientException e) {
            logger.warn("Intento de login fallido para usuario: {}", authDTO.getUsername());
            throw new UserException("Credenciales inválidas", 401);
        } catch (Exception e) {
            logger.error("Error interno en getToken para usuario: {}", authDTO.getUsername(), e);
            throw new UserException("Error interno en autenticación", 500);
        }
    }
    
    @SuppressWarnings("unchecked")
    public String getTokenRPT(String token) throws JsonMappingException, JsonProcessingException {

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "urn:ietf:params:oauth:grant-type:uma-ticket");
        formData.add("audience", CLIENT_ID);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", "Bearer " + token);

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(formData, headers);

        ResponseEntity<String> response = new RestTemplate().postForEntity(tokenUrl, requestEntity, String.class);

        String responseBody = response.getBody();

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> responseMap = mapper.readValue(responseBody, Map.class);
        String accessToken = (String) responseMap.get("access_token");

        return accessToken;
    }

    private String getTokenRPTFromRefresh() throws JsonProcessingException {

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "client_credentials");
        formData.add("client_id", CLIENT_ID);
        formData.add("client_secret", CLIENT_SECRET);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> requestEntity =
                new HttpEntity<>(formData, headers);

        ResponseEntity<String> response =
                new RestTemplate().postForEntity(tokenUrl, requestEntity, String.class);

        Map<String, Object> responseMap =
                new ObjectMapper().readValue(response.getBody(), Map.class);

        return (String) responseMap.get("access_token");
    }


    private String createRefreshToken(String userId, String accessJti) {
        String refreshToken = java.util.UUID.randomUUID().toString();

        Map<String, String> data = new HashMap<>();
        data.put("userId", userId);
        data.put("accessJti", accessJti);

        try {
            String value = new ObjectMapper().writeValueAsString(data);

            redis.opsForValue().set(
                refreshPrefix + refreshToken,
                value,
                Duration.ofDays(refreshTtlDays)
            );

            return refreshToken;
        } catch (JsonProcessingException e) {
            throw new UserException("Error creando refresh token", 500);
        }
    }

    @Override
    public String refreshToken(String refreshToken) {

        String key = refreshPrefix + refreshToken;
        String value = redis.opsForValue().get(key);

        if (value == null) {
            throw new UserException("Refresh token inválido", 401);
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, String> data = mapper.readValue(value, Map.class);

            String oldJti = data.get("accessJti");

            // si el access token fue revocado → refresh inválido
            if (Boolean.TRUE.equals(redis.hasKey(blacklistPrefix + oldJti))) {
                redis.delete(key);
                throw new UserException("Sesión inválida", 401);
            }

            // emitir nuevo token
            String newAccessToken = getTokenRPTFromRefresh();

            String newJti = JwtUtils.getJti(newAccessToken);
            String newRefreshToken = createRefreshToken(data.get("userId"), newJti);

            // ROTACIÓN
            redis.delete(key);

            Map<String, Object> response = new HashMap<>();
            response.put("access_token", newAccessToken);
            response.put("refresh_token", newRefreshToken);

            eventPublisher.publishEvent(
                new UserLoggedInEvent(newAccessToken, null)
            );

            return mapper.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            throw new UserException("Error procesando refresh token", 500);
        }
    }


    
}

