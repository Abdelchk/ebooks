// Messages d'erreur et de succès

export const ERROR_MESSAGES = {
  NETWORK_ERROR: 'Erreur de connexion au serveur. Veuillez réessayer.',
  INVALID_CREDENTIALS: 'Email ou mot de passe incorrect.',
  ACCOUNT_DISABLED: 'Votre compte n\'est pas encore activé. Veuillez vérifier votre email.',
  EMAIL_EXISTS: 'Un compte avec cet email existe déjà.',
  INVALID_EMAIL: 'Veuillez entrer une adresse email valide.',
  INVALID_PASSWORD: 'Le mot de passe ne respecte pas les critères de sécurité.',
  PASSWORDS_NOT_MATCH: 'Les mots de passe ne correspondent pas.',
  INVALID_TOKEN: 'Le lien est invalide ou a expiré.',
  INVALID_CODE: 'Code invalide ou expiré.',
  RECAPTCHA_FAILED: 'La vérification reCAPTCHA a échoué. Veuillez réessayer.',
  SERVER_ERROR: 'Une erreur serveur s\'est produite. Veuillez réessayer plus tard.',
};

export const SUCCESS_MESSAGES = {
  LOGIN_SUCCESS: 'Connexion réussie !',
  LOGOUT_SUCCESS: 'Déconnexion réussie.',
  REGISTER_SUCCESS: 'Inscription réussie ! Veuillez vérifier votre email.',
  PASSWORD_UPDATED: 'Mot de passe mis à jour avec succès.',
  PASSWORD_RESET: 'Votre mot de passe a été réinitialisé avec succès.',
  EMAIL_SENT: 'Un email a été envoyé à votre adresse.',
  CODE_SENT: 'Un nouveau code a été envoyé.',
  ACCOUNT_VERIFIED: 'Votre compte est maintenant vérifié.',
};

