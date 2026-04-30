# Security Improvements Report

This report summarizes the concrete hardening steps implemented after the security assessment.

## A. Firewall (UFW) setup and verification

UFW should be configured on the production server with default deny incoming and only required ports open.

Applied command set (run on server):

```bash
sudo ufw --force reset
sudo ufw default deny incoming
sudo ufw default allow outgoing
sudo ufw allow 22/tcp
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw enable
sudo ufw status verbose
```

Expected state:
- Only `22/tcp`, `80/tcp`, and `443/tcp` are allowed.
- No direct public DB/monitoring/application ports.

Docker/UFW caveat addressed:
- Docker `-p` can bypass UFW via iptables rules.
- To reduce exposure risk, production compose and swarm configs were changed to stop publishing unnecessary ports.
- Verify from an external host with `nmap` (or similar) that blocked ports are truly closed.

## C. Container hardening

### 1) Non-root containers

Implemented:
- `devops_backend/Dockerfile`
  - Runtime base changed to `eclipse-temurin:21-jre`
  - Added system user `appuser` (`uid 10001`)
  - Added `USER appuser`
- `devops_frontend/Dockerfile`
  - Base updated from `node:20-alpine` to `node:22-alpine`
  - Added `USER node`

Result:
- Application containers no longer run as root by default.

### 2) Reduce exposed container/network attack surface

Implemented:
- `docker-compose.prod.yml`
  - Removed public host port mappings for:
    - Postgres (`5432`)
    - Prometheus (`9090`)
    - Loki (`3100`)
    - Alloy (`12345`)
    - Grafana (`3001`)
- `swarm/proxy-stack.yml`
  - Removed legacy public `6001` entrypoint
- `swarm/app-stack.yml`
  - Removed legacy plain HTTP backend router tied to `6001`

Result:
- Fewer internet-exposed services and better alignment with least-exposure principle.

### 3) Keep base images current

Implemented:
- Frontend image updated to `node:22-alpine`.
- Backend runtime image narrowed to `eclipse-temurin:21-jre` (smaller runtime footprint than full JDK image).

## D. CI pipeline security enhancements (shift-left)

Security checks now run before publish/deploy stages and fail early on severe findings.

Implemented in both workflows:

- `.github/workflows/ci_backend.yml`
- `.github/workflows/ci_frontend.yml`

### Added static application security scanning (SAST)

- Added `semgrep` job using `returntocorp/semgrep-action@v1`
- Ruleset: `p/security-audit`

### Added container vulnerability scanning

- Added `image-scan` job:
  - Builds local image (`docker build ...:scan`)
  - Runs Trivy (`aquasecurity/trivy-action@0.24.0`)
  - Fails on `HIGH,CRITICAL` vulnerabilities (`exit-code: 1`)
  - Scans both OS and library vulnerabilities

### Enforced gating in CI

- Backend build now requires: `semgrep`, tests, checkstyle, hadolint, and Trivy scan.
- Frontend build now requires: `semgrep`, tests/lint, and Trivy scan.

This ensures vulnerable code/images are blocked before image publishing and downstream deployment.

