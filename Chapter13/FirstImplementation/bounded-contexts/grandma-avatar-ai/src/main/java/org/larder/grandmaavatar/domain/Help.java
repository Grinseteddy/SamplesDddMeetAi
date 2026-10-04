package org.larder.grandmaavatar.domain;

import java.util.Objects;

/**
 * The Help Grandma gives for a request (Help glossary). Created only through {@link #answer}, which
 * enforces the invariants: Grandma answers only requests meant for her, the answer's type equals the
 * request's type, and recipe, step and ingredients in the answer are those of the request. The help
 * is always machine-generated ({@link HelpProviderType#GRANDMA_AVATAR}) and has no human provider.
 */
public final class Help {

    private final HelpId id;
    private final HelpRequestId helpRequest;
    private final CookId helpRequester;
    private final String answerTitle;
    private final Answer answer;

    private Help(HelpId id, HelpRequestId helpRequest, CookId helpRequester, String answerTitle, Answer answer) {
        this.id = id;
        this.helpRequest = helpRequest;
        this.helpRequester = helpRequester;
        this.answerTitle = answerTitle;
        this.answer = answer;
    }

    /** @throws InvalidAnswerException if the advice does not answer this request */
    public static Help answer(HelpRequest request, Advice advice) {
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(advice, "advice");
        Answer.require(request.isForGrandma(), "Grandma only answers open requests that name her");
        Answer.require(advice.answer().type() == request.type(),
                "the answer type " + advice.answer().type() + " must equal the request type " + request.type());
        advice.answer().mustFit(request);
        return new Help(HelpId.forRequest(request.id()), request.id(), request.requester(), advice.title(), advice.answer());
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

    public String answerTitle() {
        return answerTitle;
    }

    public Answer answer() {
        return answer;
    }

    public HelpType type() {
        return answer.type();
    }

    public HelpProviderType providerType() {
        return HelpProviderType.GRANDMA_AVATAR;
    }
}
