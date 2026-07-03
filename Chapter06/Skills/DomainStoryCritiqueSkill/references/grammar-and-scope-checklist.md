# Grammar & scope checklist

The per-block question banks for a domain-story critique. For each building
block: the **diagnostic question**, what a **strong** version looks like, and the
**common holes**. Then the three **scope dimensions** that the story must hold
consistent. Use this as a menu — raise only the findings that actually bite for
the story in front of you.

A domain-story sentence is `Actor —(N verb)→ Work object —(linking word)→ Work
object`. Each block below is one slot in that grammar.

---

## 1. Actors (the subject — a person or a software system)

**Diagnostic question:** *Would the domain expert recognise each actor as a real,
distinct participant — and is anyone who acts in this domain missing?*

**Strong version:** every actor is a specific role or named software system;
different roles carry different names; a system that drives steps on its own
(a payment service, a scheduler, an external registry) appears as its own actor,
not hidden inside a human's verb.

**Common holes:**
- **Collapsed actors.** Everyone is "User," "Customer," or one role name. Either
  the domain truly has one actor (say so, and check the self-referencing steps
  hold up) or distinct roles — requester vs. approver, seller vs. buyer — have
  been flattened, and the story has lost the differentiation that is half its
  value.
- **Role instances read as one person.** *X assigns a task to X* is **two role
  instances** (a manager role and an assignee role), not one human doing
  something to themselves. If the model treats them as identical, permissions and
  responsibilities downstream will be wrong.
- **Missing software-system actor.** An obviously digital flow with no system
  actor anywhere means either the story is pure-domain (fine — but then it
  shouldn't reference apps and screens) or the systems that actually do the work
  were silently absorbed into human verbs.
- **Missing human actor.** The clearest tell: a downstream artifact with no
  upstream actor — an invoice that's billed but no customer who receives it; a
  decision with no one it's communicated to. The actor a whole subprocess is
  *for* is often the one that's absent.
- **An actor that's really a work object** (or vice versa). A "System" that is
  only ever acted *upon* is a work object; a "Document" that *does* something is
  mislabelled.

---

## 2. Activities (the verb)

**Diagnostic question:** *Is this the word the domain expert would use for what
happens here — or a generic database/UI word standing in for it?*

**Strong version:** verbs come from the domain's **ubiquitous language** and name
a meaningful business act ("quotes," "dispatches," "settles," "rejects,"
"escalates"). Each verb earns its place; you could not swap it for any other and
keep the meaning.

**Common holes:**
- **CRUD verbs.** "creates / reads / updates / deletes" (and "adds," "sets,"
  "enters") describe what the *database* does, not what the *business* does. A
  craftsman doesn't "create a task," they "plan a job" or "take on work." CRUD
  verbs are the single most common sign a process diagram has been mislabelled as
  a domain story.
- **UI gestures.** "selects," "clicks," "opens," "navigates to," "drags" describe
  the *interface*, not the domain. They are also a granularity smell — they sit
  far below the level of a business activity.
- **Vague catch-alls.** "handles," "processes," "manages," "does" name no real
  act and hide whatever actually happens. Push for the specific verb.
- **A verb doing two jobs.** If one arrow means both "decides" and "records the
  decision," it's two activities compressed into one — split them if the order or
  the actor differs.
- **Redundant verb pairs.** "creates X" immediately followed by "selects X" (you
  select the thing you just made) is usually UI mechanics leaking in; the second
  step rarely carries domain meaning.

---

## 3. Work objects (the noun the verb acts on)

**Diagnostic question:** *Is this a thing the domain genuinely has — and is it a
thing, a place, or a passing state?*

**Strong version:** work objects are nouns from the domain (a Job, a Quote, an
Invoice, a Route). Documents, tools, and the things produced or consumed are all
fair game, each recognisable to the expert.

**Common holes:**
- **A UI surface treated as a work object.** "App," "screen," "form,"
  "dashboard," "dropdown" is *where* an activity happens (a channel), not a
  domain thing. It tells you the medium, not the model.
- **States dressed up as separate objects.** *New task → Task → Assigned Task →
  Task in progress → Task done → Task rejected* is **one entity (Task)** moving
  through **states**, not six different things. Drawing each state as its own
  work object can be a legitimate way to show a lifecycle — but if it clutters
  the story or hides that it's all one entity, name it: the lifecycle is the
  backbone, the adjectives are states, the verbs between them are transitions.
- **Inconsistent naming of the same object.** "New task" in one sentence and "New
  Task" in the next (or "Task" vs. "Job") forces the reader to guess whether
  they're the same thing. In a method whose entire point is a shared, unambiguous
  language, casual renaming is a real defect.
- **An object that appears from nowhere.** A work object acted on in step 6 that
  was never produced or introduced earlier leaves a gap the numbered sequence
  implies but doesn't show.

---

## 4. Sequence & numbering (the order)

**Diagnostic question:** *Does each numbered step plausibly cause or enable the
next, in the order shown?*

**Strong version:** the numbers trace one causal chain; reading 1→N tells a story
that makes sense forwards, with no step that couldn't happen yet and none that's
already been superseded.

**Common holes:**
- **Out-of-order steps.** A "rejects" *after* a "finishes," a "bills" before the
  work is done. Often a sign that an alternative outcome has been numbered into
  the happy path (see `coherence-and-story.md`).
- **Implied gaps.** The numbers jump from a step to one that assumes work in
  between that was never told. Either the gap is fine (coarse granularity) or a
  step is missing — decide which.
- **Redundant steps.** Two consecutive steps that are the same act seen twice
  (create-then-select; save-then-confirm) usually mean UI mechanics, not domain
  events.

---

## 5. Groups (the lanes / boxes)

**Diagnostic question:** *Does each group name a meaningful cluster — usually one
subdomain or bounded context — and does every sentence sit in the right one?*

**Strong version:** groups partition the story into coherent subdomains
("Booking," "Dispatch," "Billing"); each lane's sentences belong together; the
boundaries reflect real seams in the domain.

**Common holes:**
- **One context split across two lanes.** The *same* group label appearing on two
  non-adjacent lanes (e.g. "Project Management" at the top *and* the bottom)
  suggests either the story should be re-grouped so the context is contiguous, or
  the two lanes are actually different contexts that share a name.
- **A sentence in the wrong lane.** A billing step sitting in the task lane, or a
  completion step filed under a context that doesn't own it.
- **Groups that carry no meaning.** Lanes drawn for layout rather than domain
  seams add visual structure but no insight — note them as cosmetic.
- **No groups at all.** Fine for a small story; for a large one, the absence of
  any subdomain structure is itself worth a question.

---

## The three scope dimensions (must be held *consistent*)

Hofer & Schwentner (2022) describe a domain story as positioned on three
dimensions. A good story picks one point on each **and stays there**. Mixing is a
classic, high-value finding because the story then means different things in
different places.

- **Granularity — coarse-grained vs. fine-grained.** A coarse story has a few big
  steps ("Dispatcher schedules the job"); a fine one breaks each into many small
  ones. Both are valid; *mixing* them isn't. One step that says "handles the
  order" sitting beside five steps of button-clicks tells the reader the
  modelling drifted.
- **Point in time — as-is vs. to-be.** An **as-is** story records how the domain
  works *today*; a **to-be** story proposes how it *should* work. They are
  different conversations. A story that is mostly current reality but smuggles in
  two aspirational steps will be read as fact by some and as proposal by others —
  exactly the misunderstanding the method exists to prevent.
- **Degree of detail — pure vs. digitalized.** A **pure** story stays in the
  domain and ignores which steps are software-supported; a **digitalized** story
  shows the systems and screens. Choose one. A pure story that suddenly names a
  specific app, or a digitalized one that omits the systems for half its steps,
  is inconsistent.

For each dimension, the question is the same: *is the story at one consistent
point, and is that the point the team needs for this conversation?* A consistent
story at the wrong granularity for its purpose (too fine for a strategy
discussion, too coarse for a build spec) is also a finding worth naming.