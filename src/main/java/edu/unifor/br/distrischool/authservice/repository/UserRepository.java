package edu.unifor.br.distrischool.authservice.repository;

import edu.unifor.br.distrischool.authservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<User> findByVerificationToken(String token);

    Optional<User> findByResetPasswordTokenAndResetPasswordExpiresAfter(
            String token,
            LocalDateTime now
    );
}
