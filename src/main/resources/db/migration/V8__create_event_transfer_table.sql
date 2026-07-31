CREATE TABLE event_transfer
(
    id          UUID PRIMARY KEY,
    event_type  VARCHAR(255) NOT NULL,
    transfer_id UUID         NOT NULL,
    status      VARCHAR(50)  NOT NULL DEFAULT 'PENDING' check (status IN ('PENDING', 'PROCESSED', 'FAILED', 'DEAD_LETTER')),
    payload     JSONB        NOT NULL,
    attempts    INT          NOT NULL DEFAULT 0,
    last_error  TEXT,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_event_transfer_status_created_at ON event_transfer (created_at) WHERE status = 'PENDING';