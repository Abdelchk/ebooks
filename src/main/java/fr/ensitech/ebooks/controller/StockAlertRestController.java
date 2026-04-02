package fr.ensitech.ebooks.controller;

import fr.ensitech.ebooks.entity.StockAlert;
import fr.ensitech.ebooks.service.IStockAlertService;
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
@RequestMapping("/api/rest/stock-alerts")
public class StockAlertRestController {

    @Autowired
    private IStockAlertService stockAlertService;

    @PostMapping("/create/{bookId}")
    public ResponseEntity<?> createAlert(@PathVariable Long bookId, Authentication auth) {
        try {
            CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
            Long userId = userDetails.getUser().getId();
            StockAlert alert = stockAlertService.createAlert(userId, bookId);
            return ResponseEntity.ok(alert);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<StockAlert>> getUserAlerts(Authentication auth) {
        try {
            CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
            Long userId = userDetails.getUser().getId();
            List<StockAlert> alerts = stockAlertService.getUserAlerts(userId);
            return ResponseEntity.ok(alerts);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancelAlert(@PathVariable Long id, Authentication auth) {
        try {
            CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
            Long userId = userDetails.getUser().getId();
            StockAlert alert = stockAlertService.cancelAlert(id, userId);
            return ResponseEntity.ok(alert);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}

