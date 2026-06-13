# Backend AI Handoff

This document is for the next AI agent/developer continuing the backend work.

Current project:

- Repo root: `D:\UIT\NhapMon_UngDungDiDong\SE114-HeThongDatSanBong`
- Backend root: `D:\UIT\NhapMon_UngDungDiDong\SE114-HeThongDatSanBong\backend`
- Main backend package: `com.example.backend`
- Java/Spring: Java 21, Spring Boot 3.5.0
- Database: PostgreSQL on Supabase, managed by Flyway migrations
- Local cache/temp service: Redis via Docker Compose
- Current date when this file was created: 2026-06-13

Reference project:

- Local reference path: `D:\UIT\Ngon Ngu Java\SE303-final-project\backend`
- Reference repo link: `TODO: paste reference GitHub link here`
- Current project repo link: `TODO: paste current GitHub link here`

Important: the reference project is only a guide. Do not copy files directly without adapting them. The reference project uses many `String`/UUID ids and Supabase Auth style flows. This current project has already standardized entity ids as `Long` to match the frontend.

## Current Working State

At the time this handoff was written:

- Current branch: `feat/backend-team-member`
- The `feat/backend-team-member` work has been implemented but may still need final manual API smoke testing and commit.
- `.\mvnw.cmd test` passed after implementing team member.
- `git status` showed uncommitted changes for the team member feature.

Before starting a new feature, run:

```powershell
cd D:\UIT\NhapMon_UngDungDiDong\SE114-HeThongDatSanBong
git status
cd backend
.\mvnw.cmd test
```

If team member is still uncommitted and tests pass, commit it:

```powershell
git add backend/src/main/java/com/example/backend/config/SecurityConfig.java `
  backend/src/main/java/com/example/backend/controller/TeamController.java `
  backend/src/main/java/com/example/backend/dto/request/TeamInvitationDecisionRequest.java `
  backend/src/main/java/com/example/backend/dto/request/TeamInviteRequest.java `
  backend/src/main/java/com/example/backend/dto/response/TeamMemberResponse.java `
  backend/src/main/java/com/example/backend/dto/response/TeamResponse.java `
  backend/src/main/java/com/example/backend/entity/TeamMember.java `
  backend/src/main/java/com/example/backend/repository/TeamMemberRepository.java `
  backend/src/main/java/com/example/backend/service/TeamService.java `
  backend/src/main/java/com/example/backend/service/impl/TeamServiceImpl.java `
  backend/src/main/java/com/example/backend/utils/Enums.java `
  backend/src/main/resources/db/migration/V4__add_team_members.sql

git commit -m "feat(team): add team member invitations"
git push origin feat/backend-team-member
```

## What Has Been Done

The backend has already been built through these major features:

- Base backend setup: Spring Boot, PostgreSQL, Flyway, environment config.
- IDs standardized to `Long` for frontend compatibility.
- Supabase PostgreSQL connection support through `.env`.
- Redis infrastructure:
  - `spring-boot-starter-data-redis`
  - local Redis in `backend/docker-compose.yml`
  - booking Redis lock in `BookingLockService`
- Auth/user:
  - register/login with JWT
  - password hashing with BCrypt
  - OTP registration/password reset using Redis
  - mail dependency added
  - secrets moved to `.env`
- Field/time slot:
  - CRUD fields
  - CRUD time slots
  - availability by date
- Booking:
  - create/cancel/confirm/complete booking
  - pending booking cleanup scheduler
  - Redis lock around create booking
- Payment:
  - Stripe checkout session
  - Stripe webhook handling
  - payment records
  - booking status update to `DEPOSIT_PAID`
- Team:
  - basic team CRUD
  - current in-progress feature adds team member invitation flow
- Matchmaking:
  - match post
  - match request
  - accept/reject request
  - creates match conversation after accepted request
- Review:
  - review linked to accepted match request
  - duplicate review prevention
  - trust score update/admin review logic
- Notification:
  - create/list/read/delete notifications
- Chat:
  - REST conversation/message flow
  - no WebSocket yet
- Admin dashboard:
  - overview and admin list endpoints
- Maintenance seed:
  - scheduler cleanup
  - database seeder, currently protected by `app.seed.enabled=false`

## Config And Secret Policy

`application.properties` should not contain secrets.

Secrets/credentials must stay in `backend/.env`:

- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `JWT_SECRET`
- `STRIPE_SECRET_KEY`
- `STRIPE_WEBHOOK_SECRET`
- `SPRING_REDIS_PASSWORD` if Redis is remote/protected
- `SPRING_MAIL_USERNAME`
- `SPRING_MAIL_PASSWORD`
- `APP_SEED_*_EMAIL`
- `APP_SEED_*_PASSWORD`

Non-secret config can stay directly in `application.properties`:

- boolean flags
- TTLs
- local ports
- local Redis host/port
- cleanup delays
- feature prefixes such as `REG_OTP_`, `OTP_`, `lock:booking:slot:`

Current local Redis flow:

```powershell
cd D:\UIT\NhapMon_UngDungDiDong\SE114-HeThongDatSanBong\backend
docker compose up -d redis
```

PostgreSQL local is optional and should not be started by default because the current database is Supabase. If local PostgreSQL is needed:

```powershell
docker compose --profile local-db up -d postgres
```

## Technical Conventions

Follow these conventions when continuing:

- Use `Long` ids in entities, DTOs, repositories, services, and controllers.
- Do not introduce UUID/String ids from the reference project.
- Prefer the existing manual mapper style. Do not add MapStruct unless there is a strong reason.
- Keep packages that already exist:
  - `controller`
  - `dto.request`
  - `dto.response`
  - `entity`
  - `repository`
  - `service`
  - `service.impl`
  - `utils`
- Avoid creating extra packages unless needed by a specific feature.
- Add Flyway migrations for schema changes. Do not rely on Hibernate `ddl-auto` to mutate schema.
- Keep `spring.jpa.hibernate.ddl-auto=validate`.
- Run `.\mvnw.cmd test` after each feature.
- If a feature has runtime dependencies, provide a PowerShell smoke test script.

## Current Team Member Feature

Implemented behavior:

- New table `team_members`.
- New enum `TeamMemberStatus`: `PENDING`, `ACCEPTED`, `REJECTED`.
- New notification type `TEAM_INVITE`.
- Creating a team adds the captain as an accepted team member.
- Captain can invite a user by email.
- Invited user can list pending invitations.
- Invited user can accept/reject an invitation.
- Captain can remove non-captain members.
- `GET /api/teams/my` now returns teams where the user is captain plus teams where the user is accepted member.

Expected endpoints:

```text
GET    /api/teams/{id}/members
POST   /api/teams/{id}/invite
DELETE /api/teams/{id}/members/{memberId}
GET    /api/teams/invitations/my
PUT    /api/teams/invitations/{invitationId}
```

Important security detail:

- `SecurityConfig` was adjusted so `GET /api/teams/my` and `GET /api/teams/invitations/my` require authentication before the broader public `GET /api/teams/**` rule.

Need manual smoke test:

1. Register captain/player.
2. Captain creates team.
3. Captain invites player by email.
4. Player lists invitations.
5. Player accepts invitation.
6. `GET /api/teams/my` as player includes the accepted team.
7. Captain lists members.
8. Captain removes player.

## Remaining Features To Build

### 1. Finalize Team Chat Integration

Current team member feature does not create team chat yet. The reference project creates a team conversation when a team is created and adds accepted members to that conversation.

To implement properly in this project:

- Add `ConversationType.TEAM`.
- Add fields to `conversations`:
  - `name`
  - `status` if needed
  - maybe `match_id` later for match lifecycle
- Add `conversation_id` to `teams`.
- Update `Team` entity with `conversationId`.
- On create team:
  - create team conversation
  - save `conversation_id` on team
  - add captain to `conversation_members`
- On invite accept:
  - add accepted user to team conversation.
- On remove member:
  - remove user from team conversation.

Suggested branch:

```text
feat/backend-team-chat
```

### 2. Chat Realtime With WebSocket

Current chat is REST-only. Reference project has STOMP WebSocket.

Need to build:

- Add `spring-boot-starter-websocket`.
- Add `WebSocketConfig`.
- Add JWT channel interceptor adapted to current JWT/Long id.
- Broadcast messages to `/topic/conversations/{conversationId}`.
- Optional user notifications on `/queue/notifications`.
- Add Redis unread counters:
  - key pattern: `unread_count:{userId}:{conversationId}`
  - increment on message send for other members
  - reset when user opens conversation/messages
- Keep DB as source of truth for messages.

Suggested branch:

```text
feat/backend-chat-realtime
```

### 3. Upload Images

Reference project uses Cloudinary.

Need to decide provider first:

- Cloudinary like reference project, or
- Supabase Storage, likely better since DB is already Supabase.

If using Cloudinary:

- Add dependency `cloudinary-http44`.
- Add `CloudinaryConfig`, `CloudinaryService`, `FileUploadController`.
- Secrets go in `.env`.

If using Supabase Storage:

- Add service using Supabase Storage REST API.
- Use `SUPABASE_URL`, `SUPABASE_SERVICE_ROLE_KEY`, bucket name from env.

Suggested branch:

```text
feat/backend-file-upload
```

### 4. Fairplay / Opponent Report

Current review has trust score logic, but does not fully match the reference fairplay court flow.

Need:

- Decide whether to reuse current `reviews` table or add `opponent_reviews`.
- If matching reference project:
  - add `OpponentReview`
  - add `OpponentRatingType`: `GOOD`, `NO_SHOW`, `BAD_BEHAVIOR`
  - add `FairplayStatus`: `PENDING`, `RESOLVED`, `REJECTED`
  - submit opponent review/report
  - admin list pending reports
  - admin resolve/reject and apply trust score delta
  - optional image evidence URL
- Avoid duplicating current trust score review behavior without clear separation.

Suggested branch:

```text
feat/backend-fairplay
```

### 5. AI Chatbot And AI Recommendations

Reference project uses Groq.

Need:

- Add `GroqAiService`.
- Add DTOs:
  - `dto.aiChatBot.request.ChatCreateRequest`
  - `dto.aiChatBot.response.ChatResponse`
  - AI recommendation DTOs if needed
- Add `/api/chat/ask`.
- Add Redis `ChatSession` with TTL 3600 seconds.
- Keep context grounded in real DB data:
  - fields
  - booking policy
  - match/fairplay rules
- Add env:
  - `GROQ_API_KEY`
  - `GROQ_API_URL`
  - `GROQ_API_MODEL`

Suggested branch:

```text
feat/backend-ai-chatbot
```

For AI opponent recommendations:

- Use existing match posts and trust score.
- Return recommended match posts with AI reason.
- Keep fallback empty list if AI fails.

Suggested branch:

```text
feat/backend-ai-match-recommendation
```

### 6. Auth Enhancements

Already implemented:

- register/login
- register OTP
- forgot password
- verify reset OTP
- reset password

Potential future auth work:

- Enforce registration OTP in production by setting `app.auth.registration-otp-required=true`.
- Enable real mail sending by setting `app.auth.otp.mail-enabled=true` and adding mail env credentials.
- Google login/sync if frontend needs it.
- Supabase Auth integration only if the team decides to use Supabase Auth, not just Supabase PostgreSQL.

Suggested branch if needed:

```text
feat/backend-auth-google
```

### 7. Payment And Booking Operation Polish

Current payment is better than the old project in several ways, but operational flows can be expanded:

- owner check-in
- owner checkout
- cash remaining amount payment
- no-show handling
- refund/cancel policy
- more precise booking lifecycle

Suggested branch:

```text
feat/backend-booking-operations
```

### 8. Admin Dashboard Polish

Current admin dashboard exists.

Possible additions:

- transaction history endpoint similar to reference project
- fairplay pending/resolve dashboard
- user trust score management
- field/payment charts if frontend needs them

Suggested branch:

```text
feat/backend-admin-polish
```

### 9. API Docs

Reference project has Swagger.

Need:

- Add `springdoc-openapi-starter-webmvc-ui`.
- Configure Swagger path.
- Make sure secured endpoints document Bearer token.

Suggested branch:

```text
feat/backend-api-docs
```

## Suggested Roadmap From Here

Recommended order:

1. Finish and commit `feat/backend-team-member`.
2. `feat/backend-team-chat`.
3. `feat/backend-chat-realtime`.
4. `feat/backend-file-upload`.
5. `feat/backend-fairplay`.
6. `feat/backend-ai-chatbot`.
7. `feat/backend-ai-match-recommendation`.
8. `feat/backend-booking-operations`.
9. `feat/backend-api-docs`.
10. Final polish and end-to-end scripts.

## Useful Commands

Build/test:

```powershell
cd D:\UIT\NhapMon_UngDungDiDong\SE114-HeThongDatSanBong\backend
.\mvnw.cmd test
```

Run Redis:

```powershell
docker compose up -d redis
```

Optional local PostgreSQL:

```powershell
docker compose --profile local-db up -d postgres
```

Run backend:

```powershell
.\mvnw.cmd spring-boot:run
```

Stripe webhook local:

```powershell
stripe listen --forward-to localhost:8080/api/payments/webhook
```

## Notes For The Next Agent

- Always inspect current files before editing. The user may have pushed/merged between sessions.
- Do not revert user changes.
- Do not edit `.env` unless explicitly asked; never print secret values.
- If reading the reference project, treat it as a behavioral guide only.
- Keep feature PRs small and branch-scoped.
- For every feature, provide:
  - what changed
  - how to test
  - commit message
  - PR description

