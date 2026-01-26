CREATE TABLE app_notifications (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    title VARCHAR(255) NOT NULL,
    body TEXT,
    type VARCHAR(32) NOT NULL,
    reference_id VARCHAR(36),
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_notification_user_id ON app_notifications(user_id);
CREATE INDEX idx_notification_user_read ON app_notifications(user_id, is_read);
CREATE INDEX idx_notification_created_at ON app_notifications(created_at DESC);
