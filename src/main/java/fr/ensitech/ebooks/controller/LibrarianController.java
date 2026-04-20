package fr.ensitech.ebooks.controller;

import fr.ensitech.ebooks.entity.Book;
import fr.ensitech.ebooks.entity.Reservation;
import fr.ensitech.ebooks.securingweb.CustomUserDetails;
import fr.ensitech.ebooks.service.IBookService;
import fr.ensitech.ebooks.service.IReservationService;
import fr.ensitech.ebooks.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/librarian")
@RequiredArgsConstructor
public class LibrarianController {

    private final IReservationService reservationService;
    private final IBookService bookService;

    // Obtenir toutes les réservations en attente de validation
    @GetMapping("/reservations/pending")
    public ResponseEntity<?> getPendingReservations() {
        List<Reservation> reservations = reservationService.findPendingReservations();
        return ResponseEntity.ok(reservations);
    }

    // Obtenir toutes les réservations (avec filtres)
    @GetMapping("/reservations")
    public ResponseEntity<?> getAllReservations(@RequestParam(required = false) String status) {
        List<Reservation> reservations;
        if (status != null && !status.isEmpty()) {
            reservations = reservationService.findByStatus(Reservation.ReservationStatus.valueOf(status.toUpperCase()));
        } else {
            reservations = reservationService.findAll();
        }
        return ResponseEntity.ok(reservations);
    }

    // Valider une réservation
    @PostMapping("/reservations/{id}/validate")
    public ResponseEntity<?> validateReservation(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            Long librarianId = userDetails.getUser().getId();
            Reservation reservation = reservationService.validateReservation(id, librarianId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Réservation validée avec succès",
                "reservation", reservation
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // Rejeter une réservation
    @PostMapping("/reservations/{id}/reject")
    public ResponseEntity<?> rejectReservation(@PathVariable Long id) {
        try {
            reservationService.cancelReservation(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "Réservation rejetée"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // GESTION DES LIVRES

    // Ajouter un livre
    @PostMapping("/books")
    public ResponseEntity<?> addBook(@RequestBody Book book) {
        try {
            Book savedBook = bookService.save(book);
            return ResponseEntity.ok(Map.of("success", true, "message", "Livre ajouté avec succès", "book", savedBook));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // Modifier un livre (avec notification de restockage)
    @PutMapping("/books/{id}")
    public ResponseEntity<?> updateBook(@PathVariable Long id, @RequestBody Book book) {
        try {
            book.setId(id);
            Book updatedBook = bookService.updateBook(book);
            return ResponseEntity.ok(Map.of("success", true, "message", "Livre modifié avec succès", "book", updatedBook));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // Supprimer un livre
    @DeleteMapping("/books/{id}")
    public ResponseEntity<?> deleteBook(@PathVariable Long id) {
        try {
            bookService.deleteById(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "Livre supprimé avec succès"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // Consulter les alertes de disponibilité de livres
    @GetMapping("/alerts/availability")
    public ResponseEntity<?> getAvailabilityAlerts() {
        List<Book> lowStockBooks = bookService.findLowStockBooks();
        return ResponseEntity.ok(Map.of("success", true, "alerts", lowStockBooks));
    }
}


