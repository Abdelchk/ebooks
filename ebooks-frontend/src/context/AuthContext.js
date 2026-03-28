import React, { createContext, useState, useEffect, useContext } from 'react';
import { authService } from '../services/authService';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    checkAuthStatus();
  }, []);

  const checkAuthStatus = async () => {
    try {
      const response = await authService.checkAuth();
      if (response.authenticated) {
        setUser(response);
      }
    } catch (error) {
      console.error('Erreur lors de la vérification de l\'authentification:', error);
    } finally {
      setLoading(false);
    }
  };

  const login = async (email, password) => {
    try {
      console.log('🔐 AuthContext: Appel de authService.login');
      const response = await authService.login(email, password);
      console.log('📥 AuthContext: Réponse reçue:', response);

      if (response.success) {
        console.log('✅ AuthContext: Login réussi, vérification du statut...');
        await checkAuthStatus();
      }
      return response;
    } catch (error) {
      console.error('❌ AuthContext: Erreur de login:', error);
      throw error;
    }
  };

  const logout = async () => {
    await authService.logout();
    setUser(null);
  };

  const register = async (userData) => {
    return await authService.register(userData);
  };

  return (
    <AuthContext.Provider value={{ user, login, logout, register, loading, checkAuthStatus }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth doit être utilisé dans un AuthProvider');
  }
  return context;
};

