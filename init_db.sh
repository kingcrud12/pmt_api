#!/usr/bin/env bash
set -euo pipefail

source env.sh

# =========================
# TEST VPN (TCP PORT) AVANT MYSQL
# =========================
echo "=== TEST VPN / PORT MYSQL ==="
if ! nc -z -w 3 "${WG_SERVER_IP}" "${MYSQL_PORT}"; then
  echo "KO: impossible de joindre ${WG_SERVER_IP}:${MYSQL_PORT} (VPN non fonctionnel ou MySQL non joignable)"
  exit 1
fi
echo "OK: ${WG_SERVER_IP}:${MYSQL_PORT} joignable"

# =========================
# MYSQL (forcé en TCP distant)
# =========================
MYSQL_BIN="${MYSQL_BIN:-mysql}"

mysql_root_args=(--protocol=TCP -h "${WG_SERVER_IP}" -P "${MYSQL_PORT}" -u "${MYSQL_DISTANT_ADMIN}")
if [[ -n "${MYSQL_ROOT_PASSWORD:-}" ]]; then
  # pas en clair dans la commande (utilise MYSQL_PWD)
  export MYSQL_PWD="${MYSQL_ROOT_PASSWORD}"
else
  unset MYSQL_PWD || true
fi

# =========================
# PROVISIONING (bases + users + droits)
# =========================
"${MYSQL_BIN}" "${mysql_root_args[@]}" --verbose --show-warnings <<SQL

-- =========================
-- BASES DE DONNÉES
-- =========================
SET @db := '${DB_DEV}';
SELECT IF(
  EXISTS (SELECT 1 FROM information_schema.schemata WHERE schema_name = @db),
  CONCAT('INFO: base ', @db, ' existe déjà'),
  CONCAT('INFO: création de la base ', @db)
) AS msg;
CREATE DATABASE IF NOT EXISTS \`${DB_DEV}\`
  CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

SET @db := '${DB_STAGING}';
SELECT IF(
  EXISTS (SELECT 1 FROM information_schema.schemata WHERE schema_name = @db),
  CONCAT('INFO: base ', @db, ' existe déjà'),
  CONCAT('INFO: création de la base ', @db)
) AS msg;
CREATE DATABASE IF NOT EXISTS \`${DB_STAGING}\`
  CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

SET @db := '${DB_PROD}';
SELECT IF(
  EXISTS (SELECT 1 FROM information_schema.schemata WHERE schema_name = @db),
  CONCAT('INFO: base ', @db, ' existe déjà'),
  CONCAT('INFO: création de la base ', @db)
) AS msg;
CREATE DATABASE IF NOT EXISTS \`${DB_PROD}\`
  CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

-- =========================
-- UTILISATEURS (création + MAJ mot de passe)
-- =========================
SET @user := 'pmt_dev';
SELECT IF(
  EXISTS (SELECT 1 FROM mysql.user WHERE user = @user),
  CONCAT('INFO: utilisateur ', @user, ' existe → mot de passe mis à jour'),
  CONCAT('INFO: création de l''utilisateur ', @user)
) AS msg;
CREATE USER IF NOT EXISTS 'pmt_dev'@'%' IDENTIFIED BY '${PMT_DEV_DB_PASSWORD}';
ALTER USER 'pmt_dev'@'%' IDENTIFIED WITH caching_sha2_password BY '${PMT_DEV_DB_PASSWORD}';

SET @user := 'pmt_staging';
SELECT IF(
  EXISTS (SELECT 1 FROM mysql.user WHERE user = @user),
  CONCAT('INFO: utilisateur ', @user, ' existe → mot de passe mis à jour'),
  CONCAT('INFO: création de l''utilisateur ', @user)
) AS msg;
CREATE USER IF NOT EXISTS 'pmt_staging'@'%' IDENTIFIED BY '${PMT_STAGING_DB_PASSWORD}';
ALTER USER 'pmt_staging'@'%' IDENTIFIED WITH caching_sha2_password BY '${PMT_STAGING_DB_PASSWORD}';

SET @user := 'pmt_prod';
SELECT IF(
  EXISTS (SELECT 1 FROM mysql.user WHERE user = @user),
  CONCAT('INFO: utilisateur ', @user, ' existe → mot de passe mis à jour'),
  CONCAT('INFO: création de l''utilisateur ', @user)
) AS msg;
CREATE USER IF NOT EXISTS 'pmt_prod'@'%' IDENTIFIED BY '${PMT_PROD_DB_PASSWORD}';
ALTER USER 'pmt_prod'@'%' IDENTIFIED WITH caching_sha2_password BY '${PMT_PROD_DB_PASSWORD}';

SET @user := 'pmt_admin';
SELECT IF(
  EXISTS (SELECT 1 FROM mysql.user WHERE user = @user),
  CONCAT('INFO: utilisateur ', @user, ' existe → mot de passe mis à jour'),
  CONCAT('INFO: création de l''utilisateur ', @user)
) AS msg;
CREATE USER IF NOT EXISTS 'pmt_admin'@'%' IDENTIFIED BY '${PMT_ADMIN_DB_PASSWORD}';
ALTER USER 'pmt_admin'@'%' IDENTIFIED WITH caching_sha2_password BY '${PMT_ADMIN_DB_PASSWORD}';

-- =========================
-- PRIVILÈGES
-- =========================

-- DEV : tous les droits sur sa base
GRANT ALL PRIVILEGES ON \`${DB_DEV}\`.* TO 'pmt_dev'@'%';

-- STAGING/PROD : droits applicatifs typiques
GRANT SELECT, INSERT, UPDATE, DELETE, EXECUTE
  ON \`${DB_STAGING}\`.* TO 'pmt_staging'@'%';

GRANT SELECT, INSERT, UPDATE, DELETE, EXECUTE
  ON \`${DB_PROD}\`.* TO 'pmt_prod'@'%';

-- ADMIN : tous les droits sur toutes les bases PMT
GRANT ALL PRIVILEGES ON \`${DB_DEV}\`.* TO 'pmt_admin'@'%';
GRANT ALL PRIVILEGES ON \`${DB_STAGING}\`.* TO 'pmt_admin'@'%';
GRANT ALL PRIVILEGES ON \`${DB_PROD}\`.* TO 'pmt_admin'@'%';

FLUSH PRIVILEGES;

SQL

echo "OK — Provisioning exécuté."

# =========================
# TESTS DE CONNEXION MYSQL (toujours -h)
# =========================
echo
echo "=== TESTS DE CONNEXION MYSQL VIA WG ==="

test_mysql_login() {
  local user="$1"
  local pass="$2"
  local db="$3"

  "${MYSQL_BIN}" --protocol=TCP -h "${WG_SERVER_IP}" -P "${MYSQL_PORT}" \
    -u "${user}" -p"${pass}" --ssl-mode=REQUIRED -D "${db}" -e "SELECT 1;" 2>&1 | grep -v "Using a password on the command line"
}

if test_mysql_login "${PMT_DEV_DB_USER}" "${PMT_DEV_DB_PASSWORD}" "${DB_DEV}"; then
  echo "OK: connexion DEV (pmt_dev -> ${DB_DEV})"
else
  echo "KO: connexion DEV (pmt_dev -> ${DB_DEV})"
fi

if test_mysql_login "${PMT_STAGING_DB_USER}" "${PMT_STAGING_DB_PASSWORD}" "${DB_STAGING}"; then
  echo "OK: connexion STAGING (pmt_staging -> ${DB_STAGING})"
else
  echo "KO: connexion STAGING (pmt_staging -> ${DB_STAGING})"
fi

if test_mysql_login "${PMT_PROD_DB_USER}" "${PMT_PROD_DB_PASSWORD}" "${DB_PROD}"; then
  echo "OK: connexion PROD (pmt_prod -> ${DB_PROD})"
else
  echo "KO: connexion PROD (pmt_prod -> ${DB_PROD})"
fi

if test_mysql_login "${PMT_ADMIN_DB_USER}" "${PMT_ADMIN_DB_PASSWORD}" "${DB_DEV}"; then
  echo "OK: connexion ADMIN (pmt_admin -> ${DB_DEV})"
else
  echo "KO: connexion ADMIN (pmt_admin -> ${DB_DEV})"
fi

# Nettoyage
unset MYSQL_PWD || true
