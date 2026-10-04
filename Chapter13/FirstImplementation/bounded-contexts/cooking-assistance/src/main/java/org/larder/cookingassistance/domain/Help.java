package org.larder.cookingassistance.domain;

import static org.larder.cookingassistance.domain.HelpRuleViolationException.INVALID_ANSWER;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/**
 * An answer given to a help request by a chef, the Grandma Avatar or the community (aggregate root).
 * Created only through {@link HelpRequest#answer}, which checks that it fits the request.
 *
 * <ul>
 *   <li>The help provider is the person who gave it; the Grandma Avatar's help has none.</li>
 *   <li>Only the provider changes or withdraws a help; a replaced answer must still fit the request.</li>
 *   <li>A chef's help is seen only by the cook who raised the request.</li>
 * </ul>
 */
public final class Help {

    private final HelpId id;
    private final HelpRequestId helpRequest;
    private final CookId helpRequester;
    private final HelpProviderType providerType;
    private final CookId helpProvider;
    private final Instant createdAt;
    private String answerTitle;
    private Answer answer;
    private Instant updatedAt;

    private Help(HelpId id, HelpRequestId helpRequest, CookId helpRequester, HelpProviderType providerType,
                 CookId helpProvider, String answerTitle, Answer answer, Instant createdAt, Instant updatedAt) {
        this.id = Objects.requireNonNull(id);
        this.helpRequest = Objects.requireNonNull(helpRequest);
        this.helpRequester = Objects.requireNonNull(helpRequester);
        this.providerType = Objects.requireNonNull(providerType);
        this.helpProvider = helpProvider;
        this.answerTitle = Texts.required(answerTitle, Texts.TITLE, INVALID_ANSWER, "The title of an answer");
        this.answer = Objects.requireNonNull(answer);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    static Help give(HelpId id, HelpRequest request, HelpProviderType providerType, CookId helpProvider,
                     String answerTitle, Answer answer, Instant now) {
        return new Help(id, request.id(), request.requester(), providerType, helpProvider, answerTitle, answer, now, now);
    }

    /** Recreates a stored help. */
    public static Help restore(HelpId id, HelpRequestId helpRequest, CookId helpRequester, HelpProviderType providerType,
                               CookId helpProvider, String answerTitle, Answer answer, Instant createdAt, Instant updatedAt) {
        return new Help(id, helpRequest, helpRequester, providerType, helpProvider, answerTitle, answer, createdAt,
                updatedAt);
    }

    /**
     * Changes title and/or answer; {@code null} leaves them as they are. A new answer must still fit
     * {@code request}, the request this help answers.
     */
    public void revise(String newAnswerTitle, Answer newAnswer, HelpRequest request, Instant now) {
        if (!request.id().equals(helpRequest)) {
            throw new IllegalArgumentException("Help " + id.value() + " does not answer help request " + request.id().value());
        }
        String title = newAnswerTitle == null ? answerTitle
                : Texts.required(newAnswerTitle, Texts.TITLE, INVALID_ANSWER, "The title of an answer");
        if (newAnswer != null) {
            request.verifyAnswer(newAnswer);
            answer = newAnswer;
        }
        answerTitle = title;
        updatedAt = now;
    }

    public boolean isProvidedBy(CookId candidate) {
        return helpProvider != null && helpProvider.equals(candidate);
    }

    /** A chef's help is visible only to the cook who raised the request; every other help to everybody. */
    public boolean isVisibleTo(CookId caller) {
        return providerType != HelpProviderType.CHEF || helpRequester.equals(caller);
    }

    public HelpId id() {
        return id;
    }

    public HelpRequestId helpRequest() {
        return helpRequest;
    }

    public CookId helpRequester() {
        return helpRequester;
    }

    public HelpProviderType providerType() {
        return providerType;
    }

    public Optional<CookId> helpProvider() {
        return Optional.ofNullable(helpProvider);
    }

    public String answerTitle() {
        return answerTitle;
    }

    public Answer answer() {
        return answer;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }
}
