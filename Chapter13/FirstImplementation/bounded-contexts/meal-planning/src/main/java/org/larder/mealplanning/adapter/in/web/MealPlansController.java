package org.larder.mealplanning.adapter.in.web;

import org.larder.mealplanning.adapter.in.web.api.MealPlansApi;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST adapter for the MealPlans operations of the contract
 * {@code contracts/openapi/meal-planning.openapi.yaml}. Operations not yet
 * overridden answer 501 Not Implemented.
 */
@RestController("mealplanningMealPlansController")
@RequestMapping("/meal-planning")
class MealPlansController implements MealPlansApi {
}
