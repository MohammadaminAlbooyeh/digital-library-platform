from app.models.embedding_model import EmbeddingModel


def test_embed_returns_unit_vector_of_fixed_size():
    model = EmbeddingModel(vector_size=64)
    vector = model.embed("space exploration and artificial intelligence")

    assert len(vector) == 64
    norm = sum(v * v for v in vector) ** 0.5
    assert abs(norm - 1.0) < 1e-9


def test_embed_empty_text_is_zero_vector():
    model = EmbeddingModel()
    assert model.embed("") == [0.0] * model.vector_size


def test_embedding_is_deterministic():
    model = EmbeddingModel()
    assert model.embed("mystery novel") == model.embed("mystery novel")


def test_similar_text_scores_higher_than_unrelated():
    model = EmbeddingModel()
    finance = model.embed("a practical guide to personal finance and investing money")
    finance_2 = model.embed("personal finance investing and saving money guide")
    scifi = model.embed("space exploration robots and distant galaxies")

    assert model.cosine_similarity(finance, finance_2) > model.cosine_similarity(finance, scifi)


def test_cosine_similarity_mismatched_lengths_returns_zero():
    model = EmbeddingModel()
    assert model.cosine_similarity([1.0, 2.0], [1.0]) == 0.0
