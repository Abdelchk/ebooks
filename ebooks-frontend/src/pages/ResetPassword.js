import React, { useState, useEffect } from 'react';
import { useNavigate, useSearchParams, Link } from 'react-router-dom';
import { Container, Form, Button, Alert } from 'react-bootstrap';
import { authService } from '../services/authService';
import Navigation from '../components/Navbar';
import Loader from '../components/Loader';
import '../styles/password-toggle.css';

const ResetPassword = () => {
  const [searchParams] = useSearchParams();
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [showNewPassword, setShowNewPassword] = useState('');
  const [showConfirmPassword, setShowConfirmPassword] = useState('');
  const [error, setError] = useState('');
  const [validated, setValidated] = useState(false);
  const [tokenValid, setTokenValid] = useState(false);
  const [checking, setChecking] = useState(true);
  const navigate = useNavigate();
  const token = searchParams.get('token');

  useEffect(() => {
    const validateToken = async () => {
      try {
        const response = await authService.validateResetToken(token);
        if (!response.valid) {
          setError('Le lien de réinitialisation est invalide ou a expiré.');
          setTimeout(() => navigate('/login'), 3000);
        } else {
          setTokenValid(true);
        }
      } catch (err) {
        setError('Erreur lors de la validation du lien');
      } finally {
        setChecking(false);
      }
    };

    if (token) {
      validateToken();
    } else {
      setError('Token manquant');
      setChecking(false);
    }
  }, [token, navigate]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    const form = e.currentTarget;

    if (form.checkValidity() === false) {
      e.stopPropagation();
      setValidated(true);
      return;
    }

    if (newPassword !== confirmPassword) {
      setError('Les mots de passe ne correspondent pas');
      setValidated(true);
      return;
    }

    setValidated(true);

    try {
      const response = await authService.resetPassword(token, newPassword, confirmPassword);
      if (response.success) {
        alert('Votre mot de passe a été réinitialisé avec succès !');
        navigate('/login');
      } else {
        setError(response.message);
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Erreur lors de la réinitialisation');
    }
  };

  if (checking) {
    return (
      <>
        <Navigation />
        <Container>
          <Loader message="Validation du lien..." />
        </Container>
      </>
    );
  }

  return (
    <>
      <Navigation />
      <Container>
        <h2 className="mt-5">Réinitialisation du mot de passe</h2>

        {error && <Alert variant="danger">{error}</Alert>}

        {tokenValid && (
          <>
            <Alert variant="info">
              <strong>Politique de mot de passe :</strong>
              <ul>
                <li>Minimum 12 caractères</li>
                <li>Au moins une lettre</li>
                <li>Au moins un chiffre</li>
                <li>Au moins un caractère spécial (@$!%*?&#)</li>
                <li>Ne doit pas être l'un des 5 derniers mots de passe utilisés</li>
              </ul>
            </Alert>

            <Form noValidate validated={validated} onSubmit={handleSubmit}>
              <Form.Group className="mb-3">
                <Form.Label>Nouveau mot de passe</Form.Label>
                <div className="input-group">
                  <Form.Control
                      type={showNewPassword ? "text" : "password"}
                      value={newPassword}
                      onChange={(e) => setNewPassword(e.target.value)}
                      pattern="^(?=.*[A-Za-zÀ-ÖØ-öø-ÿ])(?=.*\d)(?=.*[@$!%*?&#])[A-Za-zÀ-ÖØ-öø-ÿ\d@$!%*?&#]{12,}$"
                      required
                  />
                  <Button
                      variant="outline-secondary"
                      onClick={() => setShowNewPassword(!showNewPassword)}
                      type="button"
                      title={showNewPassword ? "Masquer" : "Afficher"}
                  >
                    <i className={`bi ${showNewPassword ? 'bi-eye-slash-fill' : 'bi-eye-fill'}`}></i>
                  </Button>
                  <Form.Control.Feedback type="invalid">
                    Le mot de passe doit contenir au moins 12 caractères, une lettre, un chiffre et un caractère spécial.
                  </Form.Control.Feedback>
                </div>
              </Form.Group>

              <Form.Group className="mb-3">
                <Form.Label>Confirmer le nouveau mot de passe</Form.Label>
                <div className="input-group">
                  <Form.Control
                    type={showConfirmPassword ? "text" : "password"}
                    value={confirmPassword}
                    onChange={(e) => setConfirmPassword(e.target.value)}
                    required
                  />
                  <Button 
                    variant="outline-secondary" 
                    onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                    type="button"
                    title={showConfirmPassword ? "Masquer" : "Afficher"}
                  >
                    <i className={`bi ${showConfirmPassword ? 'bi-eye-slash-fill' : 'bi-eye-fill'}`}></i>
                  </Button>
                  <Form.Control.Feedback type="invalid">
                    Veuillez confirmer votre nouveau mot de passe.
                  </Form.Control.Feedback>
                </div>
              </Form.Group>

              <Button variant="primary" type="submit">
                Réinitialiser le mot de passe
              </Button>
              {' '}
              <Link to="/login">
                <Button variant="secondary">Retour à la connexion</Button>
              </Link>
            </Form>
          </>
        )}
      </Container>
    </>
  );
};

export default ResetPassword;



