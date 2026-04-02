package fr.ensitech.ebooks.repository;

import fr.ensitech.ebooks.entity.StockAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IStockAlertRepository extends JpaRepository<StockAlert, Long> {
    
    List<StockAlert> findByUserId(Long userId);
    
    @Query("SELECT sa FROM StockAlert sa WHERE sa.user.id = :userId AND sa.book.id = :bookId AND sa.status = 'ACTIVE'")
    Optional<StockAlert> findActiveAlertByUserAndBook(@Param("userId") Long userId, @Param("bookId") Long bookId);
    
    @Query("SELECT sa FROM StockAlert sa WHERE sa.book.id = :bookId AND sa.status = 'ACTIVE'")
    List<StockAlert> findActiveAlertsByBook(@Param("bookId") Long bookId);
    
    @Query("SELECT sa FROM StockAlert sa WHERE sa.status = 'ACTIVE'")
    List<StockAlert> findAllActiveAlerts();
}

