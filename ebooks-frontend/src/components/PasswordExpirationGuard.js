// components/PasswordExpirationGuard.js
import { useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';

/**
 * Composant garde pour vérifier l'expiration du mot de passe
 * Redirige vers /update-password si le mot de passe est expiré
 */
export const PasswordExpirationGuard = ({ children }) => {
  const navigate = useNavigate();
  const location = useLocation();

  useEffect(() => {
    const checkPasswordExpiration = async () => {
      // Ne pas vérifier sur les pages publiques
      const publicPaths = [
        '/login', 
        '/register', 
        '/forgot-password', 
        '/reset-password',
        '/verify-email'
      ];
      
      if (publicPaths.some(path => location.pathname.startsWith(path))) {
        return;
      }

      // Ne pas vérifier sur la page de mise à jour elle-même
      if (location.pathname === '/update-password') {
        return;
      }

      try {
        const response = await fetch('http://localhost:8080/api/auth/password-status', {
          credentials: 'include'
        });
        
        if (!response.ok) {
          // Si non authentifié, laisser le système d'auth gérer
          return;
        }

        const data = await response.json();

        if (data.expired) {
          // Bloquer l'accès et rediriger
          navigate('/update-password', {
            replace: true, // Empêcher le retour en arrière
            state: {
              forced: true,
              message: data.message
            }
          });
        }
      } catch (error) {
        console.error('Erreur lors de la vérification de l\'expiration:', error);
      }
    };

    checkPasswordExpiration();
  }, [location.pathname, navigate]);

  return children;
};

export default PasswordExpirationGuard;

