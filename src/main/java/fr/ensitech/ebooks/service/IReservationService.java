package fr.ensitech.ebooks.service;

import fr.ensitech.ebooks.entity.Reservation;
import java.util.List;

public interface IReservationService {
    List<Reservation> createReservationsFromCart(Long userId);
    Reservation cancelReservation(Long reservationId, Long userId);
    List<Reservation> getUserReservations(Long userId);
    void checkAndExpireReservations();
    Reservation convertToLoan(Long reservationId);

    // Méthodes pour le bibliothécaire
    List<Reservation> findPendingReservations();
    List<Reservation> findByStatus(Reservation.ReservationStatus status);
    List<Reservation> findAll();
    Reservation validateReservation(Long reservationId, Long librarianId);
    void cancelReservation(Long reservationId);
}
