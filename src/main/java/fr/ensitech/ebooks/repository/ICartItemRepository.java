package fr.ensitech.ebooks.repository;

import fr.ensitech.ebooks.entity.CartItem;
import fr.ensitech.ebooks.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ICartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByUser(User user);
    List<CartItem> findByUserId(Long userId);
    Optional<CartItem> findByUserIdAndBookId(Long userId, Long bookId);
    void deleteByUserId(Long userId);
    long countByUserId(Long userId);
}

