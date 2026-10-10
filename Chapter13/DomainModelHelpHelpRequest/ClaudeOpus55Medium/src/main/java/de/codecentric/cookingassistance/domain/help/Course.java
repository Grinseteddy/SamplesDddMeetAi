package de.codecentric.cookingassistance.domain.help;

import de.codecentric.cookingassistance.domain.external.RecipeId;
import de.codecentric.cookingassistance.domain.shared.DomainRuleViolation;
import de.codecentric.cookingassistance.domain.shared.Require;

/**
 * Glossary term "course" of a menu proposal.
 *
 * @param dish   "contains 1 dish" — example "2", read as the serving step the
 *               course belongs to. Marked "!" on the glossary, and
 *               "different courses can have the same step when served in
 *               parallel", so it is NOT unique within a menu.
 * @param recipe "contains 1 recipe"
 */
public record Course(int dish, RecipeId recipe) {

    public Course {
        if (dish < 1) {
            throw new DishNotPositive(dish);
        }
        Require.present(recipe, "recipe");
    }

    public static final class DishNotPositive extends DomainRuleViolation {
        public DishNotPositive(int dish) {
            super("a course's dish (serving step) starts at 1, not " + dish);
        }
    }
}
