#!/usr/bin/env bash
# Despliega en el servidor la versión indicada (por defecto, lo último de master).
# Lo ejecuta GitHub Actions por SSH, pero también se puede lanzar a mano:
#   bash ~/ForoLuna/scripts/deploy.sh            -> último commit de master
#   bash ~/ForoLuna/scripts/deploy.sh <commit>   -> un commit concreto (ej. para volver atrás)
set -euo pipefail

REF="${1:-origin/master}"
cd "$(dirname "$0")/.."

echo "==> Descargando cambios"
git fetch --quiet origin master
# reset --hard deja el código idéntico al commit; .env no se toca porque está en .gitignore
git reset --hard "$REF"
echo "    Versión: $(git log -1 --format='%h %s')"

echo "==> Construyendo y reiniciando contenedores (solo los que cambiaron)"
docker compose up -d --build --remove-orphans

echo "==> Limpiando imágenes antiguas"
docker image prune -f > /dev/null

docker compose ps
echo "==> Despliegue terminado"
