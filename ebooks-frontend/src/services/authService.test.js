import { authService } from './authService';
import api, { invalidateCsrfToken } from './api';

jest.mock('./api');

describe('authService', () => {
    afterEach(() => {
        jest.clearAllMocks();
    });

    it('login appelle POST /api/auth/login avec email et password', async () => {
        api.post.mockResolvedValue({ data: { token: 'abc123', user: { email: 'test@test.com' } } });

        const result = await authService.login('test@test.com', 'Password123@');

        expect(api.post).toHaveBeenCalledWith('/api/auth/login', {
            email: 'test@test.com',
            password: 'Password123@',
        });
        expect(result.token).toBe('abc123');
    });

    it('login invalide le cache CSRF après une connexion réussie', async () => {
        api.post.mockResolvedValue({ data: { token: 'abc123' } });

        await authService.login('test@test.com', 'Password123@');

        // Le token CSRF doit être invalidé car Spring Security régénère la session après login
        expect(invalidateCsrfToken).toHaveBeenCalledTimes(1);
    });

    it('login propage les erreurs réseau', async () => {
        api.post.mockRejectedValue(new Error('Network Error'));

        await expect(authService.login('test@test.com', 'pass')).rejects.toThrow('Network Error');
    });

    it('logout appelle POST /api/auth/logout', async () => {
        api.post.mockResolvedValue({ data: { message: 'Déconnecté' } });

        const result = await authService.logout();

        expect(api.post).toHaveBeenCalledWith('/api/auth/logout');
        expect(result.message).toBe('Déconnecté');
    });

    it('logout invalide le cache CSRF après déconnexion', async () => {
        api.post.mockResolvedValue({ data: { message: 'Déconnecté' } });

        await authService.logout();

        // La session est détruite → le token CSRF associé doit être invalidé
        expect(invalidateCsrfToken).toHaveBeenCalledTimes(1);
    });

    it('register appelle POST /api/auth/register avec les données utilisateur', async () => {
        const userData = { email: 'new@test.com', password: 'Password123@', firstName: 'Jean' };
        api.post.mockResolvedValue({ data: { id: 1, email: 'new@test.com' } });

        const result = await authService.register(userData);

        expect(api.post).toHaveBeenCalledWith('/api/auth/register', userData);
        expect(result.email).toBe('new@test.com');
    });

    it('checkAuth appelle GET /api/auth/check', async () => {
        api.get.mockResolvedValue({ data: { authenticated: true, user: { email: 'test@test.com' } } });

        const result = await authService.checkAuth();

        expect(api.get).toHaveBeenCalledWith('/api/auth/check');
        expect(result.authenticated).toBe(true);
    });

    it('forgotPassword appelle POST /api/auth/forgot-password avec email', async () => {
        api.post.mockResolvedValue({ data: { message: 'Email envoyé' } });

        const result = await authService.forgotPassword('test@test.com');

        expect(api.post).toHaveBeenCalledWith('/api/auth/forgot-password', { email: 'test@test.com' });
        expect(result.message).toBe('Email envoyé');
    });

    it('resetPassword appelle POST /api/auth/reset-password avec les bons paramètres', async () => {
        api.post.mockResolvedValue({ data: { message: 'Mot de passe réinitialisé' } });

        const result = await authService.resetPassword('token123', 'NewPass123@', 'NewPass123@');

        expect(api.post).toHaveBeenCalledWith('/api/auth/reset-password', {
            token: 'token123',
            newPassword: 'NewPass123@',
            confirmPassword: 'NewPass123@',
        });
        expect(result.message).toBe('Mot de passe réinitialisé');
    });

    it('validateResetToken appelle GET avec le token en query param', async () => {
        api.get.mockResolvedValue({ data: { valid: true } });

        const result = await authService.validateResetToken('mytoken');

        expect(api.get).toHaveBeenCalledWith('/api/auth/validate-reset-token?token=mytoken');
        expect(result.valid).toBe(true);
    });

    it('verifyEmail appelle GET avec le token en query param', async () => {
        api.get.mockResolvedValue({ data: { verified: true } });

        const result = await authService.verifyEmail('emailtoken');

        expect(api.get).toHaveBeenCalledWith('/api/auth/verify-email?token=emailtoken');
        expect(result.verified).toBe(true);
    });

    it('verifyCode appelle POST /api/auth/verify-code avec le code', async () => {
        api.post.mockResolvedValue({ data: { success: true } });

        const result = await authService.verifyCode('123456');

        expect(api.post).toHaveBeenCalledWith('/api/auth/verify-code', { code: '123456' });
        expect(result.success).toBe(true);
    });

    it('resendCode appelle POST /api/auth/resend-code', async () => {
        api.post.mockResolvedValue({ data: { message: 'Code renvoyé' } });

        const result = await authService.resendCode();

        expect(api.post).toHaveBeenCalledWith('/api/auth/resend-code');
        expect(result.message).toBe('Code renvoyé');
    });

    it('getSecurityQuestions appelle GET /api/auth/security-questions', async () => {
        api.get.mockResolvedValue({ data: ['Question 1', 'Question 2'] });

        const result = await authService.getSecurityQuestions();

        expect(api.get).toHaveBeenCalledWith('/api/auth/security-questions');
        expect(result).toHaveLength(2);
    });

    it('getRecaptchaKey appelle GET /api/auth/recaptcha-key', async () => {
        api.get.mockResolvedValue({ data: { key: 'recaptcha-site-key' } });

        const result = await authService.getRecaptchaKey();

        expect(api.get).toHaveBeenCalledWith('/api/auth/recaptcha-key');
        expect(result.key).toBe('recaptcha-site-key');
    });

    it('getSecurityQuestion appelle GET /api/auth/security-question', async () => {
        api.get.mockResolvedValue({ data: { question: 'Quel est le nom de votre animal ?' } });

        const result = await authService.getSecurityQuestion();

        expect(api.get).toHaveBeenCalledWith('/api/auth/security-question');
        expect(result.question).toBeDefined();
    });

    it('updatePassword appelle POST avec tous les paramètres', async () => {
        api.post.mockResolvedValue({ data: { message: 'Mot de passe mis à jour' } });

        const result = await authService.updatePassword('OldPass@123', 'NewPass@123', 'NewPass@123', 'réponse');

        expect(api.post).toHaveBeenCalledWith('/api/auth/update-password', {
            oldPassword: 'OldPass@123',
            newPassword: 'NewPass@123',
            confirmPassword: 'NewPass@123',
            securityAnswer: 'réponse',
        });
        expect(result.message).toBe('Mot de passe mis à jour');
    });
});

