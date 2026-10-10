package cookingassistance.domain;

import java.util.Optional;
import static cookingassistance.domain.Ids.*;
import static cookingassistance.domain.Values.*;
import static cookingassistance.domain.DomainViolation.*;
public final class Help {
    private final HelpId id;
    private final HelpRequestId helpRequest;
    private final RequesterId helpRequester;
    private final String answerTitle;
    private final Provider helpProviderType;
    private final Optional<HelpProviderId> helpProvider;
    private final Answer answer;
    private boolean isAccepted;
    private Help(HelpId id, HelpRequest request, String title, Provider providerType, Optional<HelpProviderId> provider, Answer answer) {
        this.id=required(id,"HelpId"); required(request,"HelpRequest");
        this.helpRequest=request.id(); this.helpRequester=request.requester();
        this.answerTitle=required(title,"AnswerTitle"); this.helpProviderType=required(providerType,"HelpProviderType");
        this.helpProvider=required(provider,"HelpProviderOptional"); this.answer=required(answer,"Answer");
        validateAgainst(request);
    }
    public static Help answer(HelpId id, HelpRequest request, String title, Provider providerType, Optional<HelpProviderId> provider, Answer answer) {
        return new Help(id,request,title,providerType,provider,answer);
    }
    void validateAgainst(HelpRequest request) {
        required(request,"HelpRequest");
        require(helpRequest.equals(request.id()) && helpRequester.equals(request.requester()),"HelpMustReferToThisRequestAndRequester");
        require(request.status()!=Status.CLOSED,"ClosedRequestCannotBeAnswered");
        require(answer.answerType()==request.type(),"AnswerTypeMustMatchRequestType");
        require(request.preferredProviders().contains(helpProviderType),"ProviderMustBeRequested");
        require(helpProviderType!=Provider.CHEF || request.type()==Type.MENU_PROPOSAL,"ChefOnlyForMenuProposal");
        // The only known shared identity for a cook is RequesterId; see open question 7.
        require(helpProviderType!=Provider.CHEF || helpProvider.isEmpty() || !helpProvider.get().value().equals(helpRequester.value()),"CookCannotAnswerOwnChefRequest");
        if(answer instanceof Answer.Substitutes a) {
            require(request.recipe().equals(Optional.of(a.recipe())),"AnswerRecipeMustMatchRequest");
            require(a.substitutes().stream().allMatch(s->request.ingredients().contains(s.ingredient())),"SubstituteIngredientMustBeRequested");
        } else if(answer instanceof Answer.PreparationStepExplanation a) {
            require(request.recipe().equals(Optional.of(a.recipe())),"AnswerRecipeMustMatchRequest");
            require(request.howToStep().equals(Optional.of(a.howToStep())),"AnswerHowToStepMustMatchRequest");
        } else if(answer instanceof Answer.CatastropheMitigation a) {
            require(a.recipe().isEmpty() || a.recipe().equals(request.recipe()),"AnswerRecipeMustMatchRequest");
        }
    }
    void accept(){isAccepted=true;}
    public HelpId id(){return id;} public HelpRequestId helpRequest(){return helpRequest;}
    public RequesterId helpRequester(){return helpRequester;} public String answerTitle(){return answerTitle;}
    public Provider helpProviderType(){return helpProviderType;} public Optional<HelpProviderId> helpProvider(){return helpProvider;}
    public Answer answer(){return answer;} public boolean isAccepted(){return isAccepted;}
    @Override public boolean equals(Object other){return other instanceof Help that && id.equals(that.id);}
    @Override public int hashCode(){return id.hashCode();}
}
