package org.larder.mealpreparation.adapter.out.persistence;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

import org.larder.mealpreparation.application.MealPreparationRepository;
import org.larder.mealpreparation.application.MealPreparationService;
import org.larder.mealpreparation.domain.CookId;
import org.larder.mealpreparation.domain.HowToStep;
import org.larder.mealpreparation.domain.HowToStepId;
import org.larder.mealpreparation.domain.MealPreparation;
import org.larder.mealpreparation.domain.PreparationId;
import org.larder.mealpreparation.domain.RecipeId;
import org.larder.mealpreparation.domain.RecipeSteps;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.annotation.Transactional;

class JdbcMealPreparationRepository implements MealPreparationRepository {

    private static final String SELECT = """
            select preparation_id, cook, recipe_id, current_step_id
              from meal_preparation
             where preparation_id = :id
            """;

    private final JdbcClient jdbc;

    JdbcMealPreparationRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Atomic on its own: starting a preparation stores it outside a use-case transaction, because the
     * use case calls Recipe Catalog first. The snapshot of steps is written once and never updated.
     */
    @Override
    @Transactional(transactionManager = MealPreparationService.TRANSACTIONS)
    public void save(MealPreparation preparation) {
        jdbc.sql("""
                insert into meal_preparation (preparation_id, cook, recipe_id, current_step_id)
                values (:id, :cook, :recipe, :currentStep)
                on conflict (preparation_id) do update set current_step_id = excluded.current_step_id
                """)
                .param("id", preparation.id().value())
                .param("cook", preparation.cook().value())
                .param("recipe", preparation.recipe().value())
                .param("currentStep", preparation.currentStep().id().value())
                .update();
        for (HowToStep step : preparation.steps().steps()) {
            jdbc.sql("""
                    insert into meal_preparation_step (preparation_id, how_to_step_id, sequence_number)
                    values (:id, :stepId, :sequenceNumber)
                    on conflict do nothing
                    """)
                    .param("id", preparation.id().value())
                    .param("stepId", step.id().value())
                    .param("sequenceNumber", step.sequenceNumber())
                    .update();
        }
    }

    @Override
    public Optional<MealPreparation> findById(PreparationId id) {
        return find(SELECT, id);
    }

    @Override
    public Optional<MealPreparation> findByIdForMove(PreparationId id) {
        return find(SELECT + " for update", id);
    }

    private Optional<MealPreparation> find(String sql, PreparationId id) {
        return jdbc.sql(sql).param("id", id.value()).query(this::map).optional();
    }

    private MealPreparation map(ResultSet rs, int row) throws SQLException {
        var id = new PreparationId(rs.getObject("preparation_id", UUID.class));
        return MealPreparation.restore(
                id,
                new CookId(rs.getObject("cook", UUID.class)),
                new RecipeId(rs.getObject("recipe_id", UUID.class)),
                stepsOf(id),
                new HowToStepId(rs.getObject("current_step_id", UUID.class)));
    }

    private RecipeSteps stepsOf(PreparationId id) {
        return new RecipeSteps(jdbc.sql("""
                        select how_to_step_id, sequence_number
                          from meal_preparation_step
                         where preparation_id = :id
                         order by sequence_number
                        """)
                .param("id", id.value())
                .query((rs, row) -> new HowToStep(
                        new HowToStepId(rs.getObject("how_to_step_id", UUID.class)), rs.getInt("sequence_number")))
                .list());
    }
}
