import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Container, Form, Button, Alert } from 'react-bootstrap';
import { authService } from '../services/authService';

const VerifyCode = () => {
  const [code, setCode] = useState('');
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();

    try {
      const response = await authService.verifyCode(code);
      if (response.success) {
        navigate('/accueil');
      } else {
        setError(response.message);
        setMessage('');
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Code invalide ou expiré');
      setMessage('');
    }
  };

  const handleResend = async () => {
    try {
      const response = await authService.resendCode();
      if (response.success) {
        setMessage(response.message);
        setError('');
      }
    } catch (err) {
      setError('Erreur lors du renvoi du code');
      setMessage('');
    }
  };

  return (
    <Container>
      <h2 className="mt-5">Saisissez le code de vérification</h2>

      {error && <Alert variant="danger">{error}</Alert>}
      {message && <Alert variant="success">{message}</Alert>}

      <Form onSubmit={handleSubmit}>
        <Form.Group className="mb-3">
          <Form.Control
            type="text"
            value={code}
            onChange={(e) => setCode(e.target.value)}
            maxLength="6"
            pattern="[0-9]{6}"
            placeholder="Code à 6 chiffres"
            required
          />
        </Form.Group>
        <Button variant="primary" type="submit">
          Valider
        </Button>
      </Form>

      <Button variant="secondary" className="mt-3" onClick={handleResend}>
        Renvoyer le code
      </Button>
    </Container>
  );
};

export default VerifyCode;

