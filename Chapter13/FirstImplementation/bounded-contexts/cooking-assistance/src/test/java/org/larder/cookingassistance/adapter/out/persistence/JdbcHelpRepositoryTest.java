package org.larder.cookingassistance.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
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

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.larder.cookingassistance.domain.Answer;
import org.larder.cookingassistance.domain.CatastropheMitigation;
import org.larder.cookingassistance.domain.Help;
import org.larder.cookingassistance.domain.HelpId;
import org.larder.cookingassistance.domain.HelpProviderType;
import org.larder.cookingassistance.domain.HelpRequest;
import org.larder.cookingassistance.domain.HelpRequestDraft;
import org.larder.cookingassistance.domain.HelpType;
import org.larder.platform.test.TestDatabase;

/** Runs the real migrations against PostgreSQL in its own schema with its own user. */
class JdbcHelpRepositoryTest {

    private JdbcHelpRequestRepository requests;
    private JdbcHelpRepository helps;

    @BeforeEach
    void freshSchema() {
        var database = TestDatabase.forSchema("cookingassistance");
        requests = new JdbcHelpRequestRepository(database.jdbcClient());
        helps = new JdbcHelpRepository(database.jdbcClient());
    }

    private Help answered(HelpRequestDraft draft, HelpProviderType providerType, Answer answer) {
        HelpRequest request = HelpRequest.raise(COOK, draft, NOW);
        Help help = request.answer(HelpId.newId(), providerType,
                providerType == HelpProviderType.GRANDMA_AVATAR ? null : OTHER_COOK, "Answer", answer, NOW);
        requests.save(request);
        helps.save(help);
        return help;
    }

    @Test
    void storesEveryKindOfAnswerUnchanged() {
        Help calm = answered(burningCatastrophe(), HelpProviderType.GRANDMA_AVATAR, stayCalm());
        Help coldPan = answered(foldingTheDough(), HelpProviderType.GRANDMA_AVATAR, useAColdPan());
        Help yoghurt = answered(noButtermilk(), HelpProviderType.COMMUNITY, yoghurtForButtermilk());
        Help menu = answered(dinnerForTheInLaws(), HelpProviderType.CHEF, threeCourses());

        assertThat(helps.findById(calm.id()).orElseThrow().answer()).isEqualTo(stayCalm());
        assertThat(helps.findById(coldPan.id()).orElseThrow().answer()).isEqualTo(useAColdPan());
        assertThat(helps.findById(yoghurt.id()).orElseThrow().answer()).isEqualTo(yoghurtForButtermilk());
        assertThat(helps.findById(menu.id()).orElseThrow().answer()).isEqualTo(threeCourses());

        Help stored = helps.findById(menu.id()).orElseThrow();
        assertThat(stored.helpRequest()).isEqualTo(menu.helpRequest());
        assertThat(stored.helpRequester()).isEqualTo(COOK);
        assertThat(stored.providerType()).isEqualTo(HelpProviderType.CHEF);
        assertThat(stored.helpProvider()).contains(OTHER_COOK);
        assertThat(stored.answerTitle()).isEqualTo("Answer");
        assertThat(stored.createdAt()).isEqualTo(NOW);
        assertThat(helps.findById(calm.id()).orElseThrow().helpProvider()).isEmpty();
    }

    @Test
    void savingAgainRevisesTheHelp() {
        Help help = answered(burningCatastrophe(), HelpProviderType.COMMUNITY, stayCalm());
        HelpRequest request = requests.findById(help.helpRequest()).orElseThrow();
        var breathe = new CatastropheMitigation(Optional.empty(), "Breathe");
        help.revise("Breathe", breathe, request, NOW.plusSeconds(60));
        helps.save(help);

        Help stored = helps.findById(help.id()).orElseThrow();
        assertThat(stored.answerTitle()).isEqualTo("Breathe");
        assertThat(stored.answer()).isEqualTo(breathe);
        assertThat(stored.updatedAt()).isEqualTo(NOW.plusSeconds(60));
    }

    @Test
    void knowsWhetherAHelpOrAHelpForARequestExists() {
        Help help = answered(burningCatastrophe(), HelpProviderType.GRANDMA_AVATAR, stayCalm());

        assertThat(helps.exists(help.id())).isTrue();
        assertThat(helps.exists(HelpId.newId())).isFalse();
        assertThat(helps.existsFor(help.helpRequest())).isTrue();

        helps.delete(help.id());
        assertThat(helps.existsFor(help.helpRequest())).isFalse();
    }

    @Test
    void theSameHelpSavedTwiceIsStoredOnce() {
        Help help = answered(burningCatastrophe(), HelpProviderType.GRANDMA_AVATAR, stayCalm());

        helps.save(help);

        assertThat(helps.search(help.helpRequest(), null, null)).hasSize(1);
    }

    @Test
    void searchesByRequestTypeAndProvider() {
        Help calm = answered(burningCatastrophe(), HelpProviderType.COMMUNITY, stayCalm());
        Help coldPan = answered(foldingTheDough(), HelpProviderType.GRANDMA_AVATAR, useAColdPan());

        assertThat(helps.search(null, null, null)).extracting(Help::id).containsExactlyInAnyOrder(calm.id(), coldPan.id());
        assertThat(helps.search(calm.helpRequest(), null, null)).extracting(Help::id).containsExactly(calm.id());
        assertThat(helps.search(null, HelpType.PREPARATION_STEP_EXPLANATION, null)).extracting(Help::id)
                .containsExactly(coldPan.id());
        assertThat(helps.search(null, null, OTHER_COOK)).extracting(Help::id).containsExactly(calm.id());
        assertThat(helps.search(coldPan.helpRequest(), null, OTHER_COOK)).isEmpty();
    }
}
