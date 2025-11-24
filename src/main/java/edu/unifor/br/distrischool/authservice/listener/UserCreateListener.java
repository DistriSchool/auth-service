package edu.unifor.br.distrischool.authservice.listener;

import edu.unifor.br.distrischool.authservice.dto.UserEvent;
import edu.unifor.br.distrischool.authservice.entity.User;
import edu.unifor.br.distrischool.authservice.repository.UserRepository;
import edu.unifor.br.distrischool.authservice.service.EmailService;
import edu.unifor.br.distrischool.authservice.service.KafkaEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserCreateListener {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final KafkaEventService kafkaEventService;

    @KafkaListener(topics = "user.create", groupId = "auth-service-group", containerFactory = "kafkaListenerContainerFactory")
    public void handleUserCreate(UserEvent event) {
        try {
            log.info("Received user.create event for email={}", event.getEmail());

            if (event.getEmail() == null || event.getEmail().isBlank()) {
                log.warn("user.create event missing email, skipping: {}", event);
                return;
            }

            if (userRepository.existsByEmail(event.getEmail())) {
                log.warn("User already exists with email={}, skipping creation", event.getEmail());
                return;
            }

            // Map role
            User.Role role = User.Role.STUDENT;
            if (event.getRole() != null) {
                try {
                    role = event.getRole();
                } catch (Exception e) {
                    log.warn("Invalid role in event, defaulting to STUDENT: {}", event.getRole());
                }
            }

            String rawPassword = event.getPassword();
            if (rawPassword == null || rawPassword.isBlank()) {
                // generate fallback temp password
                rawPassword = java.util.UUID.randomUUID().toString().replaceAll("[^A-Za-z0-9]", "").substring(0, 10);
            }

            User user = User.builder()
                    .name(event.getName())
                    .email(event.getEmail())
                    .password(passwordEncoder.encode(rawPassword))
                    .role(role)
                    .emailVerified(false)
                    .enabled(true)
                    .build();

            userRepository.save(user);

            // send temporary password email
            emailService.sendTemporaryPasswordEmail(user.getEmail(), rawPassword);

            // publish user.registered event
            kafkaEventService.publishUserEvent("user.registered", user);

            log.info("User created from event: {}", user.getEmail());

        } catch (Exception e) {
            log.error("Error handling user.create event: {}", e.getMessage(), e);
            throw e;
        }
    }
}

