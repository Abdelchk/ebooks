import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Navbar, Nav, Container, Button } from 'react-bootstrap';
import { useAuth } from '../context/AuthContext';

import CartCounter from "./CartCounter";

const Navigation = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = async () => {
    try {
      await logout();
      navigate('/login');
    } catch (error) {
      console.error('Erreur lors de la déconnexion:', error);
    }
  };

  return (
    <Navbar bg="dark" variant="dark" expand="lg">
      <Container fluid>
        <Navbar.Brand as={Link} to="/accueil">
          Ebooks
        </Navbar.Brand>
        <Navbar.Toggle aria-controls="navbar-nav" />
        <Navbar.Collapse id="navbar-nav">
          <Nav className="me-auto">
            <Nav.Link as={Link} to="/accueil">
              <i className="bi bi-house-fill me-1"></i>
              Accueil
            </Nav.Link>
            <Nav.Link as={Link} to="/about">
              <i className="bi bi-info-circle me-1"></i>
              À propos
            </Nav.Link>

            {!user && (
              <Nav.Link as={Link} to="/login">
                <i className="bi bi-box-arrow-in-right me-1"></i>
                Connexion
              </Nav.Link>
            )}

            {user && (
              <>
                <Nav.Link as={Link} to="/cart">
                  <i className="bi bi-cart3 me-1"></i>
                  Panier
                  <CartCounter />
                </Nav.Link>
                <Nav.Link as={Link} to="/reservations">
                  <i className="bi bi-bookmark-fill me-1"></i>
                  Réservations
                </Nav.Link>
                <Nav.Link as={Link} to="/loans">
                  <i className="bi bi-book me-1"></i>
                  Emprunts
                </Nav.Link>
                <Nav.Link as={Link} to="/update-password">
                  <i className="bi bi-key-fill me-1"></i>
                  Mot de passe
                </Nav.Link>
                <Button
                  variant="link"
                  className="nav-link text-white"
                  onClick={handleLogout}
                  style={{ textDecoration: 'none' }}
                >
                  <i className="bi bi-box-arrow-right me-1"></i>
                  Déconnexion
                </Button>
              </>
            )}
          </Nav>
        </Navbar.Collapse>
      </Container>
    </Navbar>
  );
};

export default Navigation;

