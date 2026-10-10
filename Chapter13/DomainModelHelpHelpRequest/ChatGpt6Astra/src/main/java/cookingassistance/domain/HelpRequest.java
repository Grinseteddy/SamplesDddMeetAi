package cookingassistance.domain;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import static cookingassistance.domain.Ids.*;
import static cookingassistance.domain.Values.*;
import static cookingassistance.domain.DomainViolation.*;
public final class HelpRequest {
    public static final int MIN_PREFERRED_PROVIDERS=1, MAX_PREFERRED_PROVIDERS=2;
    private final HelpRequestId id;
    private final RequesterId requester;
    private final String title, description;
    private final Type type;
    private final Optional<RecipeId> recipe;
    private final Optional<HowToStepId> howToStep;
    private final Set<IngredientId> ingredients;
    private final Set<Provider> preferredProviders;
    private final List<URI> pictures;
    private Status status=Status.OPEN;
    public HelpRequest(HelpRequestId id, RequesterId requester, String title, Type type, String description,
            Optional<RecipeId> recipe, Optional<HowToStepId> howToStep, Set<IngredientId> ingredients,
            Set<Provider> preferredProviders, List<URI> pictures) {
        this.id=required(id,"HelpRequestId"); this.requester=required(requester,"Requester");
        this.title=required(title,"Title"); this.type=required(type,"Type"); this.description=required(description,"Description");
        this.recipe=required(recipe,"RecipeOptional"); this.howToStep=required(howToStep,"HowToStepOptional");
        this.ingredients=Values.set(ingredients,"Ingredients"); this.preferredProviders=Values.set(preferredProviders,"PreferredProviders"); this.pictures=Values.list(pictures,"Pictures");
        require(preferredProviders.size()>=MIN_PREFERRED_PROVIDERS && preferredProviders.size()<=MAX_PREFERRED_PROVIDERS,"OneToTwoPreferredProvidersRequired");
        require(!preferredProviders.contains(Provider.CHEF) || type==Type.MENU_PROPOSAL,"ChefOnlyForMenuProposal");
        require(!preferredProviders.contains(Provider.CHEF) || preferredProviders.size()==1,"ChefMustBeExclusive");
        require((type!=Type.INGREDIENT_SUBSTITUTE && type!=Type.PREPARATION_STEP_EXPLANATION) || recipe.isPresent(),"RecipeRequiredForRequestType");
        require(type==Type.PREPARATION_STEP_EXPLANATION || howToStep.isEmpty(),"HowToStepOnlyForPreparationStepExplanation");
        require(type==Type.INGREDIENT_SUBSTITUTE || ingredients.isEmpty(),"IngredientsOnlyForIngredientSubstitute");
    }
    // Only the coordinating domain service can change cross-aggregate acceptance state.
    void answeredBy(Help help) {
        require(help.helpRequest().equals(id),"HelpMustReferToThisRequest");
        require(help.isAccepted(),"AnsweredRequiresAcceptedHelp");
        require(status!=Status.CLOSED,"ClosedRequestCannotBeAnswered"); status=Status.ANSWERED;
    }
    public HelpRequestId id(){return id;} public RequesterId requester(){return requester;}
    public String title(){return title;} public String description(){return description;}
    public Type type(){return type;} public Optional<RecipeId> recipe(){return recipe;}
    public Optional<HowToStepId> howToStep(){return howToStep;} public Set<IngredientId> ingredients(){return ingredients;}
    public Set<Provider> preferredProviders(){return preferredProviders;} public List<URI> pictures(){return pictures;}
    public Status status(){return status;}
    @Override public boolean equals(Object other){return other instanceof HelpRequest that && id.equals(that.id);}
    @Override public int hashCode(){return id.hashCode();}
}
