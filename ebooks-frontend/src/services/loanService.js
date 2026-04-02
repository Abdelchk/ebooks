import api from './api';

export const loanService = {
    getUserLoans: async () => {
        const response = await api.get('/api/rest/loans');
        return response.data;
    },

    getActiveLoans: async () => {
        const response = await api.get('/api/rest/loans/active');
        return response.data;
    },

    extendLoan: async (loanId) => {
        const response = await api.post(`/api/rest/loans/${loanId}/extend`);
        return response.data;
    },

    returnLoan: async (loanId) => {
        const response = await api.post(`/api/rest/loans/${loanId}/return`);
        return response.data;
    },

    createFromReservation: async (reservationId) => {
        const response = await api.post(`/api/rest/loans/from-reservation/${reservationId}`);
        return response.data;
    }
};