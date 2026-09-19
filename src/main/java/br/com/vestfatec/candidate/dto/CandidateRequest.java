package br.com.vestfatec.candidate.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CandidateRequest(
        @NotBlank String fullName,
        @Pattern(regexp = "\\d{11}", message = "CPF deve conter 11 digitos numericos") String cpf,
        @Email @NotBlank String email,
        @NotBlank String phone
) {
}
