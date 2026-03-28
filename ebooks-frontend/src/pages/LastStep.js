import React from 'react';
import { Link } from 'react-router-dom';
import { Container } from 'react-bootstrap';
import Navigation from '../components/Navbar';

const LastStep = () => {
  return (
    <>
      <Navigation />
      <Container>
        <h2 className="mt-5">Vérifier votre compte</h2>
        <div>
          <p className="mb-0">
            Pour finaliser votre inscription, veuillez vérifier votre compte. Un lien de vérification a été envoyé à votre adresse email.
          </p>
        </div>
        <Link to="/login" className="btn btn-primary mt-3">
          Connexion
        </Link>
      </Container>
    </>
  );
};

export default LastStep;

