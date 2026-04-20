import React, { useState, useEffect } from 'react';
import { Container, Row, Col, Card, Button, Alert, Badge, ButtonGroup, ProgressBar } from 'react-bootstrap';
import { loanService } from '../services/loanService';
import Navigation from '../components/Navbar';
import Loader from '../components/Loader';
import ConfirmationModal from '../components/ConfirmationModal';
import { MODAL_TYPES, getModalConfig } from '../config/modalConfig';
import './Loans.css';

const Loans = ({ embedded = false }) => {
  const [loans, setLoans] = useState([]);
  const [filter, setFilter] = useState('active');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  // État modales
  const [currentModal, setCurrentModal] = useState(null);
  const [modalConfig, setModalConfig] = useState(null);
  const [pendingLoanId, setPendingLoanId] = useState(null);
  const [pendingIsOverdue, setPendingIsOverdue] = useState(false);

  const showModal = (type, loanId = null, isOverdue = false) => {
    setCurrentModal(type);
    setModalConfig(getModalConfig(type));
    setPendingLoanId(loanId);
    setPendingIsOverdue(isOverdue);
  };

  const hideModal = () => {
    setCurrentModal(null);
    setModalConfig(null);
    setPendingLoanId(null);
    setPendingIsOverdue(false);
  };

  useEffect(() => {
    loadLoans().catch(err => {
      console.error('Erreur lors du chargement initial des emprunts:', err);
    });
  }, []);

  const loadLoans = async () => {
    try {
      const data = await loanService.getUserLoans();
      setLoans(data);
    } catch (err) {
      setError('Erreur lors du chargement des emprunts');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleExtend = (loanId) => {
    showModal(MODAL_TYPES.CONFIRM_EXTEND_LOAN, loanId);
  };

  const handleReturn = (loanId, isOverdue) => {
    const type = isOverdue ? MODAL_TYPES.CONFIRM_RETURN_LOAN_OVERDUE : MODAL_TYPES.CONFIRM_RETURN_LOAN;
    showModal(type, loanId, isOverdue);
  };

  const handleModalConfirm = async () => {
    hideModal();
    try {
      if (currentModal === MODAL_TYPES.CONFIRM_EXTEND_LOAN) {
        await loanService.extendLoan(pendingLoanId);
        setError('');
        showModal(MODAL_TYPES.SUCCESS_GENERIC);
        setModalConfig(getModalConfig(MODAL_TYPES.SUCCESS_GENERIC, { message: 'Emprunt prolongé de 7 jours avec succès !' }));
      } else if (
        currentModal === MODAL_TYPES.CONFIRM_RETURN_LOAN ||
        currentModal === MODAL_TYPES.CONFIRM_RETURN_LOAN_OVERDUE
      ) {
        await loanService.returnLoan(pendingLoanId);
        setError('');
        showModal(MODAL_TYPES.SUCCESS_GENERIC);
        setModalConfig(
          getModalConfig(MODAL_TYPES.SUCCESS_GENERIC, {
            message: pendingIsOverdue
              ? 'Retour enregistré avec succès (retard pris en compte).'
              : 'Retour enregistré avec succès !'
          })
        );
      } else if (currentModal === MODAL_TYPES.SUCCESS_GENERIC) {
        hideModal();
        await loadLoans();
        return;
      }
      await loadLoans();
    } catch (err) {
      setError(err.response?.data?.message || 'Erreur lors de l\'opération');
    }
  };

  const getStatusBadge = (loan) => {
    if (loan.status === 'RETURNED') {
      return { bg: 'secondary', text: 'Rendu', icon: 'check-circle' };
    }
    if (loan.status === 'OVERDUE') {
      return { bg: 'danger', text: 'En retard', icon: 'exclamation-triangle' };
    }
    if (loan.status === 'EXTENDED') {
      return { bg: 'info', text: 'Prolongé', icon: 'arrow-repeat' };
    }
    return { bg: 'success', text: 'Emprunt en cours', icon: 'book' };
  };

  const getDaysRemaining = (dueDate, returnDate) => {
    if (returnDate) {
      return null;
    }

    const now = new Date();
    const due = new Date(dueDate);
    const diff = due - now;
    return Math.ceil(diff / (1000 * 60 * 60 * 24));
  };

  const getDaysOverdue = (dueDate, returnDate) => {
    if (returnDate) {
      return 0;
    }

    const now = new Date();
    const due = new Date(dueDate);
    
    if (now <= due) {
      return 0;
    }

    const diff = now - due;
    return Math.floor(diff / (1000 * 60 * 60 * 24));
  };

  const getProgressBarVariant = (daysRemaining) => {
    if (daysRemaining < 0) return 'danger';
    if (daysRemaining <= 3) return 'warning';
    return 'success';
  };

  const getProgressPercentage = (loanDate, dueDate, returnDate) => {
    const start = new Date(loanDate);
    const end = new Date(dueDate);
    const now = returnDate ? new Date(returnDate) : new Date();

    const total = end - start;
    const elapsed = now - start;

    return Math.min(100, Math.max(0, (elapsed / total) * 100));
  };

  const filteredLoans = loans.filter(loan => {
    if (filter === 'all') return true;
    if (filter === 'active') return loan.status === 'ACTIVE' || loan.status === 'EXTENDED';
    if (filter === 'returned') return loan.status === 'RETURNED';
    if (filter === 'overdue') return loan.status === 'OVERDUE';
    return true;
  });

  const sortedFilteredLoans = [...filteredLoans].sort((a, b) => {
    const firstDate = new Date(a.loanDate || a.createdAt || 0).getTime();
    const secondDate = new Date(b.loanDate || b.createdAt || 0).getTime();
    return secondDate - firstDate;
  });

  if (loading) {
    const content = <Loader message="Chargement des emprunts..." />;
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
        <i className="bi bi-book me-2"></i>
        Mes Emprunts
        {loans.length > 0 && (
          <Badge bg="primary" className="ms-2">{loans.length}</Badge>
        )}
      </h2>

      {error && <Alert variant="danger" dismissible onClose={() => setError('')}>{error}</Alert>}

      {/* Filtres */}
      <ButtonGroup className="mb-4 filter-buttons">
        <Button
          variant={filter === 'all' ? 'primary' : 'outline-primary'}
          onClick={() => setFilter('all')}
        >
          Tous ({loans.length})
        </Button>
        <Button
          variant={filter === 'active' ? 'success' : 'outline-success'}
          onClick={() => setFilter('active')}
        >
          Actifs ({loans.filter(l => l.status === 'ACTIVE' || l.status === 'EXTENDED').length})
        </Button>
        <Button
          variant={filter === 'overdue' ? 'danger' : 'outline-danger'}
          onClick={() => setFilter('overdue')}
        >
          En retard ({loans.filter(l => l.status === 'OVERDUE').length})
        </Button>
        <Button
          variant={filter === 'returned' ? 'secondary' : 'outline-secondary'}
          onClick={() => setFilter('returned')}
        >
          Rendus ({loans.filter(l => l.status === 'RETURNED').length})
        </Button>
      </ButtonGroup>

      {sortedFilteredLoans.length === 0 ? (
        <Alert variant="info">
          <i className="bi bi-info-circle me-2"></i>
          Aucun emprunt {filter !== 'all' && `avec le statut "${filter}"`}.
        </Alert>
      ) : (
        <Row>
          {sortedFilteredLoans.map((loan) => {
            const daysRemaining = getDaysRemaining(loan.dueDate, loan.returnDate);
            const daysOverdue = getDaysOverdue(loan.dueDate, loan.returnDate);
            const badgeInfo = getStatusBadge(loan);
            const progress = getProgressPercentage(loan.loanDate, loan.dueDate, loan.returnDate);

            return (
              <Col md={6} lg={4} key={loan.id} className="mb-4">
                <Card className={`loan-card h-100 ${loan.status === 'OVERDUE' ? 'overdue-card' : ''}`}>
                  <Card.Img
                    variant="top"
                    src={loan.book.coverImageUrl}
                    alt={loan.book.title}
                    className="loan-img"
                  />

                  <Card.Body className="d-flex flex-column">
                    <div className="mb-2">
                      <Badge bg={badgeInfo.bg} className="status-badge">
                        <i className={`bi bi-${badgeInfo.icon} me-1`}></i>
                        {badgeInfo.text}
                      </Badge>
                    </div>

                    <Card.Title>{loan.book.title}</Card.Title>
                    <Card.Text className="text-muted mb-3">
                      <i className="bi bi-person-fill me-1"></i>
                      {loan.book.author}
                    </Card.Text>

                    <div className="loan-info flex-grow-1">
                      <p className="mb-2">
                        <i className="bi bi-calendar-check me-2"></i>
                        <strong>Emprunté le :</strong><br />
                        <small>{new Date(loan.loanDate).toLocaleDateString('fr-FR')}</small>
                      </p>

                      <p className="mb-2">
                        <i className="bi bi-calendar-x me-2"></i>
                        <strong>Retour prévu :</strong><br />
                        <small>{new Date(loan.dueDate).toLocaleDateString('fr-FR')}</small>
                      </p>

                      {loan.returnDate && (
                        <p className="mb-2">
                          <i className="bi bi-check-circle me-2 text-success"></i>
                          <strong>Rendu le :</strong><br />
                          <small>{new Date(loan.returnDate).toLocaleDateString('fr-FR')}</small>
                        </p>
                      )}

                      {!loan.returnDate && (
                        <>
                          {daysOverdue > 0 ? (
                            <Alert variant="danger" className="mb-2 py-2">
                              <strong>
                                <i className="bi bi-exclamation-triangle me-1"></i>
                                Retard : {daysOverdue} jour(s)
                              </strong>
                            </Alert>
                          ) : (
                            <>
                              <div className="mb-2">
                                <small className="d-block mb-1">
                                  <i className="bi bi-hourglass-split me-1"></i>
                                  Temps restant : <strong>{daysRemaining} jour(s)</strong>
                                </small>
                                <ProgressBar
                                  now={progress}
                                  variant={getProgressBarVariant(daysRemaining)}
                                  className="progress-custom"
                                />
                              </div>

                              {daysRemaining <= 3 && (
                                <Alert variant="warning" className="mb-2 py-2">
                                  <small>
                                    <i className="bi bi-alarm me-1"></i>
                                    Échéance proche !
                                  </small>
                                </Alert>
                              )}
                            </>
                          )}
                        </>
                      )}

                      <p className="mb-2">
                        <i className="bi bi-arrow-repeat me-2"></i>
                        <strong>Prolongations :</strong> {loan.extensionCount}/2
                      </p>
                    </div>

                    {!loan.returnDate && (
                      <div className="loan-actions mt-3">
                        {loan.extensionCount < 2 && daysOverdue === 0 && (
                          <Button
                            variant="outline-primary"
                            size="sm"
                            onClick={() => handleExtend(loan.id)}
                            className="w-100 mb-2"
                          >
                            <i className="bi bi-plus-circle me-2"></i>
                            Prolonger (+7 jours)
                          </Button>
                        )}

                        <Button
                          variant={daysOverdue > 0 ? 'danger' : 'success'}
                          size="sm"
                          onClick={() => handleReturn(loan.id, daysOverdue > 0)}
                          className="w-100"
                        >
                          <i className="bi bi-check-circle me-2"></i>
                          {daysOverdue > 0 ? 'Retourner (en retard)' : 'Retourner le livre'}
                        </Button>
                      </div>
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

export default Loans;

