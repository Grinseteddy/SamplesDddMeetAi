package org.larder.recipecatalog.domain;

import java.util.List;
import java.util.Optional;

/**
 * Search criteria of the catalog. All criteria are optional and combined with AND; without any
 * criteria every recipe matches.
 *
 * <ul>
 *   <li>{@code meal} and {@code diet} match the recipe's meal and diet exactly.</li>
 *   <li>{@code ingredientNames}: the recipe contains an ingredient of every one of these names
 *       (whole name, ignoring case and surrounding blanks).</li>
 * </ul>
 */
public record RecipeSearch(Meal meal, Diet diet, List<String> ingredientNames) {

    public RecipeSearch {
        ingredientNames = ingredientNames == null ? List.of()
                : ingredientNames.stream().map(String::trim).filter(name -> !name.isEmpty()).distinct().toList();
    }

    public static RecipeSearch all() {
        return new RecipeSearch(null, null, List.of());
    }

    public Optional<Meal> mealFilter() {
        return Optional.ofNullable(meal);
    }

    public Optional<Diet> dietFilter() {
        return Optional.ofNullable(diet);
    }

    public boolean isSatisfiedBy(Recipe recipe) {
        return (meal == null || recipe.meal() == meal)
                && (diet == null || recipe.diet() == diet)
                && ingredientNames.stream().allMatch(recipe::contains);
    }
}
