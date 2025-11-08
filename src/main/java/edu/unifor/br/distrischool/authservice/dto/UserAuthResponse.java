package edu.unifor.br.distrischool.authservice.dto;

import edu.unifor.br.distrischool.authservice.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAuthResponse {
    private String token;
    private String refreshToken;
    private String type = "Bearer";
    private Long userId;
    private String email;
    private String name;
    private boolean emailVerified;
    private User.Role role;
    private long expiresIn;
}
