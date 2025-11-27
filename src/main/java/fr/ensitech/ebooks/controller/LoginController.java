package fr.ensitech.ebooks.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import fr.ensitech.ebooks.service.UserService;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }
    
    @PostMapping("/logout")
    public String logout() {
        // Logique de déconnexion si nécessaire
        return "redirect:/login?logout"; // Redirige vers la page de connexion avec un paramètre de déconnexion
    }
}
