package org.larder.mealplanning.domain;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * What a cook serves for one occasion (aggregate root): occasion, servings, meal of the day, serving
 * instructions and the courses with their recipes. Rules:
 *
 * <ul>
 *   <li>The owner is the cook who set the plan up and never changes; only the owner changes or deletes it.</li>
 *   <li>A plan may be set up empty and filled step by step; every part is optional until it is given.</li>
 *   <li>The occasion has 1-200 characters, the serving instructions 1-2000 characters (not blank);
 *       a plan is planned for at least one serving.</li>
 *   <li>Once given, a plan has 1 to 10 courses; a new list replaces the old one as a whole.
 *       Steps start at 1; courses with the same step are served in parallel.</li>
 *   <li>The plan's diet is derived from its courses: it is the least restrictive diet of the
 *       recipes, because every course must suit the guests. A plan without courses has no diet.</li>
 * </ul>
 *
 * Every change is validated completely before anything is changed, so a rejected change leaves the plan as it was.
 */
public final class MealPlan {

    static final int MAX_OCCASION_LENGTH = 200;
    static final int MAX_HOW_TO_SERVE_LENGTH = 2000;
    static final int MAX_COURSES = 10;

    private final MealPlanId id;
    private final CookId owner;
    private String occasion;
    private Integer servings;
    private Meal meal;
    private String howToServe;
    private List<Course> courses;

    private MealPlan(MealPlanId id, CookId owner, String occasion, Integer servings, Meal meal, String howToServe,
                     List<Course> courses) {
        this.id = Objects.requireNonNull(id);
        this.owner = Objects.requireNonNull(owner);
        this.occasion = requireOccasion(occasion);
        this.servings = requireServings(servings);
        this.meal = meal;
        this.howToServe = requireHowToServe(howToServe);
        this.courses = courses == null || courses.isEmpty() ? List.of() : requireCourses(courses);
    }

    /** A cook sets up a meal plan of their own, possibly empty. */
    public static MealPlan setUp(CookId owner, MealPlanDraft draft) {
        MealPlanDraft given = draft == null ? MealPlanDraft.empty() : draft;
        List<Course> courses = given.courses() == null ? null : requireCourses(given.courses());
        return new MealPlan(MealPlanId.newId(), owner, given.occasion(), given.servings(), given.meal(),
                given.howToServe(), courses);
    }

    /** Recreates a stored meal plan; a plan whose courses were never given has none. */
    public static MealPlan restore(MealPlanId id, CookId owner, String occasion, Integer servings, Meal meal,
                                   String howToServe, List<Course> courses) {
        return new MealPlan(id, owner, occasion, servings, meal, howToServe, courses);
    }

    public boolean isOwnedBy(CookId candidate) {
        return owner.equals(candidate);
    }

    /** Applies the given parts of the revision; {@code null} parts stay as they are. */
    public void revise(MealPlanRevision revision) {
        String newOccasion = revision.occasion() == null ? occasion : requireOccasion(revision.occasion());
        Integer newServings = revision.servings() == null ? servings : requireServings(revision.servings());
        Meal newMeal = revision.meal() == null ? meal : revision.meal();
        String newHowToServe = revision.removeHowToServe() ? null
                : revision.howToServe() == null ? howToServe : requireHowToServe(revision.howToServe());
        List<Course> newCourses = revision.courses() == null ? courses : requireCourses(revision.courses());

        occasion = newOccasion;
        servings = newServings;
        meal = newMeal;
        howToServe = newHowToServe;
        courses = newCourses;
    }

    /**
     * The diet the plan is suitable for, derived from the diet snapshots of its courses: the least
     * restrictive one. Empty when the plan has no courses yet.
     */
    public Optional<Diet> diet() {
        return courses.isEmpty() ? Optional.empty()
                : Optional.of(Diet.leastRestrictive(courses.stream().map(Course::diet).toList()));
    }

    /**
     * Whether every course's recipe is at least as restrictive as {@code requested}, e.g. a plan of
     * vegan courses suits vegetarians. A plan without courses suits no diet.
     */
    public boolean isSuitableFor(Diet requested) {
        return diet().map(diet -> diet.isAtLeastAsRestrictiveAs(requested)).orElse(false);
    }

    /** Whether the plan was set up for this occasion (same text, ignoring case). */
    public boolean isFor(String candidate) {
        return occasion != null && occasion.equalsIgnoreCase(candidate);
    }

    // --- rules ---------------------------------------------------------------------------------

    private static String requireOccasion(String occasion) {
        if (occasion == null) {
            return null;
        }
        if (occasion.isBlank()) {
            throw MealPlanRuleViolationException.invalid("The occasion of a meal plan must not be blank");
        }
        if (occasion.length() > MAX_OCCASION_LENGTH) {
            throw MealPlanRuleViolationException.invalid(
                    "The occasion of a meal plan has at most " + MAX_OCCASION_LENGTH + " characters");
        }
        return occasion;
    }

    private static Integer requireServings(Integer servings) {
        if (servings != null && servings < 1) {
            throw MealPlanRuleViolationException.invalid("A meal plan is planned for at least one serving, not " + servings);
        }
        return servings;
    }

    private static String requireHowToServe(String howToServe) {
        if (howToServe == null) {
            return null;
        }
        if (howToServe.isBlank()) {
            throw MealPlanRuleViolationException.invalid("The serving instructions must not be blank; remove them instead");
        }
        if (howToServe.length() > MAX_HOW_TO_SERVE_LENGTH) {
            throw MealPlanRuleViolationException.invalid(
                    "The serving instructions have at most " + MAX_HOW_TO_SERVE_LENGTH + " characters");
        }
        return howToServe;
    }

    private static List<Course> requireCourses(List<Course> courses) {
        if (courses.isEmpty() || courses.size() > MAX_COURSES) {
            throw MealPlanRuleViolationException.invalid(
                    "A meal plan has 1 to " + MAX_COURSES + " courses, not " + courses.size());
        }
        return List.copyOf(courses);
    }

    // --- state ---------------------------------------------------------------------------------

    public MealPlanId id() {
        return id;
    }

    public CookId owner() {
        return owner;
    }

    public Optional<String> occasion() {
        return Optional.ofNullable(occasion);
    }

    public Optional<Integer> servings() {
        return Optional.ofNullable(servings);
    }

    public Optional<Meal> meal() {
        return Optional.ofNullable(meal);
    }

    public Optional<String> howToServe() {
        return Optional.ofNullable(howToServe);
    }

    /** The courses in the order the cook gave them. */
    public List<Course> courses() {
        return courses;
    }
}
