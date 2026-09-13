# Worked example — Community Cooking

Same board this whole toolkit keeps coming back to, so the findings can be
checked against the pivotal-event cuts, the invariants sheet and the
context-cut story already written about it. This walks signals → questions
→ agenda rows for a handful of representative findings; a real run
produces the full agenda, not a sample of six.

Assume `domain-knowledge-graph` has already merged the EventStorming board,
the two pivotal-event cuts, the invariants sheet, the six-sentence context
cut, and the two Core Domain Chart passes referenced in the prototype
readme into `graph.ttl`.

```
python3 scripts/mine_signals.py graph.ttl --json signals.json
```

## Signal → question, four ways

### A straddler → a boundary question

**Signal:** `STRAD-7`, label `Help request`, `dkg:inContext` both
`Ctx_CookingAssistance` and `Ctx_CookingHelp`, sourced to the board (events
7 and 15) and to both pivotal-event cuts.

**Applying the blast-radius test:** yes — whether `Help request` is owned
by one context or drawn as two decides whether this ships as one service
called from two triggers, or two services with a shared aggregate neither
fully owns. That second shape is a known anti-pattern; worth deciding
before either is built.

**Agenda row:**

| ID | Question | Why it moves the diagram | Options on the table | Evidence | Confidence |
|----|----------|---------------------------|-----------------------|----------|------------|
| AQ-001 | Is Cooking Assistance / Cooking Help one context or two, and if one, does it need a different response-time budget depending on whether it's triggered from planning or from the stove? | Decides whether this is one deployable service reachable from two triggers, or two services sharing an aggregate neither fully owns — the latter needs an explicit shared-kernel or customer/supplier decision either way | (a) one context, urgency carried on the request (both cuts and the invariants sheet independently reach this); (b) two contexts if the response-time requirement differs by an order of magnitude at the stove vs. at the table | Board · events 7,8,15,16; pivotal-event cut and re-run, §7; invariants sheet, INV-HELP-08 | OnArtifact (the duplication) / Inferred (the resolution) |

Note the options aren't invented — both cuts and the invariants sheet
already argued this exact fork, so the row just carries their conclusion
and its one live condition forward.

### An unwrapped external system → an ACL question

**Signal:** `EXTSYS-1`, label `Grandma Avatar`, typed `dkg:ExternalSystem`,
no comment or Decision in the graph mentioning "anticorruption" or "wrap".

**Blast-radius test:** yes — an unwrapped external system means the
avatar's response shape leaks straight into the domain model; wrapping it
means a translation layer owned by whoever owns Cooking Assistance.

**Agenda row:**

| ID | Question | Why it moves the diagram | Options on the table | Evidence | Confidence |
|----|----------|---------------------------|-----------------------|----------|------------|
| AQ-002 | Does the Grandma Avatar sit behind an anticorruption layer inside Cooking Assistance, or is it modelled as a responder the context owns directly? | An ACL means Cooking Assistance's `Help response` shape is stable even if the avatar's API changes; no ACL means every avatar-shape change is a domain change | (a) responder inside Cooking Assistance, wrapped (context-cut story, §6 and §7); (b) no wrapper — reject, no artifact argues this, listed only because the graph shows no decision either way | context-cut story, §6 border contracts; invariants sheet, INV-HELP-07 | Inferred |

### A capability whose classification changed → a build-vs-buy question

**Signal:** `CLASSCONF-1`, label `Stranger answers fast` (or whatever the
capability is named on the chart), classified `core` on one Core Domain
Chart pass and, hypothetically, `supporting` on a later one — this is the
shape the signal takes whenever two chart ingests disagree; substitute the
graph's actual pair.

**Blast-radius test:** yes — core means "invest here, build it well even
if it's slow"; supporting means "keep it working, spend less." Building to
the wrong classification either overspends on a commodity or underinvests
in the thing the product wins or loses on.

**Agenda row:**

| ID | Question | Why it moves the diagram | Options on the table | Evidence | Confidence |
|----|----------|---------------------------|-----------------------|----------|------------|
| AQ-003 | Has "a stranger answers a panicking cook fast enough to save dinner" changed from core to supporting, and does the current architecture plan reflect whichever is current? | Core justifies bespoke routing, escalation and a maintained avatar model; supporting justifies a simpler, cheaper implementation | Whichever classification is on the later chart, unless the team affirms the change was a mistake | Core Domain Chart, first and second pass | OnArtifact (both markings) / Inferred (that the change is intentional) |

### An open question that fails the blast-radius test → set aside, not dropped

**Signal:** `OPENQ-6`, label *"Can thanks be given to the Grandma Avatar, or
only to human helpers?"*, no `dkg:Decision` answers it.

**Blast-radius test:** no — either answer is a UI/copy and business-rule
decision (who can `Provide thanks` to), not a service-boundary or
integration-pattern one. It doesn't move a box on the diagram.

**Where it goes:** §5, "Not on this agenda" — not discarded, because it's a
real open question and someone still has to decide it, just not in an
architecture review.

## What the full agenda looks like once assembled

The four rows above would land in a real agenda as:

- **§3a Context boundaries & ownership:** AQ-001, plus the `Meal plan`
  orphan (`ORPHAN-1` in every version of the board — nothing reads the
  settled plan) and the `Meal Preparation` no-owned-aggregate finding
  (`NOAGG-1`, corroborated independently by all three prior analyses of
  this board).
- **§3b Integration & consistency:** AQ-002, plus any `unresolved_message_kind`
  or `borders_without_timing` findings the Event Model's canvases raise.
- **§3c Build vs. buy:** AQ-003.
- **§3d Numbers nobody supplied:** the invariants sheet's `<n>` placeholders
  (borrowing-adjacent limits, `<n>` minutes before the avatar answers) —
  `placeholder_numbers` picks these up directly from `INV-LEND-02`-style
  rule labels once the invariants sheet is in the graph.
- **§4 Severity ranking:** AQ-001 first — it's the finding every
  independent analysis of this board reached on its own, it's cited by the
  most other open questions (the ACL question and the response-time
  question both depend on its answer), and it's the one with existing,
  well-argued options already on record.
- **§5 Not on this agenda:** the Grandma-Avatar-thanks question, and
  anything else that reads as a product decision rather than a
  boundary/integration/investment one.
  This is also the shape a small-ADR log entry takes once the room decides
  AQ-001: `Date · SADR-00xx · "One assistance context" · Adopted · <author> ·
  "One context, urgency carried on the request" · <AQ-001's Question column,
  verbatim> · <AQ-001's Options column, verbatim>`. Re-ingesting that log
  closes the loop described in SKILL.md §7.