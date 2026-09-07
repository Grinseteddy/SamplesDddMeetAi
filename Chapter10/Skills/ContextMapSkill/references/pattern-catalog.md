# Pattern catalog

Everything the SKILL.md compresses. Read the section you need.

- §1 Determining direction — upstream and downstream
- §2 The nine relationship patterns
- §3 Choosing between them — the decision procedure and its ties
- §4 Integration mechanisms and staleness
- §5 Writing a border contract
- §6 The smell catalog
- §7 Reviewing a map somebody else drew
- §8 Confidence, and what a board can never tell you

---

## 1. Determining direction

Direction is an ownership claim, not a data-flow observation. Settle it before
choosing a pattern; a pattern chosen first will invent a direction to justify
itself.

**The four rules, in order of strength.**

1. **The writer is upstream.** The context whose event produces the business
   object owns its schema. Everyone reading that object downstream is, by
   construction, exposed to changes in it. This single rule settles most edges on
   a well-drawn board.
2. **The change test.** If A changed its model, would B have to react? If yes, A
   is upstream. Applied both ways it also detects mutual dependency, which is a
   finding rather than a direction.
3. **The disappearance test.** If B vanished tomorrow, would A still make sense?
   If yes, A is upstream. A supplier survives the loss of a customer; a customer
   does not survive the loss of a supplier.
4. **The vocabulary test.** Whose words are used at the border? The party whose
   terms cross unchanged is upstream. This is the tie-break for edges where both
   sides write something.

**What direction is not.**

- **Not data flow.** A downstream context sends plenty upstream — payments,
  confirmations, acknowledgments. Ask who dictates vocabulary, not who sends
  bytes.
- **Not sequence.** Later in the process does not mean downstream. On many
  boards the most upstream context (identity, catalog, pricing) appears early,
  late and in the middle.
- **Not importance.** A generic upstream context can serve a core downstream one.
  Direction says nothing about value; the Core Domain Chart says that.

**Mutual dependency.** When rules 2 and 3 both come back "yes, both ways", do
not average it into a single arrow. Record `A ↔ B` and treat it in §6 as either
a real Partnership or a misplaced boundary. On EventStorming boards, mutual
dependency between two adjacent contexts is misplaced boundary far more often
than it is Partnership.

---

## 2. The nine relationship patterns

Each entry: what it is · the evidence a board can give · what rules it out ·
what it costs · how it is misused.

### Open Host Service (OHS)

**What.** The upstream publishes a stable, general-purpose interface for many
consumers, rather than negotiating with each one.

**Board evidence.** One context writes an object that three or more other
contexts read; the context recurs across the timeline serving several phases;
its vocabulary appears unchanged inside its consumers' clusters.

**Ruled out by.** A single consumer (that is Customer/Supplier); a consumer that
gets a bespoke shape.

**Cost.** The upstream can no longer change its interface freely, and must carry
versioning. In exchange it stops doing bespoke work per consumer.

**Misused as.** A synonym for "has an API". Every context will have an API. OHS
is the commitment to *one* published contract for *all* consumers.

### Published Language (PL)

**What.** A shared, documented interchange vocabulary at the border — often
paired with OHS, which then speaks it.

**Board evidence.** The same term with the same meaning inside several bubbles;
an industry or regulatory vocabulary appearing on stickies.

**Ruled out by.** Each consumer using its own word for the crossing object —
that is translation, so look at ACL or Customer/Supplier instead.

**Cost.** Governance. A published language that one team can change unilaterally
is not published.

**Misused as.** A label for the upstream's internal model. If it was not designed
for the border, it is not a published language; it is a leak.

### Customer/Supplier (C/S)

**What.** Two contexts, both under our control, with a genuine upstream and
downstream — and a downstream that has a *voice*: it can ask for what it needs
and expect it in the upstream's plan.

**Board evidence.** One object crossing, one consumer, both bubbles clearly
internal, and — usually — a policy sticky already drawn between them.

**Ruled out by.** A downstream with no negotiating power (Conformist), an
external upstream (Conformist/ACL), or many consumers (OHS).

**Cost.** Coordination and prioritization between two teams; a supplier that
never delivers turns its customer into a Conformist by default.

**Misused as.** The default for every internal edge. It is often right, but say
what the downstream actually negotiated, or admit it is Conformist.

**This is the honest default for two internal contexts on an EventStorming
board**, because the board shows one object crossing and rarely shows the
politics.

### Conformist (CF)

**What.** The downstream adopts the upstream's model wholesale, without
translation, because it cannot influence it and translation is not worth it.

**Board evidence.** A downstream cluster using the upstream's exact terms; an
external system whose concepts appear as-is on our stickies; a noun read by our
contexts and written by nobody on the board.

**Ruled out by.** Real translation happening at the border (that is an ACL),
or the downstream being free to model its own way.

**Cost.** Every upstream change lands directly in downstream code, and the
downstream's model is shaped by someone else's concerns forever.

**Misused as.** Laziness with a pattern name. Conformist is a legitimate,
deliberate choice for generic upstreams — a tax calculator, a mapping service —
and a bad one for anything near the core domain.

### Anticorruption Layer (ACL)

**What.** The downstream builds a translation layer so the upstream's model never
enters its own.

**Board evidence.** An external-system sticky whose vocabulary is *different*
from the surrounding cluster's; an object crossing whose attributes obviously
belong to someone else's domain (device ids, protocol codes, vendor statuses);
a legacy system the team already complains about.

**Ruled out by.** No model difference to absorb. Translating field names is not
corruption.

**Cost.** A layer to build and maintain, and one more place a change must land.
Usually worth it exactly once: at the border of the core domain.

**Misused as.** A shield drawn on every external edge by reflex. Name the model
difference it absorbs; if you cannot, the honest label is Conformist.

### Shared Kernel (SK)

**What.** Two contexts share a subset of the model — code and data — and both
may change it, with agreement.

**Board evidence.** The same business object *written* by two contexts. That is
the only board evidence, and it is more often a defect than a deliberate kernel.

**Ruled out by.** One-directional use (that is upstream/downstream), or two teams
who cannot coordinate cheaply.

**Cost.** The highest ongoing cost of any pattern: every change to the shared
part needs both parties, and the shared part grows unless actively resisted.

**Misused as.** The resolution of a contested-ownership finding. If two contexts
write one object, first try: one owner and the other reads it; or two models with
two names. Choose Shared Kernel only when both contexts genuinely need to enforce
the *same invariant*, and keep it small enough to name in a sentence.

### Partnership (PS)

**What.** Two contexts and two teams that succeed or fail together; joint
planning, coordinated releases.

**Board evidence.** Genuine mutual dependency: A writes something B needs *and* B
writes something A needs, with neither able to proceed without the other. Two
contexts appearing interleaved in the same phase of the timeline.

**Ruled out by.** A clean one-way crossing; or one side being able to release
independently.

**Cost.** Coordination on every release. It is the most expensive relationship in
the organization, and it is invisible in code — it lives in calendars.

**Misused as.** The pattern you pick when direction was never settled. On a
board, mutual dependency between two *adjacent* contexts usually means the
boundary is wrong: one aggregate has been split across it. Try moving the
boundary before accepting a Partnership.

### Separate Ways (SW)

**What.** No integration. The two contexts solve their problems independently,
even if that means duplicating something.

**Board evidence.** Two bubbles with no shared nouns at all — no object written
by one and read by the other, no shared actor.

**Ruled out by.** Any crossing.

**Cost.** Duplication, and the risk that someone integrates them later by
accident.

**Misused as.** Never drawn. Most maps connect everything, which makes the
connections meaningless. **Drawing a deliberate non-edge is a real result** —
it tells a team not to build an integration somebody was about to build.

### Big Ball of Mud (BBoM)

**What.** A region where models are mixed, boundaries are not enforced and rules
are inconsistent. Marked as a *region on the map*, not an edge.

**Board evidence.** Several bubbles sharing all their nouns; one hub context
touching everything with its neighbors speaking its vocabulary; a legacy system
that every cluster reads and writes.

**Ruled out by.** Clean ownership per noun.

**Cost.** None to draw, everything to ignore.

**Misused as.** An insult. It is a description with a strategy attached: do not
model inside it, wrap it, and put an ACL between it and anything new.

---

## 3. Choosing between them

Run the questions in this order per edge. Stop at the first that fits.

1. **Is there any crossing at all?** No → **Separate Ways**, drawn and labeled.
2. **Is the other side outside our control** (vendor, legacy, another company,
   off-board)? Then only the downstream chooses:
    - it must absorb a foreign model → **ACL**
    - the foreign model is acceptable and generic → **Conformist**
    - it does not need them → **Separate Ways**
3. **Is the same object written by both sides?** → contested ownership. Resolve
   first; only if both must enforce the same invariant is it a **Shared Kernel**.
4. **Is the dependency mutual under the change test?** → **Partnership**, or move
   the boundary. Prefer moving the boundary.
5. **Does the upstream serve three or more consumers with the same contract?** →
   **Open Host Service**, and its border vocabulary is the **Published
   Language**.
6. **One consumer, internal, with a voice?** → **Customer/Supplier**.
7. **One consumer, internal, with no voice?** → **Conformist**, and say so
   plainly — that is a finding about the organization.

**Ties worth knowing.**

- *OHS vs C/S*: count consumers in the term ledger. Two is a judgment call;
  three is OHS.
- *Conformist vs ACL*: is the upstream's model near our core domain? Near the
  core, wrap it. Far from it, conform and save the effort.
- *SK vs C/S*: does the downstream need to *enforce* a rule over the shared data,
  or merely to *read* it? Reading is Customer/Supplier with a replicated read
  model — almost always the cheaper answer.
- *Partnership vs moved boundary*: if the two contexts have the same actor and
  the same clock, they were probably one context.

---

## 4. Integration mechanisms and staleness

The pattern says who bends; the mechanism says how the bytes move and how stale
they are allowed to be. Name both, or the map cannot be built from.

| Mechanism | Board evidence | Staleness | Fits |
|---|---|---|---|
| **Domain event** | a policy sticky between two bubbles | seconds to minutes, unbounded on failure | C/S, OHS; anything the downstream can act on late |
| **Replicated read model** | a green read model in B holding A's noun | as long as the projection lag | frequent reads, tolerant decisions |
| **Synchronous query** | a command in B that cannot proceed without A's answer | none, but A's downtime is B's downtime | rare, decision-critical reads |
| **Batch / file** | end-of-day or periodic events | a business cycle | reporting, reconciliation, billing runs |
| **Human carries it** | an actor sticky appearing in both bubbles | hours to days | early-stage products; often the honest answer |
| **Shared database** | *(never on a board — ask)* | none, and no boundary either | this is not an integration; it is a merge |

**Write staleness in business words.** "Until the van does its round", "before
the guest checks out", "seconds". A window nobody can name is a border nobody
has agreed to, which is itself the finding.

**Always pair staleness with a compensating action**: what the business does
when a decision was made on stale data. If nobody can name one, the rule mattered
less than claimed — or nobody has hit it yet.

---

## 5. Writing a border contract

```
Rack management → Bicycle distribution        (upstream → downstream)
  Pattern      Customer/Supplier · implied
  Crosses      rack id · free slots · rack status · last known bicycle ids
  Stays        lock hardware state, unlock codes, vendor error codes,
               the rack's own firmware and telemetry
  Translation  their "dock" → our "slot"; their "occupied" → our "not available"
  Mechanism    replicated read model, refreshed on rack events
  Staleness    <n> minutes — a van driving to a full rack is the cost
  Failure      distribution falls back to the last known state and flags the
               rack as unverified; the mechanic corrects it on arrival
```

Rules that keep contracts honest:

- **Name fields, not concepts.** "The booking" hides the disagreement; *bicycle
  id · rack id · valid until* exposes it.
- **The "stays behind" line is the interface.** A border with nothing staying
  behind is not a border. If you cannot fill this line, the two contexts are one.
- **Translation both ways.** If nothing translates in either direction, challenge
  the boundary in the smells section.
- **One contract per edge, not per message.** Message-level detail belongs in an
  OpenAPI/AsyncAPI document, and those skills take this contract as input.

---

## 6. The smell catalog

| Smell | What you see | What it usually means | Resolution options |
|---|---|---|---|
| **Contested ownership** | one noun written by two contexts | a split aggregate | one owner + reader · two names, two models · merge |
| **Mutual dependency** | A ↔ B under the change test | boundary in the wrong place | move the boundary · accept Partnership and pay for it |
| **The hub** | one context on every edge | either identity/billing, or the system itself | check vocabulary sharing; if shared, it is a BBoM region |
| **The sink** | no outbound edges | reporting, or an unread output | find the consumer or mark the object an orphan |
| **The source** | no inbound edges | an unmodeled trigger | ask what starts it |
| **The orphan object** | written, never read | undrawn consumer, or a report | ask; never invent the reader |
| **The unwritten noun** | read by several, written by none | a missing or external context | draw it dashed and name its candidate owner |
| **The thin context** | one event, no object | not a context yet | fold into a neighbor, or argue for it |
| **The wide interface** | downstream needs live upstream detail to decide | the border will not survive implementation | snapshot at the border, or merge deliberately |
| **The system-shaped context** | a bubble named after a vendor, a table or a team | a component, not a capability | rename to the capability, or wrap as external |
| **The vocabulary vacuum** | neighbors share every term | there is no border | merge, or find the distinction the team means |
| **Everything connected** | no Separate Ways anywhere | edges drawn from adjacency | delete every edge without a crossing |

---

## 7. Reviewing a map somebody else drew

Do not redraw silently. Work in this order, and keep the three verdicts apart:

1. **Reconstruct** the map from the board alone, without looking at theirs.
2. **Agreements** — edges present in both, with the same direction. Say so
   plainly; it is the cheapest confidence a team can be given.
3. **Disagreements** — same edge, different direction or different pattern. Each
   one needs the board evidence that decides it, and an honest note when the
   board cannot decide it and the answer is organizational.
4. **Omissions, both ways** — edges they drew that the board cannot support
   (ask what they know that the board does not — often they are right and the
   board is thin), and crossings on the board they did not draw.
5. **Never delete an edge just because the board lacks evidence.** Teams know
   things the wall does not record. Mark it `not supported by the board` and ask.

---

## 8. Confidence, and what a board can never tell you

Mark every relationship:

- **`on the board`** — a policy sticky, an external-system sticky, an object
  written here and read there
- **`implied`** — the board makes no sense otherwise (a command that cannot work
  without data only another context holds)
- **`inferred`** — domain or organizational knowledge; must be confirmed

**A board shows models, not organizations.** These are invisible on every
EventStorming board and must be asked for, never guessed:

- which team owns each context, and whether two of them are the same team
- whether a downstream can actually influence its upstream (C/S vs Conformist is
  an organizational fact, not a modeling one)
- release cadence and coupling — the whole basis of Partnership
- which systems already exist and which are proposals
- contractual and regulatory obligations that force a Published Language

State this limit in the output rather than letting inferred org patterns read as
findings. A map that says "these four edges are model facts and these three are
guesses about your organization" is far more useful than one that looks certain
throughout.