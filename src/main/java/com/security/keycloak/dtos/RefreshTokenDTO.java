package com.security.keycloak.dtos;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RefreshTokenDTO {
    @NotBlank
    private String refreshToken;
}
