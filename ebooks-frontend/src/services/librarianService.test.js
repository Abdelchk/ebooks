import api from './api';
import librarianService, {
    getPendingReservations,
    getAllReservations,
    validateReservation,
    rejectReservation,
    addBook,
    updateBook,
    deleteBook,
    getAvailabilityAlerts,
} from './librarianService';

jest.mock('./api', () => ({
    get: jest.fn(),
    post: jest.fn(),
    put: jest.fn(),
    delete: jest.fn(),
    patch: jest.fn(),
    interceptors: { response: { use: jest.fn() } },
}));

const API_URL = '/api/librarian';

describe('librarianService', () => {
    afterEach(() => {
        jest.clearAllMocks();
    });

    it('getPendingReservations appelle GET /api/librarian/reservations/pending', async () => {
        const mockReservations = [{ id: 1, status: 'PENDING' }];
        api.get.mockResolvedValue({ data: mockReservations });

        const result = await getPendingReservations();

        expect(api.get).toHaveBeenCalledWith(`${API_URL}/reservations/pending`);
        expect(result).toEqual(mockReservations);
    });

    it('getAllReservations sans filtre appelle GET /api/librarian/reservations', async () => {
        api.get.mockResolvedValue({ data: [] });

        await getAllReservations();

        expect(api.get).toHaveBeenCalledWith(`${API_URL}/reservations`);
    });

    it('getAllReservations avec filtre ajoute le status en query param', async () => {
        api.get.mockResolvedValue({ data: [] });

        await getAllReservations('ACTIVE');

        expect(api.get).toHaveBeenCalledWith(`${API_URL}/reservations?status=ACTIVE`);
    });

    it('validateReservation appelle POST /api/librarian/reservations/:id/validate', async () => {
        api.post.mockResolvedValue({ data: { message: 'Réservation validée' } });

        const result = await validateReservation(3);

        expect(api.post).toHaveBeenCalledWith(`${API_URL}/reservations/3/validate`, {});
        expect(result.message).toBe('Réservation validée');
    });

    it('rejectReservation appelle POST avec la raison', async () => {
        api.post.mockResolvedValue({ data: { message: 'Réservation rejetée' } });

        const result = await rejectReservation(5, 'Stock insuffisant');

        expect(api.post).toHaveBeenCalledWith(
            `${API_URL}/reservations/5/reject`,
            { reason: 'Stock insuffisant' }
        );
        expect(result.message).toBe('Réservation rejetée');
    });

    it('rejectReservation utilise une raison vide par défaut', async () => {
        api.post.mockResolvedValue({ data: { message: 'Rejeté' } });

        await rejectReservation(5);

        expect(api.post).toHaveBeenCalledWith(
            `${API_URL}/reservations/5/reject`,
            { reason: '' }
        );
    });

    it('addBook appelle POST /api/librarian/books avec les données', async () => {
        const bookData = { title: 'Nouveau Livre', author: 'Auteur' };
        api.post.mockResolvedValue({ data: { id: 100, ...bookData } });

        const result = await addBook(bookData);

        expect(api.post).toHaveBeenCalledWith(`${API_URL}/books`, bookData);
        expect(result.id).toBe(100);
    });

    it('updateBook appelle PUT /api/librarian/books/:id', async () => {
        const bookData = { title: 'Livre modifié' };
        api.put.mockResolvedValue({ data: { id: 10, ...bookData } });

        const result = await updateBook(10, bookData);

        expect(api.put).toHaveBeenCalledWith(`${API_URL}/books/10`, bookData);
        expect(result.title).toBe('Livre modifié');
    });

    it('deleteBook appelle DELETE /api/librarian/books/:id', async () => {
        api.delete.mockResolvedValue({ data: { message: 'Livre supprimé' } });

        const result = await deleteBook(10);

        expect(api.delete).toHaveBeenCalledWith(`${API_URL}/books/10`);
        expect(result.message).toBe('Livre supprimé');
    });

    it('getAvailabilityAlerts appelle GET /api/librarian/alerts/availability', async () => {
        api.get.mockResolvedValue({ data: [{ id: 1, bookId: 5 }] });

        const result = await getAvailabilityAlerts();

        expect(api.get).toHaveBeenCalledWith(`${API_URL}/alerts/availability`);
        expect(result).toHaveLength(1);
    });

    it('librarianService exporte les méthodes', () => {
        expect(typeof librarianService.getPendingReservations).toBe('function');
        expect(typeof librarianService.getAllReservations).toBe('function');
        expect(typeof librarianService.validateReservation).toBe('function');
        expect(typeof librarianService.rejectReservation).toBe('function');
        expect(typeof librarianService.addBook).toBe('function');
        expect(typeof librarianService.updateBook).toBe('function');
        expect(typeof librarianService.deleteBook).toBe('function');
        expect(typeof librarianService.getAvailabilityAlerts).toBe('function');
    });

    it('getPendingReservations propage les erreurs', async () => {
        api.get.mockRejectedValue(new Error('Forbidden'));

        await expect(getPendingReservations()).rejects.toThrow('Forbidden');
    });
});
