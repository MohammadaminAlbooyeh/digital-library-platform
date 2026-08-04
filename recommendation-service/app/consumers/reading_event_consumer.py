"""Kafka consumer that processes reading events into user profiles."""

from __future__ import annotations

import json
import logging
import threading

from kafka import KafkaConsumer

from app.services.recommendation_service import RecommendationService

logger = logging.getLogger(__name__)

TOPIC = "reading-events"
GROUP_ID = "dlp-recommendations"


def _decode_message(value: bytes) -> dict:
    if isinstance(value, bytes):
        value = value.decode("utf-8")
    return json.loads(value)


def consume_loop(service: RecommendationService, bootstrap_servers: str) -> None:
    consumer = KafkaConsumer(
        TOPIC,
        bootstrap_servers=bootstrap_servers,
        group_id=GROUP_ID,
        value_deserializer=_decode_message,
        auto_offset_reset="earliest",
        enable_auto_commit=True,
    )
    logger.info("Started reading-events consumer on %s", bootstrap_servers)
    for message in consumer:
        try:
            event = message.value
            service.record_reading(
                int(event["userId"]),
                int(event["contentId"]),
                float(event.get("progressPercent", 0.0)),
            )
        except Exception:  # noqa: BLE001
            logger.exception("Failed to process reading event")


def start_consumer(service: RecommendationService, bootstrap_servers: str) -> threading.Thread:
    thread = threading.Thread(
        target=consume_loop,
        args=(service, bootstrap_servers),
        daemon=True,
    )
    thread.start()
    return thread

