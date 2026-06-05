# Utility Billing System - Entity Relationship Diagram

## Overview

The Utility Billing System manages utility customers, meters, readings, tariffs, billing, payments, and notifications with role-based user authentication.

## ERD (Mermaid)

```mermaid
erDiagram
    ROLES ||--o{ USER_ROLES : has
    USERS ||--o{ USER_ROLES : assigned

    CUSTOMERS ||--o{ METERS : owns
    CUSTOMERS ||--o{ BILLS : receives
    CUSTOMERS ||--o{ NOTIFICATIONS : receives

    METERS ||--o{ METER_READINGS : records
    METERS ||--o{ BILLS : billed_on

    TARIFFS ||--o{ BILLS : applied_to
    BILLS ||--o{ PAYMENTS : paid_by

    ROLES {
        bigint id PK
        varchar name UK
        timestamp created_at
        timestamp updated_at
    }

    USERS {
        bigint id PK
        varchar full_names
        varchar email UK
        varchar phone_number
        varchar password
        varchar status
        timestamp created_at
        timestamp updated_at
    }

    USER_ROLES {
        bigint user_id PK,FK
        bigint role_id PK,FK
    }

    CUSTOMERS {
        bigint id PK
        varchar full_names
        varchar national_id UK
        varchar email UK
        varchar phone_number UK
        varchar address
        varchar status
        timestamp created_at
        timestamp updated_at
    }

    METERS {
        bigint id PK
        varchar meter_number UK
        varchar meter_type
        date installation_date
        varchar status
        bigint customer_id FK
        timestamp created_at
        timestamp updated_at
    }

    METER_READINGS {
        bigint id PK
        bigint meter_id FK
        decimal previous_reading
        decimal current_reading
        decimal consumption
        date reading_date
        int reading_month
        int reading_year
        timestamp created_at
        timestamp updated_at
    }

    TARIFFS {
        bigint id PK
        varchar meter_type
        varchar tariff_name
        decimal rate_per_unit
        decimal fixed_charge
        decimal vat_percentage
        decimal late_penalty_percentage
        int version_number
        date effective_date
        boolean active
        timestamp created_at
        timestamp updated_at
    }

    BILLS {
        bigint id PK
        varchar bill_number UK
        bigint customer_id FK
        bigint meter_id FK
        int billing_month
        int billing_year
        decimal consumption
        bigint tariff_id FK
        decimal fixed_charge
        decimal tax_amount
        decimal penalty_amount
        decimal total_amount
        decimal paid_amount
        decimal balance
        varchar bill_status
        date due_date
        timestamp generated_date
        timestamp created_at
        timestamp updated_at
    }

    PAYMENTS {
        bigint id PK
        varchar payment_reference UK
        bigint bill_id FK
        decimal amount_paid
        varchar payment_method
        timestamp payment_date
        timestamp created_at
        timestamp updated_at
    }

    NOTIFICATIONS {
        bigint id PK
        bigint customer_id FK
        varchar title
        text message
        timestamp notification_date
        boolean read_status
        timestamp created_at
        timestamp updated_at
    }
```

## Entity Relationships

| Parent | Child | Cardinality | Description |
|--------|-------|-------------|-------------|
| USERS | USER_ROLES | 1:N | Users can have multiple roles |
| ROLES | USER_ROLES | 1:N | Roles assigned to many users |
| CUSTOMERS | METERS | 1:N | One customer can own multiple meters |
| METERS | METER_READINGS | 1:N | One reading per meter per month/year |
| CUSTOMERS | BILLS | 1:N | Customer receives multiple bills |
| METERS | BILLS | 1:N | Bills generated per meter per period |
| TARIFFS | BILLS | 1:N | Tariff version snapshot on each bill |
| BILLS | PAYMENTS | 1:N | Bills can have partial/full payments |
| CUSTOMERS | NOTIFICATIONS | 1:N | Notifications sent to customers |

## Database Routines

| Routine | Type | Purpose |
|---------|------|---------|
| `calculate_utility_charges()` | Function | Computes subtotal, VAT, and total |
| `generate_monthly_bills()` | Stored Procedure | Cursor-based monthly bill generation |
| `trg_bill_generated_notification()` | Trigger | Auto-notify on bill insert |
| `trg_bill_paid_notification()` | Trigger | Mark PAID and notify on zero balance |
| `trg_calculate_consumption()` | Trigger | Auto-calculate consumption on reading |
