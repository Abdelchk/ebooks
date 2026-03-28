import React from 'react';
import { Container } from 'react-bootstrap';
import Navigation from '../components/Navbar';

const About = () => {
  return (
    <>
      <Navigation />
      <Container className="mt-5">
        <h2>À propos de Ebooks</h2>
        <p>
          Ebooks est une plateforme de gestion de livres électroniques développée avec Spring Boot et React.js.
        </p>
        <h4>Fonctionnalités</h4>
        <ul>
          <li>Inscription et authentification sécurisées</li>
          <li>Double authentification (2FA) par email</li>
          <li>Gestion des mots de passe avec politique stricte</li>
          <li>Protection reCAPTCHA Enterprise</li>
          <li>Questions de sécurité pour la récupération de compte</li>
          <li>Consultation du catalogue de livres</li>
        </ul>
        <h4>Technologies</h4>
        <ul>
          <li><strong>Frontend:</strong> React.js, React Router, Bootstrap, Axios</li>
          <li><strong>Backend:</strong> Spring Boot, Spring Security, Argon2</li>
          <li><strong>Base de données:</strong> MySQL/PostgreSQL</li>
        </ul>
      </Container>
    </>
  );
};

export default About;

