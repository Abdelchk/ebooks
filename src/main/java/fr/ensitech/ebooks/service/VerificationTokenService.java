package fr.ensitech.ebooks.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import fr.ensitech.ebooks.entity.User;
import fr.ensitech.ebooks.repository.IUserRepository;

@Service
@RequiredArgsConstructor
public class VerificationTokenService {

    private final IUserRepository userRepository;

    public void createVerificationToken(User user, String token) {
        user.setVerificationToken(token);
        userRepository.save(user);
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
}
