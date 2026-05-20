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

            upsertPrivilegedUser(
                    userRepository,
                    argon2,
                    "admin@ebooks.fr",
                    "Admin",
                    "Systeme",
                    "admin",
                    "0600000001",
                    adminPassword
            );

            upsertPrivilegedUser(
                    userRepository,
                    argon2,
                    "librarian@ebooks.fr",
                    "Bibliothecaire",
                    "Principal",
                    "librarian",
                    "0600000002",
                    librarianPassword
            );
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

    private void upsertPrivilegedUser(
            IUserRepository userRepository,
            PasswordEncoder argon2,
            String email,
            String firstname,
            String lastname,
            String role,
            String phone,
            String rawPassword
    ) {
        Optional<User> existing = userRepository.findByEmail(email);

        if (existing.isPresent()) {
            User user = existing.get();
            boolean changed = false;

            if (!role.equalsIgnoreCase(user.getRole())) {
                user.setRole(role);
                changed = true;
            }
            if (!user.isEnabled()) {
                user.setEnabled(true);
                changed = true;
            }
            if (!argon2.matches(rawPassword, user.getPassword())) {
                user.setPassword(argon2.encode(rawPassword));
                user.setLastPasswordUpdateDate(LocalDate.now());
                changed = true;
            }

            if (changed) {
                userRepository.save(user);
            }
            return;
        }

        User user = User.builder()
                .firstname(firstname)
                .lastname(lastname)
                .email(email)
                .password(argon2.encode(rawPassword))
                .birthdate(LocalDate.of(1990, 1, 1))
                .phoneNumber(phone)
                .role(role)
                .enabled(true)
                .verificationToken("VERIFIED")
                .passwordHistory("")
                .lastPasswordUpdateDate(LocalDate.now())
                .build();

        userRepository.save(user);
    }
}
