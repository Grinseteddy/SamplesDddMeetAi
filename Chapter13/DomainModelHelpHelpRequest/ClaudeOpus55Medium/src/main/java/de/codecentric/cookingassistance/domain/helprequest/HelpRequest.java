package de.codecentric.cookingassistance.domain.helprequest;

import de.codecentric.cookingassistance.domain.external.CookId;
import de.codecentric.cookingassistance.domain.external.HowToStepId;
import de.codecentric.cookingassistance.domain.external.IngredientId;
import de.codecentric.cookingassistance.domain.external.RecipeId;
import de.codecentric.cookingassistance.domain.helprequest.HelpRequestRuleViolation.AcceptedHelpForOtherRequest;
import de.codecentric.cookingassistance.domain.helprequest.HelpRequestRuleViolation.ChefHelpMustBeExclusive;
import de.codecentric.cookingassistance.domain.helprequest.HelpRequestRuleViolation.ChefSupportOnlyForMenuProposal;
import de.codecentric.cookingassistance.domain.helprequest.HelpRequestRuleViolation.DuplicatePreferredProvider;
import de.codecentric.cookingassistance.domain.helprequest.HelpRequestRuleViolation.HelpRequestClosed;
import de.codecentric.cookingassistance.domain.helprequest.HelpRequestRuleViolation.HowToStepOnlyForPreparationStepExplanation;
import de.codecentric.cookingassistance.domain.helprequest.HelpRequestRuleViolation.IngredientsOnlyForIngredientSubstitute;
import de.codecentric.cookingassistance.domain.helprequest.HelpRequestRuleViolation.PreferredProviderCountOutOfRange;
import de.codecentric.cookingassistance.domain.helprequest.HelpRequestRuleViolation.RecipeMandatoryForType;
import de.codecentric.cookingassistance.domain.shared.AnswerType;
import de.codecentric.cookingassistance.domain.shared.HelpAccepted;
import de.codecentric.cookingassistance.domain.shared.HelpProviderType;
import de.codecentric.cookingassistance.domain.shared.HelpRequestId;
import de.codecentric.cookingassistance.domain.shared.Require;

import java.net.URI;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Aggregate root "Help Request" — a cook asking the community, a chef or the
 * Grandma Avatar for help.
 *
 * <p>Every invariant on the Help Request glossary is checked when the request
 * is raised; afterwards only its {@link Status} changes.
 */
public final class HelpRequest {

    /** Cardinality 1..2 on "preferred Provider". */
    public static final int MIN_PREFERRED_PROVIDERS = 1;
    public static final int MAX_PREFERRED_PROVIDERS = 2;

    private static final Set<AnswerType> RECIPE_MANDATORY_FOR =
            EnumSet.of(AnswerType.PREPARATION_STEP_EXPLANATION, AnswerType.INGREDIENT_SUBSTITUTE);

    private final HelpRequestId helpRequestId;
    private final CookId requester;
    private final String title;
    private final AnswerType type;
    private final String description;
    private final RecipeId recipe;            // 0..1
    private final HowToStepId howToStep;      // 0..1
    private final List<IngredientId> ingredients;
    private final Set<HelpProviderType> preferredProvider;
    private final List<URI> picture;
    private Status status;

    private HelpRequest(HelpRequestId helpRequestId,
                        CookId requester,
                        String title,
                        AnswerType type,
                        String description,
                        RecipeId recipe,
                        HowToStepId howToStep,
                        List<IngredientId> ingredients,
                        List<HelpProviderType> preferredProvider,
                        List<URI> picture) {
        this.helpRequestId = Require.present(helpRequestId, "help request Id");
        this.requester = Require.present(requester, "requester");
        this.title = Require.text(title, "title");
        this.type = Require.present(type, "type");
        this.description = Require.text(description, "description");
        this.recipe = recipe;
        this.howToStep = howToStep;
        this.ingredients = Require.list(ingredients, "ingredients");
        this.preferredProvider = preferredProviders(preferredProvider);
        this.picture = Require.list(picture, "picture");
        this.status = Status.OPEN;

        if (RECIPE_MANDATORY_FOR.contains(type) && recipe == null) {
            throw new RecipeMandatoryForType(type);
        }
        if (howToStep != null && type != AnswerType.PREPARATION_STEP_EXPLANATION) {
            throw new HowToStepOnlyForPreparationStepExplanation(type);
        }
        if (!this.ingredients.isEmpty() && type != AnswerType.INGREDIENT_SUBSTITUTE) {
            throw new IngredientsOnlyForIngredientSubstitute(type);
        }
        if (this.preferredProvider.contains(HelpProviderType.CHEF)) {
            if (this.preferredProvider.size() > 1) {
                throw new ChefHelpMustBeExclusive();
            }
            if (type != AnswerType.MENU_PROPOSAL) {
                throw new ChefSupportOnlyForMenuProposal(type);
            }
        }
    }

    /**
     * A cook requests help. Terms drawn 0..1 are passed as {@code null} when
     * absent; terms drawn 0..* as an empty list.
     */
    public static HelpRequest request(HelpRequestId helpRequestId,
                                      CookId requester,
                                      String title,
                                      AnswerType type,
                                      String description,
                                      RecipeId recipe,
                                      HowToStepId howToStep,
                                      List<IngredientId> ingredients,
                                      List<HelpProviderType> preferredProvider,
                                      List<URI> picture) {
        return new HelpRequest(helpRequestId, requester, title, type, description,
                recipe, howToStep, ingredients, preferredProvider, picture);
    }

    private static Set<HelpProviderType> preferredProviders(List<HelpProviderType> given) {
        List<HelpProviderType> providers = Require.list(given, "preferred Provider");
        if (providers.size() < MIN_PREFERRED_PROVIDERS || providers.size() > MAX_PREFERRED_PROVIDERS) {
            throw new PreferredProviderCountOutOfRange(providers.size());
        }
        EnumSet<HelpProviderType> set = EnumSet.copyOf(providers);
        if (set.size() != providers.size()) {
            throw new DuplicatePreferredProvider();
        }
        return Collections.unmodifiableSet(set);
    }

    // ---- behaviour -------------------------------------------------------

    /**
     * A Help for this request was accepted. Moves Open → Answered; an Answered
     * request stays Answered ("Answered help requests can be still answered").
     * Being the only way into Answered, this is what guarantees
     * "At least one Help must be accepted".
     */
    public void acceptHelp(HelpAccepted accepted) {
        Require.present(accepted, "accepted help");
        if (!accepted.helpRequestId().equals(helpRequestId)) {
            throw new AcceptedHelpForOtherRequest(helpRequestId, accepted.helpRequestId());
        }
        requireNotClosed();
        status = Status.ANSWERED;
    }

    /** Close the request. Open question (glossary "!"): who may close, and from which status. */
    public void close() {
        requireNotClosed();
        status = Status.CLOSED;
    }

    /** Help may still be provided while the request is Open or Answered. */
    public boolean acceptsHelp() {
        return status != Status.CLOSED;
    }

    /** "Chef requests" — the request asks the Chef, who is then its only preferred provider. */
    public boolean isChefRequest() {
        return preferredProvider.contains(HelpProviderType.CHEF);
    }

    private void requireNotClosed() {
        if (status == Status.CLOSED) {
            throw new HelpRequestClosed(helpRequestId);
        }
    }

    // ---- read access -----------------------------------------------------

    public HelpRequestId helpRequestId() {
        return helpRequestId;
    }

    public CookId requester() {
        return requester;
    }

    public String title() {
        return title;
    }

    public AnswerType type() {
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

    public List<IngredientId> ingredients() {
        return ingredients;
    }

    public Set<HelpProviderType> preferredProvider() {
        return preferredProvider;
    }

    public Status status() {
        return status;
    }

    public List<URI> picture() {
        return picture;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof HelpRequest that && helpRequestId.equals(that.helpRequestId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(helpRequestId);
    }

    @Override
    public String toString() {
        return "HelpRequest[" + helpRequestId.value() + ", " + type + ", " + status + "]";
    }
}
