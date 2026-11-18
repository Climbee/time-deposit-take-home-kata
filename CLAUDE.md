# Rules for AI agents working on this repository

The Java solution lives in `java/`. Build and test with `./mvnw test` from that directory
(JDK 17+, Docker running for Testcontainers).

## Hard constraints (from the exercise README)

- Do not change the public API of `org.ikigaidigital.TimeDeposit`, and do not change the signature
  of `TimeDepositCalculator.updateBalance(List<TimeDeposit>)` or move either class.
- `updateBalance` behaviour must stay bit-for-bit identical. `TimeDepositCalculatorEquivalenceTest`
  guards this; never edit the copied original algorithm in that test.
- Exactly two API endpoints. Do not add more.
- Invalid-input and exception handling is out of scope.

## Way of working

1. Requirements → code analysis → tests → refactor → architecture review → dead-code removal → full test run.
2. Write or extend tests before changing behaviour-bearing code; run the relevant tests after every step.
3. One atomic commit per step, conventional commit prefix (`test:`, `refactor:`, `feat:`, `build:`, `docs:`).
4. Test behaviour and outcomes, not interactions: no mocking of our own classes; real PostgreSQL via
   Testcontainers for anything touching persistence or HTTP.
5. Boundaries are tested on both sides (30/31, 45/46, 365/366 days).
6. Keep dependencies pointing inwards: the domain (`org.ikigaidigital`, `org.ikigaidigital.interest`)
   imports no Spring or JPA; adapters depend on the application port, never the reverse.
7. No speculative abstractions. A new interface needs a second implementation or a test seam that
   is actually used.
8. State assumptions in code comments next to the code they affect, and list them in `java/README.md`.
9. The OpenAPI contract `java/src/main/resources/static/openapi.yaml` is the source of truth for the
   API; change it first, then implement the generated interface.
