package org.larder.recipecatalog.adapter.in.web;

import org.larder.recipecatalog.adapter.in.web.api.HowToStepsApi;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST adapter for the HowToSteps operations of the contract
 * {@code contracts/openapi/recipe-catalog.openapi.yaml}. Operations not yet
 * overridden answer 501 Not Implemented.
 */
@RestController("recipecatalogHowToStepsController")
@RequestMapping("/recipe-catalog")
class HowToStepsController implements HowToStepsApi {
}
