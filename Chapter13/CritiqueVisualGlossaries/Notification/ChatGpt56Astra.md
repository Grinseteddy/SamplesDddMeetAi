# Prompt 2026-10-04

/devils-advocate
Critique Visual Glossary Notification

![](../../../Chapter12/assets/ApiProductCanvas/NotificationsNotificationsVisualGlossary.jpg)

# Answer

**The strongest objection: the glossary allows multiple receivers but gives the notification one shared read status.** If Alice reads a notification and Bob hasn’t, is it `READ` or `NEW`? As drawn, it cannot represent both experiences.

I could read the attached image. Here are the challenges I’d raise:

| Challenge | Why it matters | What would resolve it |
|---|---|---|
| **Read status belongs to whom?** `Notification` contains `1..* Receiver` and exactly one `status`. | One person reading could make a notification appear read for everyone. | Either define one notification per recipient, or place read status on a recipient-specific record. |
| **“Receiver” conflates an individual and an audience.** Its example is a UUID, while the invariant says “whole community or Chef.” | A community identifier and a person identifier imply different recipient resolution rules. Membership can also change. | Distinguish the intended audience from resolved recipients. Define whether community membership is evaluated when the notification is created or later. |
| **The invariant is not precise enough to enforce.** “Help requests go to whole community or Chef.” | What selects the branch? Is “or” exclusive? Which community and which chef? | Express the actual condition: “When …, notify ….” Include what happens when there is no eligible recipient. |
| **The example may describe a different event from the invariant.** The title says help was “provided”; the rule talks about help requests. | A request for help and confirmation that help was provided may have different audiences and actions. | Give separate examples for those events, each with its intended recipients and destination. |
| **A link is mandatory for every notification.** | This assumes every notification has an actionable destination, including after the linked request disappears or access changes. | Confirm that requirement with examples; otherwise use `0..1`. Define what the recipient sees when the target is unavailable. |

**The aggregate boundary also needs a reason.** What business rule requires all recipients and their state to change together? If reading is independent per person, that is a reason to reconsider placing all recipient state inside one aggregate.

My preferred starting point—if these are personal inbox notifications—is **one notification per recipient**, with its own `NEW/READ` state. If shared content needs a separate identity, a shared notification plus recipient-specific records is another option.

Before revising the drawing, test one scenario: **a help request reaches two community members; one reads it, the other leaves the community, and help is then provided.** Can the glossary explain who sees what, which status changes, and who gets the follow-up? Resolving that would address the main modeling uncertainty.