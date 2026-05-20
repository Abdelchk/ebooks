package fr.ensitech.ebooks.service;

import fr.ensitech.ebooks.entity.CartItem;

import java.util.List;

public interface ICartService {
    CartItem addToCart(Long userId, Long bookId, Integer loanDuration);
    void removeFromCart(Long cartItemId, Long userId);
    void clearCart(Long userId);
    List<CartItem> getCartItems(Long userId);
    CartItem updateLoanDuration(Long cartItemId, Long userId, Integer newDuration);
    long getCartItemCount(Long userId);
}
