import api from './api';

export const reservationService = {
    validateCart: async () => {
        const response = await api.post('/api/rest/reservations/validate-cart');
        return response.data;
    },

    getUserReservations: async () => {
        const response = await api.get('/api/rest/reservations');
        return response.data;
    },

    cancelReservation: async (reservationId) => {
        const response = await api.post(`/api/rest/reservations/${reservationId}/cancel`);
        return response.data;
    }
};