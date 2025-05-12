# Formini : Plateforme de Partage de Formations

## 🌟 Aperçu
Formini est une plateforme de partage de formations inspirée par Skillshare, développée dans le cadre du cours PIDEV 3A à l'[Esprit School of Engineering](https://www.esprit.tn/). Basée sur Symfony 6.4, elle permet aux apprenants de s’inscrire à des cours, aux instructeurs de partager leurs compétences, et aux administrateurs de gérer utilisateurs, paiements, formations, avis, et évaluations. Avec des intégrations d’APIs externes (Stripe, Affinda, Flask, LanguageTool, Google Translate, Sightengine), Formini offre une expérience sécurisée, automatisée et collaborative pour le partage de connaissances.

## 🎯 Objectif
Formini vise à créer un écosystème où les instructeurs partagent leurs expertises via des formations, les apprenants accèdent à des cours enrichissants, et les administrateurs supervisent l’ensemble avec des outils d’analyse et de gestion. Le système automatise les inscriptions, paiements, évaluations de CV, et avis, tout en garantissant une interface intuitive et des interactions modérées.

## 🚀 Modules de l’Application

### 📘 Documentation du Module Gestion des Utilisateurs

#### 🌟 Vue d’ensemble
Le module `Gestion des Utilisateurs` permet de gérer les profils des apprenants, instructeurs, et administrateurs, avec des fonctionnalités adaptées à chaque rôle. Il offre une interface pour l’inscription, la gestion de profil, l’authentification, et des outils administratifs pour superviser les utilisateurs.

#### 🎯 Objectif
Faciliter la gestion des utilisateurs en permettant aux apprenants et instructeurs de s’inscrire et de gérer leurs profils, tout en offrant aux administrateurs un tableau de bord pour consulter, supprimer, et analyser les utilisateurs inscrits.

#### 🚀 Fonctionnalités Clés
- **Inscription des Utilisateurs** :
  - **Apprenant** : Formulaire avec niveau d’études et photo de profil.
  - **Instructeur** : Formulaire incluant photo de profil et CV.
- **Gestion de Profil** :
  - Consultation et modification des informations personnelles (nom, email, mot de passe).
  - Mise à jour de la photo de profil et du CV (instructeurs).
- **Authentification** : Connexion sécurisée pour accéder aux espaces dédiés.
- **Administration** :
  - Visualisation de la liste des utilisateurs.
  - Suppression des utilisateurs si nécessaire.
  - Statistiques sur le nombre total d’utilisateurs et la répartition apprenants/instructeurs.

#### 🛠 Détails d’Implémentation
- **Configuration** :
  - Fichier `.env` avec :
    ```env
    DB_HOST=localhost
    DB_USER=nom_utilisateur
    DB_PASSWORD=mot_de_passe
    DB_NAME=formini3
    PORT=3000
    ```
- **Dépendances** : Node.js, npm pour le module utilisateur.

#### 🚶‍♂️ Utilisation
- **Apprenant** : S’inscrit avec niveau d’études et photo ; gère son profil.
- **Instructeur** : S’inscrit avec photo et CV ; met à jour ses informations.
- **Administrateur** : Accède au tableau de bord pour gérer/supprimer les utilisateurs et consulter les statistiques.

#### ✅ Dépendances
- Node.js, npm
- Base de données MySQL

#### 🧪 Tests
- Inscrire un apprenant avec un niveau d’études ; vérifier la mise à jour du profil.
- Inscrire un instructeur avec un CV ; confirmer le téléchargement.
- En tant qu’admin, supprimer un utilisateur ; vérifier les statistiques.

#### 📧 Contact
Contacter [benabdallah2ameni@gmail.com](mailto:benabdallah2ameni@gmail.com) pour toute question.

---

### 📘 Documentation du Module Gestion des Inscriptions & Paiements

#### 🌟 Vue d’ensemble
Ce module permet aux apprenants de s’inscrire à des cours et de payer en ligne via Stripe, tout en intégrant une logique de code promo géré par l’administrateur lorsque certaines conditions sont remplies.

#### 🎯 Objectif
Ce module Symfony est destiné à :
- Gérer les inscriptions des apprenants aux cours via un formulaire.
- Effectuer les paiements en ligne grâce à l’intégration de Stripe.
- Offrir un code promo géré manuellement par l’administrateur pour les apprenants ayant plus de 2 inscriptions.
- Permettre à l’administrateur d’analyser les apprenants les plus actifs.

#### 🚀 Fonctionnalités Clés
- **Inscription aux Cours** :
  - Formulaire rempli par l’apprenant.
  - Validation des données (cours sélectionné, apprenant, etc.).
- **Paiement en Ligne** :
  - Intégration de l’API Stripe pour sécuriser les paiements.
  - Affichage du récapitulatif de paiement.
- **Code Promo Administrateur** :
  - Génération manuelle d’un code de réduction par l’administrateur.
  - Condition : l’apprenant possède plus de 2 inscriptions.
  - Réduction appliquée au montant total.
- **Calcul du Nouveau Montant** :
  - Calcul du montant final après réduction via un champ `code_promo`.
  - Affichage clair du montant initial, remise, et montant final.
- **Recherche Avancée Admin** :
  - Interface d’administration avec :
    - Liste des apprenants triés par nombre d’inscriptions.
    - Génération ou modification manuelle des codes promo.

#### 🛠 Détails d’Implémentation
- **Installation** :
  - Cloner le dépôt :
    ```bash
    git clone https://github.com/Yassminette5/inscription-cours-paiement.git
    cd inscription-cours-paiement
    ```
  - Installer les dépendances Symfony :
    ```bash
    composer install
    ```
  - Exécuter les migrations :
    ```bash
    php bin/console doctrine:migrations:migrate
    ```
- **Configuration** :
  - Fichier `.env` avec :
    ```env
    DB_USER=nom_utilisateur
    DB_PASSWORD=mot_de_passe
    STRIPE_SECRET_KEY=clé_stripe
    EMAIL_HOST=emailhog
    EMAIL_PORT=1025
    ```
- **Dépendances** : Symfony, Composer, Stripe API.

#### 🚶‍♂️ Utilisation
- **Pour l’Apprenant** :
  - Accède au formulaire d’inscription.
  - Choisit un cours.
  - Procède au paiement.
  - Si l’utilisateur a plus de 2 inscriptions, un code promo peut lui être attribué par l’administrateur.
- **Pour l’Administrateur** :
  - Gère les inscriptions dans le tableau de bord.
  - Visualise les apprenants ayant le plus grand nombre d’inscriptions.
  - Génère ou ajuste manuellement les codes promo.
  - Suit les paiements et réductions appliquées.

#### ✅ Dépendances
- PHP 8.x, Symfony 6.4
- Stripe API
- Doctrine ORM

#### 🧪 Tests
- S’inscrire à un cours ; vérifier le paiement via Stripe.
- Appliquer un code promo après 3 inscriptions ; confirmer la réduction.
- En tant qu’admin, trier les apprenants par inscriptions.

#### 📧 Contact
Contacter [benabdallah2ameni@gmail.com](mailto:benabdallah2ameni@gmail.com) pour toute question.

---

### 📘 Documentation du Module Gestion des Formations

#### 🌟 Vue d’ensemble
Le module `Gestion des Formations` permet la création, la gestion, et l’accès aux formations, offrant une structure organisée pour les cours partagés par les instructeurs.

#### 🎯 Objectif
Faciliter la gestion des formations par les administrateurs, permettre aux instructeurs de superviser leurs cours, et offrir aux apprenants un accès clair aux formations disponibles.

#### 🚀 Fonctionnalités Clés
- **Création et Gestion** :
  - Les administrateurs créent, modifient, ou suppriment des formations (titre, description, durée, instructeur).
- **Accès** :
  - Les apprenants consultent les formations disponibles.
  - Les instructeurs gèrent leurs cours assignés.
- **Organisation** : Interface intuitive pour une navigation facile.

#### 🛠 Détails d’Implémentation
- **Entité** : `Formation` avec champs `id`, `titre`, `description`, `duree`, `instructeur`.
- **Contrôleur** : Gère les routes pour la création et l’affichage des formations.
- **Configuration** : Utilise Doctrine pour la gestion de la base de données.

#### 🚶‍♂️ Utilisation
- **Administrateur** : Crée une formation et assigne un instructeur.
- **Instructeur** : Consulte et gère ses formations.
- **Apprenant** : Parcourt les formations disponibles.

#### ✅ Dépendances
- Symfony 6.4, PHP 8.x
- Doctrine ORM

#### 🧪 Tests
- Créer une formation ; vérifier son affichage pour les apprenants.
- Assigner un instructeur ; confirmer la gestion par l’instructeur.

#### 📧 Contact
Contacter [benabdallah2ameni@gmail.com](mailto:benabdallah2ameni@gmail.com) pour toute question.

---

### 📘 Documentation du Module Gestion des Avis

#### 🌟 Vue d’ensemble
Le module `Gestion d'Avis` permet aux apprenants authentifiés (`ROLE_APPRENANT`) de noter les formations avec un système d’étoiles (1-5) et de laisser des commentaires, avec un calcul automatisé du score moyen des formations. Il intègre des APIs pour l’autocorrection, la traduction, et la modération.

#### 🎯 Objectif
Automatiser la gestion des avis, calculer des scores moyens pour évaluer les formations, et garantir des commentaires appropriés via des outils d’autocorrection et de modération.

#### 🚀 Fonctionnalités Clés
- **Notation par Étoiles** : Soumission d’avis avec note (1-5) via un formulaire sécurisé.
- **Calcul du Score** :
  - Création de `FormationScore` au premier avis.
  - Ajout : `noteMoyenne = (noteMoyenne * nombreAvis + nouvelleNote) / (nombreAvis + 1)`.
  - Modification : `noteMoyenne = ((noteMoyenne * nombreAvis) - ancienneNote + nouvelleNote) / nombreAvis`.
  - Suppression : `noteMoyenne = ((noteMoyenne * nombreAvis) - noteSupprimée) / (nombreAvis - 1)` ou 0 si aucun avis.
- **Autocorrection** : LanguageTool corrige les commentaires (ex. : "parceque" → "parce que").
- **Traduction** : Google Translate traduit en anglais (ex. : "Je suis fatigué" → "I am tired").
- **Modération** : Sightengine détecte les contenus inappropriés ; admins suppriment les avis signalés.

#### 🛠 Détails d’Implémentation
- **Contrôleurs** :
  - `AvisController` : Routes front-end pour avis, autocorrection, traduction.
  - `FormationScoreController` : Routes back-office pour modération.
- **Entités** :
  - `Avis` : `id`, `note`, `commentaire`, `dateCreation`, `formation`, `apprenant`, `isFlagged`, `flaggedReason`.
  - `FormationScore` : `id`, `noteMoyenne`, `nombreAvis`, `classement`, `formation`.
- **Intégration API** :
  - LanguageTool : `https://api.languagetool.org/v2/check`.
  - Google Translate : `https://translate.googleapis.com/translate_a/single`.
  - Sightengine : `https://api.sightengine.com/1.0/text/check.json`.
- **Templates Twig** : `templates/avis/` (front-office), `templates/admin/` (back-office).

#### 🚶‍♂️ Utilisation
- **Apprenant** : Ajoute un avis avec note et commentaire ; observe l’autocorrection.
- **Administrateur** : Modère les avis signalés via le back-office.
- **Utilisateur** : Consulte les avis et scores des formations.

#### ✅ Dépendances
- PHP 8.x, Symfony 6.4
- Doctrine ORM, Symfony HttpClient
- APIs : LanguageTool, Google Translate, Sightengine

#### 🧪 Tests
- **Autocorrection** : Entrée "Je suis tres fatigé" → Résultat "Je suis très fatigué".
- **Score** : Ajouter un avis (4 étoiles), puis un autre (3 étoiles) → Score 3.5/5 ; supprimer le premier → Score 3/5.
- **Traduction** : Commentaire "C’est une excellente formation !" → "This is an excellent training!".

#### ℹ Remarques
- Seuls les `ROLE_APPRENANT` peuvent ajouter des avis.
- LanguageTool limité à ~20 requêtes/minute (gratuit).

#### 📧 Contact
Contacter [benabdallah2ameni@gmail.com](mailto:benabdallah2ameni@gmail.com) pour toute question.

---

### 📘 Documentation du Module Gestion des Évaluations

#### 🌟 Vue d’ensemble
Le module `Évaluation` évalue les CV des instructeurs, générant un score numérique, un niveau de compétence, et un statut d’acceptation, en utilisant les APIs Affinda (analyse de CV) et Flask (scoring par apprentissage automatique).

#### 🎯 Objectif
Automatiser l’évaluation des qualifications des instructeurs pour faciliter leur intégration, sans modifier le schéma de la base de données `formini3`.

#### 🚀 Fonctionnalités Clés
- **Analyse de CV** : Affinda extrait éducation, expérience, compétences, certifications.
- **Scoring** : Flask génère un score (0-100) basé sur les données du CV.
- **Niveau de Compétence** :
  - Débutant : Score < 50
  - Intermédiaire : 50 ≤ Score < 80
  - Avancé : Score ≥ 80
- **Statut d’Acceptation** : Loggé (`Accepté` si score ≥ 70, `Non Accepté` sinon).
- **Automatisation** : `InstructeurListener` crée une évaluation après inscription.

#### 🛠 Détails d’Implémentation
- **Service** : `EvaluationService` gère l’analyse, le scoring, et la définition de `niveau`/`status`.
- **Entité** : `Evaluation` avec `id`, `score`, `niveau`, `dateCreation`, `education`, `yearsOfExperience`, `skills`, `certifications`, `instructor_id`.
- **Intégration API** :
  - Affinda : Analyse de CV.
  - Flask : `http://localhost:5000/predict_cv_score`.
- **Écouteur** : `InstructeurListener` déclenché par `postPersist`.
- **Configuration** :
  - Clés API dans `.env`.
  - CV stockés dans `public/uploads/cv/`.

#### 🚶‍♂️ Utilisation
- **Instructeur** : S’inscrit via POST à `/register_instructeur` avec CV.
- **Système** : Analyse le CV, génère score/niveau, logge le statut.
- **Résultat** : Score et niveau stockés dans la base ; statut dans `var/log/dev.log`.

#### ✅ Dépendances
- PHP 8.x, Symfony 6.4
- APIs : Affinda, Flask
- Python : `flask`, `numpy`, `pickle`

#### 🧪 Tests
- Inscrire un instructeur ; vérifier score (ex. : 100) et niveau (ex. : "Avancé") dans `var/log/dev.log`.
- Exécuter `SELECT * FROM evaluation` pour confirmer les données.

#### ℹ Remarques
- Pas de modification du schéma de la base.
- Flask doit être actif (`python cv_score_api.py`).

#### 📧 Contact
Contacter [benabdallah2ameni@gmail.com](mailto:benabdallah2ameni@gmail.com) pour toute question.

## 🛠 Pile Technologique
### Frontend
- Twig (`templates/avis/`, `templates/admin/`)
- HTML, CSS, JavaScript

### Backend
- Symfony 6.4, PHP 8.x
- Doctrine ORM (MySQL `formini3`)
- Node.js (Gestion des Utilisateurs)

### Outils
- **APIs** : Stripe, Affinda, Flask, LanguageTool, Google Translate, Sightengine
- **Dépendances** : Composer, npm, Python (`flask`, `numpy`, `pickle`)

## 📂 Structure du Répertoire
```
formini/
├── config/                   # Configuration (services.yaml, .env)
├── public/                   # Actifs, CV uploads
├── src/
│   ├── Controller/           # AvisController, etc.
│   ├── Entity/              # Avis, Evaluation, etc.
│   ├── Service/             # EvaluationService
│   ├── EventListener/       # InstructeurListener
├── templates/                # Twig templates
├── var/log/                 # Logs (dev.log)
```

## 🚀 Démarrage
### Prérequis
- PHP 8.x, Composer
- Node.js, npm
- MySQL
- Python 3.x
- Clés API : Stripe, Affinda, Sightengine, Flask

### Installation
1. Cloner le dépôt :
   ```bash
   git clone https://github.com/Yassminette5/formini.git
   cd formini
   ```
2. Installer les dépendances PHP :
   ```bash
   composer install
   ```
3. Installer les dépendances Node.js :
   ```bash
   npm install
   ```
4. Configurer `.env` :
   ```env
   DB_HOST=localhost
   DB_USER=votre_utilisateur
   DB_PASSWORD=votre_mot_de_passe
   DB_NAME=formini3
   STRIPE_SECRET_KEY=votre_clé_stripe
   SIGHTENGINE_API_USER=votre_utilisateur_sightengine
   SIGHTENGINE_API_SECRET=votre_clé_sightengine
   AFFINDA_API_KEY=votre_clé_affinda
   FLASK_API_KEY=votre_clé_flask
   EMAIL_HOST=emailhog
   EMAIL_PORT=1025
   ```
5. Exécuter les migrations :
   ```bash
   php bin/console doctrine:migrations:migrate
   ```
6. Lancer l’API Flask :
   ```bash
   cd /chemin/vers/cv-score-model
   python cv_score_api.py
   ```
7. Démarrer l’application :
   ```bash
   npm start
   ```
   Accéder à `http://localhost:3000`.

## 💡 Utilisation
- **Apprenant** : S’inscrire, suivre des cours, payer, laisser des avis.
- **Instructeur** : S’inscrire avec CV, gérer formations.
- **Administrateur** : Gérer utilisateurs, formations, paiements, avis, codes promo.

## 🙏 Remerciements
Développé sous la direction de [Professeur Jane Doe](mailto:jane.doe@esprit.tn) à l’Esprit School of Engineering.

## 👩‍💻 Contributeurs
- Yassminette5
- Ameni Ben Abdallah

## 📧 Contact
Contacter [benabdallah2ameni@gmail.com](mailto:benabdallah2ameni@gmail.com) ou visiter [le dépôt GitHub](https://github.com/Yassminette5/formini).

## 📄 Licence
Licence MIT. Voir [LICENSE](LICENSE).