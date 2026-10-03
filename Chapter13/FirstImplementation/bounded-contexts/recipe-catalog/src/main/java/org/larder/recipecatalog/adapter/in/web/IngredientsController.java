package org.larder.recipecatalog.adapter.in.web;

import org.larder.recipecatalog.adapter.in.web.api.IngredientsApi;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST adapter for the Ingredients operations of the contract
 * {@code contracts/openapi/recipe-catalog.openapi.yaml}. Operations not yet
 * overridden answer 501 Not Implemented.
 */
@RestController("recipecatalogIngredientsController")
@RequestMapping("/recipe-catalog")
class IngredientsController implements IngredientsApi {
}
