// Constantes de l'application

export const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080';

export const ROUTES = {
  HOME: '/',
  LOGIN: '/login',
  REGISTER: '/register',
  ACCUEIL: '/accueil',
  FORGOT_PASSWORD: '/forgot-password',
  RESET_PASSWORD: '/reset-password',
  UPDATE_PASSWORD: '/update-password',
  VERIFY_EMAIL: '/verify-email',
  VERIFY_CODE: '/verify-code',
  LAST_STEP: '/last-step',
  ABOUT: '/about',
};

export const PASSWORD_POLICY = {
  MIN_LENGTH: 12,
  REQUIRES_LETTER: true,
  REQUIRES_DIGIT: true,
  REQUIRES_SPECIAL: true,
  SPECIAL_CHARS: '@$!%*?&#',
};

export const VERIFICATION_CODE = {
  LENGTH: 6,
  EXPIRY_MINUTES: 2,
  MAX_ACTIVE_CODES: 3,
};

export const RECAPTCHA = {
  ACTION_REGISTER: 'REGISTER',
  ACTION_LOGIN: 'LOGIN',
};

