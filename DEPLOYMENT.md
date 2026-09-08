# Deployment Plan — Raspberry Pi

This documents the deploy setup agreed on for the API, to be executed on the Pi after it's
reflashed with Ubuntu Server. Written by a Claude Code session that did **not** have access to
the Pi itself — nothing below has been executed or verified on real hardware. Treat it as a plan
to follow and adjust, not a script to run blindly.

## Architecture

- **Two environments on one Pi**: `prod` (`api.fplultimate.com`) and `stage`
  (`stage.api.fplultimate.com`), each its own Docker container, sharing one Postgres instance
  with separate databases (`fpl_ultimate` / `fpl_ultimate_stage`).
- **Deploy trigger**: push to `main` → builds, tests, and deploys **stage**. Publishing a
  GitHub Release → deploys the same commit to **prod**. This mirrors the pattern already
  documented in the frontend's README (merge → dev, release → prod), just applied to the API.
- **Deploy mechanism**: a GitHub Actions **self-hosted runner installed on the Pi**, not a
  cloud runner pushing in over SSH. The runner polls GitHub outbound, so no inbound port beyond
  80/443 is needed for deploys, and no deploy credential lives in GitHub's secret store. This was
  a deliberate choice after weighing it against SSH-push — see the chat history this doc came
  from if the reasoning needs revisiting.
- **Registry**: GitHub Container Registry (`ghcr.io/adambaldwin1st/fpl-ultimate-api`), tagged
  `:stage` and `:latest`/`:<release-tag>`.
- **nginx** is a pure reverse-proxy gateway for the API's two domains. Whether it also serves the
  frontend's static build is an **open question** — see "Open items" below before assuming either
  way.

## One-time Pi setup

1. **OS hardening** (do this before anything else, and before the port-22 forward is removed if
   you still need remote access during setup):
   - `PasswordAuthentication no` and `PermitRootLogin no` in `sshd_config`, restart `sshd`.
   - Enable `unattended-upgrades` for security patches.
   - Once satisfied, remove the port-22 forward on the router — the deploy pipeline doesn't need
     it (see Architecture above). Keep 80/443 forwarded.

2. **Install Docker + the Compose plugin**:
   ```bash
   curl -fsSL https://get.docker.com | sudo sh
   sudo usermod -aG docker $USER   # log out/in after this
   ```

3. **Create the persistent deploy directory** (holds the real `.env`, kept outside any git
   checkout so `actions/checkout`'s cleanup never touches it):
   ```bash
   sudo mkdir -p /opt/fpl-ultimate
   sudo chown $USER:$USER /opt/fpl-ultimate
   cp .env.example /opt/fpl-ultimate/.env   # from a clone of this repo
   nano /opt/fpl-ultimate/.env               # fill in real DB + football API credentials
   ```
   The deploy workflows reference this exact path (`/opt/fpl-ultimate/.env`) — if you use a
   different path, update `.github/workflows/deploy-stage.yml` and `deploy-release.yml`.

4. **Register the self-hosted runner**:
   - GitHub repo → Settings → Actions → Runners → New self-hosted runner, follow the generated
     `./config.sh` command for this repo.
   - Install as a systemd service so it survives reboots and crashes without manual intervention:
     ```bash
     sudo ./svc.sh install
     sudo ./svc.sh start
     ```
   - Confirm it shows "Idle" in the repo's Runners settings page.

5. **nginx + TLS**:
   ```bash
   sudo apt install nginx certbot python3-certbot-nginx
   sudo cp deploy/nginx/api.fplultimate.com.conf /etc/nginx/sites-available/
   sudo cp deploy/nginx/stage.api.fplultimate.com.conf /etc/nginx/sites-available/
   sudo ln -s /etc/nginx/sites-available/api.fplultimate.com.conf /etc/nginx/sites-enabled/
   sudo ln -s /etc/nginx/sites-available/stage.api.fplultimate.com.conf /etc/nginx/sites-enabled/
   sudo nginx -t && sudo systemctl reload nginx
   sudo certbot --nginx -d api.fplultimate.com
   sudo certbot --nginx -d stage.api.fplultimate.com
   ```
   Requires DNS A records for both domains already pointing at the Pi's public IP — verify this
   (and whether a dynamic-DNS updater is needed, if the IP isn't static) before running certbot.

6. **First deploy**: push to `main` or publish a release from GitHub — either will trigger the
   runner and bring up `postgres` (via `depends_on`) plus the relevant `api-*` container for the
   first time. No manual `docker compose up` should be needed, but it's a reasonable way to sanity
   check the compose file locally on the Pi before trusting the pipeline:
   ```bash
   cd /path/to/a/clone/of/this/repo
   docker compose --env-file /opt/fpl-ultimate/.env up -d
   curl http://localhost:5000/health
   curl http://localhost:5002/health
   ```

## Verifying end to end

```bash
curl https://api.fplultimate.com/health
curl https://stage.api.fplultimate.com/health
```

## Viewing logs

No paid platform needed at this scale. Two options, both already wired up:

- **CLI, zero setup**: `docker compose logs -f api-prod` (or `api-stage`, `postgres`). Log
  rotation is capped at 10MB × 3 files per container via the `x-logging` anchor in
  `docker-compose.yml`, so this can't grow unbounded on the Pi's storage.
- **Browser, live tail across all containers**: [Dozzle](https://dozzle.dev) runs as its own
  service (`docker compose up -d` brings it up along with everything else) at
  `http://<pi-lan-ip>:8081`. It's bound without a host-IP prefix, so it's reachable from any
  device on the home network but not from the internet — 8081 is never forwarded through the
  router. No auth in front of it currently; fine for LAN-only personal use, but don't add an
  8081 port-forward or a public nginx location for it without adding auth first, since it reads
  straight off the Docker socket (mounted read-only, but that's a thin protection — anyone who
  can reach Dozzle's UI can see logs from every container on the box).

## Open items — resolve before assuming either way

- **Frontend hosting model is unresolved.** `fpl-ultimate-frontend` already has a
  `.github/workflows/deploy-frontend.yml` that SFTP/SSH-pushes a build to
  `/var/www/html/fpl-ultimate/frontend/` on the Pi and restarts nginx — implying nginx serves the
  frontend directly, same-origin with the API. That workflow is currently `workflow_dispatch`-only
  (manual), not wired to push/release the way this doc assumes for the API. A GitHub
  Pages + custom domain approach was discussed as an alternative but never implemented — don't
  assume it's the plan. If frontend and API end up on different origins, the API will need CORS
  configured (not done yet); if nginx serves both under one domain, it isn't needed at all.
- **`fpl-ultimate-frontend` has a directory mix-up worth fixing before more frontend work
  happens**: `fpl-ultimate/` contains the real, working app (league standings/matchups
  components); `fpl-ultimate-react/` is a separate, emptier scaffold. That repo's `CLAUDE.md`
  currently tells future sessions the opposite — that `fpl-ultimate/` is the old version to
  ignore. Confirm which is meant to be canonical and fix the docs before trusting them.
- **Postgres credentials/tuning**: `docker-compose.yml` uses simple defaults
  (`fpl_user`/`fpl_password`) meant to be overridden via `.env` — make sure `/opt/fpl-ultimate/.env`
  actually has real values before this goes anywhere near the public internet.
