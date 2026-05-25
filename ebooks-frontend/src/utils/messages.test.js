import { ERROR_MESSAGES, SUCCESS_MESSAGES } from './messages';

describe('messages utils', () => {
    describe('ERROR_MESSAGES', () => {
        it('contient un message pour les erreurs réseau', () => {
            expect(ERROR_MESSAGES.NETWORK_ERROR).toBeDefined();
            expect(typeof ERROR_MESSAGES.NETWORK_ERROR).toBe('string');
        });

        it('contient un message pour les identifiants invalides', () => {
            expect(ERROR_MESSAGES.INVALID_CREDENTIALS).toBeDefined();
        });

        it('contient tous les messages d\'erreur requis', () => {
            expect(ERROR_MESSAGES.ACCOUNT_DISABLED).toBeDefined();
            expect(ERROR_MESSAGES.EMAIL_EXISTS).toBeDefined();
            expect(ERROR_MESSAGES.INVALID_EMAIL).toBeDefined();
            expect(ERROR_MESSAGES.INVALID_PASSWORD).toBeDefined();
            expect(ERROR_MESSAGES.PASSWORDS_NOT_MATCH).toBeDefined();
            expect(ERROR_MESSAGES.INVALID_TOKEN).toBeDefined();
            expect(ERROR_MESSAGES.INVALID_CODE).toBeDefined();
            expect(ERROR_MESSAGES.RECAPTCHA_FAILED).toBeDefined();
            expect(ERROR_MESSAGES.SERVER_ERROR).toBeDefined();
        });
    });

    describe('SUCCESS_MESSAGES', () => {
        it('contient un message de succès pour la connexion', () => {
            expect(SUCCESS_MESSAGES.LOGIN_SUCCESS).toBeDefined();
            expect(typeof SUCCESS_MESSAGES.LOGIN_SUCCESS).toBe('string');
        });

        it('contient tous les messages de succès requis', () => {
            expect(SUCCESS_MESSAGES.LOGOUT_SUCCESS).toBeDefined();
            expect(SUCCESS_MESSAGES.REGISTER_SUCCESS).toBeDefined();
            expect(SUCCESS_MESSAGES.PASSWORD_UPDATED).toBeDefined();
            expect(SUCCESS_MESSAGES.PASSWORD_RESET).toBeDefined();
            expect(SUCCESS_MESSAGES.EMAIL_SENT).toBeDefined();
            expect(SUCCESS_MESSAGES.CODE_SENT).toBeDefined();
            expect(SUCCESS_MESSAGES.ACCOUNT_VERIFIED).toBeDefined();
        });
    });
});

