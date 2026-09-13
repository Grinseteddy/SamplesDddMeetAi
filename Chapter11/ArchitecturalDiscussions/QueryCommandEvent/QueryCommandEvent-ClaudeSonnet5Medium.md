# Prompt 2026-09-13

Check where to use Commands and queries between Bounded Contexts. Use the domain knowledge graph.

[Domain Knowledge Graph](../../assets/larderEnahnced4.ttl)

# Answer

## Where Commands belong on this border — audit against the graph

I pulled every `dkg:Message` triple in `larderEnahnced4.ttl` (all nine Bounded Context Canvases, ingest 9). The headline finding is blunt:

**26 cross-context messages are typed in the graph. 20 are `"qry"`, 4 are `"evt"`, 2 are `"?"`. Zero are `"cmd"`.**

That's not a coincidence of how the canvases happened to be drawn — it's a real gap, and the graph itself flags two of the places it bites (`Q_WhatCrossesToConsentManagement`, the "?" typing left un-resolved on both Consent messages). Below is the full audit, then what I think the fix is.

### The rule I'm applying
- **Command** — an imperative crossing the border asking the *receiving* context to change its own state. Flows toward whichever context owns the data.
- **Query** — a read-only ask; receiver's state is untouched; sender uses the answer locally.
- **Event** — a fire-and-forget fact already committed, published by the owner; no reply expected, receiver reacts on its own schedule.

### Full border inventory

| Border | Current | Direction | Verdict | Why |
|---|---|---|---|---|
| Cook Profile → Consent Management, *Consent* | `?` ×2 | CP → CM | **Should be Command** (`RecordConsent`) | The canvas's own open question — *"consent, or the cook?"* — is really asking *is this a write or a read*. `Rel_CookGivesConsent` ("Cook **gives** Consent") is a verb of action, not a lookup. Consent Management's job description is "records what a cook has agreed" — recording is a state change owned by CM, triggered from CP. This is the clearest missing Command on the whole graph. |
| Sharing → Consent Management, *Consent* (×2) | `qry` | Sharing → CM | **Correct as Query** | "May this be shown?" is a read against CM's own record; Sharing changes nothing in CM. `Msg_Out_ConsentManagement_Consent_Sharing` is even annotated "CF — Sharing conforms," a Conformist read pattern, which is query-shaped by definition. |
| Cooking Assistance → Grandma Avatar AI, *Help request* (via ACL) | `evt` | CA → GA | **Should be Command**, not Event | This isn't CA publishing a fact for GA to notice whenever — CA is *delegating work*: "answer this." The synchronous reply (`Msg_Out_..._HelpResponse_GA`) only makes sense as the result of a directed request. Typing it `evt` implies GA merely observes; in fact GA is instructed. Recommend `cmd` in, with the response modeled either as the command's synchronous result or as `evt` on the way back only (request=cmd, reply=evt/data — not evt both ways). |
| Cooking Assistance → Meal Planning, *Menu / Ingredients / Recipe* | `qry` | CA → MP | **Correct** | CA is building a situation snapshot to hand to a responder; nothing in Meal Planning changes. |
| Meal Preparation → Meal Planning, *Recipe / Menu* (both directions) | `qry` | MPrep ↔ MP | **Correct** | Same shape — MPrep reads the settled plan; MP doesn't act on it. |
| Meal Planning → Recipe Catalog, *Recipe* | `qry` | MP → RC | **Correct** | RC is the conformist supplier everyone reads; nobody ever writes recipes across this border. |
| Meal Planning → Cooking Assistance, *Help response* | `qry` | MP → CA | **Correct, but see gap below** | MP reads the response to use in `Substitute ingredients` — a local write inside MP's own aggregate, not a write into CA. |
| Meal Preparation → Cooking Assistance, *Help response* | `qry` | MPrep → CA | **Correct, but see gap below** | Same shape, for the rescue. |
| Cooking Assistance → Media, *Pictures* | `qry` | CA → Media | **Correct** | Attaching evidence — a read. |
| Sharing → Media, *Pictures* (both directions) | `qry` | Sharing ↔ Media | **Correct** | Building the post from stored pictures — a read. |
| Cooking Assistance → Notification, *Help request / Help response* | `evt` | CA → Notif | **Correct — the one clean case** | Notification is a pure listener; nothing flows back. This is what an event border should look like, and it's the only one on the graph typed right by construction. |
| Sharing → Meal Preparation, *Help response* | `qry` | Sharing → MPrep | **Correct as a query, wrong destination** | `Con_HelpResponse_ES dkg:inContext :Ctx_CookingAssistance` — the object is owned by CA, not MPrep. Six canvases each independently route through whichever neighbor is closest instead of the owner. Not a cmd/qry error, but worth fixing alongside this: it should be `Sharing → Cooking Assistance`. |

### The missing Commands (not `?`, not mistyped — just absent)

The invariants sheet already named the gaps that a cross-context Command would close, but the graph shows no message for any of them:

- **"Mark the resolving Help response."** INV-HELP-06 needs exactly one response marked as the one that fixed things, and nothing on any canvas writes that back into Cooking Assistance. Whoever consumes the response — Meal Planning (after substituting) or Meal Preparation (after the rescue) — needs a **Command** like `MarkHelpResolved` sent *into* CA, not just a `qry` read *out of* it. Today the read-only borders let MP and MPrep *use* an answer without ever telling CA the loop closed — which is exactly why INV-HELP-09 ("Answered is not reopened") has nothing to enforce it.
- **Withdraw / timeout a Help request.** Local to Cooking Assistance, not a border message — flagging only so it isn't confused with the above.

### Bottom line
- The **qry borders are sound** — every read-only lookup here really is read-only; I don't see a query mislabeled as safe when it's actually mutating.
- The **two `?` on Consent** are the one place the canvas author already sensed this and left it open — resolve as **Command**.
- The **Grandma Avatar `evt`** undersells what's actually a directed request for computation — I'd retype the inbound leg as **Command**.
- The **real hole** isn't a mistyped message, it's an **absent** one: nothing closes the Help-request lifecycle across the border, so INV-HELP-06/09 have no command to attach to. That's the one I'd take to the team first — it's a one-line addition (`MarkHelpResolved: MealPlanning|MealPreparation → CookingAssistance, cmd`) that unblocks two invariants at once.