# Prompt 2026-09-13

Decide if Larder should use a choreography or orchestrator approach for UI.

[Domain Knowledge Graph](../../assets/larderEnahnced4.ttl)

# Answer

## Recommendation: hybrid, not either/or — with orchestration as the default inside a context and choreography at the seams

Larder's own knowledge graph already tells you which parts of the UI need a conductor and which parts need to just listen. Trying to pick one style project-wide would fight the domain shape the artifacts describe.

### Where the graph argues for **orchestration**

**Inside `Meal Preparation`.** The command `Prepare meal` fans out into seven distinct events (start → step unclear → catastrophe → step completed → rescue → prepared) with hard sequencing rules — INV-PREP-04 blocks "Meal prepared" while steps are open, INV-PREP-07 blocks "Meal rescued" unless the preparation is actually `In catastrophe`. That's a linear, stateful workflow with guards. A choreographed UI (independent widgets each reacting to whichever event shows up) would have no natural place to enforce "you can't confirm the rescue before a response exists." A stepper/wizard driven by one orchestrator that knows the current state of the (still-unnamed, but clearly needed) Meal Preparation aggregate is the natural fit — and it's literally what the prototype readme built: "S13–S14 Start prep / Steps," "S16 refused while any step is open."

**Inside `Meal Planning`.** Same shape: search → select → (branch: ingredients missing → help → substitute) → settle, with INV-PLAN-03/04 gating the settle step on menu-non-empty and no outstanding gaps. Another orchestrated flow.

**The recipe-competition / rating flow** in the Competition story is also a strict sequence (plan → select recipe → register → rate → crown) with a single administrator driving state transitions — orchestration again.

### Where the graph argues for **choreography**

**Cooking Assistance / Cooking Help.** This is the graph's own headline finding, repeated independently by three different analyses in the project and now confirmed by SADR0004: it's *one* context, drawn twice, triggered from two unrelated moments (a stalled plan, a mid-cook catastrophe). The prototype readme treats it exactly this way — "one set of slices, reachable from either trigger... not two screens." That's choreography by construction: the Help UI doesn't know or care which upstream screen fired the trigger event; it just reacts to `Ingredients missing` / `Meal planning stalled` / `Step unclear` / `Catastrophe happened` wherever they occur, and its own `Help provided` event is picked up independently by whichever context asked (INV-PLAN-04-adjacent settling logic, or the Meal Preparation rescue guard).

**Notification.** Its whole canvas exists to react to events crossing bounded-context borders (`Help requested`, `Help provided`) with no command of its own — a pure listener. Orchestrating it (having some central flow explicitly "call" Notification) would invert its actual role.

**Grandma Avatar AI.** Wired behind an ACL, translating events in and out (`Help request` in, `Help response` out). It's a reactive translator, not a step some orchestrator invokes on a schedule — and the graph explicitly worries about whether it's told or whether it watches (`Q_WhoDecidesAvatarAnswers`), which is itself a choreography question: does it subscribe to events, or does something call it?

**Meal Preparation ↔ Sharing ↔ Media handoffs.** `Meal prepared` triggering `Take pictures` triggering (eventually, unmodelled) `Thanks given` crosses three contexts with no single owner and genuinely loose consistency ("minutes to hours... nobody is upset if thanks lag the meal," per the border contracts). Forcing an orchestrator to walk this chain would recreate coupling the domain doesn't have — the canvases themselves record `Q_WhatTriggersThanksGiven`: *nothing on the map currently triggers it*, which is a choreography gap, not an orchestration gap.

### The one place this cuts against a naive choice: recurring vs. one-off

The reason a single project-wide orchestrator would actively hurt you here is the *recurring* nature of Cooking Assistance. An orchestrator (a top-level flow script) has to name its steps explicitly — step 1 is Planning, step 2 is Help, step 3 is Preparation. But Help isn't a step in a sequence; it's a service two different sequences call into and get interrupted by, asynchronously, at unpredictable points, with an unconfirmed 🤖-marked automation folded in on one side (`Meal rescued`). That's the textbook case where choreography avoids a combinatorial explosion of "what if help arrives during step 4 vs step 7."

### Practical shape for the UI architecture

- **Per-context local orchestration**: Meal Planning wizard, Meal Preparation stepper, Competition flow — each owns its own local state machine and UI sequencing, matching the state machines already reconstructed in the invariants sheet.
- **Cross-context choreography via events**: Cooking Assistance, Notification, Grandma Avatar AI, and the Preparation→Media→Sharing chain all subscribe/react rather than being called. This also matches how the Bounded Context Canvases model these edges as async messages (`evt`/`qry`), not synchronous RPC.
- **Resolve the two open gaps before building either half**: `Q_WhatTriggersThanksGiven` (Sharing has no inbound event today) and `Q_AvatarOwnsOrHoldsCopy` (does the avatar own state or just react to a copy) are both choreography design questions the graph flags as unanswered — worth settling with the team before wiring the reactive layer, since they determine whether Sharing needs a new subscribed event and whether the avatar needs any state at all.