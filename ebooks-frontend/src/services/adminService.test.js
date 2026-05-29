import api from './api';
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

jest.mock('./api', () => ({
    get: jest.fn(),
    post: jest.fn(),
    put: jest.fn(),
    delete: jest.fn(),
    patch: jest.fn(),
    interceptors: { response: { use: jest.fn() } },
}));

const API_URL = '/api/admin';

describe('adminService', () => {
    afterEach(() => {
        jest.clearAllMocks();
    });

    it('getAllUsers appelle GET /api/admin/users', async () => {
        const mockUsers = [{ id: 1, email: 'user@test.com', role: 'USER' }];
        api.get.mockResolvedValue({ data: mockUsers });

        const result = await getAllUsers();

        expect(api.get).toHaveBeenCalledWith(`${API_URL}/users`);
        expect(result).toEqual(mockUsers);
    });

    it('getUserById appelle GET /api/admin/users/:id', async () => {
        const mockUser = { id: 5, email: 'user5@test.com' };
        api.get.mockResolvedValue({ data: mockUser });

        const result = await getUserById(5);

        expect(api.get).toHaveBeenCalledWith(`${API_URL}/users/5`);
        expect(result.id).toBe(5);
    });

    it('createUser appelle POST /api/admin/users avec les données', async () => {
        const userData = { email: 'new@test.com', role: 'USER' };
        api.post.mockResolvedValue({ data: { id: 99, ...userData } });

        const result = await createUser(userData);

        expect(api.post).toHaveBeenCalledWith(`${API_URL}/users`, userData);
        expect(result.id).toBe(99);
    });

    it('updateUser appelle PUT /api/admin/users/:id avec les données', async () => {
        const userData = { email: 'updated@test.com' };
        api.put.mockResolvedValue({ data: { id: 3, ...userData } });

        const result = await updateUser(3, userData);

        expect(api.put).toHaveBeenCalledWith(`${API_URL}/users/3`, userData);
        expect(result.email).toBe('updated@test.com');
    });

    it('deleteUser appelle DELETE /api/admin/users/:id', async () => {
        api.delete.mockResolvedValue({ data: { message: 'Utilisateur supprimé' } });

        const result = await deleteUser(7);

        expect(api.delete).toHaveBeenCalledWith(`${API_URL}/users/7`);
        expect(result.message).toBe('Utilisateur supprimé');
    });

    it('toggleUserStatus appelle PATCH /api/admin/users/:id/toggle-status', async () => {
        api.patch.mockResolvedValue({ data: { id: 2, active: false } });

        const result = await toggleUserStatus(2);

        expect(api.patch).toHaveBeenCalledWith(`${API_URL}/users/2/toggle-status`, {});
        expect(result.active).toBe(false);
    });

    it('changeUserRole appelle PATCH /api/admin/users/:id/role avec le rôle', async () => {
        api.patch.mockResolvedValue({ data: { id: 4, role: 'LIBRARIAN' } });

        const result = await changeUserRole(4, 'LIBRARIAN');

        expect(api.patch).toHaveBeenCalledWith(`${API_URL}/users/4/role`, { role: 'LIBRARIAN' });
        expect(result.role).toBe('LIBRARIAN');
    });

    it('getStats appelle GET /api/admin/stats', async () => {
        const mockStats = { totalUsers: 100, activeLoans: 25 };
        api.get.mockResolvedValue({ data: mockStats });

        const result = await getStats();

        expect(api.get).toHaveBeenCalledWith(`${API_URL}/stats`);
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
        api.get.mockRejectedValue(new Error('Forbidden'));

        await expect(getAllUsers()).rejects.toThrow('Forbidden');
    });
});
