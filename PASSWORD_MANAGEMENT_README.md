# Implémentation de la gestion du mot de passe - Partie 2.3 et 3

## Fonctionnalités implémentées

### 1. Classe PasswordHistoryTokenizer
**Fichier:** `src/main/java/fr/ensitech/ebooks/utils/PasswordHistoryTokenizer.java`

Cette classe utilitaire gère l'historique des 5 derniers mots de passe :
- **tokenize()** : Convertit une chaîne de mots de passe (séparés par des espaces) en liste
- **detokenize()** : Convertit une liste de mots de passe en chaîne
- **addPasswordToHistory()** : Ajoute un nouveau mot de passe à l'historique et maintient uniquement les 5 derniers

### 2. Mise à jour de l'entité User
**Fichier:** `src/main/java/fr/ensitech/ebooks/entity/User.java`

Ajout de 4 nouveaux champs :
- `passwordHistory` : Stocke les 5 derniers mots de passe hashés sous forme de chaîne (séparés par des espaces)
- `lastPasswordUpdateDate` : Date de dernière mise à jour du mot de passe
- `resetPasswordToken` : Token pour la réinitialisation du mot de passe
- `resetTokenExpiryDate` : Date d'expiration du token

### 3. Extension du service UserService
**Fichier:** `src/main/java/fr/ensitech/ebooks/service/UserService.java`

Nouvelles méthodes implémentées :

#### a) Mise à jour du mot de passe (utilisateur connecté)
- **updatePassword()** : Met à jour le mot de passe d'un utilisateur connecté
  - Vérifie l'ancien mot de passe
  - Vérifie la réponse à la question de sécurité
  - Vérifie la force du nouveau mot de passe
  - Vérifie la politique de rotation (12 semaines minimum entre 2 changements)
  - Vérifie que le nouveau mot de passe n'est pas dans l'historique des 5 derniers
  - Ajoute l'ancien mot de passe à l'historique

#### b) Réinitialisation du mot de passe (mot de passe oublié)
- **initiateForgotPassword()** : Génère un token de réinitialisation et l'envoie par email
- **validateResetToken()** : Vérifie la validité du token (existe et non expiré)
- **resetPassword()** : Réinitialise le mot de passe avec le token
  - Vérifie la validité du token
  - Vérifie la force du nouveau mot de passe
  - Vérifie que le nouveau mot de passe n'est pas dans l'historique
  - Met à jour le mot de passe et supprime le token

#### c) Gestion de la question de sécurité
- **getSecurityQuestionForUser()** : Récupère la question de sécurité d'un utilisateur
- **verifySecurityAnswer()** : Vérifie la réponse à la question de sécurité

### 4. Contrôleur PasswordController
**Fichier:** `src/main/java/fr/ensitech/ebooks/controller/PasswordController.java`

Nouveaux endpoints :

#### Mise à jour du mot de passe (utilisateur connecté)
- `GET /update-password` : Affiche le formulaire de mise à jour
- `POST /update-password` : Traite la mise à jour du mot de passe

#### Réinitialisation du mot de passe (mot de passe oublié)
- `GET /forgot-password` : Affiche le formulaire "mot de passe oublié"
- `POST /forgot-password` : Envoie l'email de réinitialisation
- `GET /reset-password?token=xxx` : Affiche le formulaire de réinitialisation
- `POST /reset-password` : Traite la réinitialisation du mot de passe

### 5. Vues HTML créées

#### update-password.html
Formulaire de mise à jour du mot de passe pour un utilisateur connecté :
- Ancien mot de passe
- Nouveau mot de passe
- Confirmation du nouveau mot de passe
- Question de sécurité (affichée)
- Réponse à la question de sécurité

#### forgot-password.html
Formulaire pour demander la réinitialisation du mot de passe :
- Adresse email

#### reset-password.html
Formulaire pour réinitialiser le mot de passe avec le token :
- Nouveau mot de passe
- Confirmation du nouveau mot de passe

### 6. Mise à jour de la configuration de sécurité
**Fichier:** `src/main/java/fr/ensitech/ebooks/securingweb/WebSecurityConfig.java`

Ajout des routes `/forgot-password` et `/reset-password` aux URL autorisées sans authentification.

## Politique de mot de passe

### Règles de force du mot de passe
- Minimum 12 caractères
- Au moins une lettre (majuscule ou minuscule)
- Au moins un chiffre
- Au moins un caractère spécial (@$!%*?&#)

### Règles de rotation
- **Expiration** : Les mots de passe expirent tous les 12 semaines (84 jours)
- **Historique** : Les 5 derniers mots de passe ne peuvent pas être réutilisés
- **Vérification de la question de sécurité** : Obligatoire pour changer le mot de passe

## Comment tester

### 1. Test de la mise à jour du mot de passe (utilisateur connecté)
1. Se connecter avec un compte existant
2. Accéder à `/update-password`
3. Remplir le formulaire :
   - Ancien mot de passe
   - Nouveau mot de passe (différent des 5 derniers)
   - Confirmation
   - Réponse à la question de sécurité
4. Valider le formulaire

**Scénarios de test :**
- ✅ Mise à jour réussie avec des informations correctes
- ❌ Ancien mot de passe incorrect
- ❌ Nouveau mot de passe ne respecte pas la politique
- ❌ Mots de passe de confirmation différents
- ❌ Réponse à la question de sécurité incorrecte
- ❌ Nouveau mot de passe déjà utilisé récemment (dans l'historique)
- ❌ Tentative de changement avant 12 semaines

### 2. Test de la réinitialisation du mot de passe
1. Cliquer sur "Mot de passe oublié ?" sur la page de connexion
2. Entrer l'adresse email
3. Vérifier l'email reçu
4. Cliquer sur le lien dans l'email
5. Remplir le formulaire avec le nouveau mot de passe
6. Valider

**Scénarios de test :**
- ✅ Réinitialisation réussie avec un token valide
- ❌ Email non enregistré
- ❌ Token invalide
- ❌ Token expiré (après 24h)
- ❌ Nouveau mot de passe ne respecte pas la politique
- ❌ Nouveau mot de passe déjà utilisé récemment

### 3. Vérification de l'historique des mots de passe
Après plusieurs changements de mot de passe :
1. Vérifier dans la base de données la colonne `password_history`
2. Elle devrait contenir jusqu'à 5 mots de passe hashés séparés par des espaces
3. Essayer de réutiliser un ancien mot de passe → doit être refusé

## Structure de données

### Colonne password_history
Format : `hash1 hash2 hash3 hash4 hash5`
- Chaque hash représente un ancien mot de passe
- Les hashes sont séparés par des espaces
- Maximum 5 hashes conservés
- Le plus récent est en première position

Exemple :
```
$2a$10$xxx... $2a$10$yyy... $2a$10$zzz...
```

## Flux de réinitialisation du mot de passe

```
Utilisateur                 Système                     Email
    |                          |                          |
    |--"Mot de passe oublié"-->|                          |
    |                          |--Génère token----------->|
    |                          |                          |
    |<---------------------Email avec lien---------------|
    |                          |                          |
    |--Clic sur le lien------->|                          |
    |<--Formulaire-------------|                          |
    |                          |                          |
    |--Nouveau mot de passe--->|                          |
    |                          |--Vérifie token           |
    |                          |--Vérifie politique       |
    |                          |--Vérifie historique      |
    |                          |--Met à jour              |
    |<--Confirmation-----------|                          |
```

## Sécurité

### Protections implémentées
- ✅ Tokens de réinitialisation expirables (24h)
- ✅ Tokens à usage unique (supprimés après utilisation)
- ✅ Hachage des mots de passe avec BCrypt
- ✅ Vérification de la question de sécurité pour la mise à jour
- ✅ Politique de rotation des mots de passe
- ✅ Historique des mots de passe pour éviter la réutilisation
- ✅ Validation de la force du mot de passe côté serveur

### Prochaines améliorations possibles
- [ ] Limiter le nombre de tentatives de réinitialisation
- [ ] Notification par email lors du changement de mot de passe
- [ ] Deux facteurs d'authentification pour la réinitialisation
- [ ] Blocage temporaire du compte après plusieurs tentatives échouées

