import React from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

/**
 * AdminRoute : Route protégée réservée aux administrateurs
 * @param {React.Component} children - Composant à afficher si autorisé
 * @returns {React.Component} Affiche le composant ou redirige vers accueil
 */
const AdminRoute = ({ children }) => {
  const { user, loading } = useAuth();

  if (loading) {
    return (
      <div className="d-flex justify-content-center align-items-center" style={{ height: '100vh' }}>
        <div className="spinner-border text-primary" role="status">
          <span className="visually-hidden">Chargement...</span>
        </div>
      </div>
    );
  }

  // Vérifier si l'utilisateur est admin
  if (!user || user.role !== 'admin') {
    return <Navigate to="/accueil" replace />;
  }

  return children;
};

export default AdminRoute;

