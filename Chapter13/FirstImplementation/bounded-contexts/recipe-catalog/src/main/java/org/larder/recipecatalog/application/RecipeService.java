package org.larder.recipecatalog.application;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;

import org.larder.recipecatalog.domain.CookId;
import org.larder.recipecatalog.domain.HowToStep;
import org.larder.recipecatalog.domain.HowToStepDraft;
import org.larder.recipecatalog.domain.HowToStepId;
import org.larder.recipecatalog.domain.Ingredient;
import org.larder.recipecatalog.domain.IngredientDraft;
import org.larder.recipecatalog.domain.IngredientId;
import org.larder.recipecatalog.domain.Meal;
import org.larder.recipecatalog.domain.Quantity;
import org.larder.recipecatalog.domain.Recipe;
import org.larder.recipecatalog.domain.RecipeDraft;
import org.larder.recipecatalog.domain.RecipeId;
import org.larder.recipecatalog.domain.RecipeRevision;
import org.larder.recipecatalog.domain.RecipeSearch;
import org.larder.recipecatalog.domain.Unit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use cases of the Recipe Catalog. Cooks create and maintain their own recipes - only the owner
 * changes or deletes a recipe, its ingredients, how-to steps and meal (the contract gives no other
 * scope, not even {@code recipe:admin}, the right to change somebody else's recipe).
 * Every authenticated caller searches and reads the catalog.
 */
@Service
public class RecipeService {

    /** Transaction manager of this context's own schema. */
    public static final String TRANSACTIONS = "recipecatalogTransactionManager";

    private final RecipeRepository recipes;

    public RecipeService(RecipeRepository recipes) {
        this.recipes = recipes;
    }

    // --- recipes -------------------------------------------------------------------------------

    @Transactional(transactionManager = RecipeService.TRANSACTIONS)
    public Recipe create(CookId caller, RecipeDraft draft) {
        Recipe recipe = Recipe.create(caller, draft);
        recipes.save(recipe);
        return recipe;
    }

    @Transactional(transactionManager = RecipeService.TRANSACTIONS)
    public Recipe revise(CookId caller, RecipeId id, RecipeRevision revision) {
        Recipe recipe = ownRecipe(caller, id);
        recipe.revise(revision);
        recipes.save(recipe);
        return recipe;
    }

    @Transactional(transactionManager = RecipeService.TRANSACTIONS)
    public void delete(CookId caller, RecipeId id) {
        ownRecipe(caller, id);
        recipes.delete(id);
    }

    public Recipe recipe(RecipeId id) {
        return recipes.findById(id).orElseThrow(() -> new NotFoundException("Recipe " + id.value() + " not found"));
    }

    public List<Recipe> search(RecipeSearch search) {
        return recipes.search(search);
    }

    // --- meal ----------------------------------------------------------------------------------

    @Transactional(transactionManager = RecipeService.TRANSACTIONS)
    public Recipe assignMeal(CookId caller, RecipeId id, Meal meal) {
        Recipe recipe = ownRecipe(caller, id);
        recipe.assignTo(meal);
        recipes.save(recipe);
        return recipe;
    }

    // --- ingredients ---------------------------------------------------------------------------

    @Transactional(transactionManager = RecipeService.TRANSACTIONS)
    public Ingredient addIngredient(CookId caller, RecipeId id, IngredientDraft draft) {
        Recipe recipe = ownRecipe(caller, id);
        Ingredient ingredient = recipe.addIngredient(draft);
        recipes.save(recipe);
        return ingredient;
    }

    /** {@code null} leaves name, value or unit as they are. */
    @Transactional(transactionManager = RecipeService.TRANSACTIONS)
    public Ingredient changeIngredient(CookId caller, RecipeId id, IngredientId ingredientId,
                                       String name, BigDecimal value, Unit unit) {
        Recipe recipe = ownRecipe(caller, id);
        Ingredient current = existingIngredient(recipe, ingredientId);
        Quantity quantity = value == null && unit == null ? null : new Quantity(
                value == null ? current.quantity().value() : value,
                unit == null ? current.quantity().unit() : unit);
        Ingredient ingredient = recipe.changeIngredient(ingredientId, name, quantity);
        recipes.save(recipe);
        return ingredient;
    }

    @Transactional(transactionManager = RecipeService.TRANSACTIONS)
    public void removeIngredient(CookId caller, RecipeId id, IngredientId ingredientId) {
        Recipe recipe = ownRecipe(caller, id);
        existingIngredient(recipe, ingredientId);
        recipe.removeIngredient(ingredientId);
        recipes.save(recipe);
    }

    public Ingredient ingredient(RecipeId id, IngredientId ingredientId) {
        return existingIngredient(recipe(id), ingredientId);
    }

    // --- how-to steps --------------------------------------------------------------------------

    @Transactional(transactionManager = RecipeService.TRANSACTIONS)
    public HowToStep addHowToStep(CookId caller, RecipeId id, HowToStepDraft draft) {
        Recipe recipe = ownRecipe(caller, id);
        HowToStep step = recipe.addHowToStep(draft);
        recipes.save(recipe);
        return step;
    }

    /** {@code null} leaves sequence number, description or illustration as they are. */
    @Transactional(transactionManager = RecipeService.TRANSACTIONS)
    public HowToStep changeHowToStep(CookId caller, RecipeId id, HowToStepId howToStepId,
                                     Integer sequenceNumber, String description, URI illustration) {
        Recipe recipe = ownRecipe(caller, id);
        existingHowToStep(recipe, howToStepId);
        HowToStep step = recipe.changeHowToStep(howToStepId, sequenceNumber, description, illustration);
        recipes.save(recipe);
        return step;
    }

    @Transactional(transactionManager = RecipeService.TRANSACTIONS)
    public void removeHowToStep(CookId caller, RecipeId id, HowToStepId howToStepId) {
        Recipe recipe = ownRecipe(caller, id);
        existingHowToStep(recipe, howToStepId);
        recipe.removeHowToStep(howToStepId);
        recipes.save(recipe);
    }

    public HowToStep howToStep(RecipeId id, HowToStepId howToStepId) {
        return existingHowToStep(recipe(id), howToStepId);
    }

    // --- helpers -------------------------------------------------------------------------------

    /** Unknown recipes are 404 before ownership is checked (403). */
    private Recipe ownRecipe(CookId caller, RecipeId id) {
        Recipe recipe = recipe(id);
        if (!recipe.isOwnedBy(caller)) {
            throw new NotPermittedException("Only the owner of recipe " + id.value() + " can change it");
        }
        return recipe;
    }

    private static Ingredient existingIngredient(Recipe recipe, IngredientId ingredientId) {
        return recipe.ingredient(ingredientId).orElseThrow(() -> new NotFoundException(
                "Ingredient " + ingredientId.value() + " not found in recipe " + recipe.id().value()));
    }

    private static HowToStep existingHowToStep(Recipe recipe, HowToStepId howToStepId) {
        return recipe.howToStep(howToStepId).orElseThrow(() -> new NotFoundException(
                "How-to step " + howToStepId.value() + " not found in recipe " + recipe.id().value()));
    }
}
