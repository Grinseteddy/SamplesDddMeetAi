# Cooking Assistance domain model

Framework-free Java 17 model derived from HelpNew.jpg and HelpRequestNew.jpg.
Read MODEL-DECISIONS.md before adopting the provisional business rules.

## Build and verify

With a Java 17+ JDK:

```sh
./verify.sh
```

The script compiles production and test sources with warnings treated as errors,
then runs dependency-free smoke tests. Public value types are nested in `Ids`,
`Values` and `Answer`; the two aggregate roots are `HelpRequest` and `Help`.

## Example

```java
import cookingassistance.domain.*;
import cookingassistance.domain.Ids.*;
import cookingassistance.domain.Values.*;
import java.util.*;

var request = new HelpRequest(
    new HelpRequestId(UUID.randomUUID()), new RequesterId(UUID.randomUUID()),
    "Dinner ideas", Type.MENU_PROPOSAL, "Dinner for six",
    Optional.empty(), Optional.empty(), Set.of(), Set.of(Provider.CHEF), List.of());
var proposal = new Answer.MenuProposal("Prepare ahead", 6, Meal.DINNER,
    Optional.empty(), List.of(new Answer.Course(1, new RecipeId(UUID.randomUUID()))));
var help = Help.answer(new HelpId(UUID.randomUUID()), request, "A dinner menu",
    Provider.CHEF, Optional.empty(), proposal);
new CookingAssistance().accept(request, help);
// Persist both roots in one transaction, with concurrency control.
```

No persistence implementation, API, authentication layer or undocumented Closed
transition is included. Ten open questions and the deliberate interpretation of
inner entity-colored nodes as immutable values are recorded in MODEL-DECISIONS.md.
