// components/PasswordWarningBanner.js
import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { authService } from '../services/authService';
import './PasswordWarningBanner.css';

/**
 * Bannière d'avertissement pour l'expiration du mot de passe
 * Affiche un message quand le mot de passe expire bientôt (7 derniers jours)
 */
export const PasswordWarningBanner = () => {
  const [warning, setWarning] = useState(null);
  const [visible, setVisible] = useState(false);

  useEffect(() => {
    const checkStatus = async () => {
      try {
        const data = await authService.passwordStatus();

        // Afficher seulement si avertissement (pas si expiré, car redirection)
        if (data.warning && !data.expired) {
          setWarning(data);
          
          // Vérifier si l'utilisateur a déjà fermé la bannière aujourd'hui
          const dismissedDate = localStorage.getItem('passwordWarningDismissed');
          const today = new Date().toDateString();
          
          if (dismissedDate === today) {
            // Bannière déjà fermée aujourd'hui, ne pas afficher
            return;
          }
          setVisible(true);
        }
      } catch (error) {
        // Backend inaccessible ou non authentifié - ne pas afficher la bannière
        console.error('Erreur lors de la vérification du statut mot de passe:', error.message);
      }
    };

    checkStatus();
    
    // Vérifier toutes les 10 minutes
    const interval = setInterval(checkStatus, 10 * 60 * 1000);
    
    return () => clearInterval(interval);
  }, []);

  const handleDismiss = () => {
    setVisible(false);
    // Mémoriser que l'utilisateur a fermé la bannière aujourd'hui
    localStorage.setItem('passwordWarningDismissed', new Date().toDateString());
  };

  if (!visible || !warning) {
    return null;
  }

  return (
    <div className="password-warning-banner">
      <div className="warning-content">
        <span className="warning-icon">⚠️</span>
        <span className="warning-message">
          {warning.message}
        </span>
        <Link to="/update-password" className="warning-link">
          Changer maintenant
        </Link>
        <button 
          className="warning-close" 
          onClick={handleDismiss}
          aria-label="Fermer"
        >
          ×
        </button>
      </div>
    </div>
  );
};

export default PasswordWarningBanner;

