# Digital Library Platform

[![CI](https://github.com/MohammadaminAlbooyeh/digital-library-platform/actions/workflows/ci.yml/badge.svg)](https://github.com/MohammadaminAlbooyeh/digital-library-platform/actions/workflows/ci.yml)

A full-stack digital library platform with a Spring Boot backend, a Python recommendation
microservice, and Docker-based infrastructure.

## Architecture

```mermaid
%%{init: {'theme':'default','flowchart':{'curve':'linear'}}}%%
flowchart LR
    subgraph CLIENT["Client"]
        CLI[Web / Mobile App]
    end

    subgraph "Public Edge"
        LB[Load Balancer]
        CDN[AWS S3 CDN]
    end

    subgraph "Backend - Spring Boot"
        AUTH[Auth Controller]
        CATALOG[Book Catalog API]
        SEARCH[Search API]
        LIBRARY[Library API]
        PROGRESS[Reading Progress]
        PAYMENTS[Payments API]
        SUBSCRIPTIONS[Subscriptions API]
        DRM[DRM API]
        ADMIN[Admin API]

        subgraph "Internal"
            DRM_SVC[DRM Service]
            ENCRYPTION[Content Encryption]
            SIGNING[HMAC Token Signing]
            DELIVERY[Content Delivery]
            KAFKA_PUB[Kafka Producer]
        end
    end

    subgraph "Rec-Svc"
        RECO[FastAPI Rec Service]
    end

    subgraph "Infrastructure"
        MYSQL[(MySQL)]
        REDIS[(Redis)]
        KAFKA[(Kafka)]
        S3[(AWS S3 + KMS)]
    end

    CLI ==>|"HTTPS"| LB
    LB ==>|"REST API"| AUTH
    LB ==>|"REST API"| CATALOG
    LB ==>|"REST API"| SEARCH
    LB ==>|"REST API"| LIBRARY
    LB ==>|"REST API"| PROGRESS
    LB ==>|"REST API"| PAYMENTS
    LB ==>|"REST API"| SUBSCRIPTIONS
    LB ==>|"REST API"| DRM
    LB ==>|"REST API"| ADMIN

    AUTH ==>|DB| MYSQL
    CATALOG ==>|DB| MYSQL
    SEARCH ==>|DB| MYSQL
    LIBRARY ==>|DB| MYSQL
    PROGRESS ==>|DB| MYSQL
    PAYMENTS ==>|DB| MYSQL
    SUBSCRIPTIONS ==>|DB| MYSQL
    DRM ==>|DB| MYSQL
    ADMIN ==>|DB| MYSQL

    AUTH ==>|refresh| REDIS
    LIBRARY ==>|tokens| REDIS
    DELIVERY -.->|cache| REDIS

    PROGRESS ==>|events| KAFKA_PUB
    KAFKA_PUB ==>|produce| KAFKA
    RECO -.->|consume| KAFKA

    LIBRARY ==>|fallback| RECO
    RECO ==>|"HTTP"| DELIVERY
    DELIVERY ==>|"signed URL"| CDN

    LIBRARY ==>|DRM| DRM_SVC
    DRM ==> DRM_SVC
    DRM_SVC ==> ENCRYPTION
    DRM_SVC ==> SIGNING
    DELIVERY ==>|content| S3
    ENCRYPTION ==>|keys| S3

    classDef store fill:#e3f2fd,stroke:#1976d2,stroke-width:2px
    classDef cache fill:#ede7f6,stroke:#7b1fa2,stroke-width:2px
    classDef events fill:#fff3e0,stroke:#ef6c00,stroke-width:2px
    classDef cloud fill:#fce4ec,stroke:#c2185b,stroke-width:2px

    class MYSQL,REDIS,KAFKA store
    class S3 cloud
```

- **Backend** (`src/`) — Java 17 / Spring Boot 3: Auth (JWT), book catalog, search,
  subscriptions, payments, DRM (device registration, license issuance, content encryption),
  content delivery (CDN URL signing, download tokens, streaming chunks), reading progress
  with Kafka events, Redis cache, Flyway MySQL migrations.
- **Recommendation service** (`recommendation-service/`) — Python / FastAPI: exposes
  `/recommendations/{userId}`, consumes Kafka `reading-events` for per-user profiles.
- **Infrastructure** (`docker/`) — MySQL, Redis, Kafka + Zookeeper, AWS S3/KMS.

## Prerequisites

- JDK 17+
- Maven 3.8+
- Docker (for full stack)
- Python 3.10+ (for the recommendation service)

## Running the full stack

```bash
cd docker
docker compose up -d
```

This starts MySQL, Redis, Zookeeper, Kafka, the backend (port 8080), and the
recommendation service (port 8000).

## Running the backend locally

```bash
# Start infrastructure only
cd docker
docker compose up -d mysql redis

# Run the app
mvn spring-boot:run
```

The API docs are available at `http://localhost:8080/swagger-ui.html`.

## Configuration

Configuration is externalized via environment variables (see `src/main/resources/application.yml`):

| Variable | Default | Description |
|----------|---------|-------------|
| `DB_URL` | `jdbc:mysql://localhost:3306/dlp...` | JDBC URL |
| `DB_USERNAME` / `DB_PASSWORD` | `dlp` / `dlp` | DB credentials |
| `REDIS_HOST` / `REDIS_PORT` | `localhost` / `6379` | Redis |
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:29092` | Kafka |
| `JWT_SECRET` | (dev default) | JWT signing secret |
| `S3_BUCKET` / `AWS_REGION` | `dlp-content` / `us-east-1` | S3 |
| `CDN_BASE_URL` | `https://cdn.example.com` | CDN base URL |
| `DRM_ENCRYPTION_KEY` | (dev default) | AES DRM key |

> In production, always override `JWT_SECRET` and `DRM_ENCRYPTION_KEY`.

## API overview

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/auth/register` | Register a user |
| POST | `/api/auth/login` | Obtain JWT + refresh token |
| POST | `/api/auth/refresh` | Refresh access token |
| POST | `/api/auth/logout` | Revoke refresh token |
| GET | `/api/books` | List books |
| GET | `/api/books/{id}` | Book details |
| GET | `/api/search` | Search books |
| GET | `/api/subscriptions/plans` | List subscription plans |
| POST | `/api/subscriptions/subscribe` | Subscribe |
| POST | `/api/payments/purchase` | Purchase content |
| GET | `/api/library` | My library |
| POST | `/api/library/stream` | Get stream access (+ DRM license) |
| GET | `/api/library/recommendations` | Recommended books |
| PUT | `/api/progress/{contentId}` | Update reading progress |
| GET | `/api/admin/stats` | Admin stats |

## Testing

```bash
mvn test
```

> **JDK note:** This project targets Java 17 and must be built/tested with
> **JDK 17 or 21**. Spring Boot 3.2.x's pinned Lombok has no support for newer
> JDKs, and Mockito's inline mock-maker also fails on JDK 22+ with
> `MockitoException: Mockito cannot mock this class`. If Maven launches on a
> newer default JDK (e.g. Homebrew's `openjdk@26` when `JAVA_HOME` is unset),
> point it at a compatible JDK:
>
> ```bash
> export JAVA_HOME=/path/to/jdk-17-or-21
> mvn test
> ```
>
> The `pom.xml` also overrides Spring Boot's Mockito/ByteBuddy versions to a
> newer pair for robustness, but a JDK ≤21 is still required for the full build.
> (Tip: `unset JAVA_HOME` won't help — the Homebrew `mvn` shim then defaults
> to the latest brew JVM. Set `JAVA_HOME` explicitly.)

### Recommendation service tests

```bash
cd recommendation-service
pip install -r requirements-dev.txt
python -m pytest -q
```

### Continuous integration

`.github/workflows/ci.yml` runs `mvn -B verify` (JDK 21) and the recommendation
service's pytest suite on every push to `main` and every pull request.


