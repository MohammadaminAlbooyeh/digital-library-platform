# TODO — Digital Library Platform

## Done (completed)

- [x] Run `mvn -B verify` locally — **64 tests pass, BUILD SUCCESS** (JDK 21)
- [x] Run the recommendation-service test suite locally — **13 tests pass**
- [x] Push the current commit to `origin/main` — **CI passed** (Status: Success)
- [x] Bump deprecated GitHub Actions in `.github/workflows/ci.yml` to `@v5`
- [x] Add `README.md` CI status badge
- [x] Add Checkstyle config (`checkstyle.xml`) + `maven-checkstyle-plugin` in `pom.xml`
  (integrated into `verify` phase; **0 violations**)
- [x] Create `application-test.yml` (H2 in-memory, security disabled for tests)
- [x] Add `TestSecurityConfig` (`src/test/java/com/dlp/config/`) for `@WebMvcTest`
- [x] Add 5 REST controller integration tests (`@WebMvcTest`):
  `AuthControllerTest`, `BookControllerTest`, `LibraryControllerTest`,
  `PaymentControllerTest`, `SubscriptionControllerTest`
- [x] Add `H2` test dependency to `pom.xml`
- [x] Pin recommendation-service deps — generate `requirements.lock` via `pip freeze`
- [x] docker-compose.yml already unifies backend + recommendation service + infra
- [x] Document embedding model in `app/models/embedding_model.py`
- [x] Create `application-prod.yml` with Flyway `baseline-on-migrate` + AWS env-var config
- [x] Audit JWT: 24h expiry (`86400000ms`), secret from `JWT_SECRET` env var, no refresh token
- [x] Verify CDN signed-URL TTL: streaming = 2h, download = 1h (short, enforced in `verifyUrl`)
- [x] Audit `ContentEncryptionService`: key externalized via `DRM_ENCRYPTION_KEY` env var;
  documented known issue — static zero IV (fix: use KMS-derived per-content key + random IV)
- [x] Create `CONTRIBUTING.md` and `CODE_OF_CONDUCT.md` (Contributor Covenant v2.1)
- [x] Set up Dependabot — `.github/dependabot.yml` (Maven weekly + pip weekly)
  **Confirmed working:** 6 Dependabot PRs auto-generated after first push
  (spring-boot-starter-parent, AWS SDK, springdoc-openapi, checkstyle-plugin,
  Jackson, Python deps)
- [x] Fix unused imports found by Checkstyle (`SearchController`, `BookController`,
  `Audiobook`, `SearchService`, `ContentEncryptionService`, test files)
- [x] Fix line-length violation in `GlobalExceptionHandler`

## Future (nice-to-have)

- [ ] Add `Testcontainers` integration for `@DataJpaTest` / `@SpringBootTest` DB tests
  (current tests use H2 in-memory which is sufficient for unit-level checks)
- [ ] Add integration tests for `AdminController`, `DrmController`, `SearchController`,
  `ReadingProgressController`
- [ ] Consider `spotless:check` as an alternative/supplement to Checkstyle
- [ ] Migrate `ContentEncryptionService` to AWS KMS per-content keys with random IVs
- [ ] Add refresh-token flow for JWT (current: 24h stateless tokens, no refresh)
