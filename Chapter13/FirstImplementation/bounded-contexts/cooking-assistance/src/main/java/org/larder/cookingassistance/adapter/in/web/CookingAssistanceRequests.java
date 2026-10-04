package org.larder.cookingassistance.adapter.in.web;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.larder.cookingassistance.adapter.in.web.model.HelpRequestCreate;
import org.larder.cookingassistance.adapter.in.web.model.HelpRequestUpdate;
import org.larder.cookingassistance.domain.Answer;
import org.larder.cookingassistance.domain.CatastropheMitigation;
import org.larder.cookingassistance.domain.CookId;
import org.larder.cookingassistance.domain.Course;
import org.larder.cookingassistance.domain.HelpProviderType;
import org.larder.cookingassistance.domain.HelpRequestDraft;
import org.larder.cookingassistance.domain.HelpRequestRevision;
import org.larder.cookingassistance.domain.HelpRequestStatus;
import org.larder.cookingassistance.domain.HelpType;
import org.larder.cookingassistance.domain.HowToStepId;
import org.larder.cookingassistance.domain.IngredientId;
import org.larder.cookingassistance.domain.IngredientSubstitutes;
import org.larder.cookingassistance.domain.Meal;
import org.larder.cookingassistance.domain.MenuProposal;
import org.larder.cookingassistance.domain.PreparationStepExplanation;
import org.larder.cookingassistance.domain.RecipeId;
import org.larder.cookingassistance.domain.Substitute;
import org.larder.cookingassistance.domain.Unit;
import org.larder.platform.security.CurrentCook;
import org.springframework.http.converter.HttpMessageNotReadableException;

/**
 * Translates the contract's request bodies into the domain's drafts, revisions and answers. Bean
 * validation of the generated model has already checked the contract's constraints; the domain checks
 * the rest (type-dependent rules, the stricter limits of the published language).
 */
final class CookingAssistanceRequests {

    private CookingAssistanceRequests() {
    }

    static CookId caller() {
        return new CookId(CurrentCook.require().value());
    }

    /** The generated interfaces declare every body optional; the contract does not. */
    static <T> T body(T body) {
        if (body == null) {
            throw new HttpMessageNotReadableException("Required request body is missing", null, null);
        }
        return body;
    }

    static HelpRequestDraft toDraft(HelpRequestCreate request) {
        return new HelpRequestDraft(
                request.getTitle(),
                toDomain(request.getType()),
                request.getDescription(),
                request.getRecipe() == null ? null : new RecipeId(request.getRecipe()),
                request.getHowToStep() == null ? null : new HowToStepId(request.getHowToStep()),
                request.getIngredients() == null ? Set.of() : ingredients(request.getIngredients()),
                request.getPreferredProvider() == null ? Set.of() : providers(request.getPreferredProvider()));
    }

    /**
     * Absent (or {@code null}) properties leave the request as it is. The generated model cannot tell an
     * absent {@code recipe}/{@code howToStep} from an explicit {@code null}, nor absent collections from
     * empty ones - so neither {@code null} nor an empty list removes anything.
     */
    static HelpRequestRevision toRevision(HelpRequestUpdate request) {
        return new HelpRequestRevision(
                request.getTitle(),
                request.getDescription(),
                request.getRecipe() == null ? null : new RecipeId(request.getRecipe()),
                request.getHowToStep() == null ? null : new HowToStepId(request.getHowToStep()),
                request.getIngredients() == null || request.getIngredients().isEmpty()
                        ? null : ingredients(request.getIngredients()),
                request.getPreferredProvider() == null || request.getPreferredProvider().isEmpty()
                        ? null : providers(request.getPreferredProvider()),
                request.getStatus() == null ? null : HelpRequestStatus.valueOf(request.getStatus().name()));
    }

    static Answer toDomain(org.larder.cookingassistance.adapter.in.web.model.Answer answer) {
        return switch (answer) {
            case null -> null;
            case org.larder.cookingassistance.adapter.in.web.model.Substitutes s -> new IngredientSubstitutes(
                    recipe(s.getRecipe()),
                    s.getSubstitute() == null ? null : s.getSubstitute().stream().map(substitute -> new Substitute(
                            new IngredientId(substitute.getIngredient()),
                            substitute.getSubstituteIngredient().getName(),
                            substitute.getSubstituteIngredient().getValue(),
                            Unit.valueOf(substitute.getSubstituteIngredient().getUnit().name()))).toList());
            case org.larder.cookingassistance.adapter.in.web.model.PreparationStepExplanation p ->
                    new PreparationStepExplanation(recipe(p.getRecipe()),
                            p.getHowToStep() == null ? null : new HowToStepId(p.getHowToStep()),
                            p.getDescription(), p.getImages());
            case org.larder.cookingassistance.adapter.in.web.model.CatastropheMitigation c ->
                    new CatastropheMitigation(Optional.ofNullable(c.getRecipe()).map(RecipeId::new), c.getExplanation());
            case org.larder.cookingassistance.adapter.in.web.model.MenuProposal m -> new MenuProposal(
                    m.getNote(),
                    m.getServings(),
                    Meal.valueOf(m.getMeal().name()),
                    Optional.ofNullable(m.getHowToServe()),
                    m.getCourse().stream()
                            .map(course -> new Course(course.getStep(), new RecipeId(course.getMeal().getRecipe())))
                            .toList());
            default -> throw new IllegalStateException("Unknown answer kind " + answer.getClass().getSimpleName());
        };
    }

    static HelpType toDomain(org.larder.cookingassistance.adapter.in.web.model.HelpType type) {
        return type == null ? null : HelpType.valueOf(type.name());
    }

    static HelpProviderType toDomain(org.larder.cookingassistance.adapter.in.web.model.HelpProviderType type) {
        return type == null ? null : HelpProviderType.valueOf(type.name());
    }

    private static RecipeId recipe(UUID recipe) {
        return recipe == null ? null : new RecipeId(recipe);
    }

    private static Set<IngredientId> ingredients(Set<UUID> ingredients) {
        Set<IngredientId> result = new LinkedHashSet<>();
        ingredients.forEach(id -> result.add(new IngredientId(id)));
        return result;
    }

    private static Set<HelpProviderType> providers(
            Set<org.larder.cookingassistance.adapter.in.web.model.HelpProviderType> providers) {
        Set<HelpProviderType> result = new LinkedHashSet<>();
        providers.forEach(provider -> result.add(toDomain(provider)));
        return result;
    }
}
