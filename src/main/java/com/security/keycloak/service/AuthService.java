package com.security.keycloak.service;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.security.keycloak.dto.UserResponse;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;




@Service
public class AuthService {

    @Value("${jwt.public.key}")
    private  String publicKeyString;


     //obtener usuaruio del token
     public UserResponse getCurrentUser(String authorizationHeader) throws NoSuchAlgorithmException, InvalidKeySpecException{
          String jwt = null;
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
        }

        if (jwt != null) {
            PublicKey publicKey = getPublicKey(publicKeyString);
            Claims claims = Jwts.parser().setSigningKey(publicKey).parseClaimsJws(jwt).getBody();
            @SuppressWarnings("unchecked")
            UserResponse user = UserResponse.builder()
            .id(claims.get("sub").toString())
            .username(claims.getSubject())
            .email(claims.get("email").toString())
            .firstName(claims.get("given_name").toString())
            .lastName(claims.get("family_name").toString())
            .roles((List<String>) ((Map<String, Object>) ((Map<String, Object>) claims.get("resource_access")).get("microservices_client")).get("roles"))
            .build();


            user.setUsername(claims.getSubject());
            user.setEmail(claims.get("email").toString());
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
