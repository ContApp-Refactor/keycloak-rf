package com.security.keycloak.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

public final class JwtUtils {
  private JwtUtils() {}

  public static String getJti(String token) {
    try {
      return Jwts.parser().parseClaimsJws(token).getBody().getId();
    } catch (Exception e) { return null; }
  }

  public static String getSub(String token) {
    try {
      return Jwts.parser().parseClaimsJws(token).getBody().getSubject();
    } catch (Exception e) { return null; }
  }

  public static long getTtlSeconds(String token) {
    try {
      Claims c = Jwts.parser().parseClaimsJws(token).getBody();
      long now = System.currentTimeMillis();
      long exp = (c.getExpiration() != null) ? c.getExpiration().getTime() : now;
      long ttlMs = Math.max(0, exp - now);
      return ttlMs / 1000L;
    } catch (Exception e) { return 0; }
  }
}