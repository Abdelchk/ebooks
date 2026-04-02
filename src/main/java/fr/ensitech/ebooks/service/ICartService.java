package fr.ensitech.ebooks.service;

import fr.ensitech.ebooks.entity.CartItem;
import fr.ensitech.ebooks.entity.User;

import java.util.List;

public interface ICartService {
    CartItem addToCart(Long userId, Long bookId, Integer loanDuration) throws Exception;
    void removeFromCart(Long cartItemId, Long userId) throws Exception;
    void clearCart(Long userId) throws Exception;
    List<CartItem> getCartItems(Long userId) throws Exception;
    CartItem updateLoanDuration(Long cartItemId, Long userId, Integer newDuration) throws Exception;
    long getCartItemCount(Long userId) throws Exception;
}


