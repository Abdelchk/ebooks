import { userService } from './userService';
import api from './api';

jest.mock('./api');

describe('userService', () => {
    afterEach(() => {
        jest.clearAllMocks();
    });

    it('getCurrentUser appelle GET /api/rest/users/me', async () => {
        const mockUser = { id: 1, email: 'test@test.com', firstName: 'Jean' };
        api.get.mockResolvedValue({ data: mockUser });

        const result = await userService.getCurrentUser();

        expect(api.get).toHaveBeenCalledWith('/api/rest/users/me');
        expect(result.email).toBe('test@test.com');
    });

    it('updateUser appelle PUT /api/rest/users/update avec les données', async () => {
        const userData = { firstName: 'Jean', lastName: 'Dupont' };
        api.put.mockResolvedValue({ data: { ...userData, id: 1 } });

        const result = await userService.updateUser(userData);

        expect(api.put).toHaveBeenCalledWith('/api/rest/users/update', userData);
        expect(result.firstName).toBe('Jean');
    });

    it('changePassword appelle POST /api/auth/update-password', async () => {
        const passwordData = { oldPassword: 'Old@123', newPassword: 'New@123', confirmPassword: 'New@123' };
        api.post.mockResolvedValue({ data: { message: 'Mot de passe mis à jour' } });

        const result = await userService.changePassword(passwordData);

        expect(api.post).toHaveBeenCalledWith('/api/auth/update-password', passwordData);
        expect(result.message).toBe('Mot de passe mis à jour');
    });

    it('deleteUser appelle DELETE /api/rest/users/delete/:id', async () => {
        api.delete.mockResolvedValue({ data: { message: 'Compte supprimé' } });

        const result = await userService.deleteUser(42);

        expect(api.delete).toHaveBeenCalledWith('/api/rest/users/delete/42');
        expect(result.message).toBe('Compte supprimé');
    });

    it('getCurrentUser propage les erreurs réseau', async () => {
        api.get.mockRejectedValue(new Error('Unauthorized'));

        await expect(userService.getCurrentUser()).rejects.toThrow('Unauthorized');
    });
});

