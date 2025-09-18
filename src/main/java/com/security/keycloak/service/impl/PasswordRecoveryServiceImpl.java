package com.security.keycloak.service.impl;

import com.security.keycloak.service.IMailService;
import com.security.keycloak.service.IPasswordRecoveryService;
import com.security.keycloak.util.KeycloakProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordRecoveryServiceImpl implements IPasswordRecoveryService {

  private final StringRedisTemplate redis;
  private final IMailService mail;
  private final KeycloakProvider kc;

  @Value("${app.recovery.ttl-seconds:3600}") private long ttl;
  @Value("${app.recovery.base-url}") private String baseUrl;
  @Value("${app.jwt.recovery-prefix:pwd:reset:}") private String prefix;

  @Override
  public void startRecovery(String email) {
    var user = kc.findUserByEmailOrThrow(email);
    String token = UUID.randomUUID().toString();
    redis.opsForValue().set(prefix + token, user.getId(), Duration.ofSeconds(ttl));
    String link = baseUrl + "?token=" + URLEncoder.encode(token, StandardCharsets.UTF_8);
    mail.send(email, "Recupera tu contraseña", "Haz clic en el siguiente enlace: " + link);
  }

  @Override
  public void resetPassword(String token, String newPassword) {
    String key = prefix + token;
    String userId = redis.opsForValue().get(key);
    if (userId == null) throw new RuntimeException("Link expirado o inválido");
    kc.resetUserPassword(userId, newPassword);
    redis.delete(key);
  }
}
