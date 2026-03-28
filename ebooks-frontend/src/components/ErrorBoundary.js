import React from 'react';
import { Alert } from 'react-bootstrap';

const ErrorBoundary = ({ error, resetError }) => {
  if (!error) return null;

  return (
    <Alert variant="danger" dismissible onClose={resetError}>
      <Alert.Heading>Une erreur s'est produite</Alert.Heading>
      <p>{error}</p>
    </Alert>
  );
};

export default ErrorBoundary;

