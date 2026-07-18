create table idempotency_record
(
    idempotency_key UUID primary key,
    response_data   json null,
    created_at      timestamp default current_timestamp,
    updated_at      timestamp default current_timestamp
)