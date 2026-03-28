import api from './api';

export const userService = {
  getCurrentUser: async () => {
    const response = await api.get('/api/rest/users/me');
    return response.data;
  },

  updateUser: async (userData) => {
    const response = await api.put('/api/rest/users/update', userData);
    return response.data;
  },

  changePassword: async (passwordData) => {
    const response = await api.post('/api/auth/update-password', passwordData);
    return response.data;
  },

  deleteUser: async (id) => {
    const response = await api.delete(`/api/rest/users/delete/${id}`);
    return response.data;
  },
};

