from app.services.recommendation_service import RecommendationService


def _seeded_service() -> RecommendationService:
    service = RecommendationService()
    service.register_content(1, "science fiction adventure space exploration artificial intelligence")
    service.register_content(2, "mystery novel unexpected twists detective investigation")
    service.register_content(3, "history of ancient civilizations and their influence")
    service.register_content(4, "practical guide to personal finance investing and saving money")
    return service


def test_new_user_gets_all_content_without_error():
    service = _seeded_service()
    recs = service.recommend(user_id=42)
    assert sorted(recs) == [1, 2, 3, 4]


def test_reading_history_biases_recommendations():
    service = _seeded_service()
    service.record_reading(user_id=1, content_id=4, progress_percent=90.0)

    recs = service.recommend(user_id=1, limit=4)
    assert recs[0] == 4


def test_limit_caps_result_count():
    service = _seeded_service()
    service.record_reading(user_id=1, content_id=1, progress_percent=100.0)
    assert len(service.recommend(user_id=1, limit=2)) == 2


def test_zero_progress_does_not_move_profile():
    service = _seeded_service()
    before = service.recommend(user_id=5, limit=4)
    service.record_reading(user_id=5, content_id=2, progress_percent=0.0)
    assert service.recommend(user_id=5, limit=4) == before


def test_reading_unknown_content_is_ignored():
    service = _seeded_service()
    service.record_reading(user_id=9, content_id=999, progress_percent=100.0)
    assert sorted(service.recommend(user_id=9)) == [1, 2, 3, 4]
