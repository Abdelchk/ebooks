import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import PrivateRoute from './components/PrivateRoute';
import PasswordExpirationGuard from './components/PasswordExpirationGuard';
import PasswordWarningBanner from './components/PasswordWarningBanner';

// Pages
import Login from './pages/Login';
import Register from './pages/Register';
import Accueil from './pages/Accueil';
import BookDetail from './pages/BookDetail';
import ForgotPassword from './pages/ForgotPassword';
import ResetPassword from './pages/ResetPassword';
import UpdatePassword from './pages/UpdatePassword';
import VerifyEmail from './pages/VerifyEmail';
import VerifyCode from './pages/VerifyCode';
import LastStep from './pages/LastStep';
import About from './pages/About';
import Cart from "./pages/Cart";
import Reservations from './pages/Reservations';
import Loans from './pages/Loans';

// Bootstrap CSS
import 'bootstrap/dist/css/bootstrap.min.css';
import 'bootstrap-icons/font/bootstrap-icons.css';
import './App.css';

function App() {
  return (
    <AuthProvider>
      <Router>
        <PasswordExpirationGuard>
          <PasswordWarningBanner />
          <Routes>
            <Route path="/" element={<Navigate to="/accueil" replace />} />
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
            <Route path="/forgot-password" element={<ForgotPassword />} />
            <Route path="/reset-password" element={<ResetPassword />} />
            <Route path="/verify-email" element={<VerifyEmail />} />
            <Route path="/last-step" element={<LastStep />} />
            <Route path="/about" element={<About />} />

            {/* Routes publiques */}
            <Route path="/accueil" element={<Accueil />} />
            <Route path="/book/:id" element={<BookDetail />} />

            {/* Routes protégées */}
            <Route
              path="/cart"
              element={
                <PrivateRoute>
                  <Cart />
                </PrivateRoute>
              }
            />
            <Route
              path="/reservations"
              element={
                <PrivateRoute>
                  <Reservations />
                </PrivateRoute>
              }
            />
            <Route
              path="/loans"
              element={
                <PrivateRoute>
                  <Loans />
                </PrivateRoute>
              }
            />
            <Route
              path="/update-password"
              element={
                <PrivateRoute>
                  <UpdatePassword />
                </PrivateRoute>
              }
            />
            <Route
              path="/verify-code"
              element={
                <PrivateRoute>
                  <VerifyCode />
                </PrivateRoute>
              }
            />

            {/* Redirection par défaut */}
            <Route path="*" element={<Navigate to="/accueil" />} />
          </Routes>
        </PasswordExpirationGuard>
      </Router>
    </AuthProvider>
  );
}

export default App;
