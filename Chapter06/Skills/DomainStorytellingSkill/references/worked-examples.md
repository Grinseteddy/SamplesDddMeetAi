# Worked examples

Three real domain stories, each interpreted into an abbreviated prototype brief. Use these to pattern-match: example 1 shows **lanes as subdomains** and a cross-lane sequence; example 2 shows a **role transition** and an **"optional" group**; example 3 shows an **entity state machine** and **two role instances of one actor**.

---

## Example 1 — Bicycle-sharing service (lanes = subdomains)

### Transcription (by subdomain lane)
- **Bicycle distribution:** 1. Mechanic brings Bicycle to Rack
- **User management:** 2. Commuter registers App
- **Accounting:** 3. Commuter pays Monthly fee via App
- **Bicycle distribution:** 4. Commuter searches for Bicycle at Public transport station
- **Bicycle booking:** 5. Commuter books Bicycle via App
- **Rack management:** 6. Commuter gets Code
- **Rack management:** 7. Commuter unlocks Bicycle with Code at Rack
- **Riding:** 8. Commuter drives Home with Bicycle
- **Bicycle distribution:** 9. Commuter returns Bicycle at Rack near Home
- **Rack management:** 10. Commuter confirms return via Code

Sequence runs 1→10 across lanes — the journey weaves through subdomains.

### Actors & roles
- **Mechanic** (person) — operations role; stocks racks. Not a customer.
- **Commuter** (person) — the end user; does steps 2–10.

### Modules (bounded contexts)
User management · Accounting · Bicycle booking · Rack management · Bicycle distribution · Riding.

### Domain model
- Entities: **Membership/User** (registered via App), **Monthly fee** (payment record), **Booking** (commuter↔bicycle reservation), **Code** (unlock token tied to a booking/rack).
- External/physical: **Bicycle** (record + status: racked/booked/in-use/returned), **Rack** (location), **Public transport station** (location), **Home** (destination, not stored).
- UI surface: **App**.

### State machines
- **Bicycle:** racked → searched/found → booked → unlocked (in use) → returned (racked). Triggers: mechanic *brings*, commuter *books*, *unlocks*, *returns*.

### Use cases & journey
register → pay monthly fee → search for a bike → book a bike → get unlock code → unlock bike → ride home → return bike → confirm return. (steps 1 by Mechanic, off-app.)

### Screens (Commuter app)
Sign-up/registration · Payment & membership · Find-a-bike (map at stations) · Booking confirmation · Unlock screen (shows Code) · Active-ride screen · Return/confirm screen.

### Open questions
What if no bike is available at the chosen station? Late-return or fee-on-overrun rules? Is the mechanic's stocking a separate ops app? Membership tiers beyond a single monthly fee?

---

## Example 2 — Recipe app (role transition + "optional" group)

### Transcription
1. Anonymous register App
2. Anonymous becomes Cook
3. Cook prepares Meal **and takes** pictures *(optional)*
4. Cook shares Recipe **and** pictures *(optional)*
5. Cook tries Recipe **and takes** pictures *(optional)*
6. Cook rates Recipe **and shares** pictures *(optional)*

A box labeled **optional** encloses all the `pictures` work objects.

### Actors & roles
- **Anonymous** → **Cook**: one user with a **role transition**. "becomes" (step 2) upgrades an anonymous/guest user into a registered Cook. Treat as one persona with two states (guest, cook), not two people. Steps 3–6 require the Cook role.

### Modules
No lanes. One module; note the implicit split: account/onboarding (1–2) vs. cooking & sharing (3–6).

### Domain model
- Entities: **User/Cook** (state: anonymous → cook), **Recipe** (title, ingredients, steps, rating), **Meal** (a prepared instance of a recipe), **Picture** (attached to meal/recipe).
- UI surface: **App**.

### State machines
- **User:** anonymous → cook (trigger: *register* + *becomes*).
- **Recipe** (light): drafted/shared → tried → rated. The same Recipe is shared (4), tried (5), rated (6).

### Use cases & journey
register → become a cook → prepare a meal (optionally photograph) → share a recipe (optionally with photos) → try a recipe (optionally photograph) → rate a recipe (optionally share photos).

### Screens (Cook app)
Register/onboarding · Profile (become a cook) · Create/share recipe (with optional photo upload) · Recipe browse/detail · "I tried this" + optional photo · Rate recipe + optional photo. **Photo features are optional UI everywhere** — never block a flow on them.

### Open questions
Can anonymous users browse before registering? What's "tries" vs "rates" — do tries gate rating? Are pictures attached to a Recipe, a Meal, or a separate gallery? Moderation of shared content? (Note: the diagram has a typo — "beomes" — same as "becomes".)

---

## Example 3 — Craftsman job tracker (entity state machine + two role instances)

### Transcription (by subdomain lane)
- **Project Management:** 1. Craftsman creates New project
- **Task Management:** 2. Craftsman creates New task in Project
- **Task Management:** 3. Craftsman selects New Task from Project
- **Task Management:** 4. Craftsman assigns Task to Craftsman
- **Task Management:** 5. Craftsman selects Assigned Task (→ Project)
- **Task Management:** 6. Craftsman works on Task in progress
- **Task Management:** 7. Craftsman finishes Task done
- **Task Management:** 8. Craftsman rejects Task rejected from Project
- **Billing:** 9. Craftsman bills Task done in Invoice
- **Project Management:** 10. Craftsman finishes Project

### Actors & roles
**Craftsman** appears as **two role instances**: a **manager** (creates project/tasks, assigns, bills, closes project — steps 1–4, 9–10) and an **assignee** (selects, works on, finishes/rejects — steps 5–8). Step 4, "assigns Task to Craftsman", links the two. The prototype needs at least these two permission sets even if they're the same person type.

### Modules
Project Management · Task Management · Billing.

### Domain model
- Entities: **Project** (name, status, tasks), **Task** (title, assignee, status), **Invoice** (line items from done tasks).
- No external/physical objects; everything is digital.

### State machines
- **Task** (the core lifecycle): `New task → (assign) → Assigned Task → (select/start) → Task in progress → (finish) → Task done` with an alternative `→ (reject) → Task rejected`. `Task done` and `Task rejected` are terminal; `Task done` feeds billing. Steps 7 (finishes) and 8 (rejects) are the **two branches** — drawn as separate sentences per the no-conditionals rule; reconstruct as a fork from "in progress".
- **Project:** new → active → finished (steps 1, 10).

### Use cases & journey
create project → create task → select task → assign task → (assignee) select assigned task → work on it → finish OR reject → bill done tasks into an invoice → close project.

### Screens
- **Manager:** Project list/create · Task create (in project) · Assignment screen · Billing screen (select done tasks → invoice) · Close-project.
- **Assignee:** My assigned tasks · Task detail / start work · Finish-or-reject control (the buttons that fire the Task transitions).

### Open questions
Is the manager-craftsman always distinct from the assignee-craftsman? Can a rejected task be reassigned (loop back)? Many tasks per project, many invoices? Who can bill — only the manager? What happens to a project with rejected tasks at close?