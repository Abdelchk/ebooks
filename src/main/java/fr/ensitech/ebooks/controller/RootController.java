package fr.ensitech.ebooks.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller pour gérer les redirections de base.
 * Redirige la racine vers le frontend React (local ou production Vercel).
 */
@Controller
public class RootController {

    @Value("${frontend.url:http://localhost:3000}")
    private String frontendUrl;

    /**
     * Redirection de la racine vers le frontend React
     */
    @GetMapping("/")
    public String redirectToReact() {
        return "redirect:" + frontendUrl;
    }
}
