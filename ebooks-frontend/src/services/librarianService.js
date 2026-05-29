import api from './api';

const API_URL = '/api/librarian';

// Obtenir toutes les réservations en attente
export const getPendingReservations = async () => {
  const response = await api.get(`${API_URL}/reservations/pending`);
  return response.data;
};

// Obtenir toutes les réservations avec filtre
export const getAllReservations = async (status = null) => {
  const url = status ? `${API_URL}/reservations?status=${status}` : `${API_URL}/reservations`;
  const response = await api.get(url);
  return response.data;
};

// Valider une réservation
export const validateReservation = async (reservationId) => {
  const response = await api.post(`${API_URL}/reservations/${reservationId}/validate`, {});
  return response.data;
};

// Rejeter une réservation
export const rejectReservation = async (reservationId, reason = '') => {
  const response = await api.post(`${API_URL}/reservations/${reservationId}/reject`, { reason });
  return response.data;
};

// Ajouter un livre
export const addBook = async (bookData) => {
  const response = await api.post(`${API_URL}/books`, bookData);
  return response.data;
};

// Modifier un livre
export const updateBook = async (bookId, bookData) => {
  const response = await api.put(`${API_URL}/books/${bookId}`, bookData);
  return response.data;
};

// Supprimer un livre
export const deleteBook = async (bookId) => {
  const response = await api.delete(`${API_URL}/books/${bookId}`);
  return response.data;
};

// Obtenir les alertes de disponibilité
export const getAvailabilityAlerts = async () => {
  const response = await api.get(`${API_URL}/alerts/availability`);
  return response.data;
};

const librarianService = {
  getPendingReservations,
  getAllReservations,
  validateReservation,
  rejectReservation,
  addBook,
  updateBook,
  deleteBook,
  getAvailabilityAlerts
};

export default librarianService;

