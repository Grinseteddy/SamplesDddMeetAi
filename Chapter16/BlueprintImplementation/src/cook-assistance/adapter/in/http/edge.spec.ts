import { describe, it } from "vitest";

// Inbound HTTP — increment 0, step 9, unit 9.2: what the whole API shares (ADR-CA-006, ADR-CA-010, ADR-CA-011).

describe("HTTP edge", () => {
  it.todo("an undeclared path: 404 Problem code NotFound -- ADR-CA-006");
  it.todo("DELETE /help-requests (undeclared method): 405 Problem with Allow: POST, GET -- ADR-CA-006 RFC 9110");
  it.todo("Content-Type text/plain: 415 Problem -- ADR-CA-006");
  it.todo("a body that is not valid JSON: 400 Problem code InvalidRequest, not 500 -- ADR-CA-006");
  it.todo("a body larger than 1 MB: 413 Problem -- ADR-CA-010");
  it.todo("the 61st request within a minute from one cook: 429 Problem with Retry-After -- ADR-CA-010");
  it.todo("security headers: no X-Powered-By, Content-Security-Policy default-src 'none', Strict-Transport-Security -- ADR-CA-010");
  it.todo("gate: every OpenAPI operation is served or on the planned list (SERVED_OPERATIONS satisfies the generated operations) -- ADR-CA-011");
  it.todo("gate: no multipart handling or FileInterceptor in src (accepted multer risk GHSA-q7qp-rjg7-q26m) -- ADR-CA-010");
});
