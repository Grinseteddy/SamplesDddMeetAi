# Cooking Assistance — Model Decision Record

## Context

Java 17, root package `cookingassistance.domain`. Sources: the attached `HelpNew.jpg` and `HelpRequestNew.jpg`. This is a glossary-derived initial domain model, with the explicit provisional decisions below. No other bounded context is implemented. Recipe, ingredient, howTo Step, requester and provider identities are treated as opaque references; their owning contexts are not named in these diagrams and are not invented here.

Two aggregate roots: **Help** and **Help Request**. Source names follow Java casing, with spaces removed. Shared nested records and enums reduce file noise. All entity equality is by typed ID; all value records are immutable and structural. Required leaves enforce presence, not undocumented non-blank/range/URL rules. There are no persistence or framework annotations.

## Glossary transcription — terms and attributes

| Term (source spelling) | Context / placement | Attributes drawn |
|---|---|---|
| Help | Cooking Assistance, aggregate | help Id, help request, help requester, answer title, help Provider type, help Provider, answer, isAccepted |
| Help Request | Cooking Assistance, aggregate | help request Id, requester, title, type, description, recipe, howTo Step, ingredients, preferred Provider, status, picture |
| answer | Help | answerType; oneOf four answer payloads |
| substitutes | answer | recipe, Substitute(s), answerType |
| Substitute | substitutes | Ingredient, substitute ingredient |
| substitute ingredient | Substitute | Name, Value, Unit |
| preparation step explanation | answer | recipe, howTo Step, description, images, answerType |
| catastrophe mitigation | answer | recipe, explanation, answerType |
| menu proposal | answer | note, servings, meal, how to serve, course(s), answerType |
| course | menu proposal | dish, recipe |
| help Id, help request Id | identifier leaves | UUID examples |
| help request, help requester, requester, help Provider, recipe, howTo Step, ingredients, Ingredient | reference leaves | UUID examples |
| answer title, title, description, explanation, Name, note, how to serve | descriptive leaves | text examples where present |
| Value, servings, dish | quantity / number leaves | numerical examples for servings and dish |
| picture, images | media leaves | URL examples |
| type, answerType, help Provider type, preferred Provider, status, meal, Unit, isAccepted | discriminant / state leaves | alternatives below; isAccepted example true |

## Glossary transcription — relationships

Cardinalities are target counts per source. Reverse cardinalities are not drawn.

| From | Verb / label | To | Cardinality |
|---|---|---|---|
| Help | contains | help Id; answer title; help Provider type; answer; isAccepted | 1 each |
| Help | refers to | help request; help requester | 1 each |
| Help | contains | help Provider | 0..1 |
| answer | contains | answerType | 1 |
| answer | oneOf | substitutes; preparation step explanation; catastrophe mitigation; menu proposal | 0..1 each, interpreted exactly one in total |
| substitutes | contains | recipe; answerType | 1 each |
| substitutes | contains | Substitute | 1..* |
| Substitute | contains | Ingredient; substitute ingredient | 1 each |
| substitute ingredient | contains | Name; Value; Unit | 1 each (Name arrow number unclear; assumed 1) |
| preparation step explanation | contains | recipe; howTo Step; description; answerType | 1 each |
| preparation step explanation | contains | images | 0..* |
| catastrophe mitigation | contains | recipe | 0..1 |
| catastrophe mitigation | contains | explanation; answerType | 1 each |
| menu proposal | contains | note; servings; meal; answerType | 1 each |
| menu proposal | contains | how to serve | 0..1 |
| menu proposal | contains | course | 1..10 |
| course | contains | dish; recipe | 1 each |
| Help Request | contains | help request Id; requester; title; type; description; status | 1 each |
| Help Request | contains | recipe; howTo Step | 0..1 each |
| Help Request | contains | ingredients; picture | 0..* each |
| Help Request | contains | preferred Provider | 1..2 |

## Glossary transcription — generalisations / closed sets

| Generalisation | Sub | Super |
|---|---|---|
| is-a / closed values | Ingredient Substitute; Preparation Step Explanation; Steps to Mitigate Catastrophe; Menu proposal | type / answerType |
| is-a / closed values | Chef; Grandma Avatar; Community | preferred Provider / help Provider type |
| closed values (unlabelled arrows) | Open; Answered; Closed | status |
| is-a / closed values | breakfast; lunch; dinner; supper | meal |
| is-a / closed values | Piece; Cup; Table Spoon; Tea spoon; Fluid Ounces; Pint; Quart; Pound; Kilogram; Gram; Liter; Milliliter; Pinch | Unit |

Finite tags become enums; the four structured answer payloads become a sealed interface with record variants. The examples in grey are illustrative, never defaults.

## Classification

Grouped rows apply the same decision to every explicitly listed term.

| Term | Kind / implementation | Evidence | Confidence |
|---|---|---|---|
| Help | Aggregate Root / `Help` | Aggregate color, identifier, acceptance lifecycle | High |
| Help Request | Aggregate Root / `HelpRequest` | Aggregate color, identifier, status lifecycle, Help reference | High |
| help Id; help request Id | Value Object / `HelpId`; `HelpRequestId` | Explicit identifiers, UUID examples | High |
| help request | Aggregate reference / `HelpRequestId` | Explicit refers-to relationship | High |
| requester; help requester | External ID / `RequesterId` | UUID examples; matching role in both diagrams | Medium |
| help Provider | External ID / `HelpProviderId` | UUID example, optional reference-like leaf | Medium |
| recipe; howTo Step; ingredients; Ingredient | External ID / `RecipeId`; `HowToStepId`; `IngredientId` | UUID examples; no local internal structure | Medium |
| answer | Value Object / sealed `Answer` | Composed once; no ID, lifecycle or outside reference drawn; yellow entity color conflicts | Low |
| substitutes; preparation step explanation; catastrophe mitigation; menu proposal | Value Object / answer record variants | Exclusive payloads, owner-only composition; yellow entity colors conflict | Low |
| Substitute | Value Object / `Answer.Substitute` | Composed ingredient mapping, no independent identity | Medium |
| course | Value Object / `Answer.Course` | Composed recipe/dish pair; dish may repeat, so not identity; yellow entity color conflicts | Low |
| substitute ingredient | Value Object / `SubstituteIngredient` | Name/Value/Unit travel together, green color | High |
| type; answerType | Enum / `Type` | Same four named values and equality invariant | High |
| Ingredient Substitute; Preparation Step Explanation; Steps to Mitigate Catastrophe; Menu proposal | Enum members | Closed alternatives; structured content represented by answer variants | High |
| help Provider type; preferred Provider | Enum / `Provider` | Same three named values | High |
| Chef; Grandma Avatar; Community | Enum members | Closed provider alternatives | High |
| status | Enum / `Status` | Open / Answered / Closed values | High |
| Open; Answered; Closed | Enum members | Named states | High |
| meal; breakfast; lunch; dinner; supper | Enum / `Meal` and members | Closed set | High |
| Unit; Piece; Cup; Table Spoon; Tea spoon; Fluid Ounces; Pint; Quart; Pound; Kilogram; Gram; Liter; Milliliter; Pinch | Enum / `Unit` and members | Closed unit alternatives | High |
| answer title; title; description; explanation; Name; note; how to serve | Value leaves / String fields | No rule other than presence drawn; optional how to serve | Medium |
| Value | Value leaf / BigDecimal | Numeric quantity; exact decimal, no range drawn | Medium |
| servings; dish | Value leaves / int fields | Integer examples; no range rule drawn | Medium |
| picture; images | Value leaves / URI collections | URL examples, 0..* | Medium |
| isAccepted | Value leaf / boolean state | Explicit boolean example and required field | High |

The yellow inner nodes are deliberately not assigned invented local IDs. They are immutable values in this proposal. If their entity coloring is intentional, the domain expert must identify stable identity and lifecycle before promotion; this is an explicit deviation rather than a silently discarded visual signal.

## Aggregates

- **Help Request** owns request metadata, its optional references, requested ingredient identities, provider preferences, picture URIs and status. It holds no Help objects.
- **Help** owns answer metadata and one immutable answer tree. It holds the request and requester by ID, with optional provider identity. Its answer tree owns substitute mappings or courses as appropriate.
- `HelpRepository` and `HelpRequestRepository` are interfaces, one per root. No repositories exist for the composed values.
- `CookingAssistance.accept(request, help)` validates both roots before accepting Help and marking the request Answered. The service temporarily receives both objects; neither aggregate stores the other.

## Invariants and enforcement

| Source rule | Enforcement |
|---|---|
| Every required 1 field | Constructor required check, primitive value, or derived discriminator/state |
| Optional 0..1 fields | Optional<T>; Optional container itself cannot be null |
| 0..* fields | Defensive immutable List/Set copies; empty accepted, null members rejected |
| preferred Provider 1..2 | HelpRequest constructor; named min/max constants; distinct set |
| Chef only for Menu proposal | HelpRequest constructor and Help validation |
| Chef help provided exclusively | HelpRequest constructor requires Chef as sole preference |
| Recipe mandatory for Ingredient Substitute / Preparation Step Explanation | HelpRequest constructor |
| howTo Step only for Preparation Step Explanation | HelpRequest constructor, retaining 0..1 |
| ingredients only for Ingredient Substitute | HelpRequest constructor, retaining 0..* |
| Exactly one answer alternative | Sealed Answer type and one required Help.answer value |
| Answer type must equal request type | Help.validateAgainst; subtype discriminator cannot drift |
| Substitutes 1..* | Substitutes constructor |
| Same recipe as request | Help.validateAgainst for substitutes, preparation and present catastrophe recipe |
| Same howTo as request | Help.validateAgainst for preparation |
| Same ingredient as request | Every substitute's ingredient must belong to requested ingredients |
| course 1..10 | MenuProposal constructor; named min/max constants |
| Different courses can have same step when served in parallel | Course list allows repeated dish numbers; no uniqueness rule |
| Cook may answer own request except Chef requests | Reject equal requester/provider UUID when provider is Chef; see identity/absence limitation in Q7 |
| Answered requires at least one accepted Help | Only acceptance service changes request to Answered; accepted Help must reference same request |
| Answered requests can still be answered | Help validation rejects only Closed |
| isAccepted required | false initially, changed to true by acceptance service |

`DomainViolation` carries descriptive rule names. Acceptance is monotonic: no unaccept or delete behavior is invented. Durable enforcement requires canonical instances loaded by an application unit of work, an atomic save of both aggregates, and optimistic concurrency or equivalent locking. Repository interfaces alone do not implement those guarantees. Authorization of the acting user belongs to the application layer and is not specified by these glossaries.

## External references

Only typed IDs represent recipe, howTo Step, ingredient, requester and provider. No Recipe, User or Ingredient entity is imported or embedded. Their ownership and identity translation must be confirmed. Requester and help requester use the same local ID type as a provisional semantic match. HelpRequestId is an internal cross-aggregate ID, not an external-context ID.

## Open questions — 10 decisions for a domain expert

1. **Inner yellow nodes:** Are answer, the four payloads and course intended to have identity and mutable lifecycles? Currently value objects, Low confidence, despite entity colors. What would distinguish two equal-looking instances?
2. **Preparation gap:** howTo Step is 0..1 in a request but mandatory and equal to the request in its answer. A request without it is valid but cannot receive a matching preparation answer. Should request cardinality be 1 for that type, or can an answer introduce the step?
3. **Ingredient gap:** ingredients is 0..* in a request but substitutes is 1..* with matching ingredients. An empty request cannot receive a matching substitute answer. Should at least one requested ingredient be required, or may an answer identify it?
4. **Provider policy:** Confirm Chef exclusively means a singleton preference, and that Help must come from a preferred provider. Is preference restrictive or only advisory? The latter rule is inferred. Provider preferences are distinct, not a ranked list.
5. **Closed lifecycle:** What closes/reopens a request, and may it receive or accept Help afterward? Closed is represented, but no public transition, restoration API or close method is invented. New requests start Open and new Help starts unaccepted as provisional lifecycle defaults.
6. **Acceptance consistency:** Can accepted Help be revoked/deleted, and who accepts it? Must exactly one or multiple Helps be accepted? Current model permits multiple, retains Answered, and requires an atomic cross-aggregate transaction. A future persistence adapter must protect this invariant on restoration/deletion too.
7. **Self-answer identity:** The note reads “A cook can answer their own requests - except for Chef requests.” Do requester and provider UUIDs share the same identity space? Provider is 0..1: when absent, self-answer cannot be checked. Current rule checks only a supplied ID. Should a Chef provider ID be mandatory, or must an authenticated identity be supplied separately?
8. **Payload/cardinality interpretation:** Does oneOf require exactly one (implemented) or at most one? Is 10 courses a business limit or UI limit? Confirm Name's indistinct cardinality as 1. No unlabelled edge is assumed to be composition; status's unlabelled arrows are treated as closed values.
9. **Leaves and dish meaning:** Are blank text, nonpositive servings/quantities, and arbitrary dish integers valid? No undocumented range/format restrictions were added. Is dish a serving-order number (suggested by the parallel-step note), and must pictures/images be HTTP(S) URLs?
10. **Vocabulary and ownership:** Confirm recipe/ingredient/step/provider/requester are references to concepts owned elsewhere. Confirm requester = help requester, type = answerType, picture/images are media URLs, and capitalization variants refer to the same concepts. Menu proposal/Menu Proposal, howTo Step/howTo, Ingredient/ingredients, and Steps to Mitigate Catastrophe/catastrophe mitigation are preserved semantically with Java casing; distinguish them if the source intended different meanings.

## Verification

Java 17 compilation with `--release 17 -Xlint:all -Werror`; executable smoke tests cover each answer variant, both roots, collection bounds, conditional cardinalities, null required values, request matching, provider restrictions, self-answer policy, acceptance, identity equality and collection immutability. See `VALIDATION.txt` for the actual run. No external dependencies are needed.
