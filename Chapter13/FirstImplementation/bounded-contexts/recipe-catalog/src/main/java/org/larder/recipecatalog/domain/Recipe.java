package org.larder.recipecatalog.domain;

import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * A recipe in the catalog, owned by one cook (aggregate root). It owns its ingredients and
 * how-to steps; every change goes through the recipe, which keeps these rules:
 *
 * <ul>
 *   <li>The owner is the cook who created the recipe and never changes; only the owner changes or deletes it.</li>
 *   <li>A recipe has a name (1-200 characters), an optional subtitle (at most 300 characters),
 *       an optional main image and at most ten further images.</li>
 *   <li>A recipe yields at least one serving and has a preparation time {@code HH:MM}.</li>
 *   <li>A recipe is intended for exactly one meal and suitable for exactly one diet.</li>
 *   <li>A recipe has at least one ingredient; each has a name and a positive value in a unit.</li>
 *   <li>A recipe has at least one how-to step; sequence numbers start at 1 and are unique within the recipe.</li>
 * </ul>
 *
 * Every change is validated completely before anything is changed, so a rejected change leaves the recipe as it was.
 */
public final class Recipe {

    static final int MAX_NAME_LENGTH = 200;
    static final int MAX_SUBTITLE_LENGTH = 300;
    static final int MAX_FURTHER_IMAGES = 10;

    private final RecipeId id;
    private final CookId owner;
    private String name;
    private String subtitle;
    private URI mainImage;
    private List<URI> furtherImages;
    private PreparationTime preparationTime;
    private int servings;
    private Meal meal;
    private Diet diet;
    private final List<Ingredient> ingredients;
    private final List<HowToStep> howToSteps;

    private Recipe(RecipeId id, CookId owner, String name, String subtitle, URI mainImage, List<URI> furtherImages,
                   PreparationTime preparationTime, int servings, Meal meal, Diet diet,
                   List<Ingredient> ingredients, List<HowToStep> howToSteps) {
        this.id = Objects.requireNonNull(id);
        this.owner = Objects.requireNonNull(owner);
        this.name = requireName(name);
        this.subtitle = requireSubtitle(subtitle);
        this.mainImage = mainImage;
        this.furtherImages = requireFurtherImages(furtherImages);
        this.preparationTime = requirePresent(preparationTime, "A recipe needs a preparation time");
        this.servings = requireServings(servings);
        this.meal = requirePresent(meal, "A recipe is intended for a meal");
        this.diet = requirePresent(diet, "A recipe is suitable for a diet");
        if (ingredients.isEmpty()) {
            throw new RecipeRuleViolationException(RecipeRuleViolationException.RECIPE_NEEDS_INGREDIENT,
                    "A recipe needs at least one ingredient");
        }
        if (howToSteps.isEmpty()) {
            throw new RecipeRuleViolationException(RecipeRuleViolationException.RECIPE_NEEDS_HOW_TO_STEP,
                    "A recipe needs at least one how-to step");
        }
        var sequenceNumbers = new HashSet<Integer>();
        for (HowToStep step : howToSteps) {
            if (!sequenceNumbers.add(step.sequenceNumber())) {
                throw sequenceNumberTaken(step.sequenceNumber());
            }
        }
        this.ingredients = new ArrayList<>(ingredients);
        this.howToSteps = new ArrayList<>(howToSteps);
    }

    /** A cook creates a recipe of their own, with at least one ingredient and one how-to step. */
    public static Recipe create(CookId owner, RecipeDraft draft) {
        Objects.requireNonNull(draft, "A recipe needs its data");
        return new Recipe(RecipeId.newId(), owner, draft.name(), draft.subtitle(), draft.mainImage(), draft.furtherImages(),
                draft.preparationTime(), draft.servings(), draft.meal(), draft.diet(),
                draft.ingredients().stream().map(Ingredient::create).toList(),
                draft.howToSteps().stream().map(HowToStep::create).toList());
    }

    /** Recreates a stored recipe. */
    public static Recipe restore(RecipeId id, CookId owner, String name, String subtitle, URI mainImage,
                                 List<URI> furtherImages, PreparationTime preparationTime, int servings, Meal meal,
                                 Diet diet, List<Ingredient> ingredients, List<HowToStep> howToSteps) {
        return new Recipe(id, owner, name, subtitle, mainImage, furtherImages, preparationTime, servings, meal, diet,
                ingredients, howToSteps);
    }

    public boolean isOwnedBy(CookId candidate) {
        return owner.equals(candidate);
    }

    // --- the recipe's own details ---------------------------------------------------------------

    /** Applies the given parts of the revision; {@code null} parts stay as they are. */
    public void revise(RecipeRevision revision) {
        String newName = revision.name() == null ? name : requireName(revision.name());
        String newSubtitle = revision.subtitle() == null ? subtitle : requireSubtitle(revision.subtitle());
        URI newMainImage = revision.mainImage() == null ? mainImage : revision.mainImage();
        List<URI> newFurtherImages = revision.furtherImages() == null ? furtherImages
                : requireFurtherImages(revision.furtherImages());
        PreparationTime newPreparationTime = revision.preparationTime() == null ? preparationTime : revision.preparationTime();
        int newServings = revision.servings() == null ? servings : requireServings(revision.servings());
        Diet newDiet = revision.diet() == null ? diet : revision.diet();

        name = newName;
        subtitle = newSubtitle;
        mainImage = newMainImage;
        furtherImages = newFurtherImages;
        preparationTime = newPreparationTime;
        servings = newServings;
        diet = newDiet;
    }

    /** Replaces the meal the recipe is intended for. */
    public void assignTo(Meal newMeal) {
        meal = requirePresent(newMeal, "A recipe is intended for a meal");
    }

    // --- ingredients ---------------------------------------------------------------------------

    public Ingredient addIngredient(IngredientDraft draft) {
        Ingredient ingredient = Ingredient.create(draft);
        ingredients.add(ingredient);
        return ingredient;
    }

    /** Changes name and/or quantity of one of the recipe's ingredients; {@code null} leaves the part as it is. */
    public Ingredient changeIngredient(IngredientId ingredientId, String newName, Quantity newQuantity) {
        Ingredient ingredient = requireIngredient(ingredientId);
        ingredient.change(newName, newQuantity);
        return ingredient;
    }

    public void removeIngredient(IngredientId ingredientId) {
        Ingredient ingredient = requireIngredient(ingredientId);
        if (ingredients.size() == 1) {
            throw new RecipeRuleViolationException(RecipeRuleViolationException.RECIPE_NEEDS_INGREDIENT,
                    "A recipe must keep at least one ingredient; the last one cannot be removed");
        }
        ingredients.remove(ingredient);
    }

    public Optional<Ingredient> ingredient(IngredientId ingredientId) {
        return ingredients.stream().filter(ingredient -> ingredient.id().equals(ingredientId)).findFirst();
    }

    /** Whether the recipe has an ingredient of this name (whole name, ignoring case). */
    public boolean contains(String ingredientName) {
        return ingredients.stream().anyMatch(ingredient -> ingredient.isCalled(ingredientName));
    }

    // --- how-to steps --------------------------------------------------------------------------

    public HowToStep addHowToStep(HowToStepDraft draft) {
        Objects.requireNonNull(draft, "A how-to step needs its data");
        requireFreeSequenceNumber(draft.sequenceNumber(), null);
        HowToStep step = HowToStep.create(draft);
        howToSteps.add(step);
        return step;
    }

    /** Changes one of the recipe's how-to steps; {@code null} leaves the part as it is. */
    public HowToStep changeHowToStep(HowToStepId howToStepId, Integer newSequenceNumber, String newDescription,
                                     URI newIllustration) {
        HowToStep step = requireHowToStep(howToStepId);
        if (newSequenceNumber != null) {
            requireFreeSequenceNumber(newSequenceNumber, step);
        }
        step.change(newSequenceNumber, newDescription, newIllustration);
        return step;
    }

    public void removeHowToStep(HowToStepId howToStepId) {
        HowToStep step = requireHowToStep(howToStepId);
        if (howToSteps.size() == 1) {
            throw new RecipeRuleViolationException(RecipeRuleViolationException.RECIPE_NEEDS_HOW_TO_STEP,
                    "A recipe must keep at least one how-to step; the last one cannot be removed");
        }
        howToSteps.remove(step);
    }

    public Optional<HowToStep> howToStep(HowToStepId howToStepId) {
        return howToSteps.stream().filter(step -> step.id().equals(howToStepId)).findFirst();
    }

    // --- rules ---------------------------------------------------------------------------------

    private void requireFreeSequenceNumber(int sequenceNumber, HowToStep changed) {
        HowToStep.requireSequenceNumber(sequenceNumber);
        boolean taken = howToSteps.stream()
                .anyMatch(step -> step != changed && step.sequenceNumber() == sequenceNumber);
        if (taken) {
            throw sequenceNumberTaken(sequenceNumber);
        }
    }

    private static RecipeRuleViolationException sequenceNumberTaken(int sequenceNumber) {
        return new RecipeRuleViolationException(RecipeRuleViolationException.SEQUENCE_NUMBER_TAKEN,
                "Sequence number " + sequenceNumber + " is already used by another how-to step of this recipe");
    }

    private Ingredient requireIngredient(IngredientId ingredientId) {
        return ingredient(ingredientId).orElseThrow(() -> new IllegalArgumentException(
                "Ingredient " + ingredientId.value() + " is not part of recipe " + id.value()));
    }

    private HowToStep requireHowToStep(HowToStepId howToStepId) {
        return howToStep(howToStepId).orElseThrow(() -> new IllegalArgumentException(
                "How-to step " + howToStepId.value() + " is not part of recipe " + id.value()));
    }

    private static String requireName(String name) {
        if (name == null || name.isBlank()) {
            throw RecipeRuleViolationException.invalid("A recipe needs a name");
        }
        if (name.length() > MAX_NAME_LENGTH) {
            throw RecipeRuleViolationException.invalid("The name of a recipe has at most " + MAX_NAME_LENGTH + " characters");
        }
        return name;
    }

    private static String requireSubtitle(String subtitle) {
        if (subtitle != null && subtitle.length() > MAX_SUBTITLE_LENGTH) {
            throw RecipeRuleViolationException.invalid(
                    "The subtitle of a recipe has at most " + MAX_SUBTITLE_LENGTH + " characters");
        }
        return subtitle;
    }

    private static List<URI> requireFurtherImages(List<URI> furtherImages) {
        List<URI> images = furtherImages == null ? List.of() : List.copyOf(furtherImages);
        if (images.size() > MAX_FURTHER_IMAGES) {
            throw RecipeRuleViolationException.invalid(
                    "A recipe carries at most " + MAX_FURTHER_IMAGES + " further images, not " + images.size());
        }
        return images;
    }

    private static int requireServings(int servings) {
        if (servings < 1) {
            throw RecipeRuleViolationException.invalid("A recipe yields at least one serving, not " + servings);
        }
        return servings;
    }

    private static <T> T requirePresent(T value, String message) {
        if (value == null) {
            throw RecipeRuleViolationException.invalid(message);
        }
        return value;
    }

    // --- state ---------------------------------------------------------------------------------

    public RecipeId id() {
        return id;
    }

    public CookId owner() {
        return owner;
    }

    public String name() {
        return name;
    }

    public Optional<String> subtitle() {
        return Optional.ofNullable(subtitle);
    }

    public Optional<URI> mainImage() {
        return Optional.ofNullable(mainImage);
    }

    public List<URI> furtherImages() {
        return furtherImages;
    }

    public PreparationTime preparationTime() {
        return preparationTime;
    }

    public int servings() {
        return servings;
    }

    public Meal meal() {
        return meal;
    }

    public Diet diet() {
        return diet;
    }

    /** The ingredients in the order they were added. */
    public List<Ingredient> ingredients() {
        return List.copyOf(ingredients);
    }

    /** The how-to steps in preparation order, i.e. by sequence number. */
    public List<HowToStep> howToSteps() {
        return howToSteps.stream().sorted(Comparator.comparingInt(HowToStep::sequenceNumber)).toList();
    }
}
