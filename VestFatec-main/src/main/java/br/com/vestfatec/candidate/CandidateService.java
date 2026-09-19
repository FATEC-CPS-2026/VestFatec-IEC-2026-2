package br.com.vestfatec.candidate;

import br.com.vestfatec.candidate.dto.CandidateRequest;
import br.com.vestfatec.candidate.dto.CandidateResponse;
import java.util.List;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CandidateService {

    private final CandidateRepository candidateRepository;

    public CandidateService(CandidateRepository candidateRepository) {
        this.candidateRepository = candidateRepository;
    }

    @Transactional
    public CandidateResponse create(CandidateRequest request) {
        if (candidateRepository.existsByCpf(request.cpf())) {
            throw new IllegalArgumentException("CPF ja cadastrado");
        }
        if (candidateRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email ja cadastrado");
        }

        Candidate candidate = new Candidate(
                request.fullName(),
                request.cpf(),
                request.email(),
                request.phone()
        );

        return CandidateResponse.from(candidateRepository.save(candidate));
    }

    @Cacheable(value = "candidates", key = "'all'")
    @Transactional(readOnly = true)
    public List<CandidateResponse> findAll() {
        return candidateRepository.findAll().stream()
                .map(CandidateResponse::from)
                .toList();
    }

    @Cacheable(value = "candidates", key = "#id")
    @Transactional(readOnly = true)
    public CandidateResponse findById(Long id) {
        Candidate candidate = candidateRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Candidato nao encontrado"));
        return CandidateResponse.from(candidate);
    }
}
