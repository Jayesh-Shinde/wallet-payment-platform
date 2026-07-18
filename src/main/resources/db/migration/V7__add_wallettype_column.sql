ALTER TABLE wallet
    ADD COLUMN wallet_type VARCHAR(50) NOT NULL
        DEFAULT 'USER'
        CHECK (wallet_type IN ('USER', 'SYSTEM'));