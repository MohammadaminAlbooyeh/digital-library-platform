CREATE TABLE subscriptions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    plan VARCHAR(64) NOT NULL,
    monthly_price DECIMAL(10, 2),
    status VARCHAR(16) NOT NULL,
    start_date TIMESTAMP,
    end_date TIMESTAMP,
    CONSTRAINT fk_subscriptions_user FOREIGN KEY (user_id) REFERENCES users (id),
    INDEX idx_subscriptions_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE transactions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    subscription_id BIGINT,
    content_type VARCHAR(16),
    content_id BIGINT,
    amount DECIMAL(10, 2) NOT NULL,
    currency VARCHAR(8) NOT NULL DEFAULT 'USD',
    paypal_payment_id VARCHAR(128),
    status VARCHAR(16) DEFAULT 'COMPLETED',
    created_at TIMESTAMP,
    CONSTRAINT fk_transactions_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_transactions_subscription FOREIGN KEY (subscription_id) REFERENCES subscriptions (id),
    INDEX idx_transactions_user (user_id),
    INDEX idx_transactions_content (content_type, content_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

