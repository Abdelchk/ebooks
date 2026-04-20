import React, { useState, useEffect } from 'react';
import { Container, Table, Button, Badge, Alert, Tabs, Tab, Card, Form } from 'react-bootstrap';
import { Link } from 'react-router-dom';
import Navigation from '../components/Navbar';
import librarianService from '../services/librarianService';
import ConfirmationModal from '../components/ConfirmationModal';
import { MODAL_TYPES, getModalConfig } from '../config/modalConfig';

const LibrarianDashboard = () => {
  const [pendingReservations, setPendingReservations] = useState([]);
  const [allReservations, setAllReservations] = useState([]);
  const [lowStockBooks, setLowStockBooks] = useState([]);
  const [message, setMessage] = useState({ type: '', text: '' });
  const [loading, setLoading] = useState(false);
  const [activeTab, setActiveTab] = useState('pending');

  // État modale
  const [currentModal, setCurrentModal] = useState(null);
  const [modalConfig, setModalConfig] = useState(null);
  const [pendingAction, setPendingAction] = useState(null); // { type, id }
  const [rejectReason, setRejectReason] = useState('');

  const showModal = (type, params = {}, action = null) => {
    setCurrentModal(type);
    setModalConfig(getModalConfig(type, params));
    setPendingAction(action);
    if (type === MODAL_TYPES.CONFIRM_REJECT_RESERVATION) setRejectReason('');
  };

  const hideModal = () => {
    setCurrentModal(null);
    setModalConfig(null);
    setPendingAction(null);
  };

  useEffect(() => {
    loadPendingReservations();
    loadLowStockBooks();
  }, []);

  const loadPendingReservations = async () => {
    try {
      const data = await librarianService.getPendingReservations();
      setPendingReservations(data);
    } catch (error) {
      console.error('Erreur:', error);
      if (error.response?.status === 403) {
        setMessage({ type: 'danger', text: 'Accès refusé. Vous devez être bibliothécaire.' });
      }
    }
  };

  const loadAllReservations = async (status = null) => {
    try {
      const data = await librarianService.getAllReservations(status);
      setAllReservations(data);
    } catch (error) {
      console.error('Erreur:', error);
    }
  };

  const loadLowStockBooks = async () => {
    try {
      const data = await librarianService.getAvailabilityAlerts();
      setLowStockBooks(data.alerts || []);
    } catch (error) {
      console.error('Erreur:', error);
    }
  };

  const handleValidate = (reservationId) => {
    showModal(MODAL_TYPES.CONFIRM_VALIDATE_RESERVATION, {}, { type: 'validate', id: reservationId });
  };

  const handleReject = (reservationId) => {
    showModal(MODAL_TYPES.CONFIRM_REJECT_RESERVATION, {}, { type: 'reject', id: reservationId });
  };

  const handleModalConfirm = async () => {
    if (!pendingAction) return;
    hideModal();
    setLoading(true);

    try {
      if (pendingAction.type === 'validate') {
        await librarianService.validateReservation(pendingAction.id);
        setMessage({ type: 'success', text: 'Réservation validée avec succès !' });
        loadPendingReservations();
        if (activeTab === 'all') loadAllReservations();

      } else if (pendingAction.type === 'reject') {
        await librarianService.rejectReservation(pendingAction.id, rejectReason);
        setMessage({ type: 'success', text: 'Réservation rejetée.' });
        loadPendingReservations();
        if (activeTab === 'all') loadAllReservations();
      }
    } catch (error) {
      setMessage({ type: 'danger', text: error.response?.data?.message || 'Erreur lors de l\'opération' });
    } finally {
      setLoading(false);
    }
  };

  const getStatusBadge = (status) => {
    const badges = {
      PENDING: <Badge bg="warning">En attente</Badge>,
      VALIDATED: <Badge bg="info">Validée</Badge>,
      CANCELLED: <Badge bg="danger">Annulée</Badge>,
      EXPIRED: <Badge bg="secondary">Expirée</Badge>,
      CONVERTED: <Badge bg="success">Convertie en emprunt</Badge>
    };
    return badges[status] || <Badge bg="secondary">{status}</Badge>;
  };

  const renderReservationTable = (reservations) => (
    <>
      <Table striped bordered hover responsive>
        <thead>
          <tr>
            <th>#</th>
            <th>Utilisateur</th>
            <th>Livre</th>
            <th>Date réservation</th>
            <th>Expiration</th>
            <th>Durée emprunt</th>
            <th>Statut</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {reservations.length === 0 ? (
            <tr>
              <td colSpan="8" className="text-center">Aucune réservation</td>
            </tr>
          ) : (
            reservations.map((reservation) => (
              <tr key={reservation.id}>
                <td>{reservation.id}</td>
                <td>{reservation.user?.firstname} {reservation.user?.lastname}</td>
                <td>
                  <Link to={`/book/${reservation.book?.id}`} className="fw-bold text-decoration-none">
                    {reservation.book?.title}
                  </Link>
                  <br />
                  <small className="text-muted">{reservation.book?.author}</small>
                </td>
                <td>{new Date(reservation.reservationDate).toLocaleString('fr-FR')}</td>
                <td>{new Date(reservation.expirationDate).toLocaleString('fr-FR')}</td>
                <td>{reservation.loanDuration} jours</td>
                <td>{getStatusBadge(reservation.status)}</td>
                <td>
                  {reservation.status === 'PENDING' && (
                    <>
                      <Button
                        size="sm"
                        variant="success"
                        className="me-2"
                        onClick={() => handleValidate(reservation.id)}
                        disabled={loading}
                      >
                        <i className="bi bi-check-circle"></i> Valider
                      </Button>
                      <Button
                        size="sm"
                        variant="danger"
                        onClick={() => handleReject(reservation.id)}
                        disabled={loading}
                      >
                        <i className="bi bi-x-circle"></i> Rejeter
                      </Button>
                    </>
                  )}
                  {reservation.status === 'VALIDATED' && (
                    <Badge bg="info">Validée - En attente de retrait</Badge>
                  )}
                </td>
              </tr>
            ))
          )}
        </tbody>
      </Table>

      {/* Modale de confirmation */}
      <ConfirmationModal
        show={currentModal !== null}
        onHide={hideModal}
        onConfirm={handleModalConfirm}
        config={
          currentModal === MODAL_TYPES.CONFIRM_REJECT_RESERVATION && modalConfig
            ? {
                ...modalConfig,
                // Injecter le champ raison dans le message via alert
                alert: {
                  variant: 'light',
                  text: (
                    <Form.Group>
                      <Form.Label className="fw-semibold">Raison du rejet (optionnelle) :</Form.Label>
                      <Form.Control
                        as="textarea"
                        rows={2}
                        value={rejectReason}
                        onChange={(e) => setRejectReason(e.target.value)}
                        placeholder="Saisir une raison..."
                      />
                    </Form.Group>
                  ),
                },
              }
            : modalConfig
        }
      />
    </>
  );

  return (
    <>
      <Navigation />
      <Container className="mt-5">
        <h2>
          <i className="bi bi-journal-check"></i> Tableau de bord Bibliothécaire
        </h2>

        {message.text && (
          <Alert variant={message.type} dismissible onClose={() => setMessage({ type: '', text: '' })}>
            {message.text}
          </Alert>
        )}

        {/* Alertes de stock */}
        {lowStockBooks.length > 0 && (
          <Alert variant="warning">
            <Alert.Heading>
              <i className="bi bi-exclamation-triangle"></i> Alertes de stock faible
            </Alert.Heading>
            <ul className="mb-0">
              {lowStockBooks.map((book) => (
                <li key={book.id}>
                  <Link to={`/book/${book.id}`} className="fw-bold text-decoration-none text-dark">
                    {book.title}
                  </Link>
                  {' '}- Stock: {book.quantity} exemplaire(s)
                  <Badge bg="success" className="ms-2" style={{ cursor: 'pointer' }}>
                    <i className="bi bi-box-seam me-1"></i>Restocker
                  </Badge>
                </li>
              ))}
            </ul>
          </Alert>
        )}

        <Tabs
          activeKey={activeTab}
          onSelect={(k) => {
            setActiveTab(k);
            if (k === 'all') loadAllReservations();
          }}
          className="mb-3"
        >
          <Tab eventKey="pending" title={`En attente (${pendingReservations.length})`}>
            <Card>
              <Card.Header className="bg-warning text-dark">
                <i className="bi bi-clock-history"></i> Réservations en attente de validation
              </Card.Header>
              <Card.Body>
                {renderReservationTable(pendingReservations)}
              </Card.Body>
            </Card>
          </Tab>

          <Tab eventKey="all" title="Toutes les réservations">
            <Card>
              <Card.Header>
                <i className="bi bi-list"></i> Toutes les réservations
                <div className="float-end">
                  <Button size="sm" variant="outline-secondary" onClick={() => loadAllReservations()}>
                    Toutes
                  </Button>{' '}
                  <Button size="sm" variant="outline-warning" onClick={() => loadAllReservations('PENDING')}>
                    En attente
                  </Button>{' '}
                  <Button size="sm" variant="outline-info" onClick={() => loadAllReservations('VALIDATED')}>
                    Validées
                  </Button>{' '}
                  <Button size="sm" variant="outline-success" onClick={() => loadAllReservations('CONVERTED')}>
                    Converties
                  </Button>
                </div>
              </Card.Header>
              <Card.Body>
                {renderReservationTable(allReservations)}
              </Card.Body>
            </Card>
          </Tab>
        </Tabs>
      </Container>
    </>
  );
};

export default LibrarianDashboard;

