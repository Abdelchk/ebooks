# Guide de test - Gestion du mot de passe

## ✅ Compilation réussie
Le projet compile sans erreurs. 15 tests unitaires passent pour la classe PasswordHistoryTokenizer.

## 📋 Fonctionnalités implémentées

### 1. ✅ Mise à jour du mot de passe (utilisateur connecté)
- **Route** : `/update-password`
- **Accessible** : Depuis la navbar (lien "Changer mot de passe")
- **Formulaire** :
  - Ancien mot de passe
  - Nouveau mot de passe
  - Confirmation du nouveau mot de passe
  - Question de sécurité (affichée automatiquement)
  - Réponse à la question de sécurité

### 2. ✅ Réinitialisation du mot de passe (mot de passe oublié)
- **Route** : `/forgot-password`
- **Accessible** : Depuis la page de connexion (lien "Mot de passe oublié ?")
- **Flux** :
  1. Saisir l'adresse email
  2. Recevoir un email avec un lien de réinitialisation
  3. Cliquer sur le lien (valide 24h)
  4. Saisir le nouveau mot de passe
  5. Confirmation

### 3. ✅ Politique de rotation des mots de passe
- **Expiration** : Tous les 12 semaines (84 jours)
- **Historique** : Conservation des 5 derniers mots de passe
- **Vérification** : Interdiction de réutiliser un des 5 derniers mots de passe
- **Avertissement** : Alerte 7 jours avant l'expiration
- **Blocage** : Redirection forcée vers le changement de mot de passe après expiration

### 4. ✅ Classe PasswordHistoryTokenizer
- Gestion de l'historique sous forme de chaîne
- Séparation par espaces
- Maximum 5 mots de passe
- Tests unitaires complets (15 tests)

## 🧪 Scénarios de test

### Scénario 1 : Inscription d'un nouvel utilisateur
**Objectif** : Vérifier l'initialisation de la date de mise à jour du mot de passe

**Étapes** :
1. Accéder à `/register`
2. Remplir le formulaire d'inscription
3. Choisir une question de sécurité et y répondre
4. Valider
5. Vérifier l'email de confirmation
6. Activer le compte

**Résultat attendu** :
- Compte créé avec succès
- `lastPasswordUpdateDate` initialisé à la date d'inscription
- `passwordHistory` vide

### Scénario 2 : Mise à jour du mot de passe (cas normal)
**Objectif** : Changer le mot de passe avec succès

**Étapes** :
1. Se connecter avec un compte actif
2. Cliquer sur "Changer mot de passe" dans la navbar
3. Remplir le formulaire :
   - Ancien mot de passe : [mot de passe actuel]
   - Nouveau mot de passe : `NouveauMotDePasse123@`
   - Confirmation : `NouveauMotDePasse123@`
   - Réponse à la question de sécurité : [réponse correcte]
4. Valider

**Résultat attendu** :
- ✅ Message de succès
- ✅ Redirection vers `/accueil`
- ✅ Ancien mot de passe ajouté à `passwordHistory`
- ✅ `lastPasswordUpdateDate` mis à jour

### Scénario 3 : Tentative de réutilisation d'un ancien mot de passe
**Objectif** : Vérifier que l'historique empêche la réutilisation

**Pré-requis** : Avoir changé son mot de passe au moins une fois

**Étapes** :
1. Se connecter
2. Accéder à `/update-password`
3. Tenter de mettre un mot de passe déjà utilisé récemment
4. Valider

**Résultat attendu** :
- ❌ Message d'erreur : "Ce mot de passe a déjà été utilisé récemment. Veuillez en choisir un autre."
- ❌ Mot de passe non modifié

### Scénario 4 : Réponse incorrecte à la question de sécurité
**Objectif** : Vérifier la protection par question de sécurité

**Étapes** :
1. Se connecter
2. Accéder à `/update-password`
3. Remplir le formulaire avec une mauvaise réponse à la question de sécurité
4. Valider

**Résultat attendu** :
- ❌ Message d'erreur : "La réponse à la question de sécurité est incorrecte"
- ❌ Mot de passe non modifié

### Scénario 5 : Mot de passe oublié - Flux complet
**Objectif** : Tester la réinitialisation par email

**Étapes** :
1. Sur la page de connexion, cliquer sur "Mot de passe oublié ?"
2. Saisir son email : `user@example.com`
3. Valider
4. Vérifier l'email reçu
5. Cliquer sur le lien de réinitialisation
6. Saisir un nouveau mot de passe : `NouveauSecure2025@`
7. Confirmer le mot de passe
8. Valider

**Résultat attendu** :
- ✅ Email reçu avec le lien
- ✅ Formulaire de réinitialisation affiché
- ✅ Mot de passe changé avec succès
- ✅ Token supprimé après utilisation
- ✅ Possibilité de se connecter avec le nouveau mot de passe

### Scénario 6 : Token de réinitialisation expiré
**Objectif** : Vérifier l'expiration du token après 24h

**Étapes** :
1. Demander une réinitialisation de mot de passe
2. Attendre 24h (ou modifier manuellement la date en BDD)
3. Essayer d'utiliser le lien

**Résultat attendu** :
- ❌ Message d'erreur : "Le lien de réinitialisation est invalide ou a expiré."
- ❌ Redirection vers `/login`

### Scénario 7 : Avertissement d'expiration imminente
**Objectif** : Vérifier l'alerte 7 jours avant expiration

**Pré-requis** : Avoir un compte avec mot de passe datant de 77 à 83 jours

**Étapes** :
1. Se connecter avec le compte
2. Naviguer sur n'importe quelle page

**Résultat attendu** :
- ⚠️ Bandeau d'avertissement : "Attention : Votre mot de passe expire dans X jour(s)."
- ✅ Accès normal au système

### Scénario 8 : Mot de passe expiré
**Objectif** : Vérifier le blocage après 84 jours

**Pré-requis** : Avoir un compte avec mot de passe datant de 84 jours ou plus

**Étapes** :
1. Se connecter avec le compte
2. Essayer d'accéder à n'importe quelle page

**Résultat attendu** :
- 🚫 Redirection automatique vers `/update-password`
- ❌ Message d'erreur : "Votre mot de passe a expiré. Vous devez le changer pour continuer."
- 🔒 Impossibilité d'accéder aux autres pages tant que le mot de passe n'est pas changé

### Scénario 9 : Validation de la force du mot de passe
**Objectif** : Vérifier les règles de complexité

**Mots de passe à tester** :
- ❌ `court` → Trop court (< 12 caractères)
- ❌ `unseulmotdepasse` → Pas de chiffre ni caractère spécial
- ❌ `Motdepasse123` → Pas de caractère spécial
- ❌ `MotDePasse@@@` → Pas de chiffre
- ✅ `MotDeP@sse123` → Valide (12+ caractères, lettre, chiffre, spécial)

### Scénario 10 : Historique de 5 mots de passe
**Objectif** : Vérifier la limite de 5 mots de passe

**Étapes** :
1. Changer le mot de passe 6 fois de suite
2. Vérifier la colonne `passwordHistory` en BDD
3. Essayer de réutiliser le 6ème mot de passe (le plus ancien)

**Résultat attendu** :
- ✅ `passwordHistory` contient exactement 5 mots de passe
- ✅ Le 6ème mot de passe (le plus ancien) a été supprimé
- ✅ Possibilité de réutiliser le 6ème mot de passe (car plus dans l'historique)

## 📊 Vérifications en base de données

### Table User - Nouvelles colonnes
```sql
SELECT 
    id, 
    email, 
    password_history,
    last_password_update_date,
    reset_password_token,
    reset_token_expiry_date
FROM user
WHERE email = 'test@example.com';
```

**Vérifications** :
- `password_history` : Chaîne de hashes séparés par des espaces (max 5)
- `last_password_update_date` : Date de dernière mise à jour
- `reset_password_token` : NULL ou UUID si réinitialisation en cours
- `reset_token_expiry_date` : NULL ou date d'expiration du token

### Exemple de password_history
```
$2a$10$abc... $2a$10$def... $2a$10$ghi... $2a$10$jkl... $2a$10$mno...
```

## 🔧 Configuration

### application.properties
```properties
# Configuration email (déjà en place)
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=noreplytestebooks@gmail.com
spring.mail.password=nfjncbhdmvopwaqi
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

## 📝 Points importants

### Sécurité
- ✅ Les mots de passe sont hashés avec BCrypt
- ✅ Les réponses aux questions de sécurité sont hashées
- ✅ Les tokens de réinitialisation expirent après 24h
- ✅ Les tokens sont à usage unique (supprimés après utilisation)
- ✅ Vérification de la question de sécurité obligatoire pour la mise à jour

### Politique de rotation
- ✅ Expiration tous les 84 jours
- ✅ Avertissement 7 jours avant
- ✅ Blocage après expiration
- ✅ Historique de 5 mots de passe
- ✅ Interdiction de réutilisation

### Navigation
- ✅ Lien "Changer mot de passe" dans la navbar (utilisateur connecté)
- ✅ Lien "Mot de passe oublié ?" sur la page de connexion
- ✅ Intercepteur pour vérifier l'expiration à chaque requête

## 🎯 URLs importantes

| URL | Description | Authentification requise |
|-----|-------------|--------------------------|
| `/update-password` | Changement de mot de passe | ✅ Oui |
| `/forgot-password` | Demande de réinitialisation | ❌ Non |
| `/reset-password?token=xxx` | Réinitialisation avec token | ❌ Non |

## 🚀 Pour tester en développement

### 1. Simuler l'expiration d'un mot de passe
```sql
-- Modifier la date pour simuler un mot de passe ancien
UPDATE user 
SET last_password_update_date = DATE_SUB(NOW(), INTERVAL 85 DAY)
WHERE email = 'test@example.com';
```

### 2. Simuler un avertissement d'expiration
```sql
-- Modifier la date pour simuler un mot de passe qui va expirer
UPDATE user 
SET last_password_update_date = DATE_SUB(NOW(), INTERVAL 80 DAY)
WHERE email = 'test@example.com';
```

### 3. Vérifier l'historique des mots de passe
```sql
-- Voir l'historique
SELECT email, password_history
FROM user
WHERE email = 'test@example.com';

-- Compter le nombre de mots de passe dans l'historique
SELECT 
    email, 
    (LENGTH(password_history) - LENGTH(REPLACE(password_history, ' ', '')) + 1) as nb_passwords
FROM user
WHERE email = 'test@example.com' 
AND password_history IS NOT NULL 
AND password_history != '';
```

## 📦 Livrables

### Fichiers créés
1. ✅ `PasswordHistoryTokenizer.java` - Gestion de l'historique
2. ✅ `PasswordController.java` - Contrôleur pour les endpoints
3. ✅ `PasswordExpirationInterceptor.java` - Vérification de l'expiration
4. ✅ `update-password.html` - Formulaire de mise à jour
5. ✅ `forgot-password.html` - Formulaire "mot de passe oublié"
6. ✅ `reset-password.html` - Formulaire de réinitialisation
7. ✅ `PasswordHistoryTokenizerTest.java` - Tests unitaires (15 tests)

### Fichiers modifiés
1. ✅ `User.java` - Ajout des colonnes
2. ✅ `IUserService.java` - Ajout des méthodes
3. ✅ `UserService.java` - Implémentation des méthodes
4. ✅ `IUserRepository.java` - Ajout de findByResetPasswordToken
5. ✅ `IUserSecurityAnswerRepository.java` - Ajout de findByUser
6. ✅ `WebSecurityConfig.java` - Ajout des routes autorisées
7. ✅ `MvcConfig.java` - Ajout de l'intercepteur
8. ✅ `navbar.html` - Ajout du lien "Changer mot de passe"
9. ✅ `login.html` - Ajout du lien "Mot de passe oublié ?"

## ✨ Résumé

Toutes les fonctionnalités demandées ont été implémentées avec succès :

✅ **Partie 2.3** : Mise à jour du mot de passe via question secrète
✅ **Partie 3** : Réinitialisation par email en cas d'oubli
✅ **Politique de rotation** : Expiration à 84 jours avec historique de 5 mots de passe
✅ **Classe Tokenizer** : Gestion de l'historique sous forme de chaîne
✅ **Tests unitaires** : 15 tests passent avec succès
✅ **Compilation** : Aucune erreur de compilation

Le système est prêt à être testé et déployé ! 🎉

