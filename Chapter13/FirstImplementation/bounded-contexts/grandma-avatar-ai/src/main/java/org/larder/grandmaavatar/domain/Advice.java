package org.larder.grandmaavatar.domain;

/** What Grandma's advisor comes up with for a request: a short title and the answer itself. */
public record Advice(String title, Answer answer) {

    public Advice {
        Texts.answerText(title, "answer title", 200);
        Answer.require(answer != null, "advice needs an answer");
    }
}
