import React from 'react';
import { Link } from 'react-router-dom';
import { Container } from 'react-bootstrap';
import Navigation from '../components/Navbar';

const Index = () => {
  return (
    <>
      <Navigation />
      <Container className="mt-5">
        <p>
          Cliquez <Link to="/login">ici</Link> pour vous connecter.
        </p>
      </Container>
    </>
  );
};

export default Index;

