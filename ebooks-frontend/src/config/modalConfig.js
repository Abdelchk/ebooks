/**
 * Configuration centralisée des modals de l'application
 * Permet de gérer facilement les messages, icônes et variantes selon le contexte
 */

export const MODAL_TYPES = {
  // Modals de confirmation
  CONFIRM_REMOVE_ITEM: 'confirmRemoveItem',
  CONFIRM_CLEAR_CART: 'confirmClearCart',
  CONFIRM_VALIDATE_CART: 'confirmValidateCart',
  CONFIRM_CANCEL_RESERVATION: 'confirmCancelReservation',
  CONFIRM_CANCEL_ALERT: 'confirmCancelAlert',
  CONFIRM_DELETE: 'confirmDelete',

  // Modals de succès
  SUCCESS_GENERIC: 'successGeneric',
  SUCCESS_CART_VALIDATED: 'successCartValidated',
  SUCCESS_ITEM_ADDED: 'successItemAdded',
  SUCCESS_ALERT_CREATED: 'successAlertCreated',

  // Modals d'erreur
  ERROR_GENERIC: 'errorGeneric',

  // Modals d'information
  INFO_STOCK_ALERT: 'infoStockAlert',
};

export const modalConfigs = {
  // ========== MODALS DE CONFIRMATION ==========
  [MODAL_TYPES.CONFIRM_REMOVE_ITEM]: {
    variant: 'warning',
    icon: 'bi-exclamation-triangle',
    title: 'Confirmer la suppression',
    message: 'Êtes-vous sûr de vouloir retirer cet article de votre panier ?',
    confirmText: 'Retirer',
    confirmIcon: 'bi-trash',
    confirmVariant: 'danger',
    cancelText: 'Annuler',
    cancelIcon: 'bi-x-circle',
  },

  [MODAL_TYPES.CONFIRM_CLEAR_CART]: {
    variant: 'warning',
    icon: 'bi-exclamation-triangle',
    title: 'Vider le panier',
    message: 'Êtes-vous sûr de vouloir vider tout votre panier ?',
    subMessage: 'Cette action est irréversible.',
    subMessageVariant: 'danger',
    confirmText: 'Vider le panier',
    confirmIcon: 'bi-trash',
    confirmVariant: 'danger',
    cancelText: 'Annuler',
    cancelIcon: 'bi-x-circle',
  },

  [MODAL_TYPES.CONFIRM_VALIDATE_CART]: {
    variant: 'primary',
    icon: 'bi-check-circle',
    title: 'Valider le panier',
    message: (count) => `Vous êtes sur le point de créer ${count} réservation(s).`,
    alert: {
      variant: 'info',
      icon: 'bi-info-circle',
      text: 'Vous aurez 72 heures pour retirer vos livres à la bibliothèque. Passé ce délai, les réservations seront automatiquement annulées.',
    },
    confirmText: 'Confirmer',
    confirmIcon: 'bi-check-circle',
    confirmVariant: 'primary',
    cancelText: 'Annuler',
    cancelIcon: 'bi-x-circle',
  },

  [MODAL_TYPES.CONFIRM_CANCEL_RESERVATION]: {
    variant: 'warning',
    icon: 'bi-exclamation-triangle',
    title: 'Annuler la réservation',
    message: 'Êtes-vous sûr de vouloir annuler cette réservation ?',
    subMessage: 'Le livre sera de nouveau disponible pour d\'autres utilisateurs.',
    confirmText: 'Annuler la réservation',
    confirmIcon: 'bi-x-circle',
    confirmVariant: 'danger',
    cancelText: 'Retour',
    cancelIcon: 'bi-arrow-left',
  },

  [MODAL_TYPES.CONFIRM_CANCEL_ALERT]: {
    variant: 'warning',
    icon: 'bi-exclamation-triangle',
    title: 'Annuler l\'alerte',
    message: 'Êtes-vous sûr de vouloir annuler cette alerte ?',
    subMessage: 'Vous ne serez plus notifié quand ce livre sera disponible.',
    confirmText: 'Annuler l\'alerte',
    confirmIcon: 'bi-x-circle',
    confirmVariant: 'danger',
    cancelText: 'Retour',
    cancelIcon: 'bi-arrow-left',
  },

  [MODAL_TYPES.CONFIRM_DELETE]: {
    variant: 'danger',
    icon: 'bi-exclamation-triangle-fill',
    title: 'Confirmer la suppression',
    message: 'Cette action est irréversible. Êtes-vous sûr ?',
    confirmText: 'Supprimer',
    confirmIcon: 'bi-trash',
    confirmVariant: 'danger',
    cancelText: 'Annuler',
    cancelIcon: 'bi-x-circle',
  },

  // ========== MODALS DE SUCCÈS ==========
  [MODAL_TYPES.SUCCESS_GENERIC]: {
    variant: 'success',
    icon: 'bi-check-circle-fill',
    iconSize: '4rem',
    title: 'Succès',
    headerVariant: 'success',
    message: 'Opération effectuée avec succès !',
    confirmText: 'OK',
    confirmVariant: 'success',
    showCancelButton: false,
  },

  [MODAL_TYPES.SUCCESS_CART_VALIDATED]: {
    variant: 'success',
    icon: 'bi-check-circle-fill',
    iconSize: '4rem',
    title: 'Succès',
    headerVariant: 'success',
    message: 'Réservations créées avec succès ! Vous avez 72h pour retirer vos livres.',
    confirmText: 'OK',
    confirmVariant: 'success',
    showCancelButton: false,
  },

  [MODAL_TYPES.SUCCESS_ITEM_ADDED]: {
    variant: 'success',
    icon: 'bi-check-circle-fill',
    iconSize: '4rem',
    title: 'Succès',
    headerVariant: 'success',
    message: 'Livre ajouté au panier avec succès !',
    confirmText: 'OK',
    confirmVariant: 'success',
    showCancelButton: false,
  },

  [MODAL_TYPES.SUCCESS_ALERT_CREATED]: {
    variant: 'success',
    icon: 'bi-check-circle-fill',
    iconSize: '4rem',
    title: 'Alerte créée',
    headerVariant: 'success',
    message: (email) => `Vous serez notifié par email à ${email} dès que ce livre sera de nouveau disponible.`,
    subMessage: 'Vous pouvez annuler cette alerte à tout moment depuis votre profil.',
    subMessageVariant: 'muted',
    confirmText: 'OK',
    confirmVariant: 'success',
    showCancelButton: false,
  },

  // ========== MODALS D'ERREUR ==========
  [MODAL_TYPES.ERROR_GENERIC]: {
    variant: 'danger',
    icon: 'bi-exclamation-circle-fill',
    iconSize: '4rem',
    title: 'Erreur',
    headerVariant: 'danger',
    message: 'Une erreur est survenue.',
    confirmText: 'OK',
    confirmVariant: 'danger',
    showCancelButton: false,
  },

  // ========== MODALS D'INFORMATION ==========
  [MODAL_TYPES.INFO_STOCK_ALERT]: {
    variant: 'warning',
    icon: 'bi-bell-fill',
    title: 'Créer une alerte de disponibilité',
    message: 'Ce livre est actuellement en rupture de stock.',
    alert: {
      variant: 'warning',
      icon: 'bi-bell',
      text: (email) => `Vous recevrez un email à ${email} dès que ce livre sera de nouveau disponible.`,
    },
    subMessage: 'Vous pouvez annuler cette alerte à tout moment depuis votre profil.',
    subMessageVariant: 'muted',
    confirmText: 'Activer l\'alerte',
    confirmIcon: 'bi-bell',
    confirmVariant: 'warning',
    cancelText: 'Annuler',
    cancelIcon: 'bi-x-circle',
  },
};

/**
 * Récupère la configuration d'un modal selon son type
 * @param {string} type - Type de modal (voir MODAL_TYPES)
 * @param {object} params - Paramètres dynamiques pour les messages
 * @returns {object} Configuration du modal
 */
export const getModalConfig = (type, params = {}) => {
  const config = modalConfigs[type] || modalConfigs[MODAL_TYPES.SUCCESS_GENERIC];

  // Résoudre les fonctions dans les messages
  const resolvedConfig = { ...config };

  // Si un message personnalisé est fourni dans les params, l'utiliser
  if (params.message) {
    resolvedConfig.message = params.message;
  } else if (typeof config.message === 'function') {
    resolvedConfig.message = config.message(...Object.values(params));
  }

  // Si un titre personnalisé est fourni dans les params, l'utiliser
  if (params.title) {
    resolvedConfig.title = params.title;
  }

  if (config.alert && typeof config.alert.text === 'function') {
    resolvedConfig.alert = {
      ...config.alert,
      text: config.alert.text(...Object.values(params)),
    };
  }

  return resolvedConfig;
};

