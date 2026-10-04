package org.larder.cookingassistance.adapter.in.web;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.LinkedHashSet;
import java.util.UUID;

import org.larder.cookingassistance.adapter.in.web.model.Answer;
import org.larder.cookingassistance.adapter.in.web.model.CatastropheMitigation;
import org.larder.cookingassistance.adapter.in.web.model.Course;
import org.larder.cookingassistance.adapter.in.web.model.CourseMeal;
import org.larder.cookingassistance.adapter.in.web.model.Help;
import org.larder.cookingassistance.adapter.in.web.model.HelpProviderType;
import org.larder.cookingassistance.adapter.in.web.model.HelpRequest;
import org.larder.cookingassistance.adapter.in.web.model.HelpRequestStatus;
import org.larder.cookingassistance.adapter.in.web.model.HelpType;
import org.larder.cookingassistance.adapter.in.web.model.Meal;
import org.larder.cookingassistance.adapter.in.web.model.MenuProposal;
import org.larder.cookingassistance.adapter.in.web.model.PreparationStepExplanation;
import org.larder.cookingassistance.adapter.in.web.model.Substitute;
import org.larder.cookingassistance.adapter.in.web.model.SubstituteIngredient;
import org.larder.cookingassistance.adapter.in.web.model.Substitutes;
import org.larder.cookingassistance.adapter.in.web.model.Unit;
import org.larder.cookingassistance.domain.CookId;
import org.larder.cookingassistance.domain.HowToStepId;
import org.larder.cookingassistance.domain.IngredientId;
import org.larder.cookingassistance.domain.RecipeId;

/** Translates the domain model into the contract's model - and only in this direction. */
final class CookingAssistanceMapper {

    private CookingAssistanceMapper() {
    }

    static HelpRequest toApi(org.larder.cookingassistance.domain.HelpRequest request) {
        var providers = new LinkedHashSet<HelpProviderType>();
        request.preferredProviders().forEach(provider -> providers.add(HelpProviderType.valueOf(provider.name())));
        var ingredients = new LinkedHashSet<UUID>();
        request.ingredients().stream().map(IngredientId::value).forEach(ingredients::add);
        return new HelpRequest(
                request.id().value(),
                request.requester().value(),
                request.title(),
                toApi(request.type()),
                request.description(),
                providers,
                HelpRequestStatus.valueOf(request.status().name()))
                .recipe(request.recipe().map(RecipeId::value).orElse(null))
                .howToStep(request.howToStep().map(HowToStepId::value).orElse(null))
                .ingredients(ingredients)
                .createdAt(utc(request.createdAt()))
                .updatedAt(utc(request.updatedAt()));
    }

    static Help toApi(org.larder.cookingassistance.domain.Help help) {
        return new Help(
                help.id().value(),
                help.helpRequest().value(),
                help.helpRequester().value(),
                help.answerTitle(),
                HelpProviderType.valueOf(help.providerType().name()),
                toApi(help.answer()))
                .helpProvider(help.helpProvider().map(CookId::value).orElse(null))
                .createdAt(utc(help.createdAt()))
                .updatedAt(utc(help.updatedAt()));
    }

    static Answer toApi(org.larder.cookingassistance.domain.Answer answer) {
        HelpType type = toApi(answer.type());
        return switch (answer) {
            case org.larder.cookingassistance.domain.IngredientSubstitutes s -> new Substitutes(type, s.recipe().value(),
                    s.substitutes().stream().map(substitute -> new Substitute(substitute.ingredient().value(),
                            new SubstituteIngredient(substitute.name(), substitute.value(),
                                    Unit.valueOf(substitute.unit().name())))).toList());
            case org.larder.cookingassistance.domain.PreparationStepExplanation p -> new PreparationStepExplanation(
                    type, p.recipe().value(), p.howToStep().value(), p.description()).images(p.images());
            case org.larder.cookingassistance.domain.CatastropheMitigation c -> new CatastropheMitigation(
                    type, c.explanation()).recipe(c.recipe().map(RecipeId::value).orElse(null));
            case org.larder.cookingassistance.domain.MenuProposal m -> new MenuProposal(
                    type, m.note(), m.servings(), Meal.valueOf(m.meal().name()),
                    m.courses().stream().map(course -> new Course(course.step(), new CourseMeal(course.recipe().value())))
                            .toList())
                    .howToServe(m.howToServe().orElse(null));
        };
    }

    private static HelpType toApi(org.larder.cookingassistance.domain.HelpType type) {
        return HelpType.valueOf(type.name());
    }

    private static OffsetDateTime utc(Instant instant) {
        return instant.atOffset(ZoneOffset.UTC);
    }
}
