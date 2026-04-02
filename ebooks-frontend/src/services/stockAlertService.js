import api from './api';

export const stockAlertService = {
  createAlert: async (bookId) => {
    const response = await api.post(`/api/rest/stock-alerts/create/${bookId}`);
    return response.data;
  },

  getUserAlerts: async () => {
    const response = await api.get('/api/rest/stock-alerts');
    return response.data;
  },

  cancelAlert: async (alertId) => {
    const response = await api.post(`/api/rest/stock-alerts/${alertId}/cancel`);
    return response.data;
  },
};

