import React, { useState, useEffect } from 'react';
import { Container, Row, Col, Nav, Tab, Form, Button, Alert, Card } from 'react-bootstrap';
import { useLocation, useNavigate } from 'react-router-dom';
import { userService } from '../services/userService';
import { stockAlertService } from '../services/stockAlertService';
import { useAuth } from '../context/AuthContext';
import Navigation from '../components/Navbar';
import Loader from '../components/Loader';
import Reservations from './Reservations';
import Loans from './Loans';
import ConfirmationModal from '../components/ConfirmationModal';
import { MODAL_TYPES, getModalConfig } from '../config/modalConfig';
import './Profile.css';

const Profile = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const { checkAuthStatus } = useAuth();

  // Déterminer l'onglet actif depuis l'URL
  const getInitialTab = () => {
    const hash = location.hash.replace('#', '');
    return hash || 'info';
  };

  const [activeTab, setActiveTab] = useState(getInitialTab());
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [alerts, setAlerts] = useState([]);
  const [currentModal, setCurrentModal] = useState(null);
  const [modalConfig, setModalConfig] = useState(null);
  const [alertToCancel, setAlertToCancel] = useState(null);

  const showModal = (type, params = {}) => {
    setCurrentModal(type);
    setModalConfig(getModalConfig(type, params));
  };

  const hideModal = () => {
    setCurrentModal(null);
    setModalConfig(null);
  };

  // Informations utilisateur
  const [formData, setFormData] = useState({
    firstname: '',
    lastname: '',
    email: '',
    phoneNumber: ''
  });

  const [validated, setValidated] = useState(false);

  useEffect(() => {
    loadUserData().catch(err => {
      console.error('Erreur lors du chargement initial des données utilisateur:', err);
    });
    loadAlerts().catch(err => {
      console.error('Erreur lors du chargement initial des alertes:', err);
    });
  }, []);

  useEffect(() => {
    // Mettre à jour l'onglet actif quand l'URL change
    const hash = location.hash.replace('#', '');
    if (hash) {
      setActiveTab(hash);
    }
  }, [location]);

  const loadUserData = async () => {
    try {
      const userData = await userService.getCurrentUser();
      setFormData({
        firstname: userData.firstname || '',
        lastname: userData.lastname || '',
        email: userData.email || '',
        phoneNumber: userData.phoneNumber || ''
      });
    } catch (err) {
      setError('Erreur lors du chargement des informations utilisateur');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const loadAlerts = async () => {
    try {
      const data = await stockAlertService.getUserAlerts();
      setAlerts(data);
    } catch (err) {
      console.error('Erreur lors du chargement des alertes:', err);
    }
  };

  const handleChange = (e) => {
    const { name, value } = e.target;

    // Bloquer les chiffres dans les champs firstname et lastname
    if ((name === 'firstname' || name === 'lastname') && /\d/.test(value)) {
      return; // Ne pas mettre à jour si des chiffres sont détectés
    }

    setFormData({
      ...formData,
      [name]: value
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const form = e.currentTarget;

    if (form.checkValidity() === false) {
      e.stopPropagation();
      setValidated(true);
      return;
    }

    setValidated(true);

    try {
      await userService.updateUser({
        firstname: formData.firstname,
        lastname: formData.lastname,
        phoneNumber: formData.phoneNumber
      });
      setSuccess('Informations mises à jour avec succès !');
      setError('');
      await checkAuthStatus(); // Rafraîchir les données utilisateur
      setTimeout(() => setSuccess(''), 3000);
    } catch (err) {
      setError(err.response?.data?.message || 'Erreur lors de la mise à jour');
      setSuccess('');
    }
  };

  const handleCancelAlert = (alertId) => {
    setAlertToCancel(alertId);
    showModal(MODAL_TYPES.CONFIRM_CANCEL_ALERT);
  };

  const confirmCancelAlert = async () => {
    try {
      await stockAlertService.cancelAlert(alertToCancel);
      await loadAlerts();
      hideModal();
      setAlertToCancel(null);
      showModal(MODAL_TYPES.SUCCESS_GENERIC, {
        message: 'Alerte annulée avec succès'
      });
    } catch (err) {
      setError(err.response?.data?.message || 'Erreur lors de l\'annulation de l\'alerte');
      hideModal();
    }
  };

  const handleModalConfirm = () => {
    switch (currentModal) {
      case MODAL_TYPES.CONFIRM_CANCEL_ALERT:
        confirmCancelAlert();
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
      ACTIVE: { bg: 'primary', text: '🔔 Active' },
      NOTIFIED: { bg: 'success', text: '✓ Notifié' },
      CANCELLED: { bg: 'secondary', text: '❌ Annulée' }
    };
    return badges[status] || { bg: 'secondary', text: status };
  };

  if (loading) {
    return (
      <>
        <Navigation />
        <Container className="mt-4">
          <Loader message="Chargement du profil..." />
        </Container>
      </>
    );
  }

  return (
    <>
      <Navigation />
      <Container fluid className="mt-4">
        <Row>
          <Col md={3}>
            <Card className="profile-sidebar">
              <Card.Body>
                <div className="text-center mb-3">
                  <div className="profile-avatar mb-2">
                    <i className="bi bi-person-circle"></i>
                  </div>
                  <h5>{formData.firstname} {formData.lastname}</h5>
                  <p className="text-muted">{formData.email}</p>
                </div>
                <hr />
                <Nav variant="pills" className="flex-column" activeKey={activeTab}>
                  <Nav.Item>
                    <Nav.Link
                      eventKey="info"
                      onClick={() => {
                        setActiveTab('info');
                        navigate('#info');
                      }}
                    >
                      <i className="bi bi-person-fill me-2"></i>
                      Informations
                    </Nav.Link>
                  </Nav.Item>
                  <Nav.Item>
                    <Nav.Link
                      eventKey="reservations"
                      onClick={() => {
                        setActiveTab('reservations');
                        navigate('#reservations');
                      }}
                    >
                      <i className="bi bi-bookmark-fill me-2"></i>
                      Réservations
                    </Nav.Link>
                  </Nav.Item>
                  <Nav.Item>
                    <Nav.Link
                      eventKey="loans"
                      onClick={() => {
                        setActiveTab('loans');
                        navigate('#loans');
                      }}
                    >
                      <i className="bi bi-book me-2"></i>
                      Emprunts
                    </Nav.Link>
                  </Nav.Item>
                  <Nav.Item>
                    <Nav.Link
                      eventKey="alerts"
                      onClick={() => {
                        setActiveTab('alerts');
                        navigate('#alerts');
                      }}
                    >
                      <i className="bi bi-bell-fill me-2"></i>
                      Alertes
                    </Nav.Link>
                  </Nav.Item>
                </Nav>
              </Card.Body>
            </Card>
          </Col>

          <Col md={9}>
            <Tab.Container activeKey={activeTab}>
              <Tab.Content>
                {/* Onglet Informations */}
                <Tab.Pane eventKey="info">
                  <Card>
                    <Card.Header>
                      <h4>
                        <i className="bi bi-person-fill me-2"></i>
                        Mes Informations
                      </h4>
                    </Card.Header>
                    <Card.Body>
                      {error && <Alert variant="danger" dismissible onClose={() => setError('')}>{error}</Alert>}
                      {success && <Alert variant="success" dismissible onClose={() => setSuccess('')}>{success}</Alert>}

                      <Form noValidate validated={validated} onSubmit={handleSubmit}>
                        <Row>
                          <Col md={6}>
                            <Form.Group className="mb-3">
                              <Form.Label>Prénom</Form.Label>
                              <Form.Control
                                type="text"
                                name="firstname"
                                value={formData.firstname}
                                onChange={handleChange}
                                pattern="^[a-zA-ZÀ-ÿ\s-]+$"
                              />
                              <Form.Control.Feedback type="invalid">
                                Le prénom doit contenir uniquement des lettres
                              </Form.Control.Feedback>
                            </Form.Group>
                          </Col>
                          <Col md={6}>
                            <Form.Group className="mb-3">
                              <Form.Label>Nom</Form.Label>
                              <Form.Control
                                type="text"
                                name="lastname"
                                value={formData.lastname}
                                onChange={handleChange}
                                pattern="^[a-zA-ZÀ-ÿ\s-]+$"
                              />
                              <Form.Control.Feedback type="invalid">
                                Le nom doit contenir uniquement des lettres
                              </Form.Control.Feedback>
                            </Form.Group>
                          </Col>
                        </Row>

                        <Form.Group className="mb-3">
                          <Form.Label>Email</Form.Label>
                          <Form.Control
                            type="email"
                            name="email"
                            value={formData.email}
                            disabled
                            style={{ backgroundColor: '#e9ecef', cursor: 'not-allowed' }}
                          />
                          <Form.Text className="text-muted">
                            L'email ne peut pas être modifié
                          </Form.Text>
                        </Form.Group>

                        <Form.Group className="mb-3">
                          <Form.Label>Téléphone</Form.Label>
                          <Form.Control
                              type="tel"
                              name="phoneNumber"
                              value={formData.phoneNumber}
                              onChange={handleChange}
                              placeholder="Ex: 0612345678"
                              pattern="^(0|\+33|0033)[1-9][0-9]{8}$"
                          />
                          <Form.Control.Feedback type="invalid">
                            Le numéro doit commencer par 0 ou +33 suivi de 9 chiffres (ex: 0612345678)
                          </Form.Control.Feedback>
                        </Form.Group>

                        <div className="d-flex gap-2">
                          <Button variant="primary" type="submit">
                            <i className="bi bi-save me-2"></i>
                            Enregistrer les modifications
                          </Button>
                          <Button
                            variant="outline-secondary"
                            onClick={() => navigate('/update-password')}
                          >
                            <i className="bi bi-key-fill me-2"></i>
                            Modifier le mot de passe
                          </Button>
                        </div>
                      </Form>
                    </Card.Body>
                  </Card>
                </Tab.Pane>

                {/* Onglet Réservations */}
                <Tab.Pane eventKey="reservations">
                  <Reservations embedded={true} />
                </Tab.Pane>

                {/* Onglet Emprunts */}
                <Tab.Pane eventKey="loans">
                  <Loans embedded={true} />
                </Tab.Pane>

                {/* Onglet Alertes */}
                <Tab.Pane eventKey="alerts">
                  <Card>
                    <Card.Header>
                      <h4>
                        <i className="bi bi-bell-fill me-2"></i>
                        Mes Alertes Stock
                      </h4>
                    </Card.Header>
                    <Card.Body>
                      {error && <Alert variant="danger" dismissible onClose={() => setError('')}>{error}</Alert>}
                      {success && <Alert variant="success" dismissible onClose={() => setSuccess('')}>{success}</Alert>}

                      {alerts.length === 0 ? (
                        <Alert variant="info">
                          <i className="bi bi-info-circle me-2"></i>
                          Aucune alerte active. Vous pouvez créer une alerte pour être notifié quand un livre en rupture de stock est de nouveau disponible.
                        </Alert>
                      ) : (
                        <Row>
                          {alerts.map((alert) => (
                            <Col md={6} lg={4} key={alert.id} className="mb-3">
                              <Card className="h-100">
                                <Card.Img
                                  variant="top"
                                  src={alert.book.coverImageUrl}
                                  alt={alert.book.title}
                                  style={{ height: '200px', objectFit: 'cover' }}
                                />
                                <Card.Body>
                                  <Card.Title>{alert.book.title}</Card.Title>
                                  <Card.Text className="text-muted">
                                    <i className="bi bi-person-fill me-1"></i>
                                    {alert.book.author}
                                  </Card.Text>

                                  <div className="mb-2">
                                    <span className={`badge bg-${getStatusBadge(alert.status).bg}`}>
                                      {getStatusBadge(alert.status).text}
                                    </span>
                                  </div>

                                  <p className="mb-2">
                                    <small>
                                      <i className="bi bi-calendar me-1"></i>
                                      Créée le : {new Date(alert.createdAt).toLocaleDateString('fr-FR')}
                                    </small>
                                  </p>

                                  {alert.notifiedAt && (
                                    <p className="mb-2">
                                      <small className="text-success">
                                        <i className="bi bi-check-circle me-1"></i>
                                        Notifié le : {new Date(alert.notifiedAt).toLocaleDateString('fr-FR')}
                                      </small>
                                    </p>
                                  )}

                                  {alert.status === 'ACTIVE' && (
                                    <Button
                                      variant="outline-danger"
                                      size="sm"
                                      className="w-100"
                                      onClick={() => handleCancelAlert(alert.id)}
                                    >
                                      <i className="bi bi-x-circle me-1"></i>
                                      Annuler l'alerte
                                    </Button>
                                  )}
                                </Card.Body>
                              </Card>
                            </Col>
                          ))}
                        </Row>
                      )}
                    </Card.Body>
                  </Card>
                </Tab.Pane>
              </Tab.Content>
            </Tab.Container>
          </Col>
        </Row>

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

export default Profile;

