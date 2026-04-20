package fr.ensitech.ebooks.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "reservation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reservation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @JsonIgnoreProperties({"password", "passwordHistory", "verificationToken", "resetPasswordToken", "resetTokenExpiryDate", "lastPasswordUpdateDate", "lastVerificationCodeSentAt", "hibernateLazyInitializer", "handler"})
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;
    
    @Column(nullable = false)
    private LocalDateTime reservationDate;
    
    @Column(nullable = false)
    private LocalDateTime expirationDate; // Date limite pour récupérer le livre (72h par défaut)
    
    @Column(nullable = false)
    private Integer loanDuration; // Durée d'emprunt prévue en jours
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(20) DEFAULT 'PENDING'")
    private ReservationStatus status;
    
    @Column
    private LocalDateTime cancelledAt;
    
    @Column
    private LocalDateTime convertedToLoanAt;
    
    @Column
    private LocalDateTime validatedAt; // Date de validation par le bibliothécaire

    @Column
    private Long validatedBy; // ID du bibliothécaire qui a validé

    public enum ReservationStatus {
        PENDING,      // En attente de validation par le bibliothécaire
        VALIDATED,    // Validée par le bibliothécaire, en attente de retrait
        CANCELLED,    // Annulée par l'utilisateur
        EXPIRED,      // Expirée (non retirée à temps)
        CONVERTED     // Convertie en emprunt
    }
    
    @PrePersist
    protected void onCreate() {
        reservationDate = LocalDateTime.now();
        if (expirationDate == null) {
            expirationDate = reservationDate.plusHours(72); // 72h pour retirer le livre
        }
        if (status == null) {
            status = ReservationStatus.PENDING;
        }
    }
}

