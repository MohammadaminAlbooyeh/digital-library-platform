CREATE TABLE books (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    page_count INT,
    isbn VARCHAR(64),
    cover_image_url VARCHAR(512),
    content_file_url VARCHAR(512),
    format VARCHAR(16),
    published_year INT,
    publisher_id BIGINT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    INDEX idx_books_title (title),
    INDEX idx_books_publisher (publisher_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

