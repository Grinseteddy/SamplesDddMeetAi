# Checks — matching, mapping, the ten codes, and what not to report

Read this before building the mapping ledger. The guards matter as much as the
checks: a report that flags every legitimate abstraction is read once and then
ignored.

## Contents

1. The construct mapping table
2. The matching ladder
3. The ten checks
4. The abstraction allowlist — what not to report
5. Severity rubric

---

## 1. The construct mapping table

What a glossary construct *should* look like once it reaches a schema. Use this
to decide whether a realization is faithful, loose, or absent — and note the
right-hand column, which tells you when a mismatch isn't a finding because the
schema physically cannot say it.

| Glossary construct | Faithful schema realization | Notes |
|---|---|---|
| Term with attributes hanging off it | Object schema | |
| Leaf term at the end of a `1` / `0..1` edge | Field, or a small object used by value | Value object; a table is not required |
| Term in another context (different colour/group) | `$ref`-free identifier field, or a locally defined value type | See `CTX` |
| `A —has→ 1 B` | `b` in `required` | |
| `A —has→ 0..1 B` | `b` present, not in `required` | |
| `A —has→ 1..* B` | `type: array`, `minItems: 1` | |
| `A —has→ 0..* B` | `type: array`, `minItems: 0` or absent | |
| `A —has→ 1..10 B` | `minItems: 1, maxItems: 10` | Ask whether the bound is real |
| `A —is→ B` (is-a) | `allOf: [$ref B]` + own properties | See `SUB` |
| Two `is-a` siblings | Sibling `allOf` schemas + `discriminator` on the base | Without a discriminator the family is unreadable on the wire |
| Exclusive choice between two targets | `oneOf`, or one field with an enum of the two | The glossary usually *can't* express exclusivity — two `1` edges from one term almost always mean XOR |
| Inverse multiplicity (`how many A per B`) | **Not expressible** in a payload schema | Never a finding against the schema; it's a glossary gap |
| Relationship with no owning side | Must be given an owner in the schema | The choice of owner is a real decision — record it, don't flag it |
| Term that is a role of another term | One type, two field names | `Substitute` and `Ingredient` may be one shape in two roles |

---

## 2. The matching ladder

Walk down until something matches. Stop at the first hit; record which rung you
stopped on, because the rung *is* the finding's severity.

1. **Exact** — `Parcel` ↔ `Parcel`. No finding.
2. **Case / spacing / separator variant** — `Delivery Attempt` ↔
   `DeliveryAttempt`, `deliveryAttempt`. No finding; schema casing conventions are
   not domain language.
3. **Plural of a collection** — glossary `Parcel` ↔ schema `parcels: [Parcel]`.
   No finding when the multiplicity justifies it.
4. **Charitable typo** — `Catastrophy` ↔ `Catastrophe`. One term, minor finding,
   and note that the *glossary* is as likely to hold the typo as the schema.
5. **Role-of** — the schema uses one type where the glossary has two terms
   because one is a role of the other (`Substitute` is an `Ingredient` offered in
   place of another). Not a mismatch; a `TERM` finding asking the glossary to say
   so.
6. **Synonym** — `Courier` ↔ `driver`. `TERM`, significant: this is precisely the
   drift a glossary exists to prevent.
7. **Unmatched** — nothing plausible. `MISS` (glossary side) or `EXTRA` (schema
   side).

**Guard:** before declaring rung 7 on the schema side, check whether the
construct is *technical envelope* rather than domain vocabulary — see the
allowlist. Most unmatched schema fields are.

---

## 3. The ten checks

### `CARD` — contradicted or dropped multiplicity

**Detect:** for each glossary edge, find its schema realization via the mapping
table and compare. Three outcomes: consistent, contradicted, absent.

**Typical hits:**
- Glossary `1..*`, schema `minItems: 0` — or no `minItems` at all, which is the
  same thing.
- Glossary `1` (mandatory), field not in `required`.
- A `pattern` that silently imposes a bound the glossary contradicts: `^\d$`
  caps an index at ten, against an unbounded `1..*`.
- A bound in the schema the glossary never asserted (`maxItems: 3`). Not
  automatically wrong — but it is a business rule that appeared without anyone
  drawing it.

**Guard:** `required` on a *field* and mandatory on a *relationship* are not
always the same claim. A field can be absent from a creation payload and
mandatory in the domain. Ask whether the schema describes a full resource or a
request body before filing.

**Severity:** blocking when the two assert different facts; significant when the
schema simply drops a bound.

---

### `TERM` — same concept, different word

**Detect:** ladder rungs 4–6.

**Guard:** don't file schema *casing* or a plural on a collection. Do file a
synonym, however small — `thanksGiver` where the glossary says `Cook` is exactly
the drift worth catching, and it costs one rename.

**Resolution:** schema moves, by default. The glossary is the terminology
authority. Exception: when the glossary holds a typo or a word the team has since
abandoned, the glossary moves — say which you think it is.

---

### `SENSE` — one word, two meanings inside the schema ★

**Detect:** group schema field names across all types. Any name appearing twice
with different shapes is a candidate. Then check the descriptions — a type reused
for a second purpose usually still carries the first purpose's description.

**Why it matters more than it looks:** this is not a naming quibble. Two
developers reading two types will implement two different things, and the bug
surfaces at integration. It is also the most common consequence of a *partial*
fix, which makes it the check to run first on any re-check.

**Typical hits:**
- `ingredient` as a positional string on one type and a `$ref` to an object on
  another.
- A type named for one context's term (`Ingredient`) while a sibling field of the
  same name means an index into that context's list.
- A shared value type whose `description` still describes its original single
  use ("a substitute of a particular ingredient") after it was generalized.

**Severity:** significant, or blocking when the two shapes are incompatible types
for the same wire name.

---

### `MISS` — glossary term or relationship with no schema counterpart

**Detect:** ledger rows with `—` in the schema column.

**Guard — run this before filing, it eliminates most candidates:**
- Is the term in another **bounded context** (colour, group, lane)? Then absence
  is correct; it should appear as an identifier or a locally defined value type,
  not as a modelled entity. Allowlist.
- Is the term **out of scope for this API** — an actor, a channel, a
  physical-world thing the API never handles? Allowlist. A glossary routinely
  contains things no payload will ever carry.
- Is the term **structurally subsumed** — the picture has `Meal`, the schema
  represents that occasion as the request itself? That is a real design decision
  and worth a finding, but the finding is *"the glossary should say so"*, not
  *"the schema is missing a type"*.

**What survives the guard is usually the valuable finding:** a structured concept
the glossary drew as several terms with cardinalities, flattened into one free
text field. That trade is defensible — prose is fine when only humans read it —
but it should be a decision, not an accident. Ask what will consume the field.

---

### `EXTRA` — schema field no glossary term covers

**Detect:** ledger's below-the-divider list.

**Guard:** the allowlist eliminates most of these. What's left is genuine domain
vocabulary the picture never captured — very often the *text* fields (`question`,
`helpDescription`, `thankText`), because workshops draw structure and forget that
the payload is mostly prose.

**Resolution:** glossary moves, nearly always. Cheap, and it stops the same
finding recurring next revision.

---

### `SUB` — specialization mismatch

**Detect:** compare the glossary's `is-a` edges to the schema's `allOf` families.
The script reports `SUB-EMPTY` and `SUB-NO-DISCRIMINATOR`; the glossary-relative
checks are yours.

**Typical hits:**
- An `is-a` edge with no `allOf` — the subtype was flattened away.
- An `allOf` family the glossary doesn't draw — a distinction that appeared in
  code.
- **Asymmetric families**: the glossary subtypes both sides of a pair (three
  request kinds, three answer kinds) and the schema subtypes only one. This is
  the finding people miss, because each side looks fine alone.
- A subtype that adds nothing — indistinguishable from its base at runtime.
- No discriminator on a family of two or more.

---

### `REF` — reference style ★

**Detect:** every schema field that stands for a glossary term rather than
containing it. Classify: **identity** (a UUID or key), **positional** (an index,
`"001"`, `"step 3"`), or **by value** (the thing itself, inline).

Then ask the question that actually decides it — **stability**:

> *Does that reference still mean the same thing after the referenced side is
> edited?*

- **Positional** references are stable only if the referenced collection is
  immutable or versioned. If recipes, orders, or checklists can be edited in
  place, an index stored today points somewhere else tomorrow. If the reference
  is *stored* (in a request someone will read back), this is blocking.
- **Identity** across a context boundary is cheap and claims nothing about the
  other context's model — but only if the other context actually issues
  identifiers for that thing. Parts of an aggregate often have no global ID, and
  that objection is correct: local identity, scoped to the parent, is the pattern
  that fits.
- **By value** is the right answer more often than people expect at a context
  boundary — it makes the payload self-describing, survives reordering, and
  avoids a lookup into a model you don't own. Its cost is that it's a snapshot.

**Guard:** don't file a preference. File the stability question with the answer
you'd give and why.

---

### `DUP` — derivable data duplicated ★

**Detect:** wherever type B holds a mandatory link to type A with cardinality
`1`, list B's fields that also exist on A. Those are derivable.

**Why it matters:** two copies of one fact drift, and nothing in the schema says
which wins.

**Guard — one legitimate reason to duplicate:** a deliberate snapshot, where B
must keep the value A had at the time even if A changes later. That is real and
common (an order line keeping the price at purchase). Ask which it is; if it's a
snapshot, the fix is a description saying so, not a deletion.

**Look for the sibling that got it right.** In a family of subtypes, one usually
adds only what's new while the others copy context. That one is the template, and
pointing at it makes the recommendation concrete.

---

### `CTX` — bounded-context violation

**Detect:** terms the glossary marks as belonging to another context (colour,
group, lane) that the schema *models* rather than *references*.

**Guard, and it's the important one:** defining a local type with the same shape
as another context's type is **not** a violation. It is an anti-corruption layer,
and it is usually correct — reusing the other team's definition couples your
release cycle to theirs. The finding to file is about **naming and drift**, not
about the duplication:

- Does the local type carry the *other* context's term as its name, when it means
  something subtly different here? Then rename or document.
- Does anything in it need to stay comparable across the boundary — units,
  enums, currencies, code lists? That is where drift actually hurts. Two
  contexts each defining `CUP` or `SPOON` or `KG` will diverge silently, and the
  values are meant to be compared.

---

### `DEFECT` — schema-internal, glossary-independent

Everything `scripts/inspect_schema.py` reports. See
`references/mechanical-checks.md`. File these in their own subsection: they are
not disagreements with the glossary, and mixing them into the conformance
findings muddles two different conversations.

Report them anyway, and report them first if any are blocking. A broken `$ref`
means three types inherit nothing — which invalidates half your other findings
about those types, since the fields you thought were there aren't.

---

## 4. The abstraction allowlist — what not to report

Default allowlist. Everything here is a *deliberate* difference in resolution
between a business picture and an implementation artifact. Move candidates here
silently, then publish the list as a report section.

**Never report as missing from the schema:**
- Actors and roles that only *act* (`Cook`, `Sender`, `Reviewer`) — they appear
  as identifier fields, not as modelled entities.
- Relationships between two terms the API doesn't own (`Community —contains→
  Cook`, `Sender —lives at→ Address`).
- Terms belonging to a neighbouring bounded context, when referenced correctly.
- Inverse multiplicities. A payload schema cannot express them.
- Lifecycle, order, behaviour. A glossary can't express those either; if they
  matter, the answer is a domain story, not a schema field.

**Never report as missing from the glossary:**
- Identifiers (`id`, `uuid`, `*Identifier`) and their formats. Identity is an
  implementation concern; a glossary that names an identifier term is the
  exception, not the rule.
- `format`, `minLength`, `maxLength`, `pattern`, `examples`, `description`.
- Envelope and transport: pagination, `_links`, `createdAt`, `etag`, error types,
  `additionalProperties`.
- Casing conventions and separator style.

**Report, but as minor and with the abstraction noted:**
- Hard numeric bounds present in one artifact and not the other. They are
  business rules; both artifacts should carry them, but neither is wrong to have
  found the rule first.
- Free-text fields with no glossary term. Worth adding, never urgent.

---

## 5. Severity rubric

**Blocking** — the two assert different facts about the domain, or the schema
fails to validate what it claims to.
Examples: a mandatory `1` edge the schema makes optional; `required` naming a
property that doesn't exist; a broken `$ref`; a stored positional reference into
an editable collection; one field name with two incompatible types.

**Significant** — divergence that defeats the point of a shared language, or that
will produce two implementations from one document.
Examples: a synonym for a glossary term; a structured concept flattened to prose
without a decision; an asymmetric subtype family; a missing discriminator; a
stale description on a reused type.

**Minor** — cosmetic, coverage, or documentation.
Examples: casing; a relationship the schema has no occasion to express; a
free-text field with no term; an orphan schema; a bound in one artifact only.

**No action** — allowlist. Recorded in section 5 of the report, never numbered as
a finding.