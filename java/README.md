# Time Deposit Kata – Java solution

A Spring Boot service with exactly two endpoints, around the original `TimeDepositCalculator`.
Behaviour of `updateBalance` is unchanged and proven by tests; `TimeDeposit` is untouched.

## Requirements

- Java 17 or newer (built and tested with OpenJDK 17)
- Docker (PostgreSQL via Docker Compose for local runs, Testcontainers for tests)

Maven is not needed; use the wrapper.

## Run the tests

```bash
./mvnw test
```

23 tests: calculator characterisation tests, an equivalence test against a verbatim copy of the
original algorithm, and end-to-end API tests against PostgreSQL in Testcontainers.

## Run the service and trigger the endpoints

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

Spring Boot starts PostgreSQL from `compose.yaml`, Flyway creates the schema, and the `local`
profile loads six sample deposits (one on each side of every plan boundary).
If port 8080 is taken, append `-Dspring-boot.run.arguments=--server.port=8085`.

**Swagger UI:** http://localhost:8080/swagger-ui/index.html – it renders the contract
[`src/main/resources/static/openapi.yaml`](src/main/resources/static/openapi.yaml), the same file the
controller interface is generated from.

1. Open **GET /time-deposits** → *Try it out* → *Execute*: lists all deposits with their withdrawals.
2. Open **POST /time-deposits/balance-updates** → *Try it out* → *Execute*: returns `204` and credits
   one month of interest to every deposit.
3. Run the GET again to see the new balances.

The same with curl:

```bash
curl http://localhost:8080/time-deposits
curl -X POST http://localhost:8080/time-deposits/balance-updates
```

## Design

```
adapter.in.web           TimeDepositController (implements the generated TimeDepositsApi)
        │
application              TimeDepositService ──► TimeDepositRepository (port)
        │                                               ▲
domain  org.ikigaidigital TimeDeposit, TimeDepositCalculator, Withdrawal
        interest          InterestPolicy + Basic/Student/Premium policies
                                                        │
adapter.out.persistence  TimeDepositPersistenceAdapter (JPA, PostgreSQL, Flyway)
```

- **Hexagonal:** dependencies point inwards. The domain has no Spring or JPA imports; the
  application owns the repository port; adapters implement or call it.
- **Strategy for interest:** each plan is an `InterestPolicy`. A new plan or rule is a new class
  passed to `TimeDepositCalculator(List<InterestPolicy>)`; the calculator does not change.
- **Contract first:** the OpenAPI file is the source of truth; `openapi-generator` creates the
  interface and DTOs at build time (`target/generated-sources`).

## Assumptions

- `TimeDeposit` and `TimeDepositCalculator` stay in package `org.ikigaidigital` with their public
  API intact – moving them would be a breaking change for other users of the "shared" class.
- The README's `timeDeposits.planType` etc. are logical names; the tables are `time_deposits`,
  `plan_type`, `time_deposit_id` because PostgreSQL folds unquoted camelCase to lower case.
- Rounding and `double` arithmetic of the calculator are kept exactly, including the quirk that
  `new BigDecimal(double)` rounds the binary value (see `roundsTheBinaryDoubleValueNotTheDecimalOne`).
  Balances are stored as `NUMERIC(19,2)`.
- An unknown plan type earns no interest (original behaviour). Plan types are case-sensitive.
- `POST /time-deposits/balance-updates` is a "run the monthly job" command: not idempotent, so `POST`
  and `204 No Content`. All deposits are updated in one transaction.
- `days` is not advanced by the update; the original calculator does not do it and the README
  does not ask for it.
- Withdrawals are returned but do not affect interest; nothing in the requirements links them.

## AI-assisted development

| | |
| --- | --- |
| Tool | [Claude Code](https://claude.com/claude-code) (desktop app, Code tab), model Claude Opus 5.5 |
| Harness | Agent with shell, file and browser tools working in this repository; rules in [`../CLAUDE.md`](../CLAUDE.md) |
| Workflow | Requirements → code analysis → characterisation tests → refactor → endpoints → coverage → review, one atomic commit per step |
| Verification | Mutation spot-check of the tests, JaCoCo coverage run, manual run of both endpoints through Swagger UI |

**What was AI-assisted:** the analysis of the original code, the tests, the refactoring, the
Spring Boot adapters, the OpenAPI contract and this documentation were drafted by the agent and
reviewed line by line by me. Why: the exercise asks for it, and an agent that runs the test suite
after every change is a good fit for a refactoring where the main risk is an unnoticed behaviour
change.

**Reproduce:** open the repository in Claude Code (`claude` in the repository root); it reads
`CLAUDE.md` automatically. Prompt used: *"Read and follow the instructions, clone the repo, use
Java, test it and let me review when you're done."*, together with the exercise email.
