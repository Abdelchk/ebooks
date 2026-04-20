import React, { useState, useEffect } from 'react';
import { Container, Table, Button, Badge, Alert, Modal, Form, Card, Row, Col } from 'react-bootstrap';
import Navigation from '../components/Navbar';
import adminService from '../services/adminService';
import ConfirmationModal from '../components/ConfirmationModal';
import { MODAL_TYPES, getModalConfig } from '../config/modalConfig';

const AdminDashboard = () => {
  const [users, setUsers] = useState([]);
  const [stats, setStats] = useState({});
  const [message, setMessage] = useState({ type: '', text: '' });
  const [loading, setLoading] = useState(false);
  const [showUserModal, setShowUserModal] = useState(false);
  const [selectedUser, setSelectedUser] = useState(null);

  // État modal de confirmation
  const [currentModal, setCurrentModal] = useState(null);
  const [modalConfig, setModalConfig] = useState(null);
  const [pendingAction, setPendingAction] = useState(null);
  const [newRole, setNewRole] = useState('client');

  useEffect(() => {
    loadUsers();
    loadStats();
  }, []);

  const loadUsers = async () => {
    try {
      const data = await adminService.getAllUsers();
      setUsers(data);
    } catch (error) {
      console.error('Erreur:', error);
      if (error.response?.status === 403) {
        setMessage({ type: 'danger', text: 'Accès refusé. Vous devez être administrateur.' });
      }
    }
  };

  const loadStats = async () => {
    try {
      const data = await adminService.getStats();
      setStats(data);
    } catch (error) {
      console.error('Erreur:', error);
    }
  };

  const showModal = (type, params = {}, action = null) => {
    setCurrentModal(type);
    setModalConfig(getModalConfig(type, params));
    setPendingAction(action);
  };

  const hideModal = () => {
    setCurrentModal(null);
    setModalConfig(null);
    setPendingAction(null);
  };

  const handleModalConfirm = async () => {
    if (!pendingAction) return;
    hideModal();
    setLoading(true);

    try {
      if (pendingAction.type === 'toggleStatus') {
        await adminService.toggleUserStatus(pendingAction.userId);
        setMessage({ type: 'success', text: 'Statut modifié avec succès !' });

      } else if (pendingAction.type === 'changeRole') {
        await adminService.changeUserRole(pendingAction.userId, newRole);
        setMessage({ type: 'success', text: 'Rôle modifié avec succès !' });

      } else if (pendingAction.type === 'delete') {
        await adminService.deleteUser(pendingAction.userId);
        setMessage({ type: 'success', text: 'Utilisateur supprimé avec succès !' });
      }
      loadUsers();
      loadStats();
    } catch (error) {
      setMessage({ type: 'danger', text: error.response?.data?.message || 'Erreur lors de l\'opération' });
    } finally {
      setLoading(false);
    }
  };

  const handleToggleStatus = (user) => {
    const type = user.enabled
      ? MODAL_TYPES.CONFIRM_TOGGLE_USER_STATUS_DISABLE
      : MODAL_TYPES.CONFIRM_TOGGLE_USER_STATUS_ENABLE;
    showModal(type, {}, { type: 'toggleStatus', userId: user.id });
  };

  const handleChangeRole = (user) => {
    setNewRole(user.role === 'client' ? 'librarian' : 'client');
    showModal(
      MODAL_TYPES.CONFIRM_CHANGE_ROLE,
      { name: `${user.firstname} ${user.lastname}`, currentRole: user.role },
      { type: 'changeRole', userId: user.id }
    );
  };

  const handleDelete = (user) => {
    showModal(
      MODAL_TYPES.CONFIRM_DELETE_USER,
      { name: `${user.firstname} ${user.lastname}` },
      { type: 'delete', userId: user.id }
    );
  };

  const handleViewUser = async (userId) => {
    try {
      const user = await adminService.getUserById(userId);
      setSelectedUser(user);
      setShowUserModal(true);
    } catch (error) {
      setMessage({ type: 'danger', text: 'Erreur lors du chargement des détails' });
    }
  };

  const getRoleBadge = (role) => {
    const badges = {
      client: <Badge bg="primary">Client</Badge>,
      librarian: <Badge bg="info">Bibliothécaire</Badge>,
      admin: <Badge bg="danger">Administrateur</Badge>
    };
    return badges[role?.toLowerCase()] || <Badge bg="secondary">{role}</Badge>;
  };

  return (
    <>
      <Navigation />
      <Container className="mt-5">
        <h2>
          <i className="bi bi-shield-lock"></i> Tableau de bord Administrateur
        </h2>

        {message.text && (
          <Alert variant={message.type} dismissible onClose={() => setMessage({ type: '', text: '' })}>
            {message.text}
          </Alert>
        )}

        {/* Statistiques */}
        <Row className="mb-4">
          <Col md={3}>
            <Card className="text-center">
              <Card.Body>
                <h3 className="text-primary">{stats.totalUsers || 0}</h3>
                <p className="mb-0">Total Utilisateurs</p>
              </Card.Body>
            </Card>
          </Col>
          <Col md={3}>
            <Card className="text-center">
              <Card.Body>
                <h3 className="text-success">{stats.enabledUsers || 0}</h3>
                <p className="mb-0">Actifs</p>
              </Card.Body>
            </Card>
          </Col>
          <Col md={3}>
            <Card className="text-center">
              <Card.Body>
                <h3 className="text-info">{stats.librarians || 0}</h3>
                <p className="mb-0">Bibliothécaires</p>
              </Card.Body>
            </Card>
          </Col>
          <Col md={3}>
            <Card className="text-center">
              <Card.Body>
                <h3 className="text-primary">{stats.clients || 0}</h3>
                <p className="mb-0">Clients</p>
              </Card.Body>
            </Card>
          </Col>
        </Row>

        {/* Liste des utilisateurs */}
        <Card>
          <Card.Header>
            <i className="bi bi-people"></i> Gestion des Utilisateurs
          </Card.Header>
          <Card.Body>
            <Table striped bordered hover responsive>
              <thead>
                <tr>
                  <th>#</th>
                  <th>Nom</th>
                  <th>Prénom</th>
                  <th>Email</th>
                  <th>Téléphone</th>
                  <th>Rôle</th>
                  <th>Statut</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {users.length === 0 ? (
                  <tr><td colSpan="8" className="text-center">Aucun utilisateur</td></tr>
                ) : (
                  users.map((user) => (
                    <tr key={user.id}>
                      <td>{user.id}</td>
                      <td>{user.lastname}</td>
                      <td>{user.firstname}</td>
                      <td>{user.email}</td>
                      <td>{user.phoneNumber}</td>
                      <td>{getRoleBadge(user.role)}</td>
                      <td>
                        {user.enabled
                          ? <Badge bg="success">Actif</Badge>
                          : <Badge bg="danger">Désactivé</Badge>}
                      </td>
                      <td>
                        <Button size="sm" variant="info" className="me-1 mb-1" onClick={() => handleViewUser(user.id)}>
                          <i className="bi bi-eye"></i>
                        </Button>
                        <Button
                          size="sm"
                          variant={user.enabled ? 'warning' : 'success'}
                          className="me-1 mb-1"
                          onClick={() => handleToggleStatus(user)}
                          disabled={loading}
                        >
                          <i className={user.enabled ? 'bi bi-pause' : 'bi bi-play'}></i>
                        </Button>
                        <Button
                          size="sm"
                          variant="secondary"
                          className="me-1 mb-1"
                          onClick={() => handleChangeRole(user)}
                          disabled={loading}
                        >
                          <i className="bi bi-person-gear"></i>
                        </Button>
                        <Button
                          size="sm"
                          variant="danger"
                          className="mb-1"
                          onClick={() => handleDelete(user)}
                          disabled={loading}
                        >
                          <i className="bi bi-trash"></i>
                        </Button>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </Table>
          </Card.Body>
        </Card>

        {/* Modal de confirmation générique */}
        <ConfirmationModal
          show={currentModal !== null}
          onHide={hideModal}
          onConfirm={handleModalConfirm}
          config={
            currentModal === MODAL_TYPES.CONFIRM_CHANGE_ROLE && modalConfig
              ? {
                  ...modalConfig,
                  alert: {
                    variant: 'light',
                    text: (
                      <Form.Group>
                        <Form.Label className="fw-semibold">Nouveau rôle :</Form.Label>
                        <Form.Select value={newRole} onChange={(e) => setNewRole(e.target.value)}>
                          <option value="client">Client</option>
                          <option value="librarian">Bibliothécaire</option>
                        </Form.Select>
                      </Form.Group>
                    ),
                  },
                }
              : modalConfig
          }
        />

        {/* Modal de détails utilisateur */}
        <Modal show={showUserModal} onHide={() => setShowUserModal(false)} size="lg">
          <Modal.Header closeButton>
            <Modal.Title>
              <i className="bi bi-person-circle"></i> Détails de l'utilisateur
            </Modal.Title>
          </Modal.Header>
          <Modal.Body>
            {selectedUser && (
              <div>
                <Row className="mb-3">
                  <Col md={6}>
                    <strong>ID:</strong> {selectedUser.id}
                  </Col>
                  <Col md={6}>
                    <strong>Statut:</strong> {selectedUser.enabled ? <Badge bg="success">Actif</Badge> : <Badge bg="danger">Désactivé</Badge>}
                  </Col>
                </Row>
                <Row className="mb-3">
                  <Col md={6}>
                    <strong>Nom:</strong> {selectedUser.lastname}
                  </Col>
                  <Col md={6}>
                    <strong>Prénom:</strong> {selectedUser.firstname}
                  </Col>
                </Row>
                <Row className="mb-3">
                  <Col md={6}>
                    <strong>Email:</strong> {selectedUser.email}
                  </Col>
                  <Col md={6}>
                    <strong>Téléphone:</strong> {selectedUser.phoneNumber}
                  </Col>
                </Row>
                <Row className="mb-3">
                  <Col md={6}>
                    <strong>Date de naissance:</strong> {selectedUser.birthdate}
                  </Col>
                  <Col md={6}>
                    <strong>Rôle:</strong> {getRoleBadge(selectedUser.role)}
                  </Col>
                </Row>
                {selectedUser.lastPasswordUpdateDate && (
                  <Row className="mb-3">
                    <Col md={12}>
                      <strong>Dernière mise à jour du mot de passe:</strong> {selectedUser.lastPasswordUpdateDate}
                    </Col>
                  </Row>
                )}
              </div>
            )}
          </Modal.Body>
          <Modal.Footer>
            <Button variant="secondary" onClick={() => setShowUserModal(false)}>
              Fermer
            </Button>
          </Modal.Footer>
        </Modal>
      </Container>
    </>
  );
};

export default AdminDashboard;

