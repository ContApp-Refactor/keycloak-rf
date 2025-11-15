package com.security.keycloak.event;

import jakarta.servlet.http.HttpServletRequest;

public record UserLoggedInEvent(String jwtToken, HttpServletRequest request) {}
