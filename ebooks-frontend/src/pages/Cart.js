import React, { useState, useEffect } from 'react';
import { Container, Row, Col, Card, Button, Alert, Badge } from 'react-bootstrap';
import { useNavigate } from 'react-router-dom';
import { cartService } from '../services/cartService';
import { reservationService } from '../services/reservationService';
import Navigation from '../components/Navbar';
import Loader from '../components/Loader';
import LoanDurationSelector from '../components/LoanDurationSelector';
import ConfirmationModal from '../components/ConfirmationModal';
import { MODAL_TYPES, getModalConfig } from '../config/modalConfig';
import './Cart.css';

const Cart = () => {
  const navigate = useNavigate();
  const [cartItems, setCartItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [validating, setValidating] = useState(false);

  // États pour les modals (simplifié)
  const [currentModal, setCurrentModal] = useState(null);
  const [modalConfig, setModalConfig] = useState(null);
  const [itemToRemove, setItemToRemove] = useState(null);

  useEffect(() => {
    loadCart().catch(err => {
      console.error('Erreur lors du chargement initial du panier:', err);
    });
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

  const showModal = (type, params = {}) => {
    setCurrentModal(type);
    setModalConfig(getModalConfig(type, params));
  };

  const hideModal = () => {
    setCurrentModal(null);
    setModalConfig(null);
  };

  const handleRemove = async (cartItemId) => {
    setItemToRemove(cartItemId);
    showModal(MODAL_TYPES.CONFIRM_REMOVE_ITEM);
  };

  const confirmRemove = async () => {
    try {
      await cartService.removeFromCart(itemToRemove);
      setCartItems(cartItems.filter(item => item.id !== itemToRemove));
      hideModal();
      setItemToRemove(null);
      showModal(MODAL_TYPES.SUCCESS_GENERIC, {
        message: 'Article retiré du panier avec succès'
      });
    } catch (err) {
      setError('Erreur lors de la suppression');
      hideModal();
      console.error(err);
    }
  };

  const handleClearCart = () => {
    showModal(MODAL_TYPES.CONFIRM_CLEAR_CART);
  };

  const confirmClearCart = async () => {
    try {
      await cartService.clearCart();
      setCartItems([]);
      hideModal();
      showModal(MODAL_TYPES.SUCCESS_GENERIC, {
        message: 'Panier vidé avec succès'
      });
    } catch (err) {
      setError('Erreur lors du vidage du panier');
      hideModal();
      console.error(err);
    }
  };

  const handleUpdateDuration = async (cartItemId, newDuration) => {
    try {
      await cartService.updateDuration(cartItemId, newDuration);
      setCartItems(cartItems.map(item =>
        item.id === cartItemId ? { ...item, loanDuration: newDuration } : item
      ));
      showModal(MODAL_TYPES.SUCCESS_GENERIC, {
        message: 'Durée d\'emprunt mise à jour'
      });
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
    showModal(MODAL_TYPES.CONFIRM_VALIDATE_CART, { count: cartItems.length });
  };

  const confirmValidateCart = async () => {
    try {
      setValidating(true);
      setError('');
      hideModal();
      await reservationService.validateCart();
      showModal(MODAL_TYPES.SUCCESS_CART_VALIDATED);
      setTimeout(() => {
        navigate('/reservations');
      }, 2000);
    } catch (err) {
      setError(err.response?.data?.message || 'Erreur lors de la validation du panier');
    } finally {
      setValidating(false);
    }
  };

  const handleModalConfirm = () => {
    switch (currentModal) {
      case MODAL_TYPES.CONFIRM_REMOVE_ITEM:
        confirmRemove();
        break;
      case MODAL_TYPES.CONFIRM_CLEAR_CART:
        confirmClearCart();
        break;
      case MODAL_TYPES.CONFIRM_VALIDATE_CART:
        confirmValidateCart();
        break;
      case MODAL_TYPES.SUCCESS_GENERIC:
      case MODAL_TYPES.SUCCESS_CART_VALIDATED:
        hideModal();
        break;
      default:
        hideModal();
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

        {/* Modal générique */}
        <ConfirmationModal
          show={currentModal !== null}
          onHide={hideModal}
          onConfirm={handleModalConfirm}
          config={modalConfig}
        />
      </Container>
    </>
  );
};

export default Cart;

