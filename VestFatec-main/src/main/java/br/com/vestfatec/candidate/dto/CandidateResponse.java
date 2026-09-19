package br.com.vestfatec.candidate.dto;

import br.com.vestfatec.candidate.Candidate;

public record CandidateResponse(
        Long id,
        String fullName,
        String cpf,
        String email,
        String phone
) {
    public static CandidateResponse from(Candidate candidate) {
        return new CandidateResponse(
                candidate.getId(),
                candidate.getFullName(),
                candidate.getCpf(),
                candidate.getEmail(),
                candidate.getPhone()
        );
    }
}
