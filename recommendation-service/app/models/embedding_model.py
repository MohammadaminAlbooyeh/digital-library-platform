"""Simple content-embedding model used by the recommendation service."""

from __future__ import annotations

from collections import Counter
import hashlib
import re


class EmbeddingModel:
    """A lightweight bag-of-words embedding model with hashing."""

    def __init__(self, vector_size: int = 128) -> None:
        self.vector_size = vector_size

    def _normalize(self, text: str) -> list[str]:
        return re.findall(r"[a-z0-9']+", (text or "").lower())

    def embed(self, text: str) -> list[float]:
        """Return a normalized hashed bag-of-words vector for the given text."""
        vector = [0.0] * self.vector_size
        counts = Counter(self._normalize(text))
        for token, freq in counts.items():
            digest = hashlib.md5(token.encode("utf-8")).hexdigest()
            index = int(digest[:8], 16) % self.vector_size
            vector[index] += freq
        norm = sum(v * v for v in vector) ** 0.5
        if norm > 0:
            vector = [v / norm for v in vector]
        return vector

    def cosine_similarity(self, a: list[float], b: list[float]) -> float:
        if len(a) != len(b):
            return 0.0
        dot = sum(x * y for x, y in zip(a, b))
        return dot

