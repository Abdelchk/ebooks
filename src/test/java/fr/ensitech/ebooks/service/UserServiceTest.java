package fr.ensitech.ebooks.service;

import fr.ensitech.ebooks.entity.SecurityQuestions;
import fr.ensitech.ebooks.entity.User;
import fr.ensitech.ebooks.entity.UserSecurityAnswer;
import fr.ensitech.ebooks.entity.VerificationCode;
import fr.ensitech.ebooks.repository.ISecurityQuestionsRepository;
import fr.ensitech.ebooks.repository.IUserRepository;
import fr.ensitech.ebooks.repository.IUserSecurityAnswerRepository;
import fr.ensitech.ebooks.repository.IVerificationCodeRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour la classe UserService
 */
@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private IUserRepository userRepository;

    @Mock
    private ISecurityQuestionsRepository securityQuestionsRepository;

    @Mock
    private IUserSecurityAnswerRepository userSecurityAnswerRepository;

    @Mock
    private IVerificationCodeRepository verificationCodeRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private UserService userService;

    private User user;
    private SecurityQuestions securityQuestion;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(null)
                .firstname("Jean")
                .lastname("Dupont")
                .email("jean.dupont@test.com")
                .password("Password123!")
                .birthdate(LocalDate.of(1990, 1, 1))
                .phoneNumber("0612345678")
                .enabled(false)
                .role("client")
                .passwordHistory("")
                .lastPasswordUpdateDate(LocalDate.now())
                .build();

        securityQuestion = new SecurityQuestions();
        securityQuestion.setId(1L);
        securityQuestion.setQuestion("Quel est le nom de votre animal de compagnie ?");
    }

    @AfterEach
    void tearDown() {
        user = null;
        securityQuestion = null;
    }

    // ============ TESTS DE CRÉATION D'UTILISATEUR ============

    @Test
    void shouldCreateUserSuccessfully() {
        // GIVEN
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setId(1L);
            return savedUser;
        });

        // WHEN
        User createdUser = userService.addOrUpdateUser(user);

        // THEN
        assertThat(createdUser).isNotNull();
        assertThat(createdUser.getId()).isEqualTo(1L);
        assertThat(createdUser.getFirstname()).isEqualTo("Jean");
        assertThat(createdUser.getLastname()).isEqualTo("Dupont");
        assertThat(createdUser.getEmail()).isEqualTo("jean.dupont@test.com");
        assertThat(createdUser.isEnabled()).isFalse();
        assertThat(createdUser.getPassword()).isNotEqualTo("Password123!"); // Mot de passe doit être encodé
        assertThat(createdUser.getVerificationToken()).isNotNull();

        verify(userRepository).findByEmail(user.getEmail());
        verify(userRepository).save(any(User.class));
        verify(emailService).sendEmail(eq(user.getEmail()), anyString(), anyString());
    }

    @Test
    void shouldThrowExceptionWhenCreatingUserWithExistingEmail() {
        // GIVEN
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        // WHEN / THEN
        assertThatThrownBy(() -> userService.addOrUpdateUser(user))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Un utilisateur avec l'email " + user.getEmail() + " existe déjà");

        verify(userRepository).findByEmail(user.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldThrowExceptionWhenCreatingUserWithNullValue() {
        // GIVEN
        User invalidUser = null;

        // WHEN / THEN
        assertThatThrownBy(() -> userService.addOrUpdateUser(invalidUser))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Le user à créer ne doit pas être NULL !");
    }

    @Test
    void shouldThrowExceptionWhenCreatingUserWithMissingFirstname() {
        // GIVEN
        user.setFirstname(null);

        // WHEN / THEN
        assertThatThrownBy(() -> userService.addOrUpdateUser(user))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Tous les paramètres sont obligatoires !");
    }

    @Test
    void shouldThrowExceptionWhenCreatingUserWithEmptyEmail() {
        // GIVEN
        user.setEmail("");

        // WHEN / THEN
        assertThatThrownBy(() -> userService.addOrUpdateUser(user))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Tous les paramètres sont obligatoires !");
    }

    // ============ TESTS DE MISE À JOUR D'UTILISATEUR ============

    @Test
    void shouldUpdateUserSuccessfully() {
        // GIVEN
        User existingUser = User.builder()
                .id(1L)
                .firstname("Jean")
                .lastname("Dupont")
                .email("jean.dupont@test.com")
                .password("HashedPassword123")
                .birthdate(LocalDate.of(1990, 1, 1))
                .phoneNumber("0612345678")
                .enabled(true)
                .build();

        User updatedUserData = User.builder()
                .id(1L)
                .firstname("Jacques")
                .lastname("Martin")
                .email("jacques.martin@test.com")
                .password("HashedPassword123")
                .birthdate(LocalDate.of(1992, 5, 15))
                .phoneNumber("0698765432")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // WHEN
        User result = userService.addOrUpdateUser(updatedUserData);

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getFirstname()).isEqualTo("Jacques");
        assertThat(result.getLastname()).isEqualTo("Martin");
        assertThat(result.getEmail()).isEqualTo("jacques.martin@test.com");
        assertThat(result.getPhoneNumber()).isEqualTo("0698765432");
        assertThat(result.getBirthdate()).isEqualTo(LocalDate.of(1992, 5, 15));
        assertThat(result.getPassword()).isEqualTo("HashedPassword123"); // Le mot de passe ne doit pas changer

        verify(userRepository).findById(1L);
        verify(userRepository).save(any(User.class));
        verify(emailService).sendEmail(eq("jacques.martin@test.com"), anyString(), anyString());
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentUser() {
        // GIVEN
        user.setId(999L);
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // WHEN / THEN
        assertThatThrownBy(() -> userService.addOrUpdateUser(user))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Utilisateur introuvable avec l'ID 999");

        verify(userRepository).findById(999L);
        verify(userRepository, never()).save(any(User.class));
    }

    // ============ TESTS DE SUPPRESSION D'UTILISATEUR ============

    @Test
    void shouldDeleteUserSuccessfully() throws Exception {
        // GIVEN
        user.setId(1L);
        user.setEnabled(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // WHEN
        userService.deleteUser(1L);

        // THEN
        assertThat(user.isEnabled()).isFalse();
        verify(userRepository).findById(1L);
        verify(userRepository).save(user);
        verify(emailService).sendEmail(eq(user.getEmail()), anyString(), anyString());
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentUser() {
        // GIVEN
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // WHEN / THEN
        assertThatThrownBy(() -> userService.deleteUser(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Utilisateur introuvable avec l'ID 999");

        verify(userRepository).findById(999L);
        verify(userRepository, never()).save(any(User.class));
    }

    // ============ TESTS DE RECHERCHE D'UTILISATEUR ============

    @Test
    void shouldFindUserByEmailSuccessfully() {
        // GIVEN
        user.setId(1L);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        // WHEN
        Optional<User> foundUser = userService.findByEmail(user.getEmail());

        // THEN
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo("jean.dupont@test.com");
        verify(userRepository).findByEmail(user.getEmail());
    }

    @Test
    void shouldReturnEmptyWhenUserNotFoundByEmail() {
        // GIVEN
        when(userRepository.findByEmail("nonexistent@test.com")).thenReturn(Optional.empty());

        // WHEN
        Optional<User> foundUser = userService.findByEmail("nonexistent@test.com");

        // THEN
        assertThat(foundUser).isEmpty();
        verify(userRepository).findByEmail("nonexistent@test.com");
    }

    // ============ TESTS DE VÉRIFICATION DE TOKEN ============

    @Test
    void shouldValidateVerificationTokenSuccessfully() {
        // GIVEN
        String token = "valid-token-123";
        user.setId(1L);
        user.setVerificationToken(token);
        user.setEnabled(false);

        when(userRepository.findByVerificationToken(token)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // WHEN
        String result = userService.validateVerificationToken(token);

        // THEN
        assertThat(result).isEqualTo("valid");
        assertThat(user.isEnabled()).isTrue();
        verify(userRepository).findByVerificationToken(token);
        verify(userRepository).save(user);
        verify(emailService).sendEmail(eq(user.getEmail()), anyString(), anyString());
    }

    @Test
    void shouldReturnInvalidForNonExistentToken() {
        // GIVEN
        String token = "invalid-token";
        when(userRepository.findByVerificationToken(token)).thenReturn(Optional.empty());

        // WHEN
        String result = userService.validateVerificationToken(token);

        // THEN
        assertThat(result).isEqualTo("invalid");
        verify(userRepository).findByVerificationToken(token);
        verify(userRepository, never()).save(any(User.class));
    }


}
