# Prompt 2026-10-04

/devils-advocate
Critique the Visual Glossaries of Help and Help Request in the Bounded Context Cooking Assistance.

![](../../assets/HelpRequest.jpg)
![](../../assets/Help.jpg)

# Answer

I'll load the Devil's Advocate skill and then work through both glossaries.

Both glossaries are finished models, so I'll skip the warm-up and challenge them directly. The objections are ordered by how much I think they'd hurt you later. Treat each one as a hypothesis, and push back where you know something I don't.

## 1. The boundary between the two aggregates leaks in both directions

This is the objection I'd lead with. Look at how many rules can't be checked inside the aggregate that owns them.

**Help copies data from Help Request and then needs a rule to keep the copy honest:**
- answerType "must be the same as request type"
- recipe "same recipe as in request", stated three times
- ingredient "same ingredientId as in request"
- howTo Step "same howTo as in request"

**Help Request depends on Helps it can't see:**
- Status *Answered* requires "at least one Help must answer it"
- "Chef help is provided exclusively" only makes sense across all the Helps for one request

Neither aggregate can enforce these on its own. Every one of them becomes a cross-aggregate policy with a staleness window. In a domain where "mother-in-law arrives in 30 minutes," staleness is not an abstract worry.

**The case for the opposite design:** make Help an entity *inside* Help Request. Then Chef exclusivity, the Answered status, and every "same as request" rule become local checks, and the copied fields disappear because the answer can just look at its parent. The usual objection is write contention from many people answering at once. But how many answers does one burnt-scones request really get in 30 minutes? Probably a handful.

**What would change my mind:** a real reason Help needs its own lifecycle, such as being edited, rated, moderated, or reused across requests. If that reason exists, keep two aggregates, but stop copying fields. Keep only the `help request` reference, and rewrite the cross-aggregate invariants as named policies.

## 2. Request and answer contradict each other on which fields are required

The Help Request side says these fields are allowed for a type, not that they are required:
- **howTo Step 0..1:** only allowed for Preparation Step Explanation
- **ingredients 0..*:** only allowed for Ingredient Substitute

The Help side then demands values that must match the request:
- **howTo Step, exactly 1:** "same as in request"
- **substitute 1..\*:** each one's ingredient is "same ingredientId as in request"

So a Preparation Step Explanation request with no howTo Step is valid, and can never be answered. Recipe has a "Mandatory" note, but howTo Step and ingredients don't, so the asymmetry looks accidental.

**Suggestion:** make both fields mandatory for their type (howTo Step exactly 1, ingredients 1..\*). Also consider modelling the request the way you already model the answer: one variant per type (oneOf), instead of a flat sticky with optional fields and "only for type" rules. That would make the two pictures mirror each other.

## 3. Two words carry two meanings each

**"meal"**
- In menu proposal, *meal* means the occasion: breakfast, lunch, dinner, or supper.
- In course, *meal* "refers to 1 recipe," which means a dish.

**"step"**
- *howTo Step* is a step inside a recipe.
- A course's *step* (example "2") is its serving order, and different courses can share a step to run in parallel.

These will collide in code and in conversation ("which step?"). I'd suggest **meal occasion / dish** and **howTo step / serving slot**, or similar.

Related: the two glossaries use different names for the same concepts.

| Help Request | Help |
|---|---|
| requester | help requester |
| type | answerType |
| preferred Provider | help provider type |
| title | answer title |
| ingredients | ingredient |
| description (the problem) | description / explanation (the answer) |

Some of these differences may be deliberate. If so, the glossary should say what each term means. If not, pick one name per concept.

## 4. Provider rules are spread out and partly missing

- **Nothing links the answer's provider to the request's choice.** No rule says Help's *help provider type* must be one of the request's *preferred Provider* values. As drawn, Community could answer a Chef-only request.
- **"preferred Provider 1..2" hides a rule inside a number.** Read with "Chef exclusively," it means: Chef alone, or one or both of Grandma and Community. Add a fourth provider type and the 1..2 silently becomes wrong. I'd state the rule directly and drop the upper bound.
- **The self-answer rule is hard to parse.** "A cook can answer their own request, except for Chef requests":
    - "Cook" is not a term in either glossary.
    - Checking this rule is the only visible reason Help copies *help requester*, yet there's no "same as request" rule for it, unlike recipe and ingredient.
- **help provider 0..1:** when is it empty? If Grandma Avatar is an AI with no identity, say so. That's an important domain fact that currently has to be guessed.
- **The Chef rule appears twice:** once on *type* ("Chef support only for menu proposal") and again on *preferred Provider*. Keep it in one place.

## 5. The Help Request lifecycle is very thin

Status has only two values, Open and Answered. That leaves open questions:
- Can a request get more answers after it's Answered?
- Does the requester ever accept one answer, close the request, or cancel it?
- What happens to an urgent request nobody answers in time?

Given the 30-minute scenario, I'd expect something like Expired or Closed, and probably an *accepted answer*.

# Why don't you see the oneOf in the answer?

I did see it, but I read it wrong, and that changed three parts of the critique.

**1. I misread `oneOf 0..1` as "possibly none."** In a `oneOf`, exactly one branch is present. The `0..1` on each branch just says that any single branch may be absent because another one was chosen. So an answer always has exactly one variant. My point that "an answer with only 'Stay calm' is valid" was wrong.

**2. I called the per-variant `answerType` a duplicate.** Inside each branch, `answerType` is fixed to one value (substitutes → Ingredient Substitute, and so on). That is the discriminator mapping that ties each branch to its type. It is a standard pattern, not redundant data. The only remaining question is whether the `answerType` on `answer` and the one inside the branch are the same field drawn twice, or two fields. If it is the same field, it's fine.

**3. I missed the real finding: the request doesn't use `oneOf` at all.** The answer models type-specific content structurally. Each type gets its own branch with exactly the fields it needs, so wrong combinations can't be built.

The request does the opposite. It flattens everything into one list and guards it with "Only for type" and "Mandatory" invariant stickies:

| Request field | Guarding invariant | What a `oneOf` branch would do |
|---|---|---|
| `howTo Step 0..1` | Only for Preparation Step Explanation | sits only in that branch, cardinality 1 |
| `ingredients 0..*` | Only for Ingredient Substitute | sits only in that branch, `1..*` |
| `recipe 0..1` | Mandatory for Preparation Step Explanation and Ingredient Substitute | `1` in those two branches, `0..1` in mitigation |
| `preferred Provider` = Chef | Only for Menu proposal | allowed only in the menu proposal branch |

If the request mirrored the answer's shape, five of the invariant stickies would disappear because the structure itself would enforce them. The example request couldn't exist either, since it fills both `howTo Step` and `ingredients`, which no single branch would allow.

There is a second benefit. "answerType must be the same as request type" becomes a simple branch-to-branch match: a substitutes answer answers an ingredient-substitute request. That makes the cross-aggregate rule in point 1 much easier to enforce against a request that can't change once it is Open.

So the corrected point 2 is: **the request should use the same `oneOf` as the answer**. My question of whether one request can have more than one type still stands, but it becomes "`oneOf` or `anyOf` on the request?", which is a sharper question for the room.

# I don't see it. A Catastrophe might contain substitutes. However, the answer needs to be fast and free - therefore the free string. The other types have more time. The explanation of a step needs to be more focused and planning has usually time.

That is a good rationale, and it answers my question about more than one type. Structure scales with how much time the answer has:

- **Catastrophe:** minutes. Free text only (`explanation`, recipe optional), no images, nothing to fill in. A substitute is just a sentence in that text.
- **Preparation Step Explanation:** a bit more time, so it is focused on one recipe step and may include images.
- **Ingredient Substitute and Menu proposal:** planning time, so fully structured, down to units, servings and courses.

Seen that way, one type per request is right. The type is the urgency class, and a burning pan should never wait for a structured form. That also partly answers my "urgency only in prose" point: choosing Catastrophe *is* the urgent signal. The Chef rule fits as well, since the slowest, scarcest responder only gets the request type with the most time.

Three things remain open after your explanation:

1. **The reason isn't on the glossary.** "Free text because it must be fast" is the most important design decision here, and nothing records it. Someone will later "improve" the catastrophe answer by adding structured substitutes. I'd add an invariant sticky next to `catastrophe mitigation`, something like *"Free text only — answer must be fast."*

2. **The example request still breaks the rules.** "Burning Catastrophe" carries `howTo Step` and `ingredients`, which are "only for" the other two types. Either the example is wrong, or a catastrophe request may optionally carry that context so the responder understands faster. If it's the second, the "only for type" stickies need relaxing for Catastrophe. The asker can add context without slowing anyone down, while the answer stays free text.

3. **Speed is only half the cost of a request.** The answer is fast and free, but the cook still has to fill in title, description and type before anyone can help. If speed is the principle, it's worth asking whether a catastrophe request can be created with only a description.

My earlier suggestion to model the request as a `oneOf` is a notation choice. It doesn't conflict with your rationale, and the "only for type" invariant stickies say the same thing, so I wouldn't push it.


## Smaller points

- **answerType is stored three times:** on answer, and again inside each variant with a fixed value. The variant already tells you the type.
- **"oneOf 0..1" on all four variants allows an answer with no content.** You probably mean exactly one.
- **Several stickies are entities with no identity:** answer, substitutes, course, and the other variants. Help contains exactly one answer, so answer looks like a value object. Also, *answer title* sits on Help rather than on answer.
- **A menu proposal request has no inputs.** The answer has servings, meal occasion, and courses, but the request gives the provider nothing to base them on (servings, guests, diet).
- **The substitution model assumes one ingredient is replaced by one ingredient:**
    - Real swaps are often one-to-many, such as 1 egg → flaxseed + water.
    - The original ingredient is an ID, but its substitute is a free-text Name.
    - "Value" is a vague term; *quantity* is clearer.
- **The unit list has gaps:**
    - It mixes US and imperial measures (a US pint and an imperial pint differ by about 20%).
    - It has Fluid Ounces but no weight ounce.
    - Spelling is inconsistent: "Table Spoon" vs "Tea spoon".
- **dinner vs supper:** are these truly different occasions in your domain, or regional synonyms?

**A path forward:** if you settle #1 first, roughly half of the remaining points resolve or simplify on their own. Most of the "same as request" rules, the duplicated fields, and the provider-consistency gap all come from that boundary decision. Is there a reason for the split that the pictures don't show?