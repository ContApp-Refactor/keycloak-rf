package com.security.keycloak.event;

import jakarta.servlet.http.HttpServletRequest;

public record UserLoggedOutEvent(String jwtToken, HttpServletRequest request) {

}
