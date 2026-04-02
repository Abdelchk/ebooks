import api from './api';

export const cartService = {
    getCart: async () => {
        const response = await api.get('/api/rest/cart');
        return response.data;
    },

    addToCart: async (bookId, loanDuration = 14) => {
        const response = await api.post('/api/rest/cart/add', {
            bookId,
            loanDuration
        });
        return response.data;
    },

    removeFromCart: async (cartItemId) => {
        const response = await api.delete(`/api/rest/cart/${cartItemId}`);
        return response.data;
    },

    clearCart: async () => {
        const response = await api.delete('/api/rest/cart/clear');
        return response.data;
    },

    updateDuration: async (cartItemId, duration) => {
        const response = await api.put(`/api/rest/cart/${cartItemId}/duration`, {
            duration
        });
        return response.data;
    },

    getCartCount: async () => {
        const response = await api.get('/api/rest/cart/count');
        return response.data.count;
    }
};