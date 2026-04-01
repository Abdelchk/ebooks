import React, { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Container, Row, Col, Card, Button, Alert, Badge } from 'react-bootstrap';
import { bookService } from '../services/bookService';
import { useAuth } from '../context/AuthContext';
import Navigation from '../components/Navbar';
import Loader from '../components/Loader';
import './BookDetail.css';

const BookDetail = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  const [book, setBook] = useState(null);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);

  const loadBookDetails = useCallback(async () => {
    try {
      const data = await bookService.getBookById(id);
      setBook(data);
    } catch (err) {
      setError('Erreur lors du chargement du livre.');
      console.error(err);
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => {
    loadBookDetails();
  }, [loadBookDetails]);

  const handleBack = () => {
    navigate('/accueil');
  };

  const handleAddToCart = () => {
    if (!user) {
      // Rediriger vers la page de connexion si non connecté
      navigate('/login', {
        state: {
          from: `/book/${id}`,
          message: 'Veuillez vous connecter pour ajouter des livres au panier.'
        }
      });
    } else {
      // TODO: Implémenter la logique d'ajout au panier
      alert('Livre ajouté au panier !');
    }
  };

  const handleAddToFavorites = () => {
    if (!user) {
      // Rediriger vers la page de connexion si non connecté
      navigate('/login', {
        state: {
          from: `/book/${id}`,
          message: 'Veuillez vous connecter pour ajouter des livres aux favoris.'
        }
      });
    } else {
      // TODO: Implémenter la logique d'ajout aux favoris
      alert('Livre ajouté aux favoris !');
    }
  };

  if (loading) {
    return (
      <>
        <Navigation />
        <Container className="mt-4">
          <Loader message="Chargement du livre..." />
        </Container>
      </>
    );
  }

  if (error || !book) {
    return (
      <>
        <Navigation />
        <Container className="mt-4">
          <Alert variant="danger">{error || 'Livre introuvable'}</Alert>
          <Button variant="secondary" onClick={handleBack}>
            <i className="bi bi-arrow-left me-2"></i>Retour à la liste
          </Button>
        </Container>
      </>
    );
  }

  return (
    <>
      <Navigation />
      <Container className="mt-4 book-detail-container">
        <Button variant="link" onClick={handleBack} className="mb-3 p-0 text-decoration-none">
          <i className="bi bi-arrow-left me-2"></i>Retour à la liste
        </Button>

        <Row>
          <Col md={4}>
            <Card className="book-cover-card shadow">
              <Card.Img
                variant="top"
                src={book.coverImageUrl}
                alt={`Couverture de ${book.title}`}
                className="book-cover-image"
              />
              {book.quantity === 0 && (
                <Badge bg="danger" className="stock-badge">
                  <i className="bi bi-x-circle me-1"></i>Rupture de stock
                </Badge>
              )}
              {book.quantity > 0 && book.quantity <= 5 && (
                <Badge bg="warning" text="dark" className="stock-badge">
                  <i className="bi bi-exclamation-triangle me-1"></i>Stock limité
                </Badge>
              )}
              {book.quantity > 5 && (
                <Badge bg="success" className="stock-badge">
                  <i className="bi bi-check-circle me-1"></i>Disponible
                </Badge>
              )}
            </Card>
          </Col>

          <Col md={8}>
            <div className="book-info">
              <h1 className="book-title">{book.title}</h1>
              <p className="book-author">
                <i className="bi bi-person-fill me-2"></i>
                <strong>{book.author}</strong>
              </p>

              {book.publishDate && (
                <p className="book-meta">
                  <i className="bi bi-calendar3 me-2"></i>
                  Date de publication : {new Date(book.publishDate).toLocaleDateString('fr-FR')}
                </p>
              )}

              {book.genre && (
                <p className="book-meta">
                  <i className="bi bi-tag-fill me-2"></i>
                  Genre : <Badge bg="info">{book.genre}</Badge>
                </p>
              )}

              <div className="book-stock-info mt-3">
                <h5>
                  <i className="bi bi-box-seam me-2"></i>Stock disponible
                </h5>
                <p className="stock-quantity">
                  {book.quantity} exemplaire{book.quantity > 1 ? 's' : ''} disponible{book.quantity > 1 ? 's' : ''}
                </p>
              </div>

              <hr />

              <div className="book-description mt-4">
                <h4>
                  <i className="bi bi-card-text me-2"></i>À propos
                </h4>
                <p className="description-text">{book.description}</p>
              </div>

              {book.isbn && (
                <div className="book-isbn mt-3">
                  <small className="text-muted">
                    <i className="bi bi-upc me-2"></i>ISBN : {book.isbn}
                  </small>
                </div>
              )}

              <div className="book-actions mt-4">
                {book.quantity > 0 ? (
                  <>
                    <Button variant="primary" size="lg" className="me-2" onClick={handleAddToCart}>
                      <i className="bi bi-cart-plus me-2"></i>Ajouter au panier
                    </Button>
                    <Button variant="outline-secondary" size="lg" onClick={handleAddToFavorites}>
                      <i className="bi bi-heart me-2"></i>Ajouter aux favoris
                    </Button>
                  </>
                ) : (
                  <Button variant="secondary" size="lg" disabled>
                    <i className="bi bi-x-circle me-2"></i>Indisponible
                  </Button>
                )}
              </div>
            </div>
          </Col>
        </Row>

        <Row className="mt-5">
          <Col>
            <h4>Voir les détails produits</h4>
            <Card className="details-card">
              <Card.Body>
                <Row>
                  <Col md={6}>
                    <p><strong>Titre :</strong> {book.title}</p>
                    <p><strong>Auteur :</strong> {book.author}</p>
                    {book.publishDate && (
                      <p><strong>Publication :</strong> {new Date(book.publishDate).toLocaleDateString('fr-FR')}</p>
                    )}
                  </Col>
                  <Col md={6}>
                    {book.genre && <p><strong>Genre :</strong> {book.genre}</p>}
                    {book.isbn && <p><strong>ISBN :</strong> {book.isbn}</p>}
                    <p><strong>Stock :</strong> {book.quantity}</p>
                  </Col>
                </Row>
              </Card.Body>
            </Card>
          </Col>
        </Row>
      </Container>
    </>
  );
};

export default BookDetail;

