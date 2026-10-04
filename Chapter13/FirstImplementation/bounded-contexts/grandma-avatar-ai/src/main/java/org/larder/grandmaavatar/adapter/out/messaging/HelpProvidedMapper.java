package org.larder.grandmaavatar.adapter.out.messaging;

import java.net.URI;

import org.larder.grandmaavatar.adapter.out.messaging.HelpProvidedPayload.AnswerPayload;
import org.larder.grandmaavatar.adapter.out.messaging.HelpProvidedPayload.CatastropheMitigationPayload;
import org.larder.grandmaavatar.adapter.out.messaging.HelpProvidedPayload.CourseMealPayload;
import org.larder.grandmaavatar.adapter.out.messaging.HelpProvidedPayload.CoursePayload;
import org.larder.grandmaavatar.adapter.out.messaging.HelpProvidedPayload.MenuProposalPayload;
import org.larder.grandmaavatar.adapter.out.messaging.HelpProvidedPayload.PreparationStepExplanationPayload;
import org.larder.grandmaavatar.adapter.out.messaging.HelpProvidedPayload.SubstituteIngredientPayload;
import org.larder.grandmaavatar.adapter.out.messaging.HelpProvidedPayload.SubstitutePayload;
import org.larder.grandmaavatar.adapter.out.messaging.HelpProvidedPayload.SubstitutesPayload;
import org.larder.grandmaavatar.domain.CatastropheMitigation;
import org.larder.grandmaavatar.domain.Help;
import org.larder.grandmaavatar.domain.MenuProposal;
import org.larder.grandmaavatar.domain.PreparationStepExplanation;
import org.larder.grandmaavatar.domain.RecipeId;
import org.larder.grandmaavatar.domain.Substitutes;

/** Grandma's Help in the contract's shape. */
public final class HelpProvidedMapper {

    private HelpProvidedMapper() {
    }

    public static HelpProvidedPayload toPayload(Help help) {
        return new HelpProvidedPayload(
                help.id().value(),
                help.helpRequest().value(),
                help.helpRequester().value(),
                help.answerTitle(),
                help.providerType().name(),
                answer(help));
    }

    private static AnswerPayload answer(Help help) {
        String type = help.type().name();
        return switch (help.answer()) {
            case Substitutes s -> new AnswerPayload(type, new SubstitutesPayload(
                    s.recipe().value(),
                    s.substitutes().stream().map(it -> new SubstitutePayload(it.ingredient().value(),
                            new SubstituteIngredientPayload(it.substituteIngredient().name(),
                                    it.substituteIngredient().value(), it.substituteIngredient().unit().name()))).toList(),
                    type), null, null, null);
            case PreparationStepExplanation e -> new AnswerPayload(type, null, new PreparationStepExplanationPayload(
                    e.recipe().value(), e.howToStep().value(), e.description(),
                    e.images().stream().map(URI::toString).toList(), type), null, null);
            case CatastropheMitigation c -> new AnswerPayload(type, null, null, new CatastropheMitigationPayload(
                    c.recipe().map(RecipeId::value).orElse(null), c.explanation(), type), null);
            case MenuProposal m -> new AnswerPayload(type, null, null, null, new MenuProposalPayload(
                    m.note(), m.servings(), m.meal().name(), m.howToServe().orElse(null),
                    m.courses().stream().map(it -> new CoursePayload(it.step(), new CourseMealPayload(it.recipe().value())))
                            .toList(),
                    type));
        };
    }
}
