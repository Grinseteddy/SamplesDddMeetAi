package cookingassistance.domain;

import java.util.UUID;
public final class Ids {
 private Ids() {}
 public record HelpId(UUID value) { public HelpId { DomainViolation.required(value, "HelpId"); } }
 public record HelpRequestId(UUID value) { public HelpRequestId { DomainViolation.required(value, "HelpRequestId"); } }
 public record RequesterId(UUID value) { public RequesterId { DomainViolation.required(value, "RequesterId"); } }
 public record HelpProviderId(UUID value) { public HelpProviderId { DomainViolation.required(value, "HelpProviderId"); } }
 public record RecipeId(UUID value) { public RecipeId { DomainViolation.required(value, "RecipeId"); } }
 public record HowToStepId(UUID value) { public HowToStepId { DomainViolation.required(value, "HowToStepId"); } }
 public record IngredientId(UUID value) { public IngredientId { DomainViolation.required(value, "IngredientId"); } }
}
