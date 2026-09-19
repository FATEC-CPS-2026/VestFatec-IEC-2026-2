package br.com.vestfatec.auth.dto;

import br.com.vestfatec.user.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegisterRequest(
        @Email @NotBlank String email,
        @NotBlank String password,
        @NotNull Role role
) {
}
