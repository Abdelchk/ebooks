import React from 'react';
import PropTypes from 'prop-types';
import { Alert, Button, Container } from 'react-bootstrap';

/**
 * Vrai React Error Boundary (doit être une classe)
 * Capture toutes les erreurs de rendu et empêche la page blanche
 */
class ErrorBoundary extends React.Component {
  constructor(props) {
    super(props);
    this.state = { hasError: false, error: null };
  }

  static getDerivedStateFromError(error) {
    return { hasError: true, error };
  }

  componentDidCatch(error, info) {
    console.error('ErrorBoundary a capturé une erreur:', error, info);
  }

  handleReset = () => {
    this.setState({ hasError: false, error: null });
  };

  render() {
    if (this.state.hasError) {
      return (
        <Container className="mt-5">
          <Alert variant="danger">
            <Alert.Heading>⚠️ Une erreur inattendue s'est produite</Alert.Heading>
            <p>
              {this.state.error?.message || 'Erreur inconnue. Vérifiez que le serveur est accessible.'}
            </p>
            <hr />
            <div className="d-flex gap-2">
              <Button variant="outline-danger" onClick={this.handleReset}>
                Réessayer
              </Button>
              <Button variant="outline-secondary" onClick={() => window.location.reload()}>
                Recharger la page
              </Button>
            </div>
          </Alert>
        </Container>
      );
    }

    return this.props.children;
  }
}

ErrorBoundary.propTypes = {
  children: PropTypes.node.isRequired,
};

export default ErrorBoundary;

