import api from './api';

export const bookService = {
  getAllBooks: async () => {
    const response = await api.get('/api/rest/books/all');
    return response.data;
  },

  getBookById: async (id) => {
    const response = await api.get(`/api/rest/books/${id}`);
    return response.data;
  },

  createBook: async (bookData) => {
    const response = await api.post('/api/rest/books/create', bookData);
    return response.data;
  },

  updateBook: async (bookData) => {
    const response = await api.put('/api/rest/books/update', bookData);
    return response.data;
  },

  deleteBook: async (id) => {
    const response = await api.delete(`/api/rest/books/remove/${id}`);
    return response.data;
  },
};

