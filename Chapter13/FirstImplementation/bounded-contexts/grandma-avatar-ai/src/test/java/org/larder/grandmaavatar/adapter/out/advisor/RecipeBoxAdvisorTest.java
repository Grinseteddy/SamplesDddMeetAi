package org.larder.grandmaavatar.adapter.out.advisor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.larder.grandmaavatar.TestData.FOLD_IN;
import static org.larder.grandmaavatar.TestData.SCONES;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.larder.grandmaavatar.TestData;
import org.larder.grandmaavatar.domain.Advice;
import org.larder.grandmaavatar.domain.CatastropheMitigation;
import org.larder.grandmaavatar.domain.Help;
import org.larder.grandmaavatar.domain.HelpRequest;
import org.larder.grandmaavatar.domain.HelpType;
import org.larder.grandmaavatar.domain.PreparationStepExplanation;

class RecipeBoxAdvisorTest {

    private final RecipeBoxAdvisor recipeBox = new RecipeBoxAdvisor();

    @Test
    void burningSconesGetTheStayCalmCardWithTheirRecipe() {
        HelpRequest request = TestData.burningScones();
        Advice advice = recipeBox.advise(request).orElseThrow();

        assertThat(advice.title()).isEqualTo("Stay calm");
        CatastropheMitigation mitigation = (CatastropheMitigation) advice.answer();
        assertThat(mitigation.recipe()).contains(SCONES);
        assertThat(mitigation.explanation()).contains("Stay calm").contains("new, cold pan");
        assertThat(Help.answer(request, advice).type()).isEqualTo(HelpType.STEPS_TO_MITIGATE_CATASTROPHE);
    }

    @Test
    void aSaltySoupGetsItsOwnCardAndNoRecipeWhenNoneWasNamed() {
        HelpRequest request = TestData.catastropheWithoutRecipe();
        Advice advice = recipeBox.advise(request).orElseThrow();

        assertThat(advice.title()).isEqualTo("Too much salt is no disaster");
        assertThat(((CatastropheMitigation) advice.answer()).recipe()).isEmpty();
        Help.answer(request, advice);
    }

    @Test
    void explainsTheRequestedStep() {
        HelpRequest request = TestData.foldingTheDough();
        Advice advice = recipeBox.advise(request).orElseThrow();

        PreparationStepExplanation explanation = (PreparationStepExplanation) advice.answer();
        assertThat(explanation.recipe()).isEqualTo(SCONES);
        assertThat(explanation.howToStep()).isEqualTo(FOLD_IN);
        assertThat(explanation.images()).isEmpty();
        assertThat(Help.answer(request, advice).type()).isEqualTo(HelpType.PREPARATION_STEP_EXPLANATION);
    }

    @Test
    void hasNoSubstitutesAndNoMenusInTheBox() {
        assertThat(recipeBox.advise(TestData.noButtermilk())).isEqualTo(Optional.empty());
        assertThat(recipeBox.advise(TestData.dinnerForTheInLaws())).isEqualTo(Optional.empty());
    }

    @Test
    void isDeterministic() {
        assertThat(recipeBox.advise(TestData.burningScones())).isEqualTo(recipeBox.advise(TestData.burningScones()));
    }
}
