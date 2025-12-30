package fr.ensitech.ebooks.service;

import fr.ensitech.ebooks.email.*;
import fr.ensitech.ebooks.entity.SecurityQuestions;
import fr.ensitech.ebooks.entity.User;
import fr.ensitech.ebooks.entity.UserSecurityAnswer;
import fr.ensitech.ebooks.entity.VerificationCode;
import fr.ensitech.ebooks.repository.ISecurityQuestionsRepository;
import fr.ensitech.ebooks.repository.IUserRepository;
import fr.ensitech.ebooks.repository.IUserSecurityAnswerRepository;
import fr.ensitech.ebooks.repository.IVerificationCodeRepository;
import fr.ensitech.ebooks.utils.Dates;
import fr.ensitech.ebooks.utils.PasswordEncoderFactory;
import fr.ensitech.ebooks.utils.PasswordHistoryTokenizer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
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

    private EmailContext emailContext;

    @Override
    public User addOrUpdateUser(User user) {
        if (user == null) {
            throw new NullPointerException("Le user à créer ne doit pas être NULL !");
        }
        if (user.getLastname() == null || user.getLastname().trim().isEmpty() || user.getFirstname() == null
                || user.getFirstname().trim().isEmpty() || user.getEmail() == null
                || user.getEmail().trim().isEmpty() || user.getPassword() == null
                || user.getPassword().trim().isEmpty() || user.getBirthdate() == null
                || user.getPhoneNumber() == null || user.getPhoneNumber().trim().isEmpty())
        {
            throw new IllegalArgumentException("Tous les paramètres sont obligatoires !");
        }

        // CAS 1 : Mise à jour (l'utilisateur a un ID)
        if (user.getId() != null) {
            User existingUser = userRepository.findById(user.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable avec l'ID " + user.getId()));

            // Mettre à jour uniquement les champs modifiables
            existingUser.setFirstname(user.getFirstname());
            existingUser.setLastname(user.getLastname());
            existingUser.setPhoneNumber(user.getPhoneNumber());
            existingUser.setBirthdate(user.getBirthdate());
            existingUser.setEmail(user.getEmail()); // Permet de changer l'email

            System.out.println("Mise à jour de l'utilisateur ID " + user.getId());

            // NE PAS toucher au mot de passe lors d'une mise à jour
            userRepository.save(existingUser);

            emailContext = new EmailContext();
            emailContext.setStrategy(new AccountUpdatedEmailStrategy(emailService));
            emailContext.executeStrategy(user.getEmail());

            return existingUser;
        }

        // CAS 2 : Création (pas d'ID) - Vérifier si l'email existe déjà
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Un utilisateur avec l'email " + user.getEmail() + " existe déjà");
        }

        // Créer un nouvel utilisateur
        PasswordEncoder argon2Encoder = PasswordEncoderFactory.getArgon2Encoder();

        user.setPassword(argon2Encoder.encode(user.getPassword()));
        user.setEnabled(false);
        user.setLastPasswordUpdateDate(LocalDate.now());

        String token = UUID.randomUUID().toString();
        user.setVerificationToken(token);
        userRepository.save(user);

        String activationlink = "http://localhost:8080/verify-email?token=" + token;

        emailContext = new EmailContext();
        emailContext.setStrategy(new ActivationEmailStrategy(emailService));
        emailContext.executeStrategy(user.getEmail(), activationlink);

        System.out.println("Nouvel utilisateur créé : " + user.getEmail());

        return user;
    }

    @Override
    public void deleteUser(Long id) throws Exception {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable avec l'ID " + id));
        user.setEnabled(false);
        userRepository.save(user);

        emailContext = new EmailContext();
        emailContext.setStrategy(new AccountDeactivatedEmailStrategy(emailService));
        emailContext.executeStrategy(user.getEmail());
    }


    @Override
    public UserSecurityAnswer addSecurityAnswer(User user, Long questionId, String securityAnswer) {
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("Utilisateur invalide");
        }
        if (securityAnswer == null || securityAnswer.trim().isEmpty()) {
            throw new IllegalArgumentException("La réponse de sécurité est obligatoire");
        }
        if (securityAnswer.length() > 32) {
            throw new IllegalArgumentException("La réponse ne doit pas dépasser 32 caractères");
        }

        PasswordEncoder bcryptEncoder = PasswordEncoderFactory.getBCryptEncoder();

        UserSecurityAnswer answer = new UserSecurityAnswer();
        answer.setUser(user);
        answer.setSecurityQuestion(securityQuestionsRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Question de sécurité invalide")));
        answer.setHashedAnswer(bcryptEncoder.encode(securityAnswer.toLowerCase().trim()));
        return userSecurityAnswerRepository.save(answer);
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

        emailContext = new EmailContext();
        emailContext.setStrategy(new AccountActivatedEmailStrategy(emailService));
        emailContext.executeStrategy(user.getEmail());

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

        // Mettre à jour la date d'envoi du dernier code
        user.setLastVerificationCodeSentAt(LocalDateTime.now());
        userRepository.save(user);

        // Envoyer le code par email
        emailContext = new EmailContext();
        emailContext.setStrategy(new DoubleAuthEmailStrategy(emailService));
        emailContext.executeStrategy(user.getEmail(), code);

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

        PasswordEncoder argon2Encoder = PasswordEncoderFactory.getArgon2Encoder();

        // Vérifier l'ancien mot de passe
        if (!argon2Encoder.matches(oldPassword, user.getPassword())) {
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
            if (argon2Encoder.matches(newPassword, oldHashedPassword)) {
                throw new IllegalArgumentException("Ce mot de passe a déjà été utilisé récemment. Veuillez en choisir un autre.");
            }
        }

        // Encoder le nouveau mot de passe après toutes les vérifications
        String hashedNewPassword = argon2Encoder.encode(newPassword);

        // Ajouter l'ancien mot de passe à l'historique
        String updatedHistory = PasswordHistoryTokenizer.addPasswordToHistory(currentHistory, user.getPassword());

        // Mettre à jour le mot de passe
        user.setPassword(hashedNewPassword);
        user.setPasswordHistory(updatedHistory);
        user.setLastPasswordUpdateDate(LocalDate.now());

        userRepository.save(user);

        emailContext = new EmailContext();
        emailContext.setStrategy(new PasswordUpdatedEmailStrategy(emailService));
        emailContext.executeStrategy(user.getEmail());

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
        String resetLink = "http://localhost:8080/reset-password?token=" + token;
        emailContext = new EmailContext();
        emailContext.setStrategy(new ForgotPasswordEmailStrategy(emailService));
        emailContext.executeStrategy(user.getEmail(), resetLink);
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

        PasswordEncoder argon2Encoder = PasswordEncoderFactory.getArgon2Encoder();

        // Vérifier que le nouveau mot de passe n'est pas le mot de passe actuel
        if (argon2Encoder.matches(newPassword, user.getPassword())) {
            throw new IllegalArgumentException("Le nouveau mot de passe ne peut pas être identique au mot de passe actuel.");
        }

        // Vérifier que le nouveau mot de passe n'est pas dans l'historique
        String currentHistory = user.getPasswordHistory() != null ? user.getPasswordHistory() : "";
        List<String> passwordHistory = PasswordHistoryTokenizer.tokenize(currentHistory);

        for (String oldHashedPassword : passwordHistory) {
            if (argon2Encoder.matches(newPassword, oldHashedPassword)) {
                throw new IllegalArgumentException("Ce mot de passe a déjà été utilisé récemment. Veuillez en choisir un autre.");
            }
        }

        String hashedNewPassword = argon2Encoder.encode(newPassword);

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

        PasswordEncoder bcryptEncoder = PasswordEncoderFactory.getBCryptEncoder();
        return bcryptEncoder.matches(answer.toLowerCase().trim(), securityAnswer.getHashedAnswer());
    }
}
