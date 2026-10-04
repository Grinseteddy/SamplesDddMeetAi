package org.larder.cookingassistance.adapter.out.persistence;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.larder.cookingassistance.application.HelpRequestRepository;
import org.larder.cookingassistance.domain.CookId;
import org.larder.cookingassistance.domain.HelpProviderType;
import org.larder.cookingassistance.domain.HelpRequest;
import org.larder.cookingassistance.domain.HelpRequestId;
import org.larder.cookingassistance.domain.HelpRequestStatus;
import org.larder.cookingassistance.domain.HelpType;
import org.larder.cookingassistance.domain.HowToStepId;
import org.larder.cookingassistance.domain.IngredientId;
import org.larder.cookingassistance.domain.RecipeId;
import org.springframework.jdbc.core.simple.JdbcClient;

/** Help requests in {@code help_request} with their ingredients and preferred providers as child rows. */
class JdbcHelpRequestRepository implements HelpRequestRepository {

    private static final String SELECT = """
            select help_request_id, requester, title, type, description, recipe_id, how_to_step_id, status,
                   created_at, updated_at
              from help_request
            """;

    private final JdbcClient jdbc;

    JdbcHelpRequestRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    /** Runs inside the use case's transaction; the children are replaced as a whole. */
    @Override
    public void save(HelpRequest request) {
        UUID id = request.id().value();
        jdbc.sql("""
                insert into help_request (help_request_id, requester, title, type, description, recipe_id,
                                          how_to_step_id, status, created_at, updated_at)
                values (:id, :requester, :title, :type, :description, :recipe, :howToStep, :status, :createdAt, :updatedAt)
                on conflict (help_request_id) do update
                   set title = excluded.title, description = excluded.description, recipe_id = excluded.recipe_id,
                       how_to_step_id = excluded.how_to_step_id, status = excluded.status,
                       updated_at = excluded.updated_at
                """)
                .param("id", id)
                .param("requester", request.requester().value())
                .param("title", request.title())
                .param("type", request.type().name())
                .param("description", request.description())
                .param("recipe", request.recipe().map(RecipeId::value).orElse(null))
                .param("howToStep", request.howToStep().map(HowToStepId::value).orElse(null))
                .param("status", request.status().name())
                .param("createdAt", Timestamp.from(request.createdAt()))
                .param("updatedAt", Timestamp.from(request.updatedAt()))
                .update();

        jdbc.sql("delete from help_request_ingredient where help_request_id = :id").param("id", id).update();
        int position = 0;
        for (IngredientId ingredient : request.ingredients()) {
            jdbc.sql("""
                    insert into help_request_ingredient (help_request_id, ingredient_id, position)
                    values (:id, :ingredient, :position)
                    """)
                    .param("id", id).param("ingredient", ingredient.value()).param("position", position++).update();
        }
        jdbc.sql("delete from help_request_preferred_provider where help_request_id = :id").param("id", id).update();
        position = 0;
        for (HelpProviderType provider : request.preferredProviders()) {
            jdbc.sql("""
                    insert into help_request_preferred_provider (help_request_id, provider_type, position)
                    values (:id, :provider, :position)
                    """)
                    .param("id", id).param("provider", provider.name()).param("position", position++).update();
        }
    }

    @Override
    public Optional<HelpRequest> findById(HelpRequestId id) {
        return jdbc.sql(SELECT + " where help_request_id = :id").param("id", id.value()).query(this::row).optional()
                .map(this::restore);
    }

    @Override
    public Optional<HelpRequest> findByIdForUpdate(HelpRequestId id) {
        return jdbc.sql(SELECT + " where help_request_id = :id for update").param("id", id.value()).query(this::row)
                .optional().map(this::restore);
    }

    @Override
    public List<HelpRequest> search(CookId requester, HelpRequestStatus status) {
        List<String> conditions = new ArrayList<>();
        if (requester != null) {
            conditions.add("requester = :requester");
        }
        if (status != null) {
            conditions.add("status = :status");
        }
        String where = conditions.isEmpty() ? "" : " where " + String.join(" and ", conditions);
        var statement = jdbc.sql(SELECT + where + " order by created_at, help_request_id");
        if (requester != null) {
            statement = statement.param("requester", requester.value());
        }
        if (status != null) {
            statement = statement.param("status", status.name());
        }
        return statement.query(this::row).list().stream().map(this::restore).toList();
    }

    @Override
    public void delete(HelpRequestId id) {
        jdbc.sql("delete from help_request where help_request_id = :id").param("id", id.value()).update();
    }

    /** The row only - children are read after the row's result set is closed. */
    private Row row(ResultSet rs, int row) throws SQLException {
        UUID recipe = rs.getObject("recipe_id", UUID.class);
        UUID howToStep = rs.getObject("how_to_step_id", UUID.class);
        return new Row(
                new HelpRequestId(rs.getObject("help_request_id", UUID.class)),
                new CookId(rs.getObject("requester", UUID.class)),
                rs.getString("title"),
                HelpType.valueOf(rs.getString("type")),
                rs.getString("description"),
                recipe == null ? null : new RecipeId(recipe),
                howToStep == null ? null : new HowToStepId(howToStep),
                HelpRequestStatus.valueOf(rs.getString("status")),
                rs.getTimestamp("created_at").toInstant(),
                rs.getTimestamp("updated_at").toInstant());
    }

    private HelpRequest restore(Row row) {
        UUID id = row.id().value();
        var ingredients = new LinkedHashSet<>(jdbc.sql("""
                        select ingredient_id from help_request_ingredient where help_request_id = :id order by position
                        """)
                .param("id", id)
                .query((rs, n) -> new IngredientId(rs.getObject("ingredient_id", UUID.class))).list());
        var providers = new LinkedHashSet<>(jdbc.sql("""
                        select provider_type from help_request_preferred_provider
                         where help_request_id = :id order by position
                        """)
                .param("id", id)
                .query((rs, n) -> HelpProviderType.valueOf(rs.getString("provider_type"))).list());
        return HelpRequest.restore(row.id(), row.requester(), row.title(), row.type(), row.description(), row.recipe(),
                row.howToStep(), ingredients, providers, row.status(), row.createdAt(), row.updatedAt());
    }

    private record Row(HelpRequestId id, CookId requester, String title, HelpType type, String description,
                       RecipeId recipe, HowToStepId howToStep, HelpRequestStatus status, Instant createdAt,
                       Instant updatedAt) {
    }
}
