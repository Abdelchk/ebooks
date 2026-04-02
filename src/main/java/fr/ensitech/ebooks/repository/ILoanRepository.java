package fr.ensitech.ebooks.repository;

import fr.ensitech.ebooks.entity.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ILoanRepository extends JpaRepository<Loan, Long> {
    List<Loan> findByUserId(Long userId);
    List<Loan> findByUserIdAndStatus(Long userId, Loan.LoanStatus status);
    List<Loan> findByStatus(Loan.LoanStatus status);
    List<Loan> findByBookId(Long bookId);

    // Emprunts qui arrivent à échéance bientôt (pour les rappels)
    @Query("SELECT l FROM Loan l WHERE l.status = :status AND l.dueDate BETWEEN :startDate AND :endDate AND l.returnDate IS NULL")
    List<Loan> findLoansNearingDueDate(@Param("status") Loan.LoanStatus status,
                                       @Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate);

    // Emprunts en retard
    @Query("SELECT l FROM Loan l WHERE l.status IN ('ACTIVE', 'EXTENDED') AND l.dueDate < :now AND l.returnDate IS NULL")
    List<Loan> findOverdueLoans(@Param("now") LocalDateTime now);
}

