# Digital Library Platform

[![CI](https://github.com/MohammadaminAlbooyeh/digital-library-platform/actions/workflows/ci.yml/badge.svg)](https://github.com/MohammadaminAlbooyeh/digital-library-platform/actions/workflows/ci.yml)

A full-stack digital library platform with a Spring Boot backend, a Python recommendation
microservice, and Docker-based infrastructure.

## Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                        Client                                       │
│                  Web / Mobile App                                   │
│     sends: Authorization: Bearer <JWT>                              │
└──────────────────────────────┬──────────────────────────────────────┘
                               │ HTTPS
                               ▼
┌─────────────────────────────────────────────────────────────────────┐
│                       Load Balancer                                 │
└──────────────────────────────┬──────────────────────────────────────┘
                               │ REST API
                               ▼
┌─────────────────────────────────────────────────────────────────────┐
│     Backend - Spring Boot (Java 17 · :8080)                          │
│                                                                     │
│   ┌──────────────────────────────┐                                  │
│   │  Spring Security             │  validates JWT signature         │
│   │  JWT + Refresh Tokens        │                                  │
│   └──────────────┬───────────────┘                                  │
│                   ▼                                                  │
│   ┌──────────────────────────────┐                                  │
│   │     Controllers              │  REST API                        │
│   │  Auth, Book, Audio, Search,  │                                  │
│   │  Library, Progress, Pay,      │                                  │
│   │  Sub, DRM, Admin             │                                  │
│   └──────────────┬───────────────┘                                  │
│                   ▼                                                  │
│   ┌──────────────────────────────┐                                  │
│   │  Internal Modules            │                                  │
│   │  DRM Service · Content Encr. │  AES-CBC + KMS (random IV)      │
│   │  HMAC Signing · CD Delivery   │  CDN URL signing               │
│   │  Kafka Producer               │  reading-progress events        │
│   └──────────────┬───────────────┘                                  │
│                   │                                                  │
│                   ├──────────────────┐                             │
│                   ▼                  ▼                             │
│   ┌──────────────────────┐  ┌──────────────────────┐              │
│   │   Redis Cache        │  │  MySQL (Flyway V1-V8) │              │
│   │  refresh tokens      │  │  users, books,       │              │
│   │  stream tokens       │  │  subscriptions, etc  │              │
│   └──────────────────────┘  └──────────────────────┘              │
└─────────────────────────────────────────────────────────────────────┘
                               │
                               │ reading-events (Kafka)
                               ▼
┌─────────────────────────────────────────────────────────────────────┐
│         Recommendation Service - FastAPI (Python · :8000)           │
│                                                                     │
│   ┌──────────────────────────────┐                                  │
│   │  Kafka Consumer              │  consumes reading-events         │
│   └──────────────┬───────────────┘                                  │
│                   ▼                                                  │
│   ┌──────────────────────────────┐                                  │
│   │  Embedding Model             │  bag-of-words + cosine          │
│   └──────────────┬───────────────┘                                  │
│                   ▼                                                  │
│   ┌──────────────────────────────┐                                  │
│   │  REST API                    │  /recommendations/{userId}       │
│   └──────────────────────────────┘                                  │
└─────────────────────────────────────────────────────────────────────┘
                               │
                               │ content fetch
                               ▼
┌─────────────────────────────────────────────────────────────────────┐
│              AWS S3 + KMS (content storage & keys)                  │
└─────────────────────────────────────────────────────────────────────┘
```

- **Backend** (`src/`) — Java 17 / Spring Boot 3: Auth (JWT), book catalog, search,
  subscriptions, payments, DRM (device registration, license issuance, content encryption),
  content delivery (CDN URL signing, download tokens, streaming chunks), reading progress
  with Kafka events, Redis cache, Flyway MySQL migrations.
- **Recommendation service** (`recommendation-service/`) — Python / FastAPI: exposes
  `/recommendations/{userId}`, consumes Kafka `reading-events` for per-user profiles.
- **Infrastructure** (`docker/`) — MySQL, Redis, Kafka + Zookeeper, AWS S3/KMS.

## Project structure

```
digital-library-platform/
├── .github/
│   ├── workflows/
│   │   └── ci.yml
│   └── dependabot.yml
├── docker/
│   ├── docker-compose.yml
│   └── Dockerfile
├── recommendation-service/
│   ├── app/
│   │   ├── api/
│   │   │   └── recommendation_router.py
│   │   ├── consumers/
│   │   │   └── reading_event_consumer.py
│   │   ├── models/
│   │   │   └── embedding_model.py
│   │   ├── services/
│   │   │   └── recommendation_service.py
│   │   └── main.py
│   ├── tests/
│   │   ├── test_api.py
│   │   ├── test_embedding_model.py
│   │   └── test_recommendation_service.py
│   ├── conftest.py
│   ├── Dockerfile
│   ├── requirements.txt
│   ├── requirements-dev.txt
│   └── requirements.lock
├── src/
│   ├── main/
│   │   ├── java/com/dlp/
│   │   │   ├── DigitalLibraryPlatformApplication.java
│   │   │   ├── config/
│   │   │   │   ├── KafkaConfig.java
│   │   │   │   ├── RedisCacheConfig.java
│   │   │   │   ├── S3Config.java
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   └── SwaggerConfig.java
│   │   │   ├── controller/
│   │   │   │   ├── AdminController.java
│   │   │   │   ├── AudiobookController.java
│   │   │   │   ├── AuthController.java
│   │   │   │   ├── BookController.java
│   │   │   │   ├── DrmController.java
│   │   │   │   ├── LibraryController.java
│   │   │   │   ├── PaymentController.java
│   │   │   │   ├── PublisherController.java
│   │   │   │   ├── ReadingProgressController.java
│   │   │   │   ├── SearchController.java
│   │   │   │   └── SubscriptionController.java
│   │   │   ├── delivery/
│   │   │   │   ├── CdnUrlSigner.java
│   │   │   │   ├── DownloadTokenService.java
│   │   │   │   └── StreamingChunkService.java
│   │   │   ├── drm/
│   │   │   │   ├── ContentEncryptionService.java
│   │   │   │   ├── DeviceRegistrationService.java
│   │   │   │   ├── DrmLicenseManager.java
│   │   │   │   └── DrmTokenGenerator.java
│   │   │   ├── exception/
│   │   │   │   ├── ContentNotOwnedException.java
│   │   │   │   ├── DrmViolationException.java
│   │   │   │   ├── GlobalExceptionHandler.java
│   │   │   │   └── SubscriptionExpiredException.java
│   │   │   ├── kms/
│   │   │   │   └── KmsDataKeyService.java
│   │   │   ├── messaging/
│   │   │   │   ├── PurchaseEventProducer.java
│   │   │   │   └── ReadingEventProducer.java
│   │   │   ├── model/
│   │   │   │   ├── dto/
│   │   │   │   │   ├── BookDetailDTO.java
│   │   │   │   │   ├── ReadingProgressDTO.java
│   │   │   │   │   ├── SearchRequest.java
│   │   │   │   │   ├── StreamAccessDTO.java
│   │   │   │   │   └── SubscriptionPlanDTO.java
│   │   │   │   ├── entity/
│   │   │   │   │   ├── Audiobook.java
│   │   │   │   │   ├── Author.java
│   │   │   │   │   ├── Book.java
│   │   │   │   │   ├── Category.java
│   │   │   │   │   ├── Device.java
│   │   │   │   │   ├── Publisher.java
│   │   │   │   │   ├── ReadingProgress.java
│   │   │   │   │   ├── Subscription.java
│   │   │   │   │   ├── Transaction.java
│   │   │   │   │   ├── User.java
│   │   │   │   │   └── UserLibraryItem.java
│   │   │   │   └── enums/
│   │   │   │       ├── AccessType.java
│   │   │   │       ├── ContentType.java
│   │   │   │       └── SubscriptionStatus.java
│   │   │   ├── recommendation/
│   │   │   │   ├── FallbackRecommendationStrategy.java
│   │   │   │   └── RecommendationClient.java
│   │   │   ├── repository/
│   │   │   │   ├── AudiobookRepository.java
│   │   │   │   ├── BookRepository.java
│   │   │   │   ├── DeviceRepository.java
│   │   │   │   ├── PublisherRepository.java
│   │   │   │   ├── ReadingProgressRepository.java
│   │   │   │   ├── SubscriptionRepository.java
│   │   │   │   ├── TransactionRepository.java
│   │   │   │   ├── UserLibraryRepository.java
│   │   │   │   └── UserRepository.java
│   │   │   ├── security/
│   │   │   │   ├── CurrentUserProvider.java
│   │   │   │   ├── JwtAuthenticationFilter.java
│   │   │   │   ├── JwtService.java
│   │   │   │   └── RefreshTokenService.java
│   │   │   ├── service/
│   │   │   │   ├── AuthService.java
│   │   │   │   ├── BookCatalogService.java
│   │   │   │   ├── ContentDeliveryService.java
│   │   │   │   ├── DrmService.java
│   │   │   │   ├── LibraryService.java
│   │   │   │   ├── PaymentService.java
│   │   │   │   ├── PublisherRoyaltyService.java
│   │   │   │   ├── ReadingProgressService.java
│   │   │   │   ├── SearchService.java
│   │   │   │   └── SubscriptionService.java
│   │   │   └── util/
│   │   │       └── FileFormatValidator.java
│   │   ├── resources/
│   │   │   ├── application.yml
│   │   │   ├── application-prod.yml
│   │   │   └── db/migration/
│   │   │       ├── V1__create_books_table.sql
│   │   │       ├── V2__create_audiobooks_table.sql
│   │   │       ├── V3__create_publishers_authors_table.sql
│   │   │       ├── V4__create_user_library_table.sql
│   │   │       ├── V5__create_subscriptions_table.sql
│   │   │       ├── V6__create_devices_table.sql
│   │   │       ├── V7__create_reading_progress_table.sql
│   │   │       └── V8__seed_demo_data.sql
│   └── test/
│   │   ├── java/com/dlp/
│   │   │   ├── config/TestSecurityConfig.java
│   │   │   ├── controller/
│   │   │   │   ├── AdminControllerTest.java
│   │   │   │   ├── AuthControllerTest.java
│   │   │   │   ├── AudiobookControllerTest.java
│   │   │   │   ├── BookControllerTest.java
│   │   │   │   ├── DrmControllerTest.java
│   │   │   │   ├── LibraryControllerTest.java
│   │   │   │   ├── PaymentControllerTest.java
│   │   │   │   ├── ReadingProgressControllerTest.java
│   │   │   │   ├── SearchControllerTest.java
│   │   │   │   └── SubscriptionControllerTest.java
│   │   │   ├── delivery/CdnUrlSignerTest.java
│   │   │   ├── drm/
│   │   │   │   ├── ContentEncryptionServiceTest.java
│   │   │   │   ├── DeviceRegistrationServiceTest.java
│   │   │   │   └── DrmLicenseManagerTest.java
│   │   │   ├── integration/DatabaseIntegrationTest.java
│   │   │   ├── kms/KmsDataKeyServiceTest.java
│   │   │   ├── security/RefreshTokenServiceTest.java
│   │   │   └── service/
│   │   │       ├── AuthServiceTest.java
│   │   │       ├── BookCatalogServiceTest.java
│   │   │       ├── ContentDeliveryServiceTest.java
│   │   │       ├── LibraryServiceTest.java
│   │   │       ├── PaymentServiceTest.java
│   │   │       ├── PublisherRoyaltyServiceTest.java
│   │   │       ├── ReadingProgressServiceTest.java
│   │   │       ├── SearchServiceTest.java
│   │   │       └── SubscriptionServiceTest.java
│   │   └── resources/
│   │       └── application-test.yml
├── checkstyle.xml
├── pom.xml
├── CONTRIBUTING.md
├── CODE_OF_CONDUCT.md
└── TODO.md
```

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


