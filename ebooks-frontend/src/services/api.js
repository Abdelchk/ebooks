import axios from 'axios';

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080';
const FRONTEND_VERSION = process.env.REACT_APP_VERSION || 'dev';

// Configuration globale d'axios pour inclure les credentials (cookie de session)
axios.defaults.withCredentials = true;

const api = axios.create({
  baseURL: API_BASE_URL,
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json',
    'X-Frontend-Version': FRONTEND_VERSION,
  },
});

// ─── Gestion du jeton CSRF cross-origin ───────────────────────────────────────
//
// Problème : le frontend Vercel (https://*.vercel.app) et le backend sont sur deux
// domaines distincts. La Same-Origin Policy empêche JavaScript de lire le cookie
// XSRF-TOKEN posé par le backend sur son propre domaine.
// Mécanisme standard Axios (xsrfCookieName) → inopérant en cross-origin.
//
// Solution : on récupère le token CSRF via un endpoint dédié (GET /api/auth/csrf)
// qui retourne la valeur dans le corps JSON. On stocke ce token en mémoire et on
// l'injecte manuellement dans l'en-tête X-XSRF-TOKEN sur toutes les requêtes
// qui modifient l'état (POST, PUT, DELETE, PATCH).
// ─────────────────────────────────────────────────────────────────────────────
let csrfTokenCache = null;
let csrfFetchPromise = null; // évite plusieurs requêtes simultanées

const fetchCsrfToken = async () => {
  // Retourne le token mis en cache s'il est encore valide
  if (csrfTokenCache) return csrfTokenCache;

  // Déduplique les appels simultanés (ex: plusieurs POST lancés en même temps)
  if (!csrfFetchPromise) {
    csrfFetchPromise = api.get('/api/auth/csrf')
      .then(res => {
        csrfTokenCache = res.data.token;
        return csrfTokenCache;
      })
      .finally(() => {
        csrfFetchPromise = null;
      });
  }

  return csrfFetchPromise;
};

/** Invalide le cache CSRF (à appeler après login/logout car la session change). */
export const invalidateCsrfToken = () => {
  csrfTokenCache = null;
};

// ─── Intercepteur de requête : injection du token CSRF ───────────────────────
const STATE_CHANGING_METHODS = new Set(['post', 'put', 'delete', 'patch']);

api.interceptors.request.use(async (config) => {
  if (STATE_CHANGING_METHODS.has(config.method?.toLowerCase())) {
    // Ne pas récupérer le CSRF pour l'appel au CSRF lui-même (évite récursion)
    if (!config.url?.includes('/api/auth/csrf')) {
      const token = await fetchCsrfToken();
      if (token) {
        config.headers['X-XSRF-TOKEN'] = token;
      }
    }
  }
  return config;
}, (error) => Promise.reject(error));

// ─── Intercepteur de réponse : gestion des erreurs globales ──────────────────
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 403) {
      // Token CSRF expiré ou invalide (rotation après login/logout) → on l'invalide
      // pour forcer un nouveau fetch lors du prochain appel.
      invalidateCsrfToken();
    }
    if (error.response?.status === 401) {
      // Session expirée ou non authentifié
      console.warn('Session expirée ou non authentifié');
    }
    return Promise.reject(error);
  }
);

export default api;
