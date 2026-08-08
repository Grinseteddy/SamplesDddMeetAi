# Naming and granularity

Two decisions carry the whole proposal: what each context is *called*, and how
many there are. Both are argued, not calculated.

---

## Subdomain or bounded context?

They are not the same thing, and saying which one you are proposing prevents a
long, avoidable argument.

- A **subdomain** is a part of the *problem space* — a distinguishable area of
  the business, discovered rather than designed. Lanes in a domain story are
  most naturally read as subdomains.
- A **bounded context** is a *solution-space* boundary — a deliberate decision
  about where one model and one ubiquitous language apply, and therefore about
  modules, services, and team ownership.

The useful default is one bounded context per subdomain, and it is what a domain
story's lanes usually imply. But the mapping is a design choice: two small
subdomains may share a context to avoid integration cost, and a large subdomain
may be split into several contexts as it grows.

**In practice:** propose lanes as subdomains, then state which ones you would
implement as separate bounded contexts and why. When the person's question is
about modules, services, or teams, they are asking about contexts; when it is
about "what parts does this business have", they are asking about subdomains.

---

## Naming rules

A context name should let a domain expert say "yes, that's a thing we do" and
let a developer know what does *not* belong there.

**Do:**

- Use a **noun phrase naming a responsibility**: *Bicycle distribution*,
  *Bicycle booking*, *Rack management*, *Accounting*, *Riding*.
- Use the **domain's own words**, including the team's existing jargon. If a
  Visual Glossary or the team's vocabulary already has a term, reuse it exactly.
- Make it **survivable**: the name should still fit after the obvious next
  feature. *Monthly billing* dies the day annual plans arrive; *Accounting*
  does not.
- Keep it **narrow enough to exclude things**. If nothing is obviously outside
  the name, it is too broad.

**Avoid:**

- **Bare entity names** (*Bicycle*, *User*, *Order*). They pull every sentence
  touching that noun into one lane and produce a data-owning service.
- **Technical layers** (*BicycleService*, *API*, *Backend*, *Database*,
  *Integration*).
- **CRUD verbs** (*Bicycle creation*, *Data entry*, *Updates*).
- **Channels** (*App*, *Mobile*, *Portal*).
- **Vagueness** (*Core*, *Platform*, *Operations*, *Misc*, *Common*, *Shared*).
  *Shared* in particular is where boundaries go to die.
- **Project or system names** that mean nothing to a domain expert.

**The suffix question.** *X management* is fine when the context really is
about maintaining a thing over its lifetime (*Rack management* — installing,
monitoring, servicing, granting access to racks). It becomes a smell when it is
applied to every lane, because "management" then substitutes for having thought
about what the lane actually does. If three of five lanes end in "management",
re-ask the purpose question for each of them.

**Test each name three ways:**

1. Say it to an imagined domain expert. Do they recognise it as part of the
   business, or does it sound like software?
2. Name something that clearly does *not* belong in it. If you cannot, it is
   too broad.
3. Ask whether the lane's sentences all serve the name. A sentence you have to
   argue for is a contested call (record it), and two sentences you have to
   argue for means the name is wrong.

---

## How many contexts?

There is no formula, but there is a useful expectation. In a workshop-scale
story of **8–15 sentences, three to six lanes** is the normal landing zone.

- **Too few** (one or two lanes for a rich story) usually means the cut was made
  on actor or timeline rather than on language. Check whether a single lane
  contains two different senses of the same noun.
- **Too many** (a lane per sentence or two) means steps were mistaken for
  responsibilities. Merge on shared purpose and shared ownership.

Scale the expectation with the story: a five-sentence story may honestly be one
or two contexts, and a forty-sentence story from several sessions may support
eight or more.

### The too-big test

A lane is too big when any of these hold:

- Two different senses of the same noun live inside it.
- Two different kinds of domain expert would be needed to validate it.
- Its purpose sentence needs an "and" to be true.
- Half the story is in it.

### The too-small test

A lane is too small when:

- It holds a single sentence **and** shares its purpose, actors and vocabulary
  with a neighbour. (A single-sentence lane is legitimate when it is genuinely
  its own concern with its own language — a lone *Accounting* step is a real
  subdomain that this story simply only touches once.)
- It exists only to hold a step you could not place elsewhere.
- Splitting it off buys no independence: same team, same clock, same model,
  same consistency requirement.

### Deciding a borderline lane

Ask what changing it would cost. If two candidate lanes would always be changed
together, deployed together, and reasoned about together, they are one context
that happens to have two phases. If either could plausibly be replaced, bought,
or handed to another team without touching the other, they are two.

---

## Presenting names you are unsure about

When two names are defensible, give both and say what each implies. Names are
cheap to change now and expensive once they are in code, package structures, and
team names — so make the choice visible rather than making it silently. In
*review* mode, keep the team's existing name unless it actively misleads; the
shared understanding already attached to it outweighs your improvement.