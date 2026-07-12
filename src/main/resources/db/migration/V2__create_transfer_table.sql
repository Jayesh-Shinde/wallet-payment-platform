create table transfer
(
    id             UUID primary key,
    from_wallet_id UUID        not null references wallet (id),
    to_wallet_id   UUID        not null references wallet (id),
    amount         BIGINT      not null check (amount > 0),
    status         varchar(50) not null check (status in ('INITIATED', 'PENDING', 'COMPLETED', 'FAILED', 'ONHOLD')),
    created_at     timestamp   not null default current_timestamp,
    updated_at     timestamp   not null default current_timestamp,
    created_by     VARCHAR(255),
    updated_by     VARCHAR(255)
)