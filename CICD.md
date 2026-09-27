# CI/CD con GitHub Actions

Cada cambio que llega a GitHub pasa por este pipeline ([.github/workflows/ci-cd.yml](.github/workflows/ci-cd.yml)):

```
 git push / Pull Request
          │
          ▼
 ┌──────────────── CI (siempre) ────────────────┐
 │  Backend · tests          Frontend · lint y build │   ← en paralelo, ~3-4 min
 │  (80 tests JUnit +        (ESLint + next build     │
 │   cobertura JaCoCo)        con chequeo de tipos)   │
 └──────────────────────┬────────────────────────┘
                        │ ¿los dos pasaron? ¿es un push a master?
                        ▼
 ┌──────────────── CD (solo master) ────────────┐
 │ 1. GitHub se autentica en AWS (OIDC)          │
 │ 2. Abre el puerto 22 solo para su IP          │
 │ 3. SSH al servidor → scripts/deploy.sh        │   ← ~5-10 min
 │    (git reset al commit + docker compose)     │
 │ 4. Comprueba que frontend y API responden     │
 │ 5. Cierra el puerto 22 (siempre, aunque falle)│
 └───────────────────────────────────────────────┘
```

| Evento | CI | Despliegue |
|---|---|---|
| Push a una rama cualquiera con Pull Request abierto | ✅ | ❌ |
| Merge o push a `master` | ✅ | ✅ solo si CI pasa |
| Botón *Run workflow* en la pestaña Actions | ✅ | ✅ |

GitHub Actions es **gratis y sin límite de minutos** en repositorios públicos.

---

## Configuración inicial (una sola vez)

Necesitas estos datos a mano:
- **ID de tu cuenta de AWS:** 12 dígitos, arriba a la derecha en la consola.
- **Región:** por ejemplo `us-east-1`.
- **ID del security group:** *EC2 → Security Groups → foroluna-sg*, con el formato `sg-0abc...`.
- **Tu Elastic IP.**
- **Tu dominio:** por ejemplo `3-85-120-44.sslip.io`.

### 1. AWS: permitir que GitHub se autentique (OIDC)

Con OIDC, GitHub obtiene credenciales temporales de AWS en cada ejecución, así que **no se guarda ninguna clave de AWS en GitHub**.

*IAM → Identity providers → Add provider*:
- Provider type: **OpenID Connect**
- Provider URL: `https://token.actions.githubusercontent.com`
- Audience: `sts.amazonaws.com`
- Pulsa **Add provider**.

### 2. AWS: rol que GitHub puede usar

*IAM → Roles → Create role → **Custom trust policy***. Pega esto, cambiando `<ACCOUNT_ID>`:

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": {
        "Federated": "arn:aws:iam::<ACCOUNT_ID>:oidc-provider/token.actions.githubusercontent.com"
      },
      "Action": "sts:AssumeRoleWithWebIdentity",
      "Condition": {
        "StringEquals": {
          "token.actions.githubusercontent.com:aud": "sts.amazonaws.com",
          "token.actions.githubusercontent.com:sub": "repo:lunajulio/ForoLuna:environment:production"
        }
      }
    }
  ]
}
```

La condición `sub` hace que **solo** el job de despliegue de tu repositorio (entorno `production`) pueda usar este rol.

Sigue estos pasos del asistente:
1. En **Add permissions** no marques nada y pulsa *Next*.
2. Ponle de nombre `github-actions-foroluna-deploy` y pulsa *Create role*.
3. Abre el rol recién creado → *Add permissions → Create inline policy → JSON*.
4. Pega la política de abajo, cambiando `<REGION>`, `<ACCOUNT_ID>` y `<SG_ID>`.

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "ec2:AuthorizeSecurityGroupIngress",
        "ec2:RevokeSecurityGroupIngress"
      ],
      "Resource": "arn:aws:ec2:<REGION>:<ACCOUNT_ID>:security-group/<SG_ID>"
    }
  ]
}
```

5. Llámala `abrir-ssh-deploy` y créala.

Este rol **solo puede abrir y cerrar reglas de ese security group**. Si alguien lo robara, no podría hacer nada más en tu cuenta.

6. Copia el **ARN del rol**, con el formato `arn:aws:iam::123456789012:role/github-actions-foroluna-deploy`. Lo usarás en el paso 4.

### 3. Servidor: llave SSH exclusiva para despliegues

Usa una llave distinta de tu `foroluna.pem`, para poder revocarla sin perder tu propio acceso. Conéctate al servidor como siempre y ejecuta:

```bash
ssh-keygen -t ed25519 -f ~/.ssh/github_deploy -N "" -C "github-actions-deploy"
cat ~/.ssh/github_deploy.pub >> ~/.ssh/authorized_keys
cat ~/.ssh/github_deploy
```

El último comando muestra la **llave privada**. Cópiala completa, desde `-----BEGIN OPENSSH PRIVATE KEY-----` hasta `-----END OPENSSH PRIVATE KEY-----` incluidas, para el paso 4. Después bórrala del servidor, porque solo la necesita GitHub:

```bash
rm ~/.ssh/github_deploy
```

Para **revocarla** en el futuro, borra la línea que termina en `github-actions-deploy` dentro de `~/.ssh/authorized_keys`.

### 4. GitHub: secretos y variables

En el repositorio: *Settings → Secrets and variables → Actions*.

**Pestaña Secrets** (cifrados, nunca se muestran en los logs) → *New repository secret*:

| Nombre | Valor |
|---|---|
| `AWS_ROLE_ARN` | El ARN del rol del paso 2 |
| `EC2_SSH_KEY` | La llave privada del paso 3, completa |

**Pestaña Variables** → *New repository variable*:

| Nombre | Ejemplo |
|---|---|
| `AWS_REGION` | `us-east-1` |
| `AWS_SG_ID` | `sg-0abc123def456` |
| `EC2_HOST` | `3.85.120.44` (tu Elastic IP) |
| `DOMAIN` | `3-85-120-44.sslip.io` |

### 5. GitHub: proteger la rama `master` (recomendado)

Así nadie puede subir a `master` código que no pase los tests, tampoco tú por error.

*Settings → Branches → Add branch ruleset* (o *Add rule*):
- Branch: `master`
- ✅ **Require a pull request before merging**
- ✅ **Require status checks to pass**: añade `Backend · tests` y `Frontend · lint y build`. Aparecen en la lista después de la primera ejecución del workflow.

**Opcional:** para aprobar cada despliegue con un clic, entra en *Settings → Environments → production* y activa **Required reviewers** con tu usuario. El pipeline se detendrá antes de desplegar hasta que lo apruebes.

---

## Flujo de trabajo diario

```bash
# 1. Crea una rama para el cambio
git checkout -b mejora-comentarios

# 2. Programa, prueba en local y confirma
git add -A
git commit -m "Permitir editar comentarios"

# 3. Sube la rama
git push -u origin mejora-comentarios
```

4. En GitHub, abre un **Pull Request** hacia `master`. CI se ejecuta solo y verás ✅ o ❌ en el PR. Si falla, abre el detalle, corrige y vuelve a hacer push a la misma rama.
5. Con todo en verde, pulsa **Merge**. Esto dispara CI otra vez sobre `master` y después el **despliegue automático**.
6. Sigue el progreso en la pestaña **Actions**. Al terminar, el entorno *production* muestra el enlace a la app.

> **Cambios en la base de datos:** si el backend necesita una tabla o columna nueva, crea un archivo de migración nuevo en `backend/src/main/resources/db/migration/`, por ejemplo `V6__agregar-likes.sql`. Flyway lo aplica automáticamente al arrancar el backend en el servidor. **Nunca modifiques una migración que ya se aplicó** (V1–V5): Flyway detecta el cambio y el backend no arranca.

## Volver a una versión anterior (rollback)

**Opción recomendada:** revierte el commit problemático y deja que el pipeline despliegue.
```bash
git revert <sha-del-commit-malo>
git push
```

**Emergencia:** despliega directamente en el servidor un commit que funcionaba.
```bash
bash ~/ForoLuna/scripts/deploy.sh <sha-que-funcionaba>
```
El siguiente push a `master` volverá a desplegar lo que haya en `master`, así que después haz también el `git revert`.

## Si el despliegue falla

| Paso que falla | Causa probable |
|---|---|
| *Credenciales de AWS* | `AWS_ROLE_ARN` mal copiado, o la condición `sub` de la trust policy no coincide. Debe ser exactamente `repo:lunajulio/ForoLuna:environment:production`. |
| *Abrir SSH* | `AWS_SG_ID` o `AWS_REGION` incorrectos, o la política del rol apunta a otro security group. |
| *Desplegar* (`Permission denied (publickey)`) | `EC2_SSH_KEY` incompleta, o no se añadió la llave pública a `authorized_keys`. |
| *Desplegar* (error de Docker o Maven) | Mira el log del paso: es la misma salida que verías en el servidor. |
| *Verificar que la app responde* | El backend no arrancó. En el servidor: `docker compose logs backend`. |

Durante unos 30 segundos del despliegue, mientras se reinicia el backend, la API puede responder con error. Es normal en un despliegue con un solo servidor.
