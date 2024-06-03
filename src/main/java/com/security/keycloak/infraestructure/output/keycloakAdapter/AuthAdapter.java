package com.security.keycloak.infraestructure.output.keycloakAdapter;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.security.keycloak.application.output.IAuthOutputPort;
import com.security.keycloak.domain.models.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

@Component
public class AuthAdapter implements IAuthOutputPort{


    @Value("${jwt.public.key}")
    private  String publicKeyString;

    @Override
    public User getCurrentUser(String authorizationHeader) throws NoSuchAlgorithmException, InvalidKeySpecException {
        String jwt = null;
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
        }

        if (jwt != null) {
            PublicKey publicKey = getPublicKey(publicKeyString);
            Claims claims = Jwts.parser().setSigningKey(publicKey).parseClaimsJws(jwt).getBody();
            @SuppressWarnings("unchecked")
            User user = User.builder()
            .id(claims.get("sub").toString())
            .username(claims.get("preferred_username").toString())
            .email(claims.get("email").toString())
            .firstName(claims.get("given_name").toString())
            .lastName(claims.get("family_name").toString())
            .roles((List<String>) ((Map<String, Object>) ((Map<String, Object>) claims.get("resource_access")).get("microservices_client")).get("roles"))
            .build();

            return user;
        } else {
            return null;
        }
    }

        //obtener publicKey
    private PublicKey getPublicKey(String publicKeyString) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyString);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(keySpec);
    }
    
    
}
