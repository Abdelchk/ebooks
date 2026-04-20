import React from 'react';
import { Container, Row, Col } from 'react-bootstrap';
import { Link } from 'react-router-dom';

const Footer = () => {
  return (
    <footer className="bg-dark text-light py-4 mt-5">
      <Container>
        <Row>
          <Col md={4}>
            <h5>
              <i className="bi bi-book"></i> Ebooks
            </h5>
            <p className="text-muted">
              Plateforme de gestion de livres électroniques sécurisée et conforme au RGPD.
            </p>
          </Col>

          <Col md={4}>
            <h6>Liens utiles</h6>
            <ul className="list-unstyled">
              <li>
                <Link to="/accueil" className="text-light text-decoration-none">
                  <i className="bi bi-house me-1"></i> Accueil
                </Link>
              </li>
              <li>
                <Link to="/about" className="text-light text-decoration-none">
                  <i className="bi bi-info-circle me-1"></i> À propos
                </Link>
              </li>
              <li>
                <a href="mailto:contact@ebooks.fr" className="text-light text-decoration-none">
                  <i className="bi bi-envelope me-1"></i> Contact
                </a>
              </li>
            </ul>
          </Col>

          <Col md={4}>
            <h6>Documents légaux</h6>
            <ul className="list-unstyled">
              <li>
                <a
                  href="/CGU.pdf"
                  target="_blank"
                  rel="noopener noreferrer"
                  className="text-light text-decoration-none"
                >
                  <i className="bi bi-file-pdf me-1"></i> CGU
                </a>
              </li>
              <li>
                <Link to="/privacy-policy" className="text-light text-decoration-none">
                  <i className="bi bi-shield-lock me-1"></i> Politique de Confidentialité
                </Link>
              </li>
              <li className="mt-2">
                <span className="badge bg-success">
                  <i className="bi bi-check-circle me-1"></i> Conforme RGPD
                </span>
              </li>
            </ul>
          </Col>
        </Row>

        <hr className="bg-light" />

        <Row>
          <Col className="text-center text-muted">
            <small>
              © {new Date().getFullYear()} Ebooks. Tous droits réservés.
              | Propulsé par Spring Boot & React
              | <i className="bi bi-shield-check"></i> Sécurisé avec Argon2 & reCAPTCHA Enterprise
            </small>
          </Col>
        </Row>
      </Container>
    </footer>
  );
};

export default Footer;

