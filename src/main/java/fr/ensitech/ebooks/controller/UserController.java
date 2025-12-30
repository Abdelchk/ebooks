package fr.ensitech.ebooks.controller;

import fr.ensitech.ebooks.entity.SecurityQuestions;
import fr.ensitech.ebooks.entity.User;
import fr.ensitech.ebooks.entity.UserSecurityAnswer;
import fr.ensitech.ebooks.repository.IUserRepository;
import fr.ensitech.ebooks.service.IUserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin("http://localhost:8080")
@RequestMapping("/api/rest/users")
public class UserController implements IUserController {

    @Autowired
    private IUserService userService;

    @Override
    @PostMapping("/create")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseEntity<User> createUser(@Valid @RequestBody CreateUserRequest request) {
        try {
            User user = new User();
            user.setFirstname(request.getFirstname());
            user.setLastname(request.getLastname());
            user.setEmail(request.getEmail());
            user.setPassword(request.getPassword());
            user.setBirthdate(request.getBirthdate());
            user.setPhoneNumber(request.getPhoneNumber());

            User createdUser = userService.addOrUpdateUser(user);

            createdUser.setPassword(null);
            createdUser.setPasswordHistory(null);
            createdUser.setResetPasswordToken(null);

            UserSecurityAnswer securityAnswer = new UserSecurityAnswer();
            SecurityQuestions question = new SecurityQuestions();
            question.setId(request.getQuestionId());
            question.setQuestion(request.getSecurityQuestion());
            securityAnswer.setSecurityQuestion(question);
            securityAnswer.setHashedAnswer(request.getSecurityAnswer());
            securityAnswer.setUser(createdUser);

            return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Override
    @PutMapping("/update")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseEntity<String> updateUser(
            @RequestBody UpdateUserRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User existingUser = userService.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé"));

            // Mise à jour des champs via une méthode utilitaire
            updateIfNotBlank(request.getFirstname(), existingUser::setFirstname);
            updateIfNotBlank(request.getLastname(), existingUser::setLastname);
            updateIfNotBlank(request.getEmail(), existingUser::setEmail);
            updateIfNotBlank(request.getPhoneNumber(), existingUser::setPhoneNumber);

            if (request.getBirthdate() != null) {
                existingUser.setBirthdate(request.getBirthdate());
            }

            userService.addOrUpdateUser(existingUser);

            return ResponseEntity.ok("Utilisateur mis à jour avec succès");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Erreur de validation : " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur : " + e.getMessage());
        }
    }

    /**
     * Met à jour un champ uniquement si la valeur n'est pas vide
     */
    private void updateIfNotBlank(String value, java.util.function.Consumer<String> setter) {
        if (value != null && !value.trim().isEmpty()) {
            setter.accept(value);
        }
    }


    @Override
    @PostMapping("/check-security-question")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseEntity<String> checkSecurityQuestion(@RequestBody SecurityQuestionRequest request) {
        try {
            Optional<User> userOpt = userService.findByEmail(request.getEmail());
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Utilisateur non trouvé");
            }

            User user = userOpt.get();

            boolean isAnswerCorrect = userService.verifySecurityAnswer(user, request.getSecurityAnswer());

            if (isAnswerCorrect) {
                return ResponseEntity.ok("Réponse correcte");
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Réponse incorrecte");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur : " + e.getMessage());
        }
    }

    @Override
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteUser(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Optional<User> userOpt = userService.findByEmail(userDetails.getUsername());
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Utilisateur non trouvé");
            }

            User user = userOpt.get();

            if (!user.getId().equals(id)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Vous ne pouvez supprimer que votre propre compte");
            }

            userService.deleteUser(id);

            return ResponseEntity.ok("Compte supprimé avec succès");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur : " + e.getMessage());
        }
    }

    @Override
    @GetMapping("/activate")
    public ResponseEntity<User> activateUser(@RequestParam String activationLink) {
        try {
            String token = activationLink.contains("token=")
                    ? activationLink.substring(activationLink.lastIndexOf("=") + 1)
                    : activationLink;

            String result = userService.validateVerificationToken(token);

            if ("valid".equals(result)) {
                return ResponseEntity.ok(null);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // Endpoints additionnels utiles

    @Override
    @PutMapping("/change-password")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseEntity<String> changePassword(
            @RequestBody PasswordChangeRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Optional<User> userOpt = userService.findByEmail(userDetails.getUsername());
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Utilisateur non trouvé");
            }

            User user = userOpt.get();

            boolean success = userService.updatePassword(
                    user,
                    request.getOldPassword(),
                    request.getNewPassword(),
                    request.getConfirmPassword(),
                    request.getQuestionId(),
                    request.getSecurityAnswer()
            );

            if (success) {
                return ResponseEntity.ok("Mot de passe modifié avec succès");
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Erreur lors du changement de mot de passe");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur : " + e.getMessage());
        }
    }

    @GetMapping("/me")
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseEntity<User> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            Optional<User> userOpt = userService.findByEmail(userDetails.getUsername());
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }

            User user = userOpt.get();
            user.setPassword(null);
            user.setPasswordHistory(null);
            user.setResetPasswordToken(null);

            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // Classes DTO
    @Setter
    @Getter
    public static class CreateUserRequest {
        // Getters et setters
        @NotEmpty(message = "Le prénom est obligatoire !")
        @Length(min = 2, message = "Le prénom doit être constitué de 2 à 48 caractères !")
        @Pattern(regexp = "^[a-zA-ZÀ-ÿ\\s-]+$", message = "Le prénom ne doit contenir que des lettres !")
        private String firstname;

        @NotEmpty(message = "Le nom est obligatoire !")
        @Length(min = 2, message = "Le nom doit être constitué de 2 à 48 caractères !")
        @Pattern(regexp = "^[a-zA-ZÀ-ÿ\\s-]+$", message = "Le nom ne doit contenir que des lettres !")
        private String lastname;

        @NotEmpty(message = "L'email est obligatoire !")
        @Length(min = 10, message = "L'email doit être constitué de 10 à 60 caractères !")
        @Pattern(regexp = "\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+\\.[A-Z|a-z]{2,}\\b", message = "L'email doit être valide !")
        private String email;

        @NotEmpty(message = "Le mot de passe est obligatoire !")
        @Length(min = 12, message = "Le mot de passe doit contenir au moins 12 caractères !")
        @Pattern(regexp = "^(?=.*[A-Za-zÀ-ÖØ-öø-ÿ])(?=.*\\d)(?=.*[@$!%*?&#])[A-Za-zÀ-ÖØ-öø-ÿ\\d@$!%*?&#]{12,}$",
                message = "Le mot de passe doit contenir au moins une lettre, un chiffre et un caractère spécial.")
        private String password;

        @NotNull(message = "La date de naissance est obligatoire !")
        private java.time.LocalDate birthdate;

        @NotEmpty(message = "Le numéro de téléphone est obligatoire !")
        @Length(min = 10, message = "Le numéro de téléphone doit contenir entre 10 et 15 caractères !")
        @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Le numéro de téléphone doit être valide !")
        private String phoneNumber;

        private Long questionId;
        private String securityQuestion;
        private String securityAnswer;

        @NotEmpty(message = "Le CAPTCHA est obligatoire !")
        private String captchaId;

        @NotEmpty(message = "La réponse au CAPTCHA est obligatoire !")
        private String captchaInput;

    }

    @Setter
    @Getter
    public static class UpdateUserRequest {
        @NotEmpty(message = "Le prénom est obligatoire !")
        @Length(min = 2, message = "Le prénom doit être constitué de 2 à 48 caractères !")
        @Pattern(regexp = "^[a-zA-ZÀ-ÿ\\s-]+$", message = "Le prénom ne doit contenir que des lettres !")
        private String firstname;

        @NotEmpty(message = "Le nom est obligatoire !")
        @Length(min = 2, message = "Le nom doit être constitué de 2 à 48 caractères !")
        @Pattern(regexp = "^[a-zA-ZÀ-ÿ\\s-]+$", message = "Le nom ne doit contenir que des lettres !")
        private String lastname;

        @NotEmpty(message = "L'email est obligatoire !")
        @Length(min = 10, message = "L'email doit être constitué de 10 à 60 caractères !")
        @Pattern(regexp = "\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+\\.[A-Z|a-z]{2,}\\b", message = "L'email doit être valide !")
        private String email;

        @NotNull(message = "La date de naissance est obligatoire !")
        private java.time.LocalDate birthdate;

        @NotEmpty(message = "Le numéro de téléphone est obligatoire !")
        @Length(min = 10, message = "Le numéro de téléphone doit contenir entre 10 et 15 caractères !")
        @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Le numéro de téléphone doit être valide !")
        private String phoneNumber;

    }

    @Setter
    @Getter
    public static class PasswordChangeRequest {
        // Getters et setters
        private String oldPassword;

        @NotEmpty(message = "Le mot de passe est obligatoire !")
        @Length(min = 12, message = "Le mot de passe doit contenir au moins 12 caractères !")
        @Pattern(regexp = "^(?=.*[A-Za-zÀ-ÖØ-öø-ÿ])(?=.*\\d)(?=.*[@$!%*?&#])[A-Za-zÀ-ÖØ-öø-ÿ\\d@$!%*?&#]{12,}$",
                message = "Le mot de passe doit contenir au moins une lettre, un chiffre et un caractère spécial.")
        private String newPassword;

        private String confirmPassword;
        private Long questionId;
        private String securityAnswer;

    }

    @Setter
    @Getter
    public static class SecurityQuestionRequest {
        private String email;
        private String securityAnswer;
    }
}
