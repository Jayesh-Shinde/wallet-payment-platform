create table notification_event
(
    id         UUID PRIMARY KEY,
    event_type VARCHAR(255) NOT NULL,
    status     VARCHAR(50)  NOT NULL check (status IN ('PENDING', 'PROCESSED', 'FAILED')),
    payload    JSONB        NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255) NOT NULL,
    updated_by VARCHAR(255) NOT NULL
);