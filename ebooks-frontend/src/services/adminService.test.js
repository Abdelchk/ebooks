import axios from 'axios';
import adminService, {
    getAllUsers,
    getUserById,
    createUser,
    updateUser,
    deleteUser,
    toggleUserStatus,
    changeUserRole,
    getStats,
} from './adminService';

jest.mock('axios');

const API_URL = 'http://localhost:8080/api/admin';
const withCredentials = { withCredentials: true };

describe('adminService', () => {
    afterEach(() => {
        jest.clearAllMocks();
    });

    it('getAllUsers appelle GET /api/admin/users', async () => {
        const mockUsers = [{ id: 1, email: 'user@test.com', role: 'USER' }];
        axios.get.mockResolvedValue({ data: mockUsers });

        const result = await getAllUsers();

        expect(axios.get).toHaveBeenCalledWith(`${API_URL}/users`, withCredentials);
        expect(result).toEqual(mockUsers);
    });

    it('getUserById appelle GET /api/admin/users/:id', async () => {
        const mockUser = { id: 5, email: 'user5@test.com' };
        axios.get.mockResolvedValue({ data: mockUser });

        const result = await getUserById(5);

        expect(axios.get).toHaveBeenCalledWith(`${API_URL}/users/5`, withCredentials);
        expect(result.id).toBe(5);
    });

    it('createUser appelle POST /api/admin/users avec les données', async () => {
        const userData = { email: 'new@test.com', role: 'USER' };
        axios.post.mockResolvedValue({ data: { id: 99, ...userData } });

        const result = await createUser(userData);

        expect(axios.post).toHaveBeenCalledWith(`${API_URL}/users`, userData, withCredentials);
        expect(result.id).toBe(99);
    });

    it('updateUser appelle PUT /api/admin/users/:id avec les données', async () => {
        const userData = { email: 'updated@test.com' };
        axios.put.mockResolvedValue({ data: { id: 3, ...userData } });

        const result = await updateUser(3, userData);

        expect(axios.put).toHaveBeenCalledWith(`${API_URL}/users/3`, userData, withCredentials);
        expect(result.email).toBe('updated@test.com');
    });

    it('deleteUser appelle DELETE /api/admin/users/:id', async () => {
        axios.delete.mockResolvedValue({ data: { message: 'Utilisateur supprimé' } });

        const result = await deleteUser(7);

        expect(axios.delete).toHaveBeenCalledWith(`${API_URL}/users/7`, withCredentials);
        expect(result.message).toBe('Utilisateur supprimé');
    });

    it('toggleUserStatus appelle PATCH /api/admin/users/:id/toggle-status', async () => {
        axios.patch.mockResolvedValue({ data: { id: 2, active: false } });

        const result = await toggleUserStatus(2);

        expect(axios.patch).toHaveBeenCalledWith(
            `${API_URL}/users/2/toggle-status`,
            {},
            withCredentials
        );
        expect(result.active).toBe(false);
    });

    it('changeUserRole appelle PATCH /api/admin/users/:id/role avec le rôle', async () => {
        axios.patch.mockResolvedValue({ data: { id: 4, role: 'LIBRARIAN' } });

        const result = await changeUserRole(4, 'LIBRARIAN');

        expect(axios.patch).toHaveBeenCalledWith(
            `${API_URL}/users/4/role`,
            { role: 'LIBRARIAN' },
            withCredentials
        );
        expect(result.role).toBe('LIBRARIAN');
    });

    it('getStats appelle GET /api/admin/stats', async () => {
        const mockStats = { totalUsers: 100, activeLoans: 25 };
        axios.get.mockResolvedValue({ data: mockStats });

        const result = await getStats();

        expect(axios.get).toHaveBeenCalledWith(`${API_URL}/stats`, withCredentials);
        expect(result.totalUsers).toBe(100);
    });

    it('adminService exporte bien les méthodes', () => {
        expect(typeof adminService.getAllUsers).toBe('function');
        expect(typeof adminService.getUserById).toBe('function');
        expect(typeof adminService.createUser).toBe('function');
        expect(typeof adminService.updateUser).toBe('function');
        expect(typeof adminService.deleteUser).toBe('function');
        expect(typeof adminService.toggleUserStatus).toBe('function');
        expect(typeof adminService.changeUserRole).toBe('function');
        expect(typeof adminService.getStats).toBe('function');
    });

    it('getAllUsers propage les erreurs réseau', async () => {
        axios.get.mockRejectedValue(new Error('Forbidden'));

        await expect(getAllUsers()).rejects.toThrow('Forbidden');
    });
});

