package fr.ensitech.ebooks.service;

import fr.ensitech.ebooks.entity.SecurityQuestions;
import fr.ensitech.ebooks.entity.User;
import fr.ensitech.ebooks.entity.UserSecurityAnswer;
import fr.ensitech.ebooks.entity.VerificationCode;
import fr.ensitech.ebooks.repository.ISecurityQuestionsRepository;
import fr.ensitech.ebooks.repository.IUserRepository;
import fr.ensitech.ebooks.repository.IUserSecurityAnswerRepository;
import fr.ensitech.ebooks.repository.IVerificationCodeRepository;
import fr.ensitech.ebooks.utils.Dates;
import fr.ensitech.ebooks.utils.PasswordHistoryTokenizer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@Service
public class UserService implements IUserService {

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private ISecurityQuestionsRepository securityQuestionsRepository;

    @Autowired
    private IUserSecurityAnswerRepository userSecurityAnswerRepository;

    @Autowired
    private IVerificationCodeRepository verificationCodeRepository;

    @Autowired
    private EmailService emailService;

    @Override
    public User registerNewUserAccount(User user, Long questionId, String securityAnswer) {
        //    	if (user == null) {
//	        throw new NullPointerException("Le user à créer ne doit pas être NULL !");
//	    }
//	    if (user.getLastname() == null || user.getLastname().trim().isEmpty() || user.getFirstname() == null
//	            || user.getFirstname().trim().isEmpty() || user.getEmail() == null
//	            || user.getEmail().trim().isEmpty() || user.getPassword() == null
//	            || user.getPassword().trim().isEmpty() || user.getBirthdate() == null
//	            || Dates.convertDateToString(user.getBirthdate()).trim().isEmpty()
//	            || user.getPhoneNumber() == null || user.getPhoneNumber().trim().isEmpty())
//	    {
//	        throw new IllegalArgumentException("Tous les paramètres sont obligatoires !");
//	    }
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Un compte avec cet email existe déjà");
        }
        if (securityAnswer == null || securityAnswer.trim().isEmpty()) {
            throw new IllegalArgumentException("La réponse de sécurité est obligatoire");
        }
        if (securityAnswer.length() > 32) {
            throw new IllegalArgumentException("La réponse ne doit pas dépasser 32 caractères");
        }

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        // Encoder le mot de passe
        user.setPassword(encoder.encode(user.getPassword()));
        user.setEnabled(false);
        user.setLastPasswordUpdateDate(LocalDate.now()); // Initialiser la date de mise à jour

        // Générer le token de vérification
        String token = UUID.randomUUID().toString();
        user.setVerificationToken(token);
        userRepository.save(user);

        // Enregistrer la réponse de sécurité
        UserSecurityAnswer answer = new UserSecurityAnswer();
        answer.setUser(user);
        answer.setSecurityQuestion(securityQuestionsRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Question de sécurité invalide")));
        answer.setHashedAnswer(encoder.encode(securityAnswer.toLowerCase().trim()));
        userSecurityAnswerRepository.save(answer);

        // Envoyer l'email de vérification
        String confirmationUrl = "http://localhost:8080/verify-email?token=" + token;
        emailService.sendEmail(user.getEmail(), "Vérification de compte",
                "Cliquez sur le lien pour vérifier votre compte : " + confirmationUrl);

        return user;
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public String validateVerificationToken(String token) {
        User user = userRepository.findByVerificationToken(token).orElse(null);
        if (user == null) {
            return "invalid";
        }

        user.setEnabled(true);
        userRepository.save(user);
        return "valid";
    }

    @Override
    public List<SecurityQuestions> getAllSecurityQuestions() {
        return securityQuestionsRepository.findAll();
    }

    @Override
    @Transactional
    public String generateVerificationCode(User user) {
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("Utilisateur invalide");
        }

        if (user.getEmail() == null || !user.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("Email invalide");
        }

        // Supprimer les codes non utilisés et expirés pour cet utilisateur
        verificationCodeRepository.deleteExpiredCodes(user, LocalDateTime.now());

        // Limiter le nombre de codes actifs par utilisateur (anti-spam)
        long activeCodesCount = verificationCodeRepository.countActiveCodesByUser(user, LocalDateTime.now());
        if (activeCodesCount >= 3) {
            throw new IllegalStateException("Trop de codes actifs. Veuillez réessayer plus tard.");
        }

        String code = String.format("%06d", new Random().nextInt(999999));

        VerificationCode verificationCode = new VerificationCode();
        verificationCode.setUserId(user);
        verificationCode.setCode(code);
        verificationCode.setExpiryDate(LocalDateTime.now().plusMinutes(2));
        verificationCode.setUsed(false);
        verificationCodeRepository.save(verificationCode);

        // Envoyer le code par email
        emailService.sendEmail(user.getEmail(), "Code de vérification",
                "Votre code de vérification est : " + code + ". Il expire dans 2 minutes.");

        return code;
    }

    @Override
    public boolean validateVerificationCode(User user, String code) {
        if (user == null || code == null) {
            return false;
        }

        // Validation du format du code
        if (!code.matches("^[0-9]{6}$")) {
            return false;
        }

        Optional<VerificationCode> verificationCodeOpt =
                verificationCodeRepository.findByUserIdAndCodeAndUsedFalse(user, code);

        if (verificationCodeOpt.isEmpty()) {
            return false;
        }

        VerificationCode verificationCode = verificationCodeOpt.get();

        if (LocalDateTime.now().isAfter(verificationCode.getExpiryDate())) {
            // Marquer comme utilisé même si expiré pour éviter la réutilisation
            verificationCode.setUsed(true);
            verificationCodeRepository.save(verificationCode);
            return false;
        }

        verificationCode.setUsed(true);
        verificationCodeRepository.save(verificationCode);

        return true;
    }

    @Override
    @Transactional
    public boolean updatePassword(User user, String oldPassword, String newPassword,
                                   String confirmPassword, Long questionId, String securityAnswer) {
        if (user == null) {
            throw new IllegalArgumentException("Utilisateur invalide");
        }

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        // Vérifier l'ancien mot de passe
        if (!encoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("L'ancien mot de passe est incorrect");
        }

        // Vérifier que les nouveaux mots de passe correspondent
        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("Les mots de passe ne correspondent pas");
        }

        // Vérifier la question de sécurité
        if (!verifySecurityAnswer(user, securityAnswer)) {
            throw new IllegalArgumentException("La réponse à la question de sécurité est incorrecte");
        }

        // Vérifier la force du nouveau mot de passe
        if (!newPassword.matches("^(?=.*[A-Za-zÀ-ÖØ-öø-ÿ])(?=.*\\d)(?=.*[@$!%*?&#])[A-Za-zÀ-ÖØ-öø-ÿ\\d@$!%*?&#]{12,}$")) {
            throw new IllegalArgumentException("Le nouveau mot de passe ne respecte pas les critères de sécurité");
        }

        // Vérifier que le nouveau mot de passe n'est pas identique à l'ancien
        if (newPassword.equals(oldPassword)) {
            throw new IllegalArgumentException("Le nouveau mot de passe doit être différent de l'ancien mot de passe.");
        }

        // Vérifier que le nouveau mot de passe n'est pas dans l'historique
        String currentHistory = user.getPasswordHistory() != null ? user.getPasswordHistory() : "";
        List<String> passwordHistory = PasswordHistoryTokenizer.tokenize(currentHistory);

        for (String oldHashedPassword : passwordHistory) {
            if (encoder.matches(newPassword, oldHashedPassword)) {
                throw new IllegalArgumentException("Ce mot de passe a déjà été utilisé récemment. Veuillez en choisir un autre.");
            }
        }

        // Encoder le nouveau mot de passe après toutes les vérifications
        String hashedNewPassword = encoder.encode(newPassword);

        // Ajouter l'ancien mot de passe à l'historique
        String updatedHistory = PasswordHistoryTokenizer.addPasswordToHistory(currentHistory, user.getPassword());

        // Mettre à jour le mot de passe
        user.setPassword(hashedNewPassword);
        user.setPasswordHistory(updatedHistory);
        user.setLastPasswordUpdateDate(LocalDate.now());

        userRepository.save(user);

        return true;
    }

    @Override
    public void initiateForgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Aucun compte n'est associé à cet email"));

        // Générer un token de réinitialisation
        String token = UUID.randomUUID().toString();
        user.setResetPasswordToken(token);
        user.setResetTokenExpiryDate(LocalDate.now().plusDays(1)); // Expire dans 24h

        userRepository.save(user);

        // Envoyer l'email de réinitialisation
        String resetUrl = "http://localhost:8080/reset-password?token=" + token;
        emailService.sendEmail(user.getEmail(), "Réinitialisation de mot de passe",
                "Cliquez sur le lien pour réinitialiser votre mot de passe : " + resetUrl +
                "\n\nCe lien expire dans 24 heures.");
    }

    @Override
    public boolean validateResetToken(String token) {
        Optional<User> userOpt = userRepository.findByResetPasswordToken(token);
        if (userOpt.isEmpty()) {
            return false;
        }

        User user = userOpt.get();

        // Vérifier que le token n'a pas expiré
        if (user.getResetTokenExpiryDate() == null ||
            LocalDate.now().isAfter(user.getResetTokenExpiryDate())) {
            return false;
        }

        return true;
    }

    @Override
    @Transactional
    public boolean resetPassword(String token, String newPassword, String confirmPassword) {
        User user = userRepository.findByResetPasswordToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Token invalide"));

        // Vérifier que le token n'a pas expiré
        if (user.getResetTokenExpiryDate() == null ||
            LocalDate.now().isAfter(user.getResetTokenExpiryDate())) {
            throw new IllegalArgumentException("Le token a expiré");
        }

        // Vérifier que les mots de passe correspondent
        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("Les mots de passe ne correspondent pas");
        }

        // Vérifier la force du nouveau mot de passe
        if (!newPassword.matches("^(?=.*[A-Za-zÀ-ÖØ-öø-ÿ])(?=.*\\d)(?=.*[@$!%*?&#])[A-Za-zÀ-ÖØ-öø-ÿ\\d@$!%*?&#]{12,}$")) {
            throw new IllegalArgumentException("Le nouveau mot de passe ne respecte pas les critères de sécurité");
        }

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        // Vérifier que le nouveau mot de passe n'est pas le mot de passe actuel
        if (encoder.matches(newPassword, user.getPassword())) {
            throw new IllegalArgumentException("Le nouveau mot de passe ne peut pas être identique au mot de passe actuel.");
        }

        // Vérifier que le nouveau mot de passe n'est pas dans l'historique
        String currentHistory = user.getPasswordHistory() != null ? user.getPasswordHistory() : "";
        List<String> passwordHistory = PasswordHistoryTokenizer.tokenize(currentHistory);

        for (String oldHashedPassword : passwordHistory) {
            if (encoder.matches(newPassword, oldHashedPassword)) {
                throw new IllegalArgumentException("Ce mot de passe a déjà été utilisé récemment. Veuillez en choisir un autre.");
            }
        }

        String hashedNewPassword = encoder.encode(newPassword);

        // Ajouter l'ancien mot de passe à l'historique
        String updatedHistory = PasswordHistoryTokenizer.addPasswordToHistory(currentHistory, user.getPassword());

        // Mettre à jour le mot de passe
        user.setPassword(hashedNewPassword);
        user.setPasswordHistory(updatedHistory);
        user.setLastPasswordUpdateDate(LocalDate.now());

        // Supprimer le token de réinitialisation
        user.setResetPasswordToken(null);
        user.setResetTokenExpiryDate(null);

        userRepository.save(user);

        return true;
    }

    @Override
    public SecurityQuestions getSecurityQuestionForUser(User user) {
        UserSecurityAnswer answer = userSecurityAnswerRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("Aucune question de sécurité trouvée pour cet utilisateur"));
        return answer.getSecurityQuestion();
    }

    @Override
    public boolean verifySecurityAnswer(User user, String answer) {
        if (answer == null || answer.trim().isEmpty()) {
            return false;
        }

        UserSecurityAnswer securityAnswer = userSecurityAnswerRepository.findByUser(user)
                .orElse(null);

        if (securityAnswer == null) {
            return false;
        }

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        return encoder.matches(answer.toLowerCase().trim(), securityAnswer.getHashedAnswer());
    }
}
