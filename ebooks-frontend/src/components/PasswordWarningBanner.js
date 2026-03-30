// components/PasswordWarningBanner.js
import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
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
        console.log('🔔 PasswordWarningBanner - Vérification du statut...');
        const response = await fetch('http://localhost:8080/api/auth/password-status', {
          credentials: 'include'
        });
        
        if (!response.ok) {
          console.log('❌ Banner - Réponse pas OK:', response.status);
          return;
        }
        
        const data = await response.json();
        console.log('📊 Banner - Données reçues:', data);

        // Afficher seulement si avertissement (pas si expiré, car redirection)
        if (data.warning && !data.expired) {
          console.log('⚠️ Banner - Warning détecté !');
          setWarning(data);
          
          // Vérifier si l'utilisateur a déjà fermé la bannière aujourd'hui
          const dismissedDate = localStorage.getItem('passwordWarningDismissed');
          const today = new Date().toDateString();
          
          console.log('📅 Banner - Date dismissée:', dismissedDate);
          console.log('📅 Banner - Aujourd\'hui:', today);
          
          if (dismissedDate !== today) {
            console.log('✅ Banner - Affichage de la bannière');
            setVisible(true);
          } else {
            console.log('❌ Banner - Bannière déjà fermée aujourd\'hui');
          }
        } else {
          console.log('ℹ️ Banner - Pas de warning (expired:', data.expired, ', warning:', data.warning, ')');
        }
      } catch (error) {
        console.error('❌ Banner - Erreur:', error);
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

