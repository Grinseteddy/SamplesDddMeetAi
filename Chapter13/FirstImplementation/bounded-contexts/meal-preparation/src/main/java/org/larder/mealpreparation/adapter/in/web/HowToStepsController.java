package org.larder.mealpreparation.adapter.in.web;

import org.larder.mealpreparation.adapter.in.web.api.HowToStepsApi;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST adapter for the HowToSteps operations of the contract
 * {@code contracts/openapi/meal-preparation.openapi.yaml}. Operations not yet
 * overridden answer 501 Not Implemented.
 */
@RestController("mealpreparationHowToStepsController")
@RequestMapping("/meal-preparation")
class HowToStepsController implements HowToStepsApi {
}
