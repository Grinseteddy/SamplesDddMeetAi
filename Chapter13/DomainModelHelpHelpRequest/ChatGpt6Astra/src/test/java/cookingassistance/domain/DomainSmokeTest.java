package cookingassistance.domain;
import java.util.*;
import java.math.BigDecimal;
import java.net.URI;
import static cookingassistance.domain.Ids.*;
import static cookingassistance.domain.Values.*;
public final class DomainSmokeTest {
    private static int checks;
    private static final RequesterId REQUESTER=new RequesterId(UUID.randomUUID());
    private static final RecipeId RECIPE=new RecipeId(UUID.randomUUID());
    private static final HowToStepId STEP=new HowToStepId(UUID.randomUUID());
    private static final IngredientId INGREDIENT=new IngredientId(UUID.randomUUID());
    private static void check(boolean condition) {checks++; if(!condition) throw new AssertionError("Check "+checks);}
    private static void rejects(String rule, Runnable action) {
        checks++; try {action.run();} catch(DomainViolation e) {if(!e.getMessage().equals(rule)) throw new AssertionError(e); return;}
        throw new AssertionError("Expected "+rule);
    }
    private static HelpRequest request(Type type, Optional<RecipeId> recipe, Optional<HowToStepId> step, Set<IngredientId> ingredients, Set<Provider> providers) {
        return new HelpRequest(new HelpRequestId(UUID.randomUUID()), REQUESTER, "title",type,"description",recipe,step,ingredients,providers,List.of());
    }
    private static HelpRequest menu() {return request(Type.MENU_PROPOSAL,Optional.empty(),Optional.empty(),Set.of(),Set.of(Provider.CHEF));}
    private static Answer.MenuProposal proposal(List<Answer.Course> courses) {return new Answer.MenuProposal("note",6,Meal.DINNER,Optional.empty(),courses);}
    private static Help help(HelpRequest request, Provider provider, Answer answer) {return Help.answer(new HelpId(UUID.randomUUID()),request,"answer",provider,Optional.empty(),answer);}
    public static void main(String[] args) {
        var course=new Answer.Course(2,RECIPE);
        var request=menu(); var answer=proposal(List.of(course)); var help=help(request,Provider.CHEF,answer);
        check(request.status()==Status.OPEN && !help.isAccepted());
        new CookingAssistance().accept(request,help);
        check(request.status()==Status.ANSWERED && help.isAccepted());
        check(help(request,Provider.CHEF,answer)!=null); // Answered requests still accept answers.
        new CookingAssistance().accept(request,help); // Idempotent acceptance.
        check(proposal(Collections.nCopies(10,course)).courses().size()==10);
        check(proposal(List.of(course,course)).courses().size()==2); // Parallel dishes may share ordinal.
        rejects("MenuRequiresOneToTenCourses",()->proposal(List.of()));
        rejects("MenuRequiresOneToTenCourses",()->proposal(Collections.nCopies(11,course)));
        rejects("OneToTwoPreferredProvidersRequired",()->request(Type.MENU_PROPOSAL,Optional.empty(),Optional.empty(),Set.of(),Set.of()));
        rejects("OneToTwoPreferredProvidersRequired",()->request(Type.MENU_PROPOSAL,Optional.empty(),Optional.empty(),Set.of(),Set.of(Provider.values())));
        check(request(Type.MENU_PROPOSAL,Optional.empty(),Optional.empty(),Set.of(),Set.of(Provider.COMMUNITY,Provider.GRANDMA_AVATAR))!=null);
        rejects("ChefMustBeExclusive",()->request(Type.MENU_PROPOSAL,Optional.empty(),Optional.empty(),Set.of(),Set.of(Provider.CHEF,Provider.COMMUNITY)));
        rejects("ChefOnlyForMenuProposal",()->request(Type.STEPS_TO_MITIGATE_CATASTROPHE,Optional.empty(),Optional.empty(),Set.of(),Set.of(Provider.CHEF)));
        for(Type type:List.of(Type.INGREDIENT_SUBSTITUTE,Type.PREPARATION_STEP_EXPLANATION))
            rejects("RecipeRequiredForRequestType",()->request(type,Optional.empty(),Optional.empty(),Set.of(),Set.of(Provider.COMMUNITY)));
        rejects("HowToStepOnlyForPreparationStepExplanation",()->request(Type.MENU_PROPOSAL,Optional.empty(),Optional.of(STEP),Set.of(),Set.of(Provider.CHEF)));
        rejects("IngredientsOnlyForIngredientSubstitute",()->request(Type.MENU_PROPOSAL,Optional.empty(),Optional.empty(),Set.of(INGREDIENT),Set.of(Provider.CHEF)));
        var subRequest=request(Type.INGREDIENT_SUBSTITUTE,Optional.of(RECIPE),Optional.empty(),Set.of(INGREDIENT),Set.of(Provider.COMMUNITY));
        var substitute=new Answer.Substitute(INGREDIENT,new SubstituteIngredient("flour",BigDecimal.ONE,Unit.CUP));
        var substitutes=new Answer.Substitutes(RECIPE,List.of(substitute));
        check(help(subRequest,Provider.COMMUNITY,substitutes)!=null);
        rejects("AtLeastOneSubstituteRequired",()->new Answer.Substitutes(RECIPE,List.of()));
        rejects("AnswerRecipeMustMatchRequest",()->help(subRequest,Provider.COMMUNITY,new Answer.Substitutes(new RecipeId(UUID.randomUUID()),List.of(substitute))));
        var unknown=new Answer.Substitute(new IngredientId(UUID.randomUUID()),substitute.substituteIngredient());
        rejects("SubstituteIngredientMustBeRequested",()->help(subRequest,Provider.COMMUNITY,new Answer.Substitutes(RECIPE,List.of(unknown))));
        rejects("AnswerTypeMustMatchRequestType",()->help(subRequest,Provider.COMMUNITY,answer));
        rejects("ProviderMustBeRequested",()->help(subRequest,Provider.GRANDMA_AVATAR,substitutes));
        check(Help.answer(new HelpId(UUID.randomUUID()),subRequest,"title",Provider.COMMUNITY,Optional.of(new HelpProviderId(REQUESTER.value())),substitutes)!=null);
        rejects("CookCannotAnswerOwnChefRequest",()->Help.answer(new HelpId(UUID.randomUUID()),request,"title",Provider.CHEF,Optional.of(new HelpProviderId(REQUESTER.value())),answer));
        var stepRequest=request(Type.PREPARATION_STEP_EXPLANATION,Optional.of(RECIPE),Optional.of(STEP),Set.of(),Set.of(Provider.GRANDMA_AVATAR));
        var explanation=new Answer.PreparationStepExplanation(RECIPE,STEP,"Use a cold pan",List.of());
        check(help(stepRequest,Provider.GRANDMA_AVATAR,explanation)!=null);
        rejects("AnswerHowToStepMustMatchRequest",()->help(stepRequest,Provider.GRANDMA_AVATAR,new Answer.PreparationStepExplanation(RECIPE,new HowToStepId(UUID.randomUUID()),"text",List.of())));
        var noStep=request(Type.PREPARATION_STEP_EXPLANATION,Optional.of(RECIPE),Optional.empty(),Set.of(),Set.of(Provider.GRANDMA_AVATAR));
        rejects("AnswerHowToStepMustMatchRequest",()->help(noStep,Provider.GRANDMA_AVATAR,explanation));
        var catastrophe=request(Type.STEPS_TO_MITIGATE_CATASTROPHE,Optional.empty(),Optional.empty(),Set.of(),Set.of(Provider.COMMUNITY));
        check(help(catastrophe,Provider.COMMUNITY,new Answer.CatastropheMitigation(Optional.empty(),"Use a new pan"))!=null);
        rejects("AnswerRecipeMustMatchRequest",()->help(catastrophe,Provider.COMMUNITY,new Answer.CatastropheMitigation(Optional.of(RECIPE),"text")));
        var other=menu(); var unaccepted=help(other,Provider.CHEF,answer);
        rejects("HelpMustReferToThisRequestAndRequester",()->new CookingAssistance().accept(request,unaccepted));
        check(!unaccepted.isAccepted() && other.status()==Status.OPEN);
        rejects("AnsweredRequiresAcceptedHelp",()->other.answeredBy(unaccepted));
        rejects("HelpRequestIdIsRequired",()->new HelpRequest(null,REQUESTER,"title",Type.MENU_PROPOSAL,"desc",Optional.empty(),Optional.empty(),Set.of(),Set.of(Provider.CHEF),List.of()));
        rejects("AnswerIsRequired",()->help(menu(),Provider.CHEF,null));
        rejects("RecipeIsRequired",()->new Answer.Course(1,null));
        rejects("HowToServeOptionalIsRequired",()->new Answer.MenuProposal("note",1,Meal.DINNER,null,List.of(course)));
        rejects("ImagesMustNotContainNull",()->new Answer.PreparationStepExplanation(RECIPE,STEP,"text",Arrays.asList((URI)null)));
        var mutable=new ArrayList<Answer.Course>(); mutable.add(course); var copy=proposal(mutable); mutable.clear(); check(copy.courses().size()==1);
        try {copy.courses().clear(); throw new AssertionError("Mutable collection");} catch(UnsupportedOperationException expected) {checks++;}
        var sameId=Help.answer(help.id(),request,"different title",Provider.CHEF,Optional.empty(),answer);
        check(help.equals(sameId) && help.hashCode()==sameId.hashCode());
        System.out.println("PASS: "+checks+" checks");
    }
}
