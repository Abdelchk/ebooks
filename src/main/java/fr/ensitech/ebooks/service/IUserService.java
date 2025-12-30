package fr.ensitech.ebooks.service;

import fr.ensitech.ebooks.entity.SecurityQuestions;
import fr.ensitech.ebooks.entity.UserSecurityAnswer;
import org.springframework.stereotype.Service;

import fr.ensitech.ebooks.entity.User;

import java.util.List;
import java.util.Optional;

@Service
public interface IUserService {
	User addOrUpdateUser(User user) throws Exception;
    void deleteUser(Long id) throws Exception;
    Optional<User> findByEmail(String email);
	String validateVerificationToken(String token) throws Exception;
    UserSecurityAnswer addSecurityAnswer(User user, Long questionId, String securityAnswer) throws Exception;
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

}
