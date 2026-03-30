// EXEMPLE - Comment intégrer l'expiration du mot de passe dans App.js

import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { PasswordExpirationGuard } from './components/PasswordExpirationGuard';
import { PasswordWarningBanner } from './components/PasswordWarningBanner';
import { useAuth } from './context/AuthContext';

// Vos pages
import Login from './pages/Login';
import Register from './pages/Register';
import ForgotPassword from './pages/ForgotPassword';
import ResetPassword from './pages/ResetPassword';
import UpdatePassword from './pages/UpdatePassword';
import Accueil from './pages/Accueil';
import VerifyCode from './pages/VerifyCode';
import VerifyEmail from './pages/VerifyEmail';

function App() {
  return (
    <Router>
      {/* 
        PasswordExpirationGuard : 
        Vérifie à chaque navigation si le mot de passe est expiré
        et redirige vers /update-password si nécessaire 
      */}
      <PasswordExpirationGuard>
        {/* 
          PasswordWarningBanner : 
          Affiche une bannière d'avertissement quand le mot de passe expire bientôt 
        */}
        <PasswordWarningBanner />
        
        <Routes>
          {/* Routes publiques */}
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route path="/forgot-password" element={<ForgotPassword />} />
          <Route path="/reset-password" element={<ResetPassword />} />
          <Route path="/verify-email" element={<VerifyEmail />} />
          
          {/* Routes protégées */}
          <Route path="/verify-code" element={<PrivateRoute><VerifyCode /></PrivateRoute>} />
          <Route path="/accueil" element={<PrivateRoute><Accueil /></PrivateRoute>} />
          
          {/* 
            Route de mise à jour du mot de passe 
            Accessible à tout moment, mais obligatoire si expiré 
          */}
          <Route path="/update-password" element={<PrivateRoute><UpdatePassword /></PrivateRoute>} />
          
          <Route path="/" element={<Navigate to="/login" />} />
        </Routes>
      </PasswordExpirationGuard>
    </Router>
  );
}

// Composant pour protéger les routes authentifiées
const PrivateRoute = ({ children }) => {
  const { user, loading } = useAuth();
  
  if (loading) {
    return <div>Chargement...</div>;
  }
  
  return user ? children : <Navigate to="/login" />;
};

export default App;

/*
 * UTILISATION :
 * 
 * 1. Copiez ce fichier ou intégrez ces composants dans votre App.js existant
 * 
 * 2. Assurez-vous d'avoir créé les fichiers suivants :
 *    - src/hooks/usePasswordExpiration.js
 *    - src/components/PasswordExpirationGuard.js
 *    - src/components/PasswordWarningBanner.js
 *    - src/components/PasswordWarningBanner.css
 * 
 * 3. Le flux fonctionnera automatiquement :
 *    - À chaque navigation, PasswordExpirationGuard vérifie l'état
 *    - Si expiré (≥84 jours) → redirection forcée vers /update-password
 *    - Si avertissement (77-83 jours) → affichage de la bannière jaune
 *    - Si valide (<77 jours) → aucune action
 * 
 * 4. Test :
 *    - Connectez-vous avec un utilisateur
 *    - Modifiez en base : UPDATE user SET last_password_update_date = DATE_SUB(NOW(), INTERVAL 90 DAY)
 *    - Rechargez l'application → redirection automatique
 */

