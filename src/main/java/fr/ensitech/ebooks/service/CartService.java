package fr.ensitech.ebooks.service;

import fr.ensitech.ebooks.entity.Book;
import fr.ensitech.ebooks.entity.CartItem;
import fr.ensitech.ebooks.entity.User;
import fr.ensitech.ebooks.repository.IBookRepository;
import fr.ensitech.ebooks.repository.ICartItemRepository;
import fr.ensitech.ebooks.repository.IUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CartService implements ICartService {

    @Autowired
    private ICartItemRepository cartItemRepository;

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private IBookRepository bookRepository;

    @Override
    @Transactional
    public CartItem addToCart(Long userId, Long bookId, Integer loanDuration) throws Exception {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new Exception("Utilisateur non trouvé"));

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new Exception("Livre non trouvé"));

        if (book.getQuantity() <= 0) {
            throw new Exception("Livre non disponible en stock");
        }

        // Vérifier si le livre est déjà dans le panier
        var existingItem = cartItemRepository.findByUserIdAndBookId(userId, bookId);
        if (existingItem.isPresent()) {
            throw new Exception("Ce livre est déjà dans votre panier");
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
    public void removeFromCart(Long cartItemId, Long userId) throws Exception {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new Exception("Article non trouvé dans le panier"));

        if (!cartItem.getUser().getId().equals(userId)) {
            throw new Exception("Vous n'êtes pas autorisé à supprimer cet article");
        }

        cartItemRepository.delete(cartItem);
    }

    @Override
    @Transactional
    public void clearCart(Long userId) throws Exception {
        cartItemRepository.deleteByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CartItem> getCartItems(Long userId) throws Exception {
        return cartItemRepository.findByUserId(userId);
    }

    @Override
    @Transactional
    public CartItem updateLoanDuration(Long cartItemId, Long userId, Integer newDuration) throws Exception {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new Exception("Article non trouvé"));

        if (!cartItem.getUser().getId().equals(userId)) {
            throw new Exception("Non autorisé");
        }

        if (newDuration < 1 || newDuration > 30) {
            throw new Exception("La durée d'emprunt doit être entre 1 et 30 jours");
        }

        cartItem.setLoanDuration(newDuration);
        return cartItemRepository.save(cartItem);
    }

    @Override
    @Transactional(readOnly = true)
    public long getCartItemCount(Long userId) throws Exception {
        return cartItemRepository.countByUserId(userId);
    }
}

