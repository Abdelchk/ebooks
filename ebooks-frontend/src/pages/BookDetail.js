import React, { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {Container, Row, Col, Card, Button, Alert, Badge, Modal, Form} from 'react-bootstrap';
import { bookService } from '../services/bookService';
import { cartService } from '../services/cartService';
import { stockAlertService } from '../services/stockAlertService';
import librarianService from '../services/librarianService';
import { useAuth } from '../context/AuthContext';
import Navigation from '../components/Navbar';
import LoanDurationSelector from "../components/LoanDurationSelector";
import Loader from '../components/Loader';
import './BookDetail.css';

const BookDetail = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  const [book, setBook] = useState(null);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);
  const [loanDuration, setLoanDuration] = useState(14);
  const [addingToCart, setAddingToCart] = useState(false);
  const [showSuccessModal, setShowSuccessModal] = useState(false);
  const [successMessage, setSuccessMessage] = useState('');
  const [showAddToCartModal, setShowAddToCartModal] = useState(false);
  const [showStockAlertModal, setShowStockAlertModal] = useState(false);

  // État pour le restockage (bibliothécaire / admin)
  const isPrivileged = user && (user.role === 'librarian' || user.role === 'admin');
  const [restockQty, setRestockQty] = useState(1);
  const [restocking, setRestocking] = useState(false);
  const [restockMessage, setRestockMessage] = useState(null);

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

  const handleAddToCart = async () => {
    if (!user) {
      // Rediriger vers la page de connexion si non connecté
      navigate('/login', {
        state: {
          from: `/book/${id}`,
          message: 'Veuillez vous connecter pour ajouter des livres au panier.'
        }
      });
      return;
    }
    setShowAddToCartModal(true);
  };

  const confirmAddToCart = async () => {
    try {
      setAddingToCart(true);
      setShowAddToCartModal(false);
      await cartService.addToCart(book.id, loanDuration);
      setSuccessMessage(`"${book.title}" a été ajouté au panier pour ${loanDuration} jours !`);
      setShowSuccessModal(true);
    } catch (err) {
      setError(err.response?.data?.message || 'Erreur lors de l\'ajout au panier');
    } finally {
      setAddingToCart(false);
    }
  };

  const handleStockAlert = () => {
    if (!user) {
      navigate('/login', {
        state: {
          from: `/book/${id}`,
          message: 'Veuillez vous connecter pour recevoir des alertes de disponibilité.'
        }
      });
      return;
    }
    setShowStockAlertModal(true);
  };

  const confirmStockAlert = async () => {
    try {
      setShowStockAlertModal(false);
      await stockAlertService.createAlert(book.id);
      setSuccessMessage(`Vous serez notifié par email à ${user.email} dès que "${book.title}" sera de nouveau disponible.`);
      setShowSuccessModal(true);
    } catch (err) {
      setError(err.response?.data?.message || 'Erreur lors de la création de l\'alerte');
      console.error(err);
    }
  };

  const handleRestock = async () => {
    if (!book || restockQty < 1) return;
    setRestocking(true);
    setRestockMessage(null);
    try {
      const updatedBook = { ...book, quantity: book.quantity + restockQty };
      await librarianService.updateBook(book.id, updatedBook);
      setBook(updatedBook);
      setRestockMessage({ type: 'success', text: `Stock mis à jour : +${restockQty} exemplaire(s). Nouveau stock : ${updatedBook.quantity}` });
      setRestockQty(1);
    } catch (err) {
      setRestockMessage({ type: 'danger', text: err.response?.data?.message || 'Erreur lors du restockage' });
    } finally {
      setRestocking(false);
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

              {user && book.quantity > 0 && !isPrivileged && (
                <div className="loan-duration-section mt-4">
                  <h5>
                    <i className="bi bi-calendar-range me-2"></i>
                    Durée d'emprunt souhaitée
                  </h5>
                  <LoanDurationSelector
                    value={loanDuration}
                    onChange={setLoanDuration}
                  />
                </div>
              )}

              <div className="book-actions mt-4">
                {/* Actions CLIENT uniquement */}
                {!isPrivileged && (
                  book.quantity > 0 ? (
                    <Button
                      variant="primary"
                      size="lg"
                      className="me-2"
                      onClick={handleAddToCart}
                      disabled={addingToCart}
                    >
                      <i className="bi bi-cart-plus me-2"></i>
                      {addingToCart ? 'Ajout en cours...' : 'Ajouter au panier'}
                    </Button>
                  ) : (
                    <>
                      <Button variant="secondary" size="lg" disabled className="me-2">
                        <i className="bi bi-x-circle me-2"></i>Indisponible
                      </Button>
                      <Button variant="warning" size="lg" onClick={handleStockAlert}>
                        <i className="bi bi-bell me-2"></i>
                        M'alerter quand disponible
                      </Button>
                    </>
                  )
                )}

                {/* Section RESTOCKAGE pour bibliothécaire / admin */}
                {isPrivileged && (
                  <Card className="border-success mt-2">
                    <Card.Header className="bg-success text-white">
                      <i className="bi bi-box-seam me-2"></i>Gestion du stock
                    </Card.Header>
                    <Card.Body>
                      {restockMessage && (
                        <Alert variant={restockMessage.type} className="mb-3" dismissible onClose={() => setRestockMessage(null)}>
                          {restockMessage.text}
                        </Alert>
                      )}
                      <p className="mb-2">
                        Stock actuel : <strong>{book.quantity} exemplaire{book.quantity > 1 ? 's' : ''}</strong>
                      </p>
                      <Row className="align-items-end g-2">
                        <Col xs="auto">
                          <Form.Label className="mb-1">Quantité à ajouter</Form.Label>
                          <Form.Control
                            type="number"
                            min="1"
                            max="999"
                            value={restockQty}
                            onChange={(e) => setRestockQty(Math.max(1, parseInt(e.target.value) || 1))}
                            style={{ width: '100px' }}
                          />
                        </Col>
                        <Col xs="auto">
                          <Button
                            variant="success"
                            onClick={handleRestock}
                            disabled={restocking}
                          >
                            <i className="bi bi-plus-circle me-2"></i>
                            {restocking ? 'En cours...' : 'Restocker'}
                          </Button>
                        </Col>
                      </Row>
                    </Card.Body>
                  </Card>
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

        {/* Modal de confirmation d'ajout au panier */}
        <Modal show={showAddToCartModal} onHide={() => setShowAddToCartModal(false)} centered>
          <Modal.Header closeButton>
            <Modal.Title>
              <i className="bi bi-cart-plus text-primary me-2"></i>
              Ajouter au panier
            </Modal.Title>
          </Modal.Header>
          <Modal.Body>
            <p>
              Vous êtes sur le point d'ajouter <strong>"{book?.title}"</strong> à votre panier.
            </p>
            <Alert variant="info" className="mb-0">
              <i className="bi bi-calendar-range me-2"></i>
              Durée d'emprunt : <strong>{loanDuration} jours</strong>
            </Alert>
          </Modal.Body>
          <Modal.Footer>
            <Button variant="secondary" onClick={() => setShowAddToCartModal(false)}>
              <i className="bi bi-x-circle me-1"></i>
              Annuler
            </Button>
            <Button variant="primary" onClick={confirmAddToCart}>
              <i className="bi bi-check-circle me-1"></i>
              Confirmer
            </Button>
          </Modal.Footer>
        </Modal>

        {/* Modal de confirmation d'alerte de stock */}
        <Modal show={showStockAlertModal} onHide={() => setShowStockAlertModal(false)} centered>
          <Modal.Header closeButton>
            <Modal.Title>
              <i className="bi bi-bell text-warning me-2"></i>
              Alerte de disponibilité
            </Modal.Title>
          </Modal.Header>
          <Modal.Body>
            <p>
              <strong>"{book?.title}"</strong> est actuellement en rupture de stock.
            </p>
            <Alert variant="info">
              <i className="bi bi-envelope me-2"></i>
              Vous recevrez un email à <strong>{user?.email}</strong> dès que ce livre sera de nouveau disponible.
            </Alert>
            <p className="text-muted mb-0">
              <small>Vous pouvez annuler cette alerte à tout moment depuis votre profil.</small>
            </p>
          </Modal.Body>
          <Modal.Footer>
            <Button variant="secondary" onClick={() => setShowStockAlertModal(false)}>
              <i className="bi bi-x-circle me-1"></i>
              Annuler
            </Button>
            <Button variant="warning" onClick={confirmStockAlert}>
              <i className="bi bi-bell me-1"></i>
              Activer l'alerte
            </Button>
          </Modal.Footer>
        </Modal>

        {/* Modal de succès */}
        <Modal
          show={showSuccessModal}
          onHide={() => setShowSuccessModal(false)}
          centered
          backdrop="static"
        >
          <Modal.Header closeButton className="bg-success text-white">
            <Modal.Title>
              <i className="bi bi-check-circle-fill me-2"></i>
              Succès
            </Modal.Title>
          </Modal.Header>
          <Modal.Body className="text-center py-4">
            <i className="bi bi-check-circle text-success" style={{ fontSize: '4rem' }}></i>
            <p className="mt-3 mb-0 fs-6">{successMessage}</p>
          </Modal.Body>
          <Modal.Footer>
            <Button variant="success" onClick={() => setShowSuccessModal(false)} className="w-100">
              OK
            </Button>
          </Modal.Footer>
        </Modal>
      </Container>
    </>
  );
};

export default BookDetail;

