# Architecture & User Stories

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
