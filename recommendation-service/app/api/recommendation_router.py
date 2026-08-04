"""API router for content recommendations."""

from __future__ import annotations

from fastapi import APIRouter, HTTPException

from app.services.recommendation_service import RecommendationService

router = APIRouter(prefix="/recommendations", tags=["recommendations"])


def _service() -> RecommendationService:
    # Resolved lazily from the app singleton to avoid import cycles.
    from app.main import recommendation_service
    return recommendation_service


@router.get("/{user_id}")
def get_recommendations(user_id: int, limit: int = 10) -> dict:
    if limit <= 0 or limit > 100:
        raise HTTPException(status_code=400, detail="limit must be between 1 and 100")
    content_ids = _service().recommend(user_id, limit)
    return {"userId": user_id, "contentIds": content_ids}

