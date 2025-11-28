package fr.ensitech.ebooks.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.ensitech.ebooks.entity.User;

import java.util.Optional;
import java.util.UUID;

public interface IUserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByVerificationToken(String token);
    Optional<User> findByResetPasswordToken(String resetPasswordToken);
}
