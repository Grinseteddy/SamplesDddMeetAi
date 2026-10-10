package de.codecentric.cookingassistance.domain.help;

import de.codecentric.cookingassistance.domain.shared.AnswerType;
import de.codecentric.cookingassistance.domain.shared.DomainRuleViolation;
import de.codecentric.cookingassistance.domain.shared.Require;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Glossary term "menu proposal" — the answer to a Menu proposal request.
 * A value object (final class rather than record because of the optional
 * "how to serve").
 */
public final class MenuProposal implements Answer {

    /** Cardinality 1..10 on "course". The upper bound is a round number — see open questions. */
    public static final int MIN_COURSES = 1;
    public static final int MAX_COURSES = 10;

    private final String note;          // 1
    private final int servings;         // 1
    private final Meal meal;            // 1
    private final String howToServe;    // 0..1
    private final List<Course> course;  // 1..10

    public MenuProposal(String note, int servings, Meal meal, String howToServe, List<Course> course) {
        this.note = Require.text(note, "note");
        if (servings < 1) {
            throw new ServingsNotPositive(servings);
        }
        this.servings = servings;
        this.meal = Require.present(meal, "meal");
        this.howToServe = Require.optionalText(howToServe);
        List<Course> courses = Require.list(course, "course");
        if (courses.size() < MIN_COURSES || courses.size() > MAX_COURSES) {
            throw new CourseCountOutOfRange(courses.size());
        }
        // No uniqueness check on dish: "different courses can have the same step when served in parallel".
        this.course = courses;
    }

    public String note() {
        return note;
    }

    public int servings() {
        return servings;
    }

    public Meal meal() {
        return meal;
    }

    public Optional<String> howToServe() {
        return Optional.ofNullable(howToServe);
    }

    public List<Course> course() {
        return course;
    }

    @Override
    public AnswerType answerType() {
        return AnswerType.MENU_PROPOSAL;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof MenuProposal that
                && servings == that.servings
                && note.equals(that.note)
                && meal == that.meal
                && Objects.equals(howToServe, that.howToServe)
                && course.equals(that.course);
    }

    @Override
    public int hashCode() {
        return Objects.hash(note, servings, meal, howToServe, course);
    }

    @Override
    public String toString() {
        return "MenuProposal[" + meal + ", servings=" + servings + ", courses=" + course.size() + "]";
    }

    public static final class CourseCountOutOfRange extends DomainRuleViolation {
        public CourseCountOutOfRange(int count) {
            super("a menu proposal has " + MIN_COURSES + " to " + MAX_COURSES + " courses, not " + count);
        }
    }

    public static final class ServingsNotPositive extends DomainRuleViolation {
        public ServingsNotPositive(int servings) {
            super("a menu proposal serves at least one person, not " + servings);
        }
    }
}
