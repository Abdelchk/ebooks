import React, { useState, useEffect } from 'react';
import { useNavigate, useLocation, Link } from 'react-router-dom';
import { Container, Form, Button, Alert } from 'react-bootstrap';
import { authService } from '../services/authService';
import Navigation from '../components/Navbar';
import '../styles/password-toggle.css';

const UpdatePassword = () => {
  const [formData, setFormData] = useState({
    oldPassword: '',
    newPassword: '',
    confirmPassword: '',
    securityAnswer: '',
  });
  const [securityQuestion, setSecurityQuestion] = useState('');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [validated, setValidated] = useState(false);
  const [isForced, setIsForced] = useState(false);
  const [showOldPassword, setShowOldPassword] = useState(false);
  const [showNewPassword, setShowNewPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();

  useEffect(() => {
    loadSecurityQuestion();
    checkPasswordExpiration();
    
    // Vérifier si l'utilisateur est forcé de changer son mot de passe
    if (location.state?.forced) {
      setIsForced(true);
      if (location.state?.message) {
        // Ne pas mettre le message dans error, on l'affiche dans l'alerte spéciale
        console.log('Message d\'expiration:', location.state.message);
      }
    }
  }, [location]);

  const checkPasswordExpiration = async () => {
    try {
      console.log('🔍 Vérification de l\'état du mot de passe...');
      const response = await fetch('http://localhost:8080/api/auth/password-status', {
        credentials: 'include'
      });
      
      if (response.ok) {
        const data = await response.json();
        console.log('📊 Réponse de l\'API:', data);

        // Si le mot de passe est expiré, forcer l'affichage du message
        if (data.expired) {
          console.log('✅ Mot de passe EXPIRÉ détecté - Affichage du message forcé');
          setIsForced(true);
        } else {
          console.log('ℹ️ Mot de passe valide (expired: false)');
        }
      } else {
        console.error('❌ Erreur réponse API:', response.status);
      }
    } catch (error) {
      console.error('❌ Erreur lors de la vérification:', error);
    }
  };

  const loadSecurityQuestion = async () => {
    try {
      const data = await authService.getSecurityQuestion();
      setSecurityQuestion(data.question);
    } catch (error) {
      console.error('Erreur lors du chargement de la question:', error);
    }
  };

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const form = e.currentTarget;

    if (form.checkValidity() === false) {
      e.stopPropagation();
      setValidated(true);
      return;
    }

    if (formData.newPassword !== formData.confirmPassword) {
      setError('Les mots de passe ne correspondent pas');
      setValidated(true);
      return;
    }

    setValidated(true);

    try {
      const response = await authService.updatePassword(
        formData.oldPassword,
        formData.newPassword,
        formData.confirmPassword,
        formData.securityAnswer
      );

      if (response.success) {
        setSuccess('Mot de passe mis à jour avec succès !');
        setError('');
        setTimeout(() => navigate('/accueil'), 2000);
      } else {
        setError(response.message || 'Erreur lors de la mise à jour du mot de passe');
        setSuccess('');
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Erreur lors de la mise à jour du mot de passe');
      setSuccess('');
    }
  };

  return (
    <>
      <Navigation />
      <Container>
        <h2 className="mt-5">Mise à jour du mot de passe</h2>

        {isForced && (
          <Alert variant="danger">
            <Alert.Heading>⚠️ Action requise</Alert.Heading>
            <p>
              Votre mot de passe a expiré. Vous devez le changer pour continuer à utiliser l'application.
            </p>
            <hr />
            <p className="mb-0">
              <small>Votre mot de passe doit être changé tous les 12 semaines pour des raisons de sécurité.</small>
            </p>
          </Alert>
        )}

        {error && !isForced && <Alert variant="danger">{error}</Alert>}
        {success && <Alert variant="success">{success}</Alert>}

        <Alert variant="info">
          <strong>Politique de mot de passe :</strong>
          <ul>
            <li>Minimum 12 caractères</li>
            <li>Au moins une lettre</li>
            <li>Au moins un chiffre</li>
            <li>Au moins un caractère spécial (@$!%*?&#)</li>
            <li>Ne doit pas être l'un des 5 derniers mots de passe utilisés</li>
            <li>Peut être changé tous les 3 mois (12 semaines)</li>
          </ul>
        </Alert>

        <Form noValidate validated={validated} onSubmit={handleSubmit}>
          <Form.Group className="mb-3">
            <Form.Label>Ancien mot de passe</Form.Label>
            <div className="input-group">
              <Form.Control
                  type={showOldPassword ? "text" : "password"}
                name="oldPassword"
                value={formData.oldPassword}
                onChange={handleChange}
                required
              />
              <Button
                  variant="outline-secondary"
                  onClick={() => setShowOldPassword(!showOldPassword)}
                  type="button"
              >
                <i className={`bi ${showOldPassword ? 'bi-eye-slash' : 'bi-eye'}`}></i>
              </Button>
              <Form.Control.Feedback type="invalid">
                Veuillez entrer votre ancien mot de passe.
              </Form.Control.Feedback>
            </div>
          </Form.Group>

          <Form.Group className="mb-3">
            <Form.Label>Nouveau mot de passe</Form.Label>
            <div className="input-group">
              <Form.Control
                  type={showNewPassword ? "text" : "password"}
                  name="newPassword"
                  value={formData.newPassword}
                  onChange={handleChange}
                  pattern="^(?=.*[A-Za-zÀ-ÖØ-öø-ÿ])(?=.*\d)(?=.*[@$!%*?&#])[A-Za-zÀ-ÖØ-öø-ÿ\d@$!%*?&#]{12,}$"
                  required
              />
              <Button
                  variant="outline-secondary"
                  onClick={() => setShowNewPassword(!showNewPassword)}
                  type="button"
              >
                <i className={`bi ${showNewPassword ? 'bi-eye-slash' : 'bi-eye'}`}></i>
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
                name="confirmPassword"
                value={formData.confirmPassword}
                onChange={handleChange}
                required
              />
              <Button 
                variant="outline-secondary" 
                onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                type="button"
              >
                <i className={`bi ${showConfirmPassword ? 'bi-eye-slash' : 'bi-eye'}`}></i>
              </Button>
              <Form.Control.Feedback type="invalid">
                Veuillez confirmer votre nouveau mot de passe.
              </Form.Control.Feedback>
            </div>
          </Form.Group>

          <Form.Group className="mb-3">
            <Form.Label>Question de sécurité</Form.Label>
            <p className="form-control-plaintext">{securityQuestion}</p>
          </Form.Group>

          <Form.Group className="mb-3">
            <Form.Label>Réponse à la question de sécurité</Form.Label>
            <Form.Control
              type="text"
              name="securityAnswer"
              value={formData.securityAnswer}
              onChange={handleChange}
              maxLength="32"
              required
            />
            <Form.Control.Feedback type="invalid">
              Veuillez répondre à la question de sécurité.
            </Form.Control.Feedback>
          </Form.Group>

          <Button variant="primary" type="submit">
            Mettre à jour le mot de passe
          </Button>
          {' '}
          {!isForced && (
            <Link to="/accueil">
              <Button variant="secondary">Annuler</Button>
            </Link>
          )}
        </Form>
      </Container>
    </>
  );
};

export default UpdatePassword;




