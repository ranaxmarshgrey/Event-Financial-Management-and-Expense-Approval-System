# Event Financial Management and Expense Approval System (EFMS)

A Java-based web application built using **Spring Boot MVC** for managing the complete financial lifecycle of college events — budget creation, expense tracking, approval workflows, and final budget closure.

> **OOAD Mini Project — Semester 6**
> Team project implementing layered MVC architecture with SOLID principles and design patterns.

---

## Table of Contents
1. [Overview](#overview)
2. [Team Scope](#team-scope)
3. [Current Functionality](#current-functionality)
4. [Tech Stack](#tech-stack)
5. [Architecture](#architecture)
6. [Design Principles & Patterns](#design-principles--patterns)
7. [How to Run](#how-to-run)
8. [REST API Endpoints](#rest-api-endpoints)
9. [Using the Web UI](#using-the-web-ui)
10. [Project Structure](#project-structure)
11. [Testing with Postman](#testing-with-postman)

---

## Overview

The full system is designed to support three user roles managing the finances of college events:

| Role | Responsibilities |
|---|---|
| **Event Organizer** | Creates events & budgets, defines categories, allocates funds, submits expenses |
| **Approving Authority** | Reviews and approves/rejects expense claims |
| **Finance Admin** | Oversees all events, closes budgets, generates reports |

The system enforces structured financial planning, role-based access, budget discipline, and transparency.

---

## Team Scope

The project is divided across team members. This branch currently implements:

### Implemented in this branch
- **Major UC #1:** Create Event Budget (Event Organizer)
- **Minor UC #1:** Budget Variance Alerts (automatic validation)
- **Major UC #2:** Submit Expense Claim (Event Organizer)
- **Minor UC #2:** Role-Based Approval Flow (routing strategy at submission)

### Planned (future branches)
- Expense approval / rejection workflow (Approving Authority) — UC #3
- Expense Category Rules (Finance Admin) — minor UC #3
- Budget closure and reporting (Finance Admin) — UC #4
- Authentication and role-based access control
- Notification system

---

## Current Functionality

### Major Use Case — Create Event Budget

The Event Organizer follows a four-step workflow:

1. **Create Event** — Organizer creates a new event and an empty draft budget with a total limit.
2. **Define Categories** — Add expense categories (Food, Transport, Prizes, etc.) and allocate funds to each.
3. **Validate Budget** — Automatic variance checks run; budget transitions `DRAFT → READY` if no blocking alerts.
4. **Submit for Approval** — Strategy-based approval policy determines the outcome (auto-approved vs. manual review).

**Budget lifecycle states:** `DRAFT → READY → SUBMITTED → (APPROVED | REJECTED)`

### Minor Use Case — Budget Variance Alerts

Automatically triggered during validation and submission. The system checks:

| Alert Code | Severity | Trigger |
|---|---|---|
| `NO_CATEGORIES` | ERROR | Budget has no expense categories |
| `ALLOCATION_EXCEEDS_LIMIT` | ERROR | Sum of category allocations exceeds the total limit |
| `ALLOCATION_NEAR_LIMIT` | WARNING | Allocation reaches 90% of the total limit (advisory) |
| `INVALID_CATEGORY_ALLOCATION` | ERROR | A category has non-positive allocation |
| `CATEGORY_OVERSPENT` | ERROR | Spent amount exceeds allocated amount in a category |

**ERROR-severity alerts are blocking** — they prevent budget submission. **WARNING alerts are advisory** and don't block the flow.

### Major Use Case — Submit Expense Claim

Once a budget has been **APPROVED**, the Event Organizer can submit expense claims against any of its categories. The workflow:

1. **Choose a category** from an APPROVED budget.
2. **Fill in the claim** — description, amount, expense date, optional supporting-document URL.
3. **System validates** — budget must be APPROVED, category must have headroom (committed = pending + approved expenses), amount must be positive.
4. **Routing strategy stamps the approval chain** (minor UC) and the expense is persisted as `PENDING_APPROVAL`.

**Expense lifecycle states:** `PENDING_APPROVAL → (APPROVED | REJECTED)` *(approve/reject action itself belongs to UC #3).*

### Minor Use Case — Role-Based Approval Flow

At submission time, an `ApprovalRoutingStrategy` decides which approval chain the expense must traverse:

| Level | Meaning |
|---|---|
| `LEVEL_1` | Single-step approval (e.g. Faculty Coordinator) |
| `LEVEL_1_AND_2` | Two-step approval (e.g. Faculty Coordinator + Finance Committee) |

Two concrete strategies ship today:

- **`AmountBasedRoutingStrategy`** (default, `@Primary`) — ≤ ₹2,000 → `LEVEL_1`, otherwise `LEVEL_1_AND_2`.
- **`CategoryBasedRoutingStrategy`** — sensitive categories (`Prizes`, `Honorarium`, `Sponsorship`) always require `LEVEL_1_AND_2`; everything else `LEVEL_1`.

Swapping the active policy is a one-line change (`@Primary`) — no service-layer edit (Open/Closed).

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.2.5 (Web, Data JPA, Validation) |
| Database | H2 (in-memory — no setup required) |
| Build Tool | Maven |
| Frontend | Static HTML + Vanilla JavaScript + CSS (served by Spring Boot) |
| API Style | REST (JSON) |
| API Testing | Postman |

---

## Architecture

Classic Spring Boot layered MVC:

```
┌─────────────────────────────────────────────┐
│ Client (Browser UI / Postman / REST client) │
└─────────────────┬───────────────────────────┘
                  │ HTTP (JSON)
┌─────────────────▼───────────────────────────┐
│ CONTROLLER layer       (package: controller)│  ← HTTP only
│   BudgetController, OrganizerController     │
└─────────────────┬───────────────────────────┘
                  │
┌─────────────────▼───────────────────────────┐
│ SERVICE layer          (package: service)   │  ← business logic
│   BudgetService, VarianceAlertService       │
│   uses ApprovalStrategy (Strategy Pattern)  │
└─────────────────┬───────────────────────────┘
                  │
┌─────────────────▼───────────────────────────┐
│ REPOSITORY layer       (package: repository)│  ← data access
│   Spring Data JPA interfaces                │
└─────────────────┬───────────────────────────┘
                  │
┌─────────────────▼───────────────────────────┐
│ MODEL layer            (package: model)     │  ← JPA entities
│   User (abstract), Organizer, Event,        │
│   Budget, ExpenseCategory, BudgetStatus     │
└─────────────────┬───────────────────────────┘
                  │
              [ H2 in-memory DB ]
```

---

## Design Principles & Patterns

### SOLID Principles

| Principle | Where |
|---|---|
| **Single Responsibility** | `VarianceAlertService` only reports alerts; `BudgetService` only orchestrates; `BudgetController` only handles HTTP |
| **Open/Closed** | `ApprovalStrategy` interface — new approval policies can be added without modifying `BudgetService` |
| **Liskov Substitution** | Abstract `User` base class; `Organizer` (and future `ApprovingAuthority`, `FinanceAdmin`) substitutable anywhere a `User` is expected |
| **Interface Segregation** | Small, focused Spring Data repository interfaces (`BudgetRepository`, `EventRepository`, `OrganizerRepository`) |
| **Dependency Inversion** | `BudgetService` depends on the `ApprovalStrategy` interface, not a concrete implementation |

### Design Patterns

- **Strategy Pattern** — `ApprovalStrategy` interface with two concrete implementations:
  - `AutoApproveUnderThresholdStrategy` (default, marked `@Primary`) — auto-approves budgets under ₹10,000
  - `ManualApprovalStrategy` — requires manual review for every budget

  Swapping the active policy is a one-line configuration change, no service-layer modification required.

---

## How to Run

### Prerequisites
- **Java 17** or higher
- **Maven 3.6+**

### Steps

```bash
# Clone the repo
git clone https://github.com/harshakadapala-17/Event-Financial-Management-and-Expense-Approval-System.git
cd Event-Financial-Management-and-Expense-Approval-System

# Check out this feature branch
git checkout feature/create-event-budget

# Run the application
mvn spring-boot:run
```

The application starts on **http://localhost:8080**.

### Useful URLs

| URL | Description |
|---|---|
| http://localhost:8080/ | Web UI (Event Organizer portal) |
| http://localhost:8080/api/organizers | List organizers (sanity-check the API) |
| http://localhost:8080/h2-console | H2 database console (JDBC URL: `jdbc:h2:mem:efmsdb`, user `sa`, blank password) |

On startup, a demo organizer is seeded automatically:
- **Name:** Demo Organizer
- **Email:** demo@college.edu
- **ID:** 1

---

## REST API Endpoints

### Organizers

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/organizers` | List all organizers |
| `POST` | `/api/organizers` | Create an organizer (JSON: `{"name":"...","email":"..."}`) |

### Budgets (Create Event Budget use case)

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/budgets` | Create event + draft budget |
| `POST` | `/api/budgets/{id}/categories` | Add an expense category |
| `POST` | `/api/budgets/{id}/validate` | Run variance checks; transitions DRAFT → READY if valid |
| `POST` | `/api/budgets/{id}/submit` | Submit for approval (Strategy pattern decides the outcome) |
| `GET` | `/api/budgets/{id}` | Get current state of a budget |

### Example: Create Event + Budget

```http
POST /api/budgets
Content-Type: application/json

{
  "name": "TechFest 2026",
  "description": "Annual technical festival",
  "startDate": "2026-05-01",
  "endDate": "2026-05-03",
  "organizerId": 1,
  "totalBudget": 8000
}
```

Response:
```json
{
  "id": 1,
  "eventId": 1,
  "eventName": "TechFest 2026",
  "totalLimit": 8000.00,
  "allocatedTotal": 0.00,
  "remaining": 8000.00,
  "status": "DRAFT",
  "categories": []
}
```

### Expenses (Submit Expense Claim use case)

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/expenses` | Submit an expense claim against a category of an APPROVED budget |
| `GET` | `/api/expenses?budgetId={id}` | List all expenses for a budget |
| `GET` | `/api/expenses/{id}` | Get a single expense |
| `GET` | `/api/expenses/routing-policy` | Show which ApprovalRoutingStrategy is currently active |

#### Example: Submit an Expense

```http
POST /api/expenses
Content-Type: application/json

{
  "categoryId": 1,
  "submittedById": 1,
  "description": "Catering for day 1",
  "amount": 1500,
  "expenseDate": "2026-05-01",
  "supportingDocUrl": "https://example.com/receipt.pdf"
}
```

Response:
```json
{
  "id": 1,
  "categoryId": 1,
  "categoryName": "Food",
  "budgetId": 1,
  "description": "Catering for day 1",
  "amount": 1500.00,
  "expenseDate": "2026-05-01",
  "status": "PENDING_APPROVAL",
  "requiredApprovalLevel": "LEVEL_1",
  "submittedBy": "Demo Organizer"
}
```

### Example: Submit a Budget (Strategy Pattern in Action)

```http
POST /api/budgets/1/submit
```

Response:
```json
{
  "outcome": "AUTO_APPROVED",
  "reason": "Budget under auto-approval threshold of 10000",
  "strategyName": "AUTO_UNDER_THRESHOLD"
}
```

---

## Using the Web UI

Open **http://localhost:8080/** after starting the app. The single-page UI walks you through the entire use case:

1. **Step 1 — Create Event**: Fill the form (event name, dates, organizer, total budget) and click **Create Draft Budget**.
2. **Step 2 — Add Categories**: Use the inline form to add expense categories. Totals update live.
3. **Step 3 — Validate**: Click **Validate Budget** to run variance checks. Alerts appear color-coded:
   - Green → all clear
   - Yellow → warning (advisory)
   - Red → error (blocking)
4. **Step 4 — Submit**: Click **Submit for Approval**. The decision card shows the Strategy outcome.

The budget status badge (DRAFT / READY / SUBMITTED / APPROVED / REJECTED) is always visible and updates as you progress.

### Try the happy path
Create a budget of ₹8000, add three categories totaling ₹7500, validate (expect a WARNING: near limit), submit → **AUTO_APPROVED**.

### Try the variance alert path
Create a budget of ₹5000, add a category of ₹6000, validate (expect an **ERROR**: `ALLOCATION_EXCEEDS_LIMIT`), submit → **Submission Blocked** (HTTP 400).

---

## Project Structure

```
PROJECT/
├── pom.xml                        Maven config (Spring Boot 3.2.5, Java 17)
├── README.md                      This file
├── postman/
│   └── EFMS.postman_collection.json   Importable Postman collection
└── src/main/
    ├── resources/
    │   ├── application.properties      H2 config, server port
    │   └── static/                     Frontend (served at /)
    │       ├── index.html
    │       ├── styles.css
    │       └── app.js
    └── java/com/ooad/efms/
        ├── EfmsApplication.java        Spring Boot entry point
        ├── model/                      JPA entities
        │   ├── User.java               (abstract, SINGLE_TABLE inheritance)
        │   ├── Organizer.java
        │   ├── Event.java
        │   ├── Budget.java
        │   ├── ExpenseCategory.java
        │   └── BudgetStatus.java       (enum: DRAFT/READY/SUBMITTED/APPROVED/REJECTED)
        ├── repository/                 Spring Data JPA interfaces
        │   ├── EventRepository.java
        │   ├── BudgetRepository.java
        │   └── OrganizerRepository.java
        ├── dto/                        Data Transfer Objects
        │   ├── CreateEventRequest.java
        │   ├── AddCategoryRequest.java
        │   ├── BudgetResponse.java
        │   ├── CategoryResponse.java
        │   └── AlertDTO.java
        ├── strategy/                   Strategy Pattern implementation
        │   ├── ApprovalStrategy.java       (interface)
        │   ├── ApprovalDecision.java
        │   ├── AutoApproveUnderThresholdStrategy.java   (@Primary)
        │   └── ManualApprovalStrategy.java
        ├── service/                    Business logic
        │   ├── BudgetService.java
        │   └── VarianceAlertService.java
        ├── exception/                  Error handling
        │   ├── InvalidBudgetException.java
        │   ├── ResourceNotFoundException.java
        │   └── GlobalExceptionHandler.java
        ├── controller/                 REST endpoints
        │   ├── BudgetController.java
        │   └── OrganizerController.java
        └── config/
            └── DataInitializer.java    Seeds demo organizer on startup
```

---

## Testing with Postman

A ready-to-import Postman collection is provided at [`postman/EFMS.postman_collection.json`](postman/EFMS.postman_collection.json).

### Import
1. Open Postman → **Import** → select the JSON file.
2. The collection **"EFMS - Event Financial Management System"** appears in the sidebar with 12 pre-configured requests.

### Run
Make sure the app is running (`mvn spring-boot:run`), then run the requests **in order**:

**Happy path (requests 1–8):** Lists organizers → creates event + budget → adds three categories → validates → submits → gets final state. Expected result: `AUTO_APPROVED` outcome.

**Variance path (requests 9–12):** Creates a second budget → adds an over-limit category → validates (expect ERROR) → submits (expect HTTP 400 blocked).

The collection uses Postman variables to automatically pass the `budgetId` from one request to the next — no manual copy-pasting required.

---

## License

Academic project — for educational use as part of the OOAD course.
