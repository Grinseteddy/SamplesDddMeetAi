package cookingassistance.domain;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import static cookingassistance.domain.Ids.*;
import static cookingassistance.domain.Values.*;
import static cookingassistance.domain.DomainViolation.*;
/** Exactly one immutable answer variant; see MODEL-DECISIONS.md for color conflict. */
public sealed interface Answer permits Answer.Substitutes, Answer.PreparationStepExplanation, Answer.CatastropheMitigation, Answer.MenuProposal {
    Type answerType();
    record Substitute(IngredientId ingredient, SubstituteIngredient substituteIngredient) {
        public Substitute { required(ingredient,"Ingredient"); required(substituteIngredient,"SubstituteIngredient"); }
    }
    record Substitutes(RecipeId recipe, List<Substitute> substitutes) implements Answer {
        public Substitutes { required(recipe,"Recipe"); substitutes=Values.list(substitutes,"Substitutes"); require(!substitutes.isEmpty(),"AtLeastOneSubstituteRequired"); }
        public Type answerType() { return Type.INGREDIENT_SUBSTITUTE; }
    }
    record PreparationStepExplanation(RecipeId recipe, HowToStepId howToStep, String description, List<URI> images) implements Answer {
        public PreparationStepExplanation { required(recipe,"Recipe"); required(howToStep,"HowToStep"); required(description,"Description"); images=Values.list(images,"Images"); }
        public Type answerType() { return Type.PREPARATION_STEP_EXPLANATION; }
    }
    record CatastropheMitigation(Optional<RecipeId> recipe, String explanation) implements Answer {
        public CatastropheMitigation { required(recipe,"RecipeOptional"); required(explanation,"Explanation"); }
        public Type answerType() { return Type.STEPS_TO_MITIGATE_CATASTROPHE; }
    }
    record Course(int dish, RecipeId recipe) { public Course { required(recipe,"Recipe"); } }
    record MenuProposal(String note, int servings, Meal meal, Optional<String> howToServe, List<Course> courses) implements Answer {
        public static final int MIN_COURSES=1, MAX_COURSES=10;
        public MenuProposal { required(note,"Note"); required(meal,"Meal"); required(howToServe,"HowToServeOptional"); courses=Values.list(courses,"Courses"); require(courses.size()>=MIN_COURSES && courses.size()<=MAX_COURSES,"MenuRequiresOneToTenCourses"); }
        public Type answerType() { return Type.MENU_PROPOSAL; }
    }
}
