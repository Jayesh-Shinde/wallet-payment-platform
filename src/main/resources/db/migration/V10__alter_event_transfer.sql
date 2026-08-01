ALTER TABLE event_transfer
    DROP CONSTRAINT event_transfer_status_check,
    ADD CONSTRAINT event_transfer_status_check
        CHECK (status IN ('PENDING', 'PROCESSED', 'FAILED', 'DEAD_LETTER', 'CLAIMED'));


ALTER TABLE event_transfer
    ADD COLUMN claimed_expiry TIMESTAMP NULL;


CREATE INDEX idx_event_transfer_status_claimed_expiry ON event_transfer (claimed_expiry) WHERE status = 'CLAIMED';
