package org.larder.mealplanning.adapter.out.persistence;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.larder.mealplanning.application.MealPlanRepository;
import org.larder.mealplanning.domain.CookId;
import org.larder.mealplanning.domain.Course;
import org.larder.mealplanning.domain.Diet;
import org.larder.mealplanning.domain.Meal;
import org.larder.mealplanning.domain.MealPlan;
import org.larder.mealplanning.domain.MealPlanId;
import org.larder.mealplanning.domain.MealPlanSearch;
import org.larder.mealplanning.domain.RecipeId;
import org.springframework.jdbc.core.simple.JdbcClient;

/**
 * Stores a meal plan as a whole: the meal plan row plus its courses. Saving replaces all courses;
 * the caller's transaction ({@code MealPlanService.TRANSACTIONS}) makes that one unit of work.
 */
class JdbcMealPlanRepository implements MealPlanRepository {

    private static final String SELECT_MEAL_PLAN = """
            select p.meal_plan_id, p.owner, p.occasion, p.servings, p.meal, p.how_to_serve
              from meal_plan p
            """;

    private final JdbcClient jdbc;

    JdbcMealPlanRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void save(MealPlan mealPlan) {
        UUID id = mealPlan.id().value();
        jdbc.sql("""
                insert into meal_plan (meal_plan_id, owner, occasion, servings, meal, how_to_serve)
                values (:id, :owner, :occasion, :servings, :meal, :howToServe)
                on conflict (meal_plan_id) do update set
                    occasion = excluded.occasion, servings = excluded.servings,
                    meal = excluded.meal, how_to_serve = excluded.how_to_serve
                """)
                .param("id", id)
                .param("owner", mealPlan.owner().value())
                .param("occasion", mealPlan.occasion().orElse(null))
                .param("servings", mealPlan.servings().orElse(null))
                .param("meal", mealPlan.meal().map(Meal::name).orElse(null))
                .param("howToServe", mealPlan.howToServe().orElse(null))
                .update();

        jdbc.sql("delete from course where meal_plan_id = :id").param("id", id).update();
        List<Course> courses = mealPlan.courses();
        for (int position = 0; position < courses.size(); position++) {
            Course course = courses.get(position);
            jdbc.sql("""
                    insert into course (meal_plan_id, position, step, recipe_id, diet)
                    values (:id, :position, :step, :recipe, :diet)
                    """)
                    .param("id", id)
                    .param("position", position)
                    .param("step", course.step())
                    .param("recipe", course.recipe().value())
                    .param("diet", course.diet().name())
                    .update();
        }
    }

    @Override
    public Optional<MealPlan> findById(MealPlanId id) {
        return load(jdbc.sql(SELECT_MEAL_PLAN + " where p.meal_plan_id = :id")
                .param("id", id.value()).query(this::mapMealPlan).list()).stream().findFirst();
    }

    /**
     * Filters combined with AND. Diet: the plan has courses, and none of them is less restrictive than
     * the requested diet. Occasion: equal ignoring case.
     */
    @Override
    public List<MealPlan> search(CookId owner, MealPlanSearch search) {
        var sql = new StringBuilder(SELECT_MEAL_PLAN).append(" where p.owner = :owner");
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("owner", owner.value());
        search.dietFilter().ifPresent(diet -> {
            sql.append(" and exists (select 1 from course c where c.meal_plan_id = p.meal_plan_id)")
                    .append(" and not exists (select 1 from course c where c.meal_plan_id = p.meal_plan_id")
                    .append(" and c.diet not in (:suitableDiets))");
            params.put("suitableDiets", diet.suitableDiets().stream().map(Diet::name).sorted().toList());
        });
        search.occasionFilter().ifPresent(occasion -> {
            sql.append(" and lower(p.occasion) = lower(:occasion)");
            params.put("occasion", occasion);
        });
        sql.append(" order by p.occasion nulls last, p.meal_plan_id");
        return load(jdbc.sql(sql.toString()).params(params).query(this::mapMealPlan).list());
    }

    @Override
    public void delete(MealPlanId id) {
        jdbc.sql("delete from meal_plan where meal_plan_id = :id").param("id", id.value()).update();
    }

    /** Reads the courses of all given meal plans at once and assembles the aggregates. */
    private List<MealPlan> load(List<MealPlanRow> rows) {
        if (rows.isEmpty()) {
            return List.of();
        }
        List<UUID> ids = rows.stream().map(MealPlanRow::id).toList();
        Map<UUID, List<Course>> courses = new LinkedHashMap<>();
        jdbc.sql("""
                select meal_plan_id, step, recipe_id, diet from course
                 where meal_plan_id in (:ids) order by meal_plan_id, position
                """).param("ids", ids)
                .query((rs, n) -> new CourseRow(rs.getObject("meal_plan_id", UUID.class), new Course(
                        rs.getInt("step"), new RecipeId(rs.getObject("recipe_id", UUID.class)),
                        Diet.valueOf(rs.getString("diet")))))
                .list()
                .forEach(row -> courses.computeIfAbsent(row.mealPlanId(), key -> new ArrayList<>()).add(row.course()));
        return rows.stream().map(row -> MealPlan.restore(
                new MealPlanId(row.id()), new CookId(row.owner()), row.occasion(), row.servings(), row.meal(),
                row.howToServe(), courses.getOrDefault(row.id(), List.of()))).toList();
    }

    private MealPlanRow mapMealPlan(ResultSet rs, int row) throws SQLException {
        String meal = rs.getString("meal");
        return new MealPlanRow(
                rs.getObject("meal_plan_id", UUID.class),
                rs.getObject("owner", UUID.class),
                rs.getString("occasion"),
                rs.getObject("servings", Integer.class),
                meal == null ? null : Meal.valueOf(meal),
                rs.getString("how_to_serve"));
    }

    private record CourseRow(UUID mealPlanId, Course course) {
    }

    private record MealPlanRow(UUID id, UUID owner, String occasion, Integer servings, Meal meal, String howToServe) {
    }
}
