# Coherence & story

The block-level checklist (`grammar-and-scope-checklist.md`) tests the parts.
This file tests the **whole**: whether the parts add up to one concrete domain
story, and what that one concrete scenario structurally *cannot* show but the
domain still needs. These are usually the highest-value findings, so reach here
before nitpicking verbs.

---

## The single-scenario rule (the property most often broken)

A domain story is **one concrete scenario** — a specific run-through of the
process, the way someone would *tell* it: "So a customer calls, then I look up
their account, then…". That is its defining strength over a flowchart: it stays
concrete, so the domain expert can immediately confirm or correct it.

The corollary, and the rule most stories quietly break: **a domain story has no
branches, no loops, and no conditionals.** There is no "if approved… else…", no
arrow that loops back, no decision diamond. The story shows what *did* happen in
*this* telling.

How to spot a flowchart wearing a story's clothes:

- **Mutually exclusive outcomes in one story.** The clearest tell: two numbered
  sentences that can't both be true in a single run — *7 finishes → Task done*
  **and** *8 rejects → Task rejected*. A real telling either finishes or rejects;
  it can't do both. This story is actually **two stories** (a happy path and a
  rejection variant) drawn on top of each other.
- **A step that only sometimes happens.** "...and if it's urgent, escalates..."
  is a conditional. Pull it into its own story or mark it as a different scenario.
- **An arrow that goes backwards.** A loop ("repeats until approved") is a process
  abstraction, not a told event sequence.

**Why it matters, not just that it's "wrong":** the value of the method is that
the expert can verify a concrete story at a glance. The moment it forks, it stops
being something anyone actually experienced, and verification breaks down — the
expert can no longer say "yes, that's exactly how it goes," because it isn't how
*any single case* goes.

**The fix is almost always: split into multiple stories.** A happy-path story and
one story per significant variant. Recommend that explicitly — it's a cheap,
concrete move, and it usually *clarifies* rather than complicates.

---

## Re-tell it in one breath

Read the whole story aloud as a single narrative, following the numbers:
"A craftsman creates a project, then creates a task in it, then selects the
task, then assigns it to a craftsman, then..." Two things surface immediately
when you do this:

- **Does it sound like something a person said, or like boxes someone tidied?**
  A told story has a natural rhythm and a reason each step follows the last. A
  tidied process has even, mechanical steps ("creates… selects… updates…") that
  betray a CRUD/UI origin. If re-telling it feels like reading a database log,
  that's the finding.
- **Where do you stumble?** The spot where the narration stops making sense — a
  step out of order, an object that wasn't introduced, a leap in granularity — is
  exactly where the model has a hole. Trust the stumble.

---

## The shadow checks — what a concrete scenario can't show

A single happy path is, by design, blind to several things a real domain needs.
None of these is an excuse to *invent* content and slip it into the story
silently — they are honest "what's missing?" questions to raise:

- **The actor it's all for.** Follow the value. If there's an invoice, who
  receives and pays it? If there's a decision, who is it communicated to? A
  billing flow with no customer, a fulfilment flow with no recipient, an approval
  with no requester — the missing actor is often the *reason the process exists*,
  and a story that omits it models a tool rather than a business. This is
  frequently the single biggest finding.
- **The external systems it leans on.** A digitalized story that shows humans
  doing work software clearly does (matching, routing, charging, notifying) is
  hiding its system actors. Ask which steps a system actually performs.
- **The alternatives that were never told.** Because the story is one path, every
  *other* path is invisible: rejections, retries, cancellations, empty states,
  errors. You don't add them to *this* story — you note that they exist and ask
  whether each deserves its own story. (If one is already lurking *inside* this
  story, that's the single-scenario violation above.)
- **Multiplicity the picture flattens.** One task, one project, one invoice in
  the drawing — but does a project have many tasks? Does one invoice cover many
  tasks? The concrete scenario shows one of each; the domain may need many, and
  that shapes the model.

---

## Does it still serve shared understanding?

The ultimate test, behind all the others (Hofer & Schwentner, 2022): **would the
domain expert and the developer read this story the same way?** A story can pass
every grammar check and still fail here:

- **Ambiguity that splits readers.** A verb or object that the expert reads one
  way and the developer another (is "closes the job" finishing the work, or
  archiving the record?). Ubiquitous language exists to kill exactly this; flag
  any term that could carry two meanings.
- **Hidden assumptions stated as fact.** A to-be step the team hasn't agreed to,
  drawn as if it's settled — a quiet "false consensus" the diagram launders into
  agreement. (When an upstream workshop artifact is available, this is also a
  fidelity check — see `upstream-cross-check.md`.)
- **A story optimised for the tool, not the domain.** If the steps mirror an
  existing app's screens rather than the domain's logic, the story will teach the
  team the software they already have, not the domain they're trying to
  understand.

When you raise any of these, frame it as a question to the people who know — the
odd-looking choice is sometimes a faithful model of a genuinely unusual domain,
and asking respects that possibility while still surfacing the risk.