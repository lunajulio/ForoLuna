# Despliegue en AWS (EC2 + Docker)

Toda la aplicación corre en **un solo servidor EC2** con Docker Compose:

```
Internet ──HTTPS──▶ Caddy :443 ─┬─ /api/*  ─▶ backend (Spring Boot :8080) ─▶ MySQL
                                └─ /*      ─▶ frontend (Next.js :3000)
```

- **Caddy** obtiene y renueva solo el certificado HTTPS de Let's Encrypt.
- Solo los puertos 80 y 443 quedan abiertos. El backend y MySQL no son accesibles desde Internet.
- El frontend llama a la API en `/api` del mismo dominio, así que no hay problemas de CORS ni de contenido mixto.

**Costo aproximado:** $0 en la capa gratuita (primeros 12 meses). Después, unos $10–12/mes por la instancia `t3.micro`, 20 GB de disco y la IP pública.

---

## 0. Antes de empezar (en tu PC)

1. Sube todos los cambios a GitHub; el servidor clonará el repositorio desde ahí:
   ```bash
   git add -A
   git commit -m "Preparar despliegue en AWS"
   git push
   ```
2. Si el repositorio es **privado**, crea un token de GitHub de solo lectura (*Settings → Developer settings → Fine-grained tokens*, permiso *Contents: Read-only*). Lo usarás en el paso 5 como contraseña al clonar.

## 1. Cuenta de AWS y alerta de gastos

1. Crea o entra en tu cuenta en <https://aws.amazon.com>.
2. Activa MFA en el usuario raíz (*IAM → Security credentials*).
3. Crea una alerta de presupuesto para que AWS te avise antes de cobrarte: *Billing and Cost Management → Budgets → Create budget → Use a template → **Zero spend budget*** y pon tu correo.
4. Arriba a la derecha, elige una región cercana, por ejemplo **us-east-1 (N. Virginia)**. Usa la misma región en todos los pasos.

## 2. Crear el servidor (EC2)

*EC2 → Instances → Launch instance*:

| Campo | Valor |
|---|---|
| Name | `foroluna` |
| AMI | **Ubuntu Server 24.04 LTS** (marcada *Free tier eligible*) |
| Instance type | **t3.micro** (o la que aparezca como *Free tier eligible*) |
| Key pair | *Create new key pair* → nombre `foroluna`, tipo RSA, formato **.pem** → se descarga `foroluna.pem`. **Guárdalo bien; no se puede volver a descargar.** |
| Network settings → Edit | Crea un security group `foroluna-sg` con estas reglas de entrada:<br>• SSH (22) – Source: **My IP**<br>• HTTP (80) – Source: Anywhere (0.0.0.0/0)<br>• HTTPS (443) – Source: Anywhere (0.0.0.0/0) |
| Storage | **20 GiB gp3** |

Pulsa **Launch instance**.

## 3. IP fija (Elastic IP)

Sin una IP fija, la dirección cambia cada vez que se detiene la instancia y el dominio deja de funcionar.

1. *EC2 → Elastic IPs → Allocate Elastic IP address → Allocate*.
2. Selecciónala → *Actions → Associate Elastic IP address* → elige la instancia `foroluna` → *Associate*.
3. Anota la IP, por ejemplo `3.85.120.44`.

## 4. Dominio gratuito con sslip.io

No hace falta registrarse en nada: **sslip.io** convierte la IP en un dominio. Cambia los puntos por guiones:

```
IP 3.85.120.44  →  dominio 3-85-120-44.sslip.io
```

Compruébalo desde tu PC con `nslookup 3-85-120-44.sslip.io`; debe devolver tu IP.

> **Alternativa:** si Let's Encrypt rechaza el certificado por límite de solicitudes en sslip.io, crea un subdominio gratis en <https://www.duckdns.org> (por ejemplo `foroluna.duckdns.org`), apúntalo a tu Elastic IP y usa ese dominio.
>
> Si más adelante compras un dominio propio, crea un registro **A** que apunte a la Elastic IP, cambia `DOMAIN` en `.env` y ejecuta `docker compose up -d`.

## 5. Conectarse al servidor y preparar el entorno

Desde PowerShell, en la carpeta donde está `foroluna.pem`:

```powershell
# Solo la primera vez: SSH exige que la llave sea privada
icacls foroluna.pem /inheritance:r /grant:r "$($env:USERNAME):(R)"

ssh -i foroluna.pem ubuntu@3.85.120.44
```

Ya dentro del servidor (Ubuntu):

```bash
# 1) Actualizar el sistema e instalar Docker
sudo apt update && sudo apt upgrade -y
curl -fsSL https://get.docker.com | sudo sh
sudo usermod -aG docker ubuntu

# 2) Memoria swap de 2 GB (la t3.micro solo tiene 1 GB; sin swap, compilar las imágenes falla)
sudo fallocate -l 2G /swapfile
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile
echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab

# 3) Salir y volver a entrar para que se aplique el grupo docker
exit
```

```powershell
ssh -i foroluna.pem ubuntu@3.85.120.44
```

```bash
# 4) Clonar el proyecto
git clone https://github.com/lunajulio/ForoLuna.git
cd ForoLuna
```

## 6. Configurar los secretos

```bash
cp .env.example .env

# Genera contraseñas seguras y cópialas en el .env
openssl rand -hex 24      # para MYSQL_ROOT_PASSWORD
openssl rand -hex 24      # para DB_PASSWORD
openssl rand -base64 48   # para JWT_SECRET

nano .env
```

Así debe quedar el `.env`, con tus valores:

```env
DOMAIN=3-85-120-44.sslip.io
MYSQL_ROOT_PASSWORD=<valor generado>
DB_USER=foroluna
DB_PASSWORD=<valor generado>
JWT_SECRET=<valor generado>
TZ=America/Bogota
```

Guarda con `Ctrl+O`, `Enter` y sal con `Ctrl+X`. Luego protege el archivo con `chmod 600 .env`.

## 7. Levantar la aplicación

```bash
docker compose up -d --build
```

La primera vez tarda **10–15 minutos**, porque compila Maven y Next.js en una máquina pequeña. Para ver el progreso:

```bash
docker compose ps                  # los 4 servicios deben estar "running" / "healthy"
docker compose logs -f backend     # espera "Started ForohubApplication"
docker compose logs caddy          # espera "certificate obtained successfully"
```

Abre **https://3-85-120-44.sslip.io**. Regístrate, inicia sesión y crea un tópico para comprobar que todo funciona.

---

## Operación diaria

### Publicar una nueva versión
En tu PC haz `git push`. Luego, en el servidor:
```bash
cd ~/ForoLuna
git pull
docker compose up -d --build
```

### Ver logs y estado
```bash
docker compose logs -f --tail=100 backend
docker compose ps
free -h && df -h /
```

### Copia de seguridad de la base de datos
```bash
# Crear el respaldo
docker compose exec mysql sh -c 'mysqldump -uroot -p"$MYSQL_ROOT_PASSWORD" foroLuna' > backup-$(date +%F).sql

# Restaurar un respaldo
docker compose exec -T mysql sh -c 'mysql -uroot -p"$MYSQL_ROOT_PASSWORD" foroLuna' < backup-2026-09-27.sql
```

Para respaldos automáticos diarios, ejecuta `crontab -e` y agrega:
```
0 3 * * * cd /home/ubuntu/ForoLuna && docker compose exec -T mysql sh -c 'mysqldump -uroot -p"$MYSQL_ROOT_PASSWORD" foroLuna' > /home/ubuntu/backup-$(date +\%F).sql
```

Además, en *EC2 → Volumes → Create snapshot* puedes guardar una copia completa del disco.

### Migrar tus datos locales (opcional)
En tu PC, genera el respaldo:
```bash
mysqldump -u root -p foroLuna > foroluna.sql
```
Cópialo al servidor:
```powershell
scp -i foroluna.pem foroluna.sql ubuntu@3.85.120.44:~
```
Restáuralo en el servidor con el comando de restauración de arriba.

---

## Problemas frecuentes

| Síntoma | Causa y solución |
|---|---|
| El navegador no carga el sitio | Revisa que el security group tenga abiertos los puertos 80 y 443, y que `docker compose ps` muestre los 4 servicios en ejecución. |
| Advertencia de certificado o error de TLS | Mira `docker compose logs caddy`. Let's Encrypt necesita que el dominio resuelva a tu IP y que el puerto 80 esté abierto. Si ves errores de *rate limit*, usa DuckDNS (paso 4). |
| `docker compose up --build` se queda colgado o aparece "Killed" | Falta memoria. Comprueba el swap con `free -h` (paso 5). |
| El backend se reinicia en bucle | `docker compose logs backend`. Normalmente falta una variable en `.env` o MySQL todavía no terminó de arrancar. |
| Cambié `DB_PASSWORD` y ya no conecta | MySQL solo crea el usuario la **primera** vez. Para empezar de cero (se **borran** los datos): `docker compose down -v && docker compose up -d`. |
| La sesión se cierra al redesplegar | Es normal si cambiaste `JWT_SECRET`. |

## Apagar o eliminar todo

- **Pausar** (sigue cobrando el disco y la IP): *EC2 → Instance state → Stop*.
- **Eliminar por completo:** *Terminate instance* → *Elastic IPs → Release* → borra los snapshots que hayas creado.
