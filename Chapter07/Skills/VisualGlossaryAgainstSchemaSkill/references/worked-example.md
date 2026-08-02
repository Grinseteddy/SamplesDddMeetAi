# Worked example — Parcel Delivery glossary × delivery API components

A full run: transcription, mechanical pass, ledger, report, and a second-revision
re-check. The schema is `example-files/delivery-v1.yaml`; every finding below is
reproducible against it.

---

## The inputs

### Glossary transcription (Step 1)

Terms, with colour groups. **Yellow** stickies carry no icon; **blue** stickies
are marked with a small building icon and sit inside a box labelled
*Customer Management*.

```
Delivery context (yellow):
  Sender, Shipment, Parcel, Weight, Delivery Attempt,
  Successful Delivery, Failed Delivery, Failure Reason,
  Proof of Delivery, Photo, Courier

Customer Management (blue):
  Customer, Delivery Address
```

Relationships, read source → target:

```
 R1  Customer            —acts as→   0..1  Sender
 R2  Sender              —books→     0..*  Shipment
 R3  Shipment            —contains→  1..*  Parcel
 R4  Shipment            —to→        1     Delivery Address
 R5  Parcel              —has→       1     Weight
 R6  Parcel              —gets→      0..*  Delivery Attempt
 R7  Delivery Attempt    —by→        1     Courier
 R8  Successful Delivery —is→        (no cardinality)  Delivery Attempt
 R9  Failed Delivery     —is→        (no cardinality)  Delivery Attempt
 R10 Successful Delivery —produces→  1     Proof of Delivery
 R11 Failed Delivery     —has→       1     Failure Reason
 R12 Proof of Delivery   —contains→  0..5  Photo
```

Confirmed with the author: the blue box is a neighbouring bounded context whose
model this team does not own, and R8/R9 are drawn with the same plain arrowhead
as everything else — the `is` label is the only thing marking them as
specialization.

### Mechanical pass (Step 2)

```bash
python scripts/inspect_schema.py references/example-files/delivery-v1.yaml
```

11 findings: one `REF-BROKEN`, one `REQ-NULL`, one `REQ-UNDEFINED`, two
`EX-TYPE`, two `PAT-SINGLE-DIGIT`, one `PAT-DOT`, one `FMT-PAIR`, one
`VENDOR-TYPO`, one `ORPHAN`. They are folded into the report below as F1–F3 and
the `DEFECT` block.

---

# Conformance Report — delivery-v1.yaml × Parcel Delivery glossary

## 0. Scope checked

`delivery-v1.yaml` (components only; no `paths` supplied, so entry points and
required-on-create semantics are **unchecked**). Glossary photographed from the
workshop board, 12 relationships, all legible. Schema is a **draft**, not yet
shipped — no stored data constrains the fixes. The schema is the **newer**
artifact by three weeks.

## 1. Verdict

**Diverged.** 6 blocking, 8 significant, 4 minor. The blocking findings cluster
in one place: the specialization of `Delivery Attempt` was implemented twice, in
two different ways, and neither works.

## 2. Mapping ledger

| Glossary term | Schema construct | Kind | Status |
|---|---|---|---|
| Sender | `Shipment.sender` (uuid) | reference | = |
| Shipment | `Shipment` | type | = |
| Parcel | `Parcel` | type | = |
| Weight | `Parcel.weight` + `weightUnit` | two flat fields | ~ F11 |
| Delivery Attempt | `DeliveryAttempt` | type | = |
| Successful Delivery | `SuccessfulDelivery` | subtype (broken) | ✗ F1, F8 |
| Failed Delivery | `FailedDelivery` | standalone type | ✗ F8 |
| Failure Reason | `DeliveryAttempt.note` (free text) | flattened, wrong type | ✗ F9 |
| Proof of Delivery | `ProofOfDelivery` | type | = |
| Photo | `Photo` | value type | = |
| Courier | `DeliveryAttempt.driver` | reference, renamed | ✗ F7 |
| Customer | — | — | allowlist |
| Delivery Address | `DeliveryAddress` | local value type | ~ F12 |

*Schema-only constructs*

— *domain vocabulary missing from the glossary:* `trackingNumber`, `attemptedAt`,
`ProofOfDelivery.signedBy`, `FailedDelivery.retryScheduledFor` → **F14**
— *allowlisted:* `shipmentId`, `parcelId`, `attemptId`, all `format`,
`minLength`, `maxLength`, `examples`, `description`

## 3. Findings

### Blocking

**F1 · `DEFECT` REF-BROKEN · `SuccessfulDelivery.allOf[0]`**
`'#components/schemas/DeliveryAttempt'` is missing its slash. `SuccessfulDelivery`
inherits nothing — no `parcel`, no `driver`, no `attemptedAt`. Every other
`$ref` in the file is correct, which is the tell that this one was hand-typed.
*Note this invalidates any check of that type's fields until it is fixed.*
→ schema moves.

**F2 · `DEFECT` REQ-NULL · `SuccessfulDelivery`**
`required:` with nothing under it parses as `null`; the schema is invalid.
→ schema moves.

**F3 · `DEFECT` REQ-UNDEFINED · `ProofOfDelivery.photo`**
`required` names `photo`; the property is `photos`. Singular/plural slip.
→ schema moves.

**F4 · `CARD` · R3 `Shipment —contains→ 1..* Parcel`**
`parcels` has `maxItems: 30` but no `minItems`, and is not in `required`. A
shipment with no parcels validates. The glossary says that cannot exist.
Note `maxItems: 30` is a bound the glossary never asserted — real rule, or a
remembered screen limit? Ask what happens at the thirty-first parcel.
→ schema moves: `minItems: 1`, add `parcels` to `required`. Glossary moves: add
`1..30` if 30 is real.

**F5 · `REF` · `DeliveryAttempt.parcel`, `FailedDelivery.parcel`**
The parcel is referenced by its **position** in the shipment (`"001"`,
`pattern: ^\d$`), while `Parcel` already carries a `parcelId`. Two consequences:
the pattern caps a shipment at ten parcels, contradicting both R3 and the
schema's own `maxItems: 30`; and a stored attempt re-points at a different parcel
if the parcel list is ever reordered or one is removed. Delivery attempts are
evidence — they are read back.
→ schema moves: reference `parcelId`.

**F6 · `SENSE` · `parcel`**
The same field name is a positional string on `DeliveryAttempt` and
`FailedDelivery`, and a `$ref: Parcel` object on `ProofOfDelivery`. One wire name,
two incompatible types. Two developers reading two types will build two things.
→ schema moves; F5's fix resolves it if applied everywhere.

### Significant

**F7 · `TERM` · R7 `Delivery Attempt —by→ 1 Courier`**
The schema says `driver`. The glossary is the terminology authority and the whole
board says *Courier*.
→ schema moves: rename `driver` → `courier`.

**F8 · `SUB` · R8, R9**
The glossary draws two `is-a` edges into `Delivery Attempt`. The schema
implements one as `allOf` (broken, F1) and the other, `FailedDelivery`, as a
standalone type that copy-pastes `parcel` and `driver` from the base. So the two
siblings don't share a base, and nothing distinguishes them on the wire.
→ schema moves: `FailedDelivery` becomes `allOf: [DeliveryAttempt]`, and the base
gets a `discriminator`.

**F9 · `MISS` · R11 `Failed Delivery —has→ 1 Failure Reason`**
`Failure Reason` is a mandatory term with its own sticky. The schema has
`note`, free text, on the **base** type — so a successful delivery may carry a
failure note and a failed one need carry nothing. The flattening may be fine, but
the placement is not.
The question that decides the flattening: **what consumes it?** If anything
routes retries, triggers a customer notification, or reports on failure causes,
this needs an enum, not prose.
→ ask; then schema moves.

**F10 · `CARD` · R12 `Proof of Delivery —contains→ 0..5 Photo`**
Schema says `maxItems: 10`. And the orphan `Photos` type says `minItems: 1`, no
maximum. Three statements about one collection.
→ ask which bound is real, then align all three (or delete `Photos`, see F18).

**F11 · `CARD` + `TERM` · R10 and R5**
Two smaller ones in the same family. R10 says a successful delivery produces
**exactly one** proof of delivery; `proofOfDelivery` is not in `required` (and
currently unreachable anyway, F2). And R5 makes `Weight` a term in its own right,
while the schema splits it into `weight` + `weightUnit` — a value object drawn as
one thing, implemented as two loose fields that can disagree.
→ schema moves on both.

**F12 · `CTX` · `DeliveryAddress`**
Defined locally although the glossary marks it as Customer Management's. **This is
correct** — a local definition is an anti-corruption layer, and reusing the other
team's type would couple your release to theirs. The finding is narrower:
`postcode` is a free 3–10 character string here, and addresses are the one thing
guaranteed to be compared across the boundary. If Customer Management validates
postcodes differently, the same address will be valid on one side and not the
other.
→ ask Customer Management for their format; match it or translate deliberately.

**F13 · `DUP` · `ProofOfDelivery.parcel`, `ProofOfDelivery.deliveryAddress`**
Both are reachable from the attempt the proof belongs to: attempt → parcel →
shipment → address. On the derivability rule alone, they should go.
**But apply the guard before cutting.** A proof of delivery is *evidence*. If the
customer corrects the address after handover, the proof must still say where the
parcel actually went — that is a deliberate snapshot, and the right fix is a
description saying so, not a deletion. The `parcel` copy is harder to defend: an
identifier would carry the same evidential weight.
→ ask; expected outcome is "keep the address, document it as a snapshot; reduce
`parcel` to `parcelId`".

**F14 · `EXTRA` · four fields**
`trackingNumber`, `attemptedAt`, `signedBy`, `retryScheduledFor` are domain
vocabulary with no sticky. `attemptedAt` in particular is load-bearing — without
it, `0..*` attempts have no order.
→ glossary moves: add the terms.

### Minor

**F15 · `DEFECT` · `Parcel.weight`** — `type: number` with `format: int32` is not
a defined pairing; `int32` belongs with `type: integer`. Behaviour is
generator-dependent. Also worth deciding whether weights are fractional — if
grams, integers are fine; if kilograms, they are not.

**F16 · `DEFECT` · `Parcel.weightUnit`** — `x-extenxible-enum` is misspelled;
tooling ignores unknown `x-` keys silently, so the enum is simply absent. And
`POUNDS` alongside `GRAMS` means two units that must be compared — fine, but the
conversion has to live somewhere written down.

**F17 · `DEFECT` · patterns and examples** — `^\d\d.\d\d.\d\d\d\d$` has unescaped
dots, so it matches `15x03x2026`. The `001` examples parse as the integer `1`
against a `string` type — quote them.

**F18 · `DEFECT` ORPHAN · `Photos`** — nothing references it, and it contradicts
its inline twin (F10). A duplicated-and-unused collection type drifts by
construction.
→ delete it, or reference it from `ProofOfDelivery.photos`.

## 4. Relationships & cardinalities

| # | Relationship | Given | Schema realization | Verdict |
|---|---|---|---|---|
| R1 | Customer —acts as→ 0..1 Sender | 0..1 | — | allowlist (other context) |
| R2 | Sender —books→ 0..* Shipment | 0..* | `Shipment.sender` uuid | consistent |
| R3 | Shipment —contains→ 1..* Parcel | 1..* | `parcels`, no minItems | **contradicted** (F4) |
| R4 | Shipment —to→ 1 Delivery Address | 1 | required `deliveryAddress` | consistent |
| R5 | Parcel —has→ 1 Weight | 1 | required `weight` (+ loose unit) | consistent-ish (F11) |
| R6 | Parcel —gets→ 0..* Delivery Attempt | 0..* | inverse: `DeliveryAttempt.parcel` | consistent (F5 on style) |
| R7 | Delivery Attempt —by→ 1 Courier | 1 | required `driver` | renamed (F7) |
| R8 | Successful Delivery —is→ Delivery Attempt | — | `allOf`, broken | **contradicted** (F1) |
| R9 | Failed Delivery —is→ Delivery Attempt | — | none — standalone | **contradicted** (F8) |
| R10 | Successful Delivery —produces→ 1 PoD | 1 | optional field | **contradicted** (F11) |
| R11 | Failed Delivery —has→ 1 Failure Reason | 1 | `note` on the base | **contradicted** (F9) |
| R12 | Proof of Delivery —contains→ 0..5 Photo | 0..5 | `0..10`, and `Photos` says `1..*` | **contradicted** (F10) |

Inverse multiplicities (how many shipments per sender, how many attempts a
courier makes) are **not expressible** in a payload schema — they are a gap in
the glossary, not a finding against the code.

## 5. Allowed abstraction

Deliberately not reported:

- **`Customer` has no schema type.** It belongs to Customer Management; the API
  references a customer by identifier, which is the correct treatment of a term
  the model doesn't own. Same for R1, a relationship entirely inside that context.
- **`Sender` appears only as a uuid field.** An actor who acts on the domain
  rather than being carried in a payload.
- **Identifiers, formats, lengths, examples, descriptions** have no glossary
  equivalent by design — a glossary is drawn at business resolution.
- **Casing.** `deliveryAddress` versus `Delivery Address` is a schema convention,
  not a vocabulary difference.
- **`DeliveryAddress` being defined locally** rather than imported — see F12; the
  duplication is correct, only the postcode format is at issue.

## 6. Patch list

**Schema**
1. `SuccessfulDelivery.allOf[0]`: `#components/…` → `#/components/…` (F1)
2. `SuccessfulDelivery`: remove the empty `required:` (F2)
3. `ProofOfDelivery.required`: `photo` → `photos` (F3)
4. `Shipment.parcels`: add `minItems: 1`; add `parcels` to `required` (F4)
5. `DeliveryAttempt.parcel` / `FailedDelivery.parcel`: replace the positional
   string with a `parcelId` uuid reference; drop `pattern` (F5, F6)
6. rename `driver` → `courier` throughout (F7)
7. `FailedDelivery`: `allOf: [$ref DeliveryAttempt]`, drop the copied `parcel`
   and `courier`; add `discriminator` to `DeliveryAttempt` (F8)
8. move `note` off the base onto `FailedDelivery` as `failureReason`; decide enum
   vs free text (F9)
9. `photos`: `maxItems: 5`; delete `Photos` (F10, F18)
10. `ProofOfDelivery`: reduce `parcel` to `parcelId`; keep `deliveryAddress` but
    document it as a handover snapshot (F13)
11. `proofOfDelivery` into `SuccessfulDelivery.required` (F11)
12. `weight`: `type: integer, format: int32`, or drop the format (F15)
13. fix `x-extenxible-enum` → `x-extensible-enum` (F16)
14. escape the dots in `retryScheduledFor`; quote the `001` examples (F17)

**Glossary**
1. Add `Tracking Number` under `Shipment`; add `Attempted At` under
   `Delivery Attempt` — without it the `0..*` attempts have no order (F14)
2. Add `Signed By` under `Proof of Delivery`, `Retry Date` under
   `Failed Delivery` (F14)
3. Record `Weight` as a value with a unit, and name the units (F11, F16)
4. Confirm or drop the `0..5` photo bound and add the parcel bound if 30 is real
   (F4, F10)
5. Note that `Delivery Address` is *referenced and locally described*, not
   imported — the picture currently implies a shared type (F12)

## 7. Already aligned

`Shipment`, `Parcel`, `Delivery Attempt`, `Proof of Delivery` and `Photo` all
carry the glossary's exact term. R2, R4 and R6 are faithfully realized, including
the direction of R6 — the attempt points at the parcel, which is the only way a
payload schema can express a `0..*`. `Photo` as a URI value type matches the
glossary's leaf term exactly. The context boundary is respected in structure: the
API references Customer Management, never models it.

---

## Second revision — a re-check

The team sends `delivery-v2.yaml` two days later. Report the delta, not the
report again.

### Fixed

F1, F2, F3 — all three blocking defects closed. F7 — `driver` is now `courier`
everywhere. F4 — `parcels` has `minItems: 1` and is required.

### New breakage

**The `parcel` fix was applied on one side only.** `DeliveryAttempt.parcel` is
now a `parcelId` uuid; `FailedDelivery.parcel` is still `"001"` with
`pattern: ^\d$`. F5 is half-closed and **F6 is worse than before**: previously the
name meant "index" on two types and "object" on a third; now it means three
things. A half-applied fix can score worse than no fix.

**`required:` null has migrated.** It is gone from `SuccessfulDelivery` and
present on `RetryPolicy`, added this revision. The bug wasn't fixed; it moved.
That is the signal to add a lint step (`inspect_schema.py` in CI, or Spectral)
rather than to patch a fourth time.

**The team's own decision hasn't landed.** In the review call they agreed the
failure reason would be an enum. `delivery-v2.yaml` still has free-text `note`,
now duplicated onto `FailedDelivery` while remaining on the base. F9 is open and
has grown a `DUP`.

### Unchanged

F10 (photo bounds, three ways), F11 (`proofOfDelivery` still optional,
`Weight` still two loose fields), F12 (postcode format), F13 (the proof-of-delivery copies), F14 (four terms
still missing from the glossary), F15–F18 (`format: int32`, the enum typo, the
unescaped dots, the orphan `Photos`).

---

## What this example is meant to teach

- **The mechanical pass first.** F1 alone invalidated every field-level check on
  `SuccessfulDelivery`. Had the report led with vocabulary findings, half of them
  would have been about fields that weren't actually there.
- **The allowlist is a section, not a silence.** `Customer`, `Sender`, and every
  identifier are absent from the schema on purpose. Saying so is what makes F4
  and F9 credible.
- **`CTX` findings are usually about naming and drift, not duplication.** The
  local `DeliveryAddress` is right. The postcode format is the risk.
- **`DUP` cuts both ways.** `ProofOfDelivery` copying the address looks like
  derivable duplication — but proof of delivery is *evidence*, and evidence
  should freeze the address as it was at handover. That one is a snapshot, and
  the fix is a description saying so, not a deletion. Ask before you cut.