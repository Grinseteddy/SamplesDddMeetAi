package org.larder.recipecatalog.adapter.in.web;

import org.larder.recipecatalog.adapter.in.web.api.MealsApi;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST adapter for the Meals operations of the contract
 * {@code contracts/openapi/recipe-catalog.openapi.yaml}. Operations not yet
 * overridden answer 501 Not Implemented.
 */
@RestController("recipecatalogMealsController")
@RequestMapping("/recipe-catalog")
class MealsController implements MealsApi {
}
