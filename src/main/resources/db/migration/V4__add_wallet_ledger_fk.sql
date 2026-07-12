Alter table wallet
    add constraint fk_last_reconciled_ledger_entry foreign key (last_reconciled_ledger_entry_id) references ledger_entry (id);