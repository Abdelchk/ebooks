import React from 'react';
import { Container, Card, Row, Col, Button } from 'react-bootstrap';
import { Link } from 'react-router-dom';
import Navigation from '../components/Navbar';

const About = () => {
  return (
    <>
      <Navigation />
      <Container className="mt-5 mb-5">
        <h2><i className="bi bi-info-circle"></i> À propos de Ebooks</h2>
        <p className="lead">
          Ebooks est une plateforme de gestion de livres électroniques développée avec Spring Boot et React.js.
        </p>

        <Row className="mt-4">
          <Col md={6}>
            <Card className="mb-4">
              <Card.Header className="bg-primary text-white">
                <i className="bi bi-list-check"></i> Fonctionnalités
              </Card.Header>
              <Card.Body>
                <ul>
                  <li>Inscription et authentification sécurisées</li>
                  <li>Double authentification (2FA) par email</li>
                  <li>Gestion des mots de passe avec politique stricte</li>
                  <li>Protection reCAPTCHA Enterprise</li>
                  <li>Questions de sécurité pour la récupération de compte</li>
                  <li>Consultation du catalogue de livres</li>
                  <li>Système de réservation et d'emprunt</li>
                  <li>Notifications et alertes personnalisées</li>
                </ul>
              </Card.Body>
            </Card>
          </Col>

          <Col md={6}>
            <Card className="mb-4">
              <Card.Header className="bg-success text-white">
                <i className="bi bi-code-slash"></i> Technologies
              </Card.Header>
              <Card.Body>
                <ul>
                  <li><strong>Frontend:</strong> React.js, React Router, Bootstrap, Axios</li>
                  <li><strong>Backend:</strong> Spring Boot, Spring Security, Argon2</li>
                  <li><strong>Base de données:</strong> MySQL</li>
                  <li><strong>Sécurité:</strong> Google reCAPTCHA Enterprise, JWT</li>
                  <li><strong>Email:</strong> Spring Mail avec stratégie pattern</li>
                </ul>
              </Card.Body>
            </Card>
          </Col>
        </Row>

        {/* Section RGPD */}
        <Card className="mt-4 border-info">
          <Card.Header className="bg-info text-white">
            <i className="bi bi-shield-lock"></i> Protection des Données et RGPD
          </Card.Header>
          <Card.Body>
            <h5>Conformité RGPD</h5>
            <p>
              Chez <strong>Ebooks</strong>, nous prenons très au sérieux la protection de vos données 
              personnelles. Notre plateforme est entièrement conforme au Règlement Général sur la 
              Protection des Données (RGPD).
            </p>

            <h6 className="mt-3"><i className="bi bi-lock-fill"></i> Vos données sont protégées</h6>
            <ul>
              <li>
                <strong>Chiffrement Argon2 :</strong> Vos mots de passe sont chiffrés avec l'algorithme 
                Argon2, recommandé par l'OWASP comme le plus sûr
              </li>
              <li>
                <strong>Communications sécurisées :</strong> Tous les échanges sont chiffrés en HTTPS/SSL
              </li>
              <li>
                <strong>Double authentification :</strong> Protection supplémentaire avec 2FA par email
              </li>
              <li>
                <strong>Protection anti-fraude :</strong> Google reCAPTCHA Enterprise pour détecter 
                et bloquer les activités malveillantes
              </li>
            </ul>

            <h6 className="mt-3"><i className="bi bi-person-check"></i> Vos droits</h6>
            <p>Conformément au RGPD, vous disposez des droits suivants :</p>
            <Row>
              <Col md={6}>
                <ul>
                  <li><strong>Droit d'accès</strong> à vos données</li>
                  <li><strong>Droit de rectification</strong> de vos informations</li>
                  <li><strong>Droit à l'effacement</strong> ("droit à l'oubli")</li>
                </ul>
              </Col>
              <Col md={6}>
                <ul>
                  <li><strong>Droit à la portabilité</strong> de vos données</li>
                  <li><strong>Droit d'opposition</strong> au traitement</li>
                  <li><strong>Droit de limitation</strong> du traitement</li>
                </ul>
              </Col>
            </Row>

            <h6 className="mt-3"><i className="bi bi-clipboard-data"></i> Données collectées</h6>
            <p>Nous collectons uniquement les données nécessaires au fonctionnement du service :</p>
            <ul>
              <li>Données d'identification (nom, prénom, date de naissance)</li>
              <li>Données de contact (email, téléphone)</li>
              <li>Données de connexion (chiffrées)</li>
              <li>Historique d'utilisation (emprunts, réservations)</li>
            </ul>
            <p className="text-muted">
              <small>
                <i className="bi bi-info-circle"></i> Nous ne vendons jamais vos données à des tiers.
              </small>
            </p>

            <h6 className="mt-3"><i className="bi bi-clock-history"></i> Conservation des données</h6>
            <ul>
              <li>Compte actif : pendant toute la durée d'utilisation</li>
              <li>Compte inactif : 3 ans après la dernière connexion</li>
              <li>Données de connexion : 1 an (obligation légale)</li>
              <li>Après suppression : 30 jours en archive de sécurité</li>
            </ul>

            <div className="mt-4 p-3 bg-light rounded">
              <h6><i className="bi bi-envelope"></i> Exercer vos droits</h6>
              <p className="mb-2">
                Pour accéder, modifier ou supprimer vos données personnelles :
              </p>
              <ul className="mb-3">
                <li>Via votre espace <Link to="/profile">Profil</Link> sur le site</li>
                <li>Par email : <a href="mailto:check.abdel@gmail.com">check.abdel@gmail.com</a></li>
              </ul>
              <Link to="/privacy-policy">
                <Button variant="info" size="sm">
                  <i className="bi bi-file-text"></i> Consulter notre Politique de Confidentialité complète
                </Button>
              </Link>
            </div>
          </Card.Body>
        </Card>

        {/* Section Contact et Réclamation */}
        <Card className="mt-4 border-warning">
          <Card.Header className="bg-warning">
            <i className="bi bi-exclamation-triangle"></i> Questions ou Réclamations
          </Card.Header>
          <Card.Body>
            <p>
              <strong>Délégué à la Protection des Données (DPO) :</strong><br />
              Email : <a href="mailto:check.abdel@gmail.com">check.abdel@gmail.com</a>
            </p>
            <p className="mb-0">
              <strong>Réclamation auprès de la CNIL :</strong><br />
              Si vous estimez que vos droits ne sont pas respectés, vous pouvez déposer 
              une réclamation auprès de la Commission Nationale de l'Informatique et des 
              Libertés : <a href="https://www.cnil.fr" target="_blank" rel="noopener noreferrer">
                www.cnil.fr
              </a>
            </p>
          </Card.Body>
        </Card>

        {/* Section CGU */}
        <Card className="mt-4">
          <Card.Body>
            <h5><i className="bi bi-file-earmark-text"></i> Documents légaux</h5>
            <ul className="list-unstyled">
              <li className="mb-2">
                <a href="/CGU.pdf" target="_blank" rel="noopener noreferrer">
                  <i className="bi bi-file-pdf"></i> Conditions Générales d'Utilisation (CGU)
                </a>
              </li>
              <li className="mb-2">
                <Link to="/privacy-policy">
                  <i className="bi bi-shield-lock"></i> Politique de Confidentialité et RGPD
                </Link>
              </li>
            </ul>
          </Card.Body>
        </Card>
      </Container>
    </>
  );
};

export default About;

