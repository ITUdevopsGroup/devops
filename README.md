# ITU MiniTwit DevOps Project

[![Backend CI](https://github.com/ITUdevopsGroup/devops/actions/workflows/ci_backend.yml/badge.svg)](https://github.com/ITUdevopsGroup/devops/actions/workflows/ci_backend.yml)
[![Frontend CI](https://github.com/ITUdevopsGroup/devops/actions/workflows/ci_frontend.yml/badge.svg)](https://github.com/ITUdevopsGroup/devops/actions/workflows/ci_frontend.yml)
[![Deploy](https://github.com/ITUdevopsGroup/devops/actions/workflows/cd.yml/badge.svg)](https://github.com/ITUdevopsGroup/devops/actions/workflows/cd.yml)

This repository contains Group O's ITU MiniTwit system for the DevOps course.
It is a small Twitter-like application with a Spring Boot backend, a Next.js
frontend, PostgreSQL persistence, Docker-based local development, Docker Swarm
production deployment, and a Prometheus/Grafana/Loki monitoring stack.

The production system is built around Docker images published to Docker Hub and
deployed to a DigitalOcean-hosted Docker Swarm.

## Live System

- Frontend: <https://rollbackandrelax.dk>
- API: <https://api.rollbackandrelax.dk>
- Grafana: <https://grafana.rollbackandrelax.dk>
- Traefik dashboard: <https://traefik.rollbackandrelax.dk>
- Swarm visualizer: <https://swarm.rollbackandrelax.dk>

Some operational dashboards require credentials.

## Contents

- [Live System](#live-system)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Repository Layout](#repository-layout)
- [System Overview](#system-overview)
- [Prerequisites](#prerequisites)
- [Configuration](#configuration)
- [Installation and Local Development](#installation-and-local-development)
- [Usage](#usage)
- [Tests and Quality Checks](#tests-and-quality-checks)
- [CI/CD](#cicd)
- [Production Deployment](#production-deployment)
- [Operating Production](#operating-production)
- [Contributing Changes to Production](#contributing-changes-to-production)
- [Support](#support)
- [Security and Secrets](#security-and-secrets)
- [Project Status](#project-status)
- [License](#license)

## Features

- Public and user timelines.
- User registration and login.
- Posting messages.
- Following and unfollowing users.
- MiniTwit simulator-compatible API endpoints.
- PostgreSQL-backed persistence through Spring Data JPA.
- Prometheus metrics, Grafana dashboards, Loki logs, and Alloy log collection.
- Docker Compose setup for local development and integration testing.
- Docker Swarm deployment with Traefik, HTTPS, and stack-based service updates.

## Tech Stack

| Area | Technology |
| --- | --- |
| Backend | Java 21, Spring Boot 4, Spring Web, Spring Data JPA, Micrometer, Maven |
| Frontend | Next.js 16, React 19, Tailwind CSS, npm |
| Database | PostgreSQL 16 |
| Local runtime | Docker Compose |
| Production runtime | Docker Swarm, Traefik v3 |
| Monitoring | Prometheus, Grafana, node-exporter |
| Logging | Loki, Grafana Alloy |
| Infrastructure | DigitalOcean, Terraform |
| CI/CD | GitHub Actions, Docker Hub, Cosign |
| Tests | JUnit/Maven, pytest, requests, Playwright |

## Repository Layout

| Path | Purpose |
| --- | --- |
| `devops_backend/` | Java 21 Spring Boot backend, REST API, simulator API, JPA/PostgreSQL persistence, actuator metrics. |
| `devops_frontend/` | Next.js frontend for timeline, login, registration, following users, and posting messages. |
| `docker-compose.yml` | Local development stack: backend, frontend, PostgreSQL, Prometheus, Grafana, Loki, Alloy, node-exporter. |
| `docker-compose.ci.yml` | Minimal backend + PostgreSQL stack used by API integration tests. |
| `docker-compose.prod.yml` | Legacy single-host production compose file. Current production deployment uses `swarm/`. |
| `swarm/` | Docker Swarm stack files for Traefik proxy, app services, monitoring, and swarm visualizer. |
| `monitoring/`, `alloy/`, `loki-config.yaml` | Prometheus, Grafana dashboard/datasource provisioning, Loki, and Alloy log collection config. |
| `terraform/` | DigitalOcean droplet definition and example variables. |
| `.github/workflows/` | CI, Docker image publishing, report build, and Swarm deployment workflows. |
| `test.py` | Backend simulator/API integration tests. |
| `test_ui.py` | Playwright UI tests for the frontend. These exist, but the frontend CI workflow currently has a placeholder instead of running them. |
| `report/` | Course report source and PDF build workflow input. |

## System Overview

The backend exposes two API surfaces:

- The MiniTwit simulator API: `POST /register`, `GET /msgs`, `GET /msgs/{username}`, `POST /msgs/{username}`, `GET /fllws/{username}`, `POST /fllws/{username}`, and `GET /latest`.
- The frontend-facing legacy JSON API: `GET /`, `GET /user`, `GET /register`, `GET /spec_user`, `GET /follow`, `GET /unfollow`, `GET /add_message`, and `GET /stats`.

The backend runs on port `5001` and stores data in PostgreSQL. Hibernate manages the database schema (`spring.jpa.hibernate.ddl-auto=update`), so `schema.sql` is intentionally empty.

The frontend runs on port `3000`. It reads `NEXT_PUBLIC_API_HOST` and `NEXT_PUBLIC_API_PORT` and builds API URLs as `<host>:<port>/<path>`.

The observability stack includes:

- Prometheus scraping the backend `/actuator/prometheus` endpoint and node-exporter.
- Grafana with provisioned dashboards and datasources.
- Loki for log storage.
- Grafana Alloy for collecting Docker container logs.

## Prerequisites

For local development:

- Docker and Docker Compose
- Java 21 and Maven 3.9.x, if running the backend outside Docker
- Node.js 20 and npm, if running the frontend outside Docker
- Python 3.12 with `pytest` and `requests`, if running API tests locally
- `pytest-playwright` and Chromium, if running UI tests locally

For production/infrastructure work:

- Access to the DigitalOcean account and `do_token`
- Docker Swarm manager access to the production server
- Docker Hub credentials for `andersfrimann/devops_backend` and `andersfrimann/devops_frontend`
- GitHub repository secrets:
  - `DOCKER_PASSWORD`
  - `SSH_KEY_FOR_DEPLOYMENT`

## Configuration

Backend configuration is controlled by environment variables:

| Variable | Description | Local Compose value |
| --- | --- | --- |
| `MINITWIT_DB_URL` | JDBC URL for PostgreSQL. | `jdbc:postgresql://db:5432/minitwit` |
| `MINITWIT_DB_USER` | Database username. | `minitwit_user` |
| `MINITWIT_DB_PASSWORD` | Database password. | `minitwit_password` |

Frontend configuration is controlled by:

| Variable | Description | Local value |
| --- | --- | --- |
| `NEXT_PUBLIC_API_HOST` | API scheme and host. Do not include a trailing slash. | `http://localhost` |
| `NEXT_PUBLIC_API_PORT` | API port. | `5001` |

Other deployment variables:

| Variable | Used by | Description |
| --- | --- | --- |
| `GRAFANA_PASSWORD` | `docker-compose.yml` | Local Grafana admin password. |
| `ACME_EMAIL` | `swarm/proxy-stack.yml` | Email used by Traefik/Let's Encrypt. |
| `FORCE_RELOAD_VAR` | `swarm/proxy-stack.yml` | Any changing value that forces the Traefik service to redeploy. |
| `do_token` | `terraform/terraform.tfvars` | DigitalOcean API token. Keep the real `terraform.tfvars` file out of git. |

## Installation and Local Development

The easiest way to run the full local system is Docker Compose:

```bash
GRAFANA_PASSWORD=devops123 docker compose up --build
```

Local URLs:

- Frontend: <http://localhost:3000>
- Backend/API: <http://localhost:5001>
- Backend health: <http://localhost:5001/actuator/health>
- Backend metrics: <http://localhost:5001/actuator/prometheus>
- PostgreSQL: `localhost:5432`
- Prometheus: <http://localhost:9090>
- Grafana: <http://localhost:3001> (`admin` / `GRAFANA_PASSWORD`)
- Loki: <http://localhost:3100>
- Alloy: <http://localhost:12345>

Stop the stack and remove local volumes:

```bash
docker compose down -v
```

### Run Backend Only

Start PostgreSQL first, then run the Spring Boot app:

```bash
cd devops_backend
export MINITWIT_DB_URL="jdbc:postgresql://localhost:5432/minitwit"
export MINITWIT_DB_USER="minitwit_user"
export MINITWIT_DB_PASSWORD="minitwit_password"
mvn spring-boot:run
```

### Run Frontend Only

Start the backend first, then run the Next.js development server:

```bash
cd devops_frontend
npm install
NEXT_PUBLIC_API_HOST=http://localhost NEXT_PUBLIC_API_PORT=5001 npm run dev
```

The tracked `devops_frontend/.env.local` contains the same localhost API defaults.

## Usage

With the local stack running, open <http://localhost:3000>. The root route
redirects to `/timeline`, where you can view the public timeline. Use `/register`
to create a user, `/login` to sign in, and the timeline UI to post messages or
follow other users.

The backend can also be checked directly:

```bash
curl http://localhost:5001/actuator/health
curl http://localhost:5001/latest
curl http://localhost:5001/stats
```

Simulator endpoints under `/msgs` and `/fllws` require the simulator basic auth
header used by `test.py`.

## Tests and Quality Checks

Backend unit tests:

```bash
cd devops_backend
mvn test
```

Backend Checkstyle:

```bash
cd devops_backend
mvn checkstyle:check
```

Backend API integration tests:

```bash
docker compose -f docker-compose.ci.yml up -d --build backend
pytest -v -x test.py
docker compose -f docker-compose.ci.yml down -v
```

Frontend lint:

```bash
cd devops_frontend
npm install
npm run lint
```

Frontend UI tests are in `test_ui.py`. To run them locally:

```bash
python -m pip install pytest pytest-playwright
python -m playwright install chromium
GRAFANA_PASSWORD=devops123 docker compose up -d --build
pytest -v -x test_ui.py
docker compose down -v
```

## CI/CD

The GitHub workflows are split by responsibility:

- `ci_backend.yml`
  - Runs Maven tests.
  - Starts `docker-compose.ci.yml` and runs `test.py`.
  - Runs Checkstyle and Hadolint.
  - On non-PR pushes, builds, signs, and pushes `registry.hub.docker.com/andersfrimann/devops_backend`.
- `ci_frontend.yml`
  - Builds the full Docker Compose stack and checks that frontend/backend start.
  - Runs ESLint.
  - On non-PR pushes, builds, signs, and pushes `registry.hub.docker.com/andersfrimann/devops_frontend`.
  - The Playwright test step is currently a TODO placeholder.
- `cd.yml`
  - Runs on pushes to `main` and manual `workflow_dispatch`.
  - SSHes into `167.172.111.97`, pulls `/root/devops`, and redeploys the Swarm stacks with `--resolve-image always`.
- `build-report.yml`
  - Builds `report/report.md` into a PDF artifact with Pandoc and LaTeX.

Pull requests run the checks but do not push Docker images. Pushes to `main` publish images and trigger the production deployment workflow.

## Production Deployment

Current production is Docker Swarm, not the legacy `docker-compose.prod.yml` path.

Production services:

- `proxy`: Traefik v3 reverse proxy with HTTP to HTTPS redirection, Let's Encrypt certificates, dashboard routing, and the shared `minitwit-public` overlay network.
- `app`: backend, frontend, and PostgreSQL.
- `monitoring`: Prometheus, Grafana, Loki, Alloy, and node-exporter.
- `swarm`: Docker Swarm visualizer.

Public routes configured in the Swarm files:

- Frontend: <https://rollbackandrelax.dk>
- API: <https://api.rollbackandrelax.dk>
- Legacy simulator/API port: `5001` on the production host
- Grafana: <https://grafana.rollbackandrelax.dk>
- Traefik dashboard: <https://traefik.rollbackandrelax.dk>
- Swarm visualizer: <https://swarm.rollbackandrelax.dk>

The app stack uses:

- Backend image: `andersfrimann/devops_backend:main`
- Frontend image: `andersfrimann/devops_frontend:main`
- PostgreSQL image: `postgres:16`
- PostgreSQL data volume: `db_data_prod`
- Database placement constraint: `node.hostname == MiniTwit-Database`

### Provision Infrastructure

The `terraform/` directory defines two DigitalOcean droplets:

- `MiniTwit-Database`: `s-2vcpu-4gb`, Ubuntu 24.04, backups and monitoring enabled.
- `MiniTwit-Worker`: `s-1vcpu-2gb`, Ubuntu 24.04, backups and monitoring enabled.

Create `terraform/terraform.tfvars` from the example and apply:

```bash
cd terraform
cp terraform.tfvars.example terraform.tfvars
# edit terraform.tfvars with the real do_token
terraform init
terraform plan
terraform apply
```

Docker installation, Swarm initialization/joining, SSH access, DNS, and cloning this repository to `/root/devops` are currently server bootstrap steps outside Terraform.

### Manual Swarm Deployment

From the production server, in `/root/devops`:

```bash
export ACME_EMAIL="your-email@example.com"
export FORCE_RELOAD_VAR="$(date +%s)"

docker stack deploy --compose-file swarm/proxy-stack.yml proxy --resolve-image always
docker stack deploy --compose-file swarm/app-stack.yml app --resolve-image always
docker stack deploy --compose-file swarm/docker-swarm-ui.yml swarm --resolve-image always
docker stack deploy --compose-file swarm/monitoring-stack.yml monitoring --resolve-image always
```

Deploy `proxy` first because it creates the shared `minitwit-public` overlay network used by the app, monitoring, and visualizer stacks.

## Operating Production

Useful Swarm commands:

```bash
docker stack ls
docker stack ps app
docker stack services app
docker service logs app_backend
docker service logs app_frontend
docker service logs proxy_traefik
docker service update --force app_backend
docker service update --force app_frontend
```

To inspect the database volume and containers:

```bash
docker service ps app_db
docker volume ls
```

Prometheus scrapes the backend through the Swarm network at `backend:5001/actuator/prometheus`. Alloy reads Docker logs through `/var/run/docker.sock` and ships them to Loki.

## Contributing Changes to Production

1. Create a feature branch from `main`.
2. Make the code, Docker, infrastructure, or documentation change.
3. Run the relevant local checks from the "Tests and Quality Checks" section.
4. Open a pull request into `main`.
5. Confirm the backend/frontend CI checks pass.
6. Merge to `main`.
7. The push to `main` builds and pushes Docker images, then `cd.yml` updates the production Swarm.
8. Verify the deployment with:

```bash
docker stack ps app
docker service logs app_backend --tail 100
docker service logs app_frontend --tail 100
curl -f https://api.rollbackandrelax.dk/latest
```

For emergency or manual redeploys, run the Swarm deployment commands directly on the production server.

## Support

For project questions, use the GitHub repository issues or pull request
discussion in `ITUdevopsGroup/devops`. For production access or secrets, contact
the course group members who administer the DigitalOcean and GitHub accounts.

## Security and Secrets

- Do not commit real `.env`, `terraform.tfvars`, private keys, or access tokens.
- GitHub Actions should receive secrets through repository secrets.
- Production Docker image publishing uses `DOCKER_PASSWORD`.
- Production SSH deployment uses `SSH_KEY_FOR_DEPLOYMENT`.
- Traefik dashboard and Swarm visualizer are protected with basic auth labels in the Swarm stack files.
- The simulator API tests use basic auth credentials `simulator:super_safe!`; the backend currently protects `/msgs` and `/fllws` with this header.

## Project Status

- Active course project for Spring 2026.
- Docker Swarm is the active production deployment path.
- The frontend Playwright tests are present in `test_ui.py`, but the GitHub frontend workflow currently does not execute them.
- `docker-compose.prod.yml` is kept for legacy single-host deployment, while the active production path is Docker Swarm.
- Database backups are enabled at the DigitalOcean droplet level. A repository-managed PostgreSQL backup/restore workflow is still a TODO.

## License

No license file is currently included in this repository. Do not reuse or
redistribute the code outside the course/project context without permission from
the repository owners.
