import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Container, Form, Button, Alert, InputGroup } from 'react-bootstrap';
import { authService } from '../services/authService';
import Navigation from '../components/Navbar';

const Register = () => {
  const [formData, setFormData] = useState({
    firstname: '',
    lastname: '',
    email: '',
    password: '',
    confirmPassword: '',
    birthdate: '',
    phoneNumber: '',
    questionId: '',
    securityAnswer: '',
  });
  const [questions, setQuestions] = useState([]);
  const [error, setError] = useState('');
  const [validated, setValidated] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [recaptchaKey, setRecaptchaKey] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    loadSecurityQuestions();
    loadRecaptchaKey();
  }, []);

  const loadSecurityQuestions = async () => {
    try {
      const data = await authService.getSecurityQuestions();
      setQuestions(data);
    } catch (error) {
      console.error('Erreur lors du chargement des questions:', error);
    }
  };

  const loadRecaptchaKey = async () => {
    try {
      const data = await authService.getRecaptchaKey();
      setRecaptchaKey(data.siteKey);

      // Charger le script reCAPTCHA
      if (data.siteKey && !document.querySelector(`script[src*="recaptcha"]`)) {
        const script = document.createElement('script');
        script.src = `https://www.google.com/recaptcha/enterprise.js?render=${data.siteKey}`;
        document.head.appendChild(script);
      }
    } catch (error) {
      console.error('Erreur lors du chargement de la clé reCAPTCHA:', error);
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

    if (formData.password !== formData.confirmPassword) {
      setError('Les mots de passe ne correspondent pas');
      setValidated(true);
      return;
    }

    setValidated(true);
    setIsSubmitting(true);
    setError('');

    try {
      // Exécuter reCAPTCHA
      if (typeof window.grecaptcha === 'undefined') {
        setError('reCAPTCHA non chargé. Veuillez rafraîchir la page.');
        setIsSubmitting(false);
        return;
      }

      window.grecaptcha.enterprise.ready(async () => {
        try {
          const token = await window.grecaptcha.enterprise.execute(recaptchaKey, { action: 'REGISTER' });

          const response = await authService.register({
            ...formData,
            recaptchaToken: token,
          });

          if (response.success) {
            navigate('/last-step');
          } else {
            setError(response.message || 'Erreur lors de l\'inscription');
          }
        } catch (err) {
          if (err.response?.data?.message) {
            setError(err.response.data.message);
          } else {
            setError('Erreur lors de l\'inscription : ' + err.message);
          }
        } finally {
          setIsSubmitting(false);
        }
      });
    } catch (err) {
      setError('Erreur lors de la vérification reCAPTCHA');
      setIsSubmitting(false);
    }
  };

  return (
    <>
      <Navigation />
      <Container>
        <h2 className="mt-5">Inscription</h2>

        {error && <Alert variant="danger">{error}</Alert>}

        <Form noValidate validated={validated} onSubmit={handleSubmit}>
          <Form.Group className="mb-3">
            <Form.Label>Nom</Form.Label>
            <InputGroup>
              <Form.Control
                  type="text"
                  name="lastname"
                  value={formData.lastname}
                  onChange={handleChange}
                  pattern="^[a-zA-ZÀ-ÿ\s-]+$"
                  required
              />
              <Form.Control.Feedback type="invalid">
                Entrez votre nom.
              </Form.Control.Feedback>
            </InputGroup>
          </Form.Group>

          <Form.Group className="mb-3">
            <Form.Label>Prénom</Form.Label>
            <InputGroup>
              <Form.Control
                  type="text"
                  name="firstname"
                  value={formData.firstname}
                  onChange={handleChange}
                  pattern="^[a-zA-ZÀ-ÿ\s-]+$"
                  required
              />
              <Form.Control.Feedback type="invalid">
                Entrez votre prénom.
              </Form.Control.Feedback>
            </InputGroup>
          </Form.Group>

          <Form.Group className="mb-3">
            <Form.Label>Date de naissance</Form.Label>
            <InputGroup>
              <Form.Control
                  type="date"
                  name="birthdate"
                  value={formData.birthdate}
                  onChange={handleChange}
                  required
              />
              <Form.Control.Feedback type="invalid">
                Entrez une date de naissance valide.
              </Form.Control.Feedback>
            </InputGroup>
          </Form.Group>

          <Form.Group className="mb-3">
            <Form.Label>N° de téléphone</Form.Label>
            <InputGroup>
              <Form.Control
                  type="text"
                  name="phoneNumber"
                  value={formData.phoneNumber}
                  onChange={handleChange}
                  pattern="^(0|\+33|0033)[1-9][0-9]{8}"
                  required
              />
              <Form.Control.Feedback type="invalid">
                Le numéro de téléphone doit contenir exactement 10 chiffres.
              </Form.Control.Feedback>
            </InputGroup>
          </Form.Group>

          <Form.Group className="mb-3">
            <Form.Label>Email</Form.Label>
            <InputGroup>
              <Form.Control
                  type="email"
                  name="email"
                  value={formData.email}
                  onChange={handleChange}
                  pattern="[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+\.[A-Z|a-z]{2,}"
                  required
              />
              <Form.Control.Feedback type="invalid">
                Veuillez saisir un email valide.
              </Form.Control.Feedback>
            </InputGroup>
          </Form.Group>

          <Form.Group className="mb-3">
            <Form.Label>Question de sécurité</Form.Label>
            <InputGroup>
              <Form.Select
                  name="questionId"
                  value={formData.questionId}
                  onChange={handleChange}
                  required
              >
                <option value="">-- Choisissez une question de sécurité --</option>
                {Array.isArray(questions) && questions.length > 0 ? (
                    questions.map((question) => (
                        <option key={question.id} value={question.id}>
                          {question.question}
                        </option>
                    ))
                ) : (
                    <option disabled>Chargement des questions...</option>
                )}
              </Form.Select>
              <Form.Control.Feedback type="invalid">
                Veuillez choisir une question de sécurité.
              </Form.Control.Feedback>
            </InputGroup>
          </Form.Group>

          <Form.Group className="mb-3">
            <Form.Label>Réponse de sécurité</Form.Label>
            <InputGroup>
              <Form.Control
                  type="text"
                  name="securityAnswer"
                  value={formData.securityAnswer}
                  onChange={handleChange}
                  maxLength="32"
                  placeholder="Votre réponse"
                  required
              />
              <Form.Control.Feedback type="invalid">
                Veuillez entrer une réponse de sécurité.
              </Form.Control.Feedback>
            </InputGroup>
          </Form.Group>

          <Form.Group className="mb-3">
            <Form.Label>Mot de passe</Form.Label>
            <InputGroup>
              <Form.Control
                type={showPassword ? 'text' : 'password'}
                name="password"
                value={formData.password}
                onChange={handleChange}
                pattern="^(?=.*[A-Za-zÀ-ÖØ-öø-ÿ])(?=.*\d)(?=.*[@$!%*?&#])[A-Za-zÀ-ÖØ-öø-ÿ\d@$!%*?&#]{12,}$"
                required
              />
              <Button
                variant="outline-secondary"
                onClick={() => setShowPassword(!showPassword)}
              >
                <i className={showPassword ? 'bi bi-eye-slash-fill' : 'bi bi-eye-fill'}></i>
              </Button>
              <Form.Control.Feedback type="invalid">
                Veuillez saisir un mot de passe valide. 12 caractères minimum, au moins une lettre majuscule, une lettre minuscule, un chiffre et un caractère spécial.
              </Form.Control.Feedback>
            </InputGroup>
          </Form.Group>

          <Form.Group className="mb-3">
            <Form.Label>Confirmer mot de passe</Form.Label>
            <InputGroup>
              <Form.Control
                type={showConfirmPassword ? 'text' : 'password'}
                name="confirmPassword"
                value={formData.confirmPassword}
                onChange={handleChange}
                required
              />
              <Button
                variant="outline-secondary"
                onClick={() => setShowConfirmPassword(!showConfirmPassword)}
              >
                <i className={showConfirmPassword ? 'bi bi-eye-slash-fill' : 'bi bi-eye-fill'}></i>
              </Button>
              <Form.Control.Feedback type="invalid">
                Les mots de passe ne correspondent pas.
              </Form.Control.Feedback>
            </InputGroup>
          </Form.Group>

          <Alert variant="secondary" className="mt-3">
            <i className="bi bi-shield-check"></i> Ce site est protégé par Google reCAPTCHA Enterprise. La vérification se fait automatiquement lors de la soumission.
          </Alert>

          <Button variant="primary" type="submit" disabled={isSubmitting} className="mt-3">
            {isSubmitting ? 'Vérification en cours...' : 'Valider'}
          </Button>
        </Form>
      </Container>
    </>
  );
};

export default Register;


