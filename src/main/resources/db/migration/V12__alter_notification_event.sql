ALTER TABLE event_transfer
    add column not_eligible_before TIMESTAMP DEFAULT CURRENT_TIMESTAMP;