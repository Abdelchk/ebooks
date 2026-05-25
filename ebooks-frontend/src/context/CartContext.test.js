import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import { CartProvider, useCart } from './CartContext';
import { cartService } from '../services/cartService';

// ── Mocks ────────────────────────────────────────────────────────────────────
jest.mock('../services/cartService');
jest.mock('./AuthContext', () => ({
    useAuth: jest.fn()
}));

const { useAuth } = require('./AuthContext');
const DEFAULT_USER = { id: 1, email: 'test@test.com' };

// ── Composant consommateur ────────────────────────────────────────────────────
const TestConsumer = () => {
    const { cartCount, refreshCartCount } = useCart();
    return (
        <div>
            <span data-testid="cart-count">{cartCount}</span>
            <button onClick={refreshCartCount}>Rafraîchir</button>
        </div>
    );
};

// ── Tests ─────────────────────────────────────────────────────────────────────
describe('CartContext', () => {
    beforeEach(() => {
        jest.clearAllMocks();
        useAuth.mockReturnValue({ user: DEFAULT_USER });
        jest.spyOn(console, 'warn').mockImplementation(() => {});
    });

    afterEach(() => {
        console.warn.mockRestore();
    });

    it('initialise cartCount à 0', async () => {
        cartService.getCartCount = jest.fn().mockResolvedValue(0);
        render(<CartProvider><TestConsumer /></CartProvider>);
        await waitFor(() => {
            expect(screen.getByTestId('cart-count').textContent).toBe('0');
        });
    });

    it('charge le compteur depuis l\'API au montage', async () => {
        cartService.getCartCount = jest.fn().mockResolvedValue(3);
        render(<CartProvider><TestConsumer /></CartProvider>);
        await waitFor(() => {
            expect(screen.getByTestId('cart-count').textContent).toBe('3');
        });
        expect(cartService.getCartCount).toHaveBeenCalledTimes(1);
    });

    it('met cartCount à 0 si pas d\'utilisateur connecté', async () => {
        useAuth.mockReturnValue({ user: null });
        render(<CartProvider><TestConsumer /></CartProvider>);
        await waitFor(() => {
            expect(screen.getByTestId('cart-count').textContent).toBe('0');
        });
        expect(cartService.getCartCount).not.toHaveBeenCalled();
    });

    it('gère les erreurs API silencieusement (cartCount reste à 0)', async () => {
        cartService.getCartCount = jest.fn().mockRejectedValue(new Error('API Error'));
        render(<CartProvider><TestConsumer /></CartProvider>);
        await waitFor(() => {
            expect(screen.getByTestId('cart-count').textContent).toBe('0');
        });
    });
});
