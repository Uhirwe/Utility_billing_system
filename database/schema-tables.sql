-- =============================================================================
-- Utility Billing System - Tables, Triggers, Functions, Procedures
-- Run against existing database: utility_billing_db
-- =============================================================================

-- -----------------------------------------------------------------------------
-- ROLES
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS roles (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(50) NOT NULL UNIQUE,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- -----------------------------------------------------------------------------
-- USERS (System accounts for authentication)
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id            BIGSERIAL PRIMARY KEY,
    full_names    VARCHAR(150) NOT NULL,
    email         VARCHAR(150) NOT NULL UNIQUE,
    phone_number  VARCHAR(20)  NOT NULL,
    password      VARCHAR(255) NOT NULL,
    status        VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE'
                  CHECK (status IN ('ACTIVE', 'INACTIVE')),
    created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

-- -----------------------------------------------------------------------------
-- CUSTOMERS
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS customers (
    id            BIGSERIAL PRIMARY KEY,
    full_names    VARCHAR(150) NOT NULL,
    national_id   VARCHAR(16)  NOT NULL UNIQUE,
    email         VARCHAR(150) NOT NULL UNIQUE,
    phone_number  VARCHAR(20)  NOT NULL UNIQUE,
    address       VARCHAR(255) NOT NULL,
    status        VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE'
                  CHECK (status IN ('ACTIVE', 'INACTIVE')),
    created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_national_id_length CHECK (LENGTH(national_id) = 16)
);

-- -----------------------------------------------------------------------------
-- METERS
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS meters (
    id                 BIGSERIAL PRIMARY KEY,
    meter_number       VARCHAR(50)  NOT NULL UNIQUE,
    meter_type         VARCHAR(20)  NOT NULL CHECK (meter_type IN ('WATER', 'ELECTRICITY')),
    installation_date  DATE         NOT NULL,
    status             VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE'
                       CHECK (status IN ('ACTIVE', 'INACTIVE')),
    customer_id        BIGINT       NOT NULL REFERENCES customers(id),
    created_at         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- -----------------------------------------------------------------------------
-- METER READINGS
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS meter_readings (
    id                BIGSERIAL PRIMARY KEY,
    meter_id          BIGINT       NOT NULL REFERENCES meters(id),
    previous_reading  NUMERIC(12,2) NOT NULL DEFAULT 0 CHECK (previous_reading >= 0),
    current_reading   NUMERIC(12,2) NOT NULL CHECK (current_reading >= 0),
    consumption       NUMERIC(12,2) NOT NULL DEFAULT 0,
    reading_date      DATE          NOT NULL,
    reading_month     INT           NOT NULL,
    reading_year      INT           NOT NULL,
    created_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_reading_order CHECK (current_reading > previous_reading),
    CONSTRAINT uq_meter_reading_period UNIQUE (meter_id, reading_month, reading_year)
);

-- -----------------------------------------------------------------------------
-- TARIFFS
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS tariffs (
    id                      BIGSERIAL PRIMARY KEY,
    meter_type              VARCHAR(20)  NOT NULL CHECK (meter_type IN ('WATER', 'ELECTRICITY')),
    tariff_name             VARCHAR(100) NOT NULL,
    rate_per_unit           NUMERIC(12,4) NOT NULL CHECK (rate_per_unit >= 0),
    fixed_charge            NUMERIC(12,2) NOT NULL CHECK (fixed_charge >= 0),
    vat_percentage          NUMERIC(5,2)  NOT NULL CHECK (vat_percentage >= 0),
    late_penalty_percentage NUMERIC(5,2)  NOT NULL CHECK (late_penalty_percentage >= 0),
    version_number          INT           NOT NULL,
    effective_date          DATE          NOT NULL,
    active                  BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_tariff_version UNIQUE (meter_type, version_number)
);

-- -----------------------------------------------------------------------------
-- BILLS
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS bills (
    id              BIGSERIAL PRIMARY KEY,
    bill_number     VARCHAR(50)   NOT NULL UNIQUE,
    customer_id     BIGINT        NOT NULL REFERENCES customers(id),
    meter_id        BIGINT        NOT NULL REFERENCES meters(id),
    billing_month   INT           NOT NULL,
    billing_year    INT           NOT NULL,
    consumption     NUMERIC(12,2) NOT NULL DEFAULT 0,
    tariff_id       BIGINT        NOT NULL REFERENCES tariffs(id),
    fixed_charge    NUMERIC(12,2) NOT NULL DEFAULT 0,
    tax_amount      NUMERIC(12,2) NOT NULL DEFAULT 0,
    penalty_amount  NUMERIC(12,2) NOT NULL DEFAULT 0,
    total_amount    NUMERIC(12,2) NOT NULL DEFAULT 0,
    paid_amount     NUMERIC(12,2) NOT NULL DEFAULT 0,
    balance         NUMERIC(12,2) NOT NULL DEFAULT 0,
    bill_status     VARCHAR(20)   NOT NULL DEFAULT 'PENDING'
                    CHECK (bill_status IN ('PENDING', 'PARTIALLY_PAID', 'PAID', 'OVERDUE')),
    due_date        DATE          NOT NULL,
    generated_date  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_bill_period UNIQUE (meter_id, billing_month, billing_year)
);

-- -----------------------------------------------------------------------------
-- PAYMENTS
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS payments (
    id                 BIGSERIAL PRIMARY KEY,
    payment_reference  VARCHAR(50)   NOT NULL UNIQUE,
    bill_id            BIGINT        NOT NULL REFERENCES bills(id),
    amount_paid        NUMERIC(12,2) NOT NULL CHECK (amount_paid > 0),
    payment_method     VARCHAR(20)   NOT NULL
                       CHECK (payment_method IN ('CASH', 'MOBILE_MONEY', 'BANK_TRANSFER', 'CARD')),
    payment_date       TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- -----------------------------------------------------------------------------
-- NOTIFICATIONS
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS notifications (
    id                BIGSERIAL PRIMARY KEY,
    customer_id       BIGINT    NOT NULL REFERENCES customers(id),
    title             VARCHAR(150) NOT NULL,
    message           TEXT         NOT NULL,
    notification_date TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    read_status       BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_customers_status ON customers(status);
CREATE INDEX IF NOT EXISTS idx_meters_customer ON meters(customer_id);
CREATE INDEX IF NOT EXISTS idx_meters_status ON meters(status);
CREATE INDEX IF NOT EXISTS idx_meter_readings_meter ON meter_readings(meter_id);
CREATE INDEX IF NOT EXISTS idx_bills_customer ON bills(customer_id);
CREATE INDEX IF NOT EXISTS idx_bills_status ON bills(bill_status);
CREATE INDEX IF NOT EXISTS idx_payments_bill ON payments(bill_id);
CREATE INDEX IF NOT EXISTS idx_notifications_customer ON notifications(customer_id);
