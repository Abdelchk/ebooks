import axios from 'axios';
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

jest.mock('axios');

const API_URL = 'http://localhost:8080/api/librarian';
const withCredentials = { withCredentials: true };

describe('librarianService', () => {
    afterEach(() => {
        jest.clearAllMocks();
    });

    it('getPendingReservations appelle GET /api/librarian/reservations/pending', async () => {
        const mockReservations = [{ id: 1, status: 'PENDING' }];
        axios.get.mockResolvedValue({ data: mockReservations });

        const result = await getPendingReservations();

        expect(axios.get).toHaveBeenCalledWith(`${API_URL}/reservations/pending`, withCredentials);
        expect(result).toEqual(mockReservations);
    });

    it('getAllReservations sans filtre appelle GET /api/librarian/reservations', async () => {
        axios.get.mockResolvedValue({ data: [] });

        await getAllReservations();

        expect(axios.get).toHaveBeenCalledWith(`${API_URL}/reservations`, withCredentials);
    });

    it('getAllReservations avec filtre ajoute le status en query param', async () => {
        axios.get.mockResolvedValue({ data: [] });

        await getAllReservations('ACTIVE');

        expect(axios.get).toHaveBeenCalledWith(`${API_URL}/reservations?status=ACTIVE`, withCredentials);
    });

    it('validateReservation appelle POST /api/librarian/reservations/:id/validate', async () => {
        axios.post.mockResolvedValue({ data: { message: 'Réservation validée' } });

        const result = await validateReservation(3);

        expect(axios.post).toHaveBeenCalledWith(
            `${API_URL}/reservations/3/validate`,
            {},
            withCredentials
        );
        expect(result.message).toBe('Réservation validée');
    });

    it('rejectReservation appelle POST avec la raison', async () => {
        axios.post.mockResolvedValue({ data: { message: 'Réservation rejetée' } });

        const result = await rejectReservation(5, 'Stock insuffisant');

        expect(axios.post).toHaveBeenCalledWith(
            `${API_URL}/reservations/5/reject`,
            { reason: 'Stock insuffisant' },
            withCredentials
        );
        expect(result.message).toBe('Réservation rejetée');
    });

    it('rejectReservation utilise une raison vide par défaut', async () => {
        axios.post.mockResolvedValue({ data: { message: 'Rejeté' } });

        await rejectReservation(5);

        expect(axios.post).toHaveBeenCalledWith(
            `${API_URL}/reservations/5/reject`,
            { reason: '' },
            withCredentials
        );
    });

    it('addBook appelle POST /api/librarian/books avec les données', async () => {
        const bookData = { title: 'Nouveau Livre', author: 'Auteur' };
        axios.post.mockResolvedValue({ data: { id: 100, ...bookData } });

        const result = await addBook(bookData);

        expect(axios.post).toHaveBeenCalledWith(`${API_URL}/books`, bookData, withCredentials);
        expect(result.id).toBe(100);
    });

    it('updateBook appelle PUT /api/librarian/books/:id', async () => {
        const bookData = { title: 'Livre modifié' };
        axios.put.mockResolvedValue({ data: { id: 10, ...bookData } });

        const result = await updateBook(10, bookData);

        expect(axios.put).toHaveBeenCalledWith(`${API_URL}/books/10`, bookData, withCredentials);
        expect(result.title).toBe('Livre modifié');
    });

    it('deleteBook appelle DELETE /api/librarian/books/:id', async () => {
        axios.delete.mockResolvedValue({ data: { message: 'Livre supprimé' } });

        const result = await deleteBook(10);

        expect(axios.delete).toHaveBeenCalledWith(`${API_URL}/books/10`, withCredentials);
        expect(result.message).toBe('Livre supprimé');
    });

    it('getAvailabilityAlerts appelle GET /api/librarian/alerts/availability', async () => {
        axios.get.mockResolvedValue({ data: [{ id: 1, bookId: 5 }] });

        const result = await getAvailabilityAlerts();

        expect(axios.get).toHaveBeenCalledWith(`${API_URL}/alerts/availability`, withCredentials);
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
        axios.get.mockRejectedValue(new Error('Forbidden'));

        await expect(getPendingReservations()).rejects.toThrow('Forbidden');
    });
});

