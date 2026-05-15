# Logging

This summary is based only on files in this repository.

## What We Log

The backend logs application activity with SLF4J and Log4j:

- Requests to key endpoints such as `/`, `/user`, `/register`, `/spec_user`,
  `/msgs`, `/fllws`, `/latest`, `/add_message`, and `/stats`.
- Successful actions such as registration, message posting, following, and
  unfollowing.
- Warnings and errors such as bad requests, missing users, invalid follow
  payloads, forbidden simulator requests, and database operation failures.
- Debugging context such as usernames, user IDs, request parameters, latest
  simulator IDs, message length, remote address, and total message/user counts.

The Next.js frontend contains a few `console.log` / `console.error` calls, but
the provisioned Grafana logs dashboard does not query frontend logs.

Traefik access logs are enabled in `swarm/proxy-stack.yml` with
`--accesslog=true`, but there is no provisioned Traefik logs dashboard.

## How Logs Are Aggregated

The configured log pipeline is:

```text
Docker container logs -> Grafana Alloy -> Loki -> Grafana
```

`alloy/config.alloy` discovers Docker containers through
`unix:///var/run/docker.sock`, adds `container` and `service` labels, reads logs
with `loki.source.docker`, and forwards them to Loki at
`http://loki:3100/loki/api/v1/push`.

`loki-config.yaml` configures Loki on port `3100` with filesystem storage under
`/tmp/loki/chunks`.

Grafana is provisioned with a Loki datasource at `http://loki:3100`.

## Grafana Dashboard

`monitoring/grafana/provisioning/dashboards/minitwit-logs.json` is backend-only.
Its panels query Loki with backend filters such as:

```logql
{service=~".*backend.*"}
{service=~".*backend.*"} |= "WARN"
{service=~".*backend.*"} |= "ERROR"
{service=~".*backend.*"} |~ "post message|message posted"
```

Panels include backend logs, warnings, errors, follow/unfollow logs, message
flow, register flow, warning count, error count, and message post count.

## Tools and Reasons

| Tool | Why it is used here |
| --- | --- |
| SLF4J / Log4j | The backend source imports and uses them for application logs. |
| Docker logs | Services run in Docker Compose and Swarm containers. |
| Grafana Alloy | The config discovers Docker containers and forwards their logs. |
| Loki | Alloy sends logs to Loki, and Grafana has a Loki datasource. |
| Grafana | Already used for dashboards; using it for logs keeps metrics and logs in one UI. |

## Limitations Visible in Files

- The provisioned logs dashboard only shows backend logs.
- No frontend or Traefik logs dashboard is provisioned.
- No request correlation ID appears in backend/frontend logging code.
- Some log lines include user-identifying values such as usernames, user IDs,
  email addresses, and remote addresses.
