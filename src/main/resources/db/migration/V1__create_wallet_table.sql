create table wallet
(
    id                              UUID primary key,
    user_id                         UUID        not null,
    status                          varchar(50) not null check (status in ('ACTIVE', 'DEACTIVATED', 'PENDING_VERIFICATION')),
    last_reconciled_ledger_entry_id UUID,
    balance                         BIGINT      not null default 0,
    created_at                      timestamp   not null default current_timestamp,
    updated_at                      timestamp   not null default current_timestamp,
    created_by                      VARCHAR(255),
    updated_by                      VARCHAR(255)
)