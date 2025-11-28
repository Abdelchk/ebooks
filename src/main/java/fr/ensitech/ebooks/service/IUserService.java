package fr.ensitech.ebooks.service;

import fr.ensitech.ebooks.entity.SecurityQuestions;
import org.springframework.stereotype.Service;

import fr.ensitech.ebooks.entity.User;

import java.util.List;
import java.util.Optional;

@Service
public interface IUserService {
	User registerNewUserAccount(User user, Long questionId, String securityAnswer) throws Exception;
    Optional<User> findByEmail(String email);
	String validateVerificationToken(String token) throws Exception;
    List<SecurityQuestions> getAllSecurityQuestions();
    String generateVerificationCode(User user);
    boolean validateVerificationCode(User user, String code);

}
