package fr.ensitech.ebooks;

import fr.ensitech.ebooks.entity.User;
import fr.ensitech.ebooks.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;

@SpringBootApplication
public class EbooksApplication {

	public static void main(String[] args) {
		SpringApplication.run(EbooksApplication.class, args);
	}
}
