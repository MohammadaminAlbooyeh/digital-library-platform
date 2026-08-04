"""Recommendation service based on a lightweight embedding model."""

from __future__ import annotations

from collections import defaultdict

from app.models.embedding_model import EmbeddingModel


class RecommendationService:
    """Tracks per-user reading history and computes content recommendations."""

    def __init__(self) -> None:
        self._model = EmbeddingModel()
        self._user_profile: dict[int, list[float]] = defaultdict(lambda: [0.0] * self._model.vector_size)
        self._content_profile: dict[int, list[float]] = {}

    def register_content(self, content_id: int, description: str) -> None:
        self._content_profile[content_id] = self._model.embed(description)

    def record_reading(self, user_id: int, content_id: int, progress_percent: float) -> None:
        vector = self._model.embed("")
        profile = self._user_profile[user_id]
        content_vector = self._content_profile.get(content_id)
        if content_vector is not None:
            # Blend the content vector into the user profile, weighted by progress.
            weight = min(1.0, max(0.0, progress_percent)) / 100.0
            for i in range(self._model.vector_size):
                profile[i] += content_vector[i] * weight

    def recommend(self, user_id: int, limit: int = 10) -> list[int]:
        profile = self._user_profile[user_id]
        scored: list[tuple[float, int]] = []
        for content_id, vector in self._content_profile.items():
            if sum(abs(v) for v in profile) == 0:
                score = 0.0
            else:
                score = self._model.cosine_similarity(profile, vector)
            scored.append((score, content_id))
        scored.sort(key=lambda item: item[0], reverse=True)
        return [content_id for _, content_id in scored[:limit]]

