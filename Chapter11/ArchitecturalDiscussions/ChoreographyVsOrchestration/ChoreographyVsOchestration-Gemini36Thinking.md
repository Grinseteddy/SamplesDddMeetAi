# Prompt 2026-09-13

Decide if Larder should use a choreography or orchestrator approach for UI.

[Domain Knowledge Graph](../../assets/larderEnahnced4.ttl)

# Answer

```python
import rdflib

g = rdflib.Graph()
g.parse("larderEnahnced4.ttl", format="ttl")

print("Number of triples:", len(g))

# Search for any mentions of UI, orchestration, choreography, interface, etc.
queries = ["UI", "choreography", "orchestrat", "frontend", "interface", "User"]
for q in queries:
    matches = list(g.triples((None, None, rdflib.Literal(q, lang='en')))) + \
              [s for s, p, o in g if q.lower() in str(s).lower() or q.lower() in str(o).lower()]
    print(f"Matches for '{q}':", len(matches))


```

```text
Traceback (most recent call last):
  File "<xbox-string>", line 1, in <module>
    import rdflib
ModuleNotFoundError: No module named 'rdflib'


```

```python
with open("larderEnahnced4.ttl", "r") as f:
    text = f.read()

import re
print("Lines count:", len(text.splitlines()))

# Search for keywords in TTL text
keywords = ["UI", "choreograph", "orchestrat", "frontend", "interface", "User", "BoundedContext", "Capability"]
for kw in keywords:
    matches = [line for line in text.splitlines() if kw.lower() in line.lower()]
    print(f"Keyword '{kw}': {len(matches)} lines matched")
    for m in matches[:5]:
        print("  ", m.strip())


```

```text
Lines count: 1948
Keyword 'UI': 51 lines matched
   :Idea_DietaryRequirements a dkg:Idea ; skos:prefLabel "Dietary requirements" ; dkg:inCluster :Cluster_RecipeSearch ; dkg:source :Art_Brainstorm ; dkg:locator "Recipe Search" ; dkg:confidence dkg:OnArtifact .
   :Del_NutritionRequirementManagement a dkg:Deliverable ; skos:prefLabel "Nutrition requirement management" ; rdfs:comment "priority 1; green" ; dkg:realises :Imp_FollowsDietryPlans ; dkg:source :Art_ImpactMap ; dkg:locator "DELIVERABLES 1" ; dkg:confidence dkg:OnArtifact .
   :Idea_DietaryRequirements dkg:proposedSameAs :Del_NutritionRequirementManagement ; rdfs:comment "'Dietary requirements' sticky vs 'Nutrition requirement management' deliverable" .
   :Cap_AskGrandmaGrandpa a dkg:Component ; skos:prefLabel "Ask Grandma / Grandpa" ; dkg:evolution dkg:CustomBuilt ; rdfs:comment "sits on the Custom line; stage Inferred" ; dkg:supports :Goal_BetterCookingSkills ; dkg:dependsOn :Cap_SpecificAI ; dkg:source :Art_Wardley ; dkg:locator "visible row 1" ; dkg:confidence dkg:Inferred .
   :Cap_AskChef a dkg:Component ; skos:prefLabel "Ask chef" ; dkg:evolution dkg:CustomBuilt ; dkg:supports :Goal_BetterCookingSkills ; dkg:dependsOn :Cap_RemoteMeeting, :Cap_Chat ; dkg:source :Art_Wardley ; dkg:locator "visible row 2" ; dkg:confidence dkg:OnArtifact .
Keyword 'choreograph': 0 lines matched
Keyword 'orchestrat': 0 lines matched
Keyword 'frontend': 0 lines matched
Keyword 'interface': 0 lines matched
Keyword 'User': 25 lines matched
   # Four users at the top, seven needs beneath, 24 components on the chain.
   # The user→need and need→component fans are partly unreadable; recorded
   rdfs:comment "4 users, 7 needs, 24 components. No movement arrows, no inertia marks, no pipelines." ;
   # --- users (anchors) ---
   :Goal_BetterCookingSkills a dkg:UserNeed ; skos:prefLabel "Better cooking skills" ; dkg:source :Art_Wardley ; dkg:locator "need 1" ; dkg:confidence dkg:OnArtifact .
Keyword 'BoundedContext': 26 lines matched
   # Files: EventStormingWithBoundedContext.jpg (primary — seven distinct bubble
   rdfs:label "Community Cooking EventStorming board, with bounded contexts (EventStormingWithBoundedContext.jpg + EventStormingBoardWithBcs.jpg)" ;
   :Ctx_CookProfile a dkg:BoundedContext ; skos:prefLabel "Cook Profile" ; dkg:source :Art_ES ; dkg:locator "bubble top-left" ; dkg:confidence dkg:OnArtifact .
   :Ctx_MealPlanning a dkg:BoundedContext ; skos:prefLabel "Meal Planning" ; rdfs:comment "drawn twice: the top bubble (events 2–6) and a second bubble beneath it (events 9–10)" ; dkg:source :Art_ES ; dkg:locator "bubble top-centre", "bubble centre-left" ; dkg:confidence dkg:OnArtifact .
   :Ctx_CookingAssistance a dkg:BoundedContext ; skos:prefLabel "Cooking Assistance" ; rdfs:comment "two bubbles: top-right (event 7) and middle-left (event 8). On EventStormingBoardWithBcs.jpg all four help bubbles carry this name." ; dkg:source :Art_ES ; dkg:locator "bubble top-right", "bubble middle-left" ; dkg:confidence dkg:OnArtifact .
Keyword 'Capability': 56 lines matched
   :Cap_AskChef dkg:proposedSameAs :Act_ChefsAsSeniorPartner ; rdfs:comment "Component vs Actor — a capability named after who provides it" .
   # INGEST 5 — Capability Map, CapabilityMapStep3.jpg — 2026-09-10
   :Art_CapMap a dkg:CapabilityMap ;
   rdfs:label "Larder capability map (CapabilityMapStep3.jpg)" ;
   :Cap_Onboarding_L1 a dkg:Capability ; skos:prefLabel "Onboarding" ; rdfs:comment "level-1 chevron" ; dkg:source :Art_CapMap ; dkg:locator "level 1" ; dkg:confidence dkg:OnArtifact .


```

```python
with open("larderEnahnced4.ttl", "r") as f:
    text = f.read()

# Extract Bounded Contexts and their descriptions/messages/rules
bcs = re.findall(r'(:Ctx_\w+)\s+a dkg:BoundedContext.*?(?=\n:\w|\Z)', text, re.DOTALL)
for bc in bcs:
    print("--- BC ---")
    print(bc[:400])


```

```text
--- BC ---
:Ctx_CookProfile
--- BC ---
:Ctx_MealPlanning
--- BC ---
:Ctx_CookingAssistance
--- BC ---
:Ctx_CookingHelp
--- BC ---
:Ctx_MealPreparation
--- BC ---
:Ctx_Media
--- BC ---
:Ctx_Sharing
--- BC ---
:Ctx_ConsentManagement
--- BC ---
:Ctx_GrandmaAvatarAI
--- BC ---
:Ctx_Notification
--- BC ---
:Ctx_RecipeCatalog


```

```python
# Let's see how contexts interact and what domain flows look like in EventStorming / Domain Stories
print("=== DOMAIN STORIES SUMMARY ===")
stories = re.findall(r'(:Art_Story\w+)\s+a dkg:DomainStory.*?(?=\n:\w|\Z)', text, re.DOTALL)
for s in stories:
    print(s[:300])

print("\n=== EVENT STORMING SUMMARY ===")
es_events = re.findall(r'(:Ev_\w+)\s+a dkg:DomainEvent.*?\.', text)
for e in es_events[:10]:
    print(e)


```

```text
=== DOMAIN STORIES SUMMARY ===
:Art_StoryGrandma
:Art_StoryAskChef
:Art_StoryAskCommunity
:Art_StoryCompetition
:Art_StoryGrandmaII

=== EVENT STORMING SUMMARY ===
:Ev_CookRegistered
:Ev_DinnerPlanned
:Ev_RecipesSearched
:Ev_RecipeSelected
:Ev_IngredientsMissing
:Ev_MealPlanningStalled
:Ev_HelpRequested
:Ev_HelpProvided
:Ev_IngredientsSubstituted
:Ev_PlanSetteled


```

```python
# Print EventStorming flow in sequence
events = re.findall(r'(:Ev_\w+)\s+a dkg:DomainEvent\s*;\s*skos:prefLabel\s*"([^"]+)"\s*;\s*dkg:sequence\s*(\d+)', text)
events_sorted = sorted(events, key=lambda x: int(x[2]))
for e in events_sorted:
    print(f"Seq {e[2]}: {e[0]} - {e[1]}")


```

```text
Seq 1: :Ev_CookRegistered - Cook registered
Seq 2: :Ev_DinnerPlanned - Dinner planned
Seq 3: :Ev_RecipesSearched - Recipes searched
Seq 4: :Ev_RecipeSelected - Recipe selected
Seq 5: :Ev_IngredientsMissing - Ingredients missing
Seq 6: :Ev_MealPlanningStalled - Meal planning stalled
Seq 7: :Ev_HelpRequested - Help requested
Seq 8: :Ev_HelpProvided - Help provided
Seq 9: :Ev_IngredientsSubstituted - Ingredients substituted
Seq 10: :Ev_PlanSetteled - plan setteled
Seq 11: :Ev_MealPreparationStarted - Meal preparation started
Seq 12: :Ev_StepUnclear - Step unclear
Seq 13: :Ev_CatastropheHappened - Catastrophe happened
Seq 14: :Ev_PicturesTaken - Pictures taken
Seq 17: :Ev_StepCompeted - Step competed
Seq 18: :Ev_MealRescued - Meal rescued
Seq 19: :Ev_MealPrepared - Meal prepared
Seq 21: :Ev_ThanksGiven - Thanks given


```

Larder should adopt a **UI Orchestrator approach** (via an App Shell or Backend-for-Frontend workflow controller) for its primary user interface, complemented by **UI Choreography** for passive, asynchronous domain events.

**Architectural Comparison for Larder**

| Dimension | UI Orchestrator (Primary Approach) | UI Choreography (Secondary Approach) |
| --- | --- | --- |
| **Core Focus** | Multi-step cooking workflows & guided state transitions | Asynchronous UI updates & passive notifications |
| **State Control** | Centralized in App Shell / BFF Workflow Controller | Decentralized across isolated micro-frontend widgets |
| **Larder Use Cases** | Step-by-Step Cooking Mode, Catastrophe Rescue, Dinner Party Planner | Community activity feeds, Notification toasts, Ratings |
| **Failure Recovery** | Deterministic fallbacks (e.g., routing to Grandma AI on error) | Independent, per-widget error handling |

**Key Architectural Drivers**

* **Guided Cooking Workflows**: Interactive capabilities like *Step-by-Step Cooking Mode* and the *Dinner Party Planner* depend on strict sequential progression. An orchestrator explicitly drives state transitions, active timers, and hands-free voice controls to ensure UI components remain synchronized while cooking.
* **Catastrophe & Rescue Management**: Responding to a cooking failure (`StepUnclear` or `CatastropheHappened` $\rightarrow$ `TakePictures` $\rightarrow$ `RequestHelp` $\rightarrow$ `MealRescued`) requires conditional modal branching across `Meal Preparation`, `Cooking Assistance`, `Media`, and `Grandma Avatar AI`. A UI orchestrator dictates this multi-context dialog flow and manages real-time fallbacks seamlessly.
* **Unified View Aggregation & Gating**: The primary cooking UI synthesizes live data from the `Recipe Catalog` (ingredients and steps), `Meal Preparation` (active state), and `Cooking Assistance` (AI or Chef responses). Central orchestration aggregates these views cleanly while enforcing rules from `Consent Management` and the `Paywall for Premium Content`.
* **Choreography for Decoupled Extras**: Passive UI elements—such as toast messages from `Notification`, dynamic community feed updates (`Sharing`), and targeted promotions from `Ads Management`—subscribe to events like `MealPrepared` or `ThanksGiven` via lightweight UI choreography to avoid coupling with the main cooking pipeline.

Centralizing interactive user flows through a UI orchestrator while leveraging event-driven choreography for ambient widgets gives Larder a deterministic, hands-free kitchen experience without compromising the modularity of its social and community features.