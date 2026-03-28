import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Navbar, Nav, Container, Button } from 'react-bootstrap';
import { useAuth } from '../context/AuthContext';

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
            <Nav.Link as={Link} to="/about">
              À propos
            </Nav.Link>

            {!user && (
              <Nav.Link as={Link} to="/login">
                Connexion
              </Nav.Link>
            )}

            {user && (
              <>
                <Nav.Link as={Link} to="/update-password">
                  Changer mot de passe
                </Nav.Link>
                <Button
                  variant="link"
                  className="nav-link text-white"
                  onClick={handleLogout}
                  style={{ textDecoration: 'none' }}
                >
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

