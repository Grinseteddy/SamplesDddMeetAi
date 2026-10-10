package de.codecentric.cookingassistance.domain.help;

import de.codecentric.cookingassistance.domain.external.CookId;
import de.codecentric.cookingassistance.domain.external.IngredientId;
import de.codecentric.cookingassistance.domain.external.RecipeId;
import de.codecentric.cookingassistance.domain.help.HelpRuleViolation.AnswerTypeNotAsRequested;
import de.codecentric.cookingassistance.domain.help.HelpRuleViolation.CannotAnswerOwnChefRequest;
import de.codecentric.cookingassistance.domain.help.HelpRuleViolation.HelpAlreadyAccepted;
import de.codecentric.cookingassistance.domain.help.HelpRuleViolation.HelpBelongsToOtherRequest;
import de.codecentric.cookingassistance.domain.help.HelpRuleViolation.HelpRequestClosed;
import de.codecentric.cookingassistance.domain.help.HelpRuleViolation.HowToStepNotAsInRequest;
import de.codecentric.cookingassistance.domain.help.HelpRuleViolation.IngredientNotInRequest;
import de.codecentric.cookingassistance.domain.help.HelpRuleViolation.RecipeNotAsInRequest;
import de.codecentric.cookingassistance.domain.helprequest.HelpRequest;
import de.codecentric.cookingassistance.domain.shared.HelpAccepted;
import de.codecentric.cookingassistance.domain.shared.HelpId;
import de.codecentric.cookingassistance.domain.shared.HelpProviderType;
import de.codecentric.cookingassistance.domain.shared.HelpRequestId;
import de.codecentric.cookingassistance.domain.shared.Require;

import java.util.Objects;
import java.util.Optional;

/**
 * Aggregate root "Help" — one answer to one Help Request.
 *
 * <p>Help holds its Help Request by id only. The request is handed to
 * {@link #provide} and {@link #accept} to be <em>read</em>, because the
 * glossary's "same as in request" rules cannot be checked otherwise; it is
 * never stored.
 */
public final class Help {

    private final HelpId helpId;
    private final HelpRequestId helpRequest;
    private final CookId helpRequester;
    private final String answerTitle;
    private final HelpProviderType helpProviderType;
    private final CookId helpProvider;       // 0..1
    private final Answer answer;
    private boolean isAccepted;

    private Help(HelpId helpId,
                 HelpRequestId helpRequest,
                 CookId helpRequester,
                 String answerTitle,
                 HelpProviderType helpProviderType,
                 CookId helpProvider,
                 Answer answer) {
        this.helpId = Require.present(helpId, "help Id");
        this.helpRequest = Require.present(helpRequest, "help request");
        this.helpRequester = Require.present(helpRequester, "help requester");
        this.answerTitle = Require.text(answerTitle, "answer title");
        this.helpProviderType = Require.present(helpProviderType, "help provider type");
        this.helpProvider = helpProvider;
        this.answer = Require.present(answer, "answer");
        this.isAccepted = false;
    }

    /**
     * Provide help for a request. Checks every "same as in request" rule on
     * the Help glossary against the request as it is now.
     *
     * @param helpProvider the answering cook, or {@code null} (drawn 0..1)
     */
    public static Help provide(HelpId helpId,
                               HelpRequest request,
                               String answerTitle,
                               HelpProviderType helpProviderType,
                               CookId helpProvider,
                               Answer answer) {
        Require.present(request, "help request");
        Require.present(answer, "answer");
        if (!request.acceptsHelp()) {
            throw new HelpRequestClosed(request.helpRequestId());
        }
        if (answer.answerType() != request.type()) {
            throw new AnswerTypeNotAsRequested(request.type(), answer.answerType());
        }
        matchesRequest(answer, request);
        if (helpProvider != null && helpProvider.equals(request.requester()) && request.isChefRequest()) {
            throw new CannotAnswerOwnChefRequest();
        }
        return new Help(helpId, request.helpRequestId(), request.requester(),
                answerTitle, helpProviderType, helpProvider, answer);
    }

    private static void matchesRequest(Answer answer, HelpRequest request) {
        Optional<RecipeId> requestedRecipe = request.recipe();
        if (answer instanceof Substitutes substitutes) {
            requireSameRecipe(substitutes.recipe(), requestedRecipe);
            for (Substitute substitute : substitutes.substitute()) {
                IngredientId ingredient = substitute.ingredient();
                if (!request.ingredients().contains(ingredient)) {
                    throw new IngredientNotInRequest(ingredient);
                }
            }
        } else if (answer instanceof PreparationStepExplanation explanation) {
            requireSameRecipe(explanation.recipe(), requestedRecipe);
            if (!request.howToStep().map(explanation.howToStep()::equals).orElse(false)) {
                throw new HowToStepNotAsInRequest();
            }
        } else if (answer instanceof CatastropheMitigation mitigation) {
            mitigation.recipe().ifPresent(recipe -> requireSameRecipe(recipe, requestedRecipe));
        }
        // MenuProposal: no "same as in request" rule is drawn.
    }

    private static void requireSameRecipe(RecipeId answered, Optional<RecipeId> requested) {
        if (!requested.map(answered::equals).orElse(false)) {
            throw new RecipeNotAsInRequest();
        }
    }

    /**
     * The help is accepted. Returns the fact for the Help Request to record
     * ({@link HelpRequest#acceptHelp}). Who may accept is an open question
     * (glossary "!" on isAccepted).
     */
    public HelpAccepted accept(HelpRequest request) {
        Require.present(request, "help request");
        if (!request.helpRequestId().equals(helpRequest)) {
            throw new HelpBelongsToOtherRequest(helpId, request.helpRequestId());
        }
        if (!request.acceptsHelp()) {
            throw new HelpRequestClosed(helpRequest);
        }
        if (isAccepted) {
            throw new HelpAlreadyAccepted(helpId);
        }
        isAccepted = true;
        return new HelpAccepted(helpId, helpRequest);
    }

    // ---- read access -----------------------------------------------------

    public HelpId helpId() {
        return helpId;
    }

    public HelpRequestId helpRequest() {
        return helpRequest;
    }

    public CookId helpRequester() {
        return helpRequester;
    }

    public String answerTitle() {
        return answerTitle;
    }

    public HelpProviderType helpProviderType() {
        return helpProviderType;
    }

    public Optional<CookId> helpProvider() {
        return Optional.ofNullable(helpProvider);
    }

    public Answer answer() {
        return answer;
    }

    public boolean isAccepted() {
        return isAccepted;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Help that && helpId.equals(that.helpId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(helpId);
    }

    @Override
    public String toString() {
        return "Help[" + helpId.value() + ", " + answer.answerType() + ", accepted=" + isAccepted + "]";
    }
}
