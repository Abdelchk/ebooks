package fr.ensitech.ebooks.controller;

import fr.ensitech.ebooks.entity.CartItem;
import fr.ensitech.ebooks.service.ICartService;
import fr.ensitech.ebooks.securingweb.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
@RequestMapping("/api/rest/cart")
public class CartRestController {
    
    @Autowired
    private ICartService cartService;
    
    @GetMapping
    public ResponseEntity<List<CartItem>> getCart(Authentication auth) {
        try {
            CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
            Long userId = userDetails.getUser().getId();
            List<CartItem> items = cartService.getCartItems(userId);
            return ResponseEntity.ok(items);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PostMapping("/add")
    public ResponseEntity<?> addToCart(@RequestBody Map<String, Object> payload, Authentication auth) {
        try {
            CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
            Long userId = userDetails.getUser().getId();
            Long bookId = Long.parseLong(payload.get("bookId").toString());
            Integer duration = payload.containsKey("loanDuration") ? 
                    Integer.parseInt(payload.get("loanDuration").toString()) : 14;
            
            CartItem item = cartService.addToCart(userId, bookId, duration);
            return ResponseEntity.ok(item);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> removeFromCart(@PathVariable Long id, Authentication auth) {
        try {
            CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
            Long userId = userDetails.getUser().getId();
            cartService.removeFromCart(id, userId);
            return ResponseEntity.ok(Map.of("message", "Article supprimé"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
    
    @DeleteMapping("/clear")
    public ResponseEntity<?> clearCart(Authentication auth) {
        try {
            CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
            Long userId = userDetails.getUser().getId();
            cartService.clearCart(userId);
            return ResponseEntity.ok(Map.of("message", "Panier vidé"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PutMapping("/{id}/duration")
    public ResponseEntity<?> updateDuration(@PathVariable Long id, 
                                           @RequestBody Map<String, Integer> payload,
                                           Authentication auth) {
        try {
            CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
            Long userId = userDetails.getUser().getId();
            Integer duration = payload.get("duration");
            CartItem item = cartService.updateLoanDuration(id, userId, duration);
            return ResponseEntity.ok(item);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
    
    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> getCartCount(Authentication auth) {
        try {
            CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
            Long userId = userDetails.getUser().getId();
            long count = cartService.getCartItemCount(userId);
            return ResponseEntity.ok(Map.of("count", count));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("count", 0L));
        }
    }
}

