"""Digital Library Platform recommendation microservice entrypoint."""

from __future__ import annotations

import logging
import os

from fastapi import FastAPI

from app.api.recommendation_router import router
from app.services.recommendation_service import RecommendationService

logging.basicConfig(level=logging.INFO)

recommendation_service = RecommendationService()

app = FastAPI(
    title="Digital Library Recommendation Service",
    version="1.0.0",
)
app.include_router(router)

_bootstrap_servers = os.getenv("KAFKA_BOOTSTRAP_SERVERS", "localhost:29092")


@app.on_event("startup")
def _startup() -> None:
    # Seed a small catalogue so recommendations have content to rank.
    demo_catalogue = {
        1: "A thrilling science fiction adventure about space exploration and artificial intelligence",
        2: "An in-depth mystery novel with unexpected twists and detective investigation",
        3: "A comprehensive history of ancient civilizations and their enduring influence",
        4: "A practical guide to personal finance, investing and saving money",
        5: "An inspiring biography of a visionary entrepreneur and innovator",
    }
    for content_id, description in demo_catalogue.items():
        recommendation_service.register_content(content_id, description)

    if _bootstrap_servers:
        try:
            from app.consumers.reading_event_consumer import start_consumer
            start_consumer(recommendation_service, _bootstrap_servers)
        except Exception:  # noqa: BLE001
            logging.getLogger(__name__).exception("Could not start Kafka consumer; continuing without it")

