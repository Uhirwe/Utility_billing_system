-- Migration v2: Final business rules & validation specification
\c utility_billing_db;

-- Users: first/last name, password expiry, account lock
ALTER TABLE users ADD COLUMN IF NOT EXISTS first_name VARCHAR(75);
ALTER TABLE users ADD COLUMN IF NOT EXISTS last_name VARCHAR(75);
ALTER TABLE users ADD COLUMN IF NOT EXISTS password_expired BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS account_locked BOOLEAN NOT NULL DEFAULT FALSE;

UPDATE users SET first_name = split_part(full_names, ' ', 1),
                 last_name = COALESCE(NULLIF(substring(full_names from position(' ' in full_names) + 1), ''), split_part(full_names, ' ', 1))
WHERE first_name IS NULL;

-- Customers: date of birth, suspended status support
ALTER TABLE customers ADD COLUMN IF NOT EXISTS date_of_birth DATE;
ALTER TABLE customers DROP CONSTRAINT IF EXISTS customers_status_check;
ALTER TABLE customers ADD CONSTRAINT customers_status_check
    CHECK (status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED'));

-- Meters: 1:1 customer assignment, disconnected status, format constraint
ALTER TABLE meters DROP CONSTRAINT IF EXISTS meters_status_check;
ALTER TABLE meters ADD CONSTRAINT meters_status_check
    CHECK (status IN ('ACTIVE', 'INACTIVE', 'DISCONNECTED'));
CREATE UNIQUE INDEX IF NOT EXISTS uq_meters_customer_id ON meters(customer_id);

-- Bills: approved flag, unpaid status
ALTER TABLE bills ADD COLUMN IF NOT EXISTS approved BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE bills ADD COLUMN IF NOT EXISTS approved_at TIMESTAMP;
ALTER TABLE bills ADD COLUMN IF NOT EXISTS approved_by VARCHAR(150);
ALTER TABLE bills DROP CONSTRAINT IF EXISTS bills_bill_status_check;
UPDATE bills SET bill_status = 'UNPAID' WHERE bill_status = 'PENDING';
UPDATE bills SET bill_status = 'UNPAID' WHERE bill_status = 'OVERDUE';
ALTER TABLE bills ADD CONSTRAINT bills_bill_status_check
    CHECK (bill_status IN ('UNPAID', 'PARTIALLY_PAID', 'PAID', 'APPROVED'));

-- Payments: updated payment methods
ALTER TABLE payments DROP CONSTRAINT IF EXISTS payments_payment_method_check;
UPDATE payments SET payment_method = 'MOMO' WHERE payment_method = 'MOBILE_MONEY';
UPDATE payments SET payment_method = 'BANK' WHERE payment_method = 'BANK_TRANSFER';
ALTER TABLE payments ADD CONSTRAINT payments_payment_method_check
    CHECK (payment_method IN ('MOMO', 'BANK', 'CARD', 'CASH'));

-- Tariff tiers
CREATE TABLE IF NOT EXISTS tariff_tiers (
    id            BIGSERIAL PRIMARY KEY,
    tariff_id     BIGINT NOT NULL REFERENCES tariffs(id) ON DELETE CASCADE,
    tier_name     VARCHAR(100) NOT NULL,
    min_units     NUMERIC(12,2) NOT NULL DEFAULT 0,
    max_units     NUMERIC(12,2) NOT NULL,
    rate_per_unit NUMERIC(12,4) NOT NULL CHECK (rate_per_unit > 0),
    created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_tier_range CHECK (max_units > min_units)
);

-- Audit trail
CREATE TABLE IF NOT EXISTS audit_logs (
    id          BIGSERIAL PRIMARY KEY,
    actor_email VARCHAR(150) NOT NULL,
    action_type VARCHAR(50) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id   BIGINT,
    old_value   TEXT,
    new_value   TEXT,
    action_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_audit_logs_actor ON audit_logs(actor_email);
CREATE INDEX IF NOT EXISTS idx_audit_logs_entity ON audit_logs(entity_type, entity_id);

-- Notification deduplication helper
CREATE UNIQUE INDEX IF NOT EXISTS uq_notification_event
    ON notifications(customer_id, title, (substring(message from 1 for 100)));
