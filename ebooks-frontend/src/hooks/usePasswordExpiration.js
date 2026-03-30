// hooks/usePasswordExpiration.js
import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';

/**
 * Hook personnalisé pour gérer l'expiration du mot de passe
 * Vérifie l'état du mot de passe au chargement et redirige si nécessaire
 */
export const usePasswordExpiration = () => {
  const [passwordStatus, setPasswordStatus] = useState(null);
  const [showWarning, setShowWarning] = useState(false);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    const checkPasswordStatus = async () => {
      try {
        const response = await fetch('http://localhost:8080/api/auth/check', {
          credentials: 'include'
        });
        const data = await response.json();

        if (data.authenticated && data.passwordStatus) {
          setPasswordStatus(data.passwordStatus);

          // Rediriger si le mot de passe est expiré
          if (data.passwordStatus.expired) {
            navigate('/update-password', { 
              state: { 
                forced: true,
                message: data.passwordStatus.message 
              }
            });
          } else if (data.passwordStatus.warning) {
            setShowWarning(true);
          }
        }
      } catch (error) {
        console.error('Erreur lors de la vérification du mot de passe:', error);
      } finally {
        setLoading(false);
      }
    };

    checkPasswordStatus();
    
    // Vérifier toutes les 5 minutes
    const interval = setInterval(checkPasswordStatus, 5 * 60 * 1000);
    
    return () => clearInterval(interval);
  }, [navigate]);

  return { passwordStatus, showWarning, setShowWarning, loading };
};

export default usePasswordExpiration;

