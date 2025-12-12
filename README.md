# Project Guidelines - PMT API

Bienvenue sur le projet PMT API. Ce document sert de référence pour les standards de développement, l'architecture du code et le workflow Git à respecter par l'équipe.

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
