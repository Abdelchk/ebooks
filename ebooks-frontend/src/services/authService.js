import api from './api';

export const authService = {
  login: async (email, password) => {
    const response = await api.post('/api/auth/login', { email, password });
    return response.data;
  },

  logout: async () => {
    const response = await api.post('/api/auth/logout');
    return response.data;
  },

  register: async (userData) => {
    const response = await api.post('/api/auth/register', userData);
    return response.data;
  },

  checkAuth: async () => {
    const response = await api.get('/api/auth/check');
    return response.data;
  },

  forgotPassword: async (email) => {
    const response = await api.post('/api/auth/forgot-password', { email });
    return response.data;
  },

  resetPassword: async (token, newPassword, confirmPassword) => {
    const response = await api.post('/api/auth/reset-password', {
      token,
      newPassword,
      confirmPassword,
    });
    return response.data;
  },

  validateResetToken: async (token) => {
    const response = await api.get(`/api/auth/validate-reset-token?token=${token}`);
    return response.data;
  },

  verifyEmail: async (token) => {
    const response = await api.get(`/api/auth/verify-email?token=${token}`);
    return response.data;
  },

  verifyCode: async (code) => {
    const response = await api.post('/api/auth/verify-code', { code });
    return response.data;
  },

  resendCode: async () => {
    const response = await api.post('/api/auth/resend-code');
    return response.data;
  },

  getSecurityQuestions: async () => {
    const response = await api.get('/api/auth/security-questions');
    return response.data;
  },

  getRecaptchaKey: async () => {
    const response = await api.get('/api/auth/recaptcha-key');
    return response.data;
  },

  getSecurityQuestion: async () => {
    const response = await api.get('/api/auth/security-question');
    return response.data;
  },

  updatePassword: async (oldPassword, newPassword, confirmPassword, securityAnswer) => {
    const response = await api.post('/api/auth/update-password', {
      oldPassword,
      newPassword,
      confirmPassword,
      securityAnswer,
    });
    return response.data;
  },
};

