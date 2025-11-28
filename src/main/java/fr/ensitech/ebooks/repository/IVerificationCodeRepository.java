package fr.ensitech.ebooks.repository;

import fr.ensitech.ebooks.entity.VerificationCode;
import fr.ensitech.ebooks.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface IVerificationCodeRepository extends JpaRepository<VerificationCode, Long> {
    Optional<VerificationCode> findByUserIdAndCodeAndUsedFalse(User user, String code);
    @Modifying
    @Transactional
    @Query("DELETE FROM VerificationCode v WHERE v.userId = :user AND v.expiryDate < :now")
    void deleteExpiredCodes(@Param("user") User user, @Param("now") LocalDateTime now);

    @Query("SELECT COUNT(v) FROM VerificationCode v WHERE v.userId = :user AND v.used = false AND v.expiryDate > :now")
    long countActiveCodesByUser(@Param("user") User user, @Param("now") LocalDateTime now);

}
