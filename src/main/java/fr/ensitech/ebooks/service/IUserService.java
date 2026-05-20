package fr.ensitech.ebooks.service;

import fr.ensitech.ebooks.entity.SecurityQuestions;
import fr.ensitech.ebooks.entity.UserSecurityAnswer;
import org.springframework.stereotype.Service;

import fr.ensitech.ebooks.entity.User;

import java.util.List;
import java.util.Optional;

@Service
public interface IUserService {
	User addOrUpdateUser(User user);
    void deactivateAccount(Long userId);
    void deleteUser(Long userId);
    Optional<User> findByEmail(String email);
	String validateVerificationToken(String token);
    UserSecurityAnswer addSecurityAnswer(User user, Long questionId, String securityAnswer);
    List<SecurityQuestions> getAllSecurityQuestions();
    String generateVerificationCode(User user);
    boolean validateVerificationCode(User user, String code);

    // Gestion de la mise à jour du mot de passe
    boolean updatePassword(User user, String oldPassword, String newPassword, String confirmPassword, Long questionId, String securityAnswer);

    // Gestion de la réinitialisation du mot de passe
    void initiateForgotPassword(String email);
    boolean validateResetToken(String token);
    boolean resetPassword(String token, String newPassword, String confirmPassword);

    // Vérification de la question de sécurité
    SecurityQuestions getSecurityQuestionForUser(User user);
    boolean verifySecurityAnswer(User user, String answer);

    // Méthodes pour l'administrateur
    List<User> findAll();
    User findById(Long id);
    User save(User user);
    User updateUser(User user);

}
