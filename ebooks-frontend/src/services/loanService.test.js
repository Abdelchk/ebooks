import { loanService } from './loanService';
import api from './api';

jest.mock('./api');

describe('loanService', () => {
    afterEach(() => {
        jest.clearAllMocks();
    });

    it('getUserLoans appelle GET /api/rest/loans', async () => {
        const mockLoans = [{ id: 1, bookTitle: 'Les Misérables' }];
        api.get.mockResolvedValue({ data: mockLoans });

        const result = await loanService.getUserLoans();

        expect(api.get).toHaveBeenCalledWith('/api/rest/loans');
        expect(result).toEqual(mockLoans);
    });

    it('getActiveLoans appelle GET /api/rest/loans/active', async () => {
        api.get.mockResolvedValue({ data: [{ id: 2, status: 'ACTIVE' }] });

        const result = await loanService.getActiveLoans();

        expect(api.get).toHaveBeenCalledWith('/api/rest/loans/active');
        expect(result[0].status).toBe('ACTIVE');
    });

    it('extendLoan appelle POST /api/rest/loans/:id/extend', async () => {
        api.post.mockResolvedValue({ data: { message: 'Emprunt prolongé' } });

        const result = await loanService.extendLoan(10);

        expect(api.post).toHaveBeenCalledWith('/api/rest/loans/10/extend');
        expect(result.message).toBe('Emprunt prolongé');
    });

    it('returnLoan appelle POST /api/rest/loans/:id/return', async () => {
        api.post.mockResolvedValue({ data: { message: 'Livre rendu' } });

        const result = await loanService.returnLoan(10);

        expect(api.post).toHaveBeenCalledWith('/api/rest/loans/10/return');
        expect(result.message).toBe('Livre rendu');
    });

    it('createFromReservation appelle POST avec reservationId', async () => {
        api.post.mockResolvedValue({ data: { id: 99, bookTitle: 'Roman test' } });

        const result = await loanService.createFromReservation(5);

        expect(api.post).toHaveBeenCalledWith('/api/rest/loans/from-reservation/5');
        expect(result.id).toBe(99);
    });

    it('getUserLoans propage les erreurs réseau', async () => {
        api.get.mockRejectedValue(new Error('Unauthorized'));

        await expect(loanService.getUserLoans()).rejects.toThrow('Unauthorized');
    });
});

