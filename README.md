# Samples DDD Meets AI

This repository contains the samples used for the book 'DDD Meets AI' by Annegret Junker.
Besides AI examples, the repository contains skills that are useful when creating software from the very first idea up to an implemented application.

**[Chapter 2—How not to design software](./Chapter02)**

**[Chapter 4—Ideation Process](./Chapter04)**

- **devils-advocate** — [BrainstormingSkill](./Chapter04/Skills/BrainstormingSkill/SKILL.md)
  <details>
  <summary>ℹ️ description</summary>

  Act as the Devil's Advocate in a brainstorming, planning, or decision session — a participant whose job is to strengthen ideas by challenging them: surfacing hidden assumptions, stress-testing plans, running premortems, steelmanning the opposite view, and naming risks and blind spots the group is avoiding. Use this skill whenever the user wants someone to "play devil's advocate," "poke holes," "pressure-test," "stress-test," "red-team," "find the flaws / the strongest objection," "challenge my thinking," "check for groupthink," or "tell me why this might fail" — and whenever a group is converging on a decision and needs scrutiny before committing, even if the phrase "devil's advocate" is never used. This is a PARTICIPANT role only: it challenges ideas, it does not run, frame, or facilitate the session, and it does not get the final say. Grounded in Osborn's deferred-judgment principle and the group-creativity research on evaluation apprehension and fixation (Mullen et al.; Paulus et al.; Wilson).
  </details>

- **business-model-canvas-critic** — [BusinessModelCanvasSkill](./Chapter04/Skills/BusinessModelCanvasSkill/SKILL.md)
  <details>
  <summary>ℹ️ description</summary>

  Act as a respectful, open Devil's Advocate who critiques a Business Model Canvas — going block by block to surface gaps, untested assumptions, and weak fit between the nine building blocks (Customer Segments, Value Propositions, Channels, Customer Relationships, Revenue Streams, Key Resources, Key Activities, Key Partnerships, Cost Structure), then stress-testing the whole as a coherent story in its environment. Use whenever someone shares or describes a Business Model Canvas or any of its blocks and wants it challenged, reviewed, pressure-tested, or "poked for holes" — like "critique my business model," "review my canvas," even if the phrase isn't used. Also use when the user gives a brainstorming/ideation session result — often a PHOTO of a whiteboard, sticky notes, mind map, or Miro/Mural board — and wants the canvas checked for fidelity to that session: dropped ideas, unsupported blocks, premature convergence, or false consensus. Grounded in Osterwalder & Pigneur, Business Model Generation (2010).
  </details>

- **impact-mapping-critic** — [ImpactMappingSkill](./Chapter04/Skills/ImpactMappingSkill/SKILL.md)
  <details>
  <summary>ℹ️ description</summary>

  Act as a respectful, open Devil's Advocate who critiques an Impact Map — working through its four levels (WHY/goal, WHO/actors, HOW/impacts, WHAT/deliverables) to surface untested assumptions, solutions posing as goals, features posing as impacts, missing actors, and unmeasurable goals, then stress-testing the whole as one coherent "in order to… the actor will… we will…" story. Use whenever someone shares or describes an impact map or an impact-mapping session and wants it challenged, reviewed, pressure-tested, or "poked for holes." The map may stand alone or be derived from upstream inputs the user can also provide — a brainstorming result (often a PHOTO of a whiteboard, sticky notes, or board) and/or a Business Model Canvas; when given, the map is checked for fidelity to them (dropped actors or segments, goal–strategy mismatch, premature convergence, false consensus). Grounded in Adzic (Impact Mapping, 2012), Heath (2020), and van Kelle, Verschatse & Baas-Schwegler (Collaborative Software Design, 2024).
  </details>

**[Chapter 5—Capability Map and Core Domain](./Chapter05)**

- **capability-map-critic** — [CapabilityMapSkill](./Chapter05/Skills/CapabilityMapSkill/SKILL.md)
  <details>
  <summary>ℹ️ description</summary>

  Act as a respectful, open Devil's Advocate who interprets and critiques a business Capability Map — reconstructing its capabilities and levels, marking each as core (differentiating), supporting, or generic (commodity), then pressure-testing whether the map holds and whether those markings are defensible. Use whenever someone shares or describes a capability map or capability model and wants it interpreted, classified, challenged, reviewed, pressure-tested, or "poked for holes" — like "interpret my capability map," "is this capability differentiating," "mark these core/supporting/ generic," or "review our TOGAF/ArchiMate capability map," even if the phrase isn't used. The map may stand alone or be checked against the strategy it serves — a North Star Metric (NSM), an impact map, and/or a Business Model Canvas; the user may provide any, all, or none, and any basic (incl. the map itself) may be missing. Grounded in The Open Group TOGAF guides Business Capabilities V2 (G211) and Capability-Based Planning (G193).
  </details>

- **capability-map-review** — [CapabilityMapCoreDomainChartSkill](./Chapter05/Skills/CapabilityMapCoreDomainChartSkill/SKILL.md)
  <details>
  <summary>ℹ️ description</summary>

  End-to-end review of a business Capability Map: runs the Devil's Advocate critique and then renders the result as a Core Domain Chart with moving points, in a single pass. Use this whenever someone shares or describes a capability map (often a photo or sticky-note board) and wants it reviewed, critiqued, interpreted, pressure-tested, or marked core/supporting/generic — even with a short prompt like "review my capability map", "critique this capability map", or "poke holes in our capabilities", and even if they never ask for a chart. Prefer this over running the critique alone when a capability map is provided for review, because the chart is the default deliverable here. Optionally cross-checks the markings against any strategic anchor the user provides (a North Star Metric, an impact map, or a Business Model Canvas). Produces a written critique plus a single .svg Core Domain Chart whose grey-to-black arrows encode the recommended moves.
  </details>

- **core-domain-chart-author** — [CoreDomainChartCreationSkill](./Chapter05/Skills/CoreDomainChartCreationSkill/SKILL.md)
  <details>
  <summary>ℹ️ description</summary>

  Create (render) a Core Domain Chart as an SVG in the Nick Tune / DDD Crew style, synthesizing a Capability Map (current positions) and a Devil's Advocate critique (recommended moves) into one picture: each capability plotted on business differentiation × model complexity, with an arrow from its Capability-Map position (grey dashed circle) to its critic-derived target (bold black circle). Use whenever someone wants to build, draw, generate, render, or produce a Core Domain Chart, plot (sub)domains or capabilities as core/supporting/generic, or visualize where capabilities should move and why — e.g. "make a core domain chart from this capability map and critique," "chart these subdomains," "show the target positions with arrows," even if they don't name the technique. Pairs with the core-domain-chart-critic (which supplies the critique). Produces a single .svg matching the bundled gold-standard example. Grounded in Millett & Tune, Patterns, Principles, and Practices of DDD.
  </details>

- **core-domain-chart-critic** — [CoreDomainChartSkill](./Chapter05/Skills/CoreDomainChartSkill/SKILL.md)
  <details>
  <summary>ℹ️ description</summary>

  Act as a respectful, open Devil's Advocate who interprets and critiques a given Core Domain Chart (Nick Tune / DDD Crew) — reconstructing each (sub)domain or bounded context's placement on the two axes (business differentiation × model complexity), its core / supporting / generic type, and the Core Domain Pattern it fits (Decisive, Short-term, Hidden, Suspect Supporting, etc.), then pressure-testing whether the placements and the strategy they imply hold. Use whenever someone shares or describes a Core Domain Chart, a core/supporting/generic subdomain plot, or a differentiation-vs-complexity portfolio and wants it interpreted, challenged, reviewed, or "poked for holes" — like "critique my core domain chart," "is this really a core domain," "where should we build vs buy," even if the phrase isn't used. Checked against whichever upstream artifacts are provided: a Business Model Canvas and a Capability Map (any or none). Grounded in Millett & Tune, Patterns, Principles, and Practices of DDD.
  </details>

- **wardley-map-critic** — [WardleyMapSkill](./Chapter05/Skills/WardleyMapSkill/SKILL.md)
  <details>
  <summary>ℹ️ description</summary>

  Act as a respectful, open Devil's Advocate who interprets and critiques a Wardley Map — reconstructing its anchor (user need), value chain, and the evolution stage (Genesis / Custom-Built / Product / Commodity) of each component, then pressure-testing whether it is even a map, whether the positions are defensible, and whether the strategy it implies survives climate, doctrine, and inertia. Use whenever someone shares or describes a Wardley Map, a value-chain-over-evolution diagram, or a situational-awareness picture and wants it interpreted, challenged, reviewed, pressure-tested, or "poked for holes" — like "critique my Wardley Map," "is this really Genesis," "should we build or buy this," even if the phrase isn't used. Grounded in Simon Wardley, *Wardley Maps: Topographical Intelligence in Business*. The map may stand alone or be checked against the strategy it serves — a North Star Metric (NSM), a Business Model Canvas, and/or an Impact Map; provide any, all, or none.
  </details>

**[Chapter 6—Domain Storytelling](./Chapter06)**

- **domain-story-critic** — [DomainStoryCritiqueSkill](./Chapter06/Skills/DomainStoryCritiqueSkill/SKILL.md)
  <details>
  <summary>ℹ️ description</summary>

  Act as a respectful, open Devil's Advocate who critiques a Domain Story (a Domain Storytelling diagram in the Hofer & Schwentner pictographic language). Work through its building blocks — actors, activities/verbs, work objects, sequence, groups/lanes — to surface CRUD-or-UI verbs posing as domain language, collapsed or missing actors, missing external systems, states dressed up as work objects, and out-of-order steps, then stress-test the whole as ONE concrete scenario (no hidden branches; consistent granularity and as-is/to-be scope). Use whenever someone shares or describes a domain story or a numbered actor→activity→work-object diagram and wants it challenged, reviewed, or "poked for holes" — even if they never say "Domain Storytelling." It may stand alone or be checked for fidelity to a related artifact the user provides: an EventStorming board, a brainstorm photo, a Wardley or Capability map, a Business Model Canvas, or a Visual Glossary whose terms the story must reuse. Grounded in Hofer & Schwentner.
  </details>

- **domain-story-seeder** — [DomainStorySeederSkill](./Chapter06/Skills/DomainStorySeederSkill/SKILL.md)
  <details>
  <summary>ℹ️ description</summary>

  Prepare a Domain Storytelling session by generating sample ("seed" or strawman) domain stories as text — numbered sentences in the actor → activity → work object grammar. Optionally derives them from a Wardley Map and/or a Capability Map: when the prompt doesn't already name a focus, the skill lists the map's components or capabilities and asks the user to pick up to three to center the stories on (the map itself is optional; a one-line domain description also works). Use whenever someone wants to seed, draft, generate, author, or prepare sample domain stories, kick off / warm up / prep a Domain Storytelling or event-storming-style workshop, turn a Wardley Map or Capability Map into a worked example scenario, or produce a numbered actor-action-work-object story to put in front of a room — even if they don't say "Domain Storytelling." This is the complement of interpreting a domain story: here you PRODUCE the story rather than read one.
  </details>

- **domain-story-interpreter** — [DomainStorytellingSkill](./Chapter06/Skills/DomainStorytellingSkill/SKILL.md)
  <details>
  <summary>ℹ️ description</summary>

  Interpret domain stories (Domain Storytelling diagrams in the Hofer & Schwentner pictographic language) and turn them into a structured prototype brief — domain model, entity state machines, actors/roles, bounded contexts, user flows, and screens. Use this skill whenever the user shares or describes a domain story, a pictographic process diagram (numbered verb-labeled arrows running between actor pictograms and work-object pictograms, often grouped into lanes or boxes), an egon.io / Domain Storyteller export, or asks to "interpret", "read", "analyze", or "build a prototype / app / data model / screens from" such a diagram — even if they never say the words "domain storytelling". Trigger it any time the input is a numbered actor→activity→work-object story that needs to be translated into something buildable.
  </details>

- **domain-story-context-finder** — [ProposeBoundedContextsForADomainStory](./Chapter06/Skills/ProposeBoundedContextsForADomainStory/SKILL.md)
  <details>
  <summary>ℹ️ description</summary>

  Detect and propose subdomains / bounded contexts from one or more Domain Stories — cutting the numbered actor→activity→work-object sentences into named contexts (the labeled lanes of a grouped domain story), justifying each cut from linguistic and behavioural evidence in the story, mapping how the contexts relate, and flagging contested sentences and alternative cuts. Use whenever someone shares or describes a domain story — a picture, an egon.io export, or plain numbered sentences — and wants to know where the boundaries are: "find the bounded contexts", "where would you cut this", "group this story into subdomains", "which lanes should this story have", "how would we split this into modules, services, or teams", or "review the lanes we already drew". Trigger even when nobody says "bounded context", "subdomain", or "Domain Storytelling", and whether the story arrives with groups drawn (then validate and refine that cut) or with none (then propose one). Grounded in Hofer & Schwentner and Evans/Vernon.
  </details>

- **styled-prototype-from-domain-story** — [PrototypeSkill](./Chapter06/Skills/PrototypeSkill/SKILL.md)
  <details>
  <summary>ℹ️ description</summary>

  Build a clickable web-app prototype by combining a Domain Story (which supplies the STRUCTURE — screens, flows, entities, roles, state machines) with a screenshot / mockup (which supplies the VISUAL STYLE — colors, typography, spacing, component looks). Orchestrates the domain-story-interpreter and webapp-style-extractor skills, then assembles a navigable, interactive prototype that behaves like the story and looks like the screenshot. Use whenever the user gives a domain story (or an actor→activity→work-object diagram) AND a screenshot / UI image and wants a working, clickable, or navigable prototype / app / mockup styled to match the screenshot — e.g. "build a prototype from this domain story in the style of this screenshot", "make a clickable app from my domain story that looks like this UI", "turn this domain story into a working prototype matching this design". Trigger even if only one input is named, as long as the goal is a styled clickable prototype from a domain story — then ask for the other.
  </details>

- **webapp-style-extractor** — [WebappStyleExtractorSkill](./Chapter06/Skills/WebappStyleExtractorSkill/SKILL.md)
  <details>
  <summary>ℹ️ description</summary>

  Extract visual formatting/styling from a SCREENSHOT of a web application and output it as machine-readable JSON — colors & typography, spacing & sizing, and component styles (buttons, inputs, cards, badges) — structured so a developer can rebuild the UI in code. Use this whenever the user gives a screenshot, image, or mockup of a web app / dashboard / website and wants the formatting, styling, design tokens, CSS values, color palette, fonts, spacing, or "how it looks" pulled out into JSON (or asks to reverse-engineer / clone / reproduce a UI's look from a picture). Trigger even if they just say "get the styling out of this screenshot" or "what colors and fonts is this using" — as long as the input is an image and they want structured formatting data back. Does NOT cover layout/DOM structure extraction, nor reading styles from live HTML/CSS (this is for images).
  </details>

**[Chapter 7—Visual Glossary](./Chapter07)**

- **domain-story-glossary-consistency** — [DomainStoryVsVisualGlossaryConsistencySkill](./Chapter07/Skills/DomainStoryVsVisualGlossaryConsistencySkill/SKILL.md)
  <details>
  <summary>ℹ️ description</summary>

  Validate that one or several Domain Stories are consistent with a Visual Glossary — term fidelity (story "Bike" vs glossary "Bicycle"), coverage both ways (undefined story terms, unused glossary terms), contradicted cardinalities, bounded-context/lane alignment, and across a set of stories, vocabulary drift and one word used in two senses. Produces a Consistency Report: a term ledger with a column per story, coded findings with severity and cited evidence, and a patch list for each side. Use whenever someone provides a Visual Glossary together with domain stories (any number, as pictures, egon.io exports, or numbered actor→activity→work-object sentences) and asks to validate, check, verify, reconcile, align, or diff them, to confirm the stories speak the ubiquitous language, or to find where story and glossary disagree — even if they never say "Visual Glossary" or "Domain Storytelling". Prefer this over a general story critique when the ask is conformance to a glossary, or when several stories are in play.
  </details>

- **prototype-from-domain-story-and-glossary** — [ProtypeSkillWithVisualGlossary](./Chapter07/Skills/ProtypeSkillWithVisualGlossary/SKILL.md)
  <details>
  <summary>ℹ️ description</summary>

  Build a clickable web-app prototype from up to three inputs, re-asked for until each is supplied or declined: a Domain Story (required — STRUCTURE: screens, flows, roles, state machines), a Visual Glossary (optional — VOCABULARY and DATA SHAPE: term names, entity vs value object, cardinalities as validation), and a formatting example screenshot (optional — VISUAL STYLE). Enhances styled-prototype-from-domain-story by adding the glossary lane and a story-vs-glossary reconciliation pass, so the prototype speaks the team's agreed ubiquitous language and enforces the cardinalities the glossary asserts. Use whenever someone wants a prototype, clickable app, or mockup from a domain story — especially when a visual glossary, term diagram, or ubiquitous language artifact is also in play, e.g. "build a prototype from this domain story and our glossary". Trigger even when only the domain story is given: then ask for the other two before building.
  </details>

- **schema-glossary-consistency** — [VisualGlossaryAgainstSchemaSkill](./Chapter07/Skills/VisualGlossaryAgainstSchemaSkill/SKILL.md)
  <details>
  <summary>ℹ️ description</summary>

  Check an API schema — OpenAPI / JSON Schema, also protobuf or SQL DDL — against a Visual Glossary and report where the implementation and the agreed domain language disagree: contradicted cardinalities, renamed or unmodelled terms, fields no term covers, is-a edges that did or didn't become allOf, positional references where the glossary has an entity, and data duplicated across linked types. Tolerant of legitimate business abstraction — it publishes what it chose NOT to report — and parses the schema first, since many findings are the schema disagreeing with itself (broken $refs, `required:` parsed as null, bad examples). Use whenever someone gives a glossary, concept map, or term diagram together with a schema, spec, YAML, .proto, or data model and asks to check, validate, compare, diff, reconcile, or align them — including short asks like "does my API match the domain model", and re-checks of a revised file ("check again"), where it reports fixed / new / still-open rather than starting over.
  </details>

- **visual-glossary-interpreter** — [VisualGlossarySkill](./Chapter07/Skills/VisualGlossarySkill/SKILL.md)
  <details>
  <summary>ℹ️ description</summary>

  Interpret a Visual Glossary — the static domain-model artifact that pins down a domain's nouns and their relationships, and that pairs with Domain Storytelling, EventStorming and Event Modeling alike — turning it into a Glossary Brief with an exact term catalogue, a relationship-and-cardinality table, a derived domain model (entities, value objects, aggregates, identity), and the open questions the picture leaves unanswered. Use whenever someone shares or describes a visual glossary, domain concept map, term or noun diagram, or any picture of concept boxes or stickies joined by verb-labeled arrows carrying multiplicities like 1, 0..1, 1..*, 1..10 or 0..25, and asks to interpret, read, explain, transcribe, review, or build a data model, schema, ERD, aggregate design, ubiquitous language or glossary document from it. Trigger even when nobody says "visual glossary", and whenever such a picture accompanies a domain story, an EventStorming board or an Event Modeling timeline.
  </details>

- **visual-glossary-updater** — [VisualGlossaryUpdateSkill](./Chapter07/Skills/VisualGlossaryUpdateSkill/SKILL.md)
  <details>
  <summary>ℹ️ description</summary>

  Redraw a Visual Glossary so it matches a newer schema. Given an older glossary picture (photo, SVG, or transcription) and the schema that has since moved ahead — OpenAPI, JSON Schema, protobuf, SQL DDL — derive the updated glossary and render it as a clean SVG, abstracting technical implementation away: identifier fields become relationships rather than stickies, arrays become cardinalities, allOf becomes is-a, enums of domain nouns become value terms, and referenced-but-not-described concepts get their own bounded-context colour. Ships a renderer with a geometry checker, so the drawing is verified rather than guessed at. Use whenever someone has both a glossary and a newer schema and wants the picture updated, adapted, regenerated, synced, or brought back in line — including asks like "make the glossary fit the API", "our diagram is stale, redraw it from the spec", or "reverse-engineer a domain picture from this schema". For diffing the two without redrawing, use schema-glossary-consistency instead.
  </details>

**[Chapter 8—Event Storming](./Chapter08)**

- **event-storming-seeder** — [EventSeederSkill](./Chapter08/Skills/EventSeederSkill/SKILL.md)
  <details>
  <summary>ℹ️ description</summary>

  Generate domain events from a rough description of a business process — a past-tense event list (the orange stickies) to seed an EventStorming board. Proposes events ONLY, the way one participant contributes them: no commands, aggregates, policies, or bounded contexts. Use whenever someone wants to produce, draft, generate, brainstorm, or seed domain events, an event list, an event timeline, or an EventStorming board from a rough process description, a few sentences about how the business works, meeting notes, or a domain story — including asks like "what events happen when a customer places an order", "turn this process into domain events", "seed an event storming session", or "give me the past-tense facts for this workflow". Trigger even when nobody says "EventStorming". This is the generative complement of the event-storming-interpreter: here you PRODUCE the events rather than read them off a board.
  </details>

- **domain-story-event-seeder** — [EventSeederWithDomainStorySkill](./Chapter08/Skills/EventSeederWithDomainStorySkill/SKILL.md)
  <details>
  <summary>ℹ️ description</summary>

  Seed an EventStorming board from Domain Stories: interprets one or several uploaded domain stories (pictures, egon.io exports, or numbered actor→activity→work-object sentences) and turns them into a past-tense list of proposed domain events — the orange stickies — with every event traced back to the sentence it came from. Use whenever someone has domain stories and wants events, an event list, an event timeline, or an EventStorming board out of them — "turn these domain stories into domain events", "seed an event storming session from our stories", "what events happen in this story", "we storyboarded this process, now give me the events". Prefer this over event-storming-seeder whenever a domain story is provided, and over domain-story-interpreter whenever the wanted output is events rather than a prototype brief. If no story is supplied and the user has none, this skill hands over to event-storming-seeder and seeds from a rough description instead. Proposes events ONLY — no commands, aggregates, policies, or bounded contexts.
  </details>

- **event-storming-interpreter** — [EventStormingSkill](./Chapter08/Skills/EventStormingSkill/SKILL.md)
  <details>
  <summary>ℹ️ description</summary>

  Interpret an EventStorming board (Alberto Brandolini's notation) from a photo or screenshot and turn it into a buildable brief: bounded contexts as modules, a domain model with aggregate state machines, commands/policies/flows, actors, external systems, and open hotspots. Use this whenever the user shares or describes an EventStorming result -- a board of colored stickies where orange = domain events (past tense), blue = commands, large yellow = aggregates, small yellow = actors, green = read models, lilac/pink = policies or external systems, red = hotspots -- usually on a left-to-right timeline clustered into bounded contexts. Also trigger when they ask to interpret, read, analyze, or build an app / data model / services from such a board, even if they never say 'EventStorming' or name the flavour, or for any orange-events-on-a-timeline sticky board, a Miro/Mural/whiteboard DDD session photo, or a request to extract domain events, aggregates, or bounded contexts.
  </details>

- **pivotal-event-boundary-finder** — [FindBoundedContextsSkill](./Chapter08/Skills/FindBoundedContextsSkill/SKILL.md)
  <details>
  <summary>ℹ️ description</summary>

  Propose pivotal events as the borders between bounded contexts — screening a domain-event timeline for the events that change the state of the whole process (phase change, irreversibility, handover of people, language shift, commitment, change of clock), vetoing each candidate with the narrow-interface test, then drawing the divider lines, naming the segments between them as candidate contexts, and specifying what crosses each border. Use whenever someone has an event timeline — an EventStorming board photo, a Miro export, a past-tense event list, or a process description — and wants the cut points: "which of these are pivotal events", "where do we draw the divider lines", "mark the boundaries on our event flow", "review the contexts the team drew", or "where does this process break into contexts or services". Tests scope-exclusion claims and finds contexts missing from the board. Trigger even when nobody says "pivotal event" or "bounded context". Grounded in Brandolini and Evans.
  </details>

- **event-storming-invariant-finder** — [FindInvariantsSkill](./Chapter08/Skills/FindInvariantsSkill/SKILL.md)
  <details>
  <summary>ℹ️ description</summary>

  Extract business rules — invariants — from an EventStorming board: the hidden preconditions a command must satisfy before its event ("a copy must be available before a book can be borrowed"), the status models of aggregates read off the state stickies, and the cardinality, ordering, authorization and lifecycle rules the board implies but never writes down. Every rule is scoped to ONE bounded context and ONE aggregate, written as a Given/When/Then scenario with the rejection it causes; rules spanning two contexts are demoted to policies with a staleness window and a compensating action. Use whenever someone has an EventStorming board, sticky photo, Miro export or command/event/aggregate list and wants the rules, invariants, constraints, preconditions, guards, state machines or status models — "what are the rules behind this board", "derive the aggregate state machines", "what must be true before this command succeeds". Trigger even when nobody says "invariant". Grounded in Brandolini, Evans and Vernon.
  </details>

**[Chapter 9—Event Modeling](./Chapter09)**

- **arazzo-from-event-model** — [ArazzoFromEventModel](./Chapter09/Skills/ArazzoFromEventModel/SKILL.md)
  <details>
  <summary>ℹ️ description</summary>

  Turn an Event Model (Dymitruk: a swimlaned timeline of State Change / State View / Automation / Translation slices, often several board photos) into an Arazzo Specification file — machine-readable API workflows describing what the USER does: one workflow per user goal, one step per user-facing slice, inputs and outputs chained with runtime expressions, successCriteria from the read models, failure actions from the guards. Use whenever someone has an Event Model, event-model-author output, or a board photo and wants Arazzo, an arazzo.yaml, an API workflow description, or runnable user journeys — "turn our board into workflows the tooling can execute". Also use when extending an existing Arazzo document so it matches the house style, and when asked to validate or lint one. Trigger even when nobody says "Arazzo" but the ask is a board's user interactions as a sequence of API calls. Produces a single .arazzo.yaml that passes Redocly lint and the bundled cross-reference check.
  </details>

- **event-model-author** — [CreateEventModelSkill](./Chapter09/Skills/CreateEventModelSkill/SKILL.md)
  <details>
  <summary>ℹ️ description</summary>

  Build an Event Model — Adam Dymitruk's technique — from one or more Domain Stories and given Bounded Contexts: one left-to-right timeline of events organized into swimlanes per context, cut into workflow slices (state change, state view, automation, translation), each with a wireframe and a Given/When/Then spec, plus an information-completeness check. Use whenever someone wants to "build an event model", "do event modeling", mentions Dymitruk or eventmodeling.org, wants a "big picture" timeline or workflow/vertical slices tying a UI to a command, event and read model, or wants a Domain Story plus its contexts turned into something buildable on one page — even unnamed. Needs at least one Domain Story (ask if none supplied) and Bounded Contexts for swimlanes (ask if missing; offer domain-story-context-finder). Found events are optional — ask if the user has some (EventStorming, domain-story-event-seeder) before deriving them.
  </details>

- **prototype-from-event-model-and-domain-story** — [CreatePrototypeFromEventModelDomainStoryVisualGlossary](./Chapter09/Skills/CreatePrototypeFromEventModelDomainStoryVisualGlossary/SKILL.md)
  <details>
  <summary>ℹ️ description</summary>

  Build a clickable web-app prototype from an Event Model (Dymitruk: swimlaned timeline cut into State Change/State View/Automation/Translation slices — may arrive as several images needing stitching) plus a required Visual Glossary (term spellings, entity/value-object/aggregate shape, cardinalities) and an optional Domain Story whose lanes should match the Event Model's swimlanes. Use whenever someone has an Event Model — from event-model-author, eventmodeling.org, or a photo of a board — and wants it turned into a clickable, navigable prototype or app. The Visual Glossary is required: ask for it if missing, and offer to derive a provisional one from the model's own fields if none exists. Ask for a Domain Story too; if still absent, build from the Event Model and Glossary alone. If Bounded Contexts from the glossary or story don't match the Event Model's grid, ask whether to reconcile them. A screenshot is optional: ask for one, and absent that, pick a coloring matching the Event Model's own topic.
  </details>

- **screen-flow-from-event-model** — [CreateScreenflow](./Chapter09/Skills/CreateScreenflow/SKILL.md)
  <details>
  <summary>ℹ️ description</summary>

  Turn an Event Model (Dymitruk: a swimlaned timeline cut into State Change / State View / Automation / Translation slices, often arriving as several board photos that need stitching) into a SIMPLE screen flow as a Mermaid flowchart — screens as nodes, the command or policy that moves the user as labelled edges, plus a screen table, a transition table and a reachability check. Use whenever someone has an Event Model, an event-modeling board, or event-model-author output and wants the screens, the navigation, the UI or user flow, a sitemap, a click path, or "which screens do we need and how do you get from one to the next" — including "make a screen flow from these images", "turn this event model into a mermaid diagram", "draw the navigation for this board", or an uploaded board photo with "what are the screens?". Trigger even when nobody says Event Model, screen flow or Mermaid. Draws only what the board draws — missing screens are reported as open questions, never invented.
  </details>

- **event-model-invariant-finder** — [FindInvariantsInGivenModel](./Chapter09/Skills/FindInvariantsInGivenModel/SKILL.md)
  <details>
  <summary>ℹ️ description</summary>

  Extract business rules (invariants) and Given/When/Then Gherkin from an Event Model (Dymitruk: a shared timeline in swimlanes, cut into State Change/State View/Automation/Translation slices, each with a happy-path Given/When/Then). Mines the guards that make a slice's command fail, the state machines the slice order implies, the policies an Automation slice already names, and the border contracts a Translation slice already draws — then writes the violation scenarios the happy path omits. Use whenever someone has an Event Model (from event-model-author, eventmodeling.org, or a photo of one) and wants its rules, invariants, guards, preconditions, state machines, or acceptance tests — "what would make this command fail", "derive the state machine for this slice", "write acceptance tests for this model". Trigger even without "invariant" or "Gherkin". Sibling of event-storming-invariant-finder (same categories, from a raw discovery board instead). Grounded in Dymitruk, Evans, Vernon.
  </details>

**[Chapter 10—Context Map](./Chapter10)**

- **event-storming-context-mapper** — [ContextMapSkill](./Chapter10/Skills/ContextMapSkill/SKILL.md)
  <details>
  <summary>ℹ️ description</summary>

  Draw a Context Map from an EventStorming board: collapse the same context drawn several times along the timeline into one node, work out from the stickies who OWNS each business object and who merely reads it, then give every border a direction (upstream/downstream), a relationship pattern (Open Host Service, Customer/Supplier, Conformist, Anticorruption Layer, Shared Kernel, Partnership, Separate Ways), a contract of what crosses and what stays behind, and an integration mechanism with a staleness window. Reports contested ownership, nouns everyone reads and nobody writes, orphan objects, and the edges the business needs but nobody drew. Use whenever someone has an EventStorming board, sticky photo, Miro export or a list of bounded contexts and wants the context map, the integration picture, upstream/downstream directions or ACLs: "draw the context map for this board", "which context owns Bicycle", "what crosses this border", "review the map we drew". Trigger even when nobody says "context map".
  </details>

- **context-map-board-consistency** — [ContextBoardEventStormingConsistency](./Chapter10/Skills/ContextBoardEventStormingConsistency/SKILL.md)
  <details>
  <summary>ℹ️ description</summary>

  Check a Context Map against the EventStorming board it came from and report where the map claims what the stickies do not support: arrows reversed against the board's producer/consumer evidence, edges with nothing crossing them, one context drawn twice on the board turned into two nodes, ownership the board contradicts, patterns with no evidence, contracts naming payloads no event produces, external systems and upstreams missing, and board silences presented as a working system. Produces a Conformance Report: a term ledger of every noun by who writes it, reads it and owns it, coded findings with severity and cited sticky evidence, and two patch lists. Use whenever someone has a board (photo, Miro export, sticky list) plus a context map, integration diagram or list of bounded contexts and asks to check, validate, diff, reconcile or review them: "does our context map match the board", "did we get upstream/downstream right". Re-checks report fixed / new / still-open. Trigger even when nobody says "context map".
  </details>

- **bounded-context-canvas-author** — [BoundedContextCanvasSkill](./Chapter10/Skills/BoundedContextCanvasSkill/SKILL.md)
  <details>
  <summary>ℹ️ description</summary>

  Write a Bounded Context Canvas (DDD Crew) for every bounded context on a context map — one Markdown file per context, each carrying the canvas as a Mermaid flowchart: inbound collaborators and their messages on the left, purpose, classification, domain roles, ubiquitous language and business decisions in the middle, outbound messages on the right, assumptions, metrics and open questions beneath. Derives inbound/outbound from the map's own border contracts and business decisions from an invariants sheet — never inventing a field, and reporting every outbound message no other canvas claims as inbound. Use whenever someone has a context map, a list of bounded contexts, an EventStorming board with bubbles or a domain-story cut and wants canvases, a canvas per context, "one file per bounded context", or "turn our context map into bounded context canvases" — and whenever someone asks to document what each bounded context owns, accepts and emits. Trigger even when nobody says "Bounded Context Canvas" or "Mermaid".
  </details>

**[Chapter 11—Knowledge Graph & Lightweight ADRs](./Chapter11)**

- **domain-knowledge-graph** — [KnowledgeGraphSkill](./Chapter11/Skills/KnowledgeGraphSkill/SKILL.md)
  <details>
  <summary>ℹ️ description</summary>

  Build and grow ONE browsable knowledge graph, in Turtle/RDF, from the artifacts a modelling effort produces — a brainstorm result first, then a Business Model Canvas, Wardley Map, Impact Map, Capability Map, Core Domain Chart, Domain Stories, Visual Glossaries, EventStorming boards and Event Models as they arrive. Each ingest merges into the existing graph rather than replacing it, keeps every artifact's own spelling and provenance, and reports what newly connects, what contradicts and what is orphaned. Use whenever someone wants artifacts put into a graph, linked, cross-referenced or traced: "make a knowledge graph of this brainstorm", "add our canvas to the graph", "how does our Wardley map connect to the event storming board", "what happened to the ideas from the workshop". Trigger on any request to combine, reconcile or navigate two or more modelling artifacts together, and on follow-ups adding one more — even if nobody says "knowledge graph", "RDF" or "ontology". Outputs a .ttl file plus browsable views.
  </details>

- **xlsx-to-md** — [Xlsx_to_md](./Chapter11/Skills/Xlsx_to_md/SKILL.md)
  <details>
  <summary>ℹ️ description</summary>

  Convert an Excel workbook (.xlsx) into plain GitHub-flavored Markdown tables using scripts/xlsx_to_md.py. Produces one .md file per sheet (named 'workbook-SheetName.md', or just 'workbook.md' for a single-sheet file), each containing only the raw table — no titles or metadata headers. Use this skill whenever the user asks to convert, export, or turn an Excel/xlsx file (or a specific sheet/table in one) into Markdown, a markdown table, a .md file, or similar phrasing like 'excel table to md' — even if they don't name this skill directly. Do NOT use for creating or editing xlsx files (use the xlsx skill for that), and do not use when the deliverable should be a Word doc, PDF, or other non-Markdown format.
  </details>

**[Chapter 12—API Specifications](./Chapter12)**

- **asyncapi-spec-author** — [AsyncApiSkill](./Chapter12/Skills/AsyncApiSkill/SKILL.md)
  <details>
  <summary>ℹ️ description</summary>

  Author AsyncAPI 3.1.0 event-driven API specifications in YAML following a consistent, componentized house style (the same style as the bundled Task Management example). Use this skill whenever the user wants to create, write, draft, design, scaffold, or extend an AsyncAPI specification, model an event-driven / message-driven / pub-sub / streaming API, describe Kafka, MQTT, AMQP, WebSocket, or other broker topics, channels, messages, or events, or produce an asyncapi.yaml file — even if they don't say "AsyncAPI 3.1.0" explicitly. Also use it when adding channels, operations, messages, or schemas to an existing spec so the additions match the house conventions, and when the user asks to validate or lint an AsyncAPI document. Produces a single YAML file that validates against the AsyncAPI 3.1.0 spec and passes Spectral linting.
  </details>

- **openapi-spec-author** — [OpenApiSkill](./Chapter12/Skills/OpenApiSkill/SKILL.md)
  <details>
  <summary>ℹ️ description</summary>

  Author OpenAPI 3.1.0 API specifications in YAML following a consistent, componentized house style (the same style as the bundled Catalog Management example). Use this skill whenever the user wants to create, write, draft, design, scaffold, or extend an OpenAPI / Swagger specification, design a REST API contract, turn a feature or data model into API endpoints, or produce an api.yaml / openapi.yaml file — even if they don't say "OpenAPI 3.1.0" explicitly. Also use it when adding endpoints or schemas to an existing spec so the additions match the house conventions, and when the user asks to validate or lint a spec. Produces a single YAML file that passes Spectral linting.
  </details>

- **protobuf-model-author** — [ProtobufSkill](./Chapter12/Skills/ProtobufSkill/SKILL.md)
  <details>
  <summary>ℹ️ description</summary>

  Turn a Visual Glossary into a Protocol Buffers model — proto3 `.proto` files whose messages, enums, packages and field numbering follow a consistent house style (the same style as the bundled book-catalog example). Use this skill whenever someone wants protobuf, proto3, a `.proto` file, a protobuf schema, an IDL, or a gRPC/wire data model built from a visual glossary, glossary brief, domain concept map, term or noun diagram, ubiquitous language, or a derived domain model of entities, value objects and aggregates — including phrasings like "turn this glossary into protobuf", "generate proto messages from these terms", "model this domain in protobuf", or a glossary picture shared alongside a request for a schema. Trigger even when nobody says "Visual Glossary". Also use it when extending an existing `.proto` so additions match the conventions, and when the user asks to compile, validate or lint protobuf. Produces `.proto` files that compile and pass `buf lint`.
  </details>


The samples are collected per chapter.

E.g., prompts as md or even generated code bases in corresponding sub-folders.
