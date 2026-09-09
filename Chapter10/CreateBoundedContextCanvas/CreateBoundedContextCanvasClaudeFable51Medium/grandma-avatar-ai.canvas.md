# Bounded Context Canvas — Grandma Avatar AI

> **Source:** Community Cooking context map (ContextMap.jpg) + Visual Glossary (VisualGlossaryEnhanced.jpg); business decisions from invariants-community-cooking-board.md; purpose lines from the pivotal-event cuts and the context cut · **Mode:** derived from the team's cut
> **Provenance:** `(given)` stated by a source · `(derived)` read off one ·
> `(proposed)` mine, confirm it · `(unknown)` nothing to derive from.

## Canvas

```mermaid
---
config:
  themeVariables:
    fontSize: 22px
    fontFamily: Arial
  flowchart:
    htmlLabels: true
    padding: 12
    nodeSpacing: 30
    rankSpacing: 30
    wrappingWidth: 380
---
%% Bounded Context Canvas — Grandma Avatar AI
%% Source: Community Cooking context map + Visual Glossary · derived 2026-09-09 · provenance in the field tables below
flowchart TB

  classDef panel fill:#FFF9D6,stroke:#C9B458,stroke-width:1px,color:#1A1A1A
  classDef head fill:#FDE68A,stroke:#B08900,stroke-width:2px,color:#1A1A1A
  classDef collab fill:#EAF2FB,stroke:#6E9BD1,stroke-width:1px,color:#1A1A1A
  classDef offboard fill:#FFFFFF,stroke:#B0B0B0,stroke-width:1px,stroke-dasharray:4 3,color:#5A5A5A
  classDef unknown fill:#F4F4F4,stroke:#B0B0B0,stroke-width:1px,color:#7A7A7A
  classDef frame fill:none,stroke:none
  classDef term fill:#FDF6C8,stroke:#C9B458,stroke-width:1px,color:#1A1A1A
  classDef termgap fill:#FFFFFF,stroke:#B0B0B0,stroke-width:1px,stroke-dasharray:4 3,color:#5A5A5A

  subgraph TOP[" "]
    direction LR

    subgraph IN["Inbound communication"]
      in_cooking_assistance["<b>Cooking Assistance</b><br/><i>via ACL</i><br/>evt · Help request"]
    end

    subgraph BCX["Grandma Avatar AI"]
      direction TB
      bc["<b>Grandma Avatar AI</b><br/>Answers help requests as a machine responder, standing in for or alongside the human community, behind an anticorruption layer."]
      strat["<b>Strategic classification</b><br/>Domain — <i>unknown</i><br/>Business model — <i>unknown</i><br/>Evolution — <i>unknown</i>"]
      roles["<b>Domain roles</b><br/>Interchange · Gateway"]
      subgraph LANG["Ubiquitous language"]
        t_grandma["<b>Grandma Avatar</b>"]
        t_help_provider["<b>Help provider</b><br/><i>owned by nobody on the map</i>"]
        t_help_request["<b>Help Request</b><br/><i>contested — also on this bubble as a business object</i>"]
        t_help["<b>Help</b><br/><i>contested — map: Help response</i>"]
        t_grandma -.->|"is"| t_help_provider
        t_help_request -->|"at 1"| t_grandma
        t_grandma -->|"provides 0..*"| t_help
      end
      rules["<b>Business decisions</b><br/>An avatar answer is labelled machine-generated where shown <i>(INV-HELP-07 — enforced by Cooking Assistance, not here)</i><br/><i>none owned here (see below)</i>"]
      bc ~~~ strat
      strat ~~~ roles
      roles ~~~ LANG
      LANG ~~~ rules
    end

    subgraph OUT["Outbound communication"]
      out_cooking_assistance["<b>Cooking Assistance</b><br/><i>via ACL</i><br/>evt · Help response"]
    end

    IN --> BCX
    BCX --> OUT
  end

  subgraph META["Assumptions · Verification metrics · Open questions"]
    assume["<b>Assumptions</b><br/>The ACL on this bubble's own edge means this context translates, not Cooking Assistance<br/>The duplicated events and objects are read as a translated copy, not co-ownership"]
    metrics["<b>Verification metrics</b><br/><i>none supplied</i>"]
    quest["<b>Open questions</b><br/>Does the avatar own a Help request, or hold a translated copy?<br/>Who decides the avatar answers — is it told, or does it watch?<br/><i>…and 2 more (see below)</i>"]
    assume ~~~ metrics
    metrics ~~~ quest
  end

  TOP ~~~ META

  class bc head
  class strat,roles,rules,assume,quest panel
  class t_grandma term
  class t_help_provider,t_help_request,t_help termgap
  class metrics unknown
  class in_cooking_assistance,out_cooking_assistance collab
  class TOP frame
```

## Purpose  (derived)

Answers help requests as a machine responder — standing in for, or alongside,
the human community — and hands the answer back to Cooking Assistance through
an anticorruption layer.

Derived from the bubble (the same two events and two objects as Cooking
Assistance, behind an ACL) and the story cut's treatment of the avatar. **The
prior analyses in this project all placed the avatar *inside* the help
context**; the map draws it out, and this canvas follows the map.

## Strategic classification  (unknown)

| | |
|---|---|
| Domain | `(unknown)` — the story cut's §8 says explicitly this is "a bought or built capability inside Community Help, not a subdomain"; the map disagrees by drawing it. Nobody has classified it. |
| Business model | `(unknown)` |
| Evolution | `(unknown)` — "a differentiator or a commodity LLM behind a persona is a strategy question" (story cut). |

## Domain roles  (derived)

- **Interchange** — the ACL on its edge says it translates between Cooking
  Assistance's *Help request* and whatever a language model consumes, owning
  neither model.
- **Gateway** — it routes between the inside (a request) and something outside
  the map (an AI), and its whole traffic is request-in, answer-out.

Rejected: *Segregated core* — only if the avatar is the differentiator, which
nobody has decided.

## Inbound communication  (derived)

| Collaborator | Message | Type | What it carries | Evidence |
|---|---|---|---|---|
| Cooking Assistance `(via ACL)` | Help request | evt | the request, in Cooking Assistance's model, for the avatar to answer | yellow Help request sticky beside the rising dashed arrow |



## Outbound communication  (derived)

| Message | Type | Collaborator | What it carries | Evidence |
|---|---|---|---|---|
| Help response | evt | Cooking Assistance `(via ACL)` | a machine-generated answer, translated back | yellow Help response sticky beside the descending dashed arrow |

Asynchronous both ways — the only pair of dashed arrows on the map that
returns. The avatar can answer late, and Cooking Assistance must cope with a
human answer and an avatar answer arriving in either order.

## Ubiquitous language  (given for Grandma Avatar — from the Visual Glossary; ownership of the rest contested)

The panel above draws this table; it is repeated here because the picture caps what fits and a borrowed sense needs a sentence.

| Term | What it means *here* | Owned or borrowed | Cardinality (glossary) |
|---|---|---|---|
| Grandma Avatar | the persona that answers; the glossary term names this context's responder | **owned** | Help Request at **1** · provides **0..\*** Help · Thanks to **0..1** · is a Help provider |
| Help Request | a request to answer | **contested** — the bubble carries *Help request* as a business object and *Help requested* as an event, exactly as Cooking Assistance does; the ACL says this context has its *own* model of it, which is the opposite of sharing the object | at **1** Grandma Avatar |
| Help | the answer; map: *Help response* | **contested** — same duplication | Grandma Avatar provides **0..\*** |
| Help provider | what the avatar *is*, per the glossary | **undefined owner** | — |

**The bubble is a copy of Cooking Assistance's.** Two events, two objects,
identical names. With an ACL between them that is either (a) the avatar keeps
a translated copy — fine, and the duplication is the ACL working — or (b) two
contexts write one aggregate — the "implement it twice" trap the pivotal-event
re-run warned about. The map cannot tell which.

## Business decisions  (unknown — the invariants sheet has no context for the avatar)

**None owned here.** The invariants sheet placed the avatar inside the help
context and wrote its one rule there:

- `INV-HELP-07` — an avatar answer is labelled machine-generated wherever it is
  shown. Enforced by whoever *shows* the answer, i.e. Cooking Assistance, not
  by the avatar.

The rule that would make this context decide anything is X-04 — "if nobody
answers in *n* minutes, the avatar answers" — and that needs Cooking
Assistance's request state, so it is a **policy across the ACL**, not a
business decision here. Whose policy it is (does the avatar watch, or is it
told?) is open question 2.

## Assumptions  (derived)

- The ACL box sits on **this** context's edge, so the map is saying the avatar
  translates Cooking Assistance's model into its own. The prior analyses had
  the ACL the other way round (help context wraps the avatar). Followed the
  map.
- The duplicated *Help requested* / *Help provided* events and *Help request* /
  *Help response* objects are read as a translated copy behind the ACL, not as
  co-ownership — the more charitable reading, and the one open question 1
  should confirm.
- Both dashed stickies are read as events, named as the map names them.

## Verification metrics  (unknown)

`none supplied` — neither the map nor the glossary states one.

`(proposed)`: share of requests the avatar answers first; share of avatar answers later superseded by a human; share of avatar answers thanked.

## Open questions

1. **Does this context own a *Help request*, or hold a translated copy of Cooking Assistance's?** The bubble duplicates the object; the ACL implies a copy. *(Settles the ubiquitous language on both canvases.)*
2. **Who decides that the avatar answers?** Always, in parallel with humans (`Help Request at 1 Grandma Avatar` says every request reaches it), or only after *n* minutes unanswered (X-04)? *(Settles whether the inbound event is a request or a fallback trigger, and whose policy X-04 is.)*
3. **Is this a context or a capability inside Cooking Assistance?** Three prior analyses said inside; the map says beside. *(Settles whether this canvas should exist.)*
4. **Can the avatar be thanked?** The glossary says yes (`Thanks to 0..1 Grandma Avatar`); the invariants sheet asked. *(Settles a business decision on Sharing's canvas.)*

## Border reconciliation

| Message | Direction | Counterpart | Status |
|---|---|---|---|
| Help request | in, from Cooking Assistance | `cooking-assistance.canvas.md` | matched — `evt` both sides |
| Help response | out, to Cooking Assistance | `cooking-assistance.canvas.md` | matched — `evt` both sides |

Both messages pair with Cooking Assistance, `evt` both sides, both via the ACL.
