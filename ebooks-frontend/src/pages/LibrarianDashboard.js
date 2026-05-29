import React, { useState, useEffect } from 'react';
import { Container, Table, Button, Badge, Alert, Tabs, Tab, Card, Form, Modal, Row, Col, Image } from 'react-bootstrap';
import { Link } from 'react-router-dom';
import Navigation from '../components/Navbar';
import librarianService from '../services/librarianService';
import { bookService } from '../services/bookService';
import BookCoverUpload from '../components/BookCoverUpload';
import ConfirmationModal from '../components/ConfirmationModal';
import { MODAL_TYPES, getModalConfig } from '../config/modalConfig';
import { toArray } from '../utils/arrayUtils';

// Convertit une date reçue de l'API (dd/MM/yyyy ou yyyy-MM-dd) en yyyy-MM-dd pour l'input date
const toInputDate = (dateStr) => {
  if (!dateStr) return new Date().toISOString().split('T')[0];
  // Format dd/MM/yyyy (ancien format global Jackson)
  if (/^\d{2}\/\d{2}\/\d{4}$/.test(dateStr)) {
    const [day, month, year] = dateStr.split('/');
    return `${year}-${month}-${day}`;
  }
  // Format yyyy-MM-dd ou ISO (nouveau format via @JsonFormat)
  if (/^\d{4}-\d{2}-\d{2}/.test(dateStr)) {
    return dateStr.split('T')[0];
  }
  // Fallback sécurisé
  try {
    const d = new Date(dateStr);
    if (!Number.isNaN(d.getTime())) return d.toISOString().split('T')[0];
  } catch (e) {
    console.warn('Date invalide, utilisation de la date du jour.', e);
  }
  return new Date().toISOString().split('T')[0];
};

// Catégories disponibles (synchronisées avec Accueil.js)
const CATEGORIES = [
  'Biographie', 'Education', 'Essai', 'Fantasy', 'Fiction littéraire',
  'Histoire', 'Jeunesse', 'Philosophie', 'Polar', 'Roman', 'Romance',
  'Science-Fiction', 'Thriller'
];

const EMPTY_BOOK_FORM = {
  title: '', description: '', author: '', category: '',
  coverImageUrl: '', quantity: 1, isPublished: true,
  publicationDate: new Date().toISOString().split('T')[0],
};

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
  const [pendingAction, setPendingAction] = useState(null);
  const [rejectReason, setRejectReason] = useState('');

  // ── Gestion des livres ──────────────────────────────────────────────────────
  const [books, setBooks] = useState([]);
  const [booksLoading, setBooksLoading] = useState(false);
  const [showBookModal, setShowBookModal] = useState(false);
  const [editingBook, setEditingBook] = useState(null); // null = création
  const [bookForm, setBookForm] = useState(EMPTY_BOOK_FORM);
  const [bookSaving, setBookSaving] = useState(false);
  const [bookMessage, setBookMessage] = useState(null);
  const [bookToDelete, setBookToDelete] = useState(null);
  const [showDeleteBookConfirm, setShowDeleteBookConfirm] = useState(false);

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
      setPendingReservations(toArray(data));
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
      setAllReservations(toArray(data));
    } catch (error) {
      console.error('Erreur:', error);
    }
  };

  const loadLowStockBooks = async () => {
    try {
      const data = await librarianService.getAvailabilityAlerts();
      setLowStockBooks(toArray(data?.alerts ?? data));
    } catch (error) {
      console.error('Erreur:', error);
    }
  };

  // ── Chargement des livres ───────────────────────────────────────────────────
  const loadBooks = async () => {
    setBooksLoading(true);
    try {
      const data = await bookService.getAllBooks();
      setBooks(toArray(data));
    } catch (error) {
      console.error('Erreur chargement livres:', error);
    } finally {
      setBooksLoading(false);
    }
  };

  // ── Ouverture du formulaire ─────────────────────────────────────────────────
  const openCreateBook = () => {
    setEditingBook(null);
    setBookForm(EMPTY_BOOK_FORM);
    setBookMessage(null);
    setShowBookModal(true);
  };

  const openEditBook = (book) => {
    setEditingBook(book);
    setBookForm({
      title:           book.title || '',
      description:     book.description || '',
      author:          book.author || '',
      category:        book.category || '',
      coverImageUrl:   book.coverImageUrl || '',
      quantity:        book.quantity ?? 1,
      isPublished:     book.isPublished ?? true,
      publicationDate: toInputDate(book.publicationDate),
    });
    setBookMessage(null);
    setShowBookModal(true);
  };

  // ── Sauvegarde (create ou update) ─────────────────────────────────────────
  const handleSaveBook = async (e) => {
    e.preventDefault();
    setBookSaving(true);
    setBookMessage(null);

    const payload = {
      ...bookForm,
      quantity:        Number.parseInt(bookForm.quantity, 10),
      publicationDate: bookForm.publicationDate, // déjà au format yyyy-MM-dd depuis l'input date
    };

    try {
      if (editingBook) {
        await librarianService.updateBook(editingBook.id, payload);
        setBookMessage({ type: 'success', text: 'Livre mis à jour avec succès !' });
      } else {
        await librarianService.addBook(payload);
        setBookMessage({ type: 'success', text: 'Livre créé avec succès !' });
      }
      await loadBooks();
      setTimeout(() => setShowBookModal(false), 1200);
    } catch (err) {
      setBookMessage({ type: 'danger', text: err.response?.data?.message || 'Erreur lors de la sauvegarde.' });
    } finally {
      setBookSaving(false);
    }
  };

  // ── Suppression d'un livre ─────────────────────────────────────────────────
  const handleDeleteBook = (book) => {
    setBookToDelete(book);
    setShowDeleteBookConfirm(true);
  };

  const confirmDeleteBook = async () => {
    if (!bookToDelete) return;
    setShowDeleteBookConfirm(false);
    try {
      await librarianService.deleteBook(bookToDelete.id);
      setMessage({ type: 'success', text: `"${bookToDelete.title}" supprimé avec succès.` });
      await loadBooks();
    } catch (err) {
      setMessage({ type: 'danger', text: err.response?.data?.message || 'Erreur lors de la suppression.' });
    } finally {
      setBookToDelete(null);
    }
  };

  // ── Réservations ───────────────────────────────────────────────────────────
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
      PENDING:   <Badge bg="warning">En attente</Badge>,
      VALIDATED: <Badge bg="info">Validée</Badge>,
      CANCELLED: <Badge bg="danger">Annulée</Badge>,
      EXPIRED:   <Badge bg="secondary">Expirée</Badge>,
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
            <tr><td colSpan="8" className="text-center">Aucune réservation</td></tr>
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
                      <Button size="sm" variant="success" className="me-2"
                        onClick={() => handleValidate(reservation.id)} disabled={loading}>
                        <i className="bi bi-check-circle"></i> Valider
                      </Button>
                      <Button size="sm" variant="danger"
                        onClick={() => handleReject(reservation.id)} disabled={loading}>
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

      <ConfirmationModal
        show={currentModal !== null}
        onHide={hideModal}
        onConfirm={handleModalConfirm}
        config={
          currentModal === MODAL_TYPES.CONFIRM_REJECT_RESERVATION && modalConfig
            ? {
                ...modalConfig,
                alert: {
                  variant: 'light',
                  text: (
                    <Form.Group>
                      <Form.Label className="fw-semibold">Raison du rejet (optionnelle) :</Form.Label>
                      <Form.Control
                        as="textarea" rows={2} value={rejectReason}
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
        <h2><i className="bi bi-journal-check"></i> Tableau de bord Bibliothécaire</h2>

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
            if (k === 'books') loadBooks();
          }}
          className="mb-3"
        >
          {/* ── Onglet : réservations en attente ── */}
          <Tab eventKey="pending" title={`En attente (${pendingReservations.length})`}>
            <Card>
              <Card.Header className="bg-warning text-dark">
                <i className="bi bi-clock-history"></i> Réservations en attente de validation
              </Card.Header>
              <Card.Body>{renderReservationTable(pendingReservations)}</Card.Body>
            </Card>
          </Tab>

          {/* ── Onglet : toutes les réservations ── */}
          <Tab eventKey="all" title="Toutes les réservations">
            <Card>
              <Card.Header>
                <i className="bi bi-list"></i> Toutes les réservations
                <div className="float-end">
                  <Button size="sm" variant="outline-secondary" onClick={() => loadAllReservations()}>Toutes</Button>{' '}
                  <Button size="sm" variant="outline-warning"   onClick={() => loadAllReservations('PENDING')}>En attente</Button>{' '}
                  <Button size="sm" variant="outline-info"      onClick={() => loadAllReservations('VALIDATED')}>Validées</Button>{' '}
                  <Button size="sm" variant="outline-success"   onClick={() => loadAllReservations('CONVERTED')}>Converties</Button>
                </div>
              </Card.Header>
              <Card.Body>{renderReservationTable(allReservations)}</Card.Body>
            </Card>
          </Tab>

          {/* ── Onglet : gestion des livres ── */}
          <Tab eventKey="books" title={<><i className="bi bi-book me-1"></i>Livres</>}>
            <Card>
              <Card.Header className="d-flex justify-content-between align-items-center">
                <span><i className="bi bi-book"></i> Gestion du catalogue</span>
                <Button size="sm" variant="success" onClick={openCreateBook}>
                  <i className="bi bi-plus-circle me-1"></i>Ajouter un livre
                </Button>
              </Card.Header>
              <Card.Body>
                {booksLoading ? (
                  <p className="text-center text-muted">Chargement...</p>
                ) : (
                  <Table striped bordered hover responsive>
                    <thead>
                      <tr>
                        <th style={{ width: 60 }}>Couverture</th>
                        <th>Titre</th>
                        <th>Auteur</th>
                        <th>Catégorie</th>
                        <th>Stock</th>
                        <th>Publié</th>
                        <th>Actions</th>
                      </tr>
                    </thead>
                    <tbody>
                      {books.length === 0 ? (
                        <tr><td colSpan="7" className="text-center">Aucun livre</td></tr>
                      ) : (
                        books.map((book) => (
                          <tr key={book.id}>
                            <td>
                              {book.coverImageUrl ? (
                                <Image
                                  src={book.coverImageUrl}
                                  alt={book.title}
                                  style={{ width: 40, height: 56, objectFit: 'cover', borderRadius: 4 }}
                                />
                              ) : (
                                <div style={{ width: 40, height: 56, background: '#eee', borderRadius: 4,
                                  display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                                  <i className="bi bi-image text-muted"></i>
                                </div>
                              )}
                            </td>
                            <td className="fw-semibold">{book.title}</td>
                            <td>{book.author}</td>
                            <td><Badge bg="secondary">{book.category}</Badge></td>
                            <td>
                              <Badge bg={book.quantity > 0 ? 'success' : 'danger'}>
                                {book.quantity}
                              </Badge>
                            </td>
                            <td>
                              {book.isPublished
                                ? <Badge bg="success">Oui</Badge>
                                : <Badge bg="warning" text="dark">Non</Badge>}
                            </td>
                            <td>
                              <Button size="sm" variant="outline-primary" className="me-1"
                                onClick={() => openEditBook(book)}>
                                <i className="bi bi-pencil"></i>
                              </Button>
                              <Button size="sm" variant="outline-danger"
                                onClick={() => handleDeleteBook(book)}>
                                <i className="bi bi-trash"></i>
                              </Button>
                            </td>
                          </tr>
                        ))
                      )}
                    </tbody>
                  </Table>
                )}
              </Card.Body>
            </Card>
          </Tab>
        </Tabs>

        {/* ── Modal : créer / modifier un livre ── */}
        <Modal show={showBookModal} onHide={() => setShowBookModal(false)} size="lg">
          <Modal.Header closeButton>
            <Modal.Title>
              <i className={`bi bi-${editingBook ? 'pencil' : 'plus-circle'} me-2`}></i>
              {editingBook ? `Modifier : ${editingBook.title}` : 'Ajouter un nouveau livre'}
            </Modal.Title>
          </Modal.Header>
          <Form onSubmit={handleSaveBook}>
            <Modal.Body>
              {bookMessage && (
                <Alert variant={bookMessage.type}>{bookMessage.text}</Alert>
              )}
              <Row>
                {/* Colonne gauche : couverture */}
                <Col md={4} className="text-center mb-3">
                  <Form.Label className="fw-semibold d-block mb-2">Image de couverture</Form.Label>
                  <BookCoverUpload
                    currentImageUrl={bookForm.coverImageUrl}
                    onUploadSuccess={(url) => setBookForm(f => ({ ...f, coverImageUrl: url }))}
                  />
                  {bookForm.coverImageUrl && (
                    <Form.Text className="text-success d-block mt-1">
                      <i className="bi bi-check-circle me-1"></i>Image uploadée
                    </Form.Text>
                  )}
                </Col>

                {/* Colonne droite : champs */}
                <Col md={8}>
                  <Form.Group className="mb-2">
                    <Form.Label>Titre <span className="text-danger">*</span></Form.Label>
                    <Form.Control required maxLength={48} value={bookForm.title}
                      onChange={(e) => setBookForm(f => ({ ...f, title: e.target.value }))} />
                  </Form.Group>

                  <Form.Group className="mb-2">
                    <Form.Label>Auteur <span className="text-danger">*</span></Form.Label>
                    <Form.Control required maxLength={60} value={bookForm.author}
                      onChange={(e) => setBookForm(f => ({ ...f, author: e.target.value }))} />
                  </Form.Group>

                  <Row>
                    <Col md={6}>
                      <Form.Group className="mb-2">
                        <Form.Label>Catégorie <span className="text-danger">*</span></Form.Label>
                        <Form.Select required value={bookForm.category}
                          onChange={(e) => setBookForm(f => ({ ...f, category: e.target.value }))}>
                          <option value="">-- Choisir --</option>
                          {CATEGORIES.map(c => <option key={c} value={c}>{c}</option>)}
                        </Form.Select>
                      </Form.Group>
                    </Col>
                    <Col md={6}>
                      <Form.Group className="mb-2">
                        <Form.Label>Stock <span className="text-danger">*</span></Form.Label>
                        <Form.Control required type="number" min={0} value={bookForm.quantity}
                          onChange={(e) => setBookForm(f => ({ ...f, quantity: e.target.value }))} />
                      </Form.Group>
                    </Col>
                  </Row>

                  <Form.Group className="mb-2">
                    <Form.Label>Date de publication <span className="text-danger">*</span></Form.Label>
                    <Form.Control required type="date" value={bookForm.publicationDate}
                      onChange={(e) => setBookForm(f => ({ ...f, publicationDate: e.target.value }))} />
                  </Form.Group>

                  <Form.Group className="mb-2">
                    <Form.Check
                      type="switch"
                      id="isPublished"
                      label="Publié (visible par les clients)"
                      checked={bookForm.isPublished}
                      onChange={(e) => setBookForm(f => ({ ...f, isPublished: e.target.checked }))}
                    />
                  </Form.Group>
                </Col>
              </Row>

              <Form.Group className="mb-2">
                <Form.Label>Description <span className="text-danger">*</span></Form.Label>
                <Form.Control required as="textarea" rows={3} value={bookForm.description}
                  onChange={(e) => setBookForm(f => ({ ...f, description: e.target.value }))} />
              </Form.Group>
            </Modal.Body>
            <Modal.Footer>
              <Button variant="secondary" onClick={() => setShowBookModal(false)}>Annuler</Button>
              <Button type="submit" variant="primary" disabled={bookSaving}>
                {bookSaving ? (
                  <><span className="spinner-border spinner-border-sm me-1" />Sauvegarde...</>
                ) : (
                  <><i className="bi bi-save me-1"></i>{editingBook ? 'Mettre à jour' : 'Créer le livre'}</>
                )}
              </Button>
            </Modal.Footer>
          </Form>
        </Modal>

        {/* ── Modal : confirmer la suppression ── */}
        <Modal show={showDeleteBookConfirm} onHide={() => setShowDeleteBookConfirm(false)}>
          <Modal.Header closeButton>
            <Modal.Title><i className="bi bi-trash text-danger me-2"></i>Supprimer le livre</Modal.Title>
          </Modal.Header>
          <Modal.Body>
            <Alert variant="danger">
              Êtes-vous sûr de vouloir supprimer <strong>"{bookToDelete?.title}"</strong> ?
              <br /><small>Cette action est irréversible.</small>
            </Alert>
          </Modal.Body>
          <Modal.Footer>
            <Button variant="secondary" onClick={() => setShowDeleteBookConfirm(false)}>Annuler</Button>
            <Button variant="danger" onClick={confirmDeleteBook}>
              <i className="bi bi-trash me-1"></i>Supprimer
            </Button>
          </Modal.Footer>
        </Modal>

      </Container>
    </>
  );
};

export default LibrarianDashboard;

