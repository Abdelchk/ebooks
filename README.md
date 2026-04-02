# 📚 Application ebooks - Gestion de bibliothèque en ligne

## 🎯 Description

Application web moderne de gestion de bibliothèque avec :
- ✅ Authentification sécurisée (Argon2)
- ✅ Authentification 2FA par email
- ✅ Gestion complète des mots de passe
- ✅ Protection reCAPTCHA Google
- ✅ Interface React moderne
- ✅ API REST Spring Boot

---

## 🏗️ Architecture

### Frontend
- **Framework :** React 18
- **Routage :** React Router v6
- **UI :** Bootstrap 5
- **HTTP Client :** Axios
- **Port :** 3000

### Backend
- **Framework :** Spring Boot 3.x
- **Sécurité :** Spring Security + Argon2
- **Base de données :** PostgreSQL/H2
- **Email :** JavaMailSender
- **reCAPTCHA :** Google Enterprise
- **Port :** 8080

---

## 🚀 Démarrage rapide

### Prérequis

- Java 17+
- Node.js 16+
- Maven (inclus via wrapper)
- PostgreSQL (optionnel, H2 pour les tests)

### Option 1 : Script automatique (recommandé)

```powershell
.\start-full-app.ps1
```

### Option 2 : Démarrage manuel

#### 1. Backend
```powershell
.\mvnw.cmd spring-boot:run
```

#### 2. Frontend
```powershell
cd ebooks-frontend
npm install  # Première fois seulement
npm start
```

### Accès

- **Frontend :** http://localhost:3000
- **Backend API :** http://localhost:8080/api/...

---

## 📖 Fonctionnalités

### Authentification
- ✅ Inscription avec validation email
- ✅ Connexion avec authentification 2FA
- ✅ Questions de sécurité
- ✅ Protection reCAPTCHA

### Gestion mot de passe
- ✅ Politique de mot de passe stricte (12 caractères min)
- ✅ Hashage Argon2
- ✅ Historique des 5 derniers mots de passe
- ✅ Expiration après 12 semaines
- ✅ Mot de passe oublié avec email
- ✅ Réinitialisation sécurisée

### Catalogue
- ✅ Liste des livres
- ✅ Recherche et filtres (à venir)
- ✅ Détails des livres (à venir)

---

## 📁 Structure du projet

```
ebooks/
├── src/main/java/fr/ensitech/ebooks/
│   ├── controller/         # Controllers REST
│   │   ├── AuthRestController.java      # APIs authentification
│   │   ├── BookRestController.java      # APIs livres
│   │   ├── UserController.java          # APIs utilisateur
│   │   └── RootController.java          # Redirection racine
│   ├── service/           # Services métier
│   ├── repository/        # Repositories JPA
│   ├── entity/            # Entités JPA
│   ├── securingweb/       # Configuration sécurité
│   └── strategy/          # Pattern Strategy (emails)
│
├── ebooks-frontend/       # Application React
│   ├── src/
│   │   ├── components/    # Composants réutilisables
│   │   ├── pages/         # Pages de l'application
│   │   ├── services/      # Services API
│   │   └── context/       # Context React (Auth)
│   └── public/
│
├── *.md                   # Documentation (ignorée par git)
└── start-full-app.ps1     # Script de démarrage
```

---

## 🔗 Endpoints API

### Publics (pas d'authentification)

```
POST /api/auth/login              # Connexion
POST /api/auth/register           # Inscription
POST /api/auth/forgot-password    # Mot de passe oublié
POST /api/auth/reset-password     # Réinitialiser
GET  /api/auth/verify-email       # Vérifier email
GET  /api/auth/security-questions # Questions de sécurité
GET  /api/rest/books/all          # Liste des livres
```

### Protégés (authentification requise)

```
GET  /api/auth/check              # Vérifier session
POST /api/auth/logout             # Déconnexion
POST /api/auth/verify-code        # Code 2FA
POST /api/auth/update-password    # MAJ mot de passe
PUT  /api/rest/users/update       # MAJ profil
```

**Documentation complète :** Voir `GUIDE_TEST_API_REST.md`

---

## 📚 Documentation

| Fichier | Description |
|---------|-------------|
| **DEMARRAGE_APPLICATION.md** | 🚀 Guide de démarrage rapide |
| **GUIDE_DEPANNAGE.md** | 🔧 Résolution des problèmes courants |
| **GUIDE_TEST_API_REST.md** | 🧪 Tests Postman/curl |
| **MIGRATION_FINALE_API_REST.md** | 📋 Documentation technique |
| **CHECKLIST_FINALE.md** | ✅ Vérifications de migration |

---

## 🔒 Sécurité

### Hashage mot de passe
- **Algorithme :** Argon2 (recommandé par OWASP)
- **Paramètres :** Salt length 16, Hash length 32, 1 iteration, 4096 KB memory, 3 parallelism

### Protection
- ✅ CSRF désactivé (API REST)
- ✅ CORS configuré (localhost:3000 uniquement)
- ✅ Sessions Spring Security
- ✅ Tokens de vérification UUID
- ✅ reCAPTCHA Enterprise

---

## 🧪 Tests

### Tests unitaires
```powershell
.\mvnw.cmd test
```

### Tests d'intégration
```powershell
.\mvnw.cmd verify
```

### Exemple de test
Voir `src/test/java/fr/ensitech/ebooks/service/` pour des exemples avec Mockito.

---

## 📦 Build production

### Backend
```powershell
.\mvnw.cmd clean package
# JAR généré dans : target/ebooks-0.0.1-SNAPSHOT.jar
```

### Frontend
```powershell
cd ebooks-frontend
npm run build
# Build généré dans : build/
```

---

## 🌐 Déploiement

### Option 1 : Serveurs séparés
- Frontend sur Vercel/Netlify
- Backend sur Heroku/AWS

### Option 2 : Servir React depuis Spring Boot
Copier le contenu de `ebooks-frontend/build/` dans `src/main/resources/static/`

---

## 🤝 Contribution

### Code style
- Backend : Google Java Style
- Frontend : Prettier + ESLint

### Git workflow
```bash
git checkout -b feature/ma-fonctionnalite
git commit -m "feat: Description"
git push origin feature/ma-fonctionnalite
```

---

## 📝 Notes importantes

### Migration Thymeleaf → React

Cette application a été migrée d'une architecture monolithique (Thymeleaf) vers une SPA (React + API REST).

**Changements majeurs :**
- ❌ Plus de templates Thymeleaf
- ✅ Toutes les vues sont en React
- ✅ Communication via API REST JSON
- ✅ CORS configuré

**Documentation :** Voir `MIGRATION_API_REST_COMPLETE.md`

---

## 🐛 Problèmes connus

### "forgot-password ne fonctionnait pas"
**Résolu ✅** - Les anciens controllers Thymeleaf ont été désactivés. Tout passe maintenant par l'API REST.

**Voir :** `RESOLUTION_FINALE_COMPLETE.md`

---

## 📞 Support

En cas de problème :
1. Consulter `GUIDE_DEPANNAGE.md`
2. Vérifier les logs (backend + console navigateur)
3. Tester les APIs avec Postman (voir `GUIDE_TEST_API_REST.md`)

---

## 📜 Licence

Propriétaire - ENSITECH

---

## 👥 Auteurs

Projet développé dans le cadre d'un cours d'ingénierie logicielle.

---

## ✨ Version actuelle

**Version :** 1.0.0 (Migration API REST complète)
**Date :** Mars 2026

**Dernières modifications :**
- ✅ Migration complète vers architecture React + API REST
- ✅ Désactivation de tous les controllers Thymeleaf
- ✅ Résolution du problème forgot-password
- ✅ Ajout endpoint update-password dans AuthRestController

---

**🎉 L'application est prête à être utilisée ! Consultez `DEMARRAGE_APPLICATION.md` pour commencer.**

