# Contributing to Digital Library Platform

Thanks for your interest in contributing! This document outlines the process for
setting up a development environment and the guidelines for submitting changes.

## Prerequisites

- **JDK 17 or 21** (JDK 22+ is **not** supported — Spring Boot 3.2.x's Lombok
  and Mockito inline mock-maker break on newer JDKs)
- **Maven 3.8+**
- **Docker + docker compose** (for the full stack: MySQL, Redis, Kafka, Zookeeper)
- **Python 3.10+** (for the recommendation service)

## Development setup

### Full stack with Docker

```bash
cd docker
docker compose up -d
```

This starts MySQL (3306), Redis (6379), Kafka (29092), the backend (8080), and
the recommendation service (8000).

### Backend only (local)

```bash
cd docker
docker compose up -d mysql redis
mvn spring-boot:run
```

API docs: `http://localhost:8080/swagger-ui.html`

### Recommendation service (local)

```bash
cd recommendation-service
python3 -m venv .venv
. .venv/bin/activate
pip install -r requirements-dev.txt
uvicorn app.main:app --reload --port 8000
```

## Building and testing

```bash
# Backend: build + test (JDK 17/21)
mvn -B verify

# Recommendation service
cd recommendation-service
python -m pytest -q

# Checkstyle (style enforcement)
mvn checkstyle:check
```

## Submitting changes

1. Fork the repository and create a feature branch.
2. Write or update tests for any new behavior.
3. Ensure `mvn -B verify` and `python -m pytest -q` both pass.
4. Ensure `mvn checkstyle:check` passes (no new style violations).
5. Open a pull request with a clear description of the changes.

## Code style

- Java: 4-space indentation, no tabs. Follow the existing patterns.
- Python: PEP 8, 4-space indentation.
- Keep methods focused and under 200 lines.
- No hardcoded secrets — use environment variables and `application.yml` placeholders.
