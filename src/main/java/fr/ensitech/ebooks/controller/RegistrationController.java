package fr.ensitech.ebooks.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import fr.ensitech.ebooks.entity.User;
import fr.ensitech.ebooks.service.RecaptchaService;
import fr.ensitech.ebooks.service.UserService;
import jakarta.validation.Valid;

@Controller
public class RegistrationController {

    private static final Logger logger = LoggerFactory.getLogger(RegistrationController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private RecaptchaService recaptchaService;

    /**
     * Endpoint de test pour vérifier la clé reCAPTCHA
     */
    @GetMapping("/test-recaptcha-key")
    @ResponseBody
    public ResponseEntity<String> testRecaptchaKey() {
        String siteKey = recaptchaService.getSiteKey();
        logger.info("TEST ENDPOINT - Clé récupérée: {}", siteKey);
        return ResponseEntity.ok("Clé reCAPTCHA: " + siteKey + " | Longueur: " + (siteKey != null ? siteKey.length() : 0));
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        logger.info("=== GET /register appelé ===");
        
        String siteKey = recaptchaService.getSiteKey();
        logger.info("Clé reCAPTCHA récupérée du service: {}", siteKey);
        
        model.addAttribute("user", new User());
        model.addAttribute("questions", userService.getAllSecurityQuestions());
        model.addAttribute("recaptchaSiteKey", siteKey);
        
        logger.info("Attributs ajoutés au modèle");
        logger.info("recaptchaSiteKey dans le modèle: {}", model.getAttribute("recaptchaSiteKey"));
        logger.info("=== Fin GET /register ===");
        
        return "register";
    }

    @PostMapping("/register")
    public String registerUserAccount(
            @Valid @ModelAttribute("user") User user,
            BindingResult result,
            @RequestParam("questionId") Long questionId,
            @RequestParam("securityAnswer") String securityAnswer,
            @RequestParam("g-recaptcha-response") String recaptchaToken,
            Model model) {

        // Valider reCAPTCHA en premier
        if (recaptchaToken == null || recaptchaToken.isEmpty()) {
            model.addAttribute("error", "Veuillez valider le reCAPTCHA");
            model.addAttribute("questions", userService.getAllSecurityQuestions());
            model.addAttribute("recaptchaSiteKey", recaptchaService.getSiteKey());
            return "register";
        }

        if (!recaptchaService.verifyToken(recaptchaToken, "REGISTER")) {
            model.addAttribute("error", "La vérification reCAPTCHA a échoué. Veuillez réessayer.");
            model.addAttribute("questions", userService.getAllSecurityQuestions());
            model.addAttribute("recaptchaSiteKey", recaptchaService.getSiteKey());
            return "register";
        }

        if (result.hasErrors()) {
            model.addAttribute("questions", userService.getAllSecurityQuestions());
            model.addAttribute("recaptchaSiteKey", recaptchaService.getSiteKey());
            return "register";
        }

        try {
            userService.addOrUpdateUser(user);
            userService.addSecurityAnswer(user, questionId, securityAnswer);
            return "redirect:/last-step";
        } catch (Exception e) {
            model.addAttribute("error", "Erreur lors de l'inscription : " + e.getMessage());
            model.addAttribute("questions", userService.getAllSecurityQuestions());
            model.addAttribute("recaptchaSiteKey", recaptchaService.getSiteKey());
            return "register";
        }
    }

    @GetMapping("/last-step")
    public String showLastStep() {
        return "last-step";
    }


    @GetMapping("/verify-email")
    public String verifyEmail(@RequestParam("token") String token, Model model) {
        String result = userService.validateVerificationToken(token);
        if (result.equals("valid")) {
            model.addAttribute("message", "Votre compte est vérifié.");
            return "verify-email";
        } else {
            model.addAttribute("message", "Token de vérification invalide.");
            return "verify-email";
        }
    }
}
