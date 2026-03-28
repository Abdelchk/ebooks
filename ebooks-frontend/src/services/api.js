import axios from 'axios';

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080';

// Configuration d'axios pour inclure les credentials
axios.defaults.withCredentials = true;

const api = axios.create({
  baseURL: API_BASE_URL,
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Intercepteur pour gérer les erreurs globalement
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Non authentifié - rediriger vers login
      console.warn('Session expirée ou non authentifié');
      // window.location.href = '/login'; // Optionnel
    }
    return Promise.reject(error);
  }
);

export default api;


