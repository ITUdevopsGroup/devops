# Security Assessment - MiniTwit Web Application

## Scope and Method

This assessment is based on a manual review of the current source code and deployment configuration for:

- Backend API (`devops_backend`, Spring Boot)
- Frontend (`devops_frontend`, Next.js client)
- Deployment and CI/CD (`docker-compose*.yml`, `swarm/*.yml`, `.github/workflows/cd.yml`)

The goal is to identify practical risks, estimate likelihood and impact, prioritize via a risk matrix, and define treatment actions.

---

## A. Risk Identification

### 1) Assets

The key assets to protect are:

1. **User account data**
   - Usernames, email addresses, password hashes
2. **Authentication secrets and credentials**
   - API auth credentials, database credentials, deployment SSH key
3. **Application integrity**
   - Correctness of follow/message actions and latest-event state
4. **Database integrity and availability**
   - Postgres data and service uptime
5. **Infrastructure and production host**
   - Docker Swarm services, reverse proxy, CI/CD deployment path
6. **Operational telemetry**
   - Metrics and logs (Prometheus/Grafana/Loki)

### 2) Threat Sources

Primary threat sources relevant to this project:

- External attacker over the internet (unauthenticated access to open endpoints)
- Malicious or abusive user (sending crafted requests)
- Credential reuse or leakage (hardcoded/default secrets)
- Man-in-the-middle/eavesdropping where plaintext HTTP is used
- Compromise of CI/CD or deployment channel
- Automated bot scanning of exposed monitoring/admin services

### 3) Risk Scenarios

#### R1 - Broken authentication/authorization for user actions

Scenario: An attacker sends direct requests with arbitrary `user` or `username` values to perform actions as another user (follow/unfollow/post), because identity is carried in query/path parameters instead of a verified user session/token.

Evidence:
- Endpoints accept user identity from request values (e.g., `/add_message?user=...`, `/follow?user=...`, `/msgs/{username}`).
- The simulator filter only checks a shared static Basic auth value for `/msgs` and `/fllws`, not per-user identity.

Affected assets: user account integrity, application integrity.

---

#### R2 - Credential exposure from hardcoded/default secrets

Scenario: Attackers or unauthorized readers obtain database or admin access because production-like credentials are hardcoded/default in repo/config and monitoring uses weak/default credentials.

Evidence:
- Hardcoded DB credentials in compose files.
- Default DB credentials in backend `application.properties`.
- Grafana admin username/password configured as static values.
- Deployment uses SSH as `root`.

Affected assets: credentials, database confidentiality/integrity, host integrity.

---

#### R3 - Sensitive data overexposure in API responses

Scenario: Public endpoints leak sensitive user fields (email and password hash), enabling user enumeration and offline password attacks if hashes are captured.

Evidence:
- `DatabaseService` builds public data objects containing `email` and `pwHash` for timeline responses.

Affected assets: user data confidentiality, credential safety.

---

#### R4 - Weak password handling design in legacy endpoints

Scenario: Passwords are sent as query parameters and hashed with a static zero-initialized salt, enabling credential leakage in logs/URLs and reducing hash robustness.

Evidence:
- Legacy login/registration endpoints use GET with `password` in query string.
- Salt array exists but is never randomized/populated.

Affected assets: credential confidentiality and account security.

---

#### R5 - Excessive CORS policy and CSRF-style cross-origin abuse

Scenario: Any origin can call backend endpoints due to global permissive CORS, enabling cross-origin request abuse and easier browser-based attack chains.

Evidence:
- CORS mapping allows `/**` without origin restrictions.

Affected assets: API integrity and abuse resistance.

---

#### R6 - Attack surface expansion through openly exposed operational services

Scenario: Attackers target exposed service ports (DB/Prometheus/Grafana/Loki/API) to brute-force, enumerate, exploit vulnerabilities, or disrupt operations.

Evidence:
- Production compose exposes several ports publicly (`5432`, `9090`, `3100`, `3001`, etc.).
- Swarm includes a plain HTTP API entrypoint for legacy access.

Affected assets: availability, operational data, infrastructure security.

---

## B. Risk Analysis

### Likelihood and Impact Scale

- **Likelihood**: Low / Medium / High
- **Impact**: Low / Medium / High / Critical

### Risk Matrix (Prioritized)

| ID | Scenario | Likelihood | Impact | Priority |
|---|---|---|---|---|
| R1 | Broken auth/authz for user actions | High | Critical | **Critical** |
| R2 | Hardcoded/default secrets | High | High | **High** |
| R3 | Sensitive data overexposure in API | High | High | **High** |
| R4 | Weak password handling in legacy endpoints | Medium | High | **High** |
| R5 | Permissive CORS / cross-origin abuse | Medium | Medium | **Medium** |
| R6 | Exposed ops/service ports | Medium | High | **High** |

### Risk Treatment Plan (What to do for each scenario)

#### R1 - Broken auth/authz for user actions
**Decision:** Mitigate immediately.

Actions:
- Introduce real user authentication (session/JWT) and derive identity from auth context, not request parameters.
- Enforce authorization checks on every state-changing endpoint (post/follow/unfollow).
- Keep simulator auth only for simulator-specific routes, isolated from normal user endpoints.
- Add integration tests for horizontal privilege escalation attempts.

#### R2 - Hardcoded/default secrets
**Decision:** Mitigate immediately.

Actions:
- Remove credentials from repo and compose defaults.
- Use secret management (GitHub Secrets + Docker Swarm secrets / environment injection at deploy time).
- Rotate DB, Grafana, and deployment credentials.
- Replace `root` SSH deployment user with least-privileged deploy user.

#### R3 - Sensitive data overexposure
**Decision:** Mitigate immediately.

Actions:
- Remove `email` and `pwHash` from all public timeline/user response DTOs.
- Define explicit response models with least-privilege fields.
- Add contract tests to prevent accidental reintroduction of sensitive fields.

#### R4 - Weak password handling (legacy GET endpoints)
**Decision:** Mitigate immediately (or retire legacy endpoints quickly).

Actions:
- Remove password-in-query endpoints; use POST body over HTTPS only.
- Use per-user random salts and a strong adaptive password hash (Argon2id, bcrypt, or PBKDF2 with random salt stored per user).
- Stop logging request values that may contain credentials.
- Invalidate and rotate credentials for affected accounts if exposure occurred.

#### R5 - Permissive CORS
**Decision:** Mitigate soon.

Actions:
- Restrict allowed origins to trusted frontend domains.
- Restrict methods/headers to minimum required.
- Disable credentials unless explicitly needed.
- Add security tests for disallowed origins.

#### R6 - Exposed operational services and ports
**Decision:** Mitigate soon.

Actions:
- Avoid publishing DB/monitoring ports publicly; bind internally or behind VPN/firewall.
- Require authentication and network allowlists for monitoring endpoints.
- Remove legacy plaintext entrypoints if not required; force HTTPS where possible.
- Add host firewall rules and periodic external port scanning checks.

---

## Recommended Implementation Order

1. **Immediate (0-7 days):** R1, R2, R3, R4
2. **Short term (1-2 sprints):** R6, R5
3. **Continuous:** Security testing in CI (SAST/dependency scan), secret scanning, credential rotation cadence, and periodic threat modeling updates.

## Residual Risk Note

Even after mitigations, residual risk remains from third-party dependencies and operational misconfiguration. This should be controlled with patch management, vulnerability scanning, and incident response readiness.

