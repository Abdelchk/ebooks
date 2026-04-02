package fr.ensitech.ebooks.service;

import fr.ensitech.ebooks.entity.Reservation;
import java.util.List;

public interface IReservationService {
    List<Reservation> createReservationsFromCart(Long userId) throws Exception;
    Reservation cancelReservation(Long reservationId, Long userId) throws Exception;
    List<Reservation> getUserReservations(Long userId) throws Exception;
    void checkAndExpireReservations() throws Exception;
    Reservation convertToLoan(Long reservationId) throws Exception;
}

