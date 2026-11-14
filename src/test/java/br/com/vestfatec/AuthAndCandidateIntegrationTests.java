package br.com.vestfatec;

import br.com.vestfatec.candidate.CandidateRepository;
import br.com.vestfatec.user.Role;
import br.com.vestfatec.user.User;
import br.com.vestfatec.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthAndCandidateIntegrationTests {

    private static final String CANDIDATE_REQUEST = """
            {"fullName":"Maria Silva","cpf":"12345678901",
             "email":"maria@example.com","phone":"11999999999"}
            """;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CandidateRepository candidateRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void clearDatabase() {
        candidateRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void registrationPersistsUserAndStartsSession() throws Exception {
        MvcResult result = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"admin@example.com","password":"password","role":"ADMIN"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.email").value("admin@example.com"))
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andExpect(jsonPath("$.token").doesNotExist())
                .andExpect(jsonPath("$.password").doesNotExist())
                .andReturn();

        User user = userRepository.findByEmail("admin@example.com").orElseThrow();
        assertThat(passwordEncoder.matches("password", user.getPassword())).isTrue();

        MockHttpSession session = (MockHttpSession) result.getRequest().getSession(false);
        assertThat(session).isNotNull();
        mockMvc.perform(get("/api/candidates").session(session))
                .andExpect(status().isOk());
    }

    @Test
    void loginRotatesSessionAndAdminCanCreateAndReadCandidates() throws Exception {
        createUser(Role.ADMIN);
        MockHttpSession originalSession = new MockHttpSession();
        String originalSessionId = originalSession.getId();

        MvcResult result = mockMvc.perform(post("/auth/login")
                        .session(originalSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"user@example.com","password":"password"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andReturn();

        MockHttpSession session = (MockHttpSession) result.getRequest().getSession(false);
        assertThat(session).isNotNull();
        assertThat(session.getId()).isNotEqualTo(originalSessionId);
        mockMvc.perform(get("/api/candidates").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        mockMvc.perform(post("/api/candidates")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CANDIDATE_REQUEST))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber());
        assertThat(candidateRepository.count()).isEqualTo(1);

        mockMvc.perform(get("/api/candidates").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].email").value("maria@example.com"));
        Long candidateId = candidateRepository.findByCpf("12345678901").orElseThrow().getId();
        mockMvc.perform(get("/api/candidates/{id}", candidateId).session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cpf").value("12345678901"));
    }

    @Test
    void candidateCanReadButCannotCreateCandidates() throws Exception {
        createUser(Role.CANDIDATE);
        MockHttpSession session = login();

        mockMvc.perform(get("/api/candidates").session(session))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/candidates")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CANDIDATE_REQUEST))
                .andExpect(status().isForbidden());
        assertThat(candidateRepository.count()).isZero();
    }

    @Test
    void invalidCredentialsDoNotStartSession() throws Exception {
        createUser(Role.ADMIN);

        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"user@example.com","password":"wrong"}
                                """))
                .andExpect(status().isUnauthorized())
                .andReturn();
        assertThat(result.getRequest().getSession(false)).isNull();
    }

    @Test
    void requestsWithoutSessionAreUnauthorized() throws Exception {
        mockMvc.perform(get("/api/candidates"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logoutInvalidatesSession() throws Exception {
        createUser(Role.ADMIN);
        MockHttpSession session = login();

        mockMvc.perform(post("/auth/logout").session(session))
                .andExpect(status().isNoContent());
        assertThat(session.isInvalid()).isTrue();
        mockMvc.perform(get("/api/candidates"))
                .andExpect(status().isUnauthorized());
    }

    private void createUser(Role role) {
        userRepository.save(new User("user@example.com", passwordEncoder.encode("password"), role));
    }

    private MockHttpSession login() throws Exception {
        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"user@example.com","password":"password"}
                                """))
                .andExpect(status().isOk())
                .andReturn();
        MockHttpSession session = (MockHttpSession) result.getRequest().getSession(false);
        assertThat(session).isNotNull();
        return session;
    }
}
