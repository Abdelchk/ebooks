package fr.ensitech.ebooks.controller;

import fr.ensitech.ebooks.entity.SecurityQuestions;
import fr.ensitech.ebooks.entity.User;
import fr.ensitech.ebooks.service.IUserService;
import fr.ensitech.ebooks.service.RecaptchaService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
@RequestMapping("/api/auth")
public class AuthRestController {

    @Autowired
    private IUserService userService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private RecaptchaService recaptchaService;

    /**
     * Endpoint pour l'authentification
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, 
                                   HttpServletRequest httpServletRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            SecurityContext securityContext = SecurityContextHolder.getContext();
            securityContext.setAuthentication(authentication);
            
            httpServletRequest.getSession().setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, 
                securityContext
            );

            Optional<User> userOpt = userService.findByEmail(request.getEmail());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                
                // Vérifier si un code 2FA a déjà été envoyé dans les 24h
                java.time.LocalDateTime now = java.time.LocalDateTime.now();
                boolean codeRecentlySent = user.getLastVerificationCodeSentAt() != null &&
                        user.getLastVerificationCodeSentAt().isAfter(now.minusHours(24));
                
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Connexion réussie");
                response.put("userId", user.getId());
                response.put("email", user.getEmail());
                response.put("firstname", user.getFirstname());
                response.put("lastname", user.getLastname());
                
                if (codeRecentlySent) {
                    // Pas besoin de 2FA, redirection directe vers /accueil
                    response.put("requiresTwoFactor", false);
                    response.put("redirectTo", "/accueil");
                } else {
                    // Générer et envoyer le code 2FA
                    userService.generateVerificationCode(user);
                    response.put("requiresTwoFactor", true);
                    response.put("redirectTo", "/verify-code");
                }
                
                return ResponseEntity.ok(response);
            }
            
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("success", false, "message", "Identifiants invalides"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("success", false, "message", "Email ou mot de passe incorrect"));
        }
    }

    /**
     * Endpoint pour vérifier si l'utilisateur est authentifié
     */
    @GetMapping("/check")
    public ResponseEntity<?> checkAuth(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails != null) {
            Optional<User> userOpt = userService.findByEmail(userDetails.getUsername());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                Map<String, Object> response = new HashMap<>();
                response.put("authenticated", true);
                response.put("email", user.getEmail());
                response.put("firstname", user.getFirstname());
                response.put("lastname", user.getLastname());
                return ResponseEntity.ok(response);
            }
        }
        return ResponseEntity.ok(Map.of("authenticated", false));
    }

    /**
     * Endpoint pour la déconnexion
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        try {
            request.getSession().invalidate();
            SecurityContextHolder.clearContext();
            return ResponseEntity.ok(Map.of("success", true, "message", "Déconnexion réussie"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Erreur lors de la déconnexion"));
        }
    }

    /**
     * Endpoint pour l'inscription
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            // Vérifier le reCAPTCHA
            if (request.getRecaptchaToken() == null || request.getRecaptchaToken().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", "Veuillez valider le reCAPTCHA"));
            }

            if (!recaptchaService.verifyToken(request.getRecaptchaToken(), "REGISTER")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", "La vérification reCAPTCHA a échoué"));
            }

            User user = new User();
            user.setFirstname(request.getFirstname());
            user.setLastname(request.getLastname());
            user.setEmail(request.getEmail());
            user.setPassword(request.getPassword());
            user.setBirthdate(request.getBirthdate());
            user.setPhoneNumber(request.getPhoneNumber());

            User createdUser = userService.addOrUpdateUser(user);
            userService.addSecurityAnswer(createdUser, request.getQuestionId(), request.getSecurityAnswer());

            return ResponseEntity.ok(Map.of("success", true, "message", "Inscription réussie. Veuillez vérifier votre email."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Erreur lors de l'inscription : " + e.getMessage()));
        }
    }

    /**
     * Récupérer toutes les questions de sécurité
     */
    @GetMapping("/security-questions")
    public ResponseEntity<List<SecurityQuestions>> getSecurityQuestions() {
        try {
            List<SecurityQuestions> questions = userService.getAllSecurityQuestions();
            return ResponseEntity.ok(questions);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Obtenir la clé reCAPTCHA publique
     */
    @GetMapping("/recaptcha-key")
    public ResponseEntity<?> getRecaptchaKey() {
        try {
            String siteKey = recaptchaService.getSiteKey();
            return ResponseEntity.ok(Map.of("siteKey", siteKey));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Impossible de récupérer la clé reCAPTCHA"));
        }
    }

    /**
     * Initier la réinitialisation du mot de passe
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            userService.initiateForgotPassword(email);
            return ResponseEntity.ok(Map.of("success", true, "message", "Un email de réinitialisation a été envoyé"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Erreur lors de l'envoi de l'email"));
        }
    }

    /**
     * Réinitialiser le mot de passe
     */
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        try {
            boolean success = userService.resetPassword(
                request.getToken(), 
                request.getNewPassword(), 
                request.getConfirmPassword()
            );
            
            if (success) {
                return ResponseEntity.ok(Map.of("success", true, "message", "Mot de passe réinitialisé avec succès"));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", "Erreur lors de la réinitialisation"));
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Erreur serveur"));
        }
    }

    /**
     * Valider le token de réinitialisation
     */
    @GetMapping("/validate-reset-token")
    public ResponseEntity<?> validateResetToken(@RequestParam String token) {
        try {
            boolean valid = userService.validateResetToken(token);
            return ResponseEntity.ok(Map.of("valid", valid));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("valid", false));
        }
    }

    /**
     * Vérifier l'email avec token
     */
    @GetMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {
        try {
            String result = userService.validateVerificationToken(token);
            if ("valid".equals(result)) {
                return ResponseEntity.ok(Map.of("success", true, "message", "Votre compte est vérifié"));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", "Token de vérification invalide"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Erreur lors de la vérification"));
        }
    }

    /**
     * Vérifier le code 2FA
     */
    @PostMapping("/verify-code")
    public ResponseEntity<?> verifyCode(@RequestBody Map<String, String> request,
                                        @AuthenticationPrincipal UserDetails userDetails) {
        try {
            String code = request.get("code");
            User user = userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé"));
            
            if (userService.validateVerificationCode(user, code)) {
                return ResponseEntity.ok(Map.of("success", true, "message", "Code vérifié"));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", "Code invalide ou expiré"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * Renvoyer le code 2FA
     */
    @PostMapping("/resend-code")
    public ResponseEntity<?> resendCode(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé"));
            
            userService.generateVerificationCode(user);
            return ResponseEntity.ok(Map.of("success", true, "message", "Un nouveau code a été envoyé"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * Obtenir la question de sécurité de l'utilisateur connecté
     */
    @GetMapping("/security-question")
    public ResponseEntity<?> getSecurityQuestion(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé"));
            
            SecurityQuestions question = userService.getSecurityQuestionForUser(user);
            return ResponseEntity.ok(Map.of("question", question.getQuestion(), "id", question.getId()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Mettre à jour le mot de passe de l'utilisateur connecté
     */
    @PostMapping("/update-password")
    public ResponseEntity<?> updatePassword(@RequestBody UpdatePasswordRequest request,
                                           @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé"));
            
            SecurityQuestions securityQuestion = userService.getSecurityQuestionForUser(user);
            
            boolean success = userService.updatePassword(
                user,
                request.getOldPassword(),
                request.getNewPassword(),
                request.getConfirmPassword(),
                securityQuestion.getId(),
                request.getSecurityAnswer()
            );
            
            if (success) {
                return ResponseEntity.ok(Map.of("success", true, "message", "Mot de passe mis à jour avec succès"));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", "Erreur lors de la mise à jour"));
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Erreur serveur"));
        }
    }

    // DTO Classes
    @Getter
    @Setter
    public static class LoginRequest {
        private String email;
        private String password;
    }

    @Getter
    @Setter
    public static class RegisterRequest {
        private String firstname;
        private String lastname;
        private String email;
        private String password;
        private java.time.LocalDate birthdate;
        private String phoneNumber;
        private Long questionId;
        private String securityAnswer;
        private String recaptchaToken;
    }

    @Getter
    @Setter
    public static class ResetPasswordRequest {
        private String token;
        private String newPassword;
        private String confirmPassword;
    }

    @Getter
    @Setter
    public static class UpdatePasswordRequest {
        private String oldPassword;
        private String newPassword;
        private String confirmPassword;
        private String securityAnswer;
    }
}

