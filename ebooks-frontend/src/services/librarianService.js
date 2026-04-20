import axios from 'axios';

const API_URL = 'http://localhost:8080/api/librarian';

// Obtenir toutes les réservations en attente
export const getPendingReservations = async () => {
  const response = await axios.get(`${API_URL}/reservations/pending`, {
    withCredentials: true
  });
  return response.data;
};

// Obtenir toutes les réservations avec filtre
export const getAllReservations = async (status = null) => {
  const url = status ? `${API_URL}/reservations?status=${status}` : `${API_URL}/reservations`;
  const response = await axios.get(url, {
    withCredentials: true
  });
  return response.data;
};

// Valider une réservation
export const validateReservation = async (reservationId) => {
  const response = await axios.post(
    `${API_URL}/reservations/${reservationId}/validate`,
    {},
    { withCredentials: true }
  );
  return response.data;
};

// Rejeter une réservation
export const rejectReservation = async (reservationId, reason = '') => {
  const response = await axios.post(
    `${API_URL}/reservations/${reservationId}/reject`,
    { reason },
    { withCredentials: true }
  );
  return response.data;
};

// Ajouter un livre
export const addBook = async (bookData) => {
  const response = await axios.post(`${API_URL}/books`, bookData, {
    withCredentials: true
  });
  return response.data;
};

// Modifier un livre
export const updateBook = async (bookId, bookData) => {
  const response = await axios.put(`${API_URL}/books/${bookId}`, bookData, {
    withCredentials: true
  });
  return response.data;
};

// Supprimer un livre
export const deleteBook = async (bookId) => {
  const response = await axios.delete(`${API_URL}/books/${bookId}`, {
    withCredentials: true
  });
  return response.data;
};

// Obtenir les alertes de disponibilité
export const getAvailabilityAlerts = async () => {
  const response = await axios.get(`${API_URL}/alerts/availability`, {
    withCredentials: true
  });
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

