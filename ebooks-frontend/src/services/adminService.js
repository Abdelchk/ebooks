import api from './api';

const API_URL = '/api/admin';

// Obtenir tous les utilisateurs (sauf admins)
export const getAllUsers = async () => {
  const response = await api.get(`${API_URL}/users`);
  return response.data;
};

// Obtenir un utilisateur par ID
export const getUserById = async (userId) => {
  const response = await api.get(`${API_URL}/users/${userId}`);
  return response.data;
};

// Créer un utilisateur
export const createUser = async (userData) => {
  const response = await api.post(`${API_URL}/users`, userData);
  return response.data;
};

// Modifier un utilisateur
export const updateUser = async (userId, userData) => {
  const response = await api.put(`${API_URL}/users/${userId}`, userData);
  return response.data;
};

// Supprimer un utilisateur
export const deleteUser = async (userId) => {
  const response = await api.delete(`${API_URL}/users/${userId}`);
  return response.data;
};

// Activer/Désactiver un utilisateur
export const toggleUserStatus = async (userId) => {
  const response = await api.patch(`${API_URL}/users/${userId}/toggle-status`, {});
  return response.data;
};

// Changer le rôle d'un utilisateur
export const changeUserRole = async (userId, role) => {
  const response = await api.patch(`${API_URL}/users/${userId}/role`, { role });
  return response.data;
};

// Obtenir des statistiques
export const getStats = async () => {
  const response = await api.get(`${API_URL}/stats`);
  return response.data;
};

const adminService = {
  getAllUsers,
  getUserById,
  createUser,
  updateUser,
  deleteUser,
  toggleUserStatus,
  changeUserRole,
  getStats
};

export default adminService;

