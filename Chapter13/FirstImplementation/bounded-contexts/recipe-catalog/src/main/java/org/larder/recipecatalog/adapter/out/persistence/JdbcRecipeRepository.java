package org.larder.recipecatalog.adapter.out.persistence;

import java.math.BigDecimal;
import java.net.URI;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.larder.recipecatalog.application.RecipeRepository;
import org.larder.recipecatalog.domain.CookId;
import org.larder.recipecatalog.domain.Diet;
import org.larder.recipecatalog.domain.HowToStep;
import org.larder.recipecatalog.domain.HowToStepId;
import org.larder.recipecatalog.domain.Ingredient;
import org.larder.recipecatalog.domain.IngredientId;
import org.larder.recipecatalog.domain.Meal;
import org.larder.recipecatalog.domain.PreparationTime;
import org.larder.recipecatalog.domain.Quantity;
import org.larder.recipecatalog.domain.Recipe;
import org.larder.recipecatalog.domain.RecipeId;
import org.larder.recipecatalog.domain.RecipeSearch;
import org.larder.recipecatalog.domain.Unit;
import org.springframework.jdbc.core.simple.JdbcClient;

/**
 * Stores a recipe as a whole: the recipe row plus its further images, ingredients and how-to steps.
 * Saving replaces all child rows; the caller's transaction ({@code RecipeService.TRANSACTIONS})
 * makes that one unit of work.
 */
class JdbcRecipeRepository implements RecipeRepository {

    private static final String SELECT_RECIPE = """
            select r.recipe_id, r.owner, r.name, r.subtitle, r.main_image, r.preparation_minutes, r.servings, r.meal, r.diet
              from recipe r
            """;

    private final JdbcClient jdbc;

    JdbcRecipeRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void save(Recipe recipe) {
        UUID id = recipe.id().value();
        jdbc.sql("""
                insert into recipe (recipe_id, owner, name, subtitle, main_image, preparation_minutes, servings, meal, diet)
                values (:id, :owner, :name, :subtitle, :mainImage, :preparationMinutes, :servings, :meal, :diet)
                on conflict (recipe_id) do update set
                    name = excluded.name, subtitle = excluded.subtitle, main_image = excluded.main_image,
                    preparation_minutes = excluded.preparation_minutes, servings = excluded.servings,
                    meal = excluded.meal, diet = excluded.diet
                """)
                .param("id", id)
                .param("owner", recipe.owner().value())
                .param("name", recipe.name())
                .param("subtitle", recipe.subtitle().orElse(null))
                .param("mainImage", recipe.mainImage().map(URI::toString).orElse(null))
                .param("preparationMinutes", recipe.preparationTime().totalMinutes())
                .param("servings", recipe.servings())
                .param("meal", recipe.meal().name())
                .param("diet", recipe.diet().name())
                .update();

        jdbc.sql("delete from recipe_further_image where recipe_id = :id").param("id", id).update();
        jdbc.sql("delete from ingredient where recipe_id = :id").param("id", id).update();
        jdbc.sql("delete from how_to_step where recipe_id = :id").param("id", id).update();

        List<URI> images = recipe.furtherImages();
        for (int position = 0; position < images.size(); position++) {
            jdbc.sql("insert into recipe_further_image (recipe_id, position, image) values (:id, :position, :image)")
                    .param("id", id).param("position", position).param("image", images.get(position).toString())
                    .update();
        }
        List<Ingredient> ingredients = recipe.ingredients();
        for (int position = 0; position < ingredients.size(); position++) {
            Ingredient ingredient = ingredients.get(position);
            jdbc.sql("""
                    insert into ingredient (ingredient_id, recipe_id, position, name, value, unit)
                    values (:ingredientId, :id, :position, :name, :value, :unit)
                    """)
                    .param("ingredientId", ingredient.id().value())
                    .param("id", id)
                    .param("position", position)
                    .param("name", ingredient.name())
                    .param("value", ingredient.quantity().value())
                    .param("unit", ingredient.quantity().unit().name())
                    .update();
        }
        for (HowToStep step : recipe.howToSteps()) {
            jdbc.sql("""
                    insert into how_to_step (how_to_step_id, recipe_id, sequence_number, description, illustration)
                    values (:stepId, :id, :sequenceNumber, :description, :illustration)
                    """)
                    .param("stepId", step.id().value())
                    .param("id", id)
                    .param("sequenceNumber", step.sequenceNumber())
                    .param("description", step.description())
                    .param("illustration", step.illustration().map(URI::toString).orElse(null))
                    .update();
        }
    }

    @Override
    public Optional<Recipe> findById(RecipeId id) {
        List<RecipeRow> rows = jdbc.sql(SELECT_RECIPE + " where r.recipe_id = :id")
                .param("id", id.value()).query(this::mapRecipe).list();
        return load(rows).stream().findFirst();
    }

    /** Filters combined with AND; ingredient names match whole names ignoring case. */
    @Override
    public List<Recipe> search(RecipeSearch search) {
        var sql = new StringBuilder(SELECT_RECIPE).append(" where true");
        Map<String, Object> params = new LinkedHashMap<>();
        search.mealFilter().ifPresent(meal -> {
            sql.append(" and r.meal = :meal");
            params.put("meal", meal.name());
        });
        search.dietFilter().ifPresent(diet -> {
            sql.append(" and r.diet = :diet");
            params.put("diet", diet.name());
        });
        List<String> names = search.ingredientNames();
        for (int i = 0; i < names.size(); i++) {
            sql.append(" and exists (select 1 from ingredient i where i.recipe_id = r.recipe_id and lower(i.name) = lower(:ingredient")
                    .append(i).append("))");
            params.put("ingredient" + i, names.get(i));
        }
        sql.append(" order by r.name, r.recipe_id");
        return load(jdbc.sql(sql.toString()).params(params).query(this::mapRecipe).list());
    }

    @Override
    public void delete(RecipeId id) {
        jdbc.sql("delete from recipe where recipe_id = :id").param("id", id.value()).update();
    }

    /** Reads the child rows of all given recipes at once and assembles the aggregates. */
    private List<Recipe> load(List<RecipeRow> rows) {
        if (rows.isEmpty()) {
            return List.of();
        }
        List<UUID> ids = rows.stream().map(RecipeRow::id).toList();
        Map<UUID, List<URI>> images = group(jdbc.sql("""
                select recipe_id, image from recipe_further_image where recipe_id in (:ids) order by recipe_id, position
                """).param("ids", ids).query((rs, n) -> new Child<>(
                rs.getObject("recipe_id", UUID.class), URI.create(rs.getString("image")))).list());
        Map<UUID, List<Ingredient>> ingredients = group(jdbc.sql("""
                select recipe_id, ingredient_id, name, value, unit from ingredient
                 where recipe_id in (:ids) order by recipe_id, position
                """).param("ids", ids).query((rs, n) -> new Child<>(
                rs.getObject("recipe_id", UUID.class), mapIngredient(rs))).list());
        Map<UUID, List<HowToStep>> steps = group(jdbc.sql("""
                select recipe_id, how_to_step_id, sequence_number, description, illustration from how_to_step
                 where recipe_id in (:ids) order by recipe_id, sequence_number
                """).param("ids", ids).query((rs, n) -> new Child<>(
                rs.getObject("recipe_id", UUID.class), mapHowToStep(rs))).list());

        return rows.stream().map(row -> Recipe.restore(
                new RecipeId(row.id()), new CookId(row.owner()), row.name(), row.subtitle(), row.mainImage(),
                images.getOrDefault(row.id(), List.of()), PreparationTime.ofMinutes(row.preparationMinutes()),
                row.servings(), row.meal(), row.diet(),
                ingredients.getOrDefault(row.id(), List.of()), steps.getOrDefault(row.id(), List.of()))).toList();
    }

    private static <T> Map<UUID, List<T>> group(List<Child<T>> children) {
        Map<UUID, List<T>> grouped = new LinkedHashMap<>();
        children.forEach(child -> grouped.computeIfAbsent(child.recipeId(), id -> new ArrayList<>()).add(child.value()));
        return grouped;
    }

    private RecipeRow mapRecipe(ResultSet rs, int row) throws SQLException {
        String mainImage = rs.getString("main_image");
        return new RecipeRow(
                rs.getObject("recipe_id", UUID.class),
                rs.getObject("owner", UUID.class),
                rs.getString("name"),
                rs.getString("subtitle"),
                mainImage == null ? null : URI.create(mainImage),
                rs.getInt("preparation_minutes"),
                rs.getInt("servings"),
                Meal.valueOf(rs.getString("meal")),
                Diet.valueOf(rs.getString("diet")));
    }

    private static Ingredient mapIngredient(ResultSet rs) throws SQLException {
        // give back the value without trailing zeros, e.g. 0.5 instead of 0.50
        BigDecimal value = rs.getBigDecimal("value").stripTrailingZeros();
        if (value.scale() < 0) {
            value = value.setScale(0);
        }
        return Ingredient.restore(
                new IngredientId(rs.getObject("ingredient_id", UUID.class)),
                rs.getString("name"),
                new Quantity(value, Unit.valueOf(rs.getString("unit"))));
    }

    private static HowToStep mapHowToStep(ResultSet rs) throws SQLException {
        String illustration = rs.getString("illustration");
        return HowToStep.restore(
                new HowToStepId(rs.getObject("how_to_step_id", UUID.class)),
                rs.getInt("sequence_number"),
                rs.getString("description"),
                illustration == null ? null : URI.create(illustration));
    }

    private record RecipeRow(UUID id, UUID owner, String name, String subtitle, URI mainImage,
                             int preparationMinutes, int servings, Meal meal, Diet diet) {
    }

    private record Child<T>(UUID recipeId, T value) {
    }
}
