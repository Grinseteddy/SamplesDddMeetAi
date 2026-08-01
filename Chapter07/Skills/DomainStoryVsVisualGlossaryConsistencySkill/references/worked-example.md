# Worked example — a bicycle-sharing glossary against three stories

A full pass, transcription through report. The inputs are deliberately ordinary:
a glossary someone tidied up after a workshop, and three stories drawn on three
different days by three different pairs. Every finding type appears at least
once. Read this to calibrate tone, density, and how much of the ledger to show.

---

## Inputs as transcribed

### Glossary (Step 1)

**Contexts → terms**

| Context | Terms |
|---|---|
| User Management | Tourist, Commuter |
| Partner Management | Hotel |
| Bicycle Distribution | Bicycle |
| Bicycle Management | Bicycle |
| Rack Management | Rack, Code, Lock |
| Accounting | Monthly Fee |
| *(ungrouped)* | Fee, Station Public Transport |

**Relationships**

```
Tourist  —pays→        (no cardinality) Fee
Hotel    —charges→     0..*  Fee
Tourist  —drives→      (no cardinality) Bicycle
Commuter —drives→      (no cardinality) Bicycle
Hotel    —stores→      0..*  Bicycle
Bicycle (Management) —relates to→ 1 Bicycle (Distribution)
Rack     —stores→      0..1  Bicycle
Rack     —located at→  0..1  Station Public Transport
Rack     —contains→    1..*  Lock
Code     —unlocks→     1     Lock
Code     —locks→       (no cardinality) Bicycle
Commuter —uses→        (no cardinality) Code
Commuter —pays→        1..*  Monthly Fee
```

Glossary-internal notes: the glossary includes actor stickies, so actor names are
in scope for checking (guard G4). `Bicycle` appears in two contexts related
`1`-to-`1` — plausibly the same bicycle modelled twice on purpose.

### The stories (Step 1)

**Story A — "Tourist rents a bicycle for an afternoon"** · as-is, digitalized ·
lane: *Rental*

```
1. Tourist registers App
2. Tourist pays Fee via App
3. App sends Code to Tourist
4. Tourist unlocks Lock with Code at Stand
5. Tourist drives Bike to Museum
6. Tourist returns Bike to Stand
```

**Story B — "Commuter's morning ride"** · as-is, digitalized · lanes: *User
Management*, *Billing*

```
1. Commuter registers App
2. Commuter pays Monthly fee via App
3. Commuter unlocks Bicycle with Code at Rack
4. Commuter drives Bicycle to Office
5. User returns Bicycle to Rack
```

**Story C — "Hotel partner service"** · **to-be** · lane: *Partner Management*

```
1. Hotel orders Bicycles from Operator
2. Mechanic brings Bicycles to Rack
3. Mechanic repairs Bicycle
4. Hotel charges Fee to Guest
```

---

## The report

# Consistency Report — 3 stories × Bicycle-sharing Visual Glossary

## 0. Scope checked

| Story | Scope | Lanes |
|---|---|---|
| A — Tourist rents a bicycle | as-is, digitalized | Rental |
| B — Commuter's morning ride | as-is, digitalized | User Management, Billing |
| C — Hotel partner service | **to-be** | Partner Management |

Glossary: 10 terms across 7 contexts, 13 relationships. All elements legible;
nothing left unchecked. Story C being to-be means its new vocabulary is treated
as glossary work, not story defects (guard G5).

## 1. Verdict

**Diverged.** The three stories do not currently speak one language, and two of
them assert things the glossary forbids.

2 Blocking · 6 Significant · 4 Minor. By story: A — 5, B — 3, C — 6 (three
shared). 9 of 10 glossary terms exercised; 10 of 13 relationships exercised.

## 2. Term ledger

| Glossary term | Story A | Story B | Story C | Status |
|---|---|---|---|---|
| Tourist | Tourist | — | *Guest?* | ✗ mismatch in C |
| Commuter | — | Commuter / **User** | — | ✗ mismatch in B(5) |
| Hotel | — | — | Hotel | = |
| Bicycle | **Bike** | Bicycle | Bicycles | ✗ mismatch in A |
| Rack | **Stand** | Rack | Rack | ✗ mismatch in A |
| Code | Code | Code | — | = |
| Lock | Lock | — | — | = |
| Fee | Fee | — | Fee | = *(but see F1)* |
| Monthly Fee | — | Monthly fee | — | ~ casing |
| Station Public Transport | — | — | — | — unused |

**Story-only nouns — domain terms missing from the glossary**

| Noun | Where | Note |
|---|---|---|
| Mechanic | C(2), C(3) | Actor who racks and repairs bicycles |
| Operator | C(1) | Whoever the Hotel orders from — the company itself? |
| Guest | C(4) | Probably the glossary's `Tourist`; possibly a fourth party |

**Out of glossary scope** — `App` (UI channel, G2), `Museum` and `Office` (scene
props, G3).

## 3. Findings

**F1 · `SENSE` · Blocking** — `Fee` means two different things. In Story A(2) it
is a one-off charge a Tourist pays at rental time; in Story C(4) it is what a
Hotel invoices, alongside a glossary that already separates a recurring `Monthly
Fee` but attaches it only to Commuters. So the glossary splits fees one way
(one-off vs monthly) and the stories split them another (rider vs partner), and
the word `Fee` currently spans both splits. A homonym in the ubiquitous language
is worse than a synonym: nobody notices it until two teams have built
incompatible things and both believe they implemented "Fee." *Ask:* how many
kinds of charge exist — rental fee, subscription, partner invoice? Name each one
distinctly in the glossary.

**F2 · `CARD` · Blocking** — Story C(2) has a Mechanic bringing several
`Bicycles` to one `Rack`; the glossary caps `Rack —stores→ 0..1 Bicycle`. A rack
that holds one bicycle is also hard to square with `Rack —contains→ 1..* Lock`,
which implies many berths. *Glossary moves:* almost certainly `0..*`. The story
records what people do; the cardinality records what someone remembered in a
workshop, so the story is the stronger evidence here. Confirm, then fix the
glossary rather than the story.

**F3 · `DRIFT` · Significant** — Story A says `Bike` and `Stand` where Story B
says `Bicycle` and `Rack` for the same things. The finding is not really two
words: it is that Story A was told outside the glossary's reach, and every noun
it shares with the glossary is at risk. *Story moves:* rename both in Story A —
then re-read A for anything else that drifted.

**F4 · `TERM` · Significant** — Story B(5) says `User` where B(1)–(4) say
`Commuter` and the glossary says `Commuter`. Inconsistent inside a single story,
which usually means a late edit. *Story moves,* unless `User` was meant to cover
tourists and commuters alike — in which case the glossary is missing that
supertype, which would be a more interesting finding than the typo. Worth one
question.

**F5 · `UNDEF` · Significant** — Story C names three actors the glossary does
not define: `Mechanic` (2, 3), `Operator` (1), `Guest` (4). Story C is to-be, so
these are *glossary moves*, not story defects. `Guest` is likely the glossary's
`Tourist` seen from the hotel's side — if so, rename in the story; if a hotel
guest is a genuinely different party with a different billing relationship, the
glossary needs the term. `Mechanic` is the notable one: an entire operations role
appears in the stories and nowhere in the domain model.

**F6 · `REL-X` · Significant** — Story C implies two structural relationships the
glossary cannot express: `Mechanic repairs Bicycle` (3) — there is no maintenance
edge, and no way to say a Bicycle has a condition or is out of service — and
`Hotel orders Bicycles from Operator` (1) — no supply relationship. *Glossary
moves:* propose `Mechanic —maintains→ 0..* Bicycle` and a supply edge, with those
cardinalities marked as guesses.

**F7 · `CTX` · Significant** — Story B's lane is `Billing`; the glossary's
context for `Monthly Fee` is `Accounting`. Same subdomain, two names — the sort
of divergence that quietly becomes two modules. Also worth a question: the
glossary splits `Bicycle Distribution` from `Bicycle Management`, but no story
distinguishes them, so that `1`-to-`1` relationship is currently untested by any
told scenario.

**F8 · `UNUSED` + `REL-0` · Minor** — `Station Public Transport` is never used by
any story, and with it `Rack —located at→ 0..1 Station Public Transport`. Racks
being at transport hubs sounds like the heart of a commuter service, so the more
likely reading is a **missing story**, not dead vocabulary.

**F9 · `REL-0` · Minor** — `Code —locks→ Bicycle` is never exercised. Stories A(6)
and B(5) both *return* a bicycle, which surely involves locking it, but neither
names the Code. Either the return step is under-told or the relationship belongs
to the Lock rather than the Bicycle.

**F10 · Minor, grouped** — `Monthly fee` → `Monthly Fee` (casing, B2);
`Bicycles` → `Bicycle` (plural, C1, C2); `Tourist —pays→ Fee`, `Tourist —drives→
Bicycle`, `Commuter —uses→ Code` carry no cardinality in the glossary, so nothing
in the stories can contradict them — noted as unverifiable rather than
consistent.

## 4. Relationships & cardinalities

| Glossary relationship | Card. | Exercised by | Verdict |
|---|---|---|---|
| Tourist —pays→ Fee | — | A(2) | consistent (unbounded) |
| Hotel —charges→ Fee | 0..* | C(4) | consistent |
| Tourist —drives→ Bicycle | — | A(5) | consistent |
| Commuter —drives→ Bicycle | — | B(4) | consistent |
| Hotel —stores→ Bicycle | 0..* | — | never exercised |
| Bicycle(M) —relates to→ Bicycle(D) | 1 | — | never exercised |
| Rack —stores→ Bicycle | 0..1 | A(6), B(5), C(2) | **contradicted — F2** |
| Rack —located at→ Station PT | 0..1 | — | never exercised — F8 |
| Rack —contains→ Lock | 1..* | A(4) | consistent |
| Code —unlocks→ Lock | 1 | A(4) | consistent |
| Code —locks→ Bicycle | — | *implied only* | never exercised — F9 |
| Commuter —uses→ Code | — | B(3) | consistent |
| Commuter —pays→ Monthly Fee | 1..* | B(2) | consistent |

**Implied but undefined:** Mechanic → Bicycle (maintenance), Hotel → Operator
(supply), Bicycle → condition/service state.

## 5. Bounded contexts

| Glossary context | Story lane | Note |
|---|---|---|
| User Management | B: User Management | aligned |
| Accounting | B: Billing | **name mismatch — F7** |
| Partner Management | C: Partner Management | aligned |
| Rack Management | A: Rental *(partly)* | Story A's single lane spans rental and rack management |
| Bicycle Distribution / Management | — | no story distinguishes them — F7 |

## 6. Patch list

**Stories**
- A(4), A(6): `Stand` → `Rack`
- A(5), A(6): `Bike` → `Bicycle`
- B(2): `Monthly fee` → `Monthly Fee`
- B(5): `User` → `Commuter` *(pending F4)*
- C(1), C(2): `Bicycles` → `Bicycle`
- C(4): `Guest` → `Tourist` *(pending F5)*
- New story: a Rack at a Station Public Transport *(pending F8)*

**Glossary**
- `Rack —stores→ Bicycle`: `0..1` → `0..*` *(pending F2)*
- Split `Fee` into distinctly named concepts *(pending F1)*
- Add `Mechanic` + `Mechanic —maintains→ 0..* Bicycle`
- Add the supply relationship behind `Hotel orders Bicycles`
- Add a service-state concept for `Bicycle` if repairs are tracked
- Decide whether `Guest` and `User` are distinct parties or aliases
- Add cardinalities to the five relationships that carry none

## 7. Already aligned

Story B is clean against the glossary apart from one late-edit `User` and a
capital letter — it is the model the other two should be brought to. `Code`,
`Lock`, and the unlock sequence are consistent everywhere they appear, and the
glossary's `Rack —contains→ 1..* Lock` is exactly borne out by A(4). Every actor
in Stories A and B is a glossary actor. Story C's divergence is almost entirely
*forward* — new vocabulary for a service that does not exist yet — which is the
healthy kind.

---

## What this example is calibrated to show

- The **ledger carries the exhaustiveness** so the prose doesn't have to. Ten
  rows, four glaring, and the reader can see nothing was skipped.
- **`DRIFT` is reported as one finding about Story A**, not six word-level ones.
  The pattern is the insight.
- **`CARD` resolves toward the glossary**, `TERM` resolves toward the story.
  Split authority in practice.
- **Guards did real work**: `App`, `Museum`, `Office` sat in a two-line
  out-of-scope note instead of becoming three findings, and Story C's to-be scope
  turned nine potential defects into a glossary backlog.
- **Section 7 is not padding.** "Bring A and C to look like B" is the most
  actionable sentence in the report, and it only exists because the aligned
  parts were checked too.