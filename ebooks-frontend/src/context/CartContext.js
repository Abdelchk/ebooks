import React, { createContext, useState, useContext, useCallback, useEffect, useMemo } from 'react';
import PropTypes from 'prop-types';
import { cartService } from '../services/cartService';
import { useAuth } from './AuthContext';

const CartContext = createContext(null);

export const CartProvider = ({ children }) => {
    const [cartCount, setCartCount] = useState(0);
    const { user } = useAuth();

    const refreshCartCount = useCallback(async () => {
        if (!user) {
            setCartCount(0);
            return;
        }
        try {
            const count = await cartService.getCartCount();
            setCartCount(count);
        } catch (e) {
            // Erreur réseau : on garde le compteur à 0
            console.warn('Impossible de récupérer le compteur panier:', e.message);
            setCartCount(0);
        }
    }, [user]);

    useEffect(() => {
        refreshCartCount().then(r => r);
    }, [refreshCartCount]);

    // useMemo évite de recréer l'objet value à chaque render
    const contextValue = useMemo(
        () => ({ cartCount, refreshCartCount }),
        [cartCount, refreshCartCount]
    );

    return (
        <CartContext.Provider value={contextValue}>
            {children}
        </CartContext.Provider>
    );
};

CartProvider.propTypes = {
    children: PropTypes.node.isRequired,
};

export const useCart = () => {
    const context = useContext(CartContext);
    if (!context) {
        throw new Error('useCart doit être utilisé dans un CartProvider');
    }
    return context;
};
