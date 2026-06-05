-- Migration v4: Full project specification alignment
\c utility_billing_db;

-- Phone split: country code (default Rwanda +250) + local number
ALTER TABLE users ADD COLUMN IF NOT EXISTS phone_country_code VARCHAR(5) NOT NULL DEFAULT '+250';
ALTER TABLE customers ADD COLUMN IF NOT EXISTS phone_country_code VARCHAR(5) NOT NULL DEFAULT '+250';

-- Prepaid vs postpaid billing mode per meter (REG electricity transition support)
ALTER TABLE meters ADD COLUMN IF NOT EXISTS billing_mode VARCHAR(20) NOT NULL DEFAULT 'POSTPAID';
ALTER TABLE meters DROP CONSTRAINT IF EXISTS meters_billing_mode_check;
ALTER TABLE meters ADD CONSTRAINT meters_billing_mode_check
    CHECK (billing_mode IN ('POSTPAID', 'PREPAID'));
