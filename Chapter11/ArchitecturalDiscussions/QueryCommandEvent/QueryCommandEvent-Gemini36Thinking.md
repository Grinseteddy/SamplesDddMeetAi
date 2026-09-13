# Prompt 2026-09-13

Check where to use Commands and queries between Bounded Contexts. Use the domain knowledge graph.

[Domain Knowledge Graph](../../assets/larderEnahnced4.ttl)

# Answer

```python
import rdflib

g = rdflib.Graph()
with open("larderEnahnced4.ttl", "r") as f:
    g.parse(f, format="turtle")

# Query Bounded Context Canvas messages and EventStorming interactions between Bounded Contexts
print("=== Messages with dkg:messageKind ===")
q_msg = """
PREFIX dkg: <https://w3id.org/dkg/ns#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>

SELECT ?msg ?label ?kind ?from ?to ?mentions WHERE {
    ?msg a dkg:Message ;
         skos:prefLabel ?label ;
         dkg:from ?from ;
         dkg:to ?to .
    OPTIONAL { ?msg dkg:messageKind ?kind }
    OPTIONAL { ?msg dkg:mentions ?mentions }
}
"""
for row in g.query(q_msg):
    print(f"Msg: {row.label} | Kind: {row.kind} | From: {row['from'].split('#')[-1]} -> To: {row['to'].split('#')[-1]} | Mentions: {row.mentions}")

print("\n=== Commands & Reads/Produces in EventStorming ===")
q_cmd = """
PREFIX dkg: <https://w3id.org/dkg/ns#>
SELECT ?cmd ?cmdLabel ?evt ?evtLabel ?ctx WHERE {
    ?cmd a dkg:Command ;
         skos:prefLabel ?cmdLabel ;
         dkg:triggers ?evt .
    ?evt a dkg:DomainEvent ;
         skos:prefLabel ?evtLabel ;
         dkg:inContext ?ctx .
}
"""
for row in g.query(q_cmd):
    print(f"Cmd: {row.cmdLabel} -> Evt: {row.evtLabel} in Context: {row.ctx.split('#')[-1]}")


```

```text
Traceback (most recent call last):
  File "<xbox-string>", line 1, in <module>
    import rdflib
ModuleNotFoundError: No module named 'rdflib'


```

```python
import re

with open("larderEnahnced4.ttl", "r") as f:
    text = f.read()

# Find all dkg:Message instances
messages = re.findall(r'(:Msg_\w+)\s+a\s+dkg:Message\s*;(.*?)\.\n', text, re.DOTALL)
print("--- MESSAGES ---")
for m_id, body in messages:
    label = re.search(r'skos:prefLabel\s+"([^"]+)"', body)
    kind = re.search(r'dkg:messageKind\s+"([^"]+)"', body)
    from_ctx = re.search(r'dkg:from\s+:(\w+)', body)
    to_ctx = re.search(r'dkg:to\s+:(\w+)', body)
    mentions = re.search(r'dkg:mentions\s+:(\w+)', body)
    print(f"{m_id}: {label.group(1) if label else ''} | Kind: {kind.group(1) if kind else ''} | From: {from_ctx.group(1) if from_ctx else ''} -> To: {to_ctx.group(1) if to_ctx else ''} | Mentions: {mentions.group(1) if mentions else ''}")

# Find bounded context relationships & commands/events/queries across contexts
print("\n--- EVENTS & COMMANDS IN EVENTSTORMING ---")
events = re.findall(r'(:Ev_\w+)\s+a\s+dkg:DomainEvent\s*;(.*?)\.\n', text, re.DOTALL)
for e_id, body in events:
    label = re.search(r'skos:prefLabel\s+"([^"]+)"', body)
    ctx = re.findall(r'dkg:inContext\s+:(\w+)', body)
    print(f"{e_id}: {label.group(1) if label else ''} | Contexts: {ctx}")

# Find Commands
commands = re.findall(r'(:Cmd_\w+)\s+a\s+dkg:Command\s*;(.*?)\.\n', text, re.DOTALL)
for c_id, body in commands:
    label = re.search(r'skos:prefLabel\s+"([^"]+)"', body)
    triggers = re.findall(r'dkg:triggers\s+:(\w+)', body)
    reads = re.findall(r'dkg:reads\s+:(\w+)', body)
    produces = re.findall(r'dkg:produces\s+:(\w+)', body)
    print(f"{c_id}: {label.group(1) if label else ''} | Triggers: {triggers} | Reads: {reads} | Produces: {produces}")


```

```text
--- MESSAGES ---
:Msg_Out_ConsentManagement_Consent: Consent | Kind: ? | From: Ctx_CookProfile -> To: Ctx_ConsentManagement | Mentions: Con_Consent
:Msg_In_CookProfile_Consent: Consent | Kind: ? | From: Ctx_CookProfile -> To: Ctx_ConsentManagement | Mentions: Con_Consent
:Msg_In_Sharing_Consent: Consent | Kind: qry | From: Ctx_Sharing -> To: Ctx_ConsentManagement | Mentions: Con_Consent
:Msg_In_CookingAssistance_HelpRequest_GA: Help request | Kind: evt | From: Ctx_CookingAssistance -> To: Ctx_GrandmaAvatarAI | Mentions: Con_HelpRequest_ES
:Msg_Out_CookingAssistance_HelpResponse_GA: Help response | Kind: evt | From: Ctx_GrandmaAvatarAI -> To: Ctx_CookingAssistance | Mentions: Con_HelpResponse_ES
:Msg_In_CookingAssistance_Menu: Menu | Kind: qry | From: Ctx_CookingAssistance -> To: Ctx_MealPlanning | Mentions: Con_Menu_ES
:Msg_In_CookingAssistance_Ingredients: Ingredients | Kind: qry | From: Ctx_CookingAssistance -> To: Ctx_MealPlanning | Mentions: Con_Ingredients
:Msg_In_CookingAssistance_Recipe_MP: Recipe | Kind: qry | From: Ctx_CookingAssistance -> To: Ctx_MealPlanning | Mentions: Con_Recipe
:Msg_In_MealPreparation_Recipe: Recipe | Kind: qry | From: Ctx_MealPreparation -> To: Ctx_MealPlanning | Mentions: Con_Recipe
:Msg_In_MealPreparation_Menu: Menu | Kind: qry | From: Ctx_MealPreparation -> To: Ctx_MealPlanning | Mentions: Con_Menu_ES
:Msg_Out_RecipeCatalog_Recipe: Recipe | Kind: qry | From: Ctx_MealPlanning -> To: Ctx_RecipeCatalog | Mentions: Con_Recipe
:Msg_Out_CookingAssistance_HelpResponse_MP: Help response | Kind: qry | From: Ctx_MealPlanning -> To: Ctx_CookingAssistance | Mentions: Con_HelpResponse_ES
:Msg_In_CookingAssistance_Menu_MPrep: Menu | Kind: qry | From: Ctx_CookingAssistance -> To: Ctx_MealPreparation | Mentions: Con_Menu_ES
:Msg_In_CookingAssistance_Recipe_MPrep: Recipe | Kind: qry | From: Ctx_CookingAssistance -> To: Ctx_MealPreparation | Mentions: Con_Recipe
:Msg_In_Sharing_HelpResponse: Help response | Kind: qry | From: Ctx_Sharing -> To: Ctx_MealPreparation | Mentions: Con_HelpResponse_ES
:Msg_Out_MealPlanning_Recipe: Recipe | Kind: qry | From: Ctx_MealPreparation -> To: Ctx_MealPlanning | Mentions: Con_Recipe
:Msg_Out_MealPlanning_Menu: Menu | Kind: qry | From: Ctx_MealPreparation -> To: Ctx_MealPlanning | Mentions: Con_Menu_ES
:Msg_Out_CookingAssistance_HelpResponse_MPrep: Help response | Kind: qry | From: Ctx_MealPreparation -> To: Ctx_CookingAssistance | Mentions: Con_HelpResponse_ES
:Msg_In_CookingAssistance_Pictures: Pictures | Kind: qry | From: Ctx_CookingAssistance -> To: Ctx_Media | Mentions: Con_Pictures_ES
:Msg_In_Sharing_Pictures: Pictures | Kind: qry | From: Ctx_Sharing -> To: Ctx_Media | Mentions: Con_Pictures_ES
:Msg_In_CookingAssistance_HelpRequest_Notif: Help request | Kind: evt | From: Ctx_CookingAssistance -> To: Ctx_Notification | Mentions: Con_HelpRequest_ES
:Msg_In_CookingAssistance_HelpResponse_Notif: Help response | Kind: evt | From: Ctx_CookingAssistance -> To: Ctx_Notification | Mentions: Con_HelpResponse_ES
:Msg_In_MealPlanning_Recipe: Recipe | Kind: qry | From: Ctx_MealPlanning -> To: Ctx_RecipeCatalog | Mentions: Con_Recipe
:Msg_Out_Media_Pictures: Pictures | Kind: qry | From: Ctx_Sharing -> To: Ctx_Media | Mentions: Con_Pictures_ES
:Msg_Out_MealPreparation_HelpResponse: Help response | Kind: qry | From: Ctx_Sharing -> To: Ctx_MealPreparation | Mentions: Con_HelpResponse_ES
:Msg_Out_ConsentManagement_Consent_Sharing: Consent | Kind: qry | From: Ctx_Sharing -> To: Ctx_ConsentManagement | Mentions: Con_Consent

--- EVENTS & COMMANDS IN EVENTSTORMING ---
:Ev_CookRegistered: Cook registered | Contexts: ['Ctx_CookProfile']
:Ev_DinnerPlanned: Dinner planned | Contexts: ['Ctx_MealPlanning']
:Ev_RecipesSearched: Recipes searched | Contexts: ['Ctx_MealPlanning']
:Ev_RecipeSelected: Recipe selected | Contexts: ['Ctx_MealPlanning']
:Ev_IngredientsMissing: Ingredients missing | Contexts: ['Ctx_MealPlanning']
:Ev_MealPlanningStalled: Meal planning stalled | Contexts: ['Ctx_MealPlanning']
:Ev_HelpRequested: Help requested | Contexts: ['Ctx_CookingAssistance']
:Ev_HelpProvided: Help provided | Contexts: ['Ctx_CookingAssistance']
:Ev_IngredientsSubstituted: Ingredients substituted | Contexts: ['Ctx_MealPlanning']
:Ev_PlanSetteled: plan setteled | Contexts: ['Ctx_MealPlanning']
:Ev_MealPreparationStarted: Meal preparation started | Contexts: ['Ctx_MealPreparation']
:Ev_StepUnclear: Step unclear | Contexts: ['Ctx_MealPreparation']
:Ev_CatastropheHappened: Catastrophe happened | Contexts: ['Ctx_MealPreparation']
:Ev_PicturesTaken: Pictures taken | Contexts: ['Ctx_Media']
:Ev_StepCompeted: Step competed | Contexts: ['Ctx_MealPreparation']
:Ev_MealRescued: Meal rescued | Contexts: ['Ctx_MealPreparation']
:Ev_MealPrepared: Meal prepared | Contexts: ['Ctx_MealPreparation']
:Ev_ThanksGiven: Thanks given | Contexts: ['Ctx_Sharing']
:Cmd_RegisterCook: Register cook | Triggers: ['Ev_CookRegistered'] | Reads: ['Con_User_ES'] | Produces: ['Con_Cook_ES']
:Cmd_PlanDinner: Plan dinner | Triggers: ['Ev_DinnerPlanned'] | Reads: ['Con_Guests_ES'] | Produces: ['Con_Menu_ES']
:Cmd_SearchRecipes: Search recipes | Triggers: ['Ev_RecipesSearched'] | Reads: ['Con_Recipe'] | Produces: []
:Cmd_SearchIngredients: Search Ingredients | Triggers: ['Ev_IngredientsMissing'] | Reads: ['Con_Recipe'] | Produces: []
:Cmd_PrepareMeal: Prepare meal | Triggers: ['Ev_MealPlanningStalled'] | Reads: ['Con_Catastrophe_ES'] | Produces: []
:Cmd_RequestHelp: Request help | Triggers: ['Ev_HelpRequested'] | Reads: ['Con_Catastrophe_ES'] | Produces: ['Con_HelpRequest_ES']
:Cmd_ProvideHelp: Provide help | Triggers: ['Ev_HelpProvided'] | Reads: ['Con_HelpRequest_ES'] | Produces: ['Con_HelpResponse_ES']
:Cmd_SubstituteIngredients: Substitute ingredients | Triggers: ['Ev_IngredientsSubstituted'] | Reads: ['Con_HelpResponse_ES'] | Produces: ['Con_Ingredients']
:Cmd_PlanMeal: Plan meal | Triggers: ['Ev_PlanSetteled'] | Reads: ['Con_HelpResponse_ES'] | Produces: ['Con_MealPlan_ES']
:Cmd_TakePictures: Take pictures | Triggers: ['Ev_PicturesTaken'] | Reads: ['Con_Catastrophe_ES'] | Produces: ['Con_Pictures14_ES']
:Cmd_ProvideThanks: Provide thanks | Triggers: ['Ev_ThanksGiven'] | Reads: ['Con_HelpProvider_ES'] | Produces: ['Con_Thanks_ES']


```

```python
# Let's write a complete script to parse all inbound/outbound communication between contexts from Bounded Context Canvases & EventStorming.

with open("larderEnahnced4.ttl", "r") as f:
    text = f.read()

# Get all context nodes
contexts = re.findall(r'(:Ctx_\w+)\s+a\s+dkg:BoundedContext', text)
print("Contexts:", set(contexts))

# Find all Messages and group by (From, To, Kind, Label/Mentions)
msgs = re.findall(r'(:Msg_\w+)\s+a\s+dkg:Message\s*;(.*?)\.\n', text, re.DOTALL)

interactions = []
for m_id, body in msgs:
    from_match = re.search(r'dkg:from\s+:(\w+)', body)
    to_match = re.search(r'dkg:to\s+:(\w+)', body)
    kind_match = re.search(r'dkg:messageKind\s+"([^"]+)"', body)
    label_match = re.search(r'skos:prefLabel\s+"([^"]+)"', body)
    
    if from_match and to_match:
        from_c = from_match.group(1)
        to_c = to_match.group(1)
        kind = kind_match.group(1) if kind_match else 'unknown'
        label = label_match.group(1) if label_match else ''
        interactions.append((from_c, to_c, kind, label, m_id))

# Deduplicate interactions by (From, To, Label, Kind)
unique_interactions = {}
for from_c, to_c, kind, label, m_id in interactions:
    key = (from_c, to_c, label, kind)
    if key not in unique_interactions:
        unique_interactions[key] = m_id

print("\n--- UNIQUE CROSS-CONTEXT MESSAGES ---")
for (from_c, to_c, label, kind), m_id in sorted(unique_interactions.items()):
    print(f"From: {from_c:20s} -> To: {to_c:22s} | Type: {kind:5s} | Payload: {label}")


```

```text
Contexts: {':Ctx_MealPlanning', ':Ctx_Notification', ':Ctx_GrandmaAvatarAI', ':Ctx_MealPreparation', ':Ctx_ConsentManagement', ':Ctx_Media', ':Ctx_Sharing', ':Ctx_RecipeCatalog', ':Ctx_CookingAssistance', ':Ctx_CookingHelp', ':Ctx_CookProfile'}

--- UNIQUE CROSS-CONTEXT MESSAGES ---
From: Ctx_CookProfile      -> To: Ctx_ConsentManagement  | Type: ?     | Payload: Consent
From: Ctx_CookingAssistance -> To: Ctx_GrandmaAvatarAI    | Type: evt   | Payload: Help request
From: Ctx_CookingAssistance -> To: Ctx_MealPlanning       | Type: qry   | Payload: Ingredients
From: Ctx_CookingAssistance -> To: Ctx_MealPlanning       | Type: qry   | Payload: Menu
From: Ctx_CookingAssistance -> To: Ctx_MealPlanning       | Type: qry   | Payload: Recipe
From: Ctx_CookingAssistance -> To: Ctx_MealPreparation    | Type: qry   | Payload: Menu
From: Ctx_CookingAssistance -> To: Ctx_MealPreparation    | Type: qry   | Payload: Recipe
From: Ctx_CookingAssistance -> To: Ctx_Media              | Type: qry   | Payload: Pictures
From: Ctx_CookingAssistance -> To: Ctx_Notification       | Type: evt   | Payload: Help request
From: Ctx_CookingAssistance -> To: Ctx_Notification       | Type: evt   | Payload: Help response
From: Ctx_GrandmaAvatarAI  -> To: Ctx_CookingAssistance  | Type: evt   | Payload: Help response
From: Ctx_MealPlanning     -> To: Ctx_CookingAssistance  | Type: qry   | Payload: Help response
From: Ctx_MealPlanning     -> To: Ctx_RecipeCatalog      | Type: qry   | Payload: Recipe
From: Ctx_MealPreparation  -> To: Ctx_CookingAssistance  | Type: qry   | Payload: Help response
From: Ctx_MealPreparation  -> To: Ctx_MealPlanning       | Type: qry   | Payload: Menu
From: Ctx_MealPreparation  -> To: Ctx_MealPlanning       | Type: qry   | Payload: Recipe
From: Ctx_Sharing          -> To: Ctx_ConsentManagement  | Type: qry   | Payload: Consent
From: Ctx_Sharing          -> To: Ctx_MealPreparation    | Type: qry   | Payload: Help response
From: Ctx_Sharing          -> To: Ctx_Media              | Type: qry   | Payload: Pictures


```

In the Larder domain knowledge graph, inter-context interactions are split between read-only **Queries (`qry`)** to retrieve context data and asynchronous **Events/Commands (`evt`)** that trigger state changes or downstream actions across context boundaries.

**Queries Between Bounded Contexts (`qry`)**

Queries perform read-only lookups across boundaries without mutating source context state.

| Calling Context | Target Context | Query Payload | Purpose |
| --- | --- | --- | --- |
| **Meal Planning** | **Recipe Catalog** | `Recipe` | Fetches recipe structures, ingredients, and steps during planning.

|
| **Meal Planning** | **Cooking Assistance** | `Help response` | Checks for past or active help advice to settle meal plans.

|
| **Meal Preparation** | **Meal Planning** | `Menu`, `Recipe` | Pulls planned menu details and recipes while executing cooking steps.

|
| **Meal Preparation** | **Cooking Assistance** | `Help response` | Fetches advice/rescue responses to resolve cooking catastrophes or unclear steps.

|
| **Cooking Assistance** | **Meal Planning** | `Menu`, `Ingredients`, `Recipe` | Reads plan context to provide tailored advice for missing ingredients or dishes.

|
| **Cooking Assistance** | **Meal Preparation** | `Menu`, `Recipe` | Queries real-time cooking execution context.

|
| **Cooking Assistance** | **Media** | `Pictures` | Pulls uploaded evidence photos of cooking catastrophes.

|
| **Sharing** | **Consent Management** | `Consent` | Checks if cook and guest consent permits publishing media.

|
| **Sharing** | **Meal Preparation** | `Help response` | Passes through help response details to attribute and thank helpers.

|
| **Sharing** | **Media** | `Pictures` | Retrieves final meal trophy photos for community sharing.

|

---

**Events and Commands Between Bounded Contexts (`evt`)**

Event- and command-driven communications propagate state changes, trigger downstream workflows, or pass requests across Anti-Corruption Layers (ACL).

| Source Context | Destination Context | Interaction Payload | Execution Purpose |
| --- | --- | --- | --- |
| **Cooking Assistance** | **Grandma Avatar AI** | `Help request` | Dispatches an active help request across an ACL to the AI avatar.

|
| **Grandma Avatar AI** | **Cooking Assistance** | `Help response` | Delivers the generated AI advice back to the assistance context.

|
| **Cooking Assistance** | **Notification** | `Help request`, `Help response` | Emits lifecycle events to broadcast alerts when help is needed or provided.

|

---

**Unclassified Context Communications (`?`)**

* **Cook Profile $\rightarrow$ Consent Management**: Transmits `Consent` payload upon user registration. The message type is currently marked unclassified (`?`) on the context canvas and requires formal design decision on whether it operates as an event or command stream.