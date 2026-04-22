package com.security.keycloak.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.security.keycloak.service.IPasswordRecoveryService;
import com.security.keycloak.validation.ISanitize;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/keycloak")
@RequiredArgsConstructor
@Validated
public class RecoveryController {

  private final IPasswordRecoveryService recoveryService;

  @PostMapping(value = "/reset-password", consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Void> reset(@RequestBody @Validated ResetDTO req) {
    recoveryService.resetPassword(req.getToken().trim(), req.getNewPassword());
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }
  
  @Data
  public static class ResetDTO {
    @NotBlank @ISanitize private String token;
    @NotBlank @Size(min = 8, max = 64) private String newPassword;
  }
}
