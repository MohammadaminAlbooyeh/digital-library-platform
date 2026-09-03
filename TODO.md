# TODO — Digital Library Platform

## Immediate (before pushing)

- [ ] Run `mvn -B verify` locally to confirm the build and all tests pass (JDK 21 or pinned 17).
- [ ] Run the recommendation-service test suite locally:
  `cd recommendation-service && pip install -r requirements-dev.txt && python -m pytest -q`.
- [ ] Push the current commit to `origin/main` and confirm CI passes on GitHub Actions.

## Short-term polish

- [ ] Add an `application-test.yml` (or `@DataJpaTest`/`@SpringBootTest` config) so the Java service tests
  don't require a live MySQL/Flyway/RabbitMQ/Redis instance; wire Testcontainers where external
  services are unavoidable.
- [ ] Add integration tests for the REST controllers (`BookController`, `LibraryController`,
  `PaymentController`, `SubscriptionController`, etc.) using `@WebMvcTest` + mocked services.
- [ ] Run `mvn spotless:check` / `mvn checkstyle:check` if the team adopts a formatter/linter
  (`spotless`, `checkstyle`, or `pmd`). Add the plugin to `pom.xml` if missing.
- [ ] Add a `README.md` badge for the CI status (e.g. `![CI](.github/workflows/ci.yml/badge.svg)...`).

## Recommendation service

- [ ] Pin dependency versions in `requirements-dev.txt` (add a lock file or `requirements.lock`).
- [ ] Add a `Dockerfile` multi-stage build already exists; add a `docker-compose.yml` service entry
  so the recommendation service runs alongside MySQL/Redis/RabbitMQ/Kafka for local dev.
- [ ] Decide on the embedding model used by `app/models/embedding_model.py` and document it.

## Deployment / infra

- [ ] Decide whether `docker-compose.yml` should spin up the Spring Boot jar + recommendation
  service + external dependencies (MySQL, Redis, RabbitMQ, Kafka).
- [ ] Configure Flyway `baseline-on-migrate` for production; verify migration `V8` runs cleanly.
- [ ] Set up environment-based `application-prod.yml` with AWS S3 / KMS secrets (no hardcoded
  credentials in the repo).

## Security & DRM

- [ ] Review token expiration / refresh semantics in `JwtService`.
- [ ] Verify `CdnUrlSigner` and `DownloadTokenService` signed URLs use a short TTL.
- [ ] Audit `ContentEncryptionService` key storage (KMS vs. local).

## Future

- [ ] Add a `CONTRIBUTING.md` and a code-of-conduct if opening contributions.
- [ ] Set up Dependabot for Maven + pip to keep dependencies fresh.
