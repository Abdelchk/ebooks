import api from './api';

export const chatService = {
    sendMessage: async (message) => {
        const response = await api.post('/api/rest/chat', { message });
        return response.data; // { reply: string, books: BookDto[] }
    }
};

