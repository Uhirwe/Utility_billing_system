# Utility Billing System — Architecture Diagram Codes

Paste these into [Mermaid Live Editor](https://mermaid.live), draw.io (Mermaid plugin), GitHub Markdown, or PlantUML tools.

---

## 1. High-Level System Architecture (C4 Container)

```mermaid
flowchart TB
    subgraph Clients["Client Layer"]
        WEB["Web / Mobile Client"]
        SWAGGER["Swagger UI\n/api/swagger-ui.html"]
        POSTMAN["Postman / API Client"]
    end

    subgraph App["Spring Boot 3.3 — Java 21"]
        API["REST Controllers\n/context-path: /api"]
        SEC["Spring Security + JWT Filter"]
        SVC["Service Layer"]
        REPO["Spring Data JPA Repositories"]
        AUDIT["Audit Service"]
        EMAIL["Email Service"]
        CALC["Billing Calculator"]
    end

    subgraph Data["Data Layer"]
        PG[("PostgreSQL\nutility_billing_db")]
        TRG["Triggers & Functions"]
        PROC["Stored Procedures\ngenerate_monthly_bills()"]
    end

    subgraph External["External Systems"]
        SMTP["SMTP Mail Server"]
    end

    WEB --> API
    SWAGGER --> API
    POSTMAN --> API
    API --> SEC
    SEC --> SVC
    SVC --> REPO
    SVC --> AUDIT
    SVC --> EMAIL
    SVC --> CALC
    REPO --> PG
    PG --> TRG
    PG --> PROC
    EMAIL --> SMTP
```

---

## 2. Layered Application Architecture

```mermaid
flowchart TD
    subgraph Presentation["Presentation Layer"]
        AC[AuthController]
        UC[UserController]
        CC[CustomerController]
        MC[MeterController]
        RC[MeterReadingController]
        TC[TariffController]
        BC[BillController]
        PC[PaymentController]
        NC[NotificationController]
        AUC[AuditController]
    end

    subgraph Security["Security Layer"]
        JWT[JwtAuthenticationFilter]
        JTP[JwtTokenProvider]
        CDS[CustomUserDetailsService]
        SC[SecurityConfig]
    end

    subgraph Business["Business / Service Layer"]
        AS[AuthService]
        US[UserService]
        CS[CustomerService]
        MS[MeterService]
        RS[MeterReadingService]
        TS[TariffService]
        BS[BillService]
        PS[PaymentService]
        NS[NotificationService]
        ES[EmailService]
        AU[AuditService]
    end

    subgraph Mapping["Mapper Layer"]
        MAP[UserMapper, CustomerMapper,\nBillMapper, TariffMapper, ...]
    end

    subgraph Persistence["Persistence Layer"]
        UR[UserRepository]
        CR[CustomerRepository]
        MR[MeterRepository]
        RR[MeterReadingRepository]
        TR[TariffRepository]
        BR[BillRepository]
        PR[PaymentRepository]
        NR[NotificationRepository]
        AR[AuditLogRepository]
    end

    subgraph Domain["Domain / Entity Layer"]
        ENT[User, Customer, Meter,\nMeterReading, Tariff, TariffTier,\nBill, Payment, Notification, AuditLog]
    end

    subgraph CrossCutting["Cross-Cutting"]
        GEH[GlobalExceptionHandler]
        APR[ApiResponse Wrapper]
        VAL[Jakarta Validation]
        OAPI[OpenApiConfig / Swagger]
    end

    AC & UC & CC & MC & RC & TC & BC & PC & NC & AUC --> SEC
    SEC --> JWT --> CDS
    AC & UC & CC & MC & RC & TC & BC & PC & NC & AUC --> AS & US & CS & MS & RS & TS & BS & PS & NS & AU
    US --> ES
    AS & US & CS & MS & RS & TS & BS & PS & NS --> MAP
    AS & US & CS & MS & RS & TS & BS & PS & NS & AU --> UR & CR & MR & RR & TR & BR & PR & NR & AR
    UR & CR & MR & RR & TR & BR & PR & NR & AR --> ENT
```

---

## 3. Module / Package Structure

```mermaid
flowchart LR
    ROOT["com.utilitybilling"]

    ROOT --> CONFIG["config/\nSecurity, JPA, OpenAPI, DataInit"]
    ROOT --> SECPKG["security/\nCustomUserDetailsService"]
    ROOT --> JWT["security/jwt/\nFilter, Provider, Properties"]
    ROOT --> CTRL["controller/\n10 REST Controllers"]
    ROOT --> SVC["service/ + service/impl/"]
    ROOT --> REPO["repository/\n9 JPA Repositories"]
    ROOT --> ENT["entity/\n11 Entities + BaseAuditEntity"]
    ROOT --> DTO["dto/\nRequest & Response DTOs"]
    ROOT --> MAP["mapper/\nEntity ↔ DTO"]
    ROOT --> ENUM["enums/\nStatus, Roles, Methods"]
    ROOT --> EXC["exception/\n5 Custom + GlobalHandler"]
    ROOT --> PAY["payload/\nApiResponse<T>"]
    ROOT --> UTIL["util/\nValidators, BillingCalculator"]
```

---

## 4. Security & JWT Authentication Flow

```mermaid
sequenceDiagram
    autonumber
    actor User
    participant Swagger as Swagger / Client
    participant Auth as AuthController
    participant AuthSvc as AuthService
    participant Sec as Spring Security
    participant JWT as JwtTokenProvider
    participant Filter as JwtAuthenticationFilter
    participant API as Protected Controller

    User->>Swagger: POST /auth/login {email, password}
    Swagger->>Auth: LoginRequest
    Auth->>AuthSvc: login()
    AuthSvc->>AuthSvc: Validate active, not locked,\npassword_expired=false
    AuthSvc->>Sec: authenticate()
    Sec-->>AuthSvc: Authentication
    AuthSvc->>JWT: generateToken()
    JWT-->>AuthSvc: JWT accessToken
    AuthSvc-->>Swagger: AuthResponse + Bearer token

    User->>Swagger: Authorize Bearer token
    User->>Swagger: GET /customers (example)
    Swagger->>Filter: Authorization: Bearer xxx
    Filter->>JWT: validateToken()
    JWT-->>Filter: valid + email
    Filter->>Sec: set SecurityContext
    Filter->>API: forward request
    API->>API: @PreAuthorize role check
    API-->>Swagger: ApiResponse<T>
```

---

## 5. Admin User Creation & First Login Flow

```mermaid
sequenceDiagram
    autonumber
    actor Admin
    participant UC as UserController
    participant US as UserService
    participant PG as PasswordGenerator
    participant Mail as EmailService
    participant DB as PostgreSQL
    actor NewUser

    Admin->>UC: POST /users (ADMIN)
    UC->>US: createUser()
    US->>PG: generateTemporaryPassword()
    US->>DB: save user (password_expired=true)
    US->>Mail: sendWelcomeEmail(temp password)
    US->>US: auditService.log(USER_CREATED)
    US-->>Admin: UserResponse

    NewUser->>UC: POST /auth/first-login/change-password
    Note over NewUser,DB: Public endpoint — no JWT yet
    UC->>US: completeFirstLogin()
    US->>DB: update password, password_expired=false
    US-->>NewUser: Success

    NewUser->>UC: POST /auth/login
    UC-->>NewUser: JWT token
```

---

## 6. Core Billing Business Flow

```mermaid
sequenceDiagram
    autonumber
    actor Operator
    actor Finance
    participant API as REST API
    participant SVC as BillService
    participant READ as MeterReadingRepository
    participant TAR as TariffRepository
    participant CALC as BillingCalculator
    participant DB as PostgreSQL
    participant TRG as DB Triggers

    Operator->>API: POST /readings
    API->>DB: save reading (consumption auto-calc)

    Finance->>API: POST /bills/generate
    API->>SVC: generateBill()
    SVC->>READ: find reading for period
    SVC->>TAR: find active tariff + tiers
    SVC->>CALC: calculateWithTiers()
    SVC->>DB: insert bill (UNPAID)
    DB->>TRG: bill_generated_notification
    TRG->>DB: insert notification
    SVC-->>Finance: BillResponse

    Finance->>API: PATCH /bills/{id}/approve
    Note over Finance,API: @PreAuthorize FINANCE only
    API->>SVC: approveBill()
    SVC->>DB: approved=true, status=APPROVED

    Finance->>API: POST /payments
    API->>DB: save payment, update balance
    DB->>TRG: bill_paid_notification (if balance=0)
    API-->>Finance: PaymentResponse
```

---

## 7. Role-Based Access Control (RBAC)

```mermaid
flowchart LR
    subgraph Roles
        ADMIN["ROLE_ADMIN"]
        OP["ROLE_OPERATOR"]
        FIN["ROLE_FINANCE"]
        CUST["ROLE_CUSTOMER"]
    end

    subgraph Modules
        M1["User Mgmt\nPOST /users"]
        M2["Customers\nPOST /customers"]
        M3["Meters & Readings"]
        M4["Tariffs"]
        M5["Bills Generate"]
        M6["Bill Approve"]
        M7["Payments"]
        M8["Audit Logs"]
        M9["Overdue BI"]
    end

    ADMIN --> M1 & M2 & M3 & M4 & M5 & M6 & M7 & M8 & M9
    OP --> M2 & M3 & M7
    FIN --> M4 & M5 & M6 & M7
    CUST --> M7
```

---

## 8. Database Architecture

```mermaid
erDiagram
    USERS ||--o{ USER_ROLES : has
    ROLES ||--o{ USER_ROLES : assigned
    CUSTOMERS ||--|| METERS : owns_1_to_1
    METERS ||--o{ METER_READINGS : records
    TARIFFS ||--o{ TARIFF_TIERS : contains
    TARIFFS ||--o{ BILLS : priced_by
    CUSTOMERS ||--o{ BILLS : receives
    METERS ||--o{ BILLS : billed_on
    BILLS ||--o{ PAYMENTS : paid_by
    CUSTOMERS ||--o{ NOTIFICATIONS : notified

    USERS {
        bigint id PK
        string first_name
        string last_name
        string email UK
        boolean password_expired
        boolean account_locked
    }

    CUSTOMERS {
        bigint id PK
        string national_id UK
        date date_of_birth
        string status
    }

    METERS {
        bigint id PK
        string meter_number UK
        bigint customer_id UK_FK
        string status
    }

    TARIFF_TIERS {
        bigint id PK
        bigint tariff_id FK
        decimal min_units
        decimal max_units
        decimal rate_per_unit
    }

    BILLS {
        bigint id PK
        string bill_number UK
        boolean approved
        string bill_status
        decimal balance
    }

    AUDIT_LOGS {
        bigint id PK
        string actor_email
        string action_type
        string entity_type
    }
```

---

## 9. PostgreSQL Database Routines

```mermaid
flowchart TD
    subgraph Triggers
        T1["trg_bill_generated_notification\nAFTER INSERT ON bills"]
        T2["trg_bill_paid_notification\nBEFORE UPDATE ON bills"]
        T3["trg_calculate_consumption\nBEFORE INSERT/UPDATE ON meter_readings"]
    end

    subgraph Functions
        F1["calculate_utility_charges()\n→ subtotal, tax, total"]
    end

    subgraph Procedures
        P1["generate_monthly_bills(month, year)\nCursor over active customers/meters"]
    end

    P1 --> F1
    P1 --> T1
    T2 --> NOTIF["notifications table"]
    T1 --> NOTIF
    T3 --> READ["meter_readings.consumption"]
```

---

## 10. Deployment Architecture

```mermaid
flowchart TB
    subgraph DevMachine["Developer Machine"]
        IDE["IntelliJ IDEA"]
        APP["Spring Boot JAR\nPort 8080\nContext /api"]
    end

    subgraph LocalServices["Local Services"]
        PG[("PostgreSQL 18\nPort 5432\nutility_billing_db")]
        MAIL["SMTP / MailHog\nPort 1025"]
    end

    subgraph Tools["API Tools"]
        SW["Swagger UI\n:8080/api/swagger-ui.html"]
    end

    IDE --> APP
    APP --> PG
    APP --> MAIL
    SW --> APP
```

---

## 11. Request/Response Pipeline

```mermaid
flowchart LR
    REQ["HTTP Request"] --> FILTER["JwtAuthenticationFilter"]
    FILTER --> AUTHZ["@PreAuthorize RBAC"]
    AUTHZ --> CTRL["Controller"]
    CTRL --> VAL["@Valid DTO Validation"]
    VAL --> SVC["Service + Business Rules"]
    SVC --> REPO["Repository"]
    REPO --> DB[("PostgreSQL")]
    SVC --> AUD["Audit Log"]
    SVC --> WRAP["ApiResponse<T>"]
    WRAP --> GEH["GlobalExceptionHandler\n(on error)"]
    WRAP --> RES["JSON Response"]
```

---

## 12. PlantUML — Component Diagram (alternative)

```plantuml
@startuml UtilityBilling_Component
!theme plain

package "Presentation" {
  [AuthController]
  [UserController]
  [CustomerController]
  [MeterController]
  [BillController]
  [PaymentController]
}

package "Security" {
  [JwtAuthenticationFilter]
  [SecurityConfig]
  [CustomUserDetailsService]
}

package "Business Services" {
  [AuthService]
  [UserService]
  [CustomerService]
  [BillService]
  [PaymentService]
  [EmailService]
  [AuditService]
}

package "Persistence" {
  database "PostgreSQL" {
    [JPA Repositories]
    [Triggers & Procedures]
  }
}

[AuthController] --> [AuthService]
[UserController] --> [UserService]
[BillController] --> [BillService]
[UserService] --> [EmailService]
[UserService] --> [AuditService]
[BillService] --> [JPA Repositories]
[JPA Repositories] --> [Triggers & Procedures]

[JwtAuthenticationFilter] ..> [SecurityConfig]
@enduml
```

---

## 13. PlantUML — Deployment Diagram

```plantuml
@startuml UtilityBilling_Deployment
node "Client" {
  artifact "Browser / Postman"
}

node "Application Server" {
  artifact "utility-billing-system.jar\nJava 21 + Spring Boot 3.3"
}

node "Database Server" {
  database "PostgreSQL\nutility_billing_db" as PG
}

cloud "SMTP Server" as SMTP

"Browser / Postman" --> "utility-billing-system.jar" : HTTPS/HTTP :8080/api
"utility-billing-system.jar" --> PG : JDBC :5432
"utility-billing-system.jar" --> SMTP : SMTP :1025
@enduml
```

---

## Tools to Render These Diagrams

| Tool | URL | Format |
|------|-----|--------|
| Mermaid Live | https://mermaid.live | `.mmd` / paste code |
| draw.io | https://app.diagrams.net | Insert → Mermaid |
| PlantUML | https://www.plantuml.com/plantuml | PlantUML blocks |
| VS Code | Mermaid / PlantUML extensions | `.md` files |
| GitHub | Push this `.md` file | Auto-renders Mermaid |
