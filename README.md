# Project Guidelines - PMT API


Bienvenue sur le projet PMT API. Ce document sert de référence pour les standards de développement, l'architecture du code et le workflow Git à respecter par l'équipe.

## 🚀 Démarrage Rapide

### Pré-requis
*   Java 17+ (ou version du projet)
*   Maven
*   MySQL 8.0+

### Configuration initiale
```bash
# 1. Copier le fichier de configuration
cp env.sh.example env.sh

# 2. Éditer env.sh et configurer vos mots de passe
nano env.sh

# 3. Créer la base de données MySQL
mysql -u root -p -e "CREATE DATABASE project_management_tool_bdd_dev;"
```

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

1. **Copier le template de configuration** :
   ```bash
   cp env.sh.example env.sh
   ```

2. **Éditer `env.sh`** avec vos vraies valeurs :
   ```bash
   nano env.sh  # ou votre éditeur préféré
   ```
   Remplacez les valeurs `CHANGE_ME_*` par vos mots de passe réels pour staging et production.

3. **Charger les variables d'environnement** :
   ```bash
   source env.sh
   ```

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
./run.sh dev info         # Voir l'état des migrations
./run.sh dev migrate      # Exécuter les migrations (procédure complète)
./run.sh staging validate # Valider les migrations
./run.sh dev clean        # Nettoyer la DB (avec confirmation)
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

**Migration en développement/staging** (`./run.sh dev migrate`) :
1. Vérifier l'état des migrations (`flyway:info`)
2. Valider les migrations (`flyway:validate`)
3. Exécuter les migrations (`flyway:migrate`)
4. Vérification finale (`flyway:info`)

**Migration en production** (`./run.sh prod migrate`) :
1. Demande de confirmation (avec backup obligatoire)
2. Build de l'application
3. Vérification des migrations en attente
4. Validation des migrations
5. Exécution des migrations
6. Instructions pour lancer l'application

#### Sécurités intégrées

- ⚠️ **Clean désactivé** en staging/prod (disponible uniquement en dev)
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

| Profil | Base de données | Flyway | JPA DDL | Usage |
|:-------|:----------------|:-------|:--------|:------|
| `dev` | `project_management_tool_bdd_dev` | ✅ Activé | `validate` | Développement local |
| `staging` | `project_management_tool_bdd_staging` | ✅ Activé | `validate` | Tests d'intégration |
| `prod` | `project_management_tool_bdd` | ✅ Activé | `validate` | Production |
| `ddl` | Aucune connexion | ❌ Désactivé | `none` | Génération de schéma |

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
./run.sh dev info      # Voir l'état
./run.sh dev migrate   # Exécuter les migrations (procédure complète)
./run.sh dev validate  # Valider
```

#### Méthode manuelle avec Maven

Si vous préférez utiliser Maven directement, le plugin Flyway Maven permet de gérer les migrations sans démarrer l'application Spring Boot.

**Vérifier l'état des migrations** :
```bash
source env.sh
mvn flyway:info           # Environnement dev (par défaut)
mvn flyway:info -Pstaging # Environnement staging
mvn flyway:info -Pprod    # Environnement production
```

#### Exécuter les migrations
```bash
source env.sh
mvn flyway:migrate           # Dev
mvn flyway:migrate -Pstaging # Staging
mvn flyway:migrate -Pprod    # Production
```

#### Valider les migrations
Vérifie que les migrations appliquées correspondent aux fichiers présents :
```bash
source env.sh
mvn flyway:validate           # Dev
mvn flyway:validate -Pstaging # Staging
mvn flyway:validate -Pprod    # Production
```

#### Réparer la table de métadonnées
En cas de problème avec la table `flyway_schema_history` :
```bash
source env.sh
mvn flyway:repair           # Dev
mvn flyway:repair -Pstaging # Staging
mvn flyway:repair -Pprod    # Production
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
./run.sh staging migrate  # Procédure complète automatique
./run.sh staging launch   # Puis lancer l'application
```

**Ou manuellement** :
```bash
# 1. Charger les variables d'environnement
source env.sh

# 2. Vérifier les migrations en attente
mvn flyway:info -Pstaging

# 3. Exécuter les migrations
mvn flyway:migrate -Pstaging

# 4. Démarrer l'application
java -jar target/PMT_API-0.0.1-SNAPSHOT.jar --spring.profiles.active=staging
```

#### Sur Production

**Avec le script run.sh (recommandé)** :
```bash
# 1. BACKUP de la base de données (OBLIGATOIRE)
mysqldump -u pmt_prod -p project_management_tool_bdd > backup_$(date +%Y%m%d_%H%M%S).sql

# 2. Exécuter la procédure complète sécurisée
./run.sh prod migrate     # Demande confirmation, build, valide et migre

# 3. Lancer l'application
java -jar target/PMT_API-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

**Ou manuellement** :
```bash
# 1. Charger les variables d'environnement
source env.sh

# 2. BACKUP de la base de données (OBLIGATOIRE)
mysqldump -u pmt_prod -p project_management_tool_bdd > backup_$(date +%Y%m%d_%H%M%S).sql

# 3. Vérifier les migrations en attente
mvn flyway:info -Pprod

# 4. Valider les migrations
mvn flyway:validate -Pprod

# 5. Exécuter les migrations
mvn flyway:migrate -Pprod

# 6. Vérifier que tout s'est bien passé
mvn flyway:info -Pprod

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
