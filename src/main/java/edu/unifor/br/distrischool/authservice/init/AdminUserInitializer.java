package edu.unifor.br.distrischool.authservice.init;

import edu.unifor.br.distrischool.authservice.entity.User;
import edu.unifor.br.distrischool.authservice.repository.UserRepository;
import edu.unifor.br.distrischool.authservice.service.KafkaEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminUserInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final KafkaEventService kafkaEventService;

    @Value("${app.admin.name}")
    private String adminName;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (adminEmail == null || adminEmail.isBlank()) {
            log.info("ADMIN_EMAIL not provided, skipping admin user initialization");
            return;
        }

        if (userRepository.existsByEmail(adminEmail)) {
            log.info("Admin user with email {} already exists, skipping creation", adminEmail);
            return;
        }

        if (adminPassword == null || adminPassword.isBlank()) {
            log.warn("ADMIN_PASSWORD not provided or empty, skipping admin user creation for email={}", adminEmail);
            return;
        }

        String nameToUse = (adminName == null || adminName.isBlank()) ? "Administrator" : adminName;

        User admin = User.builder()
                .name(nameToUse)
                .email(adminEmail)
                .password(passwordEncoder.encode(adminPassword))
                .role(User.Role.ADMIN)
                .emailVerified(true)
                .enabled(true)
                .build();

        userRepository.save(admin);

        try {
            kafkaEventService.publishUserEvent("user.registered", admin);
        } catch (Exception e) {
            log.warn("Failed to publish user.registered event for admin: {} - {}", adminEmail, e.getMessage());
        }

        log.info("Admin user created with email={}", adminEmail);
    }
}
