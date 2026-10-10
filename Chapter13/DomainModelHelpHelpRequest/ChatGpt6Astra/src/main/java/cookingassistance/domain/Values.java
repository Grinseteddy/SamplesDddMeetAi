package cookingassistance.domain;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.Set;
public final class Values {
    private Values() {}
    public enum Type { INGREDIENT_SUBSTITUTE, PREPARATION_STEP_EXPLANATION, STEPS_TO_MITIGATE_CATASTROPHE, MENU_PROPOSAL }
    public enum Provider { CHEF, GRANDMA_AVATAR, COMMUNITY }
    public enum Status { OPEN, ANSWERED, CLOSED }
    public enum Meal { BREAKFAST, LUNCH, DINNER, SUPPER }
    public enum Unit { PIECE, CUP, TABLE_SPOON, TEA_SPOON, FLUID_OUNCES, PINT, QUART, POUND, KILOGRAM, GRAM, LITER, MILLILITER, PINCH }
    // Presence only: no unsupported format/range restrictions on glossary leaves.
    public record SubstituteIngredient(String name, BigDecimal value, Unit unit) {
        public SubstituteIngredient { DomainViolation.required(name,"Name"); DomainViolation.required(value,"Value"); DomainViolation.required(unit,"Unit"); }
    }
    static <T> List<T> list(List<T> values, String name) {
        DomainViolation.required(values,name);
        DomainViolation.require(values.stream().noneMatch(v -> v == null), name+"MustNotContainNull");
        return List.copyOf(values);
    }
    static <T> Set<T> set(Set<T> values, String name) {
        DomainViolation.required(values,name);
        DomainViolation.require(values.stream().noneMatch(v -> v == null), name+"MustNotContainNull");
        return Set.copyOf(values);
    }
}
