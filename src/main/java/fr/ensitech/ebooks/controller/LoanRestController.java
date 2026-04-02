package fr.ensitech.ebooks.controller;

import fr.ensitech.ebooks.entity.Loan;
import fr.ensitech.ebooks.service.ILoanService;
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
@RequestMapping("/api/rest/loans")
public class LoanRestController {

    @Autowired
    private ILoanService loanService;

    @PostMapping("/from-reservation/{id}")
    public ResponseEntity<?> createLoanFromReservation(@PathVariable Long id, Authentication auth) {
        try {
            Loan loan = loanService.createLoanFromReservation(id);
            return ResponseEntity.ok(loan);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
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
    public ResponseEntity<?> extendLoan(@PathVariable Long id, Authentication auth) {
        try {
            CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
            Long userId = userDetails.getUser().getId();
            Loan loan = loanService.extendLoan(id, userId);
            return ResponseEntity.ok(loan);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/{id}/return")
    public ResponseEntity<?> returnLoan(@PathVariable Long id, Authentication auth) {
        try {
            CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
            Long userId = userDetails.getUser().getId();
            Loan loan = loanService.returnLoan(id, userId);
            return ResponseEntity.ok(loan);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}

