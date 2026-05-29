package fr.ensitech.ebooks.controller;

import fr.ensitech.ebooks.entity.Loan;
import fr.ensitech.ebooks.service.ILoanService;
import fr.ensitech.ebooks.securingweb.CustomUserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rest/loans")
public class LoanRestController {

    private static final String MESSAGE_KEY = "message";

    private final ILoanService loanService;

    public LoanRestController(ILoanService loanService) {
        this.loanService = loanService;
    }

    @PostMapping("/from-reservation/{id}")
    public ResponseEntity<Map<String, Object>> createLoanFromReservation(@PathVariable Long id) {
        try {
            Loan loan = loanService.createLoanFromReservation(id);
            return ResponseEntity.ok(Map.of(MESSAGE_KEY, "Emprunt créé", "loan", loan));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(MESSAGE_KEY, e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<Loan>> getUserLoans(Authentication auth) {
        try {
            CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
            Long userId = userDetails.getUser().getId();
            List<Loan> loans = loanService.getUserLoans(userId);
            return ResponseEntity.ok(loans);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/active")
    public ResponseEntity<List<Loan>> getActiveLoans(Authentication auth) {
        try {
            CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
            Long userId = userDetails.getUser().getId();
            List<Loan> loans = loanService.getActiveLoans(userId);
            return ResponseEntity.ok(loans);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/{id}/extend")
    public ResponseEntity<Map<String, Object>> extendLoan(@PathVariable Long id, Authentication auth) {
        try {
            CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
            Long userId = userDetails.getUser().getId();
            Loan loan = loanService.extendLoan(id, userId);
            return ResponseEntity.ok(Map.of(MESSAGE_KEY, "Emprunt prolongé", "loan", loan));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(MESSAGE_KEY, e.getMessage()));
        }
    }

    @PostMapping("/{id}/return")
    public ResponseEntity<Map<String, Object>> returnLoan(@PathVariable Long id, Authentication auth) {
        try {
            CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
            Long userId = userDetails.getUser().getId();
            Loan loan = loanService.returnLoan(id, userId);
            return ResponseEntity.ok(Map.of(MESSAGE_KEY, "Emprunt retourné", "loan", loan));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(MESSAGE_KEY, e.getMessage()));
        }
    }
}
