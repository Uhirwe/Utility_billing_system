-- Migration v3: WASAC/REG business rules alignment
\c utility_billing_db;

-- Link customers to self-registered user accounts
ALTER TABLE customers ADD COLUMN IF NOT EXISTS user_id BIGINT UNIQUE REFERENCES users(id);

-- Allow multiple meters per customer
DROP INDEX IF EXISTS uq_meters_customer_id;

-- Ensure date_of_birth exists on customers
ALTER TABLE customers ADD COLUMN IF NOT EXISTS date_of_birth DATE;
