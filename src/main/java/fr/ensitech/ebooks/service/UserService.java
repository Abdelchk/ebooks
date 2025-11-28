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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}
