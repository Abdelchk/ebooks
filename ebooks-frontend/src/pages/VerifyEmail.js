import React, { useEffect, useState } from 'react';
import { useSearchParams, Link } from 'react-router-dom';
import { Container, Alert } from 'react-bootstrap';
import { authService } from '../services/authService';
import Navigation from '../components/Navbar';

const VerifyEmail = () => {
  const [searchParams] = useSearchParams();
  const [message, setMessage] = useState('');
  const [isSuccess, setIsSuccess] = useState(false);
  const token = searchParams.get('token');

  useEffect(() => {
    const verifyToken = async () => {
      try {
        const response = await authService.verifyEmail(token);
        if (response.success) {
          setMessage('Votre compte est vérifié.');
          setIsSuccess(true);
        } else {
          setMessage(response.message || 'Token de vérification invalide.');
          setIsSuccess(false);
        }
      } catch (error) {
        setMessage('Erreur lors de la vérification du compte.');
        setIsSuccess(false);
      }
    };

    if (token) {
      verifyToken();
    } else {
      setMessage('Token de vérification manquant');
      setIsSuccess(false);
    }
  }, [token]);

  return (
    <>
      <Navigation />
      <Container>
        <h2 className="mt-5">Vérifier l'Email</h2>
        <Alert variant={isSuccess ? 'success' : 'danger'}>{message}</Alert>
        <Link to="/login" className="btn btn-primary">
          Login
        </Link>
      </Container>
    </>
  );
};

export default VerifyEmail;


