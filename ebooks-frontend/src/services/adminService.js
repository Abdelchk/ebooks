import axios from 'axios';

const API_URL = 'http://localhost:8080/api/admin';

// Obtenir tous les utilisateurs (sauf admins)
export const getAllUsers = async () => {
  const response = await axios.get(`${API_URL}/users`, {
    withCredentials: true
  });
  return response.data;
};

// Obtenir un utilisateur par ID
export const getUserById = async (userId) => {
  const response = await axios.get(`${API_URL}/users/${userId}`, {
    withCredentials: true
  });
  return response.data;
};

// Créer un utilisateur
export const createUser = async (userData) => {
  const response = await axios.post(`${API_URL}/users`, userData, {
    withCredentials: true
  });
  return response.data;
};

// Modifier un utilisateur
export const updateUser = async (userId, userData) => {
  const response = await axios.put(`${API_URL}/users/${userId}`, userData, {
    withCredentials: true
  });
  return response.data;
};

// Supprimer un utilisateur
export const deleteUser = async (userId) => {
  const response = await axios.delete(`${API_URL}/users/${userId}`, {
    withCredentials: true
  });
  return response.data;
};

// Activer/Désactiver un utilisateur
export const toggleUserStatus = async (userId) => {
  const response = await axios.patch(
    `${API_URL}/users/${userId}/toggle-status`,
    {},
    { withCredentials: true }
  );
  return response.data;
};

// Changer le rôle d'un utilisateur
export const changeUserRole = async (userId, role) => {
  const response = await axios.patch(
    `${API_URL}/users/${userId}/role`,
    { role },
    { withCredentials: true }
  );
  return response.data;
};

// Obtenir des statistiques
export const getStats = async () => {
  const response = await axios.get(`${API_URL}/stats`, {
    withCredentials: true
  });
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

