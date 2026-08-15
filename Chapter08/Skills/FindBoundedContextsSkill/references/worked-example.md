# Worked example — a recruiting board (propose mode)

Input: a photographed Big Picture board, 22 orange events on one timeline with
small yellow actors and two red hotspots. No dividers drawn, so this runs in
**propose** mode.

Read this to calibrate depth and tone. Note especially §3, where the evidence
sits in the cells rather than ticks; §7, where the method admits what it cannot
see; and §8, where a strong candidate is rejected and the question that would
overturn the rejection is written out.

---

# Pivotal Event Cut — Recruiting board

## 1. Event line as read

| # | Event | Actor / system | Touches | Notes |
|---|-------|----------------|---------|-------|
| 1 | Headcount approved | Finance, Hiring manager | Headcount plan | Board starts here |
| 2 | Job opening drafted | Hiring manager, Recruiter | Job opening | |
| 3 | Job opening published | Recruiter, *Job board* (ext.) | Job opening | |
| 4 | Application received | Candidate, *Job board* (ext.) | Application | **loop** — many per opening |
| 5 | Application screened | Recruiter | Application | loop |
| 6 | Application rejected | Recruiter | Application | **branch** — feeds back to §7 |
| 7 | Candidate shortlisted | Recruiter | Application | loop |
| 8 | Interview scheduled | Coordinator, *Calendar* (ext.) | Interview | loop |
| 9 | Interview held | Interviewer, Candidate | Interview | **loop** — 2–5 rounds |
| 10 | Scorecard submitted | Interviewer | Scorecard | loop |
| 11 | Debrief held | Panel, Hiring manager | — | |
| 12 | Hiring decision made | Hiring manager | Application | 🔴 hotspot |
| 13 | Level and compensation set | Compensation, Hiring manager | Offer | |
| 14 | Offer approved | Finance, Head of dept. | Offer | |
| 15 | Offer sent | Recruiter | Offer | |
| 16 | Offer accepted | Candidate | Offer | |
| 17 | Background check cleared | *Screening vendor* (ext.) | Check | 🔴 hotspot |
| 18 | Contract signed | New hire, HR ops, *e-sign* (ext.) | Contract | |
| 19 | Start date agreed | HR ops, Hiring manager | Employment | |
| 20 | Equipment ordered | IT | Asset request | parallel with 21 |
| 21 | Accounts provisioned | IT, *Identity provider* (ext.) | Accounts | parallel with 20 |
| 22 | New hire onboarded | HR ops, Hiring manager | Employment | Board ends here |

Assumptions flagged: events 20–21 are drawn side by side and read as parallel;
event 6 has an arrow curving back toward the sourcing area, read as rejected
candidates re-entering the pool later.

## 2. Screening

| Removed | Why |
|---------|-----|
| 1, 22 | Board edges — scope boundaries, not internal borders |
| 4, 5, 7, 8, 9, 10 | Inside the per-candidate loop; a divider must be crossed once |
| 6 | Branch outcome, not a transition of the whole process |
| 11 | Meeting, no state change of its own; the decision at 12 is the fact |
| 20, 21 | Parallel provisioning steps inside one phase |

Survivors scored below: **3, 12, 14, 16, 17, 18, 19**.

## 3. Scoring

| # | Event | Phase | Irrev. | Handover | Language | Commit | Clock | Narrow interface | Verdict |
|---|-------|-------|--------|----------|----------|--------|-------|------------------|---------|
| 3 | Job opening published | planning a role → attracting people | unpublishing is a named action with its own approval | internal only → candidates + job boards | *Requisition* → *Job posting*; posting ID minted | public promise of a role and a band | instant → weeks of inbound | opening ID, title, requirements, location, band, manager — 6 stable fields | **Divider** |
| 12 | Hiring decision made | — same people continue | reversible: reopen the shortlist | none — hiring manager throughout | *Candidate* stays *Candidate* | internal intention only | none | offer prep reads scorecards, debrief notes, and the runner-up's dossier on decline | **Milestone** (vetoed) |
| 14 | Offer approved | — | approval can be re-run | Finance joins, then leaves | none | internal control | hours | offer terms only, but downstream is the same team | **Milestone** |
| 16 | Offer accepted | choosing someone → turning them into a colleague | withdrawal letter, legal exposure, notice already given | recruiting → HR ops + IT + payroll | *Candidate* → *New hire*; employee ID minted at 19 | mutual obligation; start date binds both | days–weeks of notice period | name, contact, role, level, comp, start date, manager, entity — 8 fields, never the scorecards | **Divider** |
| 17 | Background check cleared | — | re-run possible | external vendor answers, no ownership change | none | none | days | full identity data flows back to the same team | **Gate** |
| 18 | Contract signed | arguably the legal point of no return | rescission | HR ops throughout | *New hire* already in use since 16 | contract, but 16 already bound both sides | hours–days | same eight fields as 16 | **Contested** — see §8 |
| 19 | Start date agreed | — | reschedulable | HR ops → IT | employee ID minted here | firms up 16 | days | schedule only | **Milestone** |

## 4. The dividers

**#3 Job opening published.** The moment the process stops being about a plan
and starts being about people. Every test but the clock fires, and the interface
is a posting: the sourcing side never needs the headcount case, the budget
argument, or the org chart reasoning that produced the opening.

**#16 Offer accepted.** The point of no return. Both parties are bound, the
candidate is giving notice elsewhere, and the work moves to a set of people who
were not in the room for any of the interviews. Onboarding needs eight fields
and none of the evaluation.

## 5. Segments as candidate contexts

**Workforce Planning** (1–3) — decides which roles the company will pay for and
publishes them. Owns: Headcount plan, Job opening. Actors: Finance, hiring
manager, recruiter. Terms: *headcount*, *requisition*, *band*, *job opening*.

**Candidate Selection** (4–15) — attracts applicants and decides which one the
company wants, on what terms. Owns: Application, Interview, Scorecard, Offer.
Actors: recruiter, coordinator, interviewers, hiring manager, compensation.
Terms: *candidate*, *pipeline*, *shortlist*, *scorecard*, *debrief*, *offer*.

**Employment Onboarding** (17–22) — turns an accepted offer into a working
colleague. Owns: Contract, Employment, Asset request, Accounts. Actors: HR ops,
IT, new hire. Terms: *new hire*, *contract*, *start date*, *provisioning*.

Divider strip (the redraw spec):

| # | Event | Segment | Divider after? |
|---|-------|---------|----------------|
| 1–2 | … | Workforce Planning | |
| 3 | Job opening published | Workforce Planning | **yes** |
| 4–15 | … | Candidate Selection | |
| 16 | Offer accepted | Candidate Selection | **yes** |
| 17–22 | … | Employment Onboarding | |

## 6. Border contracts

**Workforce Planning → Candidate Selection**, on *Job opening published*.
Crosses: opening ID, title, requirements, location, compensation band, hiring
manager, target start. Stays upstream: the headcount business case, budget
approvals, org-chart reasoning, the internal-vs-external debate. Relationship:
customer/supplier — selection negotiates requirement changes back into the
opening. Consistency: minutes; nobody notices a lag.

**Candidate Selection → Employment Onboarding**, on *Offer accepted*.
Crosses: name, contact, role, level, compensation, start date, manager, legal
entity. Stays upstream: scorecards, debrief notes, sourcing channel, every other
candidate. Relationship: customer/supplier with an anticorruption layer —
onboarding's *New hire* is not selection's *Candidate*, and translating at the
border keeps the evaluation model out of HR. Consistency: hours; the notice
period gives days of slack.

## 7. What the timeline cannot see

- **Cross-cutting contexts** visible in the actors and systems but with no
  pivotal event of their own: **Identity & Access** (event 21, an identity
  provider serving the whole company), **Scheduling** (event 8, a calendar
  system used far beyond recruiting), **Background Screening** (event 17, an
  external vendor — a supplier, not a segment), and **Payroll**, which is
  downstream of 22 and off this board entirely.
- **Recurring context:** the arrow from event 6 means rejected candidates return
  to sourcing later. That is a **Talent Pool** capability supplying Candidate
  Selection over time — not a fourth segment on this timeline. It has no pivotal
  event because it never ends.
- **Straddling aggregates:** none clean, but *Offer* is worth watching. It is
  created at 13, decided at 16, and referenced by the contract at 18. Placed in
  Candidate Selection, which owns its lifecycle; Onboarding should hold the
  *terms* copied at the border, not a live reference to the Offer.

## 8. Contested calls & alternatives

**#18 Contract signed vs #16 Offer accepted.** Both are defensible; only one is
the border. Chosen 16, because the people change there and because by the time
the contract is signed the new hire is already resigning elsewhere — the
business behaves as though it is committed. The counter-argument is legal: until
signature there is no enforceable employment.

**#12 Hiring decision made — rejected despite the hotspot.** It scores on drama
and on nothing else. The same hiring manager continues, *Candidate* keeps
meaning what it meant, and offer preparation reads the whole evaluation dossier.
Vetoed by interface width. The red sticky here is a real disagreement about
*who decides*, which is a policy question inside Candidate Selection.

**Coarser cut:** drop divider 3 and merge planning into selection. Loses the
distinction between a role the company has funded and a role it is filling — the
distinction that matters when openings are frozen or reallocated.

**Finer cut:** split Candidate Selection at *Candidate shortlisted* into
sourcing and assessment. Rejected: same recruiters, same vocabulary, and
assessment constantly reads the application. Revisit if sourcing acquires its
own team and its own funnel metrics.

**What would settle it:**

1. If an accepted offer is withdrawn before signature, who owns the
   conversation — recruiting or HR ops? (Decides the 16-vs-18 call.)
2. Does the company ever fill an opening without publishing it — internal
   moves, direct approaches? (Would weaken divider 3 and reveal a second entry
   path into selection.)
3. Do rejected candidates ever get re-approached, and by whom? (Confirms or
   kills the Talent Pool context in §7.)

**Hotspots verbatim:** 🔴 "who actually decides — panel or HM?" (at 12);
🔴 "vendor takes 2 weeks, blocks start date" (at 17).