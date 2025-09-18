package com.security.keycloak.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = InstitutionalEmailValidator.class)
public @interface InstitutionalEmail {
  String message() default "El correo debe ser institucional (@unicauca.edu.co)";
  Class<?>[] groups() default {};
  Class<? extends Payload>[] payload() default {};
  String domain() default "unicauca.edu.co";
}
