# Project Guidelines - PMT API


Bienvenue sur le projet PMT API. Ce document sert de référence pour les standards de développement, l'architecture du code et le workflow Git à respecter par l'équipe.

## 🚀 Démarrage Rapide

### Pré-requis
*   Java 17+ (ou version du projet)
*   Maven

### Lancer le serveur
```bash
mvn spring-boot:run
```
Le serveur démarrera par défaut sur le port `8080`.

## 📚 Documentation API

Une fois le serveur lancé, vous pouvez accéder à la documentation de l'API :

*   **Swagger UI (Interface visuelle)** : [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
*   **OpenAPI JSON** : [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

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
