CREATE TABLE audiobooks (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    narrator VARCHAR(255),
    duration_seconds BIGINT,
    audio_file_url VARCHAR(512),
    cover_image_url VARCHAR(512),
    book_id BIGINT,
    created_at TIMESTAMP,
    INDEX idx_audiobooks_book (book_id),
    CONSTRAINT fk_audiobooks_book FOREIGN KEY (book_id) REFERENCES books (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

