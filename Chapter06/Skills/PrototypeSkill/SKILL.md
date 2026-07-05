---
name: styled-prototype-from-domain-story
description: >-
  Build a clickable web-app prototype by combining a Domain Story (which supplies the
  STRUCTURE — screens, flows, entities, roles, state machines) with a screenshot /
  mockup (which supplies the VISUAL STYLE — colors, typography, spacing, component
  looks). Orchestrates the domain-story-interpreter and webapp-style-extractor skills,
  then assembles a navigable, interactive prototype that behaves like the story and
  looks like the screenshot. Use whenever the user gives a domain story (or an
  actor→activity→work-object diagram) AND a screenshot / UI image and
  wants a working, clickable, or navigable prototype / app / mockup styled to match
  the screenshot — e.g. "build a prototype from this domain story in the style of this
  screenshot", "make a clickable app from my domain story that looks like this UI",
  "turn this domain story into a working prototype matching this design". Trigger even
  if only one input is named, as long as the goal is a styled clickable prototype from
  a domain story — then ask for the other.
---

# Styled Prototype from a Domain Story

Produce a **clickable web-app prototype** from two inputs that answer two different
questions:

- A **Domain Story** answers *what does it do* — the screens, the navigation, the
  entities, the roles, and the state transitions the prototype must support.
- A **screenshot / mockup** answers *what does it look like* — the exact colors,
  typography, spacing, and component styling to reproduce.

Your job is to interpret each with the right existing skill, then **assemble one
prototype that behaves like the story and looks like the screenshot**.

## The one idea that makes this work

The two inputs live in **separate lanes and must not leak into each other**:

- Everything **structural or behavioral** (which screens exist, what buttons do,
  what statuses an entity can hold, who can see what) comes **only from the domain
  story**. Do not invent screens, features, or flows the story doesn't justify —
  surface them as open questions instead.
- Everything **visual** (hex values, font sizes, radii, paddings, component looks)
  comes **only from the extracted style spec**. Do not eyeball or invent a palette
  or type identity — if the screenshot didn't show it, derive it from the extracted
  tokens and say so.

When you catch yourself adding a screen "because the design has room for it," or a
color "because it would look nice," stop — that's a lane violation. The prototype is
faithful on both axes or it isn't trustworthy.

## Inputs

Both inputs usually arrive as uploaded files under `/mnt/user-data/uploads/`.

- **Domain story**: a pictographic diagram (image), an egon.io export, or a written
  actor→activity→work-object story. May already be an interpreted brief.
- **Screenshot**: an image of the target UI (web app, dashboard, mockup).

If only one input is present, do that half and **ask for the other** before
building — you cannot fake structure from a screenshot or style from a story.

## Workflow

Run the two interpretation tracks first (either order — they're independent), then
reconcile and build.

### Track A — Extract the visual style

Consult the **webapp-style-extractor** skill (`/mnt/skills/user/webapp-style-extractor/SKILL.md`)
and run it against the screenshot. You want its JSON output: `colors` (roles +
palette), `typography` (named text styles), `spacing` (base unit + scale), and
`components` (button, input, card, badge, nav item… with measured fills, radii,
paddings, fonts, shadows). Sample values from pixels — never eyeball them. Keep the
`meta.notes` caveats; you'll pass the honest ones through to the final readme.

### Track B — Interpret the domain story

Consult the **domain-story-interpreter** skill (`/mnt/skills/user/domain-story-interpreter/SKILL.md`)
and run it against the story to produce the **Prototype Brief**. You specifically
need: modules (§3), domain model (§4), **state machines (§5)**, use-cases &
user-journey (§6), screens & navigation (§7), actors/roles (§2), and open questions
(§8). If the story is a non-trivial image, follow the interpreter's rule and
**confirm your transcription with the user before building** — a misread is cheap to
fix now and expensive to fix in code.

### Track C — Reconcile into a build plan

Merge the two outputs into one plan. Map them explicitly (write this table out — it
is the spec the build follows):

| From the Prototype Brief | Becomes in the prototype | Styled using (from style spec) |
|---|---|---|
| Modules (§3) | Top-level app sections / nav areas | nav-item component + surface colors |
| Screens (§7) | Individual routes/views | card / list / form components + spacing scale |
| State machines (§5) | Status badges + the buttons that transition them | badge component + primary/secondary button styles |
| Use-case order (§6) | The click-through navigation flow | — |
| Domain model (§4) | Shape of the seed/mock data + form fields | input component + typography styles |
| Actors & roles (§2) | Role gating + a role switcher affordance | — |
| Open questions (§8) | Decisions you flag, **not** silently fill | — |

Two reconciliation rules:

1. **The screenshot is the design direction; fidelity beats flair.** You may consult
   the **frontend-design** skill for build *craft* — CSS specificity discipline,
   visible focus states, responsive down to mobile, respecting reduced motion, and
   clear interface copy. But do **not** spend its "take an aesthetic risk / invent a
   distinctive identity" latitude here: the identity is fixed by the screenshot.
   Every color and type decision derives from the extracted tokens, not from a fresh
   palette.
2. **Screenshots show a subset of components.** When the story needs a UI element the
   screenshot never displayed (a modal, an empty state, a status the screenshot
   didn't include), synthesize it from the *established tokens* — the palette,
   radius, spacing scale, and nearest existing component — so it feels native, and
   note it as extrapolated in the readme.

### Track D — Build the clickable prototype

Build a **single, self-contained app** (a React artifact or a standalone HTML file —
whichever the environment renders inline) with:

- **A token layer first.** Turn the style spec into CSS variables / a theme object:
  color roles, the named typography styles, and the spacing scale. Everything else
  references these — no hard-coded hex or px scattered through the markup.
- **Reusable components built to the extracted specs.** Button, Card, Badge, Input,
  NavItem etc., each matching its `components` entry (fill, radius, padding, font,
  shadow). Assemble screens from these, so the look stays consistent and on-brand.
- **A navigable spine.** Every screen from §7 is reachable through in-app navigation
  following the §6 journey order. "Clickable" means you can actually move around.
- **A live state machine.** Wire each transition button (§5) to update in-memory
  state and re-render — moving an entity New → Assigned → In progress → Done, with
  its badge and available actions updating. This interactivity is what separates a
  prototype from a static mock.
- **Seed data in meaningful states.** Populate a few example entities (the story's
  concrete scenario gives you at least one realistic instance) across the relevant
  statuses so no screen is empty.
- **A role switcher.** Since a prototype has no real auth, add a lightweight
  role/persona selector so a reviewer can see the role-gated screens from §2. Label
  it clearly as a prototype affordance.

Constraints for this environment: keep all state **in memory** — do **not** use
`localStorage`/`sessionStorage` (they fail in claude.ai artifacts). Use mock data,
not a real backend.

### Track E — Validate and deliver

Before handing over, check:

- Every screen in §7 is reachable; no dead links.
- Every state transition in §5 has a working control and updates the badge/actions.
- All visual values trace to the style spec (or to a token-derived extrapolation you
  noted) — nothing eyeballed.
- If your environment can screenshot the rendered result, compare it against the
  source screenshot and fix drift in palette, type, and spacing.

Save the prototype to `/mnt/user-data/outputs/`, present it, and include a short
**readme** that: (a) maps each built feature back to the story sentence/state it
came from, (b) lists the style caveats carried over from `meta.notes` (font families
and shadows are usually best-guesses), (c) names anything extrapolated beyond what
the screenshot showed, and (d) restates the brief's **open questions** the reviewer
still needs to decide. Keep it tight — the prototype is the deliverable.

## Worked sketch (illustrative)

Input: a task-management domain story (Craftsman *assigns* Task to Craftsman; Task
moves New → Assigned → In progress → Done / Rejected) **plus** a screenshot of a
Linear-style board.

- Track A → tokens: near-white surfaces, one indigo `primary`, 8px base unit, cards
  with `8px` radius and a subtle shadow, 13px medium nav labels.
- Track B → brief: one **Task** entity with a 5-state machine; roles **Manager** and
  **Assignee**; screens *Board*, *Task detail*, *Assign task*.
- Track C/D → a board whose columns **are** the Task states, cards styled to the
  extracted card spec, a status badge per Task, an "Assign" button (Manager only) and
  Start/Complete/Reject buttons (Assignee) that fire the §5 transitions, seeded with
  a couple of tasks, plus a Manager/Assignee switcher.
- Track E → readme notes the "Rejected" column came from an inferred branch in the
  story, and that the font family is a guess flagged by the extractor.

The columns come from the **story**; the card, badge, and button looks come from the
**screenshot**. Neither lane invents the other's content.

## Common pitfalls

- **Letting the design invent structure.** Extra screens/tabs "to fill the layout"
  are lane violations — build only what the story justifies.
- **Letting the story invent style.** Reaching for a default palette or a "nice"
  accent instead of the sampled tokens. Every visual value comes from the extractor.
- **A pretty but dead mock.** If the transition buttons don't change state, it isn't
  a clickable prototype. Wire the state machine.
- **Silently overriding the screenshot with frontend-design flair.** Craft yes,
  new identity no. The screenshot is the brand.
- **Empty screens.** Seed data in the states each screen is meant to show.