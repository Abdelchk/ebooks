import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { Container, Form, Button, Alert } from 'react-bootstrap';
import { authService } from '../services/authService';
import Navigation from '../components/Navbar';

const ForgotPassword = () => {
  const [email, setEmail] = useState('');
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [validated, setValidated] = useState(false);
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    const form = e.currentTarget;

    if (form.checkValidity() === false) {
      e.stopPropagation();
      setValidated(true);
      return;
    }

    setValidated(true);

    try {
      const response = await authService.forgotPassword(email);
      if (response.success) {
        setMessage('Un email de réinitialisation a été envoyé à votre adresse.');
        setError('');
        setTimeout(() => navigate('/login'), 3000);
      } else {
        setError(response.message);
        setMessage('');
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Erreur lors de l\'envoi de l\'email');
      setMessage('');
    }
  };

  return (
    <>
      <Navigation />
      <Container>
        <h2 className="mt-5">Mot de passe oublié</h2>

        {error && <Alert variant="danger">{error}</Alert>}
        {message && <Alert variant="success">{message}</Alert>}

        <Alert variant="info">
          Entrez votre adresse email. Nous vous enverrons un lien pour réinitialiser votre mot de passe.
        </Alert>

        <Form noValidate validated={validated} onSubmit={handleSubmit}>
          <Form.Group className="mb-3">
            <Form.Label>Adresse email</Form.Label>
            <Form.Control
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              pattern="[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+\.[A-Z|a-z]{2,}"
              required
            />
            <Form.Control.Feedback type="invalid">
              Veuillez entrer une adresse email valide.
            </Form.Control.Feedback>
          </Form.Group>

          <Button variant="primary" type="submit">
            Envoyer le lien de réinitialisation
          </Button>
          {' '}
          <Link to="/login">
            <Button variant="secondary">Retour à la connexion</Button>
          </Link>
        </Form>
      </Container>
    </>
  );
};

export default ForgotPassword;

