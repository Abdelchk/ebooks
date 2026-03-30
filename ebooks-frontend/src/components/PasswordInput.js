// components/PasswordInput.js
import React, { useState } from 'react';
import { Form, Button } from 'react-bootstrap';
import PropTypes from 'prop-types';

/**
 * Composant réutilisable pour les champs de mot de passe avec toggle de visibilité
 */
const PasswordInput = ({ 
  label, 
  name, 
  value, 
  onChange, 
  required = true,
  pattern = null,
  placeholder = "",
  invalidFeedback = "Veuillez entrer un mot de passe valide.",
  helpText = null
}) => {
  const [showPassword, setShowPassword] = useState(false);

  return (
    <Form.Group className="mb-3">
      <Form.Label>{label}</Form.Label>
      <div className="input-group">
        <Form.Control
          type={showPassword ? "text" : "password"}
          name={name}
          value={value}
          onChange={onChange}
          pattern={pattern}
          placeholder={placeholder}
          required={required}
          className="form-control-lg"
        />
        <Button 
          variant="outline-secondary" 
          onClick={() => setShowPassword(!showPassword)}
          type="button"
          title={showPassword ? "Masquer le mot de passe" : "Afficher le mot de passe"}
          style={{ 
            border: '1px solid #ced4da', 
            borderLeft: 'none',
            transition: 'all 0.2s'
          }}
        >
          <i 
            className={`bi ${showPassword ? 'bi-eye-slash-fill' : 'bi-eye-fill'}`}
            style={{ fontSize: '1.1rem' }}
          ></i>
        </Button>
      </div>
      {helpText && (
        <Form.Text className="text-muted">
          {helpText}
        </Form.Text>
      )}
      <Form.Control.Feedback type="invalid">
        {invalidFeedback}
      </Form.Control.Feedback>
    </Form.Group>
  );
};

PasswordInput.propTypes = {
  label: PropTypes.string.isRequired,
  name: PropTypes.string.isRequired,
  value: PropTypes.string.isRequired,
  onChange: PropTypes.func.isRequired,
  required: PropTypes.bool,
  pattern: PropTypes.string,
  placeholder: PropTypes.string,
  invalidFeedback: PropTypes.string,
  helpText: PropTypes.string,
};

export default PasswordInput;

