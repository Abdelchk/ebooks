import React from 'react';
import { Modal, Button, Alert } from 'react-bootstrap';
import PropTypes from 'prop-types';
import './ConfirmationModal.css';

/**
 * Composant modal générique et réutilisable
 * Supporte différents types de modals (confirmation, succès, erreur, info)
 * Configuration via modalConfig.js
 */
const ConfirmationModal = ({
  show,
  onHide,
  onConfirm,
  config,
  backdrop = 'static',
  centered = true,
}) => {
  if (!config) return null;

  const {
    variant = 'primary',
    icon,
    iconSize = '1.2rem',
    title,
    headerVariant,
    message,
    subMessage,
    subMessageVariant,
    alert,
    confirmText = 'Confirmer',
    confirmIcon,
    confirmVariant = 'primary',
    cancelText = 'Annuler',
    cancelIcon,
    showCancelButton = true,
  } = config;

  return (
    <Modal
      show={show}
      onHide={onHide}
      backdrop={backdrop}
      centered={centered}
      className="confirmation-modal"
    >
      <Modal.Header
        closeButton
        className={headerVariant ? `bg-${headerVariant} text-white` : ''}
      >
        <Modal.Title>
          {icon && (
            <i
              className={`bi ${icon} me-2`}
              style={{ fontSize: iconSize }}
            />
          )}
          {title}
        </Modal.Title>
      </Modal.Header>

      <Modal.Body className={iconSize === '4rem' ? 'text-center py-4' : ''}>
        {/* Icône principale (pour les modals de succès/erreur) */}
        {iconSize === '4rem' && icon && (
          <i
            className={`bi ${icon} text-${variant}`}
            style={{ fontSize: iconSize }}
          />
        )}

        {/* Message principal */}
        {message && (
          <p className={iconSize === '4rem' ? 'mt-3 mb-0 fs-5' : 'mb-2'}>
            {message}
          </p>
        )}

        {/* Sous-message */}
        {subMessage && (
          <p className={`text-${subMessageVariant || variant} mb-0`}>
            {subMessageVariant === 'danger' && <strong>{subMessage}</strong>}
            {subMessageVariant !== 'danger' && <small>{subMessage}</small>}
          </p>
        )}

        {/* Alerte contextuelle */}
        {alert && (
          <Alert variant={alert.variant} className="mb-0 mt-3">
            {alert.icon && <i className={`bi ${alert.icon} me-2`} />}
            {alert.text}
          </Alert>
        )}
      </Modal.Body>

      <Modal.Footer>
        {showCancelButton && (
          <Button variant="secondary" onClick={onHide}>
            {cancelIcon && <i className={`bi ${cancelIcon} me-1`} />}
            {cancelText}
          </Button>
        )}
        <Button
          variant={confirmVariant}
          onClick={onConfirm}
          className={!showCancelButton ? 'w-100' : ''}
        >
          {confirmIcon && <i className={`bi ${confirmIcon} me-1`} />}
          {confirmText}
        </Button>
      </Modal.Footer>
    </Modal>
  );
};

ConfirmationModal.propTypes = {
  show: PropTypes.bool.isRequired,
  onHide: PropTypes.func.isRequired,
  onConfirm: PropTypes.func.isRequired,
  config: PropTypes.shape({
    variant: PropTypes.string,
    icon: PropTypes.string,
    iconSize: PropTypes.string,
    title: PropTypes.string,
    headerVariant: PropTypes.string,
    message: PropTypes.string,
    subMessage: PropTypes.string,
    subMessageVariant: PropTypes.string,
    alert: PropTypes.shape({
      variant: PropTypes.string,
      icon: PropTypes.string,
      text: PropTypes.string,
    }),
    confirmText: PropTypes.string,
    confirmIcon: PropTypes.string,
    confirmVariant: PropTypes.string,
    cancelText: PropTypes.string,
    cancelIcon: PropTypes.string,
    showCancelButton: PropTypes.bool,
  }),
  backdrop: PropTypes.oneOfType([PropTypes.bool, PropTypes.string]),
  centered: PropTypes.bool,
};

export default ConfirmationModal;

