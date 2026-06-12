# The Domain Storytelling pictographic language

Reference for reading the notation. Based on Hofer & Schwentner, *Domain Storytelling* (2021) and domainstorytelling.org. Read this when a diagram element is ambiguous.

## Building blocks

| Block | What it looks like | Meaning |
|---|---|---|
| **Actor** | A person/role pictogram, or a system pictogram, with a noun label | Plays an *active* role — initiates activities. A person → a user role. A system → an external service. |
| **Work object** | A pictogram (document, phone, folder, physical thing…) with a noun label | Something an actor works **on** or **with**. The data and things the process touches. |
| **Activity** | An arrow labeled with a **verb**, carrying a **sequence number** at its origin | What an actor does with a work object. The predicate of the sentence. |
| **Sequence number** | The number prefixing the verb (1, 2, 3…) | Reading order of the sentences. May run across several lanes. |
| **Annotation** | Free text near an element | Notes, assumptions, domain-term explanations, variations. |
| **Group** | An outline/box/lane around part of the story, usually labeled | Clusters related parts: most often a **subdomain/bounded context**, sometimes "optional", sometimes a variant. |
| **Modeling canvas** | The whole drawing area | One scenario. |

Colors, when present, are just an extra grouping signal — treat them like groups.

## Grammar of a sentence

A domain-story sentence is built as:

```
Actor  +  (number) verb  +  work object  [ + preposition + work object ]*
```

- **Actors and work objects are nouns; activities are verbs.** If a label reads like a verb, it's an activity arrow; if it's a noun, it's an actor or work object.
- The **first arrow** out of the actor is numbered and carries the main verb.
- **Follow-on arrows** are unnumbered and carry a linking word — a preposition (`to`, `at`, `with`, `from`, `in`, `near`, `via`) or a light verb (`and`, `and takes`, `and shares`) — chaining further work objects into the same sentence.
- Read the chain as one sentence. Example: `Commuter —(7 unlocks)→ Bicycle —(with)→ Code —(at)→ Rack` = "The commuter unlocks the bicycle with the code at the rack."

## Two rules that matter for interpretation

**No conditionals.** A domain story never branches inside an arrow. There is no if/else, no loops. A story is a single concrete walk-through. Alternatives are drawn as *separate numbered sentences* (e.g. one sentence for "finishes", another for "rejects") or captured in *separate stories* (a happy path and an error path). When you interpret for a prototype, you must reconstruct the branching the story implies and label it as inferred.

**Concrete, not exhaustive.** A story shows one real example ("the moviegoer watches a film"), not the full space of possibilities. Treat anything not drawn as unknown, not as nonexistent. Multiplicities (one-to-many), validation, and error handling are typically *not* in the story.

## Reading mechanics for images

- Find each number; its arrow's tail touches the **actor** for that sentence and its head points at the first **work object**.
- A single actor often fans out several numbered arrows (one Commuter doing steps 2–10). Same pictogram, same actor.
- The same noun appearing in several places with different adjectives ("New task", "Task", "Task done") is **one entity in different states**, not several entities. This is the strongest signal of a state machine.
- Lane labels (often right- or top-aligned) name the **subdomain** that the sentences in that lane belong to.
- A box labeled **optional** marks skippable steps.

## Common tools / export formats

The usual modeling tool is **egon.io** (the Domain Storyteller). It can export a story as a `.dst` / `.egn` JSON file (actors, workobjects, activities with `from`/`to`/`number`, and groups) and as SVG/PNG. If given a JSON export, parse the activities by their `number` field to recover sentence order rather than reading pixels.