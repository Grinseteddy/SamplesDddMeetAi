# Worked example — a bicycle-sharing story

A ten-sentence domain story arrives with **no lanes drawn**. This is the whole
method run end to end, at roughly the depth a real answer should have, including
the calls that could have gone the other way.

---

## Step 1 — Transcription

```
1.  Mechanic brings Bicycle to Rack
2.  Commuter registers App
3.  Commuter pays Monthly fee via App
4.  Commuter searches for Bicycle at Public transport station
5.  Commuter books Bicycle
6.  Commuter gets Code
7.  Commuter unlocks Bicycle with Code at Rack
8.  Commuter drives Home with Bicycle
9.  Commuter returns Bicycle at Rack near Home
10. Commuter confirms return via Code
```

One actor does nine of the ten steps. Per signal 3, that proves nothing about
the number of contexts — resist reading it as "one context, one user".

## Step 2 — Signal table

| # | Actor | Activity | Work objects | What it is for | Sense of the shared nouns |
|---|---|---|---|---|---|
| 1 | Mechanic | brings | Bicycle, Rack | Bikes are where riders will want them | *Bicycle* = serviceable asset with a condition and a location; *Rack* = a place with capacity |
| 2 | Commuter | registers | App | Someone becomes a known, contactable customer | — (*App* is a channel, not a domain object) |
| 3 | Commuter | pays | Monthly fee, App | The company gets paid | *Monthly fee* = a receivable |
| 4 | Commuter | searches for | Bicycle, Public transport station | Rider finds supply near them | *Bicycle* = availability at a location |
| 5 | Commuter | books | Bicycle | A specific bike is held for this rider | *Bicycle* = reservable inventory, one identity, a time window |
| 6 | Commuter | gets | Code | Rider is given the means to open a lock | *Code* = a time-limited access credential |
| 7 | Commuter | unlocks | Bicycle, Code, Rack | Physical release of the bike | *Bicycle* = a locked object; *Rack* = a lock holder |
| 8 | Commuter | drives | Home, Bicycle | The rider actually gets somewhere | *Bicycle* = a vehicle in someone's custody |
| 9 | Commuter | returns | Bicycle, Rack, Home | The bike re-enters supply somewhere new | *Bicycle* = an asset back in circulation; *Rack* = capacity again |
| 10 | Commuter | confirms return | Code | The system knows the ride is over | *Code* = proof the lock closed |

The last column is already doing the cutting. *Bicycle* carries at least four
distinct senses; *Rack* two; *Code* one but sharply defined.

## Step 3 — What the signals say

- **Language shift (1):** *Bicycle*-as-asset (1, 4, 9) versus
  *Bicycle*-as-reservable-inventory (5) versus *Bicycle*-as-locked-object (7)
  versus *Bicycle*-as-vehicle-I-am-riding (8). Four senses, and note where they
  fall: **1, 4 and 9 are not adjacent.** That non-contiguity is the tell that
  this is a real context and not a phase.
- **Purpose (2):** "so bikes are where riders are" answers 1, 4 and 9 with one
  sentence. "So we get paid" answers 3 alone. "So the rider can physically get
  the bike out and give it back" answers 6, 7 and 10. "So someone becomes a
  customer" answers 2. "So the rider gets somewhere" answers 8.
- **Actor (3):** only sentence 1 has a different actor. Weak here — useful
  confirmation that 1 is not part of the booking flow, nothing more.
- **Handoff (4):** custody passes at 7 (bike leaves the rack) and returns at 9.
  Both boundaries sit next to a real change of responsibility.
- **Cohesion (5):** {Code, Rack, Bicycle} recur together in 6, 7, 10.
  {Bicycle, Rack, location} recur in 1, 4, 9.
- **Ownership (6):** booking creates the reservation; rack management only
  *consumes* the code it was given. Direction established.

## Step 4 — Proposed contexts

**Bicycle distribution** — sentences 1, 4, 9.
Keeps serviceable bikes available where riders need them.
Actors: Mechanic, Commuter. Owns: *Bicycle* (as an asset — condition, location,
availability), *Rack* (as a location with capacity), *Public transport station*.
Justified by: language shift, purpose, handoff, cohesion. The recurrence across
the sequence is the strongest evidence in the cut.

**User management** — sentence 2.
Turns a person into a known customer of the service.
Owns: *Commuter* (as an account and identity). Single-sentence lane, and
legitimately so: registration is its own concern with its own language and its
own experts, and this story simply touches it once.

**Accounting** — sentence 3.
Collects what riders owe.
Owns: *Monthly fee*, and any future pricing, invoicing and dunning. Almost
certainly a generic subdomain (see classification below).

**Bicycle booking** — sentence 5.
Holds a specific bike for a specific rider for a period.
Owns: *Booking*; borrows *Bicycle* in a different sense (reservable inventory
rather than physical asset) — that borrowing is precisely why it is separate
from distribution.

**Rack management** — sentences 6, 7, 10.
Controls physical access to bikes at racks and knows whether a bike is locked in.
Owns: *Code*, *Lock*, *Rack* (as hardware). Consumes a booking from Bicycle
booking. Note it too is non-contiguous, wrapping around the ride.

**Riding** — sentence 8.
The trip the rider actually takes.
Owns: *Ride* / *Trip* (duration, route, start and end rack) — an entity the
story never names but the lane implies. Thin here, and worth keeping separate
anyway because it is the only place trip data lives.

## Step 4b — Lane assignment (the redraw spec)

| # | Sentence | Lane |
|---|---|---|
| 1 | Mechanic brings Bicycle to Rack | Bicycle distribution |
| 2 | Commuter registers App | User management |
| 3 | Commuter pays Monthly fee via App | Accounting |
| 4 | Commuter searches for Bicycle at Public transport station | Bicycle distribution |
| 5 | Commuter books Bicycle | Bicycle booking |
| 6 | Commuter gets Code | Rack management |
| 7 | Commuter unlocks Bicycle with Code at Rack | Rack management |
| 8 | Commuter drives Home with Bicycle | Riding |
| 9 | Commuter returns Bicycle at Rack near Home | Bicycle distribution |
| 10 | Commuter confirms return via Code | Rack management |

Six lanes for ten sentences, three of them recurring. Nothing named after the
App.

## Step 5 — Context map

| From | To | What flows | Pattern | Note |
|---|---|---|---|---|
| User management | Accounting | Registered commuter (identity, contact) | Customer/supplier | Accounting needs someone to bill |
| User management | Bicycle booking | Commuter identity, entitlement to ride | Customer/supplier | |
| Bicycle distribution | Bicycle booking | Which bikes exist, where, available | Customer/supplier | Booking conforms to distribution's inventory view |
| Bicycle booking | Rack management | A booking that authorises unlocking | Customer/supplier | The *Code* is the published token of this |
| Rack management | Riding | Bike unlocked → ride starts | Customer/supplier | |
| Riding | Bicycle distribution | Bike returned at a rack → back in supply | Customer/supplier | The 9/10 pair straddles this seam |
| Riding | Accounting | Ride completed (usage) | Customer/supplier | Only matters if pricing ever becomes usage-based — flag as an assumption |

## Step 6 — Classification (hypothesis)

- **Bicycle distribution — core.** Getting bikes where demand is, is the thing
  this business either wins or loses on.
- **Bicycle booking — core or supporting**, depending on strategy. Ask.
- **Rack management — supporting**, and partly hardware-constrained.
- **Riding — supporting**, thin today, likely richer later (route data).
- **User management — generic.** Buy identity.
- **Accounting — generic.** Buy billing.

## Step 7 — Contested calls and alternatives

**Sentence 9 (returns Bicycle at Rack near Home)** — the genuinely hard one.
*For Bicycle distribution:* the business meaning of a return is that a bike
re-enters supply at a new location, which is distribution's whole concern.
*For Rack management:* physically, the rider is docking and locking, which is
what 7 did in reverse. *Placed in distribution* because the lane's purpose
sentence covers it and because 10 already carries the locking side. **What
would settle it:** ask operations whether they think of a return as "a bike is
now available there" or as "the rental is closed out". If the answer is the
second, 9 moves and distribution keeps only 1 and 4.

**Sentence 4 (searches for Bicycle)** — could sit in Bicycle booking, since
search-then-book reads as one rider journey. Placed in distribution because what
the rider is searching is the *supply picture* — availability by location —
which distribution owns; booking then acts on the result. Moving it makes
booking a coherent "find and reserve" lane, which some teams prefer. Both cuts
are defensible; the language test favours distribution.

**Sentence 6 (gets Code)** — could belong to booking, since the code is what a
booking produces. Placed in rack management because a code is a credential
against a lock, and its lifetime, format and revocation are lock concerns.

**Coarser cut (four lanes)** — merge *Riding* into *Bicycle booking* (the
booking spans the ride), and *User management* into *Accounting* (one "customer"
lane). Loses the separate ride model, which hurts as soon as trip data matters,
and couples identity to billing, which hurts as soon as a second payment model
appears.

**Finer cut (seven or eight lanes)** — split *Bicycle distribution* into
*Fleet maintenance* (mechanic, condition, servicing) and *Availability*
(where bikes are, rebalancing). Worth doing the moment maintenance gets its own
steps — the mechanic in sentence 1 is doing a job this story barely shows.

**Open questions:** where bikes are serviced and by whom; what happens when a
rack is full on return; whether pricing is subscription-only or usage-based;
who owns the *Ride* entity the story implies but never names.

---

## The same story in review mode

If the story had arrived **with** these lanes already drawn, the output changes
shape, not substance: confirm the lanes the evidence supports (distribution's
recurrence across 1/4/9 is well judged; nothing is named after the App), then
raise the disagreements — sentence 9's placement, the single-sentence *Riding*
lane's thinness, and whether *User management* and *Accounting* being separate
is deliberate or accidental. Keep their names. Argue the sentences.