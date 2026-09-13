# Prompt 2026-09-13

Decide if Larder should use a choreography or orchestrator approach for UI.

[Domain Knowledge Graph](../../assets/larderEnahnced4.ttl)

# Answer

**Use an orchestrator for Larder’s UI journeys.** A small journey coordinator should track the current step, issue the next user action, and show pending, success, and recovery states. The graph’s cooking flow crosses Meal Planning, Cooking Assistance, Meal Preparation, Media, and Sharing; leaving each UI component to react independently to events would make that visible sequence hard to manage.

Keep the coordinator focused on **user experience, not domain authority**. Each context should own its commands and rules. For example, Meal Preparation says the cook must confirm a rescue after receiving advice, while Sharing consults Consent Management before showing pictures; the UI should wait for those outcomes and reflect them, not decide them itself.

Use choreography for independent follow-on work such as notifications, whose canvas already describes consuming help-request and help-response events. This is a **UI architecture recommendation inferred from the graph**, not a decision the artifacts have already made. The unresolved help-context boundary and missing preparation end states should be settled when specifying the coordinator’s states.   
