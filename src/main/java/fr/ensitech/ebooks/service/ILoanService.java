package fr.ensitech.ebooks.service;

import fr.ensitech.ebooks.entity.Loan;
import java.util.List;

public interface ILoanService {
    Loan createLoanFromReservation(Long reservationId);
    Loan extendLoan(Long loanId, Long userId);
    Loan returnLoan(Long loanId, Long userId);
    List<Loan> getUserLoans(Long userId);
    List<Loan> getActiveLoans(Long userId);
    void checkAndNotifyUpcomingDueDates();
    void checkAndNotifyOverdueLoans();
}
