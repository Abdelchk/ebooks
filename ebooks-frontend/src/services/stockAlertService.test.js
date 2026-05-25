import { stockAlertService } from './stockAlertService';
import api from './api';

jest.mock('./api');

describe('stockAlertService', () => {
    afterEach(() => {
        jest.clearAllMocks();
    });

    it('createAlert appelle POST /api/rest/stock-alerts/create/:bookId', async () => {
        api.post.mockResolvedValue({ data: { id: 1, bookId: 10, status: 'ACTIVE' } });

        const result = await stockAlertService.createAlert(10);

        expect(api.post).toHaveBeenCalledWith('/api/rest/stock-alerts/create/10');
        expect(result.bookId).toBe(10);
    });

    it('getUserAlerts appelle GET /api/rest/stock-alerts', async () => {
        const mockAlerts = [{ id: 1, bookTitle: 'Les Misérables' }];
        api.get.mockResolvedValue({ data: mockAlerts });

        const result = await stockAlertService.getUserAlerts();

        expect(api.get).toHaveBeenCalledWith('/api/rest/stock-alerts');
        expect(result).toEqual(mockAlerts);
    });

    it('cancelAlert appelle POST /api/rest/stock-alerts/:id/cancel', async () => {
        api.post.mockResolvedValue({ data: { message: 'Alerte annulée' } });

        const result = await stockAlertService.cancelAlert(5);

        expect(api.post).toHaveBeenCalledWith('/api/rest/stock-alerts/5/cancel');
        expect(result.message).toBe('Alerte annulée');
    });

    it('createAlert propage les erreurs réseau', async () => {
        api.post.mockRejectedValue(new Error('Network Error'));

        await expect(stockAlertService.createAlert(1)).rejects.toThrow('Network Error');
    });
});

