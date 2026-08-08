# Cut signals — where a domain story divides

Nine signals, ordered by how much weight one of them can carry alone. Each entry
gives the diagnostic question, what counts as evidence *inside a domain story*
(you only have actors, verbs, work objects, prepositions, order and annotations
— so the evidence has to be visible there), how much the signal proves by
itself, and how it misleads.

A working rule: **two signals to keep sentences together, three to draw a
boundary between them.** One signal is a hypothesis, not a cut.

---

## 1. Language shift — the same word, a different model

**Ask:** does this noun mean the same thing here as it did three sentences ago?
Would a domain expert reach for a different set of attributes, a different
lifecycle, a different question to ask about it?

**Evidence in the story:** the same work-object pictogram recurring in sentences
whose surrounding verbs imply different concerns. *Bicycle* + *brings to Rack*
implies condition, location, roadworthiness. *Bicycle* + *books* implies
availability, reservation window, price. *Bicycle* + *drives home with* implies
custody and a trip. Also: the same word appearing with different qualifying
prepositions (`at Public transport station` vs `at Rack near Home`), and
adjectival variants (*New task*, *Assigned task*, *Task done*) which mark
**states within one context** rather than separate contexts — do not confuse the
two.

**Strength:** the strongest signal there is. Evans defines the boundary this
way: a bounded context is exactly the span within which a term has one
unambiguous meaning. A boundary supported by a genuine language shift rarely
turns out wrong.

**Misleads when:** you mistake a *state* for a *sense*. A Task moving from
assigned to done is one model in one context. The test: does the change alter
which attributes matter and which questions get asked, or only the value of a
status field? Also beware of inventing a distinction to justify a boundary you
already wanted — write down the two attribute sets and see whether they really
differ.

---

## 2. Purpose change — the business reason for the step

**Ask:** why does this step exist at all, said in the domain's words? Not what
it does — what it is *for*.

**Evidence:** the answers cluster. "So there are bikes where people need them"
covers racking, searching, returning. "So the company gets paid" covers the
monthly fee. "So a rider can physically get the bike out" covers codes and
locks. Steps whose purposes are answered by the same sentence belong together
even when they sit far apart in the sequence.

**Strength:** high, and it is the signal that produces good *names* — the
purpose sentence usually contains the lane name already.

**Misleads when:** the purpose is phrased at the wrong altitude. "So the
customer is happy" covers everything and cuts nothing; "so the code field gets
populated" is implementation. Aim for the altitude a domain expert would use
when explaining their job to a new colleague.

---

## 3. Actor / expertise change

**Ask:** is a different kind of person — different role, department, training,
or system — involved? If you needed to check this step, would you have to ask a
different expert?

**Evidence:** a change of actor pictogram (Mechanic vs Commuter); a software
system acting where a person acted; the same actor name in two positions of a
sentence (two role *instances*, e.g. *Craftsman assigns task to Craftsman* —
manager and assignee).

**Strength:** moderate. A new actor is a good reason to look for a boundary, not
a reason to draw one. Crucially, the inverse is weak: **one actor threading
through the whole story does not mean one context.** A Commuter registers, pays,
books, unlocks, rides and returns — six steps, several contexts, one person.
Stories where a single actor does everything are common and prove nothing.

**Misleads when:** the story has collapsed several roles into one generic
"User", hiding real boundaries; or when a specialist actor appears for a single
step that genuinely belongs to a neighbouring context.

---

## 4. Handoff / pivotal moment

**Ask:** could the story plausibly pause here? Does responsibility, custody,
ownership, or authority pass from one party to another?

**Evidence:** possession changing (a bike leaves the rack with a rider); an
approval, payment, or confirmation completing something; a step that a domain
expert would describe as "and then it's out of our hands." In EventStorming
terms these are the pivotal events, and boundaries like to sit next to them.

**Strength:** moderate-to-high, and unusually useful because it also tells you
the *direction* of the relationship in the context map — the handing-off context
is usually upstream.

**Misleads when:** every step is a small handoff. Look for the ones where a
domain expert would accept a delay of hours or days without alarm; a pause the
domain tolerates is a boundary you can afford.

---

## 5. Work-object cohesion

**Ask:** which nouns keep turning up together?

**Evidence:** build the co-occurrence set per sentence and cluster. *Code* +
*Rack* + *Bicycle* recurring together is a cluster. Nouns appearing in one
sentence only are weak evidence; nouns appearing in *every* sentence are not
context markers at all — they are the shared concept the contexts each model
differently, and they are the reason signal 1 exists.

**Strength:** moderate. It is mechanical, which makes it a useful cross-check on
a cut you formed for other reasons — but it is blind to meaning, so it will
happily group everything touching *Bicycle* into one lane.

**Misleads when:** used first. Cohesion alone drives you straight into the
entity-shaped-context trap.

---

## 6. Ownership and lifecycle

**Ask:** which step *creates* this thing, which merely reads or references it,
and which retires it?

**Evidence:** the verb tells you — *registers*, *books*, *gets* create; *unlocks
with*, *confirms via* reference. The creating step's context normally owns the
model and publishes it; referencing contexts hold an identifier or a projection
of it, and their version can be much thinner.

**Strength:** moderate, and it is what makes the context map concrete: the owner
is upstream, the referencer is downstream.

**Misleads when:** creation is off-story. Plenty of things a story references
were created in a scenario nobody drew — record that as an open question rather
than assigning ownership to whichever lane happens to touch it first.

---

## 7. Rate of change / volatility

**Ask:** do these steps change on the same business clock?

**Evidence:** pricing, promotions and fee rules change monthly; lock hardware
and rack topology change rarely; registration and identity change when
regulation does. Steps forced to change together should be together; steps that
change for unrelated reasons are being coupled for no return.

**Strength:** low as evidence *in the story* (a story rarely shows time), but
high as a sanity check once you have a candidate cut. Ask the person directly.

**Misleads when:** you guess. Volatility is domain knowledge, not diagram
content — if the person cannot confirm it, mark it as an assumption.

---

## 8. Consistency need

**Ask:** what genuinely has to be true at the same instant, and what can settle
a second (or an hour) later?

**Evidence:** a booking and the availability it consumes must agree immediately
or two riders get one bike. A monthly fee and a ride can reconcile later. Steps
with a hard atomic requirement resist being split; steps that tolerate lag mark
an affordable boundary.

**Strength:** moderate, and it is the signal most likely to *veto* an otherwise
attractive cut.

**Misleads when:** you apply a technical instinct instead of a business one. The
question is what the domain requires, not what a single transaction would be
convenient.

---

## 9. Organisational seam

**Ask:** do different teams, departments, vendors, or systems of record already
own these steps?

**Evidence:** stated in the story (an external system as actor), in annotations,
or told to you by the person.

**Strength:** real and practical — Conway's law means a boundary that fights the
organisation will be re-fought forever. But it describes the *current* shape,
which may be the thing the redesign is meant to change. Weigh it; do not obey it.

**Misleads when:** the org chart is treated as the answer. Note the alignment,
name where the ideal cut and the current org disagree, and let the person decide
which one moves.

---

## Anti-signals — things that look like boundaries and are not

- **Sequence position.** Steps being adjacent in the numbering means only that
  they happen in that order.
- **Screens and channels.** Everything happening "via App" is not a context;
  the app is the surface the domain acts through.
- **A change of pictogram.** A house icon appearing does not start a new
  subdomain.
- **Verb tense or phrasing.** *Confirms* vs *gets* is grammar, not a boundary.
- **Story length.** A long lane is not automatically two contexts, and a lane
  holding one sentence is not automatically wrong (see
  `naming-and-granularity.md`).

---

## Trap catalogue — check every cut against this before publishing

| Trap | How it shows | The fix |
|---|---|---|
| **Timeline slicing** | Every lane is a contiguous run of numbers | Re-run signal 1; look for the recurrence you missed. Cuts where nothing recurs are usually phases, not contexts |
| **One lane per step** | Nearly as many lanes as sentences | Merge on shared purpose; a lane is a responsibility, not an action |
| **Entity-shaped context** | A lane named for a noun holding every sentence that touches it | Split by what is *done* with the noun; each sense of it belongs to a different lane |
| **CRUD / technical lanes** | *Data entry*, *Validation*, *Reporting*, *Notifications* | These are layers. Ask what business outcome each step serves and re-cut on that |
| **Channel as context** | *App*, *Web*, *Mobile*, *Frontend* | Delivery surface. Note it as the channel attribute of the step instead |
| **The god lane** | One lane holds most of the story | Look for purpose changes inside it; it is usually two or three lanes that share an actor |
| **Org-chart copy** | Lanes match the current team names exactly | Fine if the evidence agrees; suspicious if it was the starting point. Check signals 1–2 independently |
| **Invented steps** | The cut needs a step the story never told | Move it to open questions. Never draw a sentence the domain expert did not say |
| **Glossary drift** | Lane names use words the team does not | Reuse the team's terms, especially any Visual Glossary's exact wording |