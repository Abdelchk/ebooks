import React, { useState, useEffect } from 'react';
import { Container, Row, Col, Card, Button, Alert, Badge, Modal } from 'react-bootstrap';
import { useNavigate } from 'react-router-dom';
import { cartService } from '../services/cartService';
import { reservationService } from '../services/reservationService';
import Navigation from '../components/Navbar';
import Loader from '../components/Loader';
import LoanDurationSelector from '../components/LoanDurationSelector';
import './Cart.css';

const Cart = () => {
  const navigate = useNavigate();
  const [cartItems, setCartItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [validating, setValidating] = useState(false);

  // États pour les modals
  const [showRemoveModal, setShowRemoveModal] = useState(false);
  const [showClearModal, setShowClearModal] = useState(false);
  const [showValidateModal, setShowValidateModal] = useState(false);
  const [showSuccessModal, setShowSuccessModal] = useState(false);
  const [itemToRemove, setItemToRemove] = useState(null);
  const [successMessage, setSuccessMessage] = useState('');

  useEffect(() => {
    loadCart();
  }, []);

  const loadCart = async () => {
    try {
      const data = await cartService.getCart();
      setCartItems(data);
    } catch (err) {
      setError('Erreur lors du chargement du panier');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleRemove = async (cartItemId) => {
    setItemToRemove(cartItemId);
    setShowRemoveModal(true);
  };

  const confirmRemove = async () => {
    try {
      await cartService.removeFromCart(itemToRemove);
      setCartItems(cartItems.filter(item => item.id !== itemToRemove));
      setShowRemoveModal(false);
      setItemToRemove(null);
      setSuccessMessage('Article retiré du panier avec succès');
      setShowSuccessModal(true);
    } catch (err) {
      setError('Erreur lors de la suppression');
      setShowRemoveModal(false);
      console.error(err);
    }
  };

  const handleClearCart = () => {
    setShowClearModal(true);
  };

  const confirmClearCart = async () => {
    try {
      await cartService.clearCart();
      setCartItems([]);
      setShowClearModal(false);
      setSuccessMessage('Panier vidé avec succès');
      setShowSuccessModal(true);
    } catch (err) {
      setError('Erreur lors du vidage du panier');
      setShowClearModal(false);
      console.error(err);
    }
  };

  const handleUpdateDuration = async (cartItemId, newDuration) => {
    try {
      await cartService.updateDuration(cartItemId, newDuration);
      setCartItems(cartItems.map(item =>
        item.id === cartItemId ? { ...item, loanDuration: newDuration } : item
      ));
      setSuccessMessage('Durée d\'emprunt mise à jour');
      setShowSuccessModal(true);
    } catch (err) {
      setError('Erreur lors de la mise à jour');
      console.error(err);
    }
  };

  const handleValidateCart = () => {
    if (cartItems.length === 0) {
      setError('Votre panier est vide');
      return;
    }
    setShowValidateModal(true);
  };

  const confirmValidateCart = async () => {
    try {
      setValidating(true);
      setError('');
      setShowValidateModal(false);
      await reservationService.validateCart();
      setSuccessMessage('Réservations créées avec succès ! Vous avez 72h pour retirer vos livres.');
      setShowSuccessModal(true);
      setTimeout(() => {
        navigate('/reservations');
      }, 2000);
    } catch (err) {
      setError(err.response?.data?.message || 'Erreur lors de la validation du panier');
    } finally {
      setValidating(false);
    }
  };

  if (loading) {
    return (
      <>
        <Navigation />
        <Container className="mt-4">
          <Loader message="Chargement du panier..." />
        </Container>
      </>
    );
  }

  return (
    <>
      <Navigation />
      <Container className="mt-4 cart-container">
        <h2 className="mb-4">
          <i className="bi bi-cart3 me-2"></i>
          Mon Panier
          {cartItems.length > 0 && (
            <Badge bg="primary" className="ms-2">{cartItems.length}</Badge>
          )}
        </h2>

        {error && <Alert variant="danger" dismissible onClose={() => setError('')}>{error}</Alert>}

        {cartItems.length === 0 ? (
          <Alert variant="info">
            <i className="bi bi-info-circle me-2"></i>
            Votre panier est vide. 
            <Alert.Link href="/accueil"> Parcourir les livres</Alert.Link>
          </Alert>
        ) : (
          <>
            <Row>
              {cartItems.map((item) => (
                <Col md={12} key={item.id} className="mb-3">
                  <Card className="cart-item-card">
                    <Card.Body>
                      <Row>
                        <Col md={2}>
                          <img
                            src={item.book.coverImageUrl}
                            alt={item.book.title}
                            className="cart-item-img"
                          />
                        </Col>
                        <Col md={7}>
                          <h5>{item.book.title}</h5>
                          <p className="text-muted mb-2">
                            <i className="bi bi-person-fill me-1"></i>
                            {item.book.author}
                          </p>
                          <p className="text-muted mb-3">
                            <i className="bi bi-box-seam me-1"></i>
                            Stock disponible : {item.book.quantity}
                          </p>
                          
                          <div className="duration-section">
                            <label className="fw-bold mb-2">
                              <i className="bi bi-calendar-range me-1"></i>
                              Durée d'emprunt :
                            </label>
                            <LoanDurationSelector
                              value={item.loanDuration}
                              onChange={(duration) => handleUpdateDuration(item.id, duration)}
                            />
                          </div>
                        </Col>
                        <Col md={3} className="text-end">
                          <Button
                            variant="outline-danger"
                            size="sm"
                            onClick={() => handleRemove(item.id)}
                          >
                            <i className="bi bi-trash me-1"></i>
                            Retirer
                          </Button>
                        </Col>
                      </Row>
                    </Card.Body>
                  </Card>
                </Col>
              ))}
            </Row>

            <div className="cart-actions mt-4">
              <Row>
                <Col md={6}>
                  <Button
                    variant="outline-secondary"
                    onClick={handleClearCart}
                    className="w-100"
                  >
                    <i className="bi bi-trash me-2"></i>
                    Vider le panier
                  </Button>
                </Col>
                <Col md={6}>
                  <Button
                    variant="primary"
                    size="lg"
                    onClick={handleValidateCart}
                    disabled={validating}
                    className="w-100"
                  >
                    <i className="bi bi-check-circle me-2"></i>
                    {validating ? 'Validation en cours...' : `Valider le panier (${cartItems.length})`}
                  </Button>
                </Col>
              </Row>
              <Alert variant="warning" className="mt-3">
                <i className="bi bi-exclamation-triangle me-2"></i>
                En validant, vous créez des réservations. Vous aurez <strong>72 heures</strong> pour retirer vos livres.
              </Alert>
            </div>
          </>
        )}

        {/* Modal de confirmation de retrait d'article */}
        <Modal show={showRemoveModal} onHide={() => setShowRemoveModal(false)} centered>
          <Modal.Header closeButton>
            <Modal.Title>
              <i className="bi bi-exclamation-triangle text-warning me-2"></i>
              Confirmer la suppression
            </Modal.Title>
          </Modal.Header>
          <Modal.Body>
            Êtes-vous sûr de vouloir retirer cet article de votre panier ?
          </Modal.Body>
          <Modal.Footer>
            <Button variant="secondary" onClick={() => setShowRemoveModal(false)}>
              <i className="bi bi-x-circle me-1"></i>
              Annuler
            </Button>
            <Button variant="danger" onClick={confirmRemove}>
              <i className="bi bi-trash me-1"></i>
              Retirer
            </Button>
          </Modal.Footer>
        </Modal>

        {/* Modal de confirmation de vidage du panier */}
        <Modal show={showClearModal} onHide={() => setShowClearModal(false)} centered>
          <Modal.Header closeButton>
            <Modal.Title>
              <i className="bi bi-exclamation-triangle text-warning me-2"></i>
              Vider le panier
            </Modal.Title>
          </Modal.Header>
          <Modal.Body>
            <p className="mb-0">
              Êtes-vous sûr de vouloir vider tout votre panier ?
            </p>
            <p className="text-danger mb-0">
              <strong>Cette action est irréversible.</strong>
            </p>
          </Modal.Body>
          <Modal.Footer>
            <Button variant="secondary" onClick={() => setShowClearModal(false)}>
              <i className="bi bi-x-circle me-1"></i>
              Annuler
            </Button>
            <Button variant="danger" onClick={confirmClearCart}>
              <i className="bi bi-trash me-1"></i>
              Vider le panier
            </Button>
          </Modal.Footer>
        </Modal>

        {/* Modal de confirmation de validation du panier */}
        <Modal show={showValidateModal} onHide={() => setShowValidateModal(false)} centered>
          <Modal.Header closeButton>
            <Modal.Title>
              <i className="bi bi-check-circle text-primary me-2"></i>
              Valider le panier
            </Modal.Title>
          </Modal.Header>
          <Modal.Body>
            <p>
              Vous êtes sur le point de créer <strong>{cartItems.length}</strong> réservation(s).
            </p>
            <Alert variant="info" className="mb-0">
              <i className="bi bi-info-circle me-2"></i>
              Vous aurez <strong>72 heures</strong> pour retirer vos livres à la bibliothèque.
              Passé ce délai, les réservations seront automatiquement annulées.
            </Alert>
          </Modal.Body>
          <Modal.Footer>
            <Button variant="secondary" onClick={() => setShowValidateModal(false)}>
              <i className="bi bi-x-circle me-1"></i>
              Annuler
            </Button>
            <Button variant="primary" onClick={confirmValidateCart}>
              <i className="bi bi-check-circle me-1"></i>
              Confirmer
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
            <p className="mt-3 mb-0 fs-5">{successMessage}</p>
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

export default Cart;

