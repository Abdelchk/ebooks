package fr.ensitech.ebooks.service;

import fr.ensitech.ebooks.entity.Book;
import fr.ensitech.ebooks.entity.CartItem;
import fr.ensitech.ebooks.entity.User;
import fr.ensitech.ebooks.repository.IBookRepository;
import fr.ensitech.ebooks.repository.ICartItemRepository;
import fr.ensitech.ebooks.repository.IUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService implements ICartService {

    private final ICartItemRepository cartItemRepository;
    private final IUserRepository userRepository;
    private final IBookRepository bookRepository;

    @Override
    @Transactional
    public CartItem addToCart(Long userId, Long bookId, Integer loanDuration) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé"));

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Livre non trouvé"));

        if (book.getQuantity() <= 0) {
            throw new IllegalStateException("Livre non disponible en stock");
        }

        // Vérifier si le livre est déjà dans le panier
        var existingItem = cartItemRepository.findByUserIdAndBookId(userId, bookId);
        if (existingItem.isPresent()) {
            throw new IllegalStateException("Ce livre est déjà dans votre panier");
        }

        CartItem cartItem = CartItem.builder()
                .user(user)
                .book(book)
                .loanDuration(loanDuration != null ? loanDuration : 14)
                .build();

        return cartItemRepository.save(cartItem);
    }

    @Override
    @Transactional
    public void removeFromCart(Long cartItemId, Long userId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new IllegalArgumentException("Article non trouvé dans le panier"));

        if (!cartItem.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Vous n'êtes pas autorisé à supprimer cet article");
        }

        cartItemRepository.delete(cartItem);
    }

    @Override
    @Transactional
    public void clearCart(Long userId) {
        cartItemRepository.deleteByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CartItem> getCartItems(Long userId) {
        return cartItemRepository.findByUserId(userId);
    }

    @Override
    @Transactional
    public CartItem updateLoanDuration(Long cartItemId, Long userId, Integer newDuration) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new IllegalArgumentException("Article non trouvé"));

        if (!cartItem.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Non autorisé");
        }

        if (newDuration < 1 || newDuration > 30) {
            throw new IllegalArgumentException("La durée d'emprunt doit être entre 1 et 30 jours");
        }

        cartItem.setLoanDuration(newDuration);
        return cartItemRepository.save(cartItem);
    }

    @Override
    @Transactional(readOnly = true)
    public long getCartItemCount(Long userId) {
        return cartItemRepository.countByUserId(userId);
    }
}
