package fr.ensitech.ebooks.repository;

import fr.ensitech.ebooks.entity.User;
import fr.ensitech.ebooks.entity.UserSecurityAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IUserSecurityAnswerRepository extends JpaRepository<UserSecurityAnswer, Long> {
    Optional<UserSecurityAnswer> findByUserId(Long userId);
    Optional<UserSecurityAnswer> findByUser(User user);
}
