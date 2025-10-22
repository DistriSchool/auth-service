package edu.unifor.br.distrischool.authservice.dto;

import edu.unifor.br.distrischool.authservice.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {
    private Long id;
    private String name;
    private String email;
    private User.Role role;
    private boolean emailVerified;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;
}

