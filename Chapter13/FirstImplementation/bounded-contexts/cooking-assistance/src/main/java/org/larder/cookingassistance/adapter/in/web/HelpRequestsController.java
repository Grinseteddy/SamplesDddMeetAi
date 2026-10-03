package org.larder.cookingassistance.adapter.in.web;

import org.larder.cookingassistance.adapter.in.web.api.HelpRequestsApi;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST adapter for the HelpRequests operations of the contract
 * {@code contracts/openapi/cooking-assistance.openapi.yaml}. Operations not yet
 * overridden answer 501 Not Implemented.
 */
@RestController("cookingassistanceHelpRequestsController")
@RequestMapping("/cooking-assistance")
class HelpRequestsController implements HelpRequestsApi {
}
