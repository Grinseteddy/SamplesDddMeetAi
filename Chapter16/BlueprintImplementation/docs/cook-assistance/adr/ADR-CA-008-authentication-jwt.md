# ADR-CA-008 — Authentication: OIDC bearer tokens validated by the service

Status: accepted (2026-10-24) · Context: Cook Assistance · Step 5, question 8

## Context
Callers are cooks, community cooks and Chefs through the Larder app shell (ADR0005), and the Grandma
Avatar AI through RabbitMQ only. The OpenAPI spec declares OAuth 2.0 (authorization code with PKCE) and
the scopes `cooking-assistance:read|write|admin`. Chapter 13 runs Keycloak and derives the CookId from
the token. AP0008 asks for fine-grained access rights.

## Decision
- **HTTP:** every operation needs an OIDC **bearer access token** issued by Keycloak. The service
  validates it itself with `jose`: issuer, audience `cook-assistance`, signature against the issuer's
  JWKS (cached, refreshed on unknown `kid`), `exp`/`nbf` with **30 s** clock skew. Tokens never appear
  in URLs or logs.
- The inbound adapter turns a valid token into the application's **`Actor`** — `cookId` (`sub`), role
  (`COOK`, `COMMUNITY_COOK`, `CHEF`), `premium` flag, scopes. No framework or token type crosses into
  the application or domain.
- **Messaging:** Cook Assistance connects to RabbitMQ with **its own broker user** (credentials from the
  environment), with permissions limited to publishing on exchange `cook-assistance` and consuming its
  own queues. Messages on the consumed queue are trusted because only the Grandma Avatar AI may publish
  on exchange `grandma-avatar` (broker permission, owned by that context); `source` must be
  `grandma-avatar`, otherwise the message is dead-lettered.

## Consequences
- 401 Problem for missing/invalid tokens; the security schemes of both specs match this decision.
- Role and premium claims must be present in the token (Keycloak mappers) — see question 9 for where
  premium comes from.

## Principles and quality scenarios
Honors AP0008. Serves the security goal (rank 3), QS-19.
