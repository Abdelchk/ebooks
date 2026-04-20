import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Navbar, Nav, Container, Button, NavDropdown } from 'react-bootstrap';
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
                {/* Liens pour Bibliothécaire */}
                {user.role === 'librarian' && (
                  <Nav.Link as={Link} to="/librarian">
                    <i className="bi bi-journal-check me-1"></i>
                    Gestion Réservations
                  </Nav.Link>
                )}

                {/* Liens pour Administrateur */}
                {user.role === 'admin' && (
                  <Nav.Link as={Link} to="/admin">
                    <i className="bi bi-shield-lock me-1"></i>
                    Administration
                  </Nav.Link>
                )}

                {/* Liens pour Client */}
                {user.role === 'client' && (
                  <Nav.Link as={Link} to="/cart">
                    <i className="bi bi-cart3 me-1"></i>
                    Panier
                    <CartCounter />
                  </Nav.Link>
                )}

                {/* Menu Profil pour tous les utilisateurs connectés */}
                <NavDropdown
                  title={
                    <>
                      <i className="bi bi-person-circle me-1"></i>
                      Profil
                    </>
                  }
                  id="profile-dropdown"
                >
                  <NavDropdown.Item as={Link} to="/profile#info">
                    <i className="bi bi-person-fill me-2"></i>
                    Informations
                  </NavDropdown.Item>
                  <NavDropdown.Item as={Link} to="/profile#reservations">
                    <i className="bi bi-bookmark-fill me-2"></i>
                    Réservations
                  </NavDropdown.Item>
                  <NavDropdown.Item as={Link} to="/profile#loans">
                    <i className="bi bi-book me-2"></i>
                    Emprunts
                  </NavDropdown.Item>
                  <NavDropdown.Item as={Link} to="/profile#alerts">
                    <i className="bi bi-bell-fill me-2"></i>
                    Alertes
                  </NavDropdown.Item>
                  <NavDropdown.Divider />
                  <NavDropdown.Item as={Link} to="/update-password">
                    <i className="bi bi-key-fill me-2"></i>
                    Changer le mot de passe
                  </NavDropdown.Item>
                </NavDropdown>
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

