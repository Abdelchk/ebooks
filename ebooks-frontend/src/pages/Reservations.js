import React, { useState, useEffect } from 'react';
import { Container, Row, Col, Card, Button, Alert, Badge, ButtonGroup } from 'react-bootstrap';
import { reservationService } from '../services/reservationService';
import Navigation from '../components/Navbar';
import Loader from '../components/Loader';
import ConfirmationModal from '../components/ConfirmationModal';
import { MODAL_TYPES, getModalConfig } from '../config/modalConfig';
import './Reservations.css';

const Reservations = ({ embedded = false }) => {
  const [reservations, setReservations] = useState([]);
  const [filter, setFilter] = useState('all');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [currentModal, setCurrentModal] = useState(null);
  const [modalConfig, setModalConfig] = useState(null);
  const [reservationToCancel, setReservationToCancel] = useState(null);

  const showModal = (type, params = {}) => {
    setCurrentModal(type);
    setModalConfig(getModalConfig(type, params));
  };

  const hideModal = () => {
    setCurrentModal(null);
    setModalConfig(null);
  };

  useEffect(() => {
    loadReservations().catch(err => {
      console.error('Erreur lors du chargement initial des réservations:', err);
    });
  }, []);

  const loadReservations = async () => {
    try {
      const data = await reservationService.getUserReservations();
      setReservations(data);
    } catch (err) {
      setError('Erreur lors du chargement des réservations');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleCancel = async (reservationId) => {
    setReservationToCancel(reservationId);
    showModal(MODAL_TYPES.CONFIRM_CANCEL_RESERVATION);
  };

  const confirmCancel = async () => {
    try {
      await reservationService.cancelReservation(reservationToCancel);
      await loadReservations();
      hideModal();
      setReservationToCancel(null);
      showModal(MODAL_TYPES.SUCCESS_GENERIC, {
        message: 'Réservation annulée avec succès'
      });
    } catch (err) {
      setError(err.response?.data?.message || 'Erreur lors de l\'annulation de la réservation');
      hideModal();
    }
  };

  const handleModalConfirm = () => {
    switch (currentModal) {
      case MODAL_TYPES.CONFIRM_CANCEL_RESERVATION:
        confirmCancel();
        break;
      case MODAL_TYPES.SUCCESS_GENERIC:
        hideModal();
        break;
      default:
        hideModal();
    }
  };

  const getStatusBadge = (status) => {
    const badges = {
      PENDING:   { bg: 'warning',   text: 'En attente', icon: 'clock-history' },
      VALIDATED: { bg: 'info',      text: 'Prête à retirer', icon: 'bag-check' },
      CANCELLED: { bg: 'secondary', text: 'Annulée', icon: 'x-circle' },
      EXPIRED:   { bg: 'danger',    text: 'Expirée', icon: 'alarm' },
      CONVERTED: { bg: 'success',   text: 'Emprunt en cours', icon: 'book' }
    };
    return badges[status] || { bg: 'secondary', text: status, icon: 'info-circle' };
  };

  const getTimeRemaining = (expirationDate) => {
    const now = new Date();
    const expiration = new Date(expirationDate);
    const diff = expiration - now;

    if (diff <= 0) {
      return 'Expiré';
    }

    const days = Math.floor(diff / (1000 * 60 * 60 * 24));
    const hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));

    if (days > 0) {
      return `${days}j ${hours}h`;
    }
    return `${hours}h`;
  };

  const filteredReservations = reservations.filter(res => {
    if (filter === 'all') return true;
    if (filter === 'pending') return res.status === 'PENDING' || res.status === 'VALIDATED';
    if (filter === 'cancelled') return res.status === 'CANCELLED';
    if (filter === 'expired') return res.status === 'EXPIRED';
    if (filter === 'converted') return res.status === 'CONVERTED';
    return true;
  });

  const sortedFilteredReservations = [...filteredReservations].sort((a, b) => {
    const firstDate = new Date(a.reservationDate || a.createdAt || 0).getTime();
    const secondDate = new Date(b.reservationDate || b.createdAt || 0).getTime();
    return secondDate - firstDate;
  });

  if (loading) {
    const content = <Loader message="Chargement des réservations..." />;
    return embedded ? content : (
      <>
        <Navigation />
        <Container className="mt-4">
          {content}
        </Container>
      </>
    );
  }

  const mainContent = (
    <Container className={embedded ? "p-0" : "mt-4"} fluid={embedded}>
      <h2 className="mb-4">
        <i className="bi bi-bookmark-fill me-2"></i>
        Mes Réservations
        {reservations.length > 0 && (
          <Badge bg="primary" className="ms-2">{reservations.length}</Badge>
        )}
      </h2>

      {error && <Alert variant="danger" dismissible onClose={() => setError('')}>{error}</Alert>}

      {/* Filtres */}
      <ButtonGroup className="mb-4 filter-buttons">
        <Button
          variant={filter === 'all' ? 'primary' : 'outline-primary'}
          onClick={() => setFilter('all')}
        >
          Toutes ({reservations.length})
        </Button>
        <Button
          variant={filter === 'pending' ? 'primary' : 'outline-primary'}
          onClick={() => setFilter('pending')}
        >
          En attente ({reservations.filter(r => r.status === 'PENDING').length})
        </Button>
        <Button
          variant={filter === 'converted' ? 'success' : 'outline-success'}
          onClick={() => setFilter('converted')}
        >
          Converties ({reservations.filter(r => r.status === 'CONVERTED').length})
        </Button>
        <Button
          variant={filter === 'cancelled' ? 'secondary' : 'outline-secondary'}
          onClick={() => setFilter('cancelled')}
        >
          Annulées ({reservations.filter(r => r.status === 'CANCELLED').length})
        </Button>
      </ButtonGroup>

      {sortedFilteredReservations.length === 0 ? (
        <Alert variant="info">
          <i className="bi bi-info-circle me-2"></i>
          Aucune réservation {filter !== 'all' && `avec le statut "${filter}"`}.
        </Alert>
      ) : (
        <Row>
          {sortedFilteredReservations.map((reservation) => {
            const badgeInfo = getStatusBadge(reservation.status);

            return (
            <Col md={6} lg={4} key={reservation.id} className="mb-4">
              <Card className="reservation-card h-100">
                <Card.Img
                  variant="top"
                  src={reservation.book.coverImageUrl}
                  alt={reservation.book.title}
                  className="reservation-img"
                />

                <Card.Body>
                  {/* Badge statut en haut du corps — plus de positionnement absolu */}
                  <div className="mb-2">
                    <Badge bg={badgeInfo.bg} className="status-badge">
                      <i className={`bi bi-${badgeInfo.icon} me-1`}></i>
                      {badgeInfo.text}
                    </Badge>
                  </div>

                  <Card.Title>{reservation.book.title}</Card.Title>
                  <Card.Text className="text-muted">
                    <i className="bi bi-person-fill me-1"></i>
                    {reservation.book.author}
                  </Card.Text>

                  <hr />

                  <div className="reservation-info">
                    <p className="mb-2">
                      <i className="bi bi-calendar-check me-2"></i>
                      <strong>Réservé le :</strong><br />
                      <small>{new Date(reservation.reservationDate).toLocaleString('fr-FR')}</small>
                    </p>

                    {reservation.status === 'PENDING' && (
                      <>
                        <p className="mb-2">
                          <i className="bi bi-alarm me-2"></i>
                          <strong>À retirer avant :</strong><br />
                          <small>{new Date(reservation.expirationDate).toLocaleString('fr-FR')}</small>
                        </p>
                        <Alert variant="warning" className="mb-2 py-2">
                          <small>
                            <i className="bi bi-hourglass-split me-1"></i>
                            Temps restant : <strong>{getTimeRemaining(reservation.expirationDate)}</strong>
                          </small>
                        </Alert>
                      </>
                    )}

                    <p className="mb-2">
                      <i className="bi bi-clock me-2"></i>
                      <strong>Durée prévue :</strong> {reservation.loanDuration} jours
                    </p>
                  </div>

                  {reservation.status === 'PENDING' && (
                    <Button
                      variant="danger"
                      className="w-100 mt-3"
                      onClick={() => handleCancel(reservation.id)}
                    >
                      <i className="bi bi-x-circle me-2"></i>
                      Annuler la réservation
                    </Button>
                  )}

                  {reservation.status === 'CANCELLED' && reservation.cancelledAt && (
                    <small className="text-muted">
                      Annulée le {new Date(reservation.cancelledAt).toLocaleDateString('fr-FR')}
                    </small>
                  )}

                  {reservation.status === 'CONVERTED' && reservation.convertedToLoanAt && (
                    <Alert variant="success" className="mb-0 mt-2 py-2">
                      <small>
                        <i className="bi bi-check-circle me-1"></i>
                        Convertie en emprunt le {new Date(reservation.convertedToLoanAt).toLocaleDateString('fr-FR')}
                      </small>
                    </Alert>
                  )}
                </Card.Body>
              </Card>
            </Col>
            );
          })}
        </Row>
      )}

      {/* Modal générique */}
      <ConfirmationModal
          show={currentModal !== null}
          onHide={hideModal}
          onConfirm={handleModalConfirm}
          config={modalConfig}
      />
    </Container>
  );

  return embedded ? mainContent : (
    <>
      <Navigation />
      {mainContent}
    </>
  );
};

export default Reservations;

