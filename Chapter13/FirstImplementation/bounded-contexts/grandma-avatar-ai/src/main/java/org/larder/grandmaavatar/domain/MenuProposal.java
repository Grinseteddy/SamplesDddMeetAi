package org.larder.grandmaavatar.domain;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** A menu of 1..10 courses; courses served in parallel share a step. */
public record MenuProposal(String note, int servings, MealType meal, Optional<String> howToServe, List<Course> courses)
        implements Answer {

    public MenuProposal {
        Texts.answerText(note, "note", 2000);
        Answer.require(servings >= 1, "a menu is for at least one serving");
        Answer.require(meal != null, "a menu is for a meal");
        howToServe = Objects.requireNonNullElse(howToServe, Optional.empty());
        howToServe.ifPresent(text -> Answer.require(text.length() <= 2000, "howToServe must not be longer than 2000 characters"));
        courses = courses == null ? List.of() : List.copyOf(courses);
        Answer.require(!courses.isEmpty() && courses.size() <= 10, "a menu has 1 to 10 courses");
    }

    @Override
    public HelpType type() {
        return HelpType.MENU_PROPOSAL;
    }

    /** A menu proposes recipes of its own; the request references none it would have to match. */
    @Override
    public void mustFit(HelpRequest request) {
        // nothing to compare
    }

    /** One course: its serving order and the recipe to cook. */
    public record Course(int step, RecipeId recipe) {

        public Course {
            Answer.require(step >= 1, "a course's step starts at 1");
            Answer.require(recipe != null, "a course needs a recipe");
        }
    }
}
