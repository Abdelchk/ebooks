import React, { createContext, useState, useContext, useCallback, useEffect } from 'react';
import { cartService } from '../services/cartService';
import { useAuth } from './AuthContext';

const CartContext = createContext(null);

export const CartProvider = ({ children }) => {
    const [cartCount, setCartCount] = useState(0);
    const { user } = useAuth();

    // Rafraîchit le compteur depuis l'API
    const refreshCartCount = useCallback(async () => {
        if (!user) {
            setCartCount(0);
            return;
        }
        try {
            const count = await cartService.getCartCount();
            setCartCount(count);
        } catch {
            setCartCount(0);
        }
    }, [user]);

    // Chargement initial + rechargement si l'utilisateur change
    useEffect(() => {
        refreshCartCount().then(r => r);
    }, [refreshCartCount]);

    return (
        <CartContext.Provider value={{ cartCount, refreshCartCount }}>
            {children}
        </CartContext.Provider>
    );
};

export const useCart = () => {
    const context = useContext(CartContext);
    if (!context) {
        throw new Error('useCart doit être utilisé dans un CartProvider');
    }
    return context;
};

