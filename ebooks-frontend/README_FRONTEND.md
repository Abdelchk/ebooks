# Ebooks Frontend - Application React

Application React pour la gestion de livres électroniques avec authentification sécurisée.

## 🚀 Démarrage rapide

```bash
npm install
npm start
```

L'application sera accessible sur **http://localhost:3000**

⚠️ **Important** : Le backend Spring Boot doit être démarré sur le port 8080.

---

## 📋 Prérequis

- Node.js 14+
- npm 6+
- Backend Spring Boot en cours d'exécution sur http://localhost:8080

---

## 🏗️ Structure du projet

```
src/
├── components/          # Composants réutilisables
│   ├── Navbar.js
│   ├── PrivateRoute.js
│   ├── Loader.js
│   ├── PasswordStrengthIndicator.js
│   └── ErrorBoundary.js
├── context/            # Contextes React
│   └── AuthContext.js
├── pages/              # Pages de l'application
│   ├── Index.js
│   ├── Login.js
│   ├── Register.js
│   ├── Accueil.js
│   ├── ForgotPassword.js
│   ├── ResetPassword.js
│   ├── UpdatePassword.js
│   ├── VerifyEmail.js
│   ├── VerifyCode.js
│   ├── LastStep.js
│   └── About.js
├── services/           # Services API
│   ├── api.js
│   ├── authService.js
│   ├── bookService.js
│   └── userService.js
├── utils/              # Utilitaires
│   ├── validation.js
│   ├── messages.js
│   └── validation.test.js
├── hooks/              # Hooks personnalisés
│   └── useForm.js
├── config/             # Configuration
│   └── constants.js
├── App.js              # Configuration des routes
├── App.css             # Styles personnalisés
└── index.js            # Point d'entrée
```

---

## ⚙️ Scripts disponibles

### `npm start`
Lance l'application en mode développement.
Ouvre automatiquement http://localhost:3000.
La page se recharge automatiquement lors des modifications.

### `npm test`
Lance les tests en mode interactif.

### `npm run build`
Compile l'application pour la production dans le dossier `build/`.
Optimise et minifie le code pour les meilleures performances.

### `npm run eject`
⚠️ **Opération irréversible !** Éjecte la configuration CRA.

---

## 🔐 Fonctionnalités

- ✅ Authentification sécurisée (login/logout)
- ✅ Inscription avec reCAPTCHA Enterprise
- ✅ Vérification email obligatoire
- ✅ Double authentification (2FA)
- ✅ Mot de passe oublié/Réinitialisation
- ✅ Mise à jour du mot de passe
- ✅ Questions de sécurité
- ✅ Routes protégées
- ✅ Liste des livres
- ✅ Validation des formulaires en temps réel

---

## 🔌 APIs utilisées

### Authentification (`/api/auth`)
- POST `/api/auth/login` - Connexion
- POST `/api/auth/logout` - Déconnexion
- POST `/api/auth/register` - Inscription
- GET `/api/auth/check` - Vérifier l'authentification
- GET `/api/auth/security-questions` - Questions de sécurité
- GET `/api/auth/recaptcha-key` - Clé reCAPTCHA
- POST `/api/auth/forgot-password` - Mot de passe oublié
- POST `/api/auth/reset-password` - Réinitialiser
- GET `/api/auth/verify-email` - Vérifier email
- POST `/api/auth/verify-code` - Vérifier code 2FA

### Utilisateur (`/api/rest/users`)
- GET `/api/rest/users/me` - Utilisateur connecté
- PUT `/api/rest/users/update` - Mettre à jour
- PUT `/api/rest/users/change-password` - Changer le mot de passe
- DELETE `/api/rest/users/delete/{id}` - Supprimer

### Livres (`/api/rest/books`)
- GET `/api/rest/books/all` - Tous les livres
- GET `/api/rest/books/{id}` - Livre par ID
- POST `/api/rest/books/create` - Créer un livre
- DELETE `/api/rest/books/remove/{id}` - Supprimer

---

## 🔧 Configuration

### Variables d'environnement (`.env`)
```env
REACT_APP_API_URL=http://localhost:8080
```

### Proxy (dans `package.json`)
```json
"proxy": "http://localhost:8080"
```

---

## 🎨 Technologies utilisées

- **React** - Framework UI
- **React Router DOM** - Routing
- **Axios** - Client HTTP
- **Bootstrap 5** - Framework CSS
- **React Bootstrap** - Composants Bootstrap pour React
- **Bootstrap Icons** - Bibliothèque d'icônes

---

## 🧪 Tests

```bash
# Lancer les tests
npm test

# Lancer avec coverage
npm test -- --coverage

# Lancer en mode watch
npm test -- --watch
```

---

## 📦 Build pour production

```bash
npm run build
```

Les fichiers optimisés seront dans le dossier `build/`.

Pour servir la build localement :
```bash
npm install -g serve
serve -s build
```

---

## 🐛 Dépannage

### L'application ne démarre pas
```bash
rm -rf node_modules package-lock.json
npm install
npm start
```

### Erreur CORS
- Vérifier que le backend est démarré sur le port 8080
- Vérifier la configuration CORS dans `WebSecurityConfig.java`
- Vérifier que le proxy est configuré dans `package.json`

### Session non persistée
- Vérifier que `withCredentials: true` est dans `api.js`
- Vérifier que les cookies sont acceptés par le navigateur
- Vérifier la configuration CORS `allowCredentials: true`

---

## 📚 Documentation

Consultez les fichiers suivants dans le dossier parent :
- **START.md** - Démarrage en 30 secondes
- **README_REACT_FINAL.md** - Résumé complet
- **API_DOCUMENTATION.md** - Documentation des APIs
- **EXEMPLES_UTILISATION.md** - Exemples de code

---

## 🎯 Développement

### Ajouter une nouvelle page
1. Créer le composant dans `src/pages/NomPage.js`
2. Ajouter la route dans `src/App.js`
3. Importer le composant

### Ajouter un nouveau service API
1. Créer les fonctions dans `src/services/nomService.js`
2. Utiliser le client `api` depuis `src/services/api.js`
3. Importer et utiliser dans vos composants

### Utiliser l'authentification
```javascript
import { useAuth } from './context/AuthContext';

const MyComponent = () => {
  const { user, login, logout } = useAuth();
  
  if (!user) {
    return <div>Non connecté</div>;
  }
  
  return <div>Bonjour {user.firstname} !</div>;
};
```

---

## 🔒 Sécurité

- Toutes les routes sensibles nécessitent une authentification
- Les credentials (cookies) sont automatiquement inclus dans les requêtes
- Les mots de passe ne sont jamais stockés côté client
- reCAPTCHA Enterprise protège l'inscription
- Validation des formulaires côté client ET serveur

---

## 💡 Conseils

- Utilisez React DevTools pour déboguer
- Consultez la console du navigateur (F12) en cas d'erreur
- Testez les APIs avec Postman si besoin
- Gardez le backend et le frontend en cours d'exécution simultanément

---

## 📞 Support

En cas de problème :
1. Vérifiez les logs du backend
2. Vérifiez la console du navigateur (F12)
3. Consultez la documentation dans le dossier parent
4. Vérifiez que les deux applications sont démarrées

---

## ✨ Prochaines étapes

1. Personnaliser le design
2. Ajouter des tests unitaires
3. Implémenter le CRUD complet des livres
4. Ajouter la recherche et les filtres
5. Optimiser les performances

---

**Bon développement avec React ! 🚀**

