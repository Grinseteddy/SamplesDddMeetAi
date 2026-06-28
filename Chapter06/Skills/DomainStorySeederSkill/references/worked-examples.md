# Worked examples — seeding domain stories

Three complete passes, one per input type. Each shows the input, the candidate
focuses, the focus chosen, how the mining table was applied, and the resulting
seed. Pattern-match these; don't copy them literally.

---

## Example A — from a Wardley Map

**Input (described in text):**

> Anchor: *Homeowner wants a broken appliance fixed.*
> Value chain (visible → invisible):
> - Repair booking — *Custom-built*
> - Technician scheduling — *Custom-built*
> - Customer account — *Product*
> - Route planning — *Commodity (external service)*
> - Card payment — *Commodity (external service)*

**Candidate focuses offered:** the anchor ("get an appliance fixed"), Repair
booking, Technician scheduling, Customer account, Route planning, Card payment.

**Focus chosen:** the anchor's primary journey (the user declined to pick, so we
defaulted to the most story-worthy candidate and said so).

**Mining (how the table was applied):**
- Anchor → primary actor **Homeowner**, goal = appliance fixed.
- *Custom-built* components (Repair booking, Technician scheduling) → **human
  activities**: a Dispatcher does the scheduling by hand.
- *Commodity* components (Route planning, Card payment) → **external system
  actors**: "Routing service…", "Payment service…".
- The chain links → linking words ("from the Fault", "for the Repair job").

### Story 1 — Homeowner books an appliance repair *(coarse granularity)*

1. Homeowner reports a Fault on the Appliance via the Booking page
2. Homeowner picks a Time slot for the Visit
3. Booking system creates a Repair job from the Fault
4. Dispatcher assigns the Repair job to a Technician
5. Routing service adds the Visit to the Technician's Route
6. Technician inspects the Appliance at the Home
7. Technician fixes the Appliance
8. Technician closes the Repair job
9. Payment service charges the Customer for the Repair job
10. Homeowner receives a Receipt for the Repair job

### Story 2 — …but a spare part is missing *(variant)*

1. Technician finds the Appliance needs a Spare part
2. Technician orders the Spare part from the Supplier
3. Dispatcher pauses the Repair job
4. Dispatcher books a second Visit with the Homeowner

### For the session
- **Assumptions to confirm:** that a human Dispatcher assigns jobs (vs.
  auto-assignment); that the Homeowner and the paying Customer are the same
  person; that payment happens after the fix, not before.
- **Candidate groups:** *Booking* (1–3), *Scheduling & dispatch* (4–5),
  *On-site repair* (6–8), *Billing* (9–10).
- **Questions for the room:** what happens when the Homeowner misses the Visit?
  Who decides a job is "done"?

---

## Example B — from a Capability Map

**Input (described in text):** a motor-insurer capability map, levelled, with
markings:
- **Core:** Claims assessment, Fraud detection, Risk pricing
- **Supporting:** Policy administration, Document management
- **Generic:** Identity verification, Payments, Notifications

**Candidate focuses offered:** Claims assessment, Fraud detection, Risk pricing,
Policy administration, Document management (with a note that the *core* ones make
the richest seeds).

**Focus chosen:** **Claims assessment** (a core capability — the spine).

**Mining (how the table was applied):**
- Core capability *Claims assessment* → the **spine**: the Claims handler's
  review-and-decide activities carry the story.
- Generic capabilities (*Fraud detection, Payments, Notifications, Identity
  verification*) → **system actors** that serve the spine: "Fraud-check
  service…", "Payment service…", "Notification service…".
- Supporting capability *Document management* → a **work object** in play (the
  Photos / the Case file), not its own actor.

### Story 1 — Policyholder files a motor claim *(medium granularity)*

1. Policyholder submits a Claim with Photos of the Damage
2. Claims system opens a Case from the Claim
3. Fraud-check service scores the Case for risk
4. Claims handler reviews the Case with the Photos
5. Claims handler approves the Claim
6. Claims handler sets the Payout amount on the Case
7. Payment service transfers the Payout to the Policyholder
8. Notification service sends a Decision letter to the Policyholder

### Story 2 — …the damage needs an on-site inspection *(variant)*

1. Claims handler requests an Inspection for the Case
2. Assessor inspects the Vehicle at the Garage
3. Assessor records the Damage report on the Case
4. Claims handler reopens the review with the Damage report

### For the session
- **Assumptions to confirm:** that a human handler makes the approval (vs.
  straight-through auto-approval below a threshold); that fraud scoring runs
  before, not after, the handler's review.
- **Candidate groups (from capability levels):** *Intake* (1–2), *Assessment*
  (3–6), *Settlement* (7–8).
- **Questions for the room:** who handles a *rejected* claim, and how is that
  story different? Where does Risk pricing actually touch this flow?

---

## Example C — no artifact, just a one-line description

When no map is given, seed from the domain sentence the user provides. The
"input optional" path — no mining table, just clean Domain Storytelling grammar.

**Input:** "We run a small coworking space — seed a story about a new member
joining."

### Story 1 — A visitor becomes a member *(coarse granularity)*

1. Visitor tours the Space with the Host
2. Visitor chooses a Membership plan
3. Host creates a Member profile for the Visitor
4. Member signs the Agreement
5. Host issues an Access card to the Member
6. Member books a Desk for the day
7. Member checks in at Reception with the Access card

### For the session
- **Assumptions to confirm:** that the tour precedes signup; that the Host (a
  person) issues the card rather than a self-service kiosk.
- **Questions for the room:** is there an online signup path with no tour? What
  changes for a returning day-pass guest vs. a monthly member?

---

## Example D — tracing a downstream dependency to a fulfillment fork

This is the move from Step 2 that's easy to miss: don't stop at the focus
component — follow its links *downward*.

**Input (a recipe/cooking-platform Wardley Map, focus = "Ask Grandma/Grandpa"):**

> "Ask Grandma/Grandpa" sits in **Custom**, high (Visible). A line runs *down*
> from it to **Specific AI** (Custom), which depends on **Gen AI** (Commodity)
> and **Cloud** (Commodity).

**The trap:** read only the focus component and you get one story — a human
grandparent giving live advice over a call. **The fix:** the downstream link to
Specific AI means the same need ("ask an experienced cook") can be fulfilled two
ways — a **human mentor** *or* an **AI "Grandma" avatar**. That's a fulfillment
fork. The avatar is **domain-visible** (the cook talks *to* it), so it becomes a
**system actor**; Specific AI / Gen AI / Cloud stay off the page as plumbing.
Note too that "Grandma" is a *persona*, not the user's own kin — the dependency
is what tells you that.

### Story 1 — Home cook asks a Grandma avatar *(AI path)*
1. Home cook gets stuck on a Step of the Recipe
2. Home cook picks a Grandma avatar
3. Home cook asks the Grandma avatar about the Step
4. Grandma avatar explains the Technique to the Home cook
5. Home cook applies the Technique to the Dish

### Story 2 — Home cook asks a human Grandma mentor *(live path)*
1. Home cook requests a Grandma mentor for the Recipe
2. Platform matches the Home cook with a Grandma volunteer
3. Home cook shows the Step to the Grandma volunteer over a Remote meeting
4. Grandma volunteer explains the Technique to the Home cook

### Story 3 — …avatar can't help, hand off to a human *(escalation — the key variant)*
1. Home cook asks the Grandma avatar about the Step
2. Grandma avatar cannot answer the Question
3. Grandma avatar offers a human Grandma mentor to the Home cook
4. Home cook continues the Question with a Grandma volunteer over a Remote meeting

### For the session
- **The fork that matters:** is "Ask Grandma" human, AI avatar, or avatar-first
  with human escalation (Story 3)? That's the design decision the Specific AI
  dependency forces — validate it first.
- **Assumptions to confirm:** that "Grandma" is a persona, not the user's own
  family; that the avatar is a tuned persona (Specific AI) on top of Gen AI, so
  its voice is a product choice.

**Lesson:** a Custom human-activity perched on top of an AI/automation component
is the classic signature of a human-vs-system fork. Trace the link, surface both
paths, and make the escalation its own story.

---

## Recurring patterns to reuse


- **Evolution stage picks the actor type.** Genesis/Custom-built → a *named human
  role* doing it by hand. Product/Commodity → a *system actor* ("X service …").
  This is the single most useful Wardley→story move.
- **Core capability = the spine; generic capabilities = system actors.** The
  differentiating capability carries the human drama of the story; the commodity
  ones quietly serve it from off-stage.
- **One happy path per story; branches are separate stories.** Resist the urge to
  write "if". A second tiny story is always the right move.
- **Trace links downward from the focus component.** A Custom human-activity
  sitting on top of an AI/automation component signals a human-vs-system
  fulfillment fork; surface both paths and make the escalation its own story.
- **Name the uncertainty.** The "For the session" notes are not filler — the spots
  you flag as guesses are precisely where the workshop earns its value.