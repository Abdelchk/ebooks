package fr.ensitech.ebooks.service;

import fr.ensitech.ebooks.entity.User;

public interface IVerificationTokenService {
	void createVerificationToken(User user, String token) throws Exception;
	String validateVerificationToken(String token) throws Exception;
}
