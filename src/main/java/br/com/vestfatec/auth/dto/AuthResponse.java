package br.com.vestfatec.auth.dto;

import br.com.vestfatec.user.Role;
import br.com.vestfatec.user.User;

public record AuthResponse(Long id, String email, Role role) {

    public static AuthResponse from(User user) {
        return new AuthResponse(user.getId(), user.getEmail(), user.getRole());
    }
}
