import { reservationService } from './reservationService';
import api from './api';

jest.mock('./api');

describe('reservationService', () => {
    afterEach(() => {
        jest.clearAllMocks();
    });

    it('validateCart appelle POST /api/rest/reservations/validate-cart', async () => {
        api.post.mockResolvedValue({ data: { message: 'Panier validé' } });

        const result = await reservationService.validateCart();

        expect(api.post).toHaveBeenCalledWith('/api/rest/reservations/validate-cart');
        expect(result.message).toBe('Panier validé');
    });

    it('getUserReservations appelle GET /api/rest/reservations', async () => {
        const mockReservations = [{ id: 1, bookTitle: 'Les Misérables', status: 'PENDING' }];
        api.get.mockResolvedValue({ data: mockReservations });

        const result = await reservationService.getUserReservations();

        expect(api.get).toHaveBeenCalledWith('/api/rest/reservations');
        expect(result).toEqual(mockReservations);
    });

    it('cancelReservation appelle POST /api/rest/reservations/:id/cancel', async () => {
        api.post.mockResolvedValue({ data: { message: 'Réservation annulée' } });

        const result = await reservationService.cancelReservation(7);

        expect(api.post).toHaveBeenCalledWith('/api/rest/reservations/7/cancel');
        expect(result.message).toBe('Réservation annulée');
    });

    it('getUserReservations propage les erreurs réseau', async () => {
        api.get.mockRejectedValue(new Error('Network Error'));

        await expect(reservationService.getUserReservations()).rejects.toThrow('Network Error');
    });
});

