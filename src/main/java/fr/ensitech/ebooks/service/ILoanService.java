package fr.ensitech.ebooks.service;

import fr.ensitech.ebooks.entity.Loan;
import java.util.List;

public interface ILoanService {
    Loan createLoanFromReservation(Long reservationId) throws Exception;
    Loan extendLoan(Long loanId, Long userId) throws Exception;
    Loan returnLoan(Long loanId, Long userId) throws Exception;
    List<Loan> getUserLoans(Long userId) throws Exception;
    List<Loan> getActiveLoans(Long userId) throws Exception;
    void checkAndNotifyUpcomingDueDates() throws Exception;
    void checkAndNotifyOverdueLoans() throws Exception;
}

