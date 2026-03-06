## ITU MiniTwit Backend – JPA/PostgreSQL

This backend is a Spring Boot application that now uses **Spring Data JPA** with **PostgreSQL** as its database. All database access goes through JPA entities and Spring Data repositories; there is no raw SQL in the application code.

### Requirements

- JDK 21+
- Maven
- A running PostgreSQL instance

### Configuration

The application is configured via `application.properties` and overridable environment variables:

- **JDBC URL**
  - Property: `spring.datasource.url`
  - Env override: `MINITWIT_DB_URL`
  - Default: `jdbc:postgresql://localhost:5432/minitwit`
- **Username**
  - Property: `spring.datasource.username`
  - Env override: `MINITWIT_DB_USER`
  - Default: `postgres`
- **Password**
  - Property: `spring.datasource.password`
  - Env override: `MINITWIT_DB_PASSWORD`
  - Default: `postgres`

JPA / Hibernate settings:

- `spring.jpa.hibernate.ddl-auto=update` – ORM manages schema, creating/updating tables (`user`, `message`, `follower`, `meta`) as needed.
- `spring.sql.init.mode=never` – disables execution of `schema.sql` so schema is not managed via raw SQL.

To configure for your environment, set environment variables before running:

```bash
export MINITWIT_DB_URL="jdbc:postgresql://<host>:<port>/<database>"
export MINITWIT_DB_USER="<username>"
export MINITWIT_DB_PASSWORD="<password>"
```

### Running the application

From the `devops_backend` directory:

```bash
mvn spring-boot:run
```

By default the backend listens on **port 5001** (configurable via `server.port` in `application.properties`).

### Main endpoints

There are two groups of endpoints:

- **Simulator / JSON API (backed by `Store` abstraction, now using JPA):**
  - `POST /register` – register a user for the simulator
  - `GET /msgs` – list messages
  - `GET /msgs/{username}` – list messages by user
  - `POST /msgs/{username}` – post a message
  - `GET /fllws/{username}` – list follows
  - `POST /fllws/{username}` – follow/unfollow
  - `GET /latest` (via `LatestController`) – current latest event id
- **Legacy JSON API (via `Controller` + `DatabaseService`):**
  - `GET /` – public timeline
  - `GET /user?user=<sessionId>&profile=<username>`
  - `GET /register` – legacy register endpoint
  - `GET /spec_user` – legacy user lookup
  - `GET /is_followed`
  - `GET /follow`
  - `GET /unfollow`
  - `GET /add_message`
  - `GET /stats`

All these endpoints now talk to PostgreSQL through JPA repositories.

### Using an existing SQLite database (migration strategy)

The code no longer reads SQLite directly. To migrate existing data from a previous `minitwit.db` (SQLite) into PostgreSQL, use an external one-off migration, for example:

1. **Export from SQLite to CSV**
   - Use the SQLite CLI or a tool like `sqlite3` to dump each table (`user`, `message`, `follower`, optionally `meta`) to CSV.
2. **Import into PostgreSQL**
   - Create an empty PostgreSQL database and let the app start once so Hibernate creates the schema.
   - Use `COPY ... FROM STDIN WITH CSV` or a migration tool (e.g. Flyway/Liquibase, or a custom script) to load the CSVs into the corresponding tables.
3. **Start the app against PostgreSQL**
   - Ensure `MINITWIT_DB_URL`, `MINITWIT_DB_USER`, and `MINITWIT_DB_PASSWORD` point to the Postgres database with imported data.

This keeps the runtime code free of raw SQL while allowing you to reuse existing data.

