package fr.ensitech.ebooks.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import fr.ensitech.ebooks.entity.User;
import fr.ensitech.ebooks.service.UserService;
import jakarta.validation.Valid;

@Controller
public class RegistrationController {

    @Autowired
    private UserService userService;

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("questions", userService.getAllSecurityQuestions());
        return "register";
    }

    @PostMapping("/register")
    public String registerUserAccount(
            @Valid @ModelAttribute("user") User user,
            BindingResult result,
            @RequestParam("questionId") Long questionId,
            @RequestParam("securityAnswer") String securityAnswer,
            Model model) {
    	if (result.hasErrors()) {
            return "register"; // renvoie au formulaire avec erreurs
        }
        userService.registerNewUserAccount(user, questionId, securityAnswer);
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
