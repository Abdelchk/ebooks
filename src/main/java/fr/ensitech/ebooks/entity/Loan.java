package fr.ensitech.ebooks.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "loan")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id")
    private Reservation reservation; // Réservation d'origine

    @Column(nullable = false)
    private LocalDateTime loanDate;

    @Column(nullable = false)
    private LocalDateTime dueDate; // Date de retour prévue

    @Column
    private LocalDateTime returnDate; // Date de retour effective

    @Column(nullable = false)
    private Integer initialDuration; // Durée initiale en jours

    @Column(nullable = false)
    private Integer extensionCount; // Nombre de prolongations

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoanStatus status;

    @Column
    private LocalDateTime firstReminderSentAt;

    @Column
    private LocalDateTime secondReminderSentAt;

    @Column
    private LocalDateTime overdueNoticeSentAt;

    public enum LoanStatus {
        ACTIVE,       // En cours
        RETURNED,     // Rendu
        OVERDUE,      // En retard
        EXTENDED      // Prolongé
    }

    @PrePersist
    protected void onCreate() {
        loanDate = LocalDateTime.now();
        if (extensionCount == null) {
            extensionCount = 0;
        }
        if (status == null) {
            status = LoanStatus.ACTIVE;
        }
    }

    public boolean isOverdue() {
        return returnDate == null && LocalDateTime.now().isAfter(dueDate);
    }

    public long getDaysOverdue() {
        if (!isOverdue()) {
            return 0;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(dueDate, LocalDateTime.now());
    }

    public long getDaysUntilDue() {
        if (returnDate != null) {
            return 0;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDateTime.now(), dueDate);
    }
}

