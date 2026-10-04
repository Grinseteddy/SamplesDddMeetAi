package org.larder.recipecatalog.adapter.in.web;

import java.net.URI;
import java.util.List;

import org.larder.platform.security.CurrentCook;
import org.larder.recipecatalog.adapter.in.web.model.HowToStepCreate;
import org.larder.recipecatalog.adapter.in.web.model.IngredientCreate;
import org.larder.recipecatalog.adapter.in.web.model.RecipeCreate;
import org.larder.recipecatalog.adapter.in.web.model.RecipeUpdate;
import org.larder.recipecatalog.domain.CookId;
import org.larder.recipecatalog.domain.Diet;
import org.larder.recipecatalog.domain.HowToStepDraft;
import org.larder.recipecatalog.domain.IngredientDraft;
import org.larder.recipecatalog.domain.Meal;
import org.larder.recipecatalog.domain.PreparationTime;
import org.larder.recipecatalog.domain.Quantity;
import org.larder.recipecatalog.domain.RecipeDraft;
import org.larder.recipecatalog.domain.RecipeRevision;
import org.larder.recipecatalog.domain.Unit;

/**
 * Translates the contract's request bodies into the domain's drafts and revisions. Bean validation
 * of the generated model has already checked the contract's constraints; the domain checks the rest
 * (e.g. an ingredient value greater than 0, unique sequence numbers).
 */
final class RecipeRequests {

    private RecipeRequests() {
    }

    static CookId caller() {
        return new CookId(CurrentCook.require().value());
    }

    static RecipeDraft toDraft(RecipeCreate request) {
        return new RecipeDraft(
                request.getName(),
                request.getSubtitle(),
                request.getMainImage(),
                request.getFurtherImages(),
                PreparationTime.parse(request.getPreparationTime()),
                request.getServings(),
                toDomain(request.getMeal()),
                toDomain(request.getDiet()),
                request.getIngredients().stream().map(RecipeRequests::toDraft).toList(),
                request.getHowToSteps().stream().map(RecipeRequests::toDraft).toList());
    }

    static IngredientDraft toDraft(IngredientCreate request) {
        return new IngredientDraft(request.getName(), new Quantity(request.getValue(), toDomain(request.getUnit())));
    }

    static HowToStepDraft toDraft(HowToStepCreate request) {
        return new HowToStepDraft(request.getSequenceNumber(), request.getDescription(), request.getIllustration());
    }

    /**
     * Absent (or {@code null}) properties leave the recipe as it is. The generated model cannot tell an
     * absent {@code furtherImages} from an empty one, so an empty list leaves the images as they are, too.
     */
    static RecipeRevision toRevision(RecipeUpdate request) {
        List<URI> furtherImages = request.getFurtherImages() == null || request.getFurtherImages().isEmpty()
                ? null : request.getFurtherImages();
        return new RecipeRevision(
                request.getName(),
                request.getSubtitle(),
                request.getMainImage(),
                furtherImages,
                request.getPreparationTime() == null ? null : PreparationTime.parse(request.getPreparationTime()),
                request.getServings(),
                request.getDiet() == null ? null : toDomain(request.getDiet()));
    }


    static Meal toDomain(org.larder.recipecatalog.adapter.in.web.model.Meal meal) {
        return meal == null ? null : Meal.valueOf(meal.name());
    }

    static Diet toDomain(org.larder.recipecatalog.adapter.in.web.model.Diet diet) {
        return diet == null ? null : Diet.valueOf(diet.name());
    }

    static Unit toDomain(org.larder.recipecatalog.adapter.in.web.model.Unit unit) {
        return unit == null ? null : Unit.valueOf(unit.name());
    }
}
