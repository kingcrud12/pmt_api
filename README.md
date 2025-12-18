# Project Guidelines - PMT API

Bienvenue sur le projet PMT API. Ce document sert de référence pour les standards de développement, l'architecture du code et le workflow Git à respecter par l'équipe.

## 📑 Table des matières

- [🚀 Démarrage Rapide](#-démarrage-rapide)
  - [Pré-requis](#pré-requis)
  - [Architecture Base de Données](#architecture-base-de-données)
  - [Configuration initiale](#configuration-initiale)
  - [Lancer le serveur](#lancer-le-serveur)
- [🚀 Lancement](#-lancement)
  - [Configuration des variables d'environnement](#configuration-des-variables-denvironnement)
  - [Script automatisé run.sh](#-script-automatisé-runsh-recommandé)
  - [Profils disponibles](#profils-disponibles)
  - [Profils Admin](#profils-admin)
- [🗃️ Migrations de Base de Données](#️-migrations-de-base-de-données)
  - [Structure des migrations](#structure-des-migrations)
  - [Commandes Flyway](#commandes-flyway)
  - [Procédure de création d'une migration](#procédure-de-création-dune-migration)
  - [Procédure de déploiement](#procédure-de-déploiement-des-migrations)
  - [Bonnes pratiques](#bonnes-pratiques)
- [📚 Documentation API](#-documentation-api)
- [🏗 Architecture & Structure du Code](#-architecture--structure-du-code)
- [🔄 Git Workflow](#-git-workflow)
- [📝 Conventions de Nommage](#-conventions-de-nommage)
- [🚀 Guide d'Implémentation](#-guide-dimplémentation-dune-fonctionnalité)

---

## 🚀 Démarrage Rapide

### Pré-requis
*   Java 25+
*   Maven 3.8+
*   WireGuard configuré et connecté au serveur MySQL distant
*   MySQL 8.0+ (serveur distant accessible via WireGuard)

### Architecture Base de Données

Le projet utilise un **serveur MySQL distant** accessible via **WireGuard VPN** :
- **Adresse serveur** : `10.10.0.1:3306` (via WireGuard)
- **SSL/TLS** : Requis (`sslMode=REQUIRED`)
- **Authentification** : `caching_sha2_password`
- **3 environnements** : dev, staging, production

### Configuration initiale

#### 1. Configurer WireGuard
Assurez-vous que WireGuard est actif et que le serveur MySQL est accessible :
```bash
# Vérifier la connexion WireGuard
ping 10.10.0.1

# Tester la connectivité MySQL
nc -zv 10.10.0.1 3306
```

#### 2. Configurer les variables d'environnement
```bash
# Charger les variables d'environnement
source env.sh
```

Le fichier `env.sh` contient :
- Les URLs JDBC avec IP WireGuard et SSL activé
- Les utilisateurs MySQL par environnement
- Les mots de passe (DevPass2024@, StagingPass2024@, ProdPass2024@, AdminPass2024@)

#### 3. Initialiser les bases de données
Le script `init_db.sh` crée automatiquement les bases de données et les utilisateurs sur le serveur distant :
```bash
chmod +x init_db.sh
./init_db.sh
```

Ce script effectue :
- ✅ Test de connexion VPN et MySQL
- ✅ Création des bases : `project_management_tool_bdd_dev`, `project_management_tool_bdd_staging`, `project_management_tool_bdd_prod`
- ✅ Création des utilisateurs : `pmt_dev`, `pmt_staging`, `pmt_prod`, `pmt_admin`
- ✅ Configuration des privilèges appropriés
- ✅ Tests de connexion avec SSL

**Note** : Ce script est idempotent et peut être relancé sans danger.

### Lancer le serveur

**Avec le script run.sh (recommandé)** :
```bash
./run.sh dev migrate   # Exécuter les migrations
./run.sh dev launch    # Lancer l'application
```

**Ou avec Maven** :
```bash
source env.sh
mvn spring-boot:run
```

Le serveur démarrera sur le port `8080`.
- **API** : http://localhost:8080
- **Swagger UI** : http://localhost:8080/swagger-ui.html

## 🚀 Lancement

Le projet PMT API peut être lancé dans différents environnements grâce aux profils Spring Boot. Avant de lancer le projet, assurez-vous d'avoir configuré vos variables d'environnement.

### Configuration des variables d'environnement

Le fichier `env.sh` configure automatiquement toutes les variables nécessaires :

```bash
# Charger les variables d'environnement
source env.sh
```

**Variables configurées** :
- `PMT_DEV_DB_URL` : jdbc:mysql://10.10.0.1:3306/project_management_tool_bdd_dev?sslMode=REQUIRED&serverTimezone=UTC
- `PMT_DEV_DB_USER` : pmt_dev
- `PMT_DEV_DB_PASSWORD` : DevPass2024@
- *(Idem pour STAGING et PROD)*

**Important** :
- ⚠️ Les mots de passe sont définis dans `env.sh` (non versionné pour la sécurité)
- ✅ SSL est obligatoire pour toutes les connexions
- ✅ Toutes les URLs utilisent l'IP WireGuard (10.10.0.1)

### 🎯 Script automatisé `run.sh` (Recommandé)

Le projet inclut un script `run.sh` qui automatise toutes les opérations courantes avec gestion d'erreurs et validation complète.

**Avantages** :
- ✅ Charge automatiquement les variables d'environnement depuis `env.sh`
- ✅ Vérifie les prérequis (Java, Maven, fichiers de configuration)
- ✅ Gestion d'erreurs robuste avec arrêt au premier échec
- ✅ Messages colorés et informatifs
- ✅ Procédures complètes de bout en bout
- ✅ Sécurités intégrées (confirmation pour actions dangereuses)

**Usage** :
```bash
./run.sh [PROFIL] [ACTION]
```

**Profils** : `dev`, `staging`, `prod`, `ddl`
**Actions** : `info`, `migrate`, `validate`, `repair`, `clean`, `launch`, `build`, `schema`

#### Exemples d'utilisation du script

**Lancer l'application** :
```bash
./run.sh dev launch       # Développement
./run.sh staging launch   # Staging
./run.sh prod launch      # Production
```

**Gérer les migrations** :
```bash
./run.sh dev info              # Voir l'état des migrations
./run.sh dev migrate           # Exécuter les migrations en dev
./run.sh admin-staging migrate # Exécuter les migrations en staging (admin requis)
./run.sh admin-prod migrate    # Exécuter les migrations en prod (admin requis)
./run.sh staging validate      # Valider les migrations
./run.sh dev clean             # Nettoyer la DB (avec confirmation)
```

**Builder et générer** :
```bash
./run.sh dev build        # Builder l'application
./run.sh ddl schema       # Générer le schéma DDL
```

**Aide** :
```bash
./run.sh --help          # Afficher l'aide complète
```

#### Procédures automatisées

Le script exécute des **procédures complètes** pour certaines actions :

**Migration en développement** (`./run.sh dev migrate`) :
1. Vérifier l'état des migrations (`flyway:info`)
2. Exécuter les migrations (`flyway:migrate`)
3. Valider les migrations (`flyway:validate`)
4. Vérification finale (`flyway:info`)

**Migration en staging** (`./run.sh admin-staging migrate`) :
1. Vérifier l'état des migrations (`flyway:info`)
2. Exécuter les migrations (`flyway:migrate`)
3. Valider les migrations (`flyway:validate`)
4. Vérification finale (`flyway:info`)

**Migration en production** (`./run.sh admin-prod migrate`) :
1. Demande de confirmation (avec backup obligatoire)
2. Build de l'application
3. Vérification des migrations en attente (`flyway:info`)
4. Exécution des migrations (`flyway:migrate`)
5. Validation des migrations (`flyway:validate`)
6. Instructions pour lancer l'application

#### Sécurités intégrées

- ⚠️ **Migrations staging/prod** : Réservées aux profils admin uniquement (`admin-staging`, `admin-prod`)
- ⚠️ **Clean désactivé** en staging/prod (disponible uniquement en dev et profils admin)
- ⚠️ **Confirmation requise** pour les actions destructives (clean, migration prod)
- ⚠️ **Validation stricte** des profils et actions
- ⚠️ **Arrêt immédiat** au premier échec

### Lancement par environnement (méthode manuelle)

#### Développement (profil `dev`)
```bash
# Charger les variables d'environnement
source env.sh

# Lancer avec Maven
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# OU avec Java directement
mvn clean package
java -jar target/PMT_API-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev
```

#### Staging (profil `staging`)
```bash
source env.sh
mvn spring-boot:run -Dspring-boot.run.profiles=staging

# OU
java -jar target/PMT_API-0.0.1-SNAPSHOT.jar --spring.profiles.active=staging
```

#### Production (profil `prod`)
```bash
source env.sh
mvn spring-boot:run -Dspring-boot.run.profiles=prod

# OU (recommandé en production)
java -jar target/PMT_API-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

#### Génération de schéma DDL (profil `ddl`)
Pour générer le fichier SQL du schéma de base de données à partir des entités JPA :
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=ddl
```
Le fichier sera généré dans `target/generated-schema/schema.sql`.

### Profils disponibles

| Profil | Base de données | Serveur | Utilisateur | Flyway | JPA DDL | Usage |
|:-------|:----------------|:--------|:-----------|:-------|:--------|:------|
| `dev` | `project_management_tool_bdd_dev` | 10.10.0.1:3306 (WG) | `pmt_dev` | ✅ Activé | `validate` | Développement |
| `staging` | `project_management_tool_bdd_staging` | 10.10.0.1:3306 (WG) | `pmt_staging` | ✅ Activé | `validate` | Tests d'intégration |
| `prod` | `project_management_tool_bdd_prod` | 10.10.0.1:3306 (WG) | `pmt_prod` | ✅ Activé | `validate` | Production |
| `admin-dev` | `project_management_tool_bdd_dev` | 10.10.0.1:3306 (WG) | `pmt_admin` | ✅ Activé | - | 🔑 Admin DEV (clean autorisé) |
| `admin-staging` | `project_management_tool_bdd_staging` | 10.10.0.1:3306 (WG) | `pmt_admin` | ✅ Activé | - | 🔑 Admin STAGING (clean autorisé) |
| `admin-prod` | `project_management_tool_bdd_prod` | 10.10.0.1:3306 (WG) | `pmt_admin` | ✅ Activé | - | 🔑 Admin PROD (clean autorisé) |
| `ddl` | Aucune connexion | - | - | ❌ Désactivé | `none` | Génération de schéma |

**Note** : Toutes les connexions nécessitent WireGuard actif et SSL activé.

### Profils Admin

Les profils `admin-*` sont spécialement conçus pour l'administration des bases de données avec l'utilisateur `pmt_admin` qui possède tous les droits.

#### Caractéristiques
- 👤 **Utilisateur** : `pmt_admin` (privilèges complets sur toutes les bases)
- 🔒 **Migrations staging/prod** : **OBLIGATOIRE** pour toutes les migrations sur staging et production
- 🗑️ **Clean autorisé** : Contrairement aux profils staging/prod classiques, les profils admin peuvent exécuter `flyway:clean`
- 🚫 **Pas d'application** : Ces profils sont uniquement pour Flyway (pas de `application.properties` associés)
- 🔐 **Sécurité** : Confirmation requise pour les opérations destructives

#### Cas d'usage
```bash
# Nettoyer complètement la base de staging
./run.sh admin-staging clean

# Vérifier l'état des migrations en prod avec droits admin
./run.sh admin-prod info

# Réparer la table flyway_schema_history en dev
./run.sh admin-dev repair

# Migrer en tant qu'admin
./run.sh admin-staging migrate
```

#### Sécurités
- ⚠️ La commande `clean` demande une confirmation explicite
- ⚠️ Les profils admin ne peuvent PAS lancer l'application Spring Boot
- ⚠️ Utilisez ces profils uniquement pour les opérations de maintenance de base de données

## 🗃️ Migrations de Base de Données

Le projet utilise **Flyway** pour gérer les migrations de base de données de manière versionnée et reproductible.

### Structure des migrations

Les fichiers de migration sont situés dans :
```
src/main/resources/db/migrations/
```

Nommage obligatoire : `V{version}__{description}.sql`
- **Exemple** : `V1__init.sql`, `V2__add_user_roles.sql`, `V3__create_notifications_table.sql`

### Commandes Flyway

**💡 Recommandation** : Utilisez le script `run.sh` pour une gestion simplifiée des migrations :
```bash
./run.sh dev info              # Voir l'état en dev
./run.sh dev migrate           # Exécuter les migrations en dev
./run.sh admin-staging migrate # Exécuter les migrations en staging (admin requis)
./run.sh admin-prod migrate    # Exécuter les migrations en prod (admin requis)
./run.sh dev validate          # Valider
```

#### Méthode manuelle avec Maven

Si vous préférez utiliser Maven directement, le plugin Flyway Maven permet de gérer les migrations sans démarrer l'application Spring Boot.

**Vérifier l'état des migrations** :
```bash
source env.sh
mvn flyway:info                 # Environnement dev (par défaut)
mvn flyway:info -Padmin-staging # Environnement staging (admin requis)
mvn flyway:info -Padmin-prod    # Environnement production (admin requis)
```

#### Exécuter les migrations
```bash
source env.sh
mvn flyway:migrate                 # Dev
mvn flyway:migrate -Padmin-staging # Staging (admin requis)
mvn flyway:migrate -Padmin-prod    # Production (admin requis)
```

⚠️ **Important** : Les migrations sur staging et production nécessitent les profils admin (`admin-staging`, `admin-prod`) pour des raisons de permissions.

#### Valider les migrations
Vérifie que les migrations appliquées correspondent aux fichiers présents :
```bash
source env.sh
mvn flyway:validate                 # Dev
mvn flyway:validate -Padmin-staging # Staging (admin requis)
mvn flyway:validate -Padmin-prod    # Production (admin requis)
```

#### Réparer la table de métadonnées
En cas de problème avec la table `flyway_schema_history` :
```bash
source env.sh
mvn flyway:repair                 # Dev
mvn flyway:repair -Padmin-staging # Staging (admin requis)
mvn flyway:repair -Padmin-prod    # Production (admin requis)
```

#### Nettoyer la base de données (⚠️ DANGEREUX)
Supprime **TOUTES** les tables de la base de données :
```bash
source env.sh
mvn flyway:clean  # Disponible uniquement en DEV
```
⚠️ **Attention** : Cette commande est **désactivée** (`clean-disabled=true`) en staging et production pour éviter les suppressions accidentelles.

### Procédure de création d'une migration

#### 1. Créer un nouveau fichier de migration
```bash
# Déterminer le prochain numéro de version
cd src/main/resources/db/migrations/
ls -la  # Voir les migrations existantes

# Créer le nouveau fichier avec le bon numéro de version
touch V2__add_notifications_table.sql
```

#### 2. Écrire la migration SQL
Exemple de contenu pour `V2__add_notifications_table.sql` :
```sql
CREATE TABLE notification (
    id CHAR(36) NOT NULL,
    user_id CHAR(36) NOT NULL,
    message TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB;

ALTER TABLE notification
    ADD CONSTRAINT fk_notification_user
    FOREIGN KEY (user_id)
    REFERENCES user (id);
```

#### 3. Tester la migration en développement

**Avec le script run.sh (recommandé)** :
```bash
./run.sh dev migrate  # Exécute la procédure complète automatiquement
```

**Ou manuellement avec Maven** :
```bash
source env.sh

# Vérifier l'état avant migration
mvn flyway:info

# Exécuter la migration
mvn flyway:migrate

# Vérifier que tout s'est bien passé
mvn flyway:info
```

#### 4. Mettre à jour les entités JPA
Après avoir créé la migration, mettez à jour vos entités JPA pour qu'elles correspondent au nouveau schéma.

#### 5. Vérifier la cohérence
Démarrez l'application pour vérifier que JPA valide correctement le schéma :
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Procédure de déploiement des migrations

#### Sur Staging

**Avec le script run.sh (recommandé)** :
```bash
./run.sh admin-staging migrate  # Procédure complète avec profil admin
./run.sh staging launch         # Puis lancer l'application
```

⚠️ **Important** : Les migrations sur staging nécessitent le profil `admin-staging` pour des raisons de permissions.

**Ou manuellement** :
```bash
# 1. Charger les variables d'environnement
source env.sh

# 2. Vérifier les migrations en attente
mvn flyway:info -Padmin-staging

# 3. Exécuter les migrations
mvn flyway:migrate -Padmin-staging

# 4. Démarrer l'application
java -jar target/PMT_API-0.0.1-SNAPSHOT.jar --spring.profiles.active=staging
```

#### Sur Production

**Avec le script run.sh (recommandé)** :
```bash
# 1. BACKUP de la base de données (OBLIGATOIRE)
source env.sh
mysqldump --protocol=TCP -h 10.10.0.1 -P 3306 -u pmt_admin -p"${PMT_ADMIN_DB_PASSWORD}" \
  --ssl-mode=REQUIRED project_management_tool_bdd_prod > backup_$(date +%Y%m%d_%H%M%S).sql

# 2. Exécuter la procédure complète sécurisée
./run.sh admin-prod migrate  # Demande confirmation, build, valide et migre

# 3. Lancer l'application
java -jar target/PMT_API-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

⚠️ **Important** : Les migrations sur production nécessitent le profil `admin-prod` pour des raisons de permissions.

**Ou manuellement** :
```bash
# 1. Charger les variables d'environnement
source env.sh

# 2. BACKUP de la base de données (OBLIGATOIRE)
mysqldump --protocol=TCP -h 10.10.0.1 -P 3306 -u pmt_admin -p"${PMT_ADMIN_DB_PASSWORD}" \
  --ssl-mode=REQUIRED project_management_tool_bdd_prod > backup_$(date +%Y%m%d_%H%M%S).sql

# 3. Vérifier les migrations en attente
mvn flyway:info -Padmin-prod

# 4. Exécuter les migrations
mvn flyway:migrate -Padmin-prod

# 5. Valider les migrations
mvn flyway:validate -Padmin-prod

# 6. Vérifier que tout s'est bien passé
mvn flyway:info -Padmin-prod

# 7. Démarrer l'application
java -jar target/PMT_API-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

### Bonnes pratiques

✅ **À faire** :
- Toujours tester les migrations en développement avant staging/production
- Créer des migrations **incrémentales** et **idempotentes** quand possible
- Faire un backup de la base avant toute migration en production
- Utiliser des transactions (`BEGIN;` ... `COMMIT;`) pour les migrations critiques
- Documenter les migrations complexes avec des commentaires SQL
- Versionner les migrations de manière séquentielle (V1, V2, V3...)

❌ **À éviter** :
- Modifier une migration déjà appliquée en staging/production
- Supprimer des migrations déjà appliquées
- Utiliser `flyway:clean` en dehors du développement
- Oublier de mettre à jour les entités JPA après une migration
- Déployer sans avoir testé les migrations

### En cas de problème

#### Migration échouée
```bash
# 1. Vérifier l'état
mvn flyway:info -Pdev

# 2. Corriger le fichier SQL problématique

# 3. Réparer la table de métadonnées
mvn flyway:repair -Pdev

# 4. Réessayer
mvn flyway:migrate -Pdev
```

#### Rollback d'une migration
Flyway ne gère pas les rollbacks automatiques. Vous devez créer une nouvelle migration qui annule les changements :
```bash
# Si V5 a créé une table, créer V6 pour la supprimer
touch V6__rollback_previous_migration.sql
```

## 📚 Documentation API

Une fois le serveur lancé, vous pouvez accéder à la documentation de l'API :

*   **Swagger UI (Interface visuelle)** : [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
*   **OpenAPI JSON** : [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

## 🌍 Architecture Globale & User Stories

Le projet consiste en une application web complète (Project Management Tool) composée de :
*   **Front-end** : Application **Angular** (SPA).
*   **Back-end** : API REST **Spring Boot** (ce projet).
*   **Base de données** : MySQL.

### Couverture Fonctionnelle

Voici comment l'API supporte les User Stories demandées à travers nos entités (`User`, `Project`, `Task`, `ProjectMember`, `TaskHistory`) :

#### 1. Gestion des Utilisateurs (Auth)
*   **Inscription** : `POST /api/v1/auth/register` (Création `User`).
*   **Connexion** : `POST /api/v1/auth/login` (JWT Token).
    *   *US : "En tant que visiteur, je veux m'inscrire..."*
    *   *US : "En tant qu’inscrit, je veux me connecter..."*

#### 2. Gestion des Projets
*   **Création** : `POST /api/v1/projects` (Entité `Project`). L'utilisateur devient Admin.
    *   *US : "Je veux créer un nouveau projet..."*
*   **Invitation** : `POST /api/v1/projects/{id}/members` (Entité `ProjectMember`).
    *   *US : "Je veux inviter d'autres membres..."*
*   **Rôles** : Gestion via l'enum `ProjectRole` (ADMIN, MEMBER, OBSERVER).
    *   *US : "Je veux attribuer des rôles..."*

#### 3. Gestion des Tâches
*   **Création** : `POST /api/v1/projects/{id}/tasks` (Entité `Task`).
    *   *US : "Je veux créer des tâches avec nom, description, date..."*
*   **Assignation** : Update `assignee_id` sur `Task`.
    *   *US : "Je veux assigner des tâches à des membres spécifiques."*
*   **Mise à jour** : `PUT /api/v1/tasks/{id}`. Permet de changer status, priorité, dates.
    *   *US : "Je veux mettre à jour une tâche..."*
*   **Visualisation** : `GET` endpoint pour détails et listes filtrées par statut.
    *   *US : "Je veux visualiser les tâches selon les statuts."*

#### 4. Suivi & Historique
*   **Historique** : Chaque modification de tâche crée une entrée `TaskHistory`.
    *   *US : "Je veux suivre l'historique des modifications..."*
*   **Notifications** : Le backend déclenchera des notifications (ex: email) lors des événements (Assignation).

## 🏗 Architecture & Structure du Code

Le projet suit une architecture Spring Boot classique en couches. Le code source se trouve sous `src/main/java/fr/techcrud/pmt_api`.

### Packages Conventionnels

| Package | Chemin | Description & Responsabilité |
| :--- | :--- | :--- |
| **Models (Entities)** | `fr.techcrud.pmt_api.models` | Classes annotées `@Entity`. Représentent les tables de la base de données. Utilisez Lombok pour les getters/setters/constructeurs. |
| **DTO** | `fr.techcrud.pmt_api.dto` | Data Transfer Objects. Utilisés pour transférer les données entre le client et l'API. Jamais d'entités exposées directement dans les contrôleurs. |
| **Repositories** | `fr.techcrud.pmt_api.repositories` | Interfaces étendant `JpaRepository`. Responsables de l'accès aux données. Seuls les Services doivent les appeler. |
| **Services** | `fr.techcrud.pmt_api.services` | Logique métier. Contient les règles de gestion, validation, et appels aux repositories. Annotés avec `@Service`. |
| **Controllers** | `fr.techcrud.pmt_api.controllers` | Points d'entrée REST. Annotés `@RestController`. Ils manipulent des DTOs et délèguent le travail aux Services. |
| **Utils** | `fr.techcrud.pmt_api.utils` | Classes utilitaires (helpers, constantes, méthodes statiques) réutilisables. |

## 🔄 Git Workflow

Nous utilisons un workflow basé sur 3 branches principales et des branches temporaires pour les développements.

### Branches Principales

*   **`dev`** : Branche de développement principale. Tout nouveau code arrive ici. C'est la base pour les nouvelles fonctionnalités.
*   **`staging`** : Environnement de pré-production/recette. Utilisé pour les tests d'intégration avant la mise en production.
*   **`prod`** : Branche de production. Le code doit y être stable et testé.

### Branches Temporaires

*   **`bug-fix/*`** : Pour la correction de bugs.
    *   *Exemple* : `bug-fix/user-login-error`
*   **`feature_waiting_validation/*`** : Pour les fonctionnalités en attente de review ou de validation métier avant merge sur `dev`.
    *   *Exemple* : `feature_waiting_validation/add-payment-gateway`

## 📝 Conventions de Nommage

### Commits

Les commits doivent suivre la convention **Conventional Commits** pour faciliter la lecture de l'historique et la génération de changelogs.

Format : `type: description`

*   `feat: ...` : Nouvelle fonctionnalité
*   `fix: ...` : Correction de bug
*   `docs: ...` : Documentation
*   `style: ...` : Formatage, point-virgule manquant (pas de changement de code fonctionnel)
*   `refactor: ...` : Refactoring du code (ni fix, ni feat)
*   `test: ...` : Ajout ou correction de tests
*   `chore: ...` : Maintenance (build, dépendances, etc.)

*Exemple* : `feat: ajouter le endpoint de création d'utilisateur`

### Merge Requests (MR) / Pull Requests (PR)

Le titre des MR doit être explicite et indiquer la cible et le type.

Format : `[CIBLE] Type: Description`

*   **CIBLE** : `DEV`, `STAGING`, `PROD`

*Exemples* :
*   `[DEV] Feat: Implémentation du système de notification`
*   `[STAGING] Fix: Correction du bug d'affichage prix`
*   `[PROD] Release: Version 1.2.0`

## 🚀 Guide d'Implémentation d'une Fonctionnalité

Voici le processus étape par étape pour implémenter une nouvelle fonctionnalité dans le projet, en respectant les standards de qualité.

### 1. Analyse & Modélisation
Avant de coder, vérifiez si la fonctionnalité se rattache à une entité existante (`models`).
*   Si **OUI** : Réutilisez l'entité existante.
*   Si **NON** : Créez une nouvelle classe annotée `@Entity` dans `fr.techcrud.pmt_api.models`.

### 2. Couche Service (Business Logic)
Toute la logique métier doit résider ici, jamais dans le contrôleur.

1.  **Créer l'interface** : Dans `fr.techcrud.pmt_api.services`.
    ```java
    public interface UserService {
        User create(User user);
        User findById(UUID id);
    }
    ```
2.  **Créer l'implémentation** : Créez une classe conventionnellement nommée `XServiceImpl` qui implémente l'interface et est annotée `@Service`.
    ```java
    @Service
    public class UserServiceImpl implements UserService {
        // ... implémentation
    }
    ```

### 3. Gestion des Exceptions (Règle d'Or)
**Le Service retourne `null`, le Controller lève l'exception.**

Le Service ne doit pas lever d'exception métier (comme `RessourceNotFoundException`). Il doit retourner `null` si l'objet n'est pas trouvé ou si l'opération échoue de manière prévue. C'est la responsabilité du Contrôleur d'interpréter ce `null`.

Exemple Service :
```java
@Override
public User findById(UUID id) {
    // Retourne l'utilisateur ou null s'il n'existe pas
    return userRepository.findById(id).orElse(null);
}
```

### 4. DTO & Sérialisation
Si les données exposées diffèrent de l'entité (ou pour éviter les boucles infinies JSON), utilisez un DTO.

1.  **Créer le DTO** : Dans `fr.techcrud.pmt_api.dto`.
2.  **Mapper/Serializer** : Utilisez un mapper (ex: MapStruct ou manuel) pour convertir Entité <-> DTO.

### 5. Controller (API REST)
Le contrôleur reçoit la requête, appelle le service, **vérifie le retour**, et lève l'exception appropriée si nécessaire.

*   **Si retour Service == null** : `throw new RessourceNotFoundException(...)` ou `BadRequestException`.
*   **Si retour Service != null** : Retourner `ResponseEntity.ok(result)`.

```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getById(@PathVariable UUID id) {
        User user = userService.findById(id);
        
        if (user == null) {
            throw new RessourceNotFoundException("User not found with id " + id);
        }
        
        return ResponseEntity.ok(user);
    }
}
```

### 6. Tests Unitaires
Chaque service doit être testé unitairement. Mockez les repositories pour isoler la logique métier.
