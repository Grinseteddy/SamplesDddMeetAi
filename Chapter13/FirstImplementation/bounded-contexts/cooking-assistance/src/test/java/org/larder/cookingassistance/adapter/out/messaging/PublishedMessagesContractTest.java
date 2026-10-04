package org.larder.cookingassistance.adapter.out.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.larder.cookingassistance.TestData.COOK;
import static org.larder.cookingassistance.TestData.NOW;
import static org.larder.cookingassistance.TestData.OTHER_COOK;
import static org.larder.cookingassistance.TestData.burningCatastrophe;
import static org.larder.cookingassistance.TestData.dinnerForTheInLaws;
import static org.larder.cookingassistance.TestData.foldingTheDough;
import static org.larder.cookingassistance.TestData.noButtermilk;
import static org.larder.cookingassistance.TestData.stayCalm;
import static org.larder.cookingassistance.TestData.threeCourses;
import static org.larder.cookingassistance.TestData.useAColdPan;
import static org.larder.cookingassistance.TestData.yoghurtForButtermilk;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.larder.cookingassistance.domain.Answer;
import org.larder.cookingassistance.domain.CatastropheMitigation;
import org.larder.cookingassistance.domain.Help;
import org.larder.cookingassistance.domain.HelpId;
import org.larder.cookingassistance.domain.HelpProviderType;
import org.larder.cookingassistance.domain.HelpRequest;
import org.larder.cookingassistance.domain.HelpRequestDraft;
import org.larder.cookingassistance.domain.HelpType;
import org.larder.cookingassistance.domain.PreparationStepExplanation;
import org.larder.cookingassistance.domain.MenuProposal;
import org.larder.platform.messaging.MessageHeader;
import org.larder.platform.test.AsyncApiContract;

import com.fasterxml.jackson.databind.ObjectMapper;

/** What this context publishes satisfies its own published language (cooking-assistance.asyncapi.yaml). */
class PublishedMessagesContractTest {

    private static final AsyncApiContract CONTRACT = AsyncApiContract.of("cooking-assistance.asyncapi.yaml");
    private final ObjectMapper json = new ObjectMapper();

    static List<HelpRequestDraft> everyTypeOfRequest() {
        HelpRequestDraft catastropheWithoutRecipe = new HelpRequestDraft("Smoke", HelpType.STEPS_TO_MITIGATE_CATASTROPHE,
                "The kitchen is full of smoke", null, null, Set.of(), burningCatastrophe().preferredProviders());
        return List.of(burningCatastrophe(), catastropheWithoutRecipe, foldingTheDough(), noButtermilk(),
                dinnerForTheInLaws());
    }

    @ParameterizedTest
    @MethodSource("everyTypeOfRequest")
    void helpRequestedMatchesTheContract(HelpRequestDraft draft) throws Exception {
        String payload = json.writeValueAsString(HelpRequestedPayload.of(HelpRequest.raise(COOK, draft, NOW)));

        assertThatCode(() -> CONTRACT.assertPayload("helpRequested", payload)).doesNotThrowAnyException();
    }

    @Test
    void helpRequestedLeavesOutWhatTheTypeDoesNotAllow() throws Exception {
        String payload = json.writeValueAsString(HelpRequestedPayload.of(HelpRequest.raise(COOK, burningCatastrophe(), NOW)));

        assertThat(payload).doesNotContain("howToStep").doesNotContain("ingredients").contains("\"status\":\"OPEN\"");
    }

    @Test
    void theContractCheckWouldCatchABrokenHelpRequested() {
        assertThatThrownBy(() -> CONTRACT.assertPayload("helpRequested",
                "{\"helpRequestId\":\"" + UUID.randomUUID() + "\",\"title\":\"incomplete\"}"))
                .isInstanceOf(AssertionError.class);
    }

    static List<Object[]> everyKindOfHelp() {
        return List.of(
                new Object[] {burningCatastrophe(), HelpProviderType.GRANDMA_AVATAR, stayCalm()},
                new Object[] {burningCatastrophe(), HelpProviderType.COMMUNITY, new CatastropheMitigation(Optional.empty(), "Breathe")},
                new Object[] {foldingTheDough(), HelpProviderType.GRANDMA_AVATAR, useAColdPan()},
                new Object[] {foldingTheDough(), HelpProviderType.GRANDMA_AVATAR,
                        new PreparationStepExplanation(useAColdPan().recipe(), useAColdPan().howToStep(), "Gently", List.of())},
                new Object[] {noButtermilk(), HelpProviderType.COMMUNITY, yoghurtForButtermilk()},
                new Object[] {dinnerForTheInLaws(), HelpProviderType.CHEF, threeCourses()},
                new Object[] {dinnerForTheInLaws(), HelpProviderType.CHEF, new MenuProposal("Simple", 2,
                        threeCourses().meal(), Optional.empty(), threeCourses().courses())});
    }

    @ParameterizedTest
    @MethodSource("everyKindOfHelp")
    void helpProvidedMatchesTheContract(HelpRequestDraft draft, HelpProviderType providerType, Answer answer) throws Exception {
        HelpRequest request = HelpRequest.raise(COOK, draft, NOW);
        Help help = request.answer(HelpId.newId(), providerType,
                providerType == HelpProviderType.GRANDMA_AVATAR ? null : OTHER_COOK, "Answer", answer, NOW);
        String payload = json.writeValueAsString(HelpProvidedPayload.of(help));

        assertThatCode(() -> CONTRACT.assertPayload("helpProvided", payload)).doesNotThrowAnyException();
        assertThat(payload).contains("\"helpRequestStatus\":\"ANSWERED\"");
    }

    @Test
    void theAvatarsHelpHasNoHelpProvider() throws Exception {
        HelpRequest request = HelpRequest.raise(COOK, burningCatastrophe(), NOW);
        Help help = request.answer(HelpId.newId(), HelpProviderType.GRANDMA_AVATAR, null, "Stay calm", stayCalm(), NOW);

        assertThat(json.writeValueAsString(HelpProvidedPayload.of(help))).doesNotContain("helpProvider\"");
    }

    @Test
    void theHeadersMatchTheContract() {
        var headers = new HashMap<String, Object>(MessageHeader.newMessage(UUID.randomUUID(),
                CookingAssistanceMessagingConfiguration.SOURCE).asAmqpHeaders());

        assertThatCode(() -> CONTRACT.assertHeaders("helpRequested", headers)).doesNotThrowAnyException();
        assertThatCode(() -> CONTRACT.assertHeaders("helpProvided", headers)).doesNotThrowAnyException();
        assertThat(CONTRACT.messageName("helpRequested")).isEqualTo(OutboxHelpEvents.HELP_REQUESTED_MESSAGE);
        assertThat(CONTRACT.messageName("helpProvided")).isEqualTo(OutboxHelpEvents.HELP_PROVIDED_MESSAGE);
    }
}
