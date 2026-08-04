CREATE TABLE reading_progress (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    content_id BIGINT NOT NULL,
    position_seconds BIGINT NOT NULL DEFAULT 0,
    total_seconds BIGINT,
    progress_percent DOUBLE NOT NULL DEFAULT 0,
    last_updated TIMESTAMP,
    CONSTRAINT fk_progress_user FOREIGN KEY (user_id) REFERENCES users (id),
    UNIQUE KEY uq_progress_user_content (user_id, content_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

