package org.larder.grandmaavatar.adapter.out.advisor;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.larder.grandmaavatar.application.HelpAdvisor;
import org.larder.grandmaavatar.domain.Advice;
import org.larder.grandmaavatar.domain.CatastropheMitigation;
import org.larder.grandmaavatar.domain.HelpRequest;
import org.larder.grandmaavatar.domain.PreparationStepExplanation;

/**
 * Grandma's recipe box: deterministic, rule-based, kind canned answers - the stand-in for the external
 * AI ({@code larder.grandma-avatar-ai.advisor=recipe-box}, the default). It never calls anything outside.
 * <ul>
 *   <li>{@code STEPS_TO_MITIGATE_CATASTROPHE}: a card chosen by keywords in title and description
 *       (burnt, too salty, curdled sauce), otherwise the general "Stay calm" card. The recipe of the
 *       request is named if the request named one.</li>
 *   <li>{@code PREPARATION_STEP_EXPLANATION}: general advice on working through a step, for the
 *       requested recipe and step. The box does not know the step's text.</li>
 *   <li>{@code INGREDIENT_SUBSTITUTE}: no answer. A valid answer needs a substitute with name, amount
 *       and unit for every requested ingredient, but the request carries only ingredient ids - the box
 *       cannot know what it would replace, and a guessed amount could spoil the dish.</li>
 *   <li>{@code MENU_PROPOSAL}: no answer. A menu needs recipe ids for its courses, servings and a meal;
 *       the box knows no recipes, and the request says neither servings nor meal.</li>
 * </ul>
 * Requests without an answer stay open for the community (or a chef).
 */
public class RecipeBoxAdvisor implements HelpAdvisor {

    private static final List<Card> CATASTROPHE_CARDS = List.of(
            new Card(List.of("burn", "burnt", "scorch", "charred"), "Stay calm", """
                    Stay calm, my dear. Take the pan off the heat at once and do not scrape the bottom - \
                    the burnt taste sits there. Lift whatever is still good into a new, cold pan and go on \
                    gently with less heat. Cut away dark crusts from baked goods; a dusting of icing sugar or \
                    a spoon of cream hides the rest, and nobody will notice."""),
            new Card(List.of("salt"), "Too much salt is no disaster", """
                    Don't worry, that happens to the best of us. Add unsalted liquid - water, stock without \
                    salt or cream - or more of the other ingredients. In soups and stews a peeled raw potato \
                    simmered for ten minutes takes up some salt; take it out before serving. A squeeze of \
                    lemon makes the rest taste less salty."""),
            new Card(List.of("curdle", "split", "lump"), "Save the sauce", """
                    Take the pot off the heat right away. For a curdled or split sauce, whisk a spoon of ice \
                    cold water or a fresh egg yolk into a clean bowl and beat the sauce into it drop by drop. \
                    Lumps you pass through a sieve. Then warm it again, gently and stirring all the time."""));

    private static final Card STAY_CALM = new Card(List.of(), "Stay calm", """
            Stay calm, my dear. Take the pot off the heat, open a window and take a deep breath. Taste what \
            is still good - most catastrophes look worse than they are. Save what you can in a new, cold \
            pan, and if it cannot be saved, a simple dish served with a smile is better than a perfect one \
            served in tears.""");

    private static final String STEP_ADVICE = """
            Read the step twice before you begin and put everything it needs within reach. Do one thing at \
            a time and do not rush: when the step says "until golden" or "until thick", trust your eyes and \
            nose more than the clock. If something looks different than you expected, lower the heat - that \
            buys you time to think.""";

    @Override
    public Optional<Advice> advise(HelpRequest request) {
        return switch (request.type()) {
            case STEPS_TO_MITIGATE_CATASTROPHE -> {
                Card card = cardFor(request);
                yield Optional.of(new Advice(card.title(), new CatastropheMitigation(request.recipe(), card.text())));
            }
            case PREPARATION_STEP_EXPLANATION -> Optional.of(new Advice("Step by step, my dear",
                    new PreparationStepExplanation(request.recipe().orElseThrow(), request.howToStep().orElseThrow(),
                            STEP_ADVICE, List.of())));
            case INGREDIENT_SUBSTITUTE, MENU_PROPOSAL -> Optional.empty();
        };
    }

    private static Card cardFor(HelpRequest request) {
        String situation = (request.title() + " " + request.description()).toLowerCase(Locale.ROOT);
        return CATASTROPHE_CARDS.stream()
                .filter(card -> card.keywords().stream().anyMatch(situation::contains))
                .findFirst()
                .orElse(STAY_CALM);
    }

    private record Card(List<String> keywords, String title, String text) {
    }
}
