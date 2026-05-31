package fr.ensitech.ebooks.data;

import fr.ensitech.ebooks.entity.SecurityQuestions;
import fr.ensitech.ebooks.entity.User;
import fr.ensitech.ebooks.enums.SecurityQuestionEnum;
import fr.ensitech.ebooks.repository.ISecurityQuestionsRepository;
import fr.ensitech.ebooks.repository.IUserRepository;
import fr.ensitech.ebooks.utils.PasswordEncoderFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Configuration
@Profile("!test")  // Ne pas exécuter en mode test (H2 CI)
public class DataInitializer {

    /** Encapsule la configuration d'un utilisateur privilégié (évite > 7 paramètres). */
    private record PrivilegedUserConfig(
            String email,
            String firstname,
            String lastname,
            String role,
            String phone,
            String rawPassword
    ) {}

    @Bean
    CommandLineRunner initDatabase(ISecurityQuestionsRepository repository) {
        return args -> {
            if (repository.count() == 0) {
                List<SecurityQuestions> questions = Arrays.stream(SecurityQuestionEnum.values())
                        .map(this::createQuestion)
                        .collect(Collectors.toList());
                repository.saveAll(questions);
            }
        };
    }

    @Bean
    CommandLineRunner initPrivilegedUsers(IUserRepository userRepository) {
        return args -> {
            PasswordEncoder argon2 = PasswordEncoderFactory.getArgon2Encoder();

            String adminPassword = readEnvOrDefault("EBOOKS_ADMIN_PASSWORD", "Admin@2024!");
            String librarianPassword = readEnvOrDefault("EBOOKS_LIBRARIAN_PASSWORD", "Librarian@2024!");

            upsertPrivilegedUser(userRepository, argon2,
                    new PrivilegedUserConfig("admin@ebooks.fr", "Admin", "Systeme", "admin", "0600000001", adminPassword));

            upsertPrivilegedUser(userRepository, argon2,
                    new PrivilegedUserConfig("librarian@ebooks.fr", "Bibliothecaire", "Principal", "librarian", "0600000002", librarianPassword));
        };
    }

    private SecurityQuestions createQuestion(SecurityQuestionEnum questionEnum) {
        SecurityQuestions sq = new SecurityQuestions();
        sq.setQuestion(questionEnum.getQuestion());
        return sq;
    }

    private String readEnvOrDefault(String envName, String defaultValue) {
        String value = System.getenv(envName);
        return (value == null || value.isBlank()) ? defaultValue : value;
    }

    private void upsertPrivilegedUser(IUserRepository userRepository, PasswordEncoder argon2, PrivilegedUserConfig config) {
        Optional<User> existing = userRepository.findByEmail(config.email());

        if (existing.isPresent()) {
            User user = existing.get();
            boolean changed = false;

            if (!config.role().equalsIgnoreCase(user.getRole())) {
                user.setRole(config.role());
                changed = true;
            }
            if (!user.isEnabled()) {
                user.setEnabled(true);
                changed = true;
            }

            // Toujours re-encoder le mot de passe au démarrage pour garantir que :
            // 1. Le mot de passe correspond bien à la configuration actuelle
            // 2. Le hash utilise les paramètres Argon2 les plus récents
            // Note : on évite argon2.matches() (lent) — on re-encode directement.
            String newHash = argon2.encode(config.rawPassword());
            user.setPassword(newHash);
            user.setLastPasswordUpdateDate(LocalDate.now());
            changed = true;

            if (changed) {
                userRepository.save(user);
            }
            return;
        }

        User user = User.builder()
                .firstname(config.firstname())
                .lastname(config.lastname())
                .email(config.email())
                .password(argon2.encode(config.rawPassword()))
                .birthdate(LocalDate.of(1990, 1, 1))
                .phoneNumber(config.phone())
                .role(config.role())
                .enabled(true)
                .verificationToken("VERIFIED")
                .passwordHistory("")
                .lastPasswordUpdateDate(LocalDate.now())
                .build();

        userRepository.save(user);
    }
}
