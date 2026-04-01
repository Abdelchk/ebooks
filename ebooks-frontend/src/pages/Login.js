import React, { useState } from 'react';
import { useNavigate, Link, useLocation } from 'react-router-dom';
import { Container, Form, Button, Alert } from 'react-bootstrap';
import { useAuth } from '../context/AuthContext';
import Navigation from '../components/Navbar';
import '../styles/password-toggle.css';

const Login = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [validated, setValidated] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const navigate = useNavigate();
  const { login } = useAuth();
  const location = useLocation();
  const message = location.state?.message;
  const from = location.state?.from || '/accueil';

  const handleSubmit = async (e) => {
    e.preventDefault();
    const form = e.currentTarget;

    if (form.checkValidity() === false) {
      e.stopPropagation();
      setValidated(true);
      return;
    }

    setValidated(true);
    setError('');

    try {
      console.log('🔐 Tentative de connexion avec:', email);
      const response = await login(email, password);
      console.log('📥 Réponse de connexion:', response);

      if (response && response.success) {
        console.log('✅ Connexion réussie');

        // Vérifier si 2FA est requis
        if (response.requiresTwoFactor) {
          console.log('🔐 2FA requis, redirection vers /verify-code');
          navigate('/verify-code');
        } else {
          console.log('✅ Pas de 2FA requis, redirection vers', from);
          navigate(from, { replace: true });
        }
      } else {
        console.error('❌ Connexion échouée:', response);
        setError(response?.message || 'Email ou mot de passe incorrect');
      }
    } catch (err) {
      console.error('❌ Erreur lors de la connexion:', err);
      if (err.response?.data?.message) {
        setError(err.response.data.message);
      } else if (err.response?.data) {
        setError(JSON.stringify(err.response.data));
      } else {
        setError('Erreur de connexion. Vérifiez que le backend est démarré.');
      }
    }
  };

  return (
    <>
      <Navigation />
      <Container>
        <h2 className="mt-5">Connexion</h2>

        {message && <Alert variant="info">{message}</Alert>}
        {error && <Alert variant="danger">{error}</Alert>}

        <Form noValidate validated={validated} onSubmit={handleSubmit}>
          <Form.Group className="mb-3">
            <Form.Label>Email</Form.Label>
            <div className="input-group">
              <Form.Control
                  type="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  pattern="[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+\.[A-Z|a-z]{2,}"
                  required
              />
              <Form.Control.Feedback type="invalid">
                Format de l'email incorrect.
              </Form.Control.Feedback>
            </div>
          </Form.Group>

          <Form.Group className="mb-3">
            <Form.Label>Mot de passe</Form.Label>
            <div className="input-group">
              <Form.Control
                type={showPassword ? "text" : "password"}
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
              />
              <Button 
                variant="outline-secondary" 
                onClick={() => setShowPassword(!showPassword)}
                type="button"
                title={showPassword ? "Masquer le mot de passe" : "Afficher le mot de passe"}
              >
                <i className={`bi ${showPassword ? 'bi-eye-slash-fill' : 'bi-eye-fill'}`}></i>
              </Button>
              <Form.Control.Feedback type="invalid">
                Veuillez entrer un mot de passe.
              </Form.Control.Feedback>
            </div>
          </Form.Group>

          <Button variant="primary" type="submit">
            Se connecter
          </Button>
          {' '}
          <Link to="/register">
            <Button variant="secondary">Créer un compte</Button>
          </Link>
          {' '}
          <Link to="/forgot-password">
            <Button variant="link">Mot de passe oublié ?</Button>
          </Link>
        </Form>
      </Container>
    </>
  );
};

export default Login;

