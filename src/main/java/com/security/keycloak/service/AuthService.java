package com.security.keycloak.service;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.List;
import java.util.Map;


import org.springframework.stereotype.Service;

import com.security.keycloak.dto.UserResponse;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;




@Service
public class AuthService {

    private  String publicKeyString = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAqXP5w8QRuGpYmYqAMcmFy1UJ+hbkY7rxGN8JyEQGclsFj5Qx00Zue2LCdqKQGxWSqpC+ZZh49h0AH2zfawnYXCQ20gDn5CuvfpBxGY94RlAgxJlcAksNpEDwbBF2g2lBxtj1ABKXpHPrQHncwxKTdDAZFpbQW6K1nJYFcAU+AOpOrP/b13EeLVJTuixvgDYiRM/WbGzLxWbNQUVo9v6fueOv1bglmTjQaAEi/FZHAvpx6Hxughk5j4KQd1ykDX1vpH3xGxIfMKdXlSX1MAlPyyDKNZAqQPUwdzwW/3vAanXmuoICZN9DbFTxIgC7LCVZQaGgaJpYH5pNReUtE6gP2wIDAQAB";  


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
