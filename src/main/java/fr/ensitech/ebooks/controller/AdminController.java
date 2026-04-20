package fr.ensitech.ebooks.controller;

import fr.ensitech.ebooks.entity.User;
import fr.ensitech.ebooks.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final IUserService userService;

    // Obtenir tous les utilisateurs (sauf les administrateurs)
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        List<User> users = userService.findAll().stream()
            .filter(u -> !"admin".equalsIgnoreCase(u.getRole()))
            .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    // Obtenir un utilisateur par ID
    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        User user = userService.findById(id);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        if ("admin".equalsIgnoreCase(user.getRole())) {
            return ResponseEntity.status(403).body(Map.of("error", "Accès refusé aux comptes administrateurs"));
        }
        return ResponseEntity.ok(user);
    }

    // Créer un utilisateur
    @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestBody User user) {
        try {
            if ("admin".equalsIgnoreCase(user.getRole())) {
                user.setRole("client");
            }
            User savedUser = userService.save(user);
            return ResponseEntity.ok(Map.of("success", true, "message", "Utilisateur créé avec succès", "user", savedUser));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // Modifier un utilisateur
    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody User user) {
        User existingUser = userService.findById(id);
        if (existingUser == null) {
            return ResponseEntity.notFound().build();
        }
        if ("admin".equalsIgnoreCase(existingUser.getRole())) {
            return ResponseEntity.status(403).body(Map.of("error", "Modification d'administrateurs interdite"));
        }
        try {
            if ("admin".equalsIgnoreCase(user.getRole())) {
                user.setRole(existingUser.getRole());
            }
            user.setId(id);
            User updatedUser = userService.updateUser(user);
            return ResponseEntity.ok(Map.of("success", true, "message", "Utilisateur modifié avec succès", "user", updatedUser));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // Supprimer un utilisateur
    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        User user = userService.findById(id);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        if ("admin".equalsIgnoreCase(user.getRole())) {
            return ResponseEntity.status(403).body(Map.of("error", "Suppression d'administrateurs interdite"));
        }
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "Utilisateur supprimé avec succès"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // Activer/Désactiver un utilisateur
    @PatchMapping("/users/{id}/toggle-status")
    public ResponseEntity<?> toggleUserStatus(@PathVariable Long id) {
        User user = userService.findById(id);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        if ("admin".equalsIgnoreCase(user.getRole())) {
            return ResponseEntity.status(403).body(Map.of("error", "Modification d'administrateurs interdite"));
        }
        try {
            user.setEnabled(!user.isEnabled());
            User updatedUser = userService.save(user);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", updatedUser.isEnabled() ? "Utilisateur activé" : "Utilisateur désactivé",
                "user", updatedUser
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // Changer le rôle d'un utilisateur
    @PatchMapping("/users/{id}/role")
    public ResponseEntity<?> changeUserRole(@PathVariable Long id, @RequestBody Map<String, String> body) {
        User user = userService.findById(id);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        if ("admin".equalsIgnoreCase(user.getRole())) {
            return ResponseEntity.status(403).body(Map.of("error", "Modification d'administrateurs interdite"));
        }
        String newRole = body.get("role");
        if (newRole == null || newRole.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Rôle non spécifié"));
        }
        if ("admin".equalsIgnoreCase(newRole)) {
            return ResponseEntity.status(403).body(Map.of("error", "Attribution du rôle admin interdite"));
        }
        try {
            user.setRole(newRole);
            User updatedUser = userService.save(user);
            return ResponseEntity.ok(Map.of("success", true, "message", "Rôle modifié avec succès", "user", updatedUser));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // Obtenir des statistiques
    @GetMapping("/stats")
    public ResponseEntity<?> getStats() {
        List<User> allUsers = userService.findAll();
        long totalUsers = allUsers.stream().filter(u -> !"admin".equalsIgnoreCase(u.getRole())).count();
        long enabledUsers = allUsers.stream().filter(u -> !"admin".equalsIgnoreCase(u.getRole()) && u.isEnabled()).count();
        long librarians = allUsers.stream().filter(u -> "librarian".equalsIgnoreCase(u.getRole())).count();
        long clients = allUsers.stream().filter(u -> "client".equalsIgnoreCase(u.getRole())).count();

        return ResponseEntity.ok(Map.of(
            "totalUsers", totalUsers,
            "enabledUsers", enabledUsers,
            "disabledUsers", totalUsers - enabledUsers,
            "librarians", librarians,
            "clients", clients
        ));
    }
}
