-- Demo/seed data for a fresh instance.
-- Book IDs 1-5 intentionally align with the recommendation service's demo
-- catalogue (recommendation-service/app/main.py) so that recommendations
-- work out of the box on a new deployment.

-- ---------------------------------------------------------------
-- Publishers
-- ---------------------------------------------------------------
INSERT INTO publishers (id, name, email, royalty_rate, created_at) VALUES
    (1, 'Orion Galactic Press', 'contact@oriongalactic.example', 0.5000, NOW()),
    (2, 'Whodunit House',       'hello@whodunit.example',        0.4500, NOW()),
    (3, 'Chronicle & Co.',      'editorial@chronicle.example',   0.4000, NOW()),
    (4, 'Ledger Line Books',    'hello@ledgerline.example',      0.4800, NOW()),
    (5, 'Summit Storyworks',    'press@summitstoryworks.example', 0.5200, NOW());

-- ---------------------------------------------------------------
-- Authors
-- ---------------------------------------------------------------
INSERT INTO authors (id, name, bio, created_at) VALUES
    (1, 'Ava Starling',     'Award-winning science fiction author exploring AI and the final frontier.', NOW()),
    (2, 'Miles Arclight',   'Former detective turned novelist, known for twist-heavy mysteries.', NOW()),
    (3, 'Dr. Rowan Kepler', 'Historian specialising in the archaeology of ancient civilisations.', NOW()),
    (4, 'Priya Varma',      'Personal finance educator and author of practical money guides.', NOW()),
    (5, 'Elias Brandt',     'Biographer chronicling the lives of visionary entrepreneurs.', NOW()),
    (6, 'Noah Sterling',    'Co-author and science communicator.', NOW());

-- ---------------------------------------------------------------
-- Categories
-- ---------------------------------------------------------------
INSERT INTO categories (id, name, created_at) VALUES
    (1, 'Science Fiction', NOW()),
    (2, 'Mystery',         NOW()),
    (3, 'History',         NOW()),
    (4, 'Finance',         NOW()),
    (5, 'Biography',       NOW()),
    (6, 'Technology',      NOW()),
    (7, 'Self-Help',       NOW());

-- ---------------------------------------------------------------
-- Books  (IDs 1-5 mirror the recommendation service demo catalogue)
-- ---------------------------------------------------------------
INSERT INTO books (id, title, description, price, page_count, isbn, cover_image_url, content_file_url, format, published_year, publisher_id, created_at, updated_at) VALUES
    (1, 'Stellar Drift',
     'A thrilling science fiction adventure about space exploration and artificial intelligence.',
     12.99, 384, '978-0-13-111111-1', 'https://cdn.example.com/covers/stellar-drift.jpg', 'https://cdn.example.com/books/stellar-drift.epub', 'epub', 2023, 1, NOW(), NOW()),
    (2, 'The Crimson Ledger',
     'An in-depth mystery novel with unexpected twists and detective investigation.',
     10.99, 296, '978-0-13-222222-2', 'https://cdn.example.com/covers/crimson-ledger.jpg', 'https://cdn.example.com/books/crimson-ledger.pdf', 'pdf', 2021, 2, NOW(), NOW()),
    (3, 'Echoes of Antiquity',
     'A comprehensive history of ancient civilisations and their enduring influence.',
     15.99, 512, '978-0-13-333333-3', 'https://cdn.example.com/covers/echoes-antiquity.jpg', 'https://cdn.example.com/books/echoes-antiquity.epub', 'epub', 2022, 3, NOW(), NOW()),
    (4, 'The Wealth Blueprint',
     'A practical guide to personal finance, investing and saving money.',
     9.99, 240, '978-0-13-444444-4', 'https://cdn.example.com/covers/wealth-blueprint.jpg', 'https://cdn.example.com/books/wealth-blueprint.mobi', 'mobi', 2020, 4, NOW(), NOW()),
    (5, 'Beyond the Threshold',
     'An inspiring biography of a visionary entrepreneur and innovator.',
     13.99, 352, '978-0-13-555555-5', 'https://cdn.example.com/covers/beyond-threshold.jpg', 'https://cdn.example.com/books/beyond-threshold.epub', 'epub', 2024, 5, NOW(), NOW());

-- ---------------------------------------------------------------
-- Book <-> Author links
-- ---------------------------------------------------------------
INSERT INTO book_authors (book_id, author_id) VALUES
    (1, 1),
    (1, 6),
    (2, 2),
    (3, 3),
    (4, 4),
    (5, 5);

-- ---------------------------------------------------------------
-- Book <-> Category links
-- ---------------------------------------------------------------
INSERT INTO book_categories (book_id, category_id) VALUES
    (1, 1),
    (1, 6),
    (2, 2),
    (3, 3),
    (4, 4),
    (4, 7),
    (5, 5),
    (5, 6);

-- ---------------------------------------------------------------
-- Audiobooks
-- ---------------------------------------------------------------
INSERT INTO audiobooks (id, title, description, price, narrator, duration_seconds, audio_file_url, cover_image_url, book_id, created_at) VALUES
    (1, 'Stellar Drift (Audiobook)',
     'Audiobook edition of Stellar Drift, narrated in full.',
     16.99, 'Maya Chen', 31320, 'https://cdn.example.com/audio/stellar-drift.mp3', 'https://cdn.example.com/covers/stellar-drift-audio.jpg', 1, NOW()),
    (2, 'The Crimson Ledger (Audiobook)',
     'Audiobook edition of The Crimson Ledger.',
     14.99, 'Victor Hales', 24780, 'https://cdn.example.com/audio/crimson-ledger.mp3', 'https://cdn.example.com/covers/crimson-ledger-audio.jpg', 2, NOW());

-- ---------------------------------------------------------------
-- Demo users
-- password hash below is BCrypt for the literal password "password"
-- ---------------------------------------------------------------
INSERT INTO users (id, email, password, name, role, created_at) VALUES
    (1, 'demo@dlp.example', '$2b$10$ygMsGZw4PhNxJbG1v80aEuwDL9fRdW22RGixTzZU6UOBGtFT3egM6', 'Demo User', 'USER', NOW()),
    (2, 'admin@dlp.example', '$2b$10$ygMsGZw4PhNxJbG1v80aEuwDL9fRdW22RGixTzZU6UOBGtFT3egM6', 'Demo Admin', 'ADMIN', NOW());

-- ---------------------------------------------------------------
-- Active subscription for the demo user
-- ---------------------------------------------------------------
INSERT INTO subscriptions (id, user_id, plan, monthly_price, status, start_date, end_date) VALUES
    (1, 1, 'Plus', 14.99, 'ACTIVE', NOW(), DATE_ADD(NOW(), INTERVAL 1 MONTH));

-- ---------------------------------------------------------------
-- Demo transactions
-- ---------------------------------------------------------------
INSERT INTO transactions (id, user_id, subscription_id, content_type, content_id, amount, currency, status, created_at) VALUES
    (1, 1, 1, 'BOOK', 1, 12.99, 'USD', 'COMPLETED', NOW()),
    (2, 1, 1, 'BOOK', 4, 9.99,  'USD', 'COMPLETED', NOW());

-- ---------------------------------------------------------------
-- Demo library items
-- ---------------------------------------------------------------
INSERT INTO user_library (id, user_id, content_id, content_type, access_type, acquired_at) VALUES
    (1, 1, 1, 'BOOK', 'OWNED', NOW()),
    (2, 1, 4, 'BOOK', 'OWNED', NOW());

-- ---------------------------------------------------------------
-- Reset AUTO_INCREMENT so future inserts never collide with seed rows
-- ---------------------------------------------------------------
ALTER TABLE publishers  AUTO_INCREMENT = 6;
ALTER TABLE authors     AUTO_INCREMENT = 7;
ALTER TABLE categories  AUTO_INCREMENT = 8;
ALTER TABLE books       AUTO_INCREMENT = 6;
ALTER TABLE audiobooks  AUTO_INCREMENT = 3;
ALTER TABLE users       AUTO_INCREMENT = 3;
ALTER TABLE subscriptions AUTO_INCREMENT = 2;
ALTER TABLE transactions AUTO_INCREMENT = 3;
ALTER TABLE user_library AUTO_INCREMENT = 3;
