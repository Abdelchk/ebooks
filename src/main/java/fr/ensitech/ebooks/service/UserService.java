package fr.ensitech.ebooks.service;

import fr.ensitech.ebooks.entity.SecurityQuestions;
import fr.ensitech.ebooks.entity.User;
import fr.ensitech.ebooks.entity.UserSecurityAnswer;
import fr.ensitech.ebooks.repository.ISecurityQuestionsRepository;
import fr.ensitech.ebooks.repository.IUserRepository;
import fr.ensitech.ebooks.repository.IUserSecurityAnswerRepository;
import fr.ensitech.ebooks.utils.Dates;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
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
        emailService.sendEmail(user.getEmail(), "Vérification Email",
                "Cliquez sur le lien pour vérifier l'Email : " + confirmationUrl);

        return user;
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
}
