package org.larder.cookingassistance.domain;

import static org.larder.cookingassistance.domain.HelpRuleViolationException.INVALID_ANSWER;

import java.util.List;
import java.util.Optional;

/** Answer of type Menu proposal: 1..10 courses for a meal and a number of servings. */
public record MenuProposal(String note, int servings, Meal meal, Optional<String> howToServe, List<Course> courses)
        implements Answer {

    static final int MAX_COURSES = 10;

    public MenuProposal {
        note = Texts.required(note, Texts.LONG_TEXT, INVALID_ANSWER, "The note of a menu proposal");
        if (servings < 1) {
            throw new HelpRuleViolationException(INVALID_ANSWER, "A menu proposal serves at least one guest");
        }
        if (meal == null) {
            throw new HelpRuleViolationException(INVALID_ANSWER, "A menu proposal names the meal of the day");
        }
        howToServe = howToServe == null ? Optional.empty() : howToServe;
        howToServe.ifPresent(text -> Texts.limited(text, Texts.LONG_TEXT, INVALID_ANSWER, "How to serve"));
        if (courses == null || courses.isEmpty() || courses.size() > MAX_COURSES) {
            throw new HelpRuleViolationException(INVALID_ANSWER, "A menu proposal has 1 to " + MAX_COURSES + " courses");
        }
        courses = List.copyOf(courses);
    }

    @Override
    public HelpType type() {
        return HelpType.MENU_PROPOSAL;
    }
}
