import React from 'react';
import { ProgressBar } from 'react-bootstrap';
import { getPasswordStrength } from '../utils/validation';

const PasswordStrengthIndicator = ({ password }) => {
  if (!password) return null;

  const strength = getPasswordStrength(password);

  const progressValue = {
    'weak': 25,
    'medium': 50,
    'good': 75,
    'strong': 100,
  };

  return (
    <div className="mt-2">
      <ProgressBar
        now={progressValue[strength.level]}
        variant={strength.color}
        label={strength.label}
      />
      <small className="text-muted">Force du mot de passe : {strength.label}</small>
    </div>
  );
};

export default PasswordStrengthIndicator;

