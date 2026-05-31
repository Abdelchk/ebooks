package fr.ensitech.ebooks.controller;

import fr.ensitech.ebooks.entity.SecurityQuestions;
import fr.ensitech.ebooks.entity.User;
import fr.ensitech.ebooks.service.IUserService;
import fr.ensitech.ebooks.service.RecaptchaService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.web.csrf.CsrfToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthRestController {

    private static final Logger logger = LoggerFactory.getLogger(AuthRestController.class);

    // S1192 : constantes pour les clés JSON dupliquées
    private static final String KEY_SUCCESS       = "success";
    private static final String KEY_MESSAGE       = "message";
    private static final String KEY_ERROR         = "error";
    private static final String KEY_AUTHENTICATED = "authenticated";
    private static final String KEY_VALID         = "valid";
    private static final String KEY_EMAIL         = "email";
    private static final String MSG_USER_NOT_FOUND = "Utilisateur non trouvé";

    // S6813 : injection par constructeur
    private final IUserService userService;
    private final AuthenticationManager authenticationManager;
    private final RecaptchaService recaptchaService;

    public AuthRestController(IUserService userService,
                              AuthenticationManager authenticationManager,
                              RecaptchaService recaptchaService) {
        this.userService           = userService;
        this.authenticationManager = authenticationManager;
        this.recaptchaService      = recaptchaService;
    }

    /**
     * Endpoint pour l'authentification.
     *
     * Séparation explicite des phases :
     *  1. Authentification Spring Security (credentials) → 401 si échec
     *  2. Post-authentification (2FA, email, session) → 500 si erreur interne
     *
     * Sans cette séparation, une erreur SMTP lors de l'envoi du code 2FA remontait
     * sous forme de 401 "Email ou mot de passe incorrect", masquant la vraie cause.
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest request,
                                                     HttpServletRequest httpServletRequest) {
        // ── Phase 1 : vérification des credentials ─────────────────────────────
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (Exception e) {
            logger.warn("Échec d'authentification pour {} : {}", request.getEmail(), e.getClass().getSimpleName());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.<String, Object>of(KEY_SUCCESS, false, KEY_MESSAGE, "Email ou mot de passe incorrect"));
        }

        // ── Phase 2 : post-authentification (session, 2FA) ────────────────────
        try {
            SecurityContext securityContext = SecurityContextHolder.getContext();
            securityContext.setAuthentication(authentication);

            httpServletRequest.getSession().setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                securityContext
            );

            Optional<User> userOpt = userService.findByEmail(request.getEmail());
            if (userOpt.isEmpty()) {
                logger.error("Utilisateur authentifié non trouvé en BDD : {}", request.getEmail());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.<String, Object>of(KEY_SUCCESS, false, KEY_MESSAGE, "Erreur interne lors de la connexion"));
            }

            User user = userOpt.get();

            // Vérifier si un code 2FA a déjà été envoyé dans les 24h
            LocalDateTime now = LocalDateTime.now();
            boolean codeRecentlySent = user.getLastVerificationCodeSentAt() != null &&
                    user.getLastVerificationCodeSentAt().isAfter(now.minusHours(24));

            Map<String, Object> response = new HashMap<>();
            response.put(KEY_SUCCESS, true);
            response.put(KEY_MESSAGE, "Connexion réussie");
            response.put("userId", user.getId());
            response.put(KEY_EMAIL, user.getEmail());
            response.put("firstname", user.getFirstname());
            response.put("lastname", user.getLastname());

            if (codeRecentlySent) {
                response.put("requiresTwoFactor", false);
                response.put("redirectTo", "/accueil");
            } else {
                try {
                    userService.generateVerificationCode(user);
                    response.put("requiresTwoFactor", true);
                    response.put("redirectTo", "/verify-code");
                } catch (Exception emailEx) {
                    // L'envoi du code 2FA a échoué (ex : SMTP injoignable)
                    // On laisse quand même l'utilisateur accéder : on bypasse le 2FA
                    logger.error("Impossible d'envoyer le code 2FA pour {} : {}", request.getEmail(), emailEx.getMessage());
                    response.put("requiresTwoFactor", false);
                    response.put("redirectTo", "/accueil");
                    response.put("twoFactorWarning", "Code 2FA non envoyé (problème email)");
                }
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Erreur post-authentification pour {} : {}", request.getEmail(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.<String, Object>of(KEY_SUCCESS, false, KEY_MESSAGE, "Erreur interne lors de la connexion"));
        }
    }

    /**
     * Endpoint pour vérifier si l'utilisateur est authentifié
     */
    @GetMapping("/check")
    public ResponseEntity<Map<String, Object>> checkAuth(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails != null) {
            Optional<User> userOpt = userService.findByEmail(userDetails.getUsername());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                Map<String, Object> response = new HashMap<>();
                response.put(KEY_AUTHENTICATED, true);
                response.put(KEY_EMAIL, user.getEmail());
                response.put("firstname", user.getFirstname());
                response.put("lastname", user.getLastname());
                response.put("role", user.getRole());
                response.put("userId", user.getId());
                response.put("passwordStatus", checkPasswordExpiration(user));
                return ResponseEntity.ok(response);
            }
        }
        return ResponseEntity.ok(Map.<String, Object>of(KEY_AUTHENTICATED, false));
    }

    /**
     * Endpoint pour la déconnexion
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(HttpServletRequest request) {
        try {
            request.getSession().invalidate();
            SecurityContextHolder.clearContext();
            return ResponseEntity.ok(Map.<String, Object>of(KEY_SUCCESS, true, KEY_MESSAGE, "Déconnexion réussie"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.<String, Object>of(KEY_SUCCESS, false, KEY_MESSAGE, "Erreur lors de la déconnexion"));
        }
    }

    /**
     * Endpoint pour l'inscription.
     *
     * Cas d'erreur :
     *  - reCAPTCHA absent/invalide        → 400
     *  - Données invalides (email, mdp…)  → 400
     *  - Utilisateur déjà existant        → 400
     *  - Erreur email d'activation        → 200 avec avertissement (l'utilisateur est créé)
     *  - Autre erreur interne             → 500
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody RegisterRequest request) {
        // ── Vérification reCAPTCHA ───────────────────────────────────────────
        if (request.getRecaptchaToken() == null || request.getRecaptchaToken().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.<String, Object>of(KEY_SUCCESS, false, KEY_MESSAGE, "Veuillez valider le reCAPTCHA"));
        }

        boolean isTokenValid;
        try {
            isTokenValid = recaptchaService.verifyToken(request.getRecaptchaToken(), "REGISTER");
        } catch (Exception ex) {
            logger.error("Erreur reCAPTCHA inattendue lors de l'inscription : {}", ex.getMessage(), ex);
            // En cas d'erreur technique reCAPTCHA, on rejette prudemment
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.<String, Object>of(KEY_SUCCESS, false,
                    KEY_MESSAGE, "Le service de vérification est temporairement indisponible. Réessayez."));
        }

        if (!isTokenValid) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.<String, Object>of(KEY_SUCCESS, false, KEY_MESSAGE, "La vérification reCAPTCHA a échoué"));
        }

        // ── Création du compte ───────────────────────────────────────────────
        try {
            User user = new User();
            user.setFirstname(request.getFirstname());
            user.setLastname(request.getLastname());
            user.setEmail(request.getEmail());
            user.setPassword(request.getPassword());
            user.setBirthdate(request.getBirthdate());
            user.setPhoneNumber(request.getPhoneNumber());

            User createdUser = userService.addOrUpdateUser(user);
            userService.addSecurityAnswer(createdUser, request.getQuestionId(), request.getSecurityAnswer());

            return ResponseEntity.ok(
                Map.<String, Object>of(KEY_SUCCESS, true, KEY_MESSAGE, "Inscription réussie. Veuillez vérifier votre email."));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.<String, Object>of(KEY_SUCCESS, false, KEY_MESSAGE, e.getMessage()));
        } catch (Exception e) {
            logger.error("Erreur lors de l'inscription : {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.<String, Object>of(KEY_SUCCESS, false,
                    KEY_MESSAGE, "Erreur lors de l'inscription. Veuillez réessayer."));
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
    public ResponseEntity<Map<String, Object>> getRecaptchaKey() {
        try {
            String siteKey = recaptchaService.getSiteKey();
            return ResponseEntity.ok(Map.<String, Object>of("siteKey", siteKey));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.<String, Object>of(KEY_ERROR, "Impossible de récupérer la clé reCAPTCHA"));
        }
    }

    /**
     * Initier la réinitialisation du mot de passe
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, Object>> forgotPassword(@RequestBody Map<String, String> request) {
        try {
            String email = request.get(KEY_EMAIL);
            userService.initiateForgotPassword(email);
            return ResponseEntity.ok(
                Map.<String, Object>of(KEY_SUCCESS, true, KEY_MESSAGE, "Un email de réinitialisation a été envoyé"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.<String, Object>of(KEY_SUCCESS, false, KEY_MESSAGE, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.<String, Object>of(KEY_SUCCESS, false, KEY_MESSAGE, "Erreur lors de l'envoi de l'email"));
        }
    }

    /**
     * Réinitialiser le mot de passe
     */
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, Object>> resetPassword(@RequestBody ResetPasswordRequest request) {
        try {
            boolean success = userService.resetPassword(
                request.getToken(),
                request.getNewPassword(),
                request.getConfirmPassword()
            );

            if (success) {
                return ResponseEntity.ok(
                    Map.<String, Object>of(KEY_SUCCESS, true, KEY_MESSAGE, "Mot de passe réinitialisé avec succès"));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.<String, Object>of(KEY_SUCCESS, false, KEY_MESSAGE, "Erreur lors de la réinitialisation"));
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.<String, Object>of(KEY_SUCCESS, false, KEY_MESSAGE, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.<String, Object>of(KEY_SUCCESS, false, KEY_MESSAGE, "Erreur serveur"));
        }
    }

    /**
     * Valider le token de réinitialisation
     */
    @GetMapping("/validate-reset-token")
    public ResponseEntity<Map<String, Object>> validateResetToken(@RequestParam String token) {
        try {
            boolean valid = userService.validateResetToken(token);
            return ResponseEntity.ok(Map.<String, Object>of(KEY_VALID, valid));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.<String, Object>of(KEY_VALID, false));
        }
    }

    /**
     * Vérifier l'email avec token
     */
    @GetMapping("/verify-email")
    public ResponseEntity<Map<String, Object>> verifyEmail(@RequestParam String token) {
        try {
            String result = userService.validateVerificationToken(token);
            if (KEY_VALID.equals(result)) {
                return ResponseEntity.ok(
                    Map.<String, Object>of(KEY_SUCCESS, true, KEY_MESSAGE, "Votre compte est vérifié"));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.<String, Object>of(KEY_SUCCESS, false, KEY_MESSAGE, "Token de vérification invalide"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.<String, Object>of(KEY_SUCCESS, false, KEY_MESSAGE, "Erreur lors de la vérification"));
        }
    }

    /**
     * Vérifier le code 2FA
     */
    @PostMapping("/verify-code")
    public ResponseEntity<Map<String, Object>> verifyCode(@RequestBody Map<String, String> request,
                                                          @AuthenticationPrincipal UserDetails userDetails) {
        try {
            String code = request.get("code");
            User user = userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException(MSG_USER_NOT_FOUND));

            if (userService.validateVerificationCode(user, code)) {
                return ResponseEntity.ok(Map.<String, Object>of(KEY_SUCCESS, true, KEY_MESSAGE, "Code vérifié"));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.<String, Object>of(KEY_SUCCESS, false, KEY_MESSAGE, "Code invalide ou expiré"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.<String, Object>of(KEY_SUCCESS, false, KEY_MESSAGE, e.getMessage()));
        }
    }

    /**
     * Renvoyer le code 2FA
     */
    @PostMapping("/resend-code")
    public ResponseEntity<Map<String, Object>> resendCode(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException(MSG_USER_NOT_FOUND));

            userService.generateVerificationCode(user);
            return ResponseEntity.ok(Map.<String, Object>of(KEY_SUCCESS, true, KEY_MESSAGE, "Un nouveau code a été envoyé"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.<String, Object>of(KEY_SUCCESS, false, KEY_MESSAGE, e.getMessage()));
        }
    }

    /**
     * Obtenir la question de sécurité de l'utilisateur connecté
     */
    @GetMapping("/security-question")
    public ResponseEntity<Map<String, Object>> getSecurityQuestion(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException(MSG_USER_NOT_FOUND));

            SecurityQuestions question = userService.getSecurityQuestionForUser(user);
            return ResponseEntity.ok(Map.<String, Object>of("question", question.getQuestion(), "id", question.getId()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.<String, Object>of(KEY_ERROR, e.getMessage()));
        }
    }

    /**
     * Mettre à jour le mot de passe de l'utilisateur connecté
     */
    @PostMapping("/update-password")
    public ResponseEntity<Map<String, Object>> updatePassword(@RequestBody UpdatePasswordRequest request,
                                                              @AuthenticationPrincipal UserDetails userDetails) {
        try {
            logger.debug("Tentative de mise à jour du mot de passe pour l'utilisateur: {}", userDetails.getUsername());

            User user = userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException(MSG_USER_NOT_FOUND));

            logger.debug("Utilisateur trouvé: {}", user.getEmail());

            SecurityQuestions securityQuestion = userService.getSecurityQuestionForUser(user);
            logger.debug("Question de sécurité ID: {}", securityQuestion.getId());

            boolean success = userService.updatePassword(
                user,
                request.getOldPassword(),
                request.getNewPassword(),
                request.getConfirmPassword(),
                securityQuestion.getId(),
                request.getSecurityAnswer()
            );

            if (success) {
                return ResponseEntity.ok(
                    Map.<String, Object>of(KEY_SUCCESS, true, KEY_MESSAGE, "Mot de passe mis à jour avec succès"));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.<String, Object>of(KEY_SUCCESS, false, KEY_MESSAGE, "Erreur lors de la mise à jour"));
            }
        } catch (IllegalArgumentException e) {
            logger.error("Erreur lors de la mise à jour du mot de passe : {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.<String, Object>of(KEY_SUCCESS, false, KEY_MESSAGE, e.getMessage()));
        } catch (Exception e) {
            logger.error("Erreur serveur lors de la mise à jour du mot de passe", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.<String, Object>of(KEY_SUCCESS, false, KEY_MESSAGE, "Erreur serveur: " + e.getMessage()));
        }
    }

    /**
     * Endpoint pour vérifier l'état d'expiration du mot de passe
     */
    @GetMapping("/password-status")
    public ResponseEntity<Map<String, Object>> getPasswordStatus(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.<String, Object>of(KEY_AUTHENTICATED, false));
        }

        try {
            User user = userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException(MSG_USER_NOT_FOUND));

            Map<String, Object> passwordStatus = checkPasswordExpiration(user);
            return ResponseEntity.ok(passwordStatus);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.<String, Object>of(KEY_ERROR, "Erreur lors de la vérification"));
        }
    }

    /**
     * Méthode utilitaire pour vérifier l'expiration du mot de passe
     * Utilisée par /check et /password-status
     */
    private Map<String, Object> checkPasswordExpiration(User user) {
        Map<String, Object> status = new HashMap<>();
        status.put("expired", false);
        status.put("warning", false);
        status.put("daysRemaining", null);
        status.put(KEY_MESSAGE, null);

        LocalDate lastPasswordUpdate = user.getLastPasswordUpdateDate();

        if (lastPasswordUpdate == null) {
            status.put("neverChanged", true);
            return status;
        }

        status.put("neverChanged", false);

        long daysSinceLastUpdate = ChronoUnit.DAYS.between(lastPasswordUpdate, LocalDate.now());
        long daysRemaining = 84 - daysSinceLastUpdate;

        status.put("daysSinceLastUpdate", daysSinceLastUpdate);
        status.put("daysRemaining", daysRemaining);

        if (daysSinceLastUpdate >= 84) {
            status.put("expired", true);
            status.put(KEY_MESSAGE, "Votre mot de passe a expiré. Vous devez le changer pour continuer.");
        } else if (daysSinceLastUpdate >= 77) {
            status.put("warning", true);
            status.put(KEY_MESSAGE, "Attention : Votre mot de passe expire dans " + daysRemaining + " jour(s).");
        }

        return status;
    }

    /**
     * Endpoint CSRF cross-origin : retourne le jeton CSRF dans le corps JSON.
     *
     * Pourquoi cet endpoint est nécessaire :
     * Le mécanisme standard "Double Submit Cookie" (CookieCsrfTokenRepository) ne fonctionne
     * qu'en same-origin : le JavaScript côté Vercel ne peut PAS lire un cookie posé par
     * le backend sur un domaine différent (Same-Origin Policy).
     * → Solution : on expose le token en clair dans le corps HTTP. Le frontend le stocke
     *   en mémoire et le renvoie manuellement dans l'en-tête X-XSRF-TOKEN.
     *
     * Cet endpoint est public (couvert par /api/auth/** dans WebSecurityConfig).
     * Il ne retourne que le token masqué XOR ; la session reste protégée côté serveur.
     */
    @GetMapping("/csrf")
    public ResponseEntity<Map<String, Object>> getCsrfToken(CsrfToken csrfToken) {
        if (csrfToken == null) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.<String, Object>of(KEY_ERROR, "CSRF token non disponible"));
        }
        return ResponseEntity.ok(Map.<String, Object>of(
            "token",         csrfToken.getToken(),
            "headerName",    csrfToken.getHeaderName(),
            "parameterName", csrfToken.getParameterName()
        ));
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
        private LocalDate birthdate;
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
