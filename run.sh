#!/bin/bash

###############################################################################
# PMT API - Script de lancement et gestion des migrations
# Usage: ./run.sh [PROFILE] [ACTION]
#
# PROFILE: dev, staging, prod, ddl
# ACTION:  info, migrate, validate, repair, clean, launch, build, schema
###############################################################################

set -e  # Exit on error

# Couleurs pour l'affichage
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
BOLD='\033[1m'
NC='\033[0m' # No Color

# Fonction pour afficher les messages
log_info() {
    printf "%b[INFO]%b %s\n" "$BLUE" "$NC" "$1"
}

log_success() {
    printf "%b[SUCCESS]%b %s\n" "$GREEN" "$NC" "$1"
}

log_warning() {
    printf "%b[WARNING]%b %s\n" "$YELLOW" "$NC" "$1"
}

log_error() {
    printf "%b[ERROR]%b %s\n" "$RED" "$NC" "$1"
}

log_step() {
    printf "\n%b==>%b %b%s%b\n" "$CYAN" "$NC" "$BOLD" "$1" "$NC"
}

# Fonction pour afficher l'aide
show_help() {
    printf "%bPMT API - Script de lancement%b\n\n" "$GREEN" "$NC"

    printf "%bUsage:%b\n" "$YELLOW" "$NC"
    printf "    ./run.sh [PROFILE] [ACTION]\n\n"

    printf "%bProfils disponibles:%b\n" "$YELLOW" "$NC"
    printf "    dev      - Environnement de développement (par défaut)\n"
    printf "    staging  - Environnement de staging/recette\n"
    printf "    prod     - Environnement de production\n"
    printf "    ddl      - Génération de schéma uniquement\n\n"

    printf "%bActions disponibles:%b\n" "$YELLOW" "$NC"
    printf "    %bMigrations Flyway:%b\n" "$CYAN" "$NC"
    printf "    info     - Afficher l'état des migrations\n"
    printf "    migrate  - Exécuter les migrations en attente\n"
    printf "    validate - Valider les migrations appliquées\n"
    printf "    repair   - Réparer la table de métadonnées Flyway\n"
    printf "    clean    - Nettoyer toute la base de données (DEV UNIQUEMENT!)\n\n"

    printf "    %bApplication:%b\n" "$CYAN" "$NC"
    printf "    launch   - Lancer l'application Spring Boot\n"
    printf "    build    - Builder l'application (package Maven)\n"
    printf "    schema   - Générer le schéma SQL (profil ddl uniquement)\n\n"

    printf "%bExemples:%b\n" "$YELLOW" "$NC"
    printf "    ./run.sh dev migrate      # Exécuter les migrations en dev\n"
    printf "    ./run.sh staging info     # Voir l'état des migrations en staging\n"
    printf "    ./run.sh prod launch      # Lancer l'application en production\n"
    printf "    ./run.sh ddl schema       # Générer le schéma DDL\n\n"

    exit 0
}

# Vérifier les prérequis
check_prerequisites() {
    log_step "Vérification des prérequis"

    # Vérifier Java
    if ! command -v java &> /dev/null; then
        log_error "Java n'est pas installé ou n'est pas dans le PATH"
        exit 1
    fi
    log_info "Java: $(java -version 2>&1 | head -n 1)"

    # Vérifier Maven
    if ! command -v mvn &> /dev/null; then
        log_error "Maven n'est pas installé ou n'est pas dans le PATH"
        exit 1
    fi
    log_info "Maven: $(mvn -version | head -n 1)"

    # Vérifier que le script est exécuté depuis le bon répertoire
    if [[ ! -f "pom.xml" ]]; then
        log_error "Le fichier pom.xml n'existe pas. Exécutez ce script depuis la racine du projet."
        exit 1
    fi

    log_success "Tous les prérequis sont satisfaits"
}

# Charger les variables d'environnement
load_env_vars() {
    # Vérifier si env.sh existe
    if [[ -f "env.sh" ]]; then
        log_info "Chargement des variables d'environnement depuis env.sh"
        source env.sh
        log_success "Variables d'environnement chargées"
    else
        log_warning "Le fichier env.sh n'existe pas"
        log_info "Créez-le depuis le template: cp env.sh.example env.sh"
    fi
}

# Vérifier et charger les variables d'environnement
check_env_vars() {
    local profile=$1

    # Le profil ddl n'a pas besoin de variables d'environnement
    if [[ "$profile" == "ddl" ]]; then
        return 0
    fi

    log_step "Vérification des variables d'environnement pour le profil: $profile"

    # Charger automatiquement les variables d'environnement
    load_env_vars

    # Vérifier si les variables d'environnement sont définies
    case $profile in
        dev)
            if [[ -z "$PMT_DEV_DB_URL" ]] || [[ -z "$PMT_DEV_DB_USER" ]] || [[ -z "$PMT_DEV_DB_PASSWORD" ]]; then
                log_error "Variables d'environnement manquantes pour le profil dev"
                log_info "Vérifiez le contenu de env.sh et assurez-vous que les variables PMT_DEV_* sont définies"
                exit 1
            fi
            ;;
        staging)
            if [[ -z "$PMT_STAGING_DB_URL" ]] || [[ -z "$PMT_STAGING_DB_USER" ]] || [[ -z "$PMT_STAGING_PASSWORD" ]]; then
                log_error "Variables d'environnement manquantes pour le profil staging"
                log_info "Vérifiez le contenu de env.sh et assurez-vous que les variables PMT_STAGING_* sont définies"
                exit 1
            fi
            ;;
        prod)
            if [[ -z "$PMT_PROD_DB_URL" ]] || [[ -z "$PMT_PROD_DB_USER" ]] || [[ -z "$PMT_PROD_PASSWORD" ]]; then
                log_error "Variables d'environnement manquantes pour le profil prod"
                log_info "Vérifiez le contenu de env.sh et assurez-vous que les variables PMT_PROD_* sont définies"
                exit 1
            fi
            ;;
    esac

    log_success "Variables d'environnement OK"
}

# Exécuter une commande Flyway
run_flyway_command() {
    local profile=$1
    local command=$2

    log_step "Exécution de: mvn flyway:$command -P$profile"

    # Vérification spéciale pour clean
    if [[ "$command" == "clean" ]] && [[ "$profile" != "dev" ]]; then
        log_error "La commande 'clean' n'est disponible qu'en environnement dev pour des raisons de sécurité"
        exit 1
    fi

    # Exécuter la commande
    if mvn flyway:$command -P$profile; then
        log_success "Commande flyway:$command exécutée avec succès"
        return 0
    else
        log_error "La commande flyway:$command a échoué"
        exit 1
    fi
}

# Lancer l'application
launch_application() {
    local profile=$1

    log_step "Lancement de l'application avec le profil: $profile"

    if [[ "$profile" == "ddl" ]]; then
        log_error "Le profil ddl ne peut pas être utilisé pour lancer l'application"
        log_info "Utilisez: ./run.sh ddl schema"
        exit 1
    fi

    log_info "Démarrage de Spring Boot..."
    log_info "L'application sera disponible sur http://localhost:8080"
    log_info "Swagger UI: http://localhost:8080/swagger-ui.html"
    printf "\n"

    # Lancer l'application
    mvn spring-boot:run -Dspring-boot.run.profiles=$profile
}

# Builder l'application
build_application() {
    log_step "Build de l'application"

    log_info "Nettoyage et packaging..."
    if mvn clean package -DskipTests; then
        log_success "Build réussi"
        log_info "JAR généré: target/PMT_API-0.0.1-SNAPSHOT.jar"
        return 0
    else
        log_error "Le build a échoué"
        exit 1
    fi
}

# Générer le schéma DDL
generate_schema() {
    log_step "Génération du schéma DDL"

    # Nettoyer le fichier existant
    rm -f target/generated-schema/schema.sql

    log_info "Lancement de JPA pour générer le schéma..."

    # Lancer avec timeout car l'application va tourner indéfiniment
    timeout 15 mvn spring-boot:run -Dspring-boot.run.profiles=ddl > /dev/null 2>&1 || true

    # Vérifier que le fichier a été généré
    if [[ -f "target/generated-schema/schema.sql" ]]; then
        log_success "Schéma DDL généré avec succès"
        log_info "Fichier: target/generated-schema/schema.sql"

        # Afficher un résumé
        local table_count=$(grep -c "create table" target/generated-schema/schema.sql || true)
        log_info "Nombre de tables générées: $table_count"

        return 0
    else
        log_error "Échec de la génération du schéma"
        exit 1
    fi
}

# Procédure complète de migration
migration_procedure() {
    local profile=$1

    log_step "Procédure complète de migration pour l'environnement: $profile"

    # Étape 1: Vérifier l'état actuel
    log_info "Étape 1/3: Vérification de l'état des migrations"
    run_flyway_command $profile "info"

    # Étape 2: Valider les migrations
    log_info "Étape 2/3: Validation des migrations"
    run_flyway_command $profile "validate"

    # Étape 3: Exécuter les migrations
    log_info "Étape 3/3: Exécution des migrations"
    run_flyway_command $profile "migrate"

    # Vérification finale
    log_info "Vérification finale"
    run_flyway_command $profile "info"

    log_success "Procédure de migration terminée avec succès!"
}

# Procédure complète de lancement en production
production_launch() {
    log_step "Procédure de lancement en PRODUCTION"

    log_warning "ATTENTION: Vous êtes sur le point de déployer en PRODUCTION"
    log_warning "Assurez-vous d'avoir fait un backup de la base de données!"
    printf "\n"
    read -p "Continuer? (tapez 'yes' pour confirmer): " confirm

    if [[ "$confirm" != "yes" ]]; then
        log_info "Opération annulée par l'utilisateur"
        exit 0
    fi

    # Étape 1: Build
    log_info "Étape 1/4: Build de l'application"
    build_application

    # Étape 2: Info migrations
    log_info "Étape 2/4: Vérification des migrations en attente"
    run_flyway_command "prod" "info"

    # Étape 3: Valider
    log_info "Étape 3/4: Validation des migrations"
    run_flyway_command "prod" "validate"

    # Étape 4: Migrer
    log_info "Étape 4/4: Exécution des migrations"
    run_flyway_command "prod" "migrate"

    log_success "Application prête à être lancée!"
    log_info "Pour lancer: java -jar target/PMT_API-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod"
}

###############################################################################
# MAIN
###############################################################################

# Afficher l'aide si demandé
if [[ "$1" == "-h" ]] || [[ "$1" == "--help" ]] || [[ -z "$1" ]]; then
    show_help
fi

# Parser les arguments
PROFILE=${1:-dev}
ACTION=${2:-launch}

# Valider le profil
case $PROFILE in
    dev|staging|prod|ddl)
        ;;
    *)
        log_error "Profil invalide: $PROFILE"
        log_info "Profils valides: dev, staging, prod, ddl"
        printf "\n"
        show_help
        ;;
esac

# Valider l'action
case $ACTION in
    info|migrate|validate|repair|clean|launch|build|schema)
        ;;
    *)
        log_error "Action invalide: $ACTION"
        log_info "Actions valides: info, migrate, validate, repair, clean, launch, build, schema"
        printf "\n"
        show_help
        ;;
esac

# Afficher l'en-tête
printf "%b╔════════════════════════════════════════════════╗%b\n" "$GREEN" "$NC"
printf "%b║%b     PMT API - Gestionnaire de Déploiement    %b║%b\n" "$GREEN" "$NC" "$GREEN" "$NC"
printf "%b╚════════════════════════════════════════════════╝%b\n" "$GREEN" "$NC"
printf "\n"
log_info "Profil: $PROFILE"
log_info "Action: $ACTION"
printf "\n"

# Vérifier les prérequis
check_prerequisites

# Vérifier les variables d'environnement
check_env_vars $PROFILE

# Exécuter l'action demandée
case $ACTION in
    info)
        run_flyway_command $PROFILE "info"
        ;;
    migrate)
        if [[ "$PROFILE" == "prod" ]]; then
            production_launch
        else
            migration_procedure $PROFILE
        fi
        ;;
    validate)
        run_flyway_command $PROFILE "validate"
        ;;
    repair)
        run_flyway_command $PROFILE "repair"
        ;;
    clean)
        log_warning "ATTENTION: Cette opération va SUPPRIMER toutes les tables de la base de données!"
        read -p "Êtes-vous sûr? (tapez 'yes' pour confirmer): " confirm
        if [[ "$confirm" == "yes" ]]; then
            run_flyway_command $PROFILE "clean"
        else
            log_info "Opération annulée"
            exit 0
        fi
        ;;
    launch)
        launch_application $PROFILE
        ;;
    build)
        build_application
        ;;
    schema)
        if [[ "$PROFILE" != "ddl" ]]; then
            log_warning "L'action 'schema' devrait être utilisée avec le profil 'ddl'"
            log_info "Suggestion: ./run.sh ddl schema"
            read -p "Continuer quand même? (y/n): " confirm
            if [[ "$confirm" != "y" ]]; then
                exit 0
            fi
        fi
        generate_schema
        ;;
esac

printf "\n"
log_success "Opération terminée!"
exit 0
