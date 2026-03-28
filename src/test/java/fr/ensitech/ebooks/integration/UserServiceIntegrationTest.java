package fr.ensitech.ebooks.integration;

import fr.ensitech.ebooks.entity.SecurityQuestions;
import fr.ensitech.ebooks.entity.User;
import fr.ensitech.ebooks.entity.UserSecurityAnswer;
import fr.ensitech.ebooks.entity.VerificationCode;
import fr.ensitech.ebooks.repository.ISecurityQuestionsRepository;
import fr.ensitech.ebooks.repository.IUserRepository;
import fr.ensitech.ebooks.repository.IUserSecurityAnswerRepository;
import fr.ensitech.ebooks.repository.IVerificationCodeRepository;
import fr.ensitech.ebooks.service.UserService;
import jakarta.transaction.Transactional;
import lombok.SneakyThrows;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests d'intégration pour UserService
 * Ce test vérifie le bon fonctionnement conjoint :
 *      - service
 *      - repository
 *      - base de données de tests
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class UserServiceIntegrationTest {

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private ISecurityQuestionsRepository securityQuestionsRepository;

    @Autowired
    private IUserSecurityAnswerRepository userSecurityAnswerRepository;

    @Autowired
    private IVerificationCodeRepository verificationCodeRepository;

    @Autowired
    private UserService userService;

    private User user;
    private SecurityQuestions securityQuestion;
    private String securityAnswerText;

    @BeforeEach
    void setUp() {
        // Créer ou récupérer une question de sécurité
        securityQuestion = new SecurityQuestions();
        securityQuestion.setQuestion("Quel est le nom de votre animal de compagnie ?");
        securityQuestion = securityQuestionsRepository.save(securityQuestion);

        securityAnswerText = "fluffy";

        // Créer un utilisateur pour les tests
        user = User.builder()
                .firstname("Jean")
                .lastname("Dupont")
                .email("jean.dupont.integration@test.com")
                .password("Password123!@")
                .birthdate(LocalDate.of(1990, 1, 1))
                .phoneNumber("0612345678")
                .enabled(false)
                .role("client")
                .passwordHistory("")
                .lastPasswordUpdateDate(LocalDate.now())
                .build();
    }

    @AfterEach
    void tearDown() {
        // Nettoyage : supprimer les données créées pendant le test
        if (user != null && user.getId() != null) {
            // Supprimer les codes de vérification
            verificationCodeRepository.findAll().stream()
                    .filter(vc -> vc.getUserId() != null && vc.getUserId().getId().equals(user.getId()))
                    .forEach(vc -> verificationCodeRepository.delete(vc));

            // Supprimer les réponses de sécurité
            userSecurityAnswerRepository.findByUserId(user.getId())
                    .ifPresent(answer -> userSecurityAnswerRepository.delete(answer));

            // Supprimer l'utilisateur
            userRepository.deleteById(user.getId());
        }

        user = null;
        securityQuestion = null;
    }

    // ============ TESTS D'INSCRIPTION D'UTILISATEUR ============

    @Test
    @DisplayName("Test d'intégration - Inscription d'un utilisateur complet avec question de sécurité")
    @SneakyThrows
    void shouldRegisterNewUserAccountInDatabase() {
        // GIVEN
        // setUp()

        // WHEN
        User registeredUser = userService.addOrUpdateUser(user);
        userService.addSecurityAnswer(registeredUser, securityQuestion.getId(), securityAnswerText);

        // THEN
        assertThat(registeredUser).isNotNull();
        assertThat(registeredUser.getId()).isNotNull();
        assertThat(registeredUser.getId()).isGreaterThan(0);

        // Vérifier que l'utilisateur est bien en base
        Optional<User> userInDb = userRepository.findById(registeredUser.getId());
        assertThat(userInDb).isPresent();
        assertThat(userInDb.get().getEmail()).isEqualTo("jean.dupont.integration@test.com");
        assertThat(userInDb.get().isEnabled()).isFalse();
        assertThat(userInDb.get().getVerificationToken()).isNotNull();
        assertThat(userInDb.get().getPassword()).isNotEqualTo("Password123!@"); // Le mot de passe doit être hashé

        // Vérifier que la réponse de sécurité est bien enregistrée
        Optional<UserSecurityAnswer> securityAnswer = userSecurityAnswerRepository.findByUserId(registeredUser.getId());
        assertThat(securityAnswer).isPresent();
        assertThat(securityAnswer.get().getSecurityQuestion().getId()).isEqualTo(securityQuestion.getId());
        assertThat(securityAnswer.get().getHashedAnswer()).isNotEqualTo(securityAnswerText); // La réponse doit être hashée

        // Mise à jour de la référence pour le nettoyage
        user = registeredUser;
    }

    @Test
    @DisplayName("Test d'intégration - Échec de l'inscription avec un email déjà existant")
    @SneakyThrows
    void shouldThrowExceptionWhenRegisteringUserWithExistingEmail() {
        // GIVEN
        user = userService.addOrUpdateUser(user);

        User duplicateUser = User.builder()
                .firstname("Jacques")
                .lastname("Martin")
                .email("jean.dupont.integration@test.com") // Même email
                .password("DifferentPass123!@")
                .birthdate(LocalDate.of(1992, 5, 15))
                .phoneNumber("0698765432")
                .build();

        // WHEN / THEN
        assertThatThrownBy(() -> userService.addOrUpdateUser(duplicateUser))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Un utilisateur avec l'email " + duplicateUser.getEmail() + " existe déjà");
    }

    // ============ TESTS DE VÉRIFICATION D'EMAIL ============

    @Test
    @DisplayName("Test d'intégration - Validation du token de vérification")
    @SneakyThrows
    void shouldValidateVerificationTokenInDatabase() {
        // GIVEN
        User registeredUser = userService.addOrUpdateUser(user);
        userService.addSecurityAnswer(registeredUser, securityQuestion.getId(), securityAnswerText);
        user = registeredUser; // Pour le nettoyage
        String token = registeredUser.getVerificationToken();

        assertThat(registeredUser.isEnabled()).isFalse();

        // WHEN
        String result = userService.validateVerificationToken(token);

        // THEN
        assertThat(result).isEqualTo("valid");

        // Vérifier que l'utilisateur est maintenant activé en base
        Optional<User> activatedUser = userRepository.findById(registeredUser.getId());
        assertThat(activatedUser).isPresent();
        assertThat(activatedUser.get().isEnabled()).isTrue();
    }

    @Test
    @DisplayName("Test d'intégration - Échec de validation avec un token invalide")
    void shouldReturnInvalidForNonExistentToken() {
        // GIVEN
        String invalidToken = "token-inexistant-123456";

        // WHEN
        String result = userService.validateVerificationToken(invalidToken);

        // THEN
        assertThat(result).isEqualTo("invalid");
    }

    // ============ TESTS DE GÉNÉRATION DE CODE DE VÉRIFICATION ============

    @Test
    @DisplayName("Test d'intégration - Génération d'un code de vérification 2FA")
    @SneakyThrows
    void shouldGenerateVerificationCodeInDatabase() {
        // GIVEN
        User registeredUser = userService.addOrUpdateUser(user);
        userService.addSecurityAnswer(registeredUser, securityQuestion.getId(), securityAnswerText);
        user = registeredUser; // Pour le nettoyage

        // WHEN
        String code = userService.generateVerificationCode(registeredUser);

        // THEN
        assertThat(code).isNotNull();
        assertThat(code).matches("^[0-9]{6}$");

        // Vérifier que le code est bien en base
        List<VerificationCode> codes = verificationCodeRepository.findAll();
        Optional<VerificationCode> generatedCode = codes.stream()
                .filter(vc -> vc.getUserId().getId().equals(registeredUser.getId()) && vc.getCode().equals(code))
                .findFirst();

        assertThat(generatedCode).isPresent();
        assertThat(generatedCode.get().isUsed()).isFalse();
        assertThat(generatedCode.get().getExpiryDate()).isAfter(LocalDateTime.now());
    }

    @Test
    @DisplayName("Test d'intégration - Validation d'un code de vérification valide")
    @SneakyThrows
    void shouldValidateVerificationCodeSuccessfully() {
        // GIVEN
        User registeredUser = userService.addOrUpdateUser(user);
        userService.addSecurityAnswer(registeredUser, securityQuestion.getId(), securityAnswerText);
        user = registeredUser; // Pour le nettoyage
        String code = userService.generateVerificationCode(registeredUser);

        // WHEN
        boolean isValid = userService.validateVerificationCode(registeredUser, code);

        // THEN
        assertThat(isValid).isTrue();

        // Vérifier que le code est marqué comme utilisé
        List<VerificationCode> codes = verificationCodeRepository.findAll();
        Optional<VerificationCode> usedCode = codes.stream()
                .filter(vc -> vc.getUserId().getId().equals(registeredUser.getId()) && vc.getCode().equals(code))
                .findFirst();

        assertThat(usedCode).isPresent();
        assertThat(usedCode.get().isUsed()).isTrue();
    }

    @Test
    @DisplayName("Test d'intégration - Échec de validation avec un code incorrect")
    @SneakyThrows
    void shouldFailToValidateIncorrectVerificationCode() {
        // GIVEN
        User registeredUser = userService.addOrUpdateUser(user);
        userService.addSecurityAnswer(registeredUser, securityQuestion.getId(), securityAnswerText);
        user = registeredUser; // Pour le nettoyage
        userService.generateVerificationCode(registeredUser);

        // WHEN
        boolean isValid = userService.validateVerificationCode(registeredUser, "000000");

        // THEN
        assertThat(isValid).isFalse();
    }

    // ============ TESTS DE RECHERCHE D'UTILISATEUR ============

    @Test
    @DisplayName("Test d'intégration - Recherche d'un utilisateur par email")
    @SneakyThrows
    void shouldFindUserByEmailInDatabase() {
        // GIVEN
        User registeredUser = userService.addOrUpdateUser(user);
        userService.addSecurityAnswer(registeredUser, securityQuestion.getId(), securityAnswerText);
        user = registeredUser; // Pour le nettoyage

        // WHEN
        Optional<User> foundUser = userService.findByEmail("jean.dupont.integration@test.com");

        // THEN
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getId()).isEqualTo(registeredUser.getId());
        assertThat(foundUser.get().getFirstname()).isEqualTo("Jean");
        assertThat(foundUser.get().getLastname()).isEqualTo("Dupont");
    }

    @Test
    @DisplayName("Test d'intégration - Recherche d'un utilisateur inexistant")
    void shouldReturnEmptyForNonExistentEmail() {
        // GIVEN
        String nonExistentEmail = "utilisateur.inexistant@test.com";

        // WHEN
        Optional<User> foundUser = userService.findByEmail(nonExistentEmail);

        // THEN
        assertThat(foundUser).isEmpty();
    }

    // ============ TESTS DE SUPPRESSION D'UTILISATEUR ============

    @Test
    @DisplayName("Test d'intégration - Désactivation d'un utilisateur")
    @SneakyThrows
    void shouldDeactivateUserInDatabase() {
        // GIVEN
        User registeredUser = userService.addOrUpdateUser(user);
        userService.addSecurityAnswer(registeredUser, securityQuestion.getId(), securityAnswerText);
        user = registeredUser; // Pour le nettoyage

        // Activer l'utilisateur d'abord
        userService.validateVerificationToken(registeredUser.getVerificationToken());

        // WHEN
        userService.deleteUser(registeredUser.getId());

        // THEN
        Optional<User> deactivatedUser = userRepository.findById(registeredUser.getId());
        assertThat(deactivatedUser).isPresent();
        assertThat(deactivatedUser.get().isEnabled()).isFalse();
    }

    @Test
    @DisplayName("Test d'intégration - Échec de suppression d'un utilisateur inexistant")
    void shouldThrowExceptionWhenDeletingNonExistentUser() {
        // GIVEN
        Long nonExistentId = 999999L;

        // WHEN / THEN
        assertThatThrownBy(() -> userService.deleteUser(nonExistentId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Utilisateur introuvable avec l'ID " + nonExistentId);
    }

    // ============ TESTS DE VÉRIFICATION DE LA RÉPONSE DE SÉCURITÉ ============

    @Test
    @DisplayName("Test d'intégration - Vérification d'une réponse de sécurité correcte")
    @SneakyThrows
    void shouldVerifySecurityAnswerSuccessfully() {
        // GIVEN
        User registeredUser = userService.addOrUpdateUser(user);
        userService.addSecurityAnswer(registeredUser, securityQuestion.getId(), securityAnswerText);
        user = registeredUser; // Pour le nettoyage

        // WHEN
        boolean isValid = userService.verifySecurityAnswer(registeredUser, securityAnswerText);

        // THEN
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Test d'intégration - Échec de vérification avec une réponse incorrecte")
    @SneakyThrows
    void shouldFailToVerifyIncorrectSecurityAnswer() {
        // GIVEN
        User registeredUser = userService.addOrUpdateUser(user);
        userService.addSecurityAnswer(registeredUser, securityQuestion.getId(), securityAnswerText);
        user = registeredUser; // Pour le nettoyage

        // WHEN
        boolean isValid = userService.verifySecurityAnswer(registeredUser, "mauvaise-reponse");

        // THEN
        assertThat(isValid).isFalse();
    }

    // ============ TESTS DE RÉCUPÉRATION DE LISTE ============

    @Test
    @DisplayName("Test d'intégration - Récupération de toutes les questions de sécurité")
    void shouldGetAllSecurityQuestions() {
        // GIVEN
        // La question est créée dans setUp()

        // WHEN
        List<SecurityQuestions> questions = userService.getAllSecurityQuestions();

        // THEN
        assertThat(questions).isNotEmpty();
        assertThat(questions).hasSizeGreaterThan(0);
    }

    @Test
    @DisplayName("Test d'intégration - Récupération de la question de sécurité d'un utilisateur")
    @SneakyThrows
    void shouldGetSecurityQuestionForUser() {
        // GIVEN
        User registeredUser = userService.addOrUpdateUser(user);
        userService.addSecurityAnswer(registeredUser, securityQuestion.getId(), securityAnswerText);
        user = registeredUser; // Pour le nettoyage

        // WHEN
        SecurityQuestions question = userService.getSecurityQuestionForUser(registeredUser);

        // THEN
        assertThat(question).isNotNull();
        assertThat(question.getId()).isEqualTo(securityQuestion.getId());
        assertThat(question.getQuestion()).isEqualTo("Quel est le nom de votre animal de compagnie ?");
    }
}

