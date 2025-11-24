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
public class UserEvent {
    private String eventType;
    private Long userId;
    private String email;
    private String name;
    private User.Role role;
    private String password; // temporary plaintext password sent by producer
    private String timestamp;
}
