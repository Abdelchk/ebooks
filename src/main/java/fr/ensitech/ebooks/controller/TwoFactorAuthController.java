package fr.ensitech.ebooks.controller;

import fr.ensitech.ebooks.entity.User;
import fr.ensitech.ebooks.repository.IUserRepository;
import fr.ensitech.ebooks.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
public class TwoFactorAuthController {

    @Autowired
    private IUserService userService;

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userService.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Email non trouvé : " + username));
    }


    @GetMapping("/verify-code")
    public String showVerifyCodePage(Model model) {
        return "verify-code";
    }

    @PostMapping("/verify-code")
    public String verifyCode(@RequestParam("code") String code,
                             @AuthenticationPrincipal UserDetails userDetails,
                             Model model) {
        User user = userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("Email non trouvé : " + userDetails.getUsername()));

        if (userService.validateVerificationCode(user, code)) {
            return "redirect:/accueil";
        } else {
            model.addAttribute("error", "Code invalide ou expiré");
            return "redirect:/verify-code?error";
        }
    }

    @PostMapping("/resend-code")
    public String resendCode(@AuthenticationPrincipal UserDetails userDetails,
            Model model) {
        User user = userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("Email non trouvé : " + userDetails.getUsername()));
        userService.generateVerificationCode(user);
        model.addAttribute("message", "Un nouveau code a été envoyé");
        return "verify-code";
    }
}
