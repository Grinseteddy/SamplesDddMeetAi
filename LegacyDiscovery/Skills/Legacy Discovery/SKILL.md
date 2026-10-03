---
name: "legacy-api-domain-discovery"
description: "Guide a user step by step through AI-assisted legacy discovery: get the repo link, pick an API to examine, then dig out domain concepts pattern by pattern, with evidence."
author: Annegret Junker
---

# Legacy API Domain Discovery

A guided dig through an old code base. The goal is to recover the domain knowledge buried behind generic APIs (classes like `DataProcessor`, `ModelADService`, operations like `createData`) and turn it into an evidence-backed glossary and candidate bounded contexts.

The user leads, Claude digs. Go one step at a time: show what was found, then let the user decide the next step. Never run all patterns in one go.

Talk to the user in their language. The pattern names below have German equivalents used in the original talk (in brackets); use them when the user writes German.

## Ground rules (apply to every step)

- **Evidence or it did not happen.** Every finding carries a source: `file:line`, a commit hash with date, or a command whose output shows it. Counts come from running the command, never from estimating.
- **Hypotheses are labelled.** Each interpretation gets a confidence (high / medium / low) and, for medium or low, a competing hypothesis.
- **The code tells *what*, rarely *why*.** If an explanation is about origin or motive and nothing in code, commit messages or ticket references states it, mark it "unverified - ask someone who was there" and add it to the hotspot list.
- **Counter-check every attractive story.** Before presenting an explanation of a code or name, search for the same code or name elsewhere in the repo with a different meaning. (Lesson from the original dig: payment code `K` read as German "Kreditkarte", but in the payment table `K` means cheque. The plausible story was wrong.)
- **Pin the version.** Record the commit hash of the analysed checkout in the ledger; all line numbers refer to it.
- **Keep a findings ledger.** Maintain `discovery-ledger.md` in the working directory: repo + commit, chosen API, then one section per completed step (prompt used, findings table, hypothesis, hotspots). Update it after every step so the user can resume later or turn it into slides.
- **Big repos are slow.** Full-history searches (`git log -S`, `git log -L`, `--follow`) can take minutes on a repo with 15,000+ commits. Scope them to paths, wrap them in `timeout`, and say when a search was cut short instead of presenting a partial answer as complete.

## Step 1 - Ask for the repository

Ask for the link to the repository (GitHub/GitLab URL) or a local path. Ask only this; do not ask about APIs yet.

- Remote: clone with full history but without blobs: `git clone --filter=blob:none <url>`. History is needed for buried APIs and time layers. If the repo is private, ask the user how they want to grant access.
- Local path on the user's computer: request folder access and work there, or ask them to attach it.
- Report back in two or three lines: number of commits, first and latest commit dates, main languages and top-level modules.

## Step 2 - Choose the API to examine

Build an inventory of API surfaces, in the current tree *and* in history:

- SOAP: `*.wsdl`, `*.xsd`, `@WebService`, `javax.jws` / `jakarta.jws`, CXF or Axis config.
- REST: `@Path`, `@RestController`, `@RequestMapping`, route files, `openapi*.y*ml` / `swagger*`.
- Others: `*.proto` (gRPC), GraphQL schemas, message listeners and topics, generic CRUD services or "service type" config tables.
- Buried APIs: `git log --diff-filter=D --name-only` for deleted WSDL/XSD/service modules, and commits whose message says "remove" / "move out of core" for web services. Recover deleted files with `git show <commit>^:<path>`.

Present a short numbered list. For each API give: name, type, location (or "deleted in <commit>, <date>"), size (files/lines, number of operations), first-seen date, and one line on why it is interesting for domain discovery (generic operation names, many flags, magic codes, one endpoint serving many purposes). Recommend one and say why: the most generic API usually hides the most domain.

Let the user pick (use the question tool; if there are more than four candidates, list them in text and offer the top three plus "other").

Then give the **first look** at the chosen API, as a new team would see it: its operations, the main data structure behind it (fields count, yes/no flags, coded values, references to other entities), and one concrete example call. End with the question the rest of the dig answers: where did the domain go?

## Step 3 - Patterns, one at a time, with skip

After the first look, and after each pattern, propose the **next pattern**: name it, say in one sentence why it looks promising *for this API* (cite what you saw), and offer:

- **Run it** (recommended)
- **Skip** - propose the following one instead
- **Different pattern** - user picks from the remaining ones
- **Stop and summarise** - go to step 4

Use the question tool for this choice. Choose the proposed pattern adaptively: e.g. status fields seen -> state machine next; several partner/customer IDs seen -> one word, several meanings next.

For each pattern, show the prompt you are about to work on (so the user can reuse it in a workshop), run the analysis, then report: a findings table (finding | evidence), the hypothesis with confidence, and hotspots. Add it to the ledger.

### The six patterns

**1. Type discriminator -> hidden concepts** (Typ-Diskriminator → verborgene Konzepte)
Look for fields that switch behaviour: `type`, `kind`, `Is*` flags, document type or subtype codes. Count the branches on them and compare side effects per branch (different reversal, different documents created, different costs). Different side effects mean different domain concepts sharing one structure.
Prompt: "Where does the logic branch on <field>? What happens in each branch, in business terms? Evidence with file and line."

**2. Magic status codes -> state machine** (Magische Statuscodes → Zustandsautomat)
Find the status enum and the place that defines allowed transitions (action tables, `getValidActions`-style methods, guards in `complete`/`void`/`close`). Reconstruct the state machine and draw it (Mermaid or a diagram). Check which transitions the UI offers versus what the engine or API allows; check which actions are actually implemented for this entity (some may always return false). Flag codes reused with different meanings (e.g. `RE` = status Reversed and action Re-activate).
Prompt: "Reconstruct the state machine for <entity>: which states exist, which actions are allowed in which state, and where do they lead? Evidence with file and line."

**3. Validations -> forgotten business rules** (Validierungen → vergessene Geschäftsregeln)
Read validation and preparation methods (`validate`, `beforeSave`, `prepare`, `check*`). Phrase each rule as Given/When/Then. For rules commented only with a bug number, trace the origin with `git log -L` (scoped, with timeout) and the ticket reference. Search for copies of the same rule elsewhere and diff them: diverged copies are hotspots.
Prompt: "Which business rules does the logic check before <entity> may be completed? Phrase each as Given/When/Then and check whether the same rule is implemented differently elsewhere."

**4. One word, several meanings -> context boundaries** (Ein Wort, mehrere Bedeutungen → Kontextgrenzen)
Pick a field or term used for several roles (partner, customer, account, address). Trace where its values flow: into invoices, shipments, credit checks, other documents. Produce a table: where | what the word means there | evidence. Each distinct meaning is a candidate bounded context. Also list role flags on the entity itself.
Prompt: "<entity> has several <term> references. Trace where each flows: who is meant by <term> in invoicing, delivery, credit checking?"

**5. Package names and tickets -> time layers** (Paketnamen und Tickets → Zeitschichten)
Count source files per package prefix, find first commits per module, count ticket references per tracker (old bug numbers, Jira keys, PR numbers), list migration folders. Present the layers with dates and counts. State which dates come from the repo and which from outside knowledge.
Prompt: "Which time layers do you see in this repository? Use package names, ticket references and git history and date each layer."

**6. Cryptic names -> ubiquitous language** (Kryptische Namen → Ubiquitous Language)
Collect the cryptic names and codes from the earlier findings and propose business terms with confidence. Explicitly search for abbreviations with several meanings in the same code base (e.g. `PO` = persistent object, purchase order, post, point of sale). Where no business term is possible (purely technical classes), say so.
Prompt: "Propose business terms for the cryptic names and codes in our findings, with confidence. Are there abbreviations with several meanings in this code?"

### Optional: plausible but wrong

If during the dig an explanation sounded convincing but had no evidence, offer to run it as an explicit two-prompt check: first ask for the explanation, then "Check your explanation against the code: does the same code appear elsewhere with a different meaning?" Record the outcome. This is a strong teaching moment for workshops and talks.

## Step 4 - Synthesis

When the user stops (or all patterns are done), produce:

1. **Intentions** - the business intentions hidden behind the generic API or structure (e.g. one order table = quotation, sales order, return, purchase order), each with its evidence.
2. **Glossary** - term | meaning | candidate context | evidence.
3. **Candidate bounded contexts** - each with its terms and the patterns whose findings support the cut. Call them candidates: team structure, change frequency and strategy decide the real cuts, and none of that is in the code.
4. **Hotspot list** - open questions only domain experts can answer (motives, diverged copies, unverified origins), each with the evidence that raised it.

Update the ledger and offer to turn the result into a shareable document, slides, or input for an EventStorming / Domain Storytelling session.

## Workshop hint

The same flow works as a mob-prompting workshop (about 2.5 hours): one screen with Claude in the repository, one whiteboard with glossary, hotspots and context candidates. A business analyst asks, a developer checks evidence, a domain expert decides hotspots, a driver types only what the group says and rotates every 10 minutes. Mention this when the user is preparing a workshop.