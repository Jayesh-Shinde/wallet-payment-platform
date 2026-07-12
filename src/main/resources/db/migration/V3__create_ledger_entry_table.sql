create table ledger_entry
(
    id          UUID primary key,
    wallet_id   UUID        not null references wallet (id),
    transfer_id UUID        not null references transfer (id),
    amount      BIGINT      not null check (amount > 0),
    entry_type  varchar(50) not null check (entry_type in ('CREDIT', 'DEBIT')),
    created_at  timestamp   not null default current_timestamp,
    created_by  VARCHAR(255)
)