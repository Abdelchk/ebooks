import React from 'react';
import { Container, Card, Accordion } from 'react-bootstrap';
import Navigation from '../components/Navbar';

const PrivacyPolicy = () => {
  return (
    <>
      <Navigation />
      <Container className="mt-5 mb-5">
        <h1 className="mb-4">
          <i className="bi bi-shield-lock"></i> Politique de Confidentialité et RGPD
        </h1>

        <Card className="mb-4">
          <Card.Body>
            <Card.Text className="text-muted">
              <strong>Dernière mise à jour :</strong> {new Date().toLocaleDateString('fr-FR')}
            </Card.Text>
            <p>
              Chez <strong>Ebooks</strong>, nous accordons une importance primordiale à la protection
              de vos données personnelles. Cette politique de confidentialité décrit comment nous
              collectons, utilisons et protégeons vos informations conformément au Règlement Général
              sur la Protection des Données (RGPD).
            </p>
          </Card.Body>
        </Card>

        <Accordion defaultActiveKey="0">
          {/* Section 1 : Responsable du traitement */}
          <Accordion.Item eventKey="0">
            <Accordion.Header>
              <i className="bi bi-building me-2"></i>
              1. Responsable du traitement des données
            </Accordion.Header>
            <Accordion.Body>
              <p>
                Le responsable du traitement des données personnelles est :
              </p>
              <ul>
                <li><strong>Nom :</strong> Ebooks</li>
                <li><strong>Adresse :</strong> 49 rue du petit albi</li>
                <li><strong>Email :</strong> contact@ebooks.fr</li>
                <li><strong>Téléphone :</strong> 0630658720</li>
              </ul>
            </Accordion.Body>
          </Accordion.Item>

          {/* Section 2 : Données collectées */}
          <Accordion.Item eventKey="1">
            <Accordion.Header>
              <i className="bi bi-clipboard-data me-2"></i>
              2. Données personnelles collectées
            </Accordion.Header>
            <Accordion.Body>
              <p>Nous collectons les données suivantes lors de votre inscription :</p>
              <ul>
                <li><strong>Données d'identification :</strong> Nom, prénom, date de naissance</li>
                <li><strong>Données de contact :</strong> Email, numéro de téléphone</li>
                <li><strong>Données de connexion :</strong> Mot de passe (chiffré avec Argon2), question et réponse de sécurité</li>
                <li><strong>Données de navigation :</strong> Adresse IP, cookies</li>
                <li><strong>Données d'utilisation :</strong> Historique des emprunts, réservations, préférences</li>
              </ul>
            </Accordion.Body>
          </Accordion.Item>

          {/* Section 3 : Finalités du traitement */}
          <Accordion.Item eventKey="2">
            <Accordion.Header>
              <i className="bi bi-bullseye me-2"></i>
              3. Finalités du traitement
            </Accordion.Header>
            <Accordion.Body>
              <p>Vos données sont collectées pour les finalités suivantes :</p>
              <ul>
                <li>Création et gestion de votre compte utilisateur</li>
                <li>Authentification et sécurisation de l'accès (2FA, reCAPTCHA)</li>
                <li>Gestion des emprunts, réservations et alertes de livres</li>
                <li>Envoi d'emails de notification (confirmation, rappels, alertes)</li>
                <li>Amélioration de nos services et de l'expérience utilisateur</li>
                <li>Respect de nos obligations légales</li>
              </ul>
            </Accordion.Body>
          </Accordion.Item>

          {/* Section 4 : Base légale */}
          <Accordion.Item eventKey="3">
            <Accordion.Header>
              <i className="bi bi-file-text me-2"></i>
              4. Base légale du traitement
            </Accordion.Header>
            <Accordion.Body>
              <p>Le traitement de vos données repose sur :</p>
              <ul>
                <li><strong>Votre consentement</strong> pour l'utilisation des services</li>
                <li><strong>L'exécution d'un contrat</strong> (CGU acceptées lors de l'inscription)</li>
                <li><strong>Le respect d'obligations légales</strong> (conservation des données de connexion)</li>
                <li><strong>L'intérêt légitime</strong> pour la sécurité et la prévention de la fraude</li>
              </ul>
            </Accordion.Body>
          </Accordion.Item>

          {/* Section 5 : Durée de conservation */}
          <Accordion.Item eventKey="4">
            <Accordion.Header>
              <i className="bi bi-clock-history me-2"></i>
              5. Durée de conservation des données
            </Accordion.Header>
            <Accordion.Body>
              <ul>
                <li><strong>Compte actif :</strong> Tant que votre compte est actif</li>
                <li><strong>Compte inactif :</strong> 3 ans après la dernière connexion</li>
                <li><strong>Données de connexion :</strong> 1 an (obligation légale)</li>
                <li><strong>Historique des emprunts :</strong> 5 ans à des fins statistiques</li>
                <li><strong>Après suppression :</strong> 30 jours en archive de sécurité</li>
              </ul>
            </Accordion.Body>
          </Accordion.Item>

          {/* Section 6 : Vos droits */}
          <Accordion.Item eventKey="5">
            <Accordion.Header>
              <i className="bi bi-person-check me-2"></i>
              6. Vos droits RGPD
            </Accordion.Header>
            <Accordion.Body>
              <p>Conformément au RGPD, vous disposez des droits suivants :</p>
              <ul>
                <li>
                  <strong>Droit d'accès :</strong> Obtenir une copie de vos données personnelles
                </li>
                <li>
                  <strong>Droit de rectification :</strong> Corriger vos données inexactes ou incomplètes
                </li>
                <li>
                  <strong>Droit à l'effacement :</strong> Demander la suppression de vos données ("droit à l'oubli")
                </li>
                <li>
                  <strong>Droit à la limitation :</strong> Limiter le traitement de vos données
                </li>
                <li>
                  <strong>Droit à la portabilité :</strong> Récupérer vos données dans un format structuré
                </li>
                <li>
                  <strong>Droit d'opposition :</strong> Vous opposer au traitement de vos données
                </li>
                <li>
                  <strong>Droit de retirer votre consentement :</strong> À tout moment
                </li>
              </ul>
              <p className="mt-3">
                <strong>Comment exercer vos droits ?</strong><br />
                Envoyez un email à : <a href="mailto:dpo@ebooks.fr">dpo@ebooks.fr</a> ou
                via votre espace "Profil" sur le site.
              </p>
            </Accordion.Body>
          </Accordion.Item>

          {/* Section 7 : Sécurité */}
          <Accordion.Item eventKey="6">
            <Accordion.Header>
              <i className="bi bi-shield-check me-2"></i>
              7. Sécurité des données
            </Accordion.Header>
            <Accordion.Body>
              <p>Nous mettons en œuvre les mesures de sécurité suivantes :</p>
              <ul>
                <li><strong>Chiffrement :</strong> Mots de passe chiffrés avec Argon2 (algorithme recommandé)</li>
                <li><strong>HTTPS :</strong> Toutes les communications sont sécurisées en SSL/TLS</li>
                <li><strong>2FA :</strong> Double authentification par email</li>
                <li><strong>reCAPTCHA Enterprise :</strong> Protection contre les bots et la fraude</li>
                <li><strong>Contrôles d'accès :</strong> Accès limité aux données par le personnel autorisé</li>
                <li><strong>Audits réguliers :</strong> Contrôles de sécurité et mises à jour</li>
                <li><strong>Sauvegarde :</strong> Sauvegardes régulières et chiffrées</li>
              </ul>
            </Accordion.Body>
          </Accordion.Item>

          {/* Section 8 : Partage des données */}
          <Accordion.Item eventKey="7">
            <Accordion.Header>
              <i className="bi bi-share me-2"></i>
              8. Partage des données avec des tiers
            </Accordion.Header>
            <Accordion.Body>
              <p>Nous ne vendons jamais vos données. Vos données peuvent être partagées avec :</p>
              <ul>
                <li><strong>Google reCAPTCHA :</strong> Pour la vérification anti-spam (données anonymisées)</li>
                <li><strong>Services d'emailing :</strong> Pour l'envoi d'emails de notification</li>
                <li><strong>Hébergeur :</strong> Pour le stockage sécurisé des données</li>
                <li><strong>Autorités légales :</strong> En cas d'obligation légale</li>
              </ul>
              <p>
                Tous nos sous-traitants sont soumis à des obligations contractuelles
                strictes de protection des données conformes au RGPD.
              </p>
            </Accordion.Body>
          </Accordion.Item>

          {/* Section 9 : Cookies */}
          <Accordion.Item eventKey="8">
            <Accordion.Header>
              <i className="bi bi-cookie me-2"></i>
              9. Cookies et technologies similaires
            </Accordion.Header>
            <Accordion.Body>
              <p>Nous utilisons les cookies suivants :</p>
              <ul>
                <li><strong>Cookies essentiels :</strong> Nécessaires au fonctionnement du site (session, authentification)</li>
                <li><strong>Cookies de sécurité :</strong> reCAPTCHA, détection de fraude</li>
                <li><strong>Cookies de performance :</strong> Analyse d'utilisation (avec votre consentement)</li>
              </ul>
              <p>
                Vous pouvez gérer vos préférences de cookies dans les paramètres de votre navigateur.
              </p>
            </Accordion.Body>
          </Accordion.Item>

          {/* Section 10 : Transferts internationaux */}
          <Accordion.Item eventKey="9">
            <Accordion.Header>
              <i className="bi bi-globe me-2"></i>
              10. Transferts internationaux de données
            </Accordion.Header>
            <Accordion.Body>
              <p>
                Certains services tiers (comme Google reCAPTCHA) peuvent impliquer des
                transferts de données hors de l'Union Européenne. Ces transferts sont
                encadrés par :
              </p>
              <ul>
                <li>Clauses contractuelles types de la Commission Européenne</li>
                <li>Certifications Privacy Shield (pour les USA)</li>
                <li>Garanties appropriées conformes au RGPD</li>
              </ul>
            </Accordion.Body>
          </Accordion.Item>

          {/* Section 11 : Modifications */}
          <Accordion.Item eventKey="10">
            <Accordion.Header>
              <i className="bi bi-pencil-square me-2"></i>
              11. Modifications de la politique
            </Accordion.Header>
            <Accordion.Body>
              <p>
                Nous nous réservons le droit de modifier cette politique de confidentialité
                à tout moment. Toute modification sera publiée sur cette page avec une mise
                à jour de la date. En cas de modification majeure, vous serez informé par email.
              </p>
            </Accordion.Body>
          </Accordion.Item>

          {/* Section 12 : Contact DPO */}
          <Accordion.Item eventKey="11">
            <Accordion.Header>
              <i className="bi bi-envelope me-2"></i>
              12. Contact - Délégué à la Protection des Données (DPO)
            </Accordion.Header>
            <Accordion.Body>
              <p>
                Pour toute question concernant vos données personnelles ou cette politique :
              </p>
              <ul>
                <li><strong>Email DPO :</strong> <a href="mailto:dpo@ebooks.fr">dpo@ebooks.fr</a></li>
                <li><strong>Courrier :</strong> DPO - Ebooks, [Adresse complète]</li>
              </ul>
              <p className="mt-3">
                <strong>Réclamation auprès de la CNIL :</strong><br />
                Si vous estimez que vos droits ne sont pas respectés, vous pouvez déposer
                une réclamation auprès de la Commission Nationale de l'Informatique et des
                Libertés (CNIL) : <a href="https://www.cnil.fr" target="_blank" rel="noopener noreferrer">www.cnil.fr</a>
              </p>
            </Accordion.Body>
          </Accordion.Item>
        </Accordion>

        <Card className="mt-4 bg-light">
          <Card.Body>
            <h5><i className="bi bi-info-circle"></i> Résumé en bref</h5>
            <ul>
              <li>✅ Vos données sont protégées et chiffrées</li>
              <li>✅ Nous respectons le RGPD et vos droits</li>
              <li>✅ Vous pouvez accéder, modifier ou supprimer vos données à tout moment</li>
              <li>✅ Nous ne vendons jamais vos données à des tiers</li>
              <li>✅ Vous êtes informé et avez le contrôle</li>
            </ul>
          </Card.Body>
        </Card>
      </Container>
    </>
  );
};

export default PrivacyPolicy;

