import { cartService } from './cartService';
import api from './api';

jest.mock('./api');

describe('cartService', () => {
    afterEach(() => {
        jest.clearAllMocks();
    });

    it('getCart appelle GET /api/rest/cart et retourne les données', async () => {
        const mockCart = [{ id: 1, bookId: 10, loanDuration: 14 }];
        api.get.mockResolvedValue({ data: mockCart });

        const result = await cartService.getCart();

        expect(api.get).toHaveBeenCalledWith('/api/rest/cart');
        expect(result).toEqual(mockCart);
    });

    it('getCart propage les erreurs réseau', async () => {
        api.get.mockRejectedValue(new Error('Network Error'));

        await expect(cartService.getCart()).rejects.toThrow('Network Error');
    });

    it('addToCart appelle POST avec bookId et loanDuration par défaut (14)', async () => {
        api.post.mockResolvedValue({ data: { id: 5, bookId: 10, loanDuration: 14 } });

        const result = await cartService.addToCart(10);

        expect(api.post).toHaveBeenCalledWith('/api/rest/cart/add', { bookId: 10, loanDuration: 14 });
        expect(result.loanDuration).toBe(14);
    });

    it('addToCart accepte une durée personnalisée', async () => {
        api.post.mockResolvedValue({ data: { id: 5, bookId: 10, loanDuration: 7 } });

        const result = await cartService.addToCart(10, 7);

        expect(api.post).toHaveBeenCalledWith('/api/rest/cart/add', { bookId: 10, loanDuration: 7 });
        expect(result.loanDuration).toBe(7);
    });

    it('removeFromCart appelle DELETE /api/rest/cart/:id', async () => {
        api.delete.mockResolvedValue({ data: { message: 'Supprimé' } });

        const result = await cartService.removeFromCart(5);

        expect(api.delete).toHaveBeenCalledWith('/api/rest/cart/5');
        expect(result.message).toBe('Supprimé');
    });

    it('clearCart appelle DELETE /api/rest/cart/clear', async () => {
        api.delete.mockResolvedValue({ data: { message: 'Panier vidé' } });

        const result = await cartService.clearCart();

        expect(api.delete).toHaveBeenCalledWith('/api/rest/cart/clear');
        expect(result.message).toBe('Panier vidé');
    });

    it('updateDuration appelle PUT avec cartItemId et duration', async () => {
        api.put.mockResolvedValue({ data: { id: 5, loanDuration: 21 } });

        const result = await cartService.updateDuration(5, 21);

        expect(api.put).toHaveBeenCalledWith('/api/rest/cart/5/duration', { duration: 21 });
        expect(result.loanDuration).toBe(21);
    });

    it('getCartCount appelle GET /api/rest/cart/count et retourne count', async () => {
        api.get.mockResolvedValue({ data: { count: 3 } });

        const result = await cartService.getCartCount();

        expect(api.get).toHaveBeenCalledWith('/api/rest/cart/count');
        expect(result).toBe(3);
    });

    it('getCartCount retourne 0 si le panier est vide', async () => {
        api.get.mockResolvedValue({ data: { count: 0 } });

        const result = await cartService.getCartCount();

        expect(result).toBe(0);
    });
});

