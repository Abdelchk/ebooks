import React, { useState, useEffect } from 'react';
import { Container, Row, Col, Card, Button, Alert, Badge, ButtonGroup } from 'react-bootstrap';
import { reservationService } from '../services/reservationService';
import Navigation from '../components/Navbar';
import Loader from '../components/Loader';
import './Reservations.css';

const Reservations = () => {
  const [reservations, setReservations] = useState([]);
  const [filter, setFilter] = useState('all');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    loadReservations();
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
    if (!window.confirm('Annuler cette réservation ?')) {
      return;
    }

    try {
      await reservationService.cancelReservation(reservationId);
      loadReservations();
      alert('Réservation annulée avec succès');
    } catch (err) {
      alert(err.response?.data?.message || 'Erreur lors de l\'annulation');
    }
  };

  const getStatusBadge = (status) => {
    const badges = {
      PENDING: { bg: 'primary', text: '📌 En attente' },
      CANCELLED: { bg: 'secondary', text: '❌ Annulée' },
      EXPIRED: { bg: 'warning', text: '⏰ Expirée' },
      CONVERTED: { bg: 'success', text: '✓ Convertie en emprunt' }
    };
    return badges[status] || { bg: 'secondary', text: status };
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
    if (filter === 'pending') return res.status === 'PENDING';
    if (filter === 'cancelled') return res.status === 'CANCELLED';
    if (filter === 'expired') return res.status === 'EXPIRED';
    if (filter === 'converted') return res.status === 'CONVERTED';
    return true;
  });

  if (loading) {
    return (
      <>
        <Navigation />
        <Container className="mt-4">
          <Loader message="Chargement des réservations..." />
        </Container>
      </>
    );
  }

  return (
    <>
      <Navigation />
      <Container className="mt-4 reservations-container">
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

        {filteredReservations.length === 0 ? (
          <Alert variant="info">
            <i className="bi bi-info-circle me-2"></i>
            Aucune réservation {filter !== 'all' && `avec le statut "${filter}"`}.
          </Alert>
        ) : (
          <Row>
            {filteredReservations.map((reservation) => (
              <Col md={6} lg={4} key={reservation.id} className="mb-4">
                <Card className="reservation-card h-100">
                  <div className="status-ribbon">
                    <Badge bg={getStatusBadge(reservation.status).bg}>
                      {getStatusBadge(reservation.status).text}
                    </Badge>
                  </div>
                  
                  <Card.Img
                    variant="top"
                    src={reservation.book.coverImageUrl}
                    alt={reservation.book.title}
                    className="reservation-img"
                  />
                  
                  <Card.Body>
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
            ))}
          </Row>
        )}
      </Container>
    </>
  );
};

export default Reservations;

