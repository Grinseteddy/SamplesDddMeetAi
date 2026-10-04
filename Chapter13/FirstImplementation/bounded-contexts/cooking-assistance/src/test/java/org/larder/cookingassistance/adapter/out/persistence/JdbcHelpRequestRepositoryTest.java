package org.larder.cookingassistance.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.larder.cookingassistance.TestData.BUTTER;
import static org.larder.cookingassistance.TestData.BUTTERMILK;
import static org.larder.cookingassistance.TestData.COOK;
import static org.larder.cookingassistance.TestData.FOLD_IN;
import static org.larder.cookingassistance.TestData.NOW;
import static org.larder.cookingassistance.TestData.OTHER_COOK;
import static org.larder.cookingassistance.TestData.SCONES;
import static org.larder.cookingassistance.TestData.burningCatastrophe;
import static org.larder.cookingassistance.TestData.dinnerForTheInLaws;
import static org.larder.cookingassistance.TestData.foldingTheDough;
import static org.larder.cookingassistance.TestData.noButtermilk;
import static org.larder.cookingassistance.TestData.stayCalm;

import java.util.LinkedHashSet;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.larder.cookingassistance.domain.HelpId;
import org.larder.cookingassistance.domain.HelpProviderType;
import org.larder.cookingassistance.domain.HelpRequest;
import org.larder.cookingassistance.domain.HelpRequestRevision;
import org.larder.cookingassistance.domain.HelpRequestStatus;
import org.larder.platform.test.TestDatabase;
import org.springframework.dao.DataIntegrityViolationException;

/** Runs the real migrations against PostgreSQL in its own schema with its own user. */
class JdbcHelpRequestRepositoryTest {

    private JdbcHelpRequestRepository requests;
    private JdbcHelpRepository helps;

    @BeforeEach
    void freshSchema() {
        var database = TestDatabase.forSchema("cookingassistance");
        requests = new JdbcHelpRequestRepository(database.jdbcClient());
        helps = new JdbcHelpRepository(database.jdbcClient());
    }

    @Test
    void storesARequestWithItsReferencesInOrder() {
        HelpRequest request = HelpRequest.raise(COOK, noButtermilk(), NOW);
        request.revise(HelpRequestRevision.none().withIngredients(new LinkedHashSet<>(List.of(BUTTER, BUTTERMILK))), false,
                NOW.plusSeconds(30));
        requests.save(request);

        HelpRequest stored = requests.findById(request.id()).orElseThrow();
        assertThat(stored.requester()).isEqualTo(COOK);
        assertThat(stored.type()).isEqualTo(request.type());
        assertThat(stored.title()).isEqualTo(request.title());
        assertThat(stored.description()).isEqualTo(request.description());
        assertThat(stored.recipe()).contains(SCONES);
        assertThat(stored.howToStep()).isEmpty();
        assertThat(stored.ingredients()).containsExactly(BUTTER, BUTTERMILK);
        assertThat(stored.preferredProviders()).containsExactly(HelpProviderType.COMMUNITY);
        assertThat(stored.status()).isEqualTo(HelpRequestStatus.OPEN);
        assertThat(stored.createdAt()).isEqualTo(NOW);
        assertThat(stored.updatedAt()).isEqualTo(NOW.plusSeconds(30));
    }

    @Test
    void savingAgainUpdatesTheRequestAndReplacesItsChildren() {
        HelpRequest request = HelpRequest.raise(COOK, foldingTheDough(), NOW);
        requests.save(request);
        request.revise(HelpRequestRevision.none().withTitle("Fold?")
                .withPreferredProviders(new LinkedHashSet<>(List.of(HelpProviderType.COMMUNITY, HelpProviderType.GRANDMA_AVATAR))),
                false, NOW);
        requests.save(request);

        HelpRequest stored = requests.findByIdForUpdate(request.id()).orElseThrow();
        assertThat(stored.title()).isEqualTo("Fold?");
        assertThat(stored.howToStep()).contains(FOLD_IN);
        assertThat(stored.preferredProviders())
                .containsExactly(HelpProviderType.COMMUNITY, HelpProviderType.GRANDMA_AVATAR);
    }

    @Test
    void searchesByRequesterAndStatus() {
        HelpRequest mine = HelpRequest.raise(COOK, burningCatastrophe(), NOW);
        HelpRequest chef = HelpRequest.raise(COOK, dinnerForTheInLaws(), NOW.plusSeconds(1));
        HelpRequest theirs = HelpRequest.raise(OTHER_COOK, burningCatastrophe(), NOW.plusSeconds(2));
        theirs.answer(HelpId.newId(), HelpProviderType.COMMUNITY, COOK, "Stay calm", stayCalm(), NOW.plusSeconds(3));
        List.of(mine, chef, theirs).forEach(requests::save);

        assertThat(requests.search(null, null)).extracting(HelpRequest::id).containsExactly(mine.id(), chef.id(), theirs.id());
        assertThat(requests.search(COOK, null)).extracting(HelpRequest::id).containsExactly(mine.id(), chef.id());
        assertThat(requests.search(null, HelpRequestStatus.ANSWERED)).extracting(HelpRequest::id).containsExactly(theirs.id());
        assertThat(requests.search(OTHER_COOK, HelpRequestStatus.OPEN)).isEmpty();
    }

    @Test
    void deletesAnOpenRequestWithItsChildren() {
        HelpRequest request = HelpRequest.raise(COOK, noButtermilk(), NOW);
        requests.save(request);

        requests.delete(request.id());

        assertThat(requests.findById(request.id())).isEmpty();
    }

    @Test
    void aRequestWithHelpsCannotBeDeletedEvenByMistake() {
        HelpRequest request = HelpRequest.raise(COOK, burningCatastrophe(), NOW);
        var help = request.answer(HelpId.newId(), HelpProviderType.GRANDMA_AVATAR, null, "Stay calm", stayCalm(), NOW);
        requests.save(request);
        helps.save(help);

        assertThatThrownBy(() -> requests.delete(request.id())).isInstanceOf(DataIntegrityViolationException.class);
    }
}
