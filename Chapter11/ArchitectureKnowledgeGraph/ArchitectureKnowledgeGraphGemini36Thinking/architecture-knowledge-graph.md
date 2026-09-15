```python
import rdflib
print(rdflib.__version__)


```

```text
Traceback (most recent call last):
  File "<xbox-string>", line 1, in <module>
    import rdflib
ModuleNotFoundError: No module named 'rdflib'


```

Ingesting `LarderArchitecturalPrinciples.md`, full-form ADRs (`ADR0001`–`ADR0005`), and small-log decisions (`SADR0001`–`SADR0007`) in **seed** mode under base IRI `[https://w3id.org/dkg/graph/larder#](https://w3id.org/dkg/graph/larder#)`.

```turtle
# dkg-adr.ttl — Vocabulary Extension for ADR & Principles Ingestion
@prefix dkg:  <https://w3id.org/dkg/ns#> .
@prefix rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .
@prefix owl:  <http://www.w3.org/2002/07/owl#> .

<https://w3id.org/dkg/ns/adr> a owl:Ontology ;
    rdfs:label "DKG ADR & Principles Extension Ontology" .

dkg:PrinciplesDocument a owl:Class ; rdfs:subClassOf dkg:Artifact .
dkg:ADRDocument        a owl:Class ; rdfs:subClassOf dkg:Artifact .
dkg:ADRLog            a owl:Class ; rdfs:subClassOf dkg:Artifact .

dkg:Principle a owl:Class .
dkg:Decision  a owl:Class .

dkg:principleId   a owl:DatatypeProperty .
dkg:decisionId    a owl:DatatypeProperty .
dkg:statement     a owl:DatatypeProperty .
dkg:implication   a owl:DatatypeProperty .
dkg:scope         a owl:DatatypeProperty .
dkg:testable      a owl:DatatypeProperty .
dkg:sourceFormat  a owl:DatatypeProperty .
dkg:decidedOption a owl:DatatypeProperty .
dkg:consideredOption a owl:DatatypeProperty .
dkg:consequence   a owl:DatatypeProperty .
dkg:acknowledged  a owl:DatatypeProperty .
dkg:citedBy       a owl:DatatypeProperty .

dkg:honors        a owl:ObjectProperty .
dkg:overrides     a owl:ObjectProperty .
dkg:cites         a owl:ObjectProperty .
dkg:inTensionWith a owl:ObjectProperty .

```

```turtle
# graph.ttl — Larder ADR & Principles Knowledge Graph
@prefix dkg:  <https://w3id.org/dkg/ns#> .
@prefix :     <https://w3id.org/dkg/graph/larder#> .
@prefix rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .
@prefix owl:  <http://www.w3.org/2002/07/owl#> .
@prefix skos: <http://www.w3.org/2004/02/skos/core#> .
@prefix xsd:  <http://www.w3.org/2001/XMLSchema#> .

<https://w3id.org/dkg/graph/larder> a owl:Ontology ;
    owl:imports <https://w3id.org/dkg/ns> , <https://w3id.org/dkg/ns/adr> ;
    rdfs:label "Larder — ADR & principles knowledge graph" .

# Artifacts
:Art_Principles_Larder a dkg:PrinciplesDocument , dkg:Artifact ;
    rdfs:label "LarderArchitecturalPrinciples.md" ;
    dkg:defect "Duplicate principle ID AP0005 assigned to Automatic Testing and Micro-UIs." ;
    dkg:ingestedAt "2026-09-15"^^xsd:date .

:Art_ADR_ADR0001 a dkg:ADRDocument , dkg:Artifact ; rdfs:label "ADR0001-Monolith.md" ; dkg:ingestedAt "2026-09-15"^^xsd:date .
:Art_ADR_ADR0002 a dkg:ADRDocument , dkg:Artifact ; rdfs:label "ADR0002-Database.md" ; dkg:ingestedAt "2026-09-15"^^xsd:date ;
    dkg:defect "Consequences table headers read Modular Monolith / Microservices / Monolith instead of database options." .
:Art_ADR_ADR0003 a dkg:ADRDocument , dkg:Artifact ; rdfs:label "ADR0003-AsynchronousCommunication.md" ; dkg:ingestedAt "2026-09-15"^^xsd:date .
:Art_ADR_ADR0004 a dkg:ADRDocument , dkg:Artifact ; rdfs:label "ADR0004-SynchronousCommunicationViaMicroUi.md" ; dkg:ingestedAt "2026-09-15"^^xsd:date .
:Art_ADR_ADR0005 a dkg:ADRDocument , dkg:Artifact ; rdfs:label "ADR0005-Orchestrator.md" ; dkg:ingestedAt "2026-09-15"^^xsd:date .
:Art_ADRLog_SadrsLarder a dkg:ADRLog , dkg:Artifact ; rdfs:label "SadrsLarder.md" ; dkg:ingestedAt "2026-09-15"^^xsd:date .

# Principles
:Prin_AP0001_MonolithFirst a dkg:Principle ;
    skos:prefLabel "Monolith before Microservices" ; dkg:principleId "AP0001" ;
    dkg:statement "We will always start with a deployment monolith before cutting services out. However, the modules are modular regarding DDD principles." ;
    dkg:rationale "In the beginning, we do not know exactly the load and performance issues the single services have to face. Therefore, we will cut later with more experience." ;
    dkg:implication "We will have later costs for cutting out services. The teams need to be disciplined to follow the strict boundaries of the Bounded Contexts." ;
    dkg:scope "deployment shape and service cutting" ; dkg:testable true ; dkg:status "Adopted" ; dkg:amendedAt "2026-09-10"^^xsd:date ;
    dkg:source :Art_Principles_Larder ; dkg:locator "AP0001" ; dkg:confidence dkg:OnArtifact .

:Prin_AP0002_AsyncBetweenContexts a dkg:Principle ;
    skos:prefLabel "Asynchronous Communication before Synchronous" ; dkg:principleId "AP0002" ;
    dkg:statement "For communication between Bounded Contexts asynchronous communication is preferred." ;
    dkg:rationale "Asynchronous communication guarantees the loose coupling between Bounded Contexts." ;
    dkg:implication "Loose coupling guarantees that we can later maintain and operate our application without large downtimes." ;
    dkg:scope "communication between Bounded Contexts" ; dkg:testable true ; dkg:status "Adopted" ; dkg:amendedAt "2026-09-10"^^xsd:date ;
    dkg:source :Art_Principles_Larder ; dkg:locator "AP0002" ; dkg:confidence dkg:OnArtifact .

:Prin_AP0003_DDD a dkg:Principle ;
    skos:prefLabel "Using DDD" ; dkg:principleId "AP0003" ;
    dkg:statement "For design of our functional architecture, we will apply the principles of DDD." ;
    dkg:rationale "Architecture coming out of the blue does not support the business requirements functionally." ;
    dkg:implication "We need to support the functions required by business. Moreover, we need to support the maintenance of those functions for the upcoming years." ;
    dkg:scope "functional architecture design" ; dkg:testable true ; dkg:status "Adopted" ; dkg:amendedAt "2026-09-10"^^xsd:date ;
    dkg:source :Art_Principles_Larder ; dkg:locator "AP0003" ; dkg:confidence dkg:OnArtifact .

:Prin_AP0004_Residuality a dkg:Principle ;
    skos:prefLabel "Using Residuality" ; dkg:principleId "AP0004" ;
    dkg:statement "For design of our technical architecture, we will apply the principles of Residuality Theory." ;
    dkg:rationale "Stressing an architecture with randomly selected stressors creates more sustaining architectures." ;
    dkg:implication "We do not know what future brings, therefore, we need the robust and best equipped architecture." ;
    dkg:scope "technical architecture design" ; dkg:testable true ; dkg:status "Adopted" ; dkg:amendedAt "2026-09-10"^^xsd:date ;
    dkg:source :Art_Principles_Larder ; dkg:locator "AP0004" ; dkg:confidence dkg:OnArtifact .

:Prin_AP0005a_AutomaticTesting a dkg:Principle ;
    skos:prefLabel "Automatic Testing" ; dkg:principleId "AP0005" ;
    dkg:statement "We can test our system by 95% automatically." ;
    dkg:rationale "Manual tests are too slow to allow us to test our system manually." ;
    dkg:implication "We need to follow a Test-Driven-Approach consequently." ;
    dkg:scope "testing approach" ; dkg:testable true ; dkg:status "Adopted" ; dkg:amendedAt "2026-09-10"^^xsd:date ;
    dkg:source :Art_Principles_Larder ; dkg:locator "AP0005 (first entry)" ; dkg:confidence dkg:OnArtifact .

:Prin_AP0005b_MicroUIs a dkg:Principle ;
    skos:prefLabel "Micro-UIs" ; dkg:principleId "AP0005" ;
    dkg:statement "The Bounded Context publish their own UI as micro-UIs." ;
    dkg:rationale "The teams developing the Bounded Contexts must be as independent as possible. The UIs are part of the Bounded Contexts projects. The micro UIs can use a common platform, so that they appear out of one hand." ;
    dkg:implication "Using a large monolithic frontend would us slow down." ;
    dkg:scope "UI ownership and team independence" ; dkg:testable true ; dkg:status "Adopted" ; dkg:amendedAt "2026-09-10"^^xsd:date ;
    dkg:source :Art_Principles_Larder ; dkg:locator "AP0005 (second entry)" ; dkg:confidence dkg:OnArtifact .

:Prin_AP0006_SyncForUIs a dkg:Principle ;
    skos:prefLabel "Synchronous communication for UIS" ; dkg:principleId "AP0006" ;
    dkg:statement "UI communication is done synchronously." ;
    dkg:rationale "UIs require synchronous communication." ;
    dkg:implication "UIs must communicate with humans, who expect to get immediate answers." ;
    dkg:scope "UI communication" ; dkg:testable true ; dkg:status "Adopted" ; dkg:amendedAt "2026-09-10"^^xsd:date ;
    dkg:source :Art_Principles_Larder ; dkg:locator "AP0006" ; dkg:confidence dkg:OnArtifact .

:Prin_AP0007_Monitoring a dkg:Principle ;
    skos:prefLabel "Monitoring" ; dkg:principleId "AP0007" ;
    dkg:statement "The system can be monitored to identify system-critical states reliable and fast." ;
    dkg:rationale "The operation of the software must be almost completely automated. We need to get a reliable alarm system based on a reliable monitoring system" ;
    dkg:implication "Using a large monolithic frontend would us slow down." ;
    dkg:defect "Implications text is copy-pasted from AP0005 (Micro-UIs)." ;
    dkg:scope "operations / observability" ; dkg:testable true ; dkg:status "Adopted" ; dkg:amendedAt "2026-09-10"^^xsd:date ;
    dkg:source :Art_Principles_Larder ; dkg:locator "AP0007" ; dkg:confidence dkg:OnArtifact .

:Prin_AP0008_FineGrainedAccess a dkg:Principle ;
    skos:prefLabel "Fine grained access rights" ; dkg:principleId "AP0008" ;
    dkg:statement "Domain artifacts are controlled by a fine-grained access control." ;
    dkg:rationale "The software will increase with time, and we need to respect the personal and author rights of our users." ;
    dkg:implication "With more coarse grained rights, we could contradict legal regulations." ;
    dkg:scope "access control on domain artifacts and user data" ; dkg:testable true ; dkg:status "Adopted" ; dkg:amendedAt "2026-09-10"^^xsd:date ;
    dkg:source :Art_Principles_Larder ; dkg:locator "AP0008" ; dkg:confidence dkg:OnArtifact .

# Decisions
:D_ADR0001 a dkg:Decision ; skos:prefLabel "Using Monolith" ; dkg:decisionId "ADR0001" ; dkg:sourceFormat "full-form" ;
    dkg:status "Adopted" ; dkg:decidedAt "2026-09-10"^^xsd:date ; dkg:decidedBy "Annegret Junker, Architect" ;
    dkg:decidedOption "We will implement Larder as Monolith." ;
    dkg:defect "Bold decision states Monolith, but Options section marks Monolith as contradicting AP0001 while modular monolith is favored." ;
    dkg:cites :Prin_AP0001_MonolithFirst ; dkg:honors :Prin_AP0001_MonolithFirst , :Prin_AP0003_DDD ;
    dkg:source :Art_ADR_ADR0001 ; dkg:locator "ADR0001" ; dkg:confidence dkg:OnArtifact .

:D_ADR0002 a dkg:Decision ; skos:prefLabel "Using One Database Instance" ; dkg:decisionId "ADR0002" ; dkg:sourceFormat "full-form" ;
    dkg:status "Adopted" ; dkg:decidedAt "2026-09-10"^^xsd:date ; dkg:decidedBy "Annegret Junker, Architect" ;
    dkg:decidedOption "We will use one database instance for Larder." ;
    dkg:cites :Prin_AP0001_MonolithFirst ; dkg:honors :Prin_AP0001_MonolithFirst ;
    dkg:source :Art_ADR_ADR0002 ; dkg:locator "ADR0002" ; dkg:confidence dkg:OnArtifact .

:D_ADR0003 a dkg:Decision ; skos:prefLabel "Using Asynchronous Communication" ; dkg:decisionId "ADR0003" ; dkg:sourceFormat "full-form" ;
    dkg:status "Adopted" ; dkg:decidedAt "2026-09-10"^^xsd:date ; dkg:decidedBy "Annegret Junker, Architect" ;
    dkg:decidedOption "We will use asynchronous communication with RabbitMQ." ;
    dkg:cites :Prin_AP0002_AsyncBetweenContexts ; dkg:honors :Prin_AP0002_AsyncBetweenContexts , :Prin_AP0003_DDD ;
    dkg:source :Art_ADR_ADR0003 ; dkg:locator "ADR0003" ; dkg:confidence dkg:OnArtifact .

:D_ADR0004 a dkg:Decision ; skos:prefLabel "Synchronous Communication per UI" ; dkg:decisionId "ADR0004" ; dkg:sourceFormat "full-form" ;
    dkg:status "Adopted" ; dkg:decidedAt "2026-09-10"^^xsd:date ; dkg:decidedBy "Annegret Junker, Architect" ;
    dkg:decidedOption "We will use synchronous communication for User Interfaces." ;
    dkg:cites :Prin_AP0006_SyncForUIs , :Prin_AP0008_FineGrainedAccess ; dkg:honors :Prin_AP0006_SyncForUIs , :Prin_AP0008_FineGrainedAccess ;
    dkg:source :Art_ADR_ADR0004 ; dkg:locator "ADR0004" ; dkg:confidence dkg:OnArtifact .

:D_ADR0005 a dkg:Decision ; skos:prefLabel "Orchestrator UI" ; dkg:decisionId "ADR0005" ; dkg:sourceFormat "full-form" ;
    dkg:status "Adopted" ; dkg:decidedAt "2026-09-10"^^xsd:date ; dkg:decidedBy "Annegret Junker, Architect" ;
    dkg:decidedOption "We will use an orchestrator for the UI or Larder. It ingests the micro UIs of the Bounded Context and provides an AppShell." ;
    dkg:overrides :Prin_AP0005b_MicroUIs ; dkg:honors :Prin_AP0005b_MicroUIs , :Prin_AP0003_DDD , :Prin_AP0006_SyncForUIs ;
    dkg:source :Art_ADR_ADR0005 ; dkg:locator "ADR0005" ; dkg:confidence dkg:OnArtifact .

# Assertions
:As_ADR0005_overrides_AP0005b a dkg:Assertion ;
    rdf:subject :D_ADR0005 ; rdf:predicate dkg:overrides ; rdf:object :Prin_AP0005b_MicroUIs ;
    dkg:acknowledged false ;
    rdfs:comment "Consequences table states 'All teams depend on AppShell team', directly breaking team independence rationale of AP0005b without acknowledging an exception." ;
    dkg:source :Art_ADR_ADR0005 ; dkg:confidence dkg:Implied .

# Small ADR Log Rows
:D_SADR0001 a dkg:Decision ; skos:prefLabel "Chef needs to be accessed exclusively" ; dkg:decisionId "SADR0001" ; dkg:sourceFormat "small-log" ; dkg:status "Adopted" ; dkg:decidedAt "2026-09-07"^^xsd:date ; dkg:decidedBy "Junker" ; dkg:decidedOption "Chef needs to be accessed exclusively" ; dkg:source :Art_ADRLog_SadrsLarder ; dkg:locator "SADR0001" ; dkg:confidence dkg:OnArtifact .
:D_SADR0002 a dkg:Decision ; skos:prefLabel "Meal Plan" ; dkg:decisionId "SADR0002" ; dkg:sourceFormat "small-log" ; dkg:status "Superseded" ; dkg:decidedAt "2026-09-08"^^xsd:date ; dkg:decidedBy "Junker" ; dkg:decidedOption "Meal plan is used for Menu and Meal plan" ; dkg:source :Art_ADRLog_SadrsLarder ; dkg:locator "SADR0002" ; dkg:confidence dkg:OnArtifact .
:D_SADR0003 a dkg:Decision ; skos:prefLabel "Menu" ; dkg:decisionId "SADR0003" ; dkg:sourceFormat "small-log" ; dkg:status "Adopted" ; dkg:decidedAt "2026-09-09"^^xsd:date ; dkg:decidedBy "Junker" ; dkg:decidedOption "Menu is used for Menu and Meal plan" ; dkg:supersedes :D_SADR0002 ; dkg:source :Art_ADRLog_SadrsLarder ; dkg:locator "SADR0003" ; dkg:confidence dkg:OnArtifact .
:D_SADR0004 a dkg:Decision ; skos:prefLabel "Help" ; dkg:decisionId "SADR0004" ; dkg:sourceFormat "small-log" ; dkg:status "Adopted" ; dkg:decidedAt "2026-09-11"^^xsd:date ; dkg:decidedBy "Junker" ; dkg:decidedOption "Help response / Help request" ; dkg:source :Art_ADRLog_SadrsLarder ; dkg:locator "SADR0004" ; dkg:confidence dkg:OnArtifact .
:D_SADR0005 a dkg:Decision ; skos:prefLabel "Visual Glossary with Legend" ; dkg:decisionId "SADR0005" ; dkg:sourceFormat "small-log" ; dkg:status "Adopted" ; dkg:decidedAt "2026-09-11"^^xsd:date ; dkg:decidedBy "Junker" ; dkg:decidedOption "Each Visual Glossary needs a legend about the meaning of icons and colors" ; dkg:source :Art_ADRLog_SadrsLarder ; dkg:locator "SADR0005" ; dkg:confidence dkg:OnArtifact .
:D_SADR0006 a dkg:Decision ; skos:prefLabel "Brainstorming with Legend" ; dkg:decisionId "SADR0006" ; dkg:sourceFormat "small-log" ; dkg:status "Adopted" ; dkg:decidedAt "2026-09-11"^^xsd:date ; dkg:decidedBy "Junker" ; dkg:decidedOption "Each Brainstorming result needs a legend about the meaning of icons and colors" ; dkg:source :Art_ADRLog_SadrsLarder ; dkg:locator "SADR0006" ; dkg:confidence dkg:OnArtifact .
:D_SADR0007 a dkg:Decision ; skos:prefLabel "Capability Map with Legend" ; dkg:decisionId "SADR0007" ; dkg:sourceFormat "small-log" ; dkg:status "Adopted" ; dkg:decidedAt "2026-09-11"^^xsd:date ; dkg:decidedBy "Junker" ; dkg:decidedOption "The Capability Map needs a legend" ; dkg:source :Art_ADRLog_SadrsLarder ; dkg:locator "SADR0007" ; dkg:confidence dkg:OnArtifact .

```

```json
{
  "unacknowledged_overrides": [
    {
      "decision": "D_ADR0005",
      "principle": "Prin_AP0005b_MicroUIs",
      "evidence": "ADR0005 consequences table states 'All teams depend on AppShell team', which contradicts AP0005b rationale ('teams must be as independent as possible'), but AP0005b is never cited or acknowledged as an exception."
    }
  ],
  "acknowledged_exceptions": [],
  "dead_letter_principles": [
    "Prin_AP0004_Residuality",
    "Prin_AP0005a_AutomaticTesting",
    "Prin_AP0007_Monitoring"
  ],
  "decisions_citing_no_principle": [
    "D_ADR0005"
  ],
  "untestable_principles": [],
  "principle_tensions": [],
  "active_decisions_contradictions": [],
  "source_defects": [
    {
      "artifact": "Art_Principles_Larder",
      "defect": "Duplicate principle ID 'AP0005' assigned to both 'Automatic Testing' and 'Micro-UIs'."
    },
    {
      "artifact": "Art_Principles_Larder",
      "defect": "AP0007 (Monitoring) copy-pasted implications text from AP0005 ('Using a large monolithic frontend would us slow down.')."
    },
    {
      "artifact": "Art_ADR_ADR0001",
      "defect": "Decision statement names 'Monolith', but options section flags Monolith as contradicting AP0001, while Modular Monolith is favored."
    },
    {
      "artifact": "Art_ADR_ADR0002",
      "defect": "Consequences table headers read 'Modular Monolith / Microservices / Monolith' (copied from ADR0001) instead of database options."
    }
  ]
}

```

---

# ADR & Principles — Larder

**Mode: seed.** Ingested `LarderArchitecturalPrinciples.md`, five full-form ADRs (`ADR0001`–`ADR0005`), and seven small-log ADR rows (`SADR0001`–`SADR0007`) into new graph `graph.ttl`.

## 1. What went in

* **Artifacts**: 7 total (1 principles document, 5 full-form ADR files, 1 small-log table).


* **Nodes**: 9 Principles, 12 Decisions, 1 Reified Override Assertion.



## 2. Principles Register

| ID | Statement | Rationale | Scope | Testable? |
| --- | --- | --- | --- | --- |
| AP0001 | We will always start with a deployment monolith before cutting services out.

| Avoid premature microservice split before understanding load.

| deployment shape

| **yes** |
| AP0002 | For communication between Bounded Contexts asynchronous communication is preferred.

| Guarantees loose coupling between Bounded Contexts.

| cross-context calls

| **yes** |
| AP0003 | For design of our functional architecture, we will apply the principles of DDD.

| Avoid unaligned functional architecture.

| functional design

| **yes** |
| AP0004 | For design of our technical architecture, we will apply the principles of Residuality Theory.

| Stressing architecture creates sustaining systems.

| technical design

| **yes** |
| AP0005a | We can test our system by 95% automatically.

| Manual tests are too slow.

| testing approach

| **yes** |
| AP0005b | The Bounded Context publish their own UI as micro-UIs.

| Teams developing Bounded Contexts must be as independent as possible.

| UI ownership & team independence

| **yes** |
| AP0006 | UI communication is done synchronously.

| Humans expect immediate answers.

| UI communication

| **yes** |
| AP0007 | The system can be monitored to identify system-critical states reliable and fast.

| Operation must be almost completely automated.

| operations

| **yes** |
| AP0008 | Domain artifacts are controlled by a fine-grained access control.

| Respect personal and author rights of users.

| access control

| **yes** |

## 3. Decision Ledger

| ID | Title | Status | Date | Decision | Supersedes | Source |
| --- | --- | --- | --- | --- | --- | --- |
| ADR0001 | Using Monolith | Adopted | 2026-09-10 | Implement Larder as Monolith.

| — | full-form

|
| ADR0002 | Using One Database Instance | Adopted | 2026-09-10 | Use one database instance with schema per Bounded Context.

| — | full-form

|
| ADR0003 | Using Asynchronous Communication | Adopted | 2026-09-10 | Use asynchronous communication via RabbitMQ.

| — | full-form

|
| ADR0004 | Synchronous Communication per UI | Adopted | 2026-09-10 | Use synchronous communication via REST API for UIs.

| — | full-form

|
| ADR0005 | Orchestrator UI | Adopted | 2026-09-10 | Use an orchestrator/AppShell for UI composition.

| — | full-form

|
| SADR0001 | Chef exclusive access | Adopted | 2026-09-07 | Chef needs to be accessed exclusively.

| — | small log

|
| SADR0002 | Meal Plan | Superseded | 2026-09-08 | Meal plan is used for Menu and Meal plan.

| — | small log

|
| SADR0003 | Menu | Adopted | 2026-09-09 | Menu is used for Menu and Meal plan.

| SADR0002

| small log

|
| SADR0004 | Help | Adopted | 2026-09-11 | Help response / Help request terminology split.

| — | small log

|
| SADR0005 | Visual Glossary Legend | Adopted | 2026-09-11 | Each Visual Glossary needs a legend.

| — | small log

|
| SADR0006 | Brainstorming Legend | Adopted | 2026-09-11 | Each Brainstorming result needs a legend.

| — | small log

|
| SADR0007 | Capability Map Legend | Adopted | 2026-09-11 | The Capability Map needs a legend.

| — | small log

|

## 4. Compliance Matrix

| Decision \ Principle | AP0001 | AP0002 | AP0003 | AP0004 | AP0005a | AP0005b | AP0006 | AP0007 | AP0008 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| **ADR0001** | ✓ (explicit)

|  | ✓ (incidental)

|  |  |  |  |  |  |
| **ADR0002** | ✓ (explicit)

|  |  |  |  |  |  |  |  |
| **ADR0003** |  | ✓ (explicit)

| ✓ (incidental)

|  |  |  |  |  |  |
| **ADR0004** |  |  |  |  |  |  | ✓ (explicit)

|  | ✓ (elimination)

|
| **ADR0005** |  |  | ✓ (incidental)

|  |  | **✗ (unack)**<br> | ✓ (incidental)

|  |  |

*Note: Silent and non-applicable pairs get no entry per section §4c rules.*

## 5. Conflicts, in full

**ADR0005 vs AP0005b (Micro-UIs) — Unacknowledged Override.**

> **Principle AP0005b**: *"The teams developing the Bounded Contexts must be as independent as possible."*
>
> **ADR0005 Decision & Consequences**: *"We will use an orchestrator for the UI... Consequences: All teams depend on AppShell team."*
>

The decision introduces centralized team coupling to an AppShell team, directly breaking AP0005b's core mandate of team independence. The ADR never mentions AP0005b nor justifies the trade-off as an explicit exception.

## 6. Governance gaps

* **Unacknowledged Principle Override**: ADR0005 introduces team coupling without acknowledging or citing AP0005b.


* **Dead-Letter Principles**: AP0004 (Residuality), AP0005a (Automatic Testing), and AP0007 (Monitoring) are never cited, honored, or overridden by any active decision.


* **Decision Citing No Principle**: ADR0005 evaluates options without citing any governing principle in its rationale or drivers.


* **Source Document Defects**:
1. `LarderArchitecturalPrinciples.md` reuses ID `AP0005` for both *Automatic Testing* and *Micro-UIs*.


2. `AP0007` (Monitoring) has copy-pasted implications text from `AP0005` (*"Using a large monolithic frontend would us slow down."*).


3. `ADR0001` decision line specifies *"Monolith"*, but its options table states plain Monolith contradicts AP0001 while favoring Modular Monolith.


4. `ADR0002` consequences table column headers were copied verbatim from ADR0001.





## 7. What newly connects

* **SADR0002 & SADR0003 Supersession**: SADR0003 replaces SADR0002 cleanly in the glossary ledger.


* **Unlinked UI Aggregation Question**: ADR0005 introduces live cross-context UI aggregation across Recipe Catalog, Meal Preparation, and Cooking Assistance. This creates an unlinked open question regarding whether backend data fetching for the AppShell is done via asynchronous read models (AP0002) or synchronous aggregate calls (AP0006).