# Mythical Pet System: Detailed Technical Report

## 1. Project Purpose

`Mythical Pet System` is a backend microservice project for managing adoption of fictional magical pets.

The project is split into two independent Spring Boot services:

- `pet-adoption-service` is the main business service. It handles authentication, authorization, pet catalog operations, adoption requests, and persistence in PostgreSQL.
- `matching-service` is an internal stateless service that calculates a compatibility score between a user and a pet.

The system is designed around a simple but realistic backend pattern:

1. external clients interact only with the main service,
2. the main service stores business state,
3. the main service delegates one isolated business capability to another service over HTTP,
4. Docker Compose runs the full local stack.

## 2. Repository Structure

Top-level repository layout:

- `docker-compose.yml` - orchestration for PostgreSQL and both services.
- `pet-adoption-service/` - main service with database, security, REST API, and business logic.
- `matching-service/` - internal scoring service.
- `PROJECT_REPORT.md` - this report.
- `out/`, `.idea/`, `target/` - IDE/build artifacts, not core business code.

Logical architecture:

```text
Client
  |
  v
pet-adoption-service
  | \
  |  \-- PostgreSQL
  |
  \----HTTP/Feign----> matching-service
```

## 3. High-Level Architecture

### 3.1 Architecture style

This is a small microservice system with one primary service and one supporting internal service.

Characteristics:

- coarse-grained service split,
- synchronous HTTP communication,
- centralized persistence in only one service,
- stateless authentication with JWT,
- role-based authorization,
- simple domain model,
- minimal infrastructure overhead.

### 3.2 Why the split exists

The split is not for scale yet; it is mostly for separation of concerns:

- `pet-adoption-service` owns the adoption domain and data.
- `matching-service` owns scoring logic only.

This means the scoring logic can evolve independently without directly coupling calculation code to the main service layer.

### 3.3 Practical implication of the current split

The microservice boundary is real, but lightweight:

- no service discovery,
- no message broker,
- no API gateway,
- no distributed tracing,
- no resilience layer like retry/circuit breaker.

So architecturally this is a microservice system, but still at an early/simple stage.

## 4. Runtime and Deployment Model

The runtime topology is defined in [docker-compose.yml](/Users/fariza/Desktop/backend/mythical-pet-system/docker-compose.yml:1).

Services:

- `postgres` on container port `5432`, exposed as local port `5440`.
- `matching-service` on port `8081`.
- `pet-adoption-service` on port `8080`.

Key runtime dependencies:

- `pet-adoption-service` waits for PostgreSQL health and for `matching-service` startup.
- `pet-adoption-service` connects to PostgreSQL through container hostname `postgres`.
- `pet-adoption-service` calls `matching-service` through container hostname `matching-service`.

Important detail:

- Docker Compose does not explicitly pass `MATCHING_SERVICE_INTERNAL_TOKEN` to either service.
- Both services rely on the same default token value `dev-internal-token`.
- This works locally only because defaults match.

## 5. Technology Stack

### 5.1 Main stack

- Java 17
- Spring Boot 4.0.5
- Spring MVC
- Spring Data JPA
- Spring Security
- Spring Cloud OpenFeign
- Hibernate ORM
- PostgreSQL
- Maven

### 5.2 Security stack

- JWT via `jjwt`
- `BCryptPasswordEncoder`
- Spring Security filter chain
- role-based endpoint protection
- internal header token for service-to-service communication

### 5.3 Testing stack

- JUnit 5
- Spring Boot Test
- Mockito
- Spring Security Test

## 6. Services Overview

## 6.1 `pet-adoption-service`

Main application entry point: [PetAdoptionServiceApplication.java](/Users/fariza/Desktop/backend/mythical-pet-system/pet-adoption-service/src/main/java/com/example/petadoptionservice/PetAdoptionServiceApplication.java:1)

The service enables Feign clients via `@EnableFeignClients`, which is necessary for calling `matching-service`.

Core responsibilities:

- register and authenticate users,
- issue JWT tokens,
- expose public pet catalog,
- allow admins to add pets,
- create and manage adoption requests,
- calculate compatibility scores through the internal matching service,
- persist users, pets, and adoption requests in PostgreSQL.

### Internal package structure

- `config` - Spring Security configuration.
- `controller` - REST endpoints.
- `dto` - request/response contracts.
- `entity` - JPA models and enums.
- `exception` - custom exceptions and global handler.
- `feign` - HTTP client interface to `matching-service`.
- `repository` - Spring Data repositories.
- `security` - JWT and user loading logic.
- `service` - business logic.

## 6.2 `matching-service`

Main application entry point: [MatchingServiceApplication.java](/Users/fariza/Desktop/backend/mythical-pet-system/matching-service/src/main/java/com/example/matchingservice/MatchingServiceApplication.java:1)

Core responsibilities:

- receive compatibility input,
- calculate a score from 0 to 100,
- return a textual explanation,
- reject requests that do not include the expected internal token.

This service is stateless:

- no database,
- no user model,
- no authentication for end-users,
- only one business capability.

### Internal package structure

- `config` - interceptor and MVC config for internal token validation.
- `controller` - endpoint exposure.
- `dto` - request/response payloads.
- `service` - scoring algorithm.

## 7. Domain Model

The persistent domain exists only in `pet-adoption-service`.

## 7.1 `User`

Source: [User.java](/Users/fariza/Desktop/backend/mythical-pet-system/pet-adoption-service/src/main/java/com/example/petadoptionservice/entity/User.java:1)

Fields:

- `id`
- `username`
- `email`
- `password`
- `role`
- `magicTolerance`
- `homeType`

Meaning:

- `email` is the unique login identity.
- `password` is stored hashed.
- `role` determines authorization rules.
- `magicTolerance` and `homeType` are business attributes used during compatibility calculation.

Enums used:

- `Role`: `USER`, `ADMIN`
- `HomeType`: `HOUSE`, `APARTMENT`, `FARM`

## 7.2 `Pet`

Source: [Pet.java](/Users/fariza/Desktop/backend/mythical-pet-system/pet-adoption-service/src/main/java/com/example/petadoptionservice/entity/Pet.java:1)

Fields:

- `id`
- `name`
- `species`
- `dangerLevel`
- `temperament`
- `magicLevel`
- `adoptionStatus`
- `description`

Meaning:

- `dangerLevel` and `magicLevel` are used by the matching algorithm.
- `temperament` also influences matching.
- `adoptionStatus` is reused as both pet availability state and adoption result marker.

Important modeling detail:

- The same enum `AdoptionStatus` is used both for `AdoptionRequest.status` and `Pet.adoptionStatus`.
- For pets, `APPROVED` effectively means “already adopted”.
- This works technically, but semantically the same enum is serving two different concepts.

## 7.3 `AdoptionRequest`

Source: [AdoptionRequest.java](/Users/fariza/Desktop/backend/mythical-pet-system/pet-adoption-service/src/main/java/com/example/petadoptionservice/entity/AdoptionRequest.java:1)

Fields:

- `id`
- `user`
- `pet`
- `status`
- `compatibilityScore`
- `createdAt`

Relationships:

- many adoption requests can belong to one user,
- many adoption requests can belong to one pet.

Meaning:

- it stores the user’s application to adopt a specific pet,
- it captures the score at creation time,
- it also stores review/moderation outcome through `status`.

## 8. Database and Persistence

Persistence is implemented through Spring Data JPA repositories:

- [UserRepository.java](/Users/fariza/Desktop/backend/mythical-pet-system/pet-adoption-service/src/main/java/com/example/petadoptionservice/repository/UserRepository.java:1)
- [PetRepository.java](/Users/fariza/Desktop/backend/mythical-pet-system/pet-adoption-service/src/main/java/com/example/petadoptionservice/repository/PetRepository.java:1)
- [AdoptionRequestRepository.java](/Users/fariza/Desktop/backend/mythical-pet-system/pet-adoption-service/src/main/java/com/example/petadoptionservice/repository/AdoptionRequestRepository.java:1)

Notable repository methods:

- `findByEmail`
- `existsByEmail`
- `findAllByUser_Email`
- `findAllByPetIdAndIdNot`
- `existsByUser_EmailAndPetId`

### Schema strategy

Configured in [application.properties](/Users/fariza/Desktop/backend/mythical-pet-system/pet-adoption-service/src/main/resources/application.properties:1):

- `spring.jpa.hibernate.ddl-auto=update`
- `spring.sql.init.mode=always`
- `spring.jpa.defer-datasource-initialization=true`

Implications:

- schema evolves automatically from entities,
- startup SQL is always executed,
- seed data is inserted after schema initialization.

### Seed data

[data.sql](/Users/fariza/Desktop/backend/mythical-pet-system/pet-adoption-service/src/main/resources/data.sql:1) inserts three pets if they do not already exist:

- `Nimbus`
- `Emberclaw`
- `Moonwhisker`

This makes the catalog usable immediately after first startup.

## 9. Security Architecture

Security is implemented only in the main service.

## 9.1 Endpoint authorization rules

Defined in [SecurityConfig.java](/Users/fariza/Desktop/backend/mythical-pet-system/pet-adoption-service/src/main/java/com/example/petadoptionservice/config/SecurityConfig.java:39).

Public:

- `POST /auth/register`
- `POST /auth/login`
- `GET /api/pets`
- `GET /api/pets/{id}`

Authenticated user:

- `POST /api/adoptions`
- `GET /api/adoptions`

Admin only:

- `POST /api/pets`
- `PATCH /api/adoptions/{id}/status`

### Security model

Core decisions:

- CSRF disabled because API is token-based.
- Sessions disabled via `SessionCreationPolicy.STATELESS`.
- custom JSON responses for `401` and `403`.
- JWT filter inserted before `UsernamePasswordAuthenticationFilter`.

## 9.2 JWT flow

JWT generation and parsing are implemented in [JwtService.java](/Users/fariza/Desktop/backend/mythical-pet-system/pet-adoption-service/src/main/java/com/example/petadoptionservice/security/JwtService.java:19).

Token contents:

- subject = user email,
- claim `role` = first authority string,
- issued-at timestamp,
- expiration timestamp.

Request authentication is performed in [JwtAuthFilter.java](/Users/fariza/Desktop/backend/mythical-pet-system/pet-adoption-service/src/main/java/com/example/petadoptionservice/security/JwtAuthFilter.java:18):

1. read `Authorization` header,
2. require `Bearer <token>`,
3. extract username from token,
4. load user from database,
5. validate token against user details,
6. put authenticated principal into `SecurityContext`.

## 9.3 User loading

[CustomUserDetailsService.java](/Users/fariza/Desktop/backend/mythical-pet-system/pet-adoption-service/src/main/java/com/example/petadoptionservice/security/CustomUserDetailsService.java:1) loads users by email and maps roles into Spring Security authorities in the form `ROLE_ADMIN` or `ROLE_USER`.

## 9.4 Internal service protection

The matching service is protected by a custom header token:

- header name: `X-Internal-Token`

Validation is implemented in [InternalTokenInterceptor.java](/Users/fariza/Desktop/backend/mythical-pet-system/matching-service/src/main/java/com/example/matchingservice/config/InternalTokenInterceptor.java:11) and registered in [WebConfig.java](/Users/fariza/Desktop/backend/mythical-pet-system/matching-service/src/main/java/com/example/matchingservice/config/WebConfig.java:7).

This is not end-user authentication. It is only service-to-service request filtering.

## 10. API Structure

## 10.1 Authentication API

Controller: [AuthController.java](/Users/fariza/Desktop/backend/mythical-pet-system/pet-adoption-service/src/main/java/com/example/petadoptionservice/controller/AuthController.java:1)

Endpoints:

- `POST /auth/register`
- `POST /auth/login`

### Register flow

Implemented in [AuthService.java](/Users/fariza/Desktop/backend/mythical-pet-system/pet-adoption-service/src/main/java/com/example/petadoptionservice/service/AuthService.java:36)

Logic:

1. reject duplicate email,
2. create `User`,
3. hash password with BCrypt,
4. force role to `USER`,
5. save user,
6. generate JWT immediately,
7. return token.

Important detail:

- role cannot be chosen from the public request DTO.
- every registered user becomes `USER`.

### Login flow

Implemented in [AuthService.java](/Users/fariza/Desktop/backend/mythical-pet-system/pet-adoption-service/src/main/java/com/example/petadoptionservice/service/AuthService.java:63)

Logic:

1. authenticate email/password via `AuthenticationManager`,
2. read full user record from database,
3. build `UserDetails`,
4. generate new JWT,
5. return token.

## 10.2 Pet API

Controller: [PetController.java](/Users/fariza/Desktop/backend/mythical-pet-system/pet-adoption-service/src/main/java/com/example/petadoptionservice/controller/PetController.java:1)

Endpoints:

- `GET /api/pets`
- `GET /api/pets/{id}`
- `POST /api/pets`

Business logic lives in [PetService.java](/Users/fariza/Desktop/backend/mythical-pet-system/pet-adoption-service/src/main/java/com/example/petadoptionservice/service/PetService.java:13).

Behavior:

- all pets are returned without pagination or filtering,
- fetching by unknown id throws `ResourceNotFoundException`,
- creating a pet always sets `adoptionStatus = PENDING`.

Current limitation:

- adopted pets are still returned in the main catalog because `getAllPets()` does not filter by availability.

## 10.3 Adoption API

Controller: [AdoptionController.java](/Users/fariza/Desktop/backend/mythical-pet-system/pet-adoption-service/src/main/java/com/example/petadoptionservice/controller/AdoptionController.java:1)

Endpoints:

- `POST /api/adoptions`
- `GET /api/adoptions`
- `PATCH /api/adoptions/{id}/status`

Business logic lives in [AdoptionService.java](/Users/fariza/Desktop/backend/mythical-pet-system/pet-adoption-service/src/main/java/com/example/petadoptionservice/service/AdoptionService.java:28).

### Create adoption request

Method: `createAdoptionRequest(...)`

Logic:

1. identify current user by email from authenticated principal,
2. load user,
3. load target pet,
4. reject if pet status is already `APPROVED`,
5. reject duplicate request for same user and same pet,
6. call matching service for score,
7. create `AdoptionRequest` with status `PENDING`,
8. set `createdAt`,
9. save and return DTO.

### List adoption requests

Method: `getAllAdoptions(...)`

Role-dependent behavior:

- `ADMIN` sees all adoption requests,
- `USER` sees only their own requests.

### Update adoption status

Method: `updateAdoptionStatus(...)`

Logic:

1. load current user,
2. verify role is `ADMIN`,
3. load adoption request by id,
4. update request status from request body,
5. if status becomes `APPROVED`:
   - mark pet as `APPROVED`,
   - reject all other pending requests for same pet,
6. if status becomes `REJECTED`:
   - check whether another approved request still exists,
   - if not, set pet status back to `PENDING`.

This is the core moderation workflow in the system.

## 11. Matching Service Logic

Controller: [MatchingController.java](/Users/fariza/Desktop/backend/mythical-pet-system/matching-service/src/main/java/com/example/matchingservice/controller/MatchingController.java:13)

Business logic: [MatchingService.java](/Users/fariza/Desktop/backend/mythical-pet-system/matching-service/src/main/java/com/example/matchingservice/service/MatchingService.java:7)

### Input model

Input fields:

- `userMagicTolerance`
- `userHomeType`
- `petDangerLevel`
- `petTemperament`
- `petMagicLevel`
- `petSpecies`

### Score calculation

Algorithm starts from base score `50`.

Adjustments:

1. compare `userMagicTolerance - petDangerLevel`
   - `>= 2` => `+25`
   - `>= 0` => `+15`
   - `== -1` => `+5`
   - otherwise => `-20`
2. if pet temperament is calm and user home type is apartment => `+15`
3. compare pet magic level to user tolerance
   - if pet magic level is more than tolerance by `> 2` => `-20`
   - if pet magic level is only slightly higher => `-10`
   - otherwise => `+10`
4. clamp final score into `0..100`

### Explanation generation

The service also returns a user-friendly text explanation:

- `>= 80` => strong match,
- `>= 60` => good match,
- otherwise => low match because of danger or magic mismatch.

### Architectural meaning

This algorithm is deterministic, synchronous, and isolated.

That makes it:

- easy to test,
- easy to replace,
- easy to expand with more rules later.

## 12. Inter-Service Communication

Client interface: [MatchingClient.java](/Users/fariza/Desktop/backend/mythical-pet-system/pet-adoption-service/src/main/java/com/example/petadoptionservice/feign/MatchingClient.java:11)

Protocol:

- `pet-adoption-service` sends `POST /api/match-score`
- request body contains normalized user/pet attributes
- header `X-Internal-Token` must match expected token
- response contains `compatibilityScore` and `explanation`

### Mapping path

The main service converts internal domain data to matching DTO in `fetchCompatibilityScore(...)` inside [AdoptionService.java](/Users/fariza/Desktop/backend/mythical-pet-system/pet-adoption-service/src/main/java/com/example/petadoptionservice/service/AdoptionService.java:146).

Field mapping:

- `User.magicTolerance` -> `userMagicTolerance`
- `User.homeType.name()` -> `userHomeType`
- `Pet.dangerLevel` -> `petDangerLevel`
- `Pet.temperament` -> `petTemperament`
- `Pet.magicLevel` -> `petMagicLevel`
- `Pet.species` -> `petSpecies`

### Failure handling

If the matching service:

- returns null,
- returns score without numeric value,
- throws Feign/network error,
- throws unexpected runtime exception,

the main service converts that into `MatchingServiceException`.

That exception is then mapped to HTTP `503 Service Unavailable` by [GlobalExceptionHandler.java](/Users/fariza/Desktop/backend/mythical-pet-system/pet-adoption-service/src/main/java/com/example/petadoptionservice/exception/GlobalExceptionHandler.java:25).

## 13. DTO and Validation Strategy

The project consistently separates transport models from entities.

Examples:

- `RegisterRequest`, `LoginRequest`
- `PetCreateRequest`, `PetResponse`
- `AdoptionRequestCreateDto`, `AdoptionResponseDto`
- `MatchRequestDto`, `MatchResponseDto`

Validation approach:

- `@NotBlank`, `@NotNull`
- `@Min`, `@Max`
- `@Email`
- `@Valid` on controller methods

Important detail:

- some auth/adoption DTOs are annotated with `@JsonIgnoreProperties(ignoreUnknown = false)`, which makes unknown request fields fail fast.
- this is used to prevent silent acceptance of unexpected JSON fields in sensitive flows.

## 14. Exception Handling

Central handler: [GlobalExceptionHandler.java](/Users/fariza/Desktop/backend/mythical-pet-system/pet-adoption-service/src/main/java/com/example/petadoptionservice/exception/GlobalExceptionHandler.java:17)

Handled categories:

- `404` for resource not found,
- `503` for matching service failures,
- `401` for invalid login credentials,
- `403` for authorization failures,
- `400` for validation/parsing/business input errors.

Design decision:

- responses are normalized to simple JSON `{ "error": "..." }`.

Special cases:

- invalid `homeType` returns a friendly explicit message,
- unknown `role` field returns `Unknown field: role`.

This makes the API more predictable for frontend clients and future integrations.

## 15. End-to-End Business Flows

## 15.1 User registration

1. Client sends `POST /auth/register`.
2. Main service validates request body.
3. Email uniqueness is checked.
4. Password is hashed.
5. User is saved with role `USER`.
6. JWT is generated and returned.

## 15.2 User login

1. Client sends `POST /auth/login`.
2. Spring Security authenticates credentials.
3. Full user record is loaded.
4. JWT is generated and returned.

## 15.3 Public catalog browsing

1. Client sends `GET /api/pets`.
2. No token required.
3. Main service returns all pets as DTOs.

## 15.4 Adoption request creation

1. Client sends `POST /api/adoptions` with JWT and `petId`.
2. JWT filter authenticates request.
3. Main service loads user and pet.
4. Business checks verify pet is not already adopted and request is not duplicated.
5. Main service calls matching service.
6. Matching service validates internal token.
7. Matching service computes score and explanation.
8. Main service stores score in adoption request.
9. Main service returns created adoption DTO.

## 15.5 Admin moderation

1. Admin sends `PATCH /api/adoptions/{id}/status`.
2. JWT identifies admin.
3. Service verifies admin role.
4. Target request is updated.
5. If approved:
   - pet becomes adopted,
   - competing pending requests are auto-rejected.
6. If rejected and no approved request remains:
   - pet becomes available again.

## 16. Current Architectural Strengths

### Clear service responsibility split

The project cleanly separates:

- business/data ownership,
- compatibility calculation,
- security concerns,
- API contracts.

### Layered code organization

The main service follows a standard and understandable structure:

- controller -> service -> repository -> database

That makes it easy for another developer or another AI agent to navigate.

### Good baseline API hygiene

The code already includes:

- DTOs instead of exposing entities directly,
- validation annotations,
- centralized exception handling,
- explicit role restrictions,
- password hashing,
- JWT-based stateless auth.

### Contained scoring engine

The matching rules are already isolated in one place. This is a good foundation for later enhancement.

## 17. Current Limitations and Architectural Risks

This section is especially important if the report will be used to plan next work.

### 17.1 Pet status is semantically overloaded

The enum `AdoptionStatus` is used both for:

- adoption request lifecycle,
- pet availability lifecycle.

This can cause confusion because `Pet.APPROVED` really means “adopted”, not “approved by moderator” in the same sense as a request.

Better long-term model:

- keep `AdoptionRequestStatus`,
- introduce separate `PetAvailabilityStatus`.

### 17.2 No pagination/filtering/search

`GET /api/pets` and `GET /api/adoptions` return full lists.

This is fine for demo scale, but not for production or larger datasets.

### 17.3 No explicit transaction locking or concurrency protection

`updateAdoptionStatus(...)` is transactional, but there is no optimistic/pessimistic locking.

Potential race condition:

- two admins could approve competing requests for the same pet nearly simultaneously.

The current code reduces inconsistency risk but does not fully harden against concurrent write conflicts.

### 17.4 Matching service availability is a hard dependency

Creating an adoption request depends on real-time response from `matching-service`.

If it is down:

- request creation fails,
- adoption cannot proceed.

There is no fallback score, retry policy, timeout strategy, or queue.

### 17.5 Main service still exposes adopted pets in the public list

Current pet catalog endpoint returns all pets, not only adoptable ones.

This may be fine as a complete catalog, but usually adoption systems need:

- available only,
- all pets,
- filter by status.

### 17.6 Role bootstrapping is incomplete

Public registration only creates `USER`.

There is no explicit admin bootstrap flow in the codebase.

Practical consequence:

- admin users likely need to be inserted manually in the database.

### 17.7 No API versioning

All endpoints are unversioned.

That is acceptable early on, but versioning becomes useful once clients multiply.

### 17.8 Security is good for local/internal setup, but still basic

Internal token protection on `matching-service` is simple and useful, but:

- it is static shared-secret style authentication,
- there is no mTLS,
- there is no gateway,
- there is no rotation mechanism,
- the default token is development-friendly rather than strongly managed.

### 17.9 `ddl-auto=update` is convenient but weak for long-term schema control

This is acceptable for development, but migrations should eventually move to Flyway or Liquibase.

## 18. Testing State

Test files exist in both services.

### `pet-adoption-service` tests

- `PetAdoptionServiceApplicationTests`
- `GlobalExceptionHandlerTest`
- `AdoptionServiceTest`

### `matching-service` tests

- `MatchingServiceApplicationTests`
- `InternalTokenInterceptorTest`

### What is covered

There is at least some coverage for:

- exception formatting,
- adoption visibility by role,
- admin-only moderation rule,
- auto-rejection of competing requests,
- internal token interceptor behavior,
- basic Spring context startup.

### Actual current test execution result

`./mvnw test` was executed in both services during analysis.

Observed status:

- `pet-adoption-service` build failed.
- `matching-service` build failed.

Root causes:

1. `pet-adoption-service` context test tries to connect to PostgreSQL and fails because local test DB is not available.
2. Mockito-based tests in both services fail because the environment cannot initialize the inline Byte Buddy mock maker.

This means the repository includes tests, but the test suite is not currently portable/reliable in the present environment without extra configuration.

### Practical interpretation

The tests show intended behavior, but they should not be treated as a stable CI-quality safety net yet.

## 19. Configuration Inventory

## 19.1 `pet-adoption-service` configuration

Source: [application.properties](/Users/fariza/Desktop/backend/mythical-pet-system/pet-adoption-service/src/main/resources/application.properties:1)

Important properties:

- datasource URL, username, password
- JPA schema mode
- SQL init behavior
- JWT secret
- JWT expiration
- server port
- matching service URL
- matching internal token

Notable detail:

- default local DB port in properties is `5435`,
- Docker Compose exposes PostgreSQL on local port `5440`.

So running outside Docker may require manual alignment of DB configuration.

## 19.2 `matching-service` configuration

Source: [application.properties](/Users/fariza/Desktop/backend/mythical-pet-system/matching-service/src/main/resources/application.properties:1)

Important properties:

- server port
- internal token

## 20. Package-by-Package Reading Guide

If another engineer or AI agent needs to continue work efficiently, this is the fastest reading order.

### Start here for system overview

1. [docker-compose.yml](/Users/fariza/Desktop/backend/mythical-pet-system/docker-compose.yml:1)
2. [pet-adoption-service/src/main/resources/application.properties](/Users/fariza/Desktop/backend/mythical-pet-system/pet-adoption-service/src/main/resources/application.properties:1)
3. [matching-service/src/main/resources/application.properties](/Users/fariza/Desktop/backend/mythical-pet-system/matching-service/src/main/resources/application.properties:1)

### Then read main business flow

1. [AdoptionController.java](/Users/fariza/Desktop/backend/mythical-pet-system/pet-adoption-service/src/main/java/com/example/petadoptionservice/controller/AdoptionController.java:1)
2. [AdoptionService.java](/Users/fariza/Desktop/backend/mythical-pet-system/pet-adoption-service/src/main/java/com/example/petadoptionservice/service/AdoptionService.java:28)
3. [MatchingClient.java](/Users/fariza/Desktop/backend/mythical-pet-system/pet-adoption-service/src/main/java/com/example/petadoptionservice/feign/MatchingClient.java:11)
4. [MatchingController.java](/Users/fariza/Desktop/backend/mythical-pet-system/matching-service/src/main/java/com/example/matchingservice/controller/MatchingController.java:13)
5. [MatchingService.java](/Users/fariza/Desktop/backend/mythical-pet-system/matching-service/src/main/java/com/example/matchingservice/service/MatchingService.java:7)

### Then read security

1. [SecurityConfig.java](/Users/fariza/Desktop/backend/mythical-pet-system/pet-adoption-service/src/main/java/com/example/petadoptionservice/config/SecurityConfig.java:39)
2. [JwtAuthFilter.java](/Users/fariza/Desktop/backend/mythical-pet-system/pet-adoption-service/src/main/java/com/example/petadoptionservice/security/JwtAuthFilter.java:18)
3. [JwtService.java](/Users/fariza/Desktop/backend/mythical-pet-system/pet-adoption-service/src/main/java/com/example/petadoptionservice/security/JwtService.java:19)
4. [CustomUserDetailsService.java](/Users/fariza/Desktop/backend/mythical-pet-system/pet-adoption-service/src/main/java/com/example/petadoptionservice/security/CustomUserDetailsService.java:1)
5. [InternalTokenInterceptor.java](/Users/fariza/Desktop/backend/mythical-pet-system/matching-service/src/main/java/com/example/matchingservice/config/InternalTokenInterceptor.java:11)

### Then read models and contracts

1. `entity/`
2. `dto/`
3. `repository/`
4. [GlobalExceptionHandler.java](/Users/fariza/Desktop/backend/mythical-pet-system/pet-adoption-service/src/main/java/com/example/petadoptionservice/exception/GlobalExceptionHandler.java:17)

## 21. Recommended Next Expansion Directions

These are the most natural next improvements based on the current architecture.

### High-impact backend improvements

- Add pagination and filtering for pets and adoption requests.
- Split pet status from adoption request status.
- Add dedicated admin bootstrap/management flow.
- Hide adopted pets from default public catalog or add status filters.
- Add database migrations with Flyway/Liquibase.
- Add optimistic locking on pet/adoption entities.
- Add proper integration tests with Testcontainers.
- Fix Mockito/test runtime portability.
- Add OpenAPI/Swagger documentation.
- Add request logging and correlation IDs.

### Matching-service evolution ideas

- support weighted species-specific rules,
- configurable scoring rules from properties or database,
- explanation with structured factors instead of one string,
- audit of why a score was assigned,
- batch scoring endpoint,
- async scoring if logic becomes expensive.

### Product-level feature ideas

- user profile update,
- admin pet update/delete,
- pet images/media,
- application comments from admins,
- notification/email flow,
- favorites/wishlist,
- recommendation endpoint,
- approval history/audit log,
- shelter/owner entities,
- multi-step adoption process.

## 22. Final Assessment

This project is already a solid foundation for further backend work.

It is not just a CRUD app:

- it has authentication,
- role separation,
- domain rules,
- inter-service communication,
- error handling,
- seed data,
- deployment setup.

At the same time, it is still small enough to extend safely.

The most important architectural understanding is this:

- `pet-adoption-service` is the system of record and workflow owner,
- `matching-service` is a pure calculation dependency,
- PostgreSQL stores all durable business state,
- JWT secures external access,
- a custom internal token secures internal scoring access,
- adoption moderation is the core domain workflow.

If future work is planned, the strongest next focus areas are:

- data/model cleanup,
- stronger testing,
- catalog/query improvements,
- admin workflows,
- resilience and concurrency hardening.
