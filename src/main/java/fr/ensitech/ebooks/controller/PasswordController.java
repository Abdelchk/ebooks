package fr.ensitech.ebooks.controller;

import fr.ensitech.ebooks.entity.SecurityQuestions;
import fr.ensitech.ebooks.entity.User;
import fr.ensitech.ebooks.service.IUserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class PasswordController {

    @Autowired
    private IUserService userService;

    /**
     * Afficher la page de mise à jour du mot de passe
     */
    @GetMapping("/update-password")
    public String showUpdatePasswordForm(Model model, HttpSession session) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        User user = userService.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé"));

        // Récupérer la question de sécurité de l'utilisateur
        SecurityQuestions securityQuestion = userService.getSecurityQuestionForUser(user);

        model.addAttribute("securityQuestion", securityQuestion);

        // Transférer les messages de session au modèle
        if (session.getAttribute("passwordExpired") != null) {
            model.addAttribute("passwordExpired", session.getAttribute("passwordExpired"));
            model.addAttribute("errorMessage", session.getAttribute("errorMessage"));
            session.removeAttribute("passwordExpired");
            session.removeAttribute("errorMessage");
        }

        if (session.getAttribute("passwordWarning") != null) {
            model.addAttribute("passwordWarning", session.getAttribute("passwordWarning"));
            session.removeAttribute("passwordWarning");
        }

        return "update-password";
    }

    /**
     * Traiter la mise à jour du mot de passe
     */
    @PostMapping("/update-password")
    public String updatePassword(@RequestParam("oldPassword") String oldPassword,
                                  @RequestParam("newPassword") String newPassword,
                                  @RequestParam("confirmPassword") String confirmPassword,
                                  @RequestParam("securityAnswer") String securityAnswer,
                                  RedirectAttributes redirectAttributes) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String email = auth.getName();

            User user = userService.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé"));

            SecurityQuestions securityQuestion = userService.getSecurityQuestionForUser(user);

            userService.updatePassword(user, oldPassword, newPassword, confirmPassword,
                                      securityQuestion.getId(), securityAnswer);

            redirectAttributes.addFlashAttribute("successMessage",
                "Mot de passe mis à jour avec succès !");

            return "redirect:/accueil";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/update-password";
        }
    }

    /**
     * Afficher la page "mot de passe oublié"
     */
    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        return "forgot-password";
    }

    /**
     * Traiter la demande de réinitialisation de mot de passe
     */
    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam("email") String email,
                                        RedirectAttributes redirectAttributes) {
        try {
            userService.initiateForgotPassword(email);
            redirectAttributes.addFlashAttribute("successMessage",
                "Un email de réinitialisation a été envoyé à votre adresse.");
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/forgot-password";
        }
    }

    /**
     * Afficher le formulaire de réinitialisation de mot de passe
     */
    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam("token") String token, Model model,
                                        RedirectAttributes redirectAttributes) {
        if (!userService.validateResetToken(token)) {
            redirectAttributes.addFlashAttribute("errorMessage",
                "Le lien de réinitialisation est invalide ou a expiré.");
            return "redirect:/login";
        }

        model.addAttribute("token", token);
        return "reset-password";
    }

    /**
     * Traiter la réinitialisation du mot de passe
     */
    @PostMapping("/reset-password")
    public String processResetPassword(@RequestParam("token") String token,
                                       @RequestParam("newPassword") String newPassword,
                                       @RequestParam("confirmPassword") String confirmPassword,
                                       RedirectAttributes redirectAttributes) {
        try {
            userService.resetPassword(token, newPassword, confirmPassword);
            redirectAttributes.addFlashAttribute("successMessage",
                "Votre mot de passe a été réinitialisé avec succès !");
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/reset-password?token=" + token;
        }
    }
}

