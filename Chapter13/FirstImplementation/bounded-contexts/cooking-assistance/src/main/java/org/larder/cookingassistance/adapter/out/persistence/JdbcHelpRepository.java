package org.larder.cookingassistance.adapter.out.persistence;

import java.math.BigDecimal;
import java.net.URI;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.larder.cookingassistance.application.HelpRepository;
import org.larder.cookingassistance.domain.Answer;
import org.larder.cookingassistance.domain.CatastropheMitigation;
import org.larder.cookingassistance.domain.CookId;
import org.larder.cookingassistance.domain.Course;
import org.larder.cookingassistance.domain.Help;
import org.larder.cookingassistance.domain.HelpId;
import org.larder.cookingassistance.domain.HelpProviderType;
import org.larder.cookingassistance.domain.HelpRequestId;
import org.larder.cookingassistance.domain.HelpType;
import org.larder.cookingassistance.domain.HowToStepId;
import org.larder.cookingassistance.domain.IngredientId;
import org.larder.cookingassistance.domain.IngredientSubstitutes;
import org.larder.cookingassistance.domain.Meal;
import org.larder.cookingassistance.domain.MenuProposal;
import org.larder.cookingassistance.domain.PreparationStepExplanation;
import org.larder.cookingassistance.domain.RecipeId;
import org.larder.cookingassistance.domain.Substitute;
import org.larder.cookingassistance.domain.Unit;
import org.springframework.jdbc.core.simple.JdbcClient;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Helps in {@code help}; the answer is one JSON document in this adapter's own storage format
 * ({@link StoredAnswer}) - deliberately not the contract's shape, so contract and storage evolve apart.
 */
class JdbcHelpRepository implements HelpRepository {

    private static final String SELECT = """
            select help_id, help_request_id, help_requester, answer_title, provider_type, help_provider, answer,
                   created_at, updated_at
              from help
            """;

    private final JdbcClient jdbc;
    private final ObjectMapper json = new ObjectMapper();

    JdbcHelpRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void save(Help help) {
        jdbc.sql("""
                insert into help (help_id, help_request_id, help_requester, answer_title, provider_type, help_provider,
                                  answer_type, answer, created_at, updated_at)
                values (:id, :helpRequest, :helpRequester, :answerTitle, :providerType, :helpProvider,
                        :answerType, cast(:answer as jsonb), :createdAt, :updatedAt)
                on conflict (help_id) do update
                   set answer_title = excluded.answer_title, answer_type = excluded.answer_type,
                       answer = excluded.answer, updated_at = excluded.updated_at
                """)
                .param("id", help.id().value())
                .param("helpRequest", help.helpRequest().value())
                .param("helpRequester", help.helpRequester().value())
                .param("answerTitle", help.answerTitle())
                .param("providerType", help.providerType().name())
                .param("helpProvider", help.helpProvider().map(CookId::value).orElse(null))
                .param("answerType", help.answer().type().name())
                .param("answer", write(StoredAnswer.of(help.answer())))
                .param("createdAt", Timestamp.from(help.createdAt()))
                .param("updatedAt", Timestamp.from(help.updatedAt()))
                .update();
    }

    @Override
    public Optional<Help> findById(HelpId id) {
        return jdbc.sql(SELECT + " where help_id = :id").param("id", id.value()).query(this::map).optional();
    }

    @Override
    public boolean exists(HelpId id) {
        return jdbc.sql("select count(*) from help where help_id = :id").param("id", id.value())
                .query(Long.class).single() > 0;
    }

    @Override
    public boolean existsFor(HelpRequestId helpRequest) {
        return jdbc.sql("select count(*) from help where help_request_id = :id").param("id", helpRequest.value())
                .query(Long.class).single() > 0;
    }

    @Override
    public List<Help> search(HelpRequestId helpRequest, HelpType type, CookId helpProvider) {
        List<String> conditions = new ArrayList<>();
        if (helpRequest != null) {
            conditions.add("help_request_id = :helpRequest");
        }
        if (type != null) {
            conditions.add("answer_type = :type");
        }
        if (helpProvider != null) {
            conditions.add("help_provider = :helpProvider");
        }
        String where = conditions.isEmpty() ? "" : " where " + String.join(" and ", conditions);
        var statement = jdbc.sql(SELECT + where + " order by created_at, help_id");
        if (helpRequest != null) {
            statement = statement.param("helpRequest", helpRequest.value());
        }
        if (type != null) {
            statement = statement.param("type", type.name());
        }
        if (helpProvider != null) {
            statement = statement.param("helpProvider", helpProvider.value());
        }
        return statement.query(this::map).list();
    }

    @Override
    public void delete(HelpId id) {
        jdbc.sql("delete from help where help_id = :id").param("id", id.value()).update();
    }

    private Help map(ResultSet rs, int row) throws SQLException {
        UUID helpProvider = rs.getObject("help_provider", UUID.class);
        return Help.restore(
                new HelpId(rs.getObject("help_id", UUID.class)),
                new HelpRequestId(rs.getObject("help_request_id", UUID.class)),
                new CookId(rs.getObject("help_requester", UUID.class)),
                HelpProviderType.valueOf(rs.getString("provider_type")),
                helpProvider == null ? null : new CookId(helpProvider),
                rs.getString("answer_title"),
                read(rs.getString("answer")).toDomain(),
                rs.getTimestamp("created_at").toInstant(),
                rs.getTimestamp("updated_at").toInstant());
    }

    private String write(StoredAnswer answer) {
        try {
            return json.writeValueAsString(answer);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Answer cannot be stored as JSON", e);
        }
    }

    private StoredAnswer read(String stored) {
        try {
            return json.readValue(stored, StoredAnswer.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Stored answer cannot be read: " + stored, e);
        }
    }

    /** Storage format of all four answer kinds; unused properties stay {@code null}. */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    record StoredAnswer(
            HelpType type,
            UUID recipe,
            UUID howToStep,
            String text,
            List<URI> images,
            List<StoredSubstitute> substitutes,
            Integer servings,
            Meal meal,
            String howToServe,
            List<StoredCourse> courses) {

        static StoredAnswer of(Answer answer) {
            return switch (answer) {
                case IngredientSubstitutes s -> new StoredAnswer(s.type(), s.recipe().value(), null, null, null,
                        s.substitutes().stream().map(StoredSubstitute::of).toList(), null, null, null, null);
                case PreparationStepExplanation p -> new StoredAnswer(p.type(), p.recipe().value(),
                        p.howToStep().value(), p.description(), p.images(), null, null, null, null, null);
                case CatastropheMitigation c -> new StoredAnswer(c.type(), c.recipe().map(RecipeId::value).orElse(null),
                        null, c.explanation(), null, null, null, null, null, null);
                case MenuProposal m -> new StoredAnswer(m.type(), null, null, m.note(), null, null, m.servings(),
                        m.meal(), m.howToServe().orElse(null),
                        m.courses().stream().map(course -> new StoredCourse(course.step(), course.recipe().value()))
                                .toList());
            };
        }

        Answer toDomain() {
            return switch (type) {
                case INGREDIENT_SUBSTITUTE -> new IngredientSubstitutes(new RecipeId(recipe),
                        substitutes.stream().map(StoredSubstitute::toDomain).toList());
                case PREPARATION_STEP_EXPLANATION -> new PreparationStepExplanation(new RecipeId(recipe),
                        new HowToStepId(howToStep), text, images);
                case STEPS_TO_MITIGATE_CATASTROPHE -> new CatastropheMitigation(
                        Optional.ofNullable(recipe).map(RecipeId::new), text);
                case MENU_PROPOSAL -> new MenuProposal(text, servings, meal, Optional.ofNullable(howToServe),
                        courses.stream().map(course -> new Course(course.step(), new RecipeId(course.recipe()))).toList());
            };
        }
    }

    record StoredSubstitute(UUID ingredient, String name, BigDecimal value, Unit unit) {

        static StoredSubstitute of(Substitute substitute) {
            return new StoredSubstitute(substitute.ingredient().value(), substitute.name(), substitute.value(),
                    substitute.unit());
        }

        Substitute toDomain() {
            return new Substitute(new IngredientId(ingredient), name, value, unit);
        }
    }

    record StoredCourse(int step, UUID recipe) {
    }
}
