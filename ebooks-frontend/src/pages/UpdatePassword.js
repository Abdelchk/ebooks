import React, { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { Container, Form, Button, Alert } from 'react-bootstrap';
import { authService } from '../services/authService';
import Navigation from '../components/Navbar';

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
  const navigate = useNavigate();

  useEffect(() => {
    loadSecurityQuestion();
  }, []);

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

        {error && <Alert variant="danger">{error}</Alert>}
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
            <Form.Control
              type="password"
              name="oldPassword"
              value={formData.oldPassword}
              onChange={handleChange}
              required
            />
            <Form.Control.Feedback type="invalid">
              Veuillez entrer votre ancien mot de passe.
            </Form.Control.Feedback>
          </Form.Group>

          <Form.Group className="mb-3">
            <Form.Label>Nouveau mot de passe</Form.Label>
            <Form.Control
              type="password"
              name="newPassword"
              value={formData.newPassword}
              onChange={handleChange}
              pattern="^(?=.*[A-Za-zÀ-ÖØ-öø-ÿ])(?=.*\d)(?=.*[@$!%*?&#])[A-Za-zÀ-ÖØ-öø-ÿ\d@$!%*?&#]{12,}$"
              required
            />
            <Form.Control.Feedback type="invalid">
              Le mot de passe doit contenir au moins 12 caractères, une lettre, un chiffre et un caractère spécial.
            </Form.Control.Feedback>
          </Form.Group>

          <Form.Group className="mb-3">
            <Form.Label>Confirmer le nouveau mot de passe</Form.Label>
            <Form.Control
              type="password"
              name="confirmPassword"
              value={formData.confirmPassword}
              onChange={handleChange}
              required
            />
            <Form.Control.Feedback type="invalid">
              Veuillez confirmer votre nouveau mot de passe.
            </Form.Control.Feedback>
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
          <Link to="/accueil">
            <Button variant="secondary">Annuler</Button>
          </Link>
        </Form>
      </Container>
    </>
  );
};

export default UpdatePassword;




