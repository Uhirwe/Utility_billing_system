-- =============================================================================
-- Utility Billing System - Test Data (v2 business rules)
-- Password for all users: Admin@12345
-- =============================================================================

\c utility_billing_db;

TRUNCATE payments, notifications, bills, meter_readings, meters, tariff_tiers, tariffs,
    user_roles, users, customers, roles, audit_logs RESTART IDENTITY CASCADE;

-- BCrypt hash for "Admin@12345"
-- $2a$10$CMSEo09O72R7RcvXmxgJhe8CGtOIbUV4E1FDIJlMC.nM8KKnP2ayW

INSERT INTO roles (id, name) VALUES
    (1, 'ROLE_ADMIN'), (2, 'ROLE_OPERATOR'), (3, 'ROLE_FINANCE'), (4, 'ROLE_CUSTOMER');
SELECT setval('roles_id_seq', 4);

INSERT INTO users (id, first_name, last_name, full_names, email, phone_country_code, phone_number, password, status, password_expired, account_locked) VALUES
    (1, 'System', 'Administrator', 'System Administrator', 'admin@utilitybilling.rw', '+250', '0788000001',
     '$2a$10$CMSEo09O72R7RcvXmxgJhe8CGtOIbUV4E1FDIJlMC.nM8KKnP2ayW', 'ACTIVE', FALSE, FALSE),
    (2, 'Operations', 'Manager', 'Operations Manager', 'operator@utilitybilling.rw', '+250', '0788000002',
     '$2a$10$CMSEo09O72R7RcvXmxgJhe8CGtOIbUV4E1FDIJlMC.nM8KKnP2ayW', 'ACTIVE', FALSE, FALSE),
    (3, 'Finance', 'Officer', 'Finance Officer', 'finance@utilitybilling.rw', '+250', '0788000003',
     '$2a$10$CMSEo09O72R7RcvXmxgJhe8CGtOIbUV4E1FDIJlMC.nM8KKnP2ayW', 'ACTIVE', FALSE, FALSE),
    (4, 'Customer', 'Portal User', 'Customer Portal User', 'customer@utilitybilling.rw', '+250', '0788000004',
     '$2a$10$CMSEo09O72R7RcvXmxgJhe8CGtOIbUV4E1FDIJlMC.nM8KKnP2ayW', 'ACTIVE', FALSE, FALSE);
SELECT setval('users_id_seq', 4);

INSERT INTO user_roles (user_id, role_id) VALUES (1,1),(2,2),(3,3),(4,4);

-- Customer 1 linked to self-registered portal user (user_id=4)
INSERT INTO customers (id, user_id, full_names, national_id, email, phone_country_code, phone_number, address, date_of_birth, status) VALUES
    (1, 4, 'Customer Portal User', '1199887766554433', 'customer@utilitybilling.rw', '+250', '0788000004', 'Kigali, Gasabo', '1990-05-15', 'ACTIVE'),
    (2, NULL, 'Marie Claire Uwase', '1199887766554434', 'marie.uwase@email.rw', '+250', '0788100002', 'Kigali, Kicukiro', '1988-03-20', 'ACTIVE'),
    (3, NULL, 'Patrick Habimana', '1199887766554435', 'patrick.habimana@email.rw', '+250', '0788100003', 'Kigali, Nyarugenge', '1992-11-10', 'ACTIVE'),
    (4, NULL, 'Grace Mukamana', '1199887766554436', 'grace.mukamana@email.rw', '+250', '0788100004', 'Huye, Ngoma', '1985-07-01', 'INACTIVE');
SELECT setval('customers_id_seq', 4);

-- Customer 1 has both WATER and ELECTRICITY meters (multiple meters per customer)
INSERT INTO meters (id, meter_number, meter_type, installation_date, status, billing_mode, customer_id) VALUES
    (1, 'WM-10001', 'WATER', '2024-01-15', 'ACTIVE', 'POSTPAID', 1),
    (2, 'EM-10002', 'ELECTRICITY', '2024-02-01', 'ACTIVE', 'POSTPAID', 1),
    (3, 'WM-10003', 'WATER', '2024-03-10', 'ACTIVE', 'POSTPAID', 3),
    (4, 'WM-10004', 'WATER', '2023-06-01', 'INACTIVE', 'PREPAID', 4);
SELECT setval('meters_id_seq', 4);

INSERT INTO tariffs (id, meter_type, tariff_name, rate_per_unit, fixed_charge, vat_percentage, late_penalty_percentage, version_number, effective_date, active) VALUES
    (1, 'WATER', 'Residential Water Tariff v1', 450.0000, 2000.00, 18.00, 5.00, 1, CURRENT_DATE, TRUE),
    (2, 'ELECTRICITY', 'Residential Electricity Tariff v1', 180.0000, 3500.00, 18.00, 5.00, 1, CURRENT_DATE, TRUE);
SELECT setval('tariffs_id_seq', 2);

INSERT INTO tariff_tiers (tariff_id, tier_name, min_units, max_units, rate_per_unit) VALUES
    (1, '0-10 units', 0, 10, 400.0000),
    (1, '11-30 units', 10, 30, 450.0000),
    (1, '31-100 units', 30, 100, 500.0000),
    (2, '0-10 units', 0, 10, 150.0000),
    (2, '11-30 units', 10, 30, 180.0000),
    (2, '31-100 units', 30, 100, 200.0000);

INSERT INTO meter_readings (meter_id, previous_reading, current_reading, reading_date) VALUES
    (1, 100.00, 125.50, CURRENT_DATE - INTERVAL '20 days'),
    (2, 500.00, 620.00, CURRENT_DATE - INTERVAL '20 days'),
    (3, 150.00, 178.00, CURRENT_DATE - INTERVAL '15 days');
-- Note: meter 2 readings for customer 1 electricity meter use meter_id=2
