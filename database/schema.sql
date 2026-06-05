-- =============================================================================
-- Utility Billing System - PostgreSQL Database Schema
-- Database: utility_billing_db
-- =============================================================================

CREATE DATABASE utility_billing_db;
\c utility_billing_db;

-- -----------------------------------------------------------------------------
-- ENUM-like CHECK constraints via VARCHAR columns
-- -----------------------------------------------------------------------------

-- -----------------------------------------------------------------------------
-- ROLES
-- -----------------------------------------------------------------------------
CREATE TABLE roles (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(50) NOT NULL UNIQUE,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- -----------------------------------------------------------------------------
-- USERS (System accounts for authentication)
-- -----------------------------------------------------------------------------
CREATE TABLE users (
    id                  BIGSERIAL PRIMARY KEY,
    first_name          VARCHAR(75)  NOT NULL,
    last_name           VARCHAR(75)  NOT NULL,
    full_names          VARCHAR(150) NOT NULL,
    email               VARCHAR(150) NOT NULL UNIQUE,
    phone_country_code  VARCHAR(5)   NOT NULL DEFAULT '+250',
    phone_number        VARCHAR(20)  NOT NULL,
    password            VARCHAR(255) NOT NULL,
    password_expired    BOOLEAN      NOT NULL DEFAULT FALSE,
    account_locked      BOOLEAN      NOT NULL DEFAULT FALSE,
    status              VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE'
                        CHECK (status IN ('ACTIVE', 'INACTIVE')),
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE user_roles (
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

-- -----------------------------------------------------------------------------
-- CUSTOMERS (Utility service subscribers)
-- -----------------------------------------------------------------------------
CREATE TABLE customers (
    id            BIGSERIAL PRIMARY KEY,
    user_id       BIGINT UNIQUE REFERENCES users(id),
    full_names    VARCHAR(150) NOT NULL,
    national_id   VARCHAR(16)  NOT NULL UNIQUE,
    email               VARCHAR(150) NOT NULL UNIQUE,
    phone_country_code  VARCHAR(5)   NOT NULL DEFAULT '+250',
    phone_number        VARCHAR(20)  NOT NULL UNIQUE,
    address             VARCHAR(255) NOT NULL,
    date_of_birth DATE         NOT NULL,
    status        VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE'
                  CHECK (status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED')),
    created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_national_id_length CHECK (LENGTH(national_id) = 16)
);

-- -----------------------------------------------------------------------------
-- METERS
-- -----------------------------------------------------------------------------
CREATE TABLE meters (
    id                 BIGSERIAL PRIMARY KEY,
    meter_number       VARCHAR(50)  NOT NULL UNIQUE,
    meter_type         VARCHAR(20)  NOT NULL CHECK (meter_type IN ('WATER', 'ELECTRICITY')),
    installation_date  DATE         NOT NULL,
    status             VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE'
                       CHECK (status IN ('ACTIVE', 'INACTIVE', 'DISCONNECTED')),
    billing_mode       VARCHAR(20)  NOT NULL DEFAULT 'POSTPAID'
                       CHECK (billing_mode IN ('POSTPAID', 'PREPAID')),
    customer_id        BIGINT       NOT NULL REFERENCES customers(id),
    created_at         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- -----------------------------------------------------------------------------
-- METER READINGS
-- -----------------------------------------------------------------------------
CREATE TABLE meter_readings (
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
-- TARIFFS (Versioned pricing)
-- -----------------------------------------------------------------------------
CREATE TABLE tariffs (
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
CREATE TABLE bills (
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
CREATE TABLE payments (
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
CREATE TABLE notifications (
    id                BIGSERIAL PRIMARY KEY,
    customer_id       BIGINT    NOT NULL REFERENCES customers(id),
    title             VARCHAR(150) NOT NULL,
    message           TEXT         NOT NULL,
    notification_date TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    read_status       BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- -----------------------------------------------------------------------------
-- INDEXES
-- -----------------------------------------------------------------------------
CREATE INDEX idx_customers_status ON customers(status);
CREATE INDEX idx_meters_customer ON meters(customer_id);
CREATE INDEX idx_meters_status ON meters(status);
CREATE INDEX idx_meter_readings_meter ON meter_readings(meter_id);
CREATE INDEX idx_bills_customer ON bills(customer_id);
CREATE INDEX idx_bills_status ON bills(bill_status);
CREATE INDEX idx_payments_bill ON payments(bill_id);
CREATE INDEX idx_notifications_customer ON notifications(customer_id);

-- =============================================================================
-- FUNCTION: Calculate Utility Charges
-- Returns total amount given consumption, rate, fixed charge, and VAT %
-- =============================================================================
CREATE OR REPLACE FUNCTION calculate_utility_charges(
    p_consumption    NUMERIC,
    p_rate_per_unit  NUMERIC,
    p_fixed_charge   NUMERIC,
    p_vat_percentage NUMERIC
)
RETURNS TABLE (
    subtotal    NUMERIC,
    tax_amount  NUMERIC,
    total_amount NUMERIC
) AS $$
DECLARE
    v_subtotal NUMERIC;
    v_tax      NUMERIC;
    v_total    NUMERIC;
BEGIN
    v_subtotal := p_consumption * p_rate_per_unit;
    v_tax      := v_subtotal * (p_vat_percentage / 100);
    v_total    := v_subtotal + p_fixed_charge + v_tax;

    subtotal     := ROUND(v_subtotal, 2);
    tax_amount   := ROUND(v_tax, 2);
    total_amount := ROUND(v_total, 2);
    RETURN NEXT;
END;
$$ LANGUAGE plpgsql;

-- =============================================================================
-- TRIGGER FUNCTION: Auto-insert notification when bill is generated
-- =============================================================================
CREATE OR REPLACE FUNCTION trg_bill_generated_notification()
RETURNS TRIGGER AS $$
DECLARE
    v_customer_name VARCHAR(150);
    v_month_name    TEXT;
BEGIN
    SELECT full_names INTO v_customer_name FROM customers WHERE id = NEW.customer_id;

    v_month_name := TO_CHAR(TO_DATE(NEW.billing_month::TEXT, 'MM'), 'Month');

    INSERT INTO notifications (customer_id, title, message, notification_date, read_status)
    VALUES (
        NEW.customer_id,
        'Bill Generated',
        'Dear ' || v_customer_name || ', Your ' || TRIM(v_month_name) || '/' || NEW.billing_year ||
        ' utility bill of ' || NEW.total_amount || ' FRW has been successfully processed.',
        CURRENT_TIMESTAMP,
        FALSE
    );
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER bill_generated_notification_trigger
    AFTER INSERT ON bills
    FOR EACH ROW
    EXECUTE FUNCTION trg_bill_generated_notification();

-- =============================================================================
-- TRIGGER FUNCTION: When bill balance reaches zero, mark PAID and notify
-- =============================================================================
CREATE OR REPLACE FUNCTION trg_bill_paid_notification()
RETURNS TRIGGER AS $$
DECLARE
    v_customer_name VARCHAR(150);
BEGIN
    IF NEW.balance <= 0 AND OLD.balance > 0 THEN
        NEW.bill_status := 'PAID';
        NEW.balance     := 0;

        SELECT full_names INTO v_customer_name FROM customers WHERE id = NEW.customer_id;

        INSERT INTO notifications (customer_id, title, message, notification_date, read_status)
        VALUES (
            NEW.customer_id,
            'Payment Completed',
            'Dear ' || v_customer_name || ', Your payment of ' || NEW.paid_amount ||
            ' FRW has been successfully received.',
            CURRENT_TIMESTAMP,
            FALSE
        );
    ELSIF NEW.balance > 0 AND NEW.paid_amount > 0 AND NEW.balance < NEW.total_amount THEN
        NEW.bill_status := 'PARTIALLY_PAID';
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER bill_paid_notification_trigger
    BEFORE UPDATE ON bills
    FOR EACH ROW
    WHEN (OLD.paid_amount IS DISTINCT FROM NEW.paid_amount OR OLD.balance IS DISTINCT FROM NEW.balance)
    EXECUTE FUNCTION trg_bill_paid_notification();

-- =============================================================================
-- TRIGGER: Auto-calculate consumption on meter reading insert/update
-- =============================================================================
CREATE OR REPLACE FUNCTION trg_calculate_consumption()
RETURNS TRIGGER AS $$
BEGIN
    NEW.consumption   := NEW.current_reading - NEW.previous_reading;
    NEW.reading_month := EXTRACT(MONTH FROM NEW.reading_date)::INT;
    NEW.reading_year  := EXTRACT(YEAR FROM NEW.reading_date)::INT;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER calculate_consumption_trigger
    BEFORE INSERT OR UPDATE ON meter_readings
    FOR EACH ROW
    EXECUTE FUNCTION trg_calculate_consumption();

-- =============================================================================
-- STORED PROCEDURE: Generate Monthly Bills (cursor-based)
-- Loops through active customers/meters and generates bills for a given period
-- =============================================================================
CREATE OR REPLACE PROCEDURE generate_monthly_bills(
    IN p_billing_month INT,
    IN p_billing_year  INT
)
LANGUAGE plpgsql
AS $$
DECLARE
    v_meter_rec       RECORD;
    v_tariff_rec      RECORD;
    v_reading_rec     RECORD;
    v_charges         RECORD;
    v_bill_number     VARCHAR(50);
    v_due_date        DATE;
    v_penalty         NUMERIC(12,2);
    v_bill_count      INT := 0;
    meter_cursor CURSOR FOR
        SELECT m.id AS meter_id, m.meter_type, m.customer_id, c.full_names
        FROM meters m
        JOIN customers c ON c.id = m.customer_id
        WHERE m.status = 'ACTIVE' AND c.status = 'ACTIVE';
BEGIN
    v_due_date := (DATE_TRUNC('month', MAKE_DATE(p_billing_year, p_billing_month, 1))
                   + INTERVAL '1 month' + INTERVAL '14 days')::DATE;

    OPEN meter_cursor;
    LOOP
        FETCH meter_cursor INTO v_meter_rec;
        EXIT WHEN NOT FOUND;

        -- Skip if bill already exists for this period
        IF EXISTS (
            SELECT 1 FROM bills
            WHERE meter_id = v_meter_rec.meter_id
              AND billing_month = p_billing_month
              AND billing_year = p_billing_year
        ) THEN
            CONTINUE;
        END IF;

        -- Get active tariff for meter type
        SELECT * INTO v_tariff_rec
        FROM tariffs
        WHERE meter_type = v_meter_rec.meter_type
          AND active = TRUE
          AND effective_date <= MAKE_DATE(p_billing_year, p_billing_month, 1)
        ORDER BY version_number DESC
        LIMIT 1;

        IF NOT FOUND THEN
            RAISE NOTICE 'No active tariff for meter type % - skipping meter %',
                v_meter_rec.meter_type, v_meter_rec.meter_id;
            CONTINUE;
        END IF;

        -- Get reading for billing period
        SELECT * INTO v_reading_rec
        FROM meter_readings
        WHERE meter_id = v_meter_rec.meter_id
          AND reading_month = p_billing_month
          AND reading_year = p_billing_year;

        IF NOT FOUND THEN
            RAISE NOTICE 'No reading for meter % in %/% - skipping',
                v_meter_rec.meter_id, p_billing_month, p_billing_year;
            CONTINUE;
        END IF;

        -- Calculate charges using function
        SELECT * INTO v_charges
        FROM calculate_utility_charges(
            v_reading_rec.consumption,
            v_tariff_rec.rate_per_unit,
            v_tariff_rec.fixed_charge,
            v_tariff_rec.vat_percentage
        );

        v_penalty := 0;
        v_bill_number := 'BILL-' || p_billing_year || LPAD(p_billing_month::TEXT, 2, '0')
                         || '-' || v_meter_rec.meter_id || '-' || EXTRACT(EPOCH FROM NOW())::BIGINT;

        INSERT INTO bills (
            bill_number, customer_id, meter_id, billing_month, billing_year,
            consumption, tariff_id, fixed_charge, tax_amount, penalty_amount,
            total_amount, paid_amount, balance, bill_status, due_date, generated_date
        ) VALUES (
            v_bill_number,
            v_meter_rec.customer_id,
            v_meter_rec.meter_id,
            p_billing_month,
            p_billing_year,
            v_reading_rec.consumption,
            v_tariff_rec.id,
            v_tariff_rec.fixed_charge,
            v_charges.tax_amount,
            v_penalty,
            v_charges.total_amount,
            0,
            v_charges.total_amount,
            'PENDING',
            v_due_date,
            CURRENT_TIMESTAMP
        );

        v_bill_count := v_bill_count + 1;
    END LOOP;

    CLOSE meter_cursor;
    RAISE NOTICE 'Generated % bills for %/%', v_bill_count, p_billing_month, p_billing_year;
END;
$$;
