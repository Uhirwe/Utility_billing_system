# Utility Billing System (WASAC / REG)

Enterprise Spring Boot backend for **water + electricity postpaid** utility billing in Rwanda.

## Integrated Situation

WASAC (water) and REG (electricity) are unifying billing. Water is postpaid; electricity is transitioning from prepaid to postpaid. Meters support `POSTPAID` (billed monthly) and `PREPAID` (legacy — excluded from monthly billing).

## Tech Stack

Java 21 · Spring Boot 3.3.5 · PostgreSQL · JPA · Spring Security + JWT · Swagger

## Prerequisites

This project requires **JDK 21**. Maven will fail on other Java versions.

```bash
# macOS — point JAVA_HOME at JDK 21 before building
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
java -version   # should report 21.x
mvn clean install
```

The repo includes `.java-version` for jenv, asdf, and similar tools.

## Database Setup

**macOS / Linux**

```bash
export PGPASSWORD="your_postgres_password"
createdb -U postgres utility_billing_db 2>/dev/null || true
tail -n +7 database/schema.sql | psql -U postgres -d utility_billing_db
psql -U postgres -d utility_billing_db -f database/migration-v2-business-rules.sql
psql -U postgres -d utility_billing_db -f database/migration-v3-wasac-business-rules.sql
psql -U postgres -d utility_billing_db -f database/migration-v4-project-spec.sql
tail -n +7 database/test-data.sql | psql -U postgres -d utility_billing_db
```

**Windows (PowerShell)**

```powershell
cd database
.\apply-schema.ps1 -Password "your_postgres_password"
psql -U postgres -d utility_billing_db -f database/migration-v2-business-rules.sql
psql -U postgres -d utility_billing_db -f database/migration-v3-wasac-business-rules.sql
psql -U postgres -d utility_billing_db -f database/migration-v4-project-spec.sql
psql -U postgres -d utility_billing_db -f database/test-data.sql
```

## Project Specification Compliance

| Task | Requirement | Status |
|------|-------------|--------|
| **1** | JWT auth, signup/login, secure endpoints | ✅ |
| **1** | User: names, email, phone (country code + local), password rules, status | ✅ |
| **1** | ROLE_ADMIN: tariffs, generate bills, manage users | ✅ |
| **1** | ROLE_OPERATOR: capture readings | ✅ |
| **1** | ROLE_FINANCE: approve bills + payments | ✅ |
| **1** | ROLE_CUSTOMER: view bills + payment history | ✅ |
| **2** | Customer fields, duplicate prevention, inactive no bills | ✅ |
| **2** | Multiple meters per customer (water/electricity) | ✅ |
| **3** | Reading rules (current>previous, one/month, active meter) | ✅ |
| **4** | Flat + tier tariffs, VAT, penalties, versioning, future effective date | ✅ |
| **5** | Payments: partial/full, balance update, PAID status | ✅ |
| **6** | DB triggers + notification message format | ✅ |
| **Docs** | ERD (`database/ERD.md`), flow diagrams (`docs/SYSTEM_ARCHITECTURE.md`) | ✅ |
| **Docs** | Swagger UI at `/api/swagger-ui.html` | ✅ |

## Roles & Endpoints

| Role | Key Endpoints |
|------|---------------|
| **ADMIN** | `GET/PUT/DELETE /users`, `POST/PUT /tariffs`, `POST /bills/generate` |
| **OPERATOR** | `POST /readings`, `POST /meters`, `PATCH /customers/{id}/activate` |
| **FINANCE** | `PATCH /bills/{id}/approve`, `POST /payments` |
| **CUSTOMER** | `POST /auth/register`, `GET /bills/me`, `GET /payments/me`, `GET /customers/me` |

## Customer Registration

```json
POST /auth/register
{
  "firstName": "Eric",
  "lastName": "Mugabo",
  "nationalId": "1199887766554401",
  "email": "customer@email.rw",
  "phoneCountryCode": "+250",
  "phoneNumber": "0788123456",
  "address": "Kigali, Rwanda",
  "dateOfBirth": "1995-01-15",
  "password": "Customer@123"
}
```

## Notification Message Format (Task 6)

DB triggers in `database/schema.sql` insert:

- **Bill:** `Dear {name}, Your {Month}/{year} utility bill of {amount} FRW has been successfully processed.`
- **Payment:** `Dear {name}, Your payment of {amount} FRW has been successfully received.`

## Test Credentials

| Role | Email | Password |
|------|-------|----------|
| Admin | admin@utilitybilling.rw | Admin@12345 |
| Operator | operator@utilitybilling.rw | Admin@12345 |
| Finance | finance@utilitybilling.rw | Admin@12345 |
| Customer | customer@utilitybilling.rw | Admin@12345 |

## Artifacts

- `database/schema.sql` — tables, triggers, functions, stored procedures
- `database/ERD.md` — entity relationship diagram
- `docs/SYSTEM_ARCHITECTURE.md` — Spring Boot flow diagrams
