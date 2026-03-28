// Utilitaires pour la validation des formulaires

export const validateEmail = (email) => {
  const regex = /^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+\.[A-Z|a-z]{2,}$/;
  return regex.test(email);
};

export const validatePassword = (password) => {
  const regex = /^(?=.*[A-Za-zÀ-ÖØ-öø-ÿ])(?=.*\d)(?=.*[@$!%*?&#])[A-Za-zÀ-ÖØ-öø-ÿ\d@$!%*?&#]{12,}$/;
  return regex.test(password);
};

export const validatePhoneNumber = (phoneNumber) => {
  const regex = /^(0|\+33|0033)[1-9][0-9]{8}$/;
  return regex.test(phoneNumber);
};

export const validateName = (name) => {
  const regex = /^[a-zA-ZÀ-ÿ\s-]+$/;
  return regex.test(name) && name.length >= 2;
};

export const passwordRequirements = [
  'Minimum 12 caractères',
  'Au moins une lettre',
  'Au moins un chiffre',
  'Au moins un caractère spécial (@$!%*?&#)',
];

export const getPasswordStrength = (password) => {
  let strength = 0;

  if (password.length >= 12) strength++;
  if (/[a-zA-ZÀ-ÖØ-öø-ÿ]/.test(password)) strength++;
  if (/\d/.test(password)) strength++;
  if (/[@$!%*?&#]/.test(password)) strength++;
  if (password.length >= 16) strength++;

  if (strength <= 2) return { level: 'weak', label: 'Faible', color: 'danger' };
  if (strength <= 3) return { level: 'medium', label: 'Moyen', color: 'warning' };
  if (strength <= 4) return { level: 'good', label: 'Bon', color: 'info' };
  return { level: 'strong', label: 'Fort', color: 'success' };
};

