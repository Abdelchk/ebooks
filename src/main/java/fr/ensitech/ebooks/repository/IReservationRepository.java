package fr.ensitech.ebooks.repository;

import fr.ensitech.ebooks.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByUserId(Long userId);
    List<Reservation> findByUserIdAndStatus(Long userId, Reservation.ReservationStatus status);
    List<Reservation> findByStatus(Reservation.ReservationStatus status);
    List<Reservation> findByStatusAndExpirationDateBefore(Reservation.ReservationStatus status, LocalDateTime date);
    List<Reservation> findByBookIdAndStatus(Long bookId, Reservation.ReservationStatus status);
}

