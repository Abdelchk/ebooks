package fr.ensitech.ebooks.service;

import fr.ensitech.ebooks.entity.SecurityQuestions;
import org.springframework.stereotype.Service;

import fr.ensitech.ebooks.entity.User;

import java.util.List;

@Service
public interface IUserService {
	User registerNewUserAccount(User user, Long questionId, String securityAnswer) throws Exception;
	String validateVerificationToken(String token) throws Exception;

    List<SecurityQuestions> getAllSecurityQuestions();
}
