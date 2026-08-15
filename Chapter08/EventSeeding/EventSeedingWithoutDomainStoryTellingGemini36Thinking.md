# Prompt 2026-08-15

Create a couple of events as start points of an EventStorming session for 'Asking for help' in a meal preparation session. Use the provided skill. 

# Answer

# Seed domain events — Asking for help during meal preparation

## What I understood

Modeling the process of seeking and receiving help during a meal preparation session.
*Trigger:* A cook encounters a bottleneck, task overload, or physical constraint during prep.
*Outcome:* Assistance is completed or declined, and prep work continues.
*Altitude:* Fine — capturing step-by-step facts within a single request-for-help workflow.

## Events

1. Prep bottleneck identified


2. Assistance requested


3. Helper notified


4. Help accepted


5. Prep task delegated


6. Task assistance completed


7. Meal prep resumed



## Alternatives & failures

* Assistance declined *(branches from 3 — rejection)*

* Help request timed out *(branches from 3 — time passing)*

* Task clarification requested *(branches from 4 — human judgement)*

* Task assistance failed *(branches from 6 — failure)*

* Help request cancelled by cook *(branches from 2 — reversal)*

* Additional helper requested *(branches from 5 — threshold reached)*


## Made up

* Separated *Assistance requested* from *Helper notified* to account for broadcast or delayed messaging.


* Included *Prep task delegated* as a discrete step prior to execution.


* Inferred failure and clarification paths (*Task clarification requested*, *Task assistance failed*).



## Questions

* Is the request broadcast to an open group or directed at a specific individual?


* Does requesting help pause active cooking timers or critical prep steps?


* What occurs if a helper accepts but lacks the skill required for the specific task?