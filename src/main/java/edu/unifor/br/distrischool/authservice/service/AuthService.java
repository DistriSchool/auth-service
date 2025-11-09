package edu.unifor.br.distrischool.authservice.service;

import edu.unifor.br.distrischool.authservice.dto.*;
import edu.unifor.br.distrischool.authservice.entity.User;
import edu.unifor.br.distrischool.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final KafkaEventService kafkaEventService;
    private final EmailService emailService;

    @Transactional
    public UserAuthResponse login(LoginRequest request) {
        log.info("Tentativa de login para o email: {}", request.getEmail());

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (!user.isEmailVerified()) {
            throw new RuntimeException("Email não verificado. Por favor, verifique seu email.");
        }

        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        String jwtToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        kafkaEventService.publishUserEvent("user.logged", user);

        log.info("Login bem-sucedido para: {}", user.getEmail());

        return UserAuthResponse.builder()
                .token(jwtToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .type("Bearer")
                .email(user.getEmail())
                .emailVerified(user.isEmailVerified())
                .name(user.getName())
                .role(user.getRole())
                .expiresIn(jwtService.getExpirationTime())
                .build();
    }

    @Transactional
    public UserAuthResponse register(RegisterRequest request) {
        log.info("Tentativa de registro para o email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email já cadastrado");
        }

        String verificationToken = UUID.randomUUID().toString();

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .verificationToken(verificationToken)
                .emailVerified(false)
                .enabled(true)
                .build();

        userRepository.save(user);

        String jwtToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        emailService.sendVerificationEmail(user.getEmail(), verificationToken);

        kafkaEventService.publishUserEvent("user.registered", user);

        log.info("Usuário registrado com sucesso: {}", user.getEmail());

        return UserAuthResponse.builder()
                .token(jwtToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .type("Bearer")
                .email(user.getEmail())
                .emailVerified(user.isEmailVerified())
                .name(user.getName())
                .role(user.getRole())
                .expiresIn(jwtService.getExpirationTime())
                .build();
    }

    @Transactional
    public MessageResponse verifyEmail(String token) {
        log.info("Verificando email com token");

        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new RuntimeException("Token inválido"));

        user.setEmailVerified(true);
        user.setVerificationToken(null);
        userRepository.save(user);

        kafkaEventService.publishUserEvent("user.email.verified", user);

        log.info("Email verificado com sucesso para: {}", user.getEmail());

        return MessageResponse.builder()
                .success(true)
                .message("Email verificado com sucesso!")
                .build();
    }

        @Transactional
        public MessageResponse resendEmailVerification() {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || authentication.getPrincipal() == null) {
                throw new RuntimeException("Usuário não autenticado");
            }

            String email;
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserDetails) {
                email = ((UserDetails) principal).getUsername();
            } else if (principal instanceof String) {
                email = (String) principal;
            } else {
                throw new RuntimeException("Não foi possível determinar o usuário autenticado");
            }

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

            if (user.isEmailVerified()) {
                throw new RuntimeException("Email já verificado");
            }

            String verificationToken = UUID.randomUUID().toString();
            user.setVerificationToken(verificationToken);
            userRepository.save(user);

            emailService.sendVerificationEmail(user.getEmail(), verificationToken);

            log.info("Email de verificação reenviado para: {}", user.getEmail());

            return MessageResponse.builder()
                    .success(true)
                    .message("Email de verificação reenviado com sucesso!")
                    .build();
        }

    @Transactional
    public MessageResponse requestPasswordReset(PasswordResetRequest request) {
        log.info("Solicitação de reset de senha para: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        String resetToken = UUID.randomUUID().toString();
        user.setResetPasswordToken(resetToken);
        user.setResetPasswordExpires(LocalDateTime.now().plusHours(1));
        userRepository.save(user);

        emailService.sendPasswordResetEmail(user.getEmail(), resetToken);

        log.info("Email de reset enviado para: {}", user.getEmail());

        return MessageResponse.builder()
                .success(true)
                .message("Email de recuperação enviado com sucesso!")
                .build();
    }

    @Transactional
    public MessageResponse resetPassword(PasswordResetConfirmRequest request) {
        log.info("Confirmando reset de senha");

        User user = userRepository.findByResetPasswordTokenAndResetPasswordExpiresAfter(
                request.getToken(),
                LocalDateTime.now()
        ).orElseThrow(() -> new RuntimeException("Token inválido ou expirado"));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setResetPasswordToken(null);
        user.setResetPasswordExpires(null);
        userRepository.save(user);

        kafkaEventService.publishUserEvent("user.password.reset", user);

        log.info("Senha resetada com sucesso para: {}", user.getEmail());

        return MessageResponse.builder()
                .success(true)
                .message("Senha alterada com sucesso!")
                .build();
    }

    public UserAuthResponse refreshToken(RefreshTokenRequest request) {
        log.info("Renovando token");

        String userEmail = jwtService.extractUsername(request.getRefreshToken());
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (!jwtService.isTokenValid(request.getRefreshToken(), user)) {
            throw new RuntimeException("Refresh token inválido");
        }

        String newAccessToken = jwtService.generateToken(user);

        return UserAuthResponse.builder()
                .token(newAccessToken)
                .refreshToken(request.getRefreshToken())
                .userId(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole())
                .expiresIn(jwtService.getExpirationTime())
                .build();
    }

    public UserProfileResponse getCurrentUserProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new RuntimeException("Usuário não autenticado");
        }

        String email;
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            email = ((UserDetails) principal).getUsername();
        } else if (principal instanceof String) {
            email = (String) principal;
        } else {
            throw new RuntimeException("Não foi possível determinar o usuário autenticado");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        return UserProfileResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .emailVerified(user.isEmailVerified())
                .createdAt(user.getCreatedAt())
                .lastLogin(user.getLastLogin())
                .build();
    }
}
