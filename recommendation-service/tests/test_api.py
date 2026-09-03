from fastapi.testclient import TestClient

from app.main import app


def test_recommendations_endpoint_returns_seeded_catalogue():
    with TestClient(app) as client:
        resp = client.get("/recommendations/123")
    assert resp.status_code == 200
    body = resp.json()
    assert body["userId"] == 123
    assert sorted(body["contentIds"]) == [1, 2, 3, 4, 5]


def test_limit_is_validated():
    with TestClient(app) as client:
        assert client.get("/recommendations/1?limit=0").status_code == 400
        assert client.get("/recommendations/1?limit=101").status_code == 400


def test_limit_is_respected():
    with TestClient(app) as client:
        body = client.get("/recommendations/1?limit=3").json()
    assert len(body["contentIds"]) == 3
