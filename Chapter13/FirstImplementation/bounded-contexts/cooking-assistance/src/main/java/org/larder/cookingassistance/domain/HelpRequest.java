package org.larder.cookingassistance.domain;

import static org.larder.cookingassistance.domain.HelpRuleViolationException.ANSWERED_WITHOUT_HELP;
import static org.larder.cookingassistance.domain.HelpRuleViolationException.ANSWER_REFERENCE_MISMATCH;
import static org.larder.cookingassistance.domain.HelpRuleViolationException.ANSWER_TYPE_MISMATCH;
import static org.larder.cookingassistance.domain.HelpRuleViolationException.CHEF_EXCLUSIVE;
import static org.larder.cookingassistance.domain.HelpRuleViolationException.CHEF_ONLY_FOR_MENU_PROPOSAL;
import static org.larder.cookingassistance.domain.HelpRuleViolationException.HELP_REQUEST_ANSWERED;
import static org.larder.cookingassistance.domain.HelpRuleViolationException.HELP_REQUEST_NOT_OPEN;
import static org.larder.cookingassistance.domain.HelpRuleViolationException.HOW_TO_STEP_NOT_ALLOWED;
import static org.larder.cookingassistance.domain.HelpRuleViolationException.HOW_TO_STEP_REQUIRED;
import static org.larder.cookingassistance.domain.HelpRuleViolationException.INGREDIENTS_NOT_ALLOWED;
import static org.larder.cookingassistance.domain.HelpRuleViolationException.INGREDIENTS_REQUIRED;
import static org.larder.cookingassistance.domain.HelpRuleViolationException.INVALID_HELP_PROVIDER;
import static org.larder.cookingassistance.domain.HelpRuleViolationException.INVALID_HELP_REQUEST;
import static org.larder.cookingassistance.domain.HelpRuleViolationException.OWN_CHEF_REQUEST;
import static org.larder.cookingassistance.domain.HelpRuleViolationException.PROVIDER_NOT_PREFERRED;
import static org.larder.cookingassistance.domain.HelpRuleViolationException.RECIPE_REQUIRED;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * A cook's request for help while planning or preparing a meal (aggregate root).
 *
 * <ul>
 *   <li>A recipe is mandatory for Preparation Step Explanation and Ingredient Substitute.</li>
 *   <li>A howToStep is only allowed - and then required - for Preparation Step Explanation.</li>
 *   <li>Ingredients are only allowed - and then at least one is required - for Ingredient Substitute.
 *       (The REST contract only forbids them elsewhere; the published HelpRequested message requires
 *       them, and this context must be able to publish every request it accepts.)</li>
 *   <li>1..2 preferred providers. Chef support only for Menu proposal, and exclusively.</li>
 *   <li>A request starts Open; it is Answered exactly while at least one help answers it. Giving a
 *       help answers it; withdrawing the last help opens it again.</li>
 *   <li>An answered request keeps what its helps refer to: recipe, step, ingredients and preferred
 *       providers can no longer change, and it cannot be set back to Open; title and description can.</li>
 *   <li>Only an open request can be withdrawn (deleted).</li>
 *   <li>A help must fit the request: same type, same recipe / step / ingredients, from one of the
 *       preferred providers; a cook may answer their own request, except a Chef request.</li>
 * </ul>
 */
public final class HelpRequest {

    static final int MAX_PREFERRED_PROVIDERS = 2;

    private final HelpRequestId id;
    private final CookId requester;
    private final HelpType type;
    private final Instant createdAt;
    private String title;
    private String description;
    private RecipeId recipe;
    private HowToStepId howToStep;
    private Set<IngredientId> ingredients;
    private Set<HelpProviderType> preferredProviders;
    private HelpRequestStatus status;
    private Instant updatedAt;

    private HelpRequest(HelpRequestId id, CookId requester, String title, HelpType type, String description,
                        RecipeId recipe, HowToStepId howToStep, Set<IngredientId> ingredients,
                        Set<HelpProviderType> preferredProviders, HelpRequestStatus status, Instant createdAt,
                        Instant updatedAt) {
        this.id = Objects.requireNonNull(id);
        this.requester = Objects.requireNonNull(requester);
        if (type == null) {
            throw new HelpRuleViolationException(INVALID_HELP_REQUEST, "A help request needs a type");
        }
        this.type = type;
        this.title = Texts.required(title, Texts.TITLE, INVALID_HELP_REQUEST, "The title of a help request");
        this.description = Texts.required(description, Texts.LONG_TEXT, INVALID_HELP_REQUEST,
                "The description of a help request");
        this.recipe = recipe;
        this.howToStep = howToStep;
        this.ingredients = copy(ingredients);
        this.preferredProviders = copy(preferredProviders);
        this.status = Objects.requireNonNull(status);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
        checkTypeRules(type, this.recipe, this.howToStep, this.ingredients);
        checkPreferredProviders(type, this.preferredProviders);
    }

    /** {@code requester} raises a new, open help request. */
    public static HelpRequest raise(CookId requester, HelpRequestDraft draft, Instant now) {
        return new HelpRequest(HelpRequestId.newId(), requester, draft.title(), draft.type(), draft.description(),
                draft.recipe(), draft.howToStep(), draft.ingredients(), draft.preferredProviders(),
                HelpRequestStatus.OPEN, now, now);
    }

    /** Recreates a stored help request. */
    public static HelpRequest restore(HelpRequestId id, CookId requester, String title, HelpType type, String description,
                                      RecipeId recipe, HowToStepId howToStep, Set<IngredientId> ingredients,
                                      Set<HelpProviderType> preferredProviders, HelpRequestStatus status,
                                      Instant createdAt, Instant updatedAt) {
        return new HelpRequest(id, requester, title, type, description, recipe, howToStep, ingredients,
                preferredProviders, status, createdAt, updatedAt);
    }

    /**
     * Applies a partial change; the type-dependent rules are checked against the resulting state.
     *
     * @param answeredByAHelp whether at least one help answers this request
     */
    public void revise(HelpRequestRevision revision, boolean answeredByAHelp, Instant now) {
        RecipeId newRecipe = revision.recipe() == null ? recipe : revision.recipe();
        HowToStepId newHowToStep = revision.howToStep() == null ? howToStep : revision.howToStep();
        Set<IngredientId> newIngredients = revision.ingredients() == null ? ingredients : copy(revision.ingredients());
        Set<HelpProviderType> newProviders = revision.preferredProviders() == null
                ? preferredProviders : copy(revision.preferredProviders());
        HelpRequestStatus newStatus = revision.status() == null ? status : revision.status();

        if (status == HelpRequestStatus.ANSWERED) {
            if (newStatus == HelpRequestStatus.OPEN) {
                throw new HelpRuleViolationException(HELP_REQUEST_ANSWERED,
                        "Help request " + id.value() + " is answered by a help and cannot be set back to Open");
            }
            if (!Objects.equals(newRecipe, recipe) || !Objects.equals(newHowToStep, howToStep)
                    || !newIngredients.equals(ingredients) || !newProviders.equals(preferredProviders)) {
                throw new HelpRuleViolationException(HELP_REQUEST_ANSWERED, "Help request " + id.value()
                        + " is answered; its recipe, step, ingredients and preferred providers can no longer change");
            }
        } else if (newStatus == HelpRequestStatus.ANSWERED && !answeredByAHelp) {
            throw new HelpRuleViolationException(ANSWERED_WITHOUT_HELP,
                    "Help request " + id.value() + " can only be Answered when at least one help answers it");
        }

        String newTitle = revision.title() == null ? title
                : Texts.required(revision.title(), Texts.TITLE, INVALID_HELP_REQUEST, "The title of a help request");
        String newDescription = revision.description() == null ? description
                : Texts.required(revision.description(), Texts.LONG_TEXT, INVALID_HELP_REQUEST,
                        "The description of a help request");
        checkTypeRules(type, newRecipe, newHowToStep, newIngredients);
        checkPreferredProviders(type, newProviders);

        title = newTitle;
        description = newDescription;
        recipe = newRecipe;
        howToStep = newHowToStep;
        ingredients = newIngredients;
        preferredProviders = newProviders;
        status = newStatus;
        updatedAt = now;
    }

    /**
     * Answers this request with a help and marks it Answered.
     *
     * @param helpProvider the person giving the help; {@code null} only for the Grandma Avatar
     */
    public Help answer(HelpId helpId, HelpProviderType providerType, CookId helpProvider, String answerTitle,
                       Answer answer, Instant now) {
        Objects.requireNonNull(helpId);
        if (providerType == null) {
            throw new HelpRuleViolationException(INVALID_HELP_PROVIDER, "A help names the kind of its provider");
        }
        if ((providerType == HelpProviderType.GRANDMA_AVATAR) != (helpProvider == null)) {
            throw new HelpRuleViolationException(INVALID_HELP_PROVIDER, providerType == HelpProviderType.GRANDMA_AVATAR
                    ? "A help of the Grandma Avatar has no human help provider"
                    : "A help of a chef or the community names the person who gave it");
        }
        if (!preferredProviders.contains(providerType)) {
            throw new HelpRuleViolationException(PROVIDER_NOT_PREFERRED, "Help request " + id.value()
                    + " asks for " + preferredProviders + ", not for " + providerType);
        }
        if (isChefRequest() && requester.equals(helpProvider)) {
            throw new HelpRuleViolationException(OWN_CHEF_REQUEST,
                    "A cook cannot answer their own Chef request " + id.value());
        }
        verifyAnswer(answer);
        Help help = Help.give(helpId, this, providerType, helpProvider, answerTitle, answer, now);
        if (status != HelpRequestStatus.ANSWERED) {
            status = HelpRequestStatus.ANSWERED;
            updatedAt = now;
        }
        return help;
    }

    /** Checks that {@code answer} fits this request: same type and same recipe, step and ingredients. */
    public void verifyAnswer(Answer answer) {
        if (answer == null) {
            throw new HelpRuleViolationException(HelpRuleViolationException.INVALID_ANSWER, "A help needs an answer");
        }
        if (answer.type() != type) {
            throw new HelpRuleViolationException(ANSWER_TYPE_MISMATCH,
                    "The answer is of type " + answer.type() + " but help request " + id.value() + " asks for " + type);
        }
        switch (answer) {
            case IngredientSubstitutes substitutes -> {
                requireSameRecipe(substitutes.recipe());
                List<IngredientId> substituted = substitutes.substitutes().stream().map(Substitute::ingredient).toList();
                if (substituted.size() != ingredients.size() || !ingredients.equals(new LinkedHashSet<>(substituted))) {
                    throw new HelpRuleViolationException(ANSWER_REFERENCE_MISMATCH,
                            "The substitutes must name each requested ingredient exactly once: " + ingredientValues());
                }
            }
            case PreparationStepExplanation explanation -> {
                requireSameRecipe(explanation.recipe());
                if (!explanation.howToStep().equals(howToStep)) {
                    throw new HelpRuleViolationException(ANSWER_REFERENCE_MISMATCH,
                            "The explanation must be about the requested step " + howToStep.value());
                }
            }
            case CatastropheMitigation mitigation -> mitigation.recipe().ifPresent(this::requireSameRecipe);
            case MenuProposal ignored -> {
                // the courses propose recipes; the request refers to none of them
            }
        }
    }

    /** A help answering this request was withdrawn; without remaining helps the request is open again. */
    public void helpWithdrawn(boolean stillAnsweredByAHelp, Instant now) {
        if (!stillAnsweredByAHelp && status == HelpRequestStatus.ANSWERED) {
            status = HelpRequestStatus.OPEN;
            updatedAt = now;
        }
    }

    /** Only an open request can be withdrawn. */
    public void checkCanBeWithdrawn() {
        if (status != HelpRequestStatus.OPEN) {
            throw new HelpRuleViolationException(HELP_REQUEST_NOT_OPEN,
                    "Help request " + id.value() + " is " + status + "; only an open request can be deleted");
        }
    }

    public boolean isRaisedBy(CookId candidate) {
        return requester.equals(candidate);
    }

    /** A request for chef help - chef help is provided exclusively. */
    public boolean isChefRequest() {
        return preferredProviders.contains(HelpProviderType.CHEF);
    }

    private void requireSameRecipe(RecipeId answered) {
        if (!answered.equals(recipe)) {
            throw new HelpRuleViolationException(ANSWER_REFERENCE_MISMATCH, recipe == null
                    ? "Help request " + id.value() + " names no recipe; the answer must not name one either"
                    : "The answer must be about the requested recipe " + recipe.value());
        }
    }

    private List<UUID> ingredientValues() {
        return ingredients.stream().map(IngredientId::value).toList();
    }

    private static void checkTypeRules(HelpType type, RecipeId recipe, HowToStepId howToStep, Set<IngredientId> ingredients) {
        boolean stepExplanation = type == HelpType.PREPARATION_STEP_EXPLANATION;
        boolean substitute = type == HelpType.INGREDIENT_SUBSTITUTE;
        if ((stepExplanation || substitute) && recipe == null) {
            throw new HelpRuleViolationException(RECIPE_REQUIRED, "A help request of type " + type + " names a recipe");
        }
        if (stepExplanation && howToStep == null) {
            throw new HelpRuleViolationException(HOW_TO_STEP_REQUIRED,
                    "A help request of type " + type + " names the unclear how-to step");
        }
        if (!stepExplanation && howToStep != null) {
            throw new HelpRuleViolationException(HOW_TO_STEP_NOT_ALLOWED,
                    "A howToStep is only allowed for type " + HelpType.PREPARATION_STEP_EXPLANATION);
        }
        if (substitute && ingredients.isEmpty()) {
            throw new HelpRuleViolationException(INGREDIENTS_REQUIRED,
                    "A help request of type " + type + " names at least one ingredient");
        }
        if (!substitute && !ingredients.isEmpty()) {
            throw new HelpRuleViolationException(INGREDIENTS_NOT_ALLOWED,
                    "Ingredients are only allowed for type " + HelpType.INGREDIENT_SUBSTITUTE);
        }
    }

    private static void checkPreferredProviders(HelpType type, Set<HelpProviderType> providers) {
        if (providers.isEmpty() || providers.size() > MAX_PREFERRED_PROVIDERS) {
            throw new HelpRuleViolationException(INVALID_HELP_REQUEST,
                    "A help request selects 1 to " + MAX_PREFERRED_PROVIDERS + " preferred providers");
        }
        if (providers.contains(HelpProviderType.CHEF)) {
            if (type != HelpType.MENU_PROPOSAL) {
                throw new HelpRuleViolationException(CHEF_ONLY_FOR_MENU_PROPOSAL,
                        "Chef support is only available for type " + HelpType.MENU_PROPOSAL);
            }
            if (providers.size() > 1) {
                throw new HelpRuleViolationException(CHEF_EXCLUSIVE,
                        "Chef help is provided exclusively and cannot be combined with another provider");
            }
        }
    }

    private static <T> Set<T> copy(Set<T> values) {
        if (values == null) {
            return Set.of();
        }
        values.forEach(Objects::requireNonNull);
        return Collections.unmodifiableSet(new LinkedHashSet<>(values));
    }

    public HelpRequestId id() {
        return id;
    }

    public CookId requester() {
        return requester;
    }

    public String title() {
        return title;
    }

    public HelpType type() {
        return type;
    }

    public String description() {
        return description;
    }

    public Optional<RecipeId> recipe() {
        return Optional.ofNullable(recipe);
    }

    public Optional<HowToStepId> howToStep() {
        return Optional.ofNullable(howToStep);
    }

    /** In the order the requester named them. */
    public Set<IngredientId> ingredients() {
        return ingredients;
    }

    /** In the order the requester named them. */
    public Set<HelpProviderType> preferredProviders() {
        return preferredProviders;
    }

    public HelpRequestStatus status() {
        return status;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }
}
