package fr.ensitech.ebooks.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller pour gérer les redirections de base.
 * Toutes les routes non-API redirigent vers le frontend React (localhost:3000)
 */
@Controller
public class RootController {

    /**
     * Redirection de la racine vers le frontend React
     * Vous pouvez aussi servir un message d'information
     */
    @GetMapping("/")
    public String redirectToReact() {
        // Retourner une simple réponse indiquant que le backend est actif
        // Le frontend React doit être accessible sur http://localhost:3000
        return "redirect:http://localhost:3000";
    }
}

