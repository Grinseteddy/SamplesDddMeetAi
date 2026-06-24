# Worked example — library management board

A full Big-Picture-with-design-detail board for a library/reading app, worked end-to-end into an **Event Storming Brief**. Use it to pattern-match: how to reconcile a non-canonical legend, parse each slice into its grammar sentence, read arrows as cross-slice flows, derive aggregate state machines, and stay honest about ambiguity instead of inventing.

The board has ten labeled bubbles (Purchase, Catalog management, Audio summary, Catalog search, Notification, Lending, Bookshelf, Reading, Reading, Lending, Notes). Every slice follows the same grammar; policies are drawn **pink with a gear**, not canonical lilac.

---

# Event Storming Brief — Library Management

## 1. Legend (as read on this board)

| On the board | Canonical role | Note |
|---|---|---|
| Orange + sparkle | Domain event | Past tense throughout — good hygiene. |
| Blue + ⌘ | Command | Imperative, except *Sent Notification* (see hotspots). |
| Large yellow + 💼 | Aggregate | The entity emitting the event. |
| Small yellow + 👤 | Actor | *Librarian*, *Member*. |
| Green + 👁 | Read model | The view consulted in the slice. |
| **Pink + ⚙** | **Policy (automated)** | Deviates from canonical lilac. *AI*, *Notification center*, *Book shelf automate* — machine reacts, no human. |
| Blue bubbles | Bounded contexts | Ten of them. |
| Black arrows | Flow / causation | Event in one slice → command in another. |

No red hotspots were drawn on the board; the hotspots in §8 are this interpretation's own findings.

## 2. Event timeline

Grouped by context, in board order (past-tense facts):

- **Purchase:** 1. Book purchased
- **Catalog management:** 2. Catalog entry created · 3. Catalog updated
- **Audio summary (AI pipeline):** 4. Book read · 5. Index created · 6. Audio summary created
- **Catalog search:** 7. Book not found · 8. Book searched
- **Notification:** 9. Notification sent
- **Lending:** 10. Book lent
- **Bookshelf:** 11. Book arranged *(manual)* · 11′. Book arranged *(automated)*
- **Reading:** 12. Reading position marked *(manual)* · 12′. Reading position marked *(automated)* · 13. Page read
- **Reading (finish):** 14. Book read *(finished)*
- **Lending (return):** 15. Book returned
- **Notes:** 16. Text marked · 17. Note created · 18. Note edited · 19. Note deleted

## 3. Bounded contexts (modules)

| Context → module | Owns (events) | Aggregates | Key read models |
|---|---|---|---|
| **Acquisition** (Purchase) | Book purchased | Book | Retailer catalog |
| **Catalog** (Catalog management) | Catalog entry created, Catalog updated | Catalog entry, Catalog | Book, Catalog entry |
| **Audio summary** | Book read, Index created, Audio summary created | Index, Audio summary | Book, Index |
| **Search** (Catalog search) | Book not found, Book searched | Search criteria | Catalog entry |
| **Notification** | Notification sent | Notification | Search criteria, Catalog |
| **Lending** | Book lent, Book returned | Lending entry | Catalog entry, Book |
| **Bookshelf** | Book arranged | Book | Book shelf |
| **Reading** | Reading position marked, Page read, Book read (finished) | Book | — |
| **Notes** | Text marked, Note created/edited/deleted | Text mark, Note | Book, Text marked |

## 4. Domain model

Aggregates → entities (attributes inferred from the commands/events around them):

- **Book** — id, title, status, shelf location, current reading position. The pivotal entity; appears across Acquisition, Bookshelf, Reading.
- **Catalog entry** — book ref, metadata, availability. **Catalog** — the collection of entries.
- **Lending entry** — book ref, member ref, lent-at, due/returned-at.
- **Note** — book ref, text-mark ref, body, status. **Text mark** ("Text marked") — book ref, location/range, marked-at.
- **Index** — book ref, structure; **Audio summary** — book ref, audio asset (AI-generated).
- **Notification** — recipient, subject, sent-at. **Search criteria** — the member's query.

Read models / views (green) — projections a screen or policy reads: *Retailer catalog* (likely an **external** retailer feed, not an internal projection — flagged §8), *Book*, *Catalog entry*, *Catalog*, *Index*, *Book shelf*, *Search criteria*, *Text marked*, *Text*.

## 5. Aggregate state machines

**Book** (the backbone lifecycle):
```
Purchased ──Arrange book──▶ Shelved ──Read book──▶ Reading ──Finish book──▶ Read(finished)
                                          ▲   │
                            Mark reading position (updates position, stays Reading)
```
| from | command → event | to |
|---|---|---|
| — | Purchase book → Book purchased | Purchased |
| Purchased | Arrange book → Book arranged | Shelved |
| Shelved | Read book → Page read | Reading |
| Reading | Mark reading position → Reading position marked | Reading (self) |
| Reading | Finish book → Book read | Read *(terminal)* |

**Lending entry:** `Lent ──Return book/Book returned──▶ Returned (terminal)`.
**Note:** `Created ──Edit note──▶ Edited ──Delete note──▶ Deleted (terminal)` (edit is a self-loop until deletion).

## 6. Commands, policies & flows

Per slice — `context · actor/policy · command · aggregate · event · read models`:

| Context | Actor / Policy | Command | Aggregate | Event | Read models |
|---|---|---|---|---|---|
| Acquisition | Librarian | Purchase book | Book | Book purchased | Retailer catalog |
| Catalog | Librarian | Create catalog entry | Catalog entry | Catalog entry created | Book |
| Catalog | Librarian | Update catalog | Catalog | Catalog updated | Catalog entry |
| Audio summary | **AI** ⚙ | Read book | *(none shown)* | Book read | Book |
| Audio summary | **AI** ⚙ | Create index | Index | Index created | Book |
| Audio summary | **AI** ⚙ | Create audio summary | Audio summary | Audio summary created | Index |
| Search | Member | Search book | Search criteria | Book not found | — |
| Search | Member | Search book | Search criteria | Book searched | Catalog entry |
| Notification | **Notification center** ⚙ | Sent Notification | Notification | Notification sent | Search criteria, Catalog |
| Lending | Member | Borrow book | Lending entry | Book lent | Catalog entry |
| Bookshelf | Member | Arrange book | Book | Book arranged | Book shelf |
| Bookshelf | **Book shelf automate** ⚙ | Arrange book | Book | Book arranged | Book shelf |
| Reading | Member | Mark reading position | Book | Reading position marked | — |
| Reading | **Book shelf automate** ⚙ | Mark reading position | Book | Reading position marked | — |
| Reading | Member | Read book | Book | Page read | — |
| Reading | Member | Finish book | Book | Book read | — |
| Lending | Member | Return book | Lending entry | Book returned | Book |
| Notes | Member | Mark text | Text mark | Text marked | Book |
| Notes | Member | Create note | Note | Note created | Text marked |
| Notes | Member | Edit note | Note | Note edited | Text |
| Notes | Member | Delete note | Note | Note deleted | Text marked |

Cross-slice flows (arrows = policies/sagas):
- **Reading position marked → Read book** (both the manual and automated variants point into the Page-read slice): marking a position resumes reading.
- **Text marked → Create note** (Notes): marking text leads to creating a note.
- **Note edited → · Note deleted → Note** (Notes, looping back): the note's later lifecycle returns to the Note aggregate. *Exact saga intent is ambiguous — see §8.*

## 7. Actors & external systems

**Human roles** (and the commands → permissions they hold):
- **Librarian** — Purchase book, Create catalog entry, Update catalog. (Back-office / staff role.)
- **Member** — Search book, Borrow book, Arrange book, Mark reading position, Read book, Finish book, Return book, Mark text, Create/Edit/Delete note. (End-user role.)

**Automated systems / policies (⚙):**
- **AI** — drives the audio-summary pipeline (Read book → Create index → Create audio summary). External/background.
- **Notification center** — sends notifications (fires *Sent Notification*).
- **Book shelf automate** — automated variants of Arrange book and Mark reading position.

## 8. Hotspots & open questions

- **"Book not found" has no handling path.** It's a failure event with no command or flow leaving it. Likely intent: it should trigger *Notification sent* when the book later becomes available (the Notification slice reads *Search criteria*) — but no arrow connects them. Confirm the saga.
- **"Book read" means two different things.** Once in *Audio summary* (the AI ingesting a book) and once in *Reading* (a member finishing a book). Same event name, two contexts and aggregates — rename one to avoid collision.
- **Audio-summary "Book read" slice has no aggregate.** What enforces the rules / holds state for AI ingestion? Likely a *Book* or *Ingestion* aggregate is missing.
- **"Sent Notification" is a command in past tense.** Should read *Send notification*; minor but worth fixing for grammar hygiene.
- **"Retailer catalog" is green (read model) but is really external.** It's a retailer's feed, not our projection — probably belongs on a pink External-System note.
- **Role-switching nouns.** *Search criteria* and *Text marked* appear as aggregates in some slices and read models in others — decide each one's home.
- **Notes loop-back arrows.** Whether *Note edited* / *Note deleted* feed a projection, a history log, or simply denote lifecycle is unclear from the drawing.
- **Two "Lending" bubbles and two "Reading" bubbles.** Likely one context each, split visually — confirm they're not distinct contexts.