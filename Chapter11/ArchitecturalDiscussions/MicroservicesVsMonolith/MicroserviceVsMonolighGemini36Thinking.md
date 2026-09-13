# Prompt 2026-09-13

Use the knowledge graph and discuss if a monolith, a microservice or a modular monolith should be used for the project.

[Domain Knowledge Graph](../../assets/larderEnahnced4.ttl)

# Answer

```python
import rdflib

g = rdflib.Graph()
with open("larderEnahnced4.ttl", "r") as f:
    g.parse(f, format="turtle")

print(f"Total triples: {len(g)}")

# Query bounded contexts
q_bc = """
SELECT ?bc ?label ?role ?bm WHERE {
    ?bc a <https://w3id.org/dkg/ns#BoundedContext> .
    OPTIONAL { ?bc <http://www.w3.org/2004/02/skos/core#prefLabel> ?label }
    OPTIONAL { ?bc <https://w3id.org/dkg/ns#domainRole> ?role }
    OPTIONAL { ?bc <https://w3id.org/dkg/ns#businessModel> ?bm }
}
"""
print("--- Bounded Contexts ---")
for row in g.query(q_bc):
    print(row)

# Query Wardley map components evolution
q_wardley = """
SELECT ?comp ?label ?evo WHERE {
    ?comp a <https://w3id.org/dkg/ns#Component> .
    OPTIONAL { ?comp <http://www.w3.org/2004/02/skos/core#prefLabel> ?label }
    OPTIONAL { ?comp <https://w3id.org/dkg/ns#evolution> ?evo }
}
"""
print("--- Wardley Components ---")
for row in g.query(q_wardley):
    print(row)

# Query Capabilities & markings (core, supporting, generic)
q_cap = """
SELECT ?cap ?label ?comment WHERE {
    ?cap a <https://w3id.org/dkg/ns#Capability> .
    OPTIONAL { ?cap <http://www.w3.org/2004/02/skos/core#prefLabel> ?label }
    OPTIONAL { ?cap <http://www.w3.org/2000/01/rdf-schema#comment> ?comment }
}
"""
print("--- Capabilities ---")
for row in g.query(q_cap):
    print(row)


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

# Find goals, targets, teams, bounded contexts, capabilities, Wardley evolution, etc.
print("--- Goals & Metrics ---")
for match in re.finditer(r'(Goal_\w+).*?prefLabel\s+"([^"]+)"', text, re.DOTALL):
    print(match.group(1), ":", match.group(2))

print("\n--- Bounded Contexts ---")
for match in re.finditer(r'(Ctx_\w+).*?prefLabel\s+"([^"]+)"', text, re.DOTALL):
    print(match.group(1), ":", match.group(2))

print("\n--- Cost structure / BMC ---")
for match in re.finditer(r'(Cost_\w+).*?prefLabel\s+"([^"]+)"', text, re.DOTALL):
    print(match.group(1), ":", match.group(2))

print("\n--- Capability Classifications (Core/Supporting/Generic) ---")
for match in re.finditer(r'(Cap_\w+).*?prefLabel\s+"([^"]+)".*?comment\s+"(core|supporting|generic)"', text, re.DOTALL):
    print(match.group(1), ":", match.group(2), "->", match.group(3))


```

```text
--- Goals & Metrics ---
Goal_LarderPayingMembers : Larder - recipe sharing platform with min. 2,500 paying members in two years
Goal_LarderPayingMembers : Shop easily ingredients and tools with discount
Goal_LarderPayingMembers : Use of community shared recipes
Goal_LarderPayingMembers : Find easily recipes
Goal_LarderPayingMembers : supports unexperienced members
Goal_LarderPayingMembers : Use effective limited budget
Goal_LarderPayingMembers : Learn cooking
Goal_LarderPayingMembers : Publish their own cookbook
Goal_LarderPayingMembers : Cookbooks make curious
Goal_LarderPayingMembers : Follow cooks
Goal_LarderPayingMembers : convert to paid plan
Goal_LarderPayingMembers : paid member renews
Goal_LarderPayingMembers : see premium content
Goal_LarderPayingMembers : pay for ads
Goal_LarderPayingMembers : Nutrition requirement management
Goal_BetterCookingSkills : Better cooking skills
Goal_EfficientUsageOfGroceries : Efficient usage of groceries
Goal_CreateContent : Create content
Goal_RateContent : Rate content
Goal_SearchRecipesWithNutritionMeals : Search recipes with nutrition, meals, ...
Goal_SupportCooking : Support cooking
Goal_BecomeAMember : Become a member
Goal_BecomeAMember : Ask Grandma / Grandpa
Goal_BetterCookingSkills : Ask chef
Goal_BetterCookingSkills : Competitions
Goal_BetterCookingSkills : Ask community
Goal_BetterCookingSkills : Refrigerator
Goal_EfficientUsageOfGroceries : Nutrition
Goal_SearchRecipesWithNutritionMeals : Meal
Goal_SearchRecipesWithNutritionMeals : Write recipe
Goal_CreateContent : Upload media
Goal_CreateContent : Timer
Goal_SupportCooking : Free hand control
Goal_SupportCooking : Member Management
Goal_BecomeAMember : Advanced Search
Goal_SearchRecipesWithNutritionMeals : Rate content
Goal_RateContent : Remote meeting
Goal_SearchRecipesWithNutritionMeals : Singles
Goal_SupportCooking : Onboarding
Goal_BecomeAMember : Cook prepares Meal
Goal_BecomeAMember : Course

--- Bounded Contexts ---
Ctx_CookProfile : Cook Profile
Ctx_MealPlanning : Meal Planning
Ctx_CookingAssistance : Cooking Assistance
Ctx_CookingHelp : Cooking Help
Ctx_MealPreparation : Meal Preparation
Ctx_Media : Media
Ctx_Sharing : Sharing
Ctx_CookingHelp : Are Cooking Assistance (planning-time help) and Cooking Help (cooking-time help) one bounded context or two?
Ctx_CookProfile : Dinner planned
Ctx_MealPlanning : Recipes searched
Ctx_MealPlanning : Recipe selected
Ctx_MealPlanning : Ingredients missing
Ctx_MealPlanning : Meal planning stalled
Ctx_MealPlanning : Help requested
Ctx_CookingAssistance : Help provided
Ctx_CookingAssistance : Ingredients substituted
Ctx_MealPlanning : plan setteled
Ctx_MealPlanning : Meal preparation started
Ctx_MealPreparation : Step unclear
Ctx_MealPreparation : Catastrophe happened
Ctx_MealPreparation : Pictures taken
Ctx_Media : Step competed
Ctx_MealPreparation : Meal rescued
Ctx_MealPreparation : Meal prepared
Ctx_MealPreparation : Thanks given
Ctx_Sharing : Register cook
Ctx_CookingAssistance : Course
Ctx_CookProfile : Consent
Ctx_CookProfile : Community contains Cook
Ctx_CookProfile : Only the user themself registers
Ctx_CookProfile : A cook carries a display identity before asking for help
Ctx_CookProfile : Who reads Cook? Nothing on the map does.
Ctx_CookProfile : Consent Management
Ctx_CookProfile : Consent
Ctx_Sharing : Consent covers Picture
Ctx_ConsentManagement : What is consent of — terms of use, guests in pictures, or both?
Ctx_Media : Grandma Avatar AI
Ctx_GrandmaAvatarAI : Help request
Ctx_CookingAssistance : Help response
Ctx_GrandmaAvatarAI : GrandmaAvatar is HelpProvider
Ctx_CookingAssistance : Does the avatar own a Help request, or hold a translated copy?
Ctx_GrandmaAvatarAI : Menu
Ctx_CookingAssistance : Ingredients
Ctx_CookingAssistance : Recipe
Ctx_CookingAssistance : Recipe
Ctx_MealPreparation : Menu
Ctx_MealPreparation : Recipe
Ctx_MealPlanning : Help response
Ctx_MealPlanning : Meal with Recipe
Ctx_MealPlanning : A plan cannot settle short — no ingredient outstanding
Ctx_MealPlanning : Who reads a settled meal plan? Nothing on the map does.
Ctx_MealPlanning : Menu
Ctx_CookingAssistance : Recipe
Ctx_CookingAssistance : Help response
Ctx_Sharing : Recipe
Ctx_MealPreparation : Menu
Ctx_MealPreparation : Help response
Ctx_MealPreparation : Help (advice to act on)
Ctx_MealPreparation : Advice alone is not a rescue; the cook confirms
Ctx_MealPreparation : What is the cook holding while cooking? No aggregate on any artifact.
Ctx_MealPreparation : Pictures
Ctx_CookingAssistance : Pictures
Ctx_Sharing : HelpRequest contains Picture
Ctx_Media : Only the taker publishes
Ctx_Media : One Picture or two — evidence and trophy?
Ctx_Media : Notification
Ctx_CookingAssistance : Help response
Ctx_CookingAssistance : Notification
Ctx_Notification : Notification about HelpRequest
Ctx_Notification : Recipe Catalog
Ctx_RecipeCatalog : Recipe
Ctx_MealPlanning : Recipe contains Step
Ctx_RecipeCatalog : A recipe has at least one step
Ctx_RecipeCatalog : Does anyone create, edit or retire a recipe? No event, no command.
Ctx_RecipeCatalog : Who owns Ingredient — Meal Planning or Recipe Catalog? Both canvases claim to write it.
Ctx_MealPlanning : Pictures
Ctx_Sharing : Help response
Ctx_Sharing : Consent
Ctx_Sharing : Thanks for Help
Ctx_Sharing : Thanks are for exactly one Help
Ctx_Sharing : What triggers Thanks given? No event reaches this context.
Ctx_MealPreparation : A Help Request is 'at 1 Grandma Avatar' and 'at 1 Community' — is every request addressed to both, and never to a Chef?

--- Cost structure / BMC ---
Cost_SoftwareDevelopment : Software development
Cost_SoftwareMaintainance : Software maintainance
Cost_CloudService : Cloud service
Cost_Licenses : Licenses
Cost_Marketing : Marketing
Cost_CloudService : Onboarding

--- Capability Classifications (Core/Supporting/Generic) ---
Cap_AskGrandmaGrandpa : Ask Grandma / Grandpa -> core
Cap_FamousChefsPartner_CM : Member management -> generic
Cap_MemberManagement_CM : Ads management -> generic
Cap_AdsManagement_CM : Cooking -> supporting
Cap_MealChooser_CM : Proposals based on larder, etc. -> core
Cap_ProposalsBasedOnLarderEtc_CM : Planner -> supporting
Cap_Planner_CM : Community Engagement -> core
Cap_CommunityEngagement_CM : Cooking Support -> supporting
Cap_NutritionRequirementManagement_CM : Direct ordering -> generic
Cap_DirectOrdering_CM : Kitchen tool ordering -> generic
Cap_KitchenToolOrdering_CM : Cooking aids -> generic
Cap_CookingAids_CM : Grandma / Grandpa -> core
Cap_GrandmaGrandpa_CM : Sharing -> core
Cap_ContentCreation_CM : Cookbook publishing -> supporting
Cap_CookbookPublishing_CM : Paywall for premium content -> generic
Cap_PaywallForPremiumContent_CM : Rating -> supporting


```

```python
with open("larderEnahnced4.ttl", "r") as f:
    text = f.read()

import re

print("=== WARDLEY MAP COMPONENTS ===")
for match in re.finditer(r'(Cap_\w+|Goal_\w+|Act_\w+)\s+a\s+dkg:(Component|UserNeed|Actor)\s*;\s*skos:prefLabel\s+"([^"]+)"(?:\s*;\s*dkg:evolution\s+dkg:(\w+))?', text):
    print(f"{match.group(1)} | {match.group(3)} | Evolution: {match.group(4)}")

print("\n=== CAPABILITY MAP MARKINGS ===")
for match in re.finditer(r'(Cap_\w+)\s+a\s+dkg:Capability\s*;\s*skos:prefLabel\s+"([^"]+)".*?rdfs:comment\s+"(core|supporting|generic)"', text, re.DOTALL):
    print(f"{match.group(1)}: {match.group(2)} -> {match.group(3)}")

print("\n=== BOUNDED CONTEXTS ===")
for match in re.finditer(r'(Ctx_\w+)\s+a\s+dkg:BoundedContext\s*;\s*skos:prefLabel\s+"([^"]+)"', text):
    print(f"{match.group(1)}: {match.group(2)}")


```

```text
=== WARDLEY MAP COMPONENTS ===
Act_Single | Single | Evolution: None
Act_YoungFamily | Young family | Evolution: None
Act_CookAsAuthor | Cook as author | Evolution: None
Act_KitchenWareRetailer | Kitchen ware retailer | Evolution: None
Act_ChefsAsSeniorPartner | Chefs as senior partner | Evolution: None
Act_GroceryStore | Grocery store | Evolution: None
Act_PhotoBookService | Photo book service | Evolution: None
Act_Propects | Propects | Evolution: None
Act_Convertable | Convertable | Evolution: None
Act_YoungFamilies | Young families | Evolution: None
Act_Singles | Singles | Evolution: None
Act_HomeCook | Home cook | Evolution: None
Act_Prospect | Prospect | Evolution: None
Goal_BetterCookingSkills | Better cooking skills | Evolution: None
Goal_EfficientUsageOfGroceries | Efficient usage of groceries | Evolution: None
Goal_CreateContent | Create content | Evolution: None
Goal_RateContent | Rate content | Evolution: None
Goal_SearchRecipesWithNutritionMeals | Search recipes with nutrition, meals, ... | Evolution: None
Goal_SupportCooking | Support cooking | Evolution: None
Goal_BecomeAMember | Become a member | Evolution: None
Cap_AskGrandmaGrandpa | Ask Grandma / Grandpa | Evolution: CustomBuilt
Cap_AskChef | Ask chef | Evolution: CustomBuilt
Cap_Competitions | Competitions | Evolution: CustomBuilt
Cap_AskCommunity | Ask community | Evolution: CustomBuilt
Cap_Refrigerator | Refrigerator | Evolution: Product
Cap_Nutrition | Nutrition | Evolution: Product
Cap_Meal | Meal | Evolution: Product
Cap_WriteRecipe | Write recipe | Evolution: Commodity
Cap_UploadMedia | Upload media | Evolution: Commodity
Cap_Timer | Timer | Evolution: Commodity
Cap_FreeHandControl | Free hand control | Evolution: Commodity
Cap_MemberManagement | Member Management | Evolution: Commodity
Cap_AdvancedSearch | Advanced Search | Evolution: CustomBuilt
Cap_RateContent | Rate content | Evolution: Product
Cap_RemoteMeeting | Remote meeting | Evolution: Commodity
Cap_Chat | Chat | Evolution: Commodity
Cap_IDM | IDM | Evolution: Commodity
Cap_SpecificAI | Specific AI | Evolution: CustomBuilt
Cap_GenAI | Gen AI | Evolution: Commodity
Cap_MediaStorage | Media Storage | Evolution: Commodity
Cap_RecipeDatabase | Recipe database | Evolution: Product
Cap_RatingDatabase | Rating Database | Evolution: Product
Cap_Cloud | Cloud | Evolution: Commodity
Cap_Mobile | Mobile | Evolution: Commodity
Act_Cook | Cook | Evolution: None
Act_GrandmaAvatar | Grandma Avatar | Evolution: None
Act_Community | Community | Evolution: None
Act_ParentsInLaw | Parents in Law | Evolution: None
Act_Chef | Chef | Evolution: None
Act_Cooks | Cooks | Evolution: None
Act_CommunityAdministrator | Community Administrator | Evolution: None
Act_OtherCooks | Other cooks | Evolution: None
Act_Grandma | Grandma | Evolution: None
Act_User | User | Evolution: None
Act_CommunityCook | Community Cook | Evolution: None

=== CAPABILITY MAP MARKINGS ===
Cap_Onboarding_L1: Onboarding -> core
Cap_MemberManagement_CM: Member management -> generic
Cap_AdsManagement_CM: Ads management -> generic
Cap_Cooking_L1: Cooking -> supporting
Cap_ProposalsBasedOnLarderEtc_CM: Proposals based on larder, etc. -> core
Cap_Planner_CM: Planner -> supporting
Cap_CommunityEngagement_CM: Community Engagement -> core
Cap_CookingSupport_L1: Cooking Support -> supporting
Cap_DirectOrdering_CM: Direct ordering -> generic
Cap_KitchenToolOrdering_CM: Kitchen tool ordering -> generic
Cap_CookingAids_CM: Cooking aids -> generic
Cap_GrandmaGrandpa_CM: Grandma / Grandpa -> core
Cap_Sharing_L1: Sharing -> core
Cap_CookbookPublishing_CM: Cookbook publishing -> supporting
Cap_PaywallForPremiumContent_CM: Paywall for premium content -> generic
Cap_Rating_L1: Rating -> supporting

=== BOUNDED CONTEXTS ===
Ctx_CookProfile: Cook Profile
Ctx_MealPlanning: Meal Planning
Ctx_CookingAssistance: Cooking Assistance
Ctx_CookingHelp: Cooking Help
Ctx_MealPreparation: Meal Preparation
Ctx_Media: Media
Ctx_Sharing: Sharing
Ctx_ConsentManagement: Consent Management
Ctx_GrandmaAvatarAI: Grandma Avatar AI
Ctx_Notification: Notification
Ctx_RecipeCatalog: Recipe Catalog


```

```python
with open("larderEnahnced4.ttl", "r") as f:
    text = f.read()

# Search for capability assertions or comments
import re
print("Capabilities and their explicit core/supporting/generic markings:")
matches = re.findall(r'(Cap_\w+).*?skos:prefLabel\s+"([^"]+)".*?rdfs:comment\s+"(core|supporting|generic)"', text, re.DOTALL)
for m in matches:
    print(f"- {m[0]} ({m[1]}): {m[2]}")


```

```text
Capabilities and their explicit core/supporting/generic markings:
- Cap_AskGrandmaGrandpa (Ask Grandma / Grandpa): core
- Cap_FamousChefsPartner_CM (Member management): generic
- Cap_MemberManagement_CM (Ads management): generic
- Cap_AdsManagement_CM (Cooking): supporting
- Cap_MealChooser_CM (Proposals based on larder, etc.): core
- Cap_ProposalsBasedOnLarderEtc_CM (Planner): supporting
- Cap_Planner_CM (Community Engagement): core
- Cap_CommunityEngagement_CM (Cooking Support): supporting
- Cap_NutritionRequirementManagement_CM (Direct ordering): generic
- Cap_DirectOrdering_CM (Kitchen tool ordering): generic
- Cap_KitchenToolOrdering_CM (Cooking aids): generic
- Cap_CookingAids_CM (Grandma / Grandpa): core
- Cap_GrandmaGrandpa_CM (Sharing): core
- Cap_ContentCreation_CM (Cookbook publishing): supporting
- Cap_CookbookPublishing_CM (Paywall for premium content): generic
- Cap_PaywallForPremiumContent_CM (Rating): supporting


```

```python
with open("larderEnahnced4.ttl", "r") as f:
    text = f.read()

import re
# Let's search specifically for assertions or comments attached to Cap_*_CM nodes
blocks = text.split(":\n")
for block in text.split(":Cap_"):
    lines = block.split("\n")
    cap_id = lines[0].split()[0] if lines else ""
    prefLabel = ""
    comment = ""
    for line in lines:
        if "skos:prefLabel" in line:
            m = re.search(r'"([^"]+)"', line)
            if m: prefLabel = m.group(1)
        if "rdfs:comment" in line and any(w in line for w in ["core", "supporting", "generic"]):
            m = re.search(r'"(core|supporting|generic)"', line)
            if m: comment = m.group(1)
    if prefLabel and comment:
        print(f"Cap_{cap_id} | Label: {prefLabel} | Type: {comment}")


```

```text
Cap_Rating_CM | Label: What do the small ▲ on Member management and ▶ beside Planner mean on the capability map? | Type: supporting
Cap_GrandmaGrandpa_CM | Label: What do the small ▲ on Member management and ▶ beside Planner mean on the capability map? | Type: generic


```

```python
with open("larderEnahnced4.ttl", "r") as f:
    text = f.read()

import re
matches = re.findall(r'(:Cap_\w+_CM).*?skos:prefLabel\s+"([^"]+)".*?rdfs:comment\s+"(core|supporting|generic)"', text, re.DOTALL)
for m in matches:
    print(f"{m[0]} | {m[1]} | {m[2]}")


```

```text
:Cap_FamousChefsPartner_CM | Famous chefs partner | core
:Cap_FamousChefsPartner_CM | Member management | generic
:Cap_MemberManagement_CM | Ads management | generic
:Cap_AdsManagement_CM | Cooking | supporting
:Cap_MealChooser_CM | Proposals based on larder, etc. | core
:Cap_ProposalsBasedOnLarderEtc_CM | Planner | supporting
:Cap_Planner_CM | Community Engagement | core
:Cap_CommunityEngagement_CM | Cooking Support | supporting
:Cap_NutritionRequirementManagement_CM | Direct ordering | generic
:Cap_DirectOrdering_CM | Kitchen tool ordering | generic
:Cap_KitchenToolOrdering_CM | Cooking aids | generic
:Cap_CookingAids_CM | Grandma / Grandpa | core
:Cap_GrandmaGrandpa_CM | Sharing | core
:Cap_ContentCreation_CM | Cookbook publishing | supporting
:Cap_CookbookPublishing_CM | Paywall for premium content | generic
:Cap_PaywallForPremiumContent_CM | Rating | supporting


```

```python
with open("larderEnahnced4.ttl", "r") as f:
    text = f.read()

import re

# Split by triple statements or blocks
for chunk in text.split(":Cap_"):
    first_line = chunk.split("\n")[0]
    cap_name = first_line.split()[0] if first_line else ""
    label_match = re.search(r'skos:prefLabel\s+"([^"]+)"', chunk[:400])
    comment_match = re.search(r'rdfs:comment\s+"(core|supporting|generic)"', chunk[:400])
    if label_match and comment_match:
        print(f"Cap_{cap_name} -> {label_match.group(1)} [{comment_match.group(1)}]")


```

```python
with open("larderEnahnced4.ttl", "r") as f:
    text = f.read()

import re

# Find section starting at INGEST 5
cm_sec = text[text.find("INGEST 5"):text.find("INGEST 6")]
for block in cm_sec.split("\n\n"):
    if ":Cap_" in block:
        name = re.search(r'(:Cap_\w+)', block)
        label = re.search(r'skos:prefLabel\s+"([^"]+)"', block)
        comment = re.search(r'rdfs:comment\s+"(core|supporting|generic)"', block)
        if name and label and comment:
            print(f"{name.group(1)} | {label.group(1)} | {comment.group(1)}")


```

```text
:Cap_Onboarding_L1 | Onboarding | core


```

```python
with open("larderEnahnced4.ttl", "r") as f:
    text = f.read()

cm_sec = text[text.find("INGEST 5"):text.find("INGEST 6")]
print(cm_sec)


```

```text
INGEST 5 — Capability Map, CapabilityMapStep3.jpg — 2026-09-10
# Five level-1 chevrons (Onboarding · Cooking · Cooking Support · Sharing ·
# Rating), 16 level-2 capabilities. Markings ARE drawn: legend Core (blue,
# person-check) / Supporting (green, magnifier) / Generic (yellow, globe).
# Each marking is a dated dkg:Assertion. Two small arrowheads (▲ on Member
# management, ▶ near Planner) have no legend and are recorded as a Question.
# Level-2 IRIs get the _CM suffix where the graph already holds the label in
# another class; identity is proposed, not merged.
##############################################################################

:Art_CapMap a dkg:CapabilityMap ;
    rdfs:label "Larder capability map (CapabilityMapStep3.jpg)" ;
    rdfs:comment "5 level-1, 16 level-2, all 16 marked core/supporting/generic via a drawn legend." ;
    dkg:ingestedAt "2026-09-10"^^xsd:date .

:Cap_Onboarding_L1 a dkg:Capability ; skos:prefLabel "Onboarding" ; rdfs:comment "level-1 chevron" ; dkg:source :Art_CapMap ; dkg:locator "level 1" ; dkg:confidence dkg:OnArtifact .
:Cap_FamousChefsPartner_CM a dkg:Capability ; skos:prefLabel "Famous chefs partner" ; skos:broader :Cap_Onboarding_L1 ; rdfs:comment "core" ; dkg:source :Art_CapMap ; dkg:locator "Onboarding 1" ; dkg:confidence dkg:OnArtifact .
:As_CM_FamousChefsPartner a dkg:Assertion ; rdf:subject :Cap_FamousChefsPartner_CM ; rdf:predicate rdfs:comment ; rdf:object "core" ; rdfs:comment "capability map marking, 2026-09-10" ; dkg:source :Art_CapMap ; dkg:confidence dkg:OnArtifact .
:Cap_MemberManagement_CM a dkg:Capability ; skos:prefLabel "Member management" ; skos:broader :Cap_Onboarding_L1 ; rdfs:comment "generic" ; dkg:source :Art_CapMap ; dkg:locator "Onboarding 2" ; dkg:confidence dkg:OnArtifact .
:As_CM_MemberManagement a dkg:Assertion ; rdf:subject :Cap_MemberManagement_CM ; rdf:predicate rdfs:comment ; rdf:object "generic" ; rdfs:comment "capability map marking, 2026-09-10" ; dkg:source :Art_CapMap ; dkg:confidence dkg:OnArtifact .
:Cap_AdsManagement_CM a dkg:Capability ; skos:prefLabel "Ads management" ; skos:broader :Cap_Onboarding_L1 ; rdfs:comment "generic" ; dkg:source :Art_CapMap ; dkg:locator "Onboarding 3" ; dkg:confidence dkg:OnArtifact .
:As_CM_AdsManagement a dkg:Assertion ; rdf:subject :Cap_AdsManagement_CM ; rdf:predicate rdfs:comment ; rdf:object "generic" ; rdfs:comment "capability map marking, 2026-09-10" ; dkg:source :Art_CapMap ; dkg:confidence dkg:OnArtifact .
:Cap_Cooking_L1 a dkg:Capability ; skos:prefLabel "Cooking" ; rdfs:comment "level-1 chevron" ; dkg:source :Art_CapMap ; dkg:locator "level 1" ; dkg:confidence dkg:OnArtifact .
:Cap_MealChooser_CM a dkg:Capability ; skos:prefLabel "Meal Chooser" ; skos:broader :Cap_Cooking_L1 ; rdfs:comment "supporting" ; dkg:source :Art_CapMap ; dkg:locator "Cooking 1" ; dkg:confidence dkg:OnArtifact .
:As_CM_MealChooser a dkg:Assertion ; rdf:subject :Cap_MealChooser_CM ; rdf:predicate rdfs:comment ; rdf:object "supporting" ; rdfs:comment "capability map marking, 2026-09-10" ; dkg:source :Art_CapMap ; dkg:confidence dkg:OnArtifact .
:Cap_ProposalsBasedOnLarderEtc_CM a dkg:Capability ; skos:prefLabel "Proposals based on larder, etc." ; skos:broader :Cap_Cooking_L1 ; rdfs:comment "core" ; dkg:source :Art_CapMap ; dkg:locator "Cooking 2" ; dkg:confidence dkg:OnArtifact .
:As_CM_ProposalsBasedOnLarderEtc a dkg:Assertion ; rdf:subject :Cap_ProposalsBasedOnLarderEtc_CM ; rdf:predicate rdfs:comment ; rdf:object "core" ; rdfs:comment "capability map marking, 2026-09-10" ; dkg:source :Art_CapMap ; dkg:confidence dkg:OnArtifact .
:Cap_Planner_CM a dkg:Capability ; skos:prefLabel "Planner" ; skos:broader :Cap_Cooking_L1 ; rdfs:comment "supporting" ; dkg:source :Art_CapMap ; dkg:locator "Cooking 3" ; dkg:confidence dkg:OnArtifact .
:As_CM_Planner a dkg:Assertion ; rdf:subject :Cap_Planner_CM ; rdf:predicate rdfs:comment ; rdf:object "supporting" ; rdfs:comment "capability map marking, 2026-09-10" ; dkg:source :Art_CapMap ; dkg:confidence dkg:OnArtifact .
:Cap_CommunityEngagement_CM a dkg:Capability ; skos:prefLabel "Community Engagement" ; skos:broader :Cap_Cooking_L1 ; rdfs:comment "core" ; dkg:source :Art_CapMap ; dkg:locator "Cooking 4" ; dkg:confidence dkg:OnArtifact .
:As_CM_CommunityEngagement a dkg:Assertion ; rdf:subject :Cap_CommunityEngagement_CM ; rdf:predicate rdfs:comment ; rdf:object "core" ; rdfs:comment "capability map marking, 2026-09-10" ; dkg:source :Art_CapMap ; dkg:confidence dkg:OnArtifact .
:Cap_CookingSupport_L1 a dkg:Capability ; skos:prefLabel "Cooking Support" ; rdfs:comment "level-1 chevron" ; dkg:source :Art_CapMap ; dkg:locator "level 1" ; dkg:confidence dkg:OnArtifact .
:Cap_NutritionRequirementManagement_CM a dkg:Capability ; skos:prefLabel "Nutrition requirement management" ; skos:broader :Cap_CookingSupport_L1 ; rdfs:comment "supporting" ; dkg:source :Art_CapMap ; dkg:locator "Cooking Support 1" ; dkg:confidence dkg:OnArtifact .
:As_CM_NutritionRequirementManagement a dkg:Assertion ; rdf:subject :Cap_NutritionRequirementManagement_CM ; rdf:predicate rdfs:comment ; rdf:object "supporting" ; rdfs:comment "capability map marking, 2026-09-10" ; dkg:source :Art_CapMap ; dkg:confidence dkg:OnArtifact .
:Cap_DirectOrdering_CM a dkg:Capability ; skos:prefLabel "Direct ordering" ; skos:broader :Cap_CookingSupport_L1 ; rdfs:comment "generic" ; dkg:source :Art_CapMap ; dkg:locator "Cooking Support 2" ; dkg:confidence dkg:OnArtifact .
:As_CM_DirectOrdering a dkg:Assertion ; rdf:subject :Cap_DirectOrdering_CM ; rdf:predicate rdfs:comment ; rdf:object "generic" ; rdfs:comment "capability map marking, 2026-09-10" ; dkg:source :Art_CapMap ; dkg:confidence dkg:OnArtifact .
:Cap_KitchenToolOrdering_CM a dkg:Capability ; skos:prefLabel "Kitchen tool ordering" ; skos:broader :Cap_CookingSupport_L1 ; rdfs:comment "generic" ; dkg:source :Art_CapMap ; dkg:locator "Cooking Support 3" ; dkg:confidence dkg:OnArtifact .
:As_CM_KitchenToolOrdering a dkg:Assertion ; rdf:subject :Cap_KitchenToolOrdering_CM ; rdf:predicate rdfs:comment ; rdf:object "generic" ; rdfs:comment "capability map marking, 2026-09-10" ; dkg:source :Art_CapMap ; dkg:confidence dkg:OnArtifact .
:Cap_CookingAids_CM a dkg:Capability ; skos:prefLabel "Cooking aids" ; skos:broader :Cap_CookingSupport_L1 ; rdfs:comment "generic" ; dkg:source :Art_CapMap ; dkg:locator "Cooking Support 4" ; dkg:confidence dkg:OnArtifact .
:As_CM_CookingAids a dkg:Assertion ; rdf:subject :Cap_CookingAids_CM ; rdf:predicate rdfs:comment ; rdf:object "generic" ; rdfs:comment "capability map marking, 2026-09-10" ; dkg:source :Art_CapMap ; dkg:confidence dkg:OnArtifact .
:Cap_GrandmaGrandpa_CM a dkg:Capability ; skos:prefLabel "Grandma / Grandpa" ; skos:broader :Cap_CookingSupport_L1 ; rdfs:comment "core" ; dkg:source :Art_CapMap ; dkg:locator "Cooking Support 5" ; dkg:confidence dkg:OnArtifact .
:As_CM_GrandmaGrandpa a dkg:Assertion ; rdf:subject :Cap_GrandmaGrandpa_CM ; rdf:predicate rdfs:comment ; rdf:object "core" ; rdfs:comment "capability map marking, 2026-09-10" ; dkg:source :Art_CapMap ; dkg:confidence dkg:OnArtifact .
:Cap_Sharing_L1 a dkg:Capability ; skos:prefLabel "Sharing" ; rdfs:comment "level-1 chevron" ; dkg:source :Art_CapMap ; dkg:locator "level 1" ; dkg:confidence dkg:OnArtifact .
:Cap_ContentCreation_CM a dkg:Capability ; skos:prefLabel "Content Creation" ; skos:broader :Cap_Sharing_L1 ; rdfs:comment "core" ; dkg:source :Art_CapMap ; dkg:locator "Sharing 1" ; dkg:confidence dkg:OnArtifact .
:As_CM_ContentCreation a dkg:Assertion ; rdf:subject :Cap_ContentCreation_CM ; rdf:predicate rdfs:comment ; rdf:object "core" ; rdfs:comment "capability map marking, 2026-09-10" ; dkg:source :Art_CapMap ; dkg:confidence dkg:OnArtifact .
:Cap_CookbookPublishing_CM a dkg:Capability ; skos:prefLabel "Cookbook publishing" ; skos:broader :Cap_Sharing_L1 ; rdfs:comment "supporting" ; dkg:source :Art_CapMap ; dkg:locator "Sharing 2" ; dkg:confidence dkg:OnArtifact .
:As_CM_CookbookPublishing a dkg:Assertion ; rdf:subject :Cap_CookbookPublishing_CM ; rdf:predicate rdfs:comment ; rdf:object "supporting" ; rdfs:comment "capability map marking, 2026-09-10" ; dkg:source :Art_CapMap ; dkg:confidence dkg:OnArtifact .
:Cap_PaywallForPremiumContent_CM a dkg:Capability ; skos:prefLabel "Paywall for premium content" ; skos:broader :Cap_Sharing_L1 ; rdfs:comment "generic" ; dkg:source :Art_CapMap ; dkg:locator "Sharing 3" ; dkg:confidence dkg:OnArtifact .
:As_CM_PaywallForPremiumContent a dkg:Assertion ; rdf:subject :Cap_PaywallForPremiumContent_CM ; rdf:predicate rdfs:comment ; rdf:object "generic" ; rdfs:comment "capability map marking, 2026-09-10" ; dkg:source :Art_CapMap ; dkg:confidence dkg:OnArtifact .
:Cap_Rating_L1 a dkg:Capability ; skos:prefLabel "Rating" ; rdfs:comment "level-1 chevron" ; dkg:source :Art_CapMap ; dkg:locator "level 1" ; dkg:confidence dkg:OnArtifact .
:Cap_Rating_CM a dkg:Capability ; skos:prefLabel "Rating" ; skos:broader :Cap_Rating_L1 ; rdfs:comment "supporting" ; dkg:source :Art_CapMap ; dkg:locator "Rating 1" ; dkg:confidence dkg:OnArtifact .
:As_CM_Rating a dkg:Assertion ; rdf:subject :Cap_Rating_CM ; rdf:predicate rdfs:comment ; rdf:object "supporting" ; rdfs:comment "capability map marking, 2026-09-10" ; dkg:source :Art_CapMap ; dkg:confidence dkg:OnArtifact .

:Q_CapMapArrows a dkg:Question ;
    skos:prefLabel "What do the small ▲ on Member management and ▶ beside Planner mean on the capability map?" ;
    dkg:source :Art_CapMap ; dkg:confidence dkg:OnArtifact .

# --- merge ledger ---
:Cap_FamousChefsPartner_CM dkg:proposedSameAs :Del_FamousChefsPartner, :Act_Chefs, :Cap_AskChef ; rdfs:comment "exact label with the impact-map deliverable; capability vs deliverable" .
:Cap_MemberManagement_CM dkg:proposedSameAs :Del_MemberManagement, :Cap_MemberManagement ; rdfs:comment "exact label with the deliverable; Wardley writes 'Member Management' — both are dkg:Capability but the Wardley node carries an evolution stage, so proposed rather than auto-merged" .
:Cap_AdsManagement_CM dkg:proposedSameAs :Del_AdsManagement .
:Cap_MealChooser_CM dkg:proposedSameAs :Del_MealChooser, :Idea_MealChooser .
:Cap_ProposalsBasedOnLarderEtc_CM dkg:proposedSameAs :Del_AIProposals, :VP_AIProposals, :Idea_LarderTracker ; rdfs:comment "'Proposals based on larder, etc.' vs 'AI proposals' — the capability map drops the word AI" .
:Cap_Planner_CM dkg:proposedSameAs :Del_Planner, :VP_Planner .
:Cap_CommunityEngagement_CM dkg:proposedSameAs :CR_Community, :Cluster_Community, :Del_CookingClub ; rdfs:comment "no other artifact uses 'Community Engagement'; nearest are the BMC relationship, the brainstorm cluster and the Cooking club deliverable" .
:Cap_NutritionRequirementManagement_CM dkg:proposedSameAs :Del_NutritionRequirementManagement, :Cap_Nutrition .
:Cap_DirectOrdering_CM dkg:proposedSameAs :Del_DirectOrdering .
:Cap_KitchenToolOrdering_CM dkg:proposedSameAs :Del_KitchenToolOrdering .
:Cap_CookingAids_CM dkg:proposedSameAs :Cap_Timer, :Cap_FreeHandControl, :Cluster_Helper ; rdfs:comment "'Cooking aids' appears nowhere else; Wardley's Timer / Free hand control and the brainstorm 'Helper' cluster are the candidates" .
:Cap_GrandmaGrandpa_CM dkg:proposedSameAs :Del_GrandmaGrandpaAI, :Cap_AskGrandmaGrandpa ; rdfs:comment "capability map says 'Grandma / Grandpa'; impact map says 'Grandma / Grandpa AI'; Wardley says 'Ask Grandma / Grandpa'" .
:Cap_ContentCreation_CM dkg:proposedSameAs :Cap_ContentCreation, :Cluster_ContentCreation ; rdfs:comment "exact label with the BMC key activity; kept apart because the map marks it core and the canvas makes no such claim" .
:Cap_CookbookPublishing_CM dkg:proposedSameAs :Del_CookbookPublishing, :Idea_CookbookPublishing .
:Cap_PaywallForPremiumContent_CM dkg:proposedSameAs :Del_PaywallForPremiumContent .
:Cap_Rating_CM dkg:proposedSameAs :Del_Rating, :Cap_RateContent, :Cap_ContentRating, :Idea_Rating .
:Cap_CookingSupport_L1 dkg:proposedSameAs :Cap_CookingSupport, :Cluster_CookingSupport ; rdfs:comment "exact label with the BMC key activity and the brainstorm cluster; level-1 bundle, so not auto-merged" .
:Cap_Sharing_L1 dkg:proposedSameAs :CR_DailyForSharing .
:Cap_Rating_L1 dkg:proposedSameAs :Cap_ContentRating .
:Cap_Onboarding_L1 dkg:proposedSameAs :Goal_BecomeAMember, :Cluster_Authentication .


##############################################################################
# 


```

A **modular monolith** is the optimal architectural choice for the Larder project, balancing clean domain isolation with operational simplicity.

**Why a Modular Monolith Fits Strategy and Scale**

* **Scale and Resource Alignment**: The business target focuses on reaching a minimum of 2,500 paying members within two years while operating under an effective limited budget. Minimizing software development, maintenance, and cloud service costs is essential at this early growth stage.


* **Explicit Bounded Context Isolation**: EventStorming and bounded context modeling reveal distinct functional areas, including `Meal Planning`, `Meal Preparation`, `Cooking Assistance`, `Media`, `Sharing`, `Consent Management`, and `Recipe Catalog`. A modular monolith enforces these strict domain boundaries within code packages without incurring distributed system overhead.


* **Strategic Capability Mapping**: The Wardley Map identifies commodity capabilities (`Cloud`, `IDM`, `Media Storage`, `Gen AI`) that should be consumed directly via third-party integrations. Custom core capabilities (`Proposals based on larder`, `Grandma / Grandpa AI`, `Community Engagement`) can be rapidly iterated inside dedicated application modules.



**Drawbacks of a Traditional Monolith**

* **Domain Drift and Coupling**: Complex interactions—such as logging a catastrophe photo during `Meal Preparation` and requesting helper feedback—can quickly lead to tangled dependencies if strict module boundaries are not enforced at compile time.



**Why Microservices are Premature**

* **High Infrastructure Overhead**: Operating independent microservices for ~9 bounded contexts introduces excessive network latency, deployment complexity, and service-management costs for an initial user base of 2,500 members.


* **Evolutionary Extraction Path**: Maintaining clear module boundaries ensures that if compute-intensive contexts (such as `Grandma Avatar AI` or media processing) require separate deployment in the future, they can be cleanly extracted into standalone services.



Would you like to explore how to define the internal module interfaces between `Meal Planning`, `Cooking Assistance`, and `Grandma Avatar AI` within the monolith?