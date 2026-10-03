package org.larder.sharing.adapter.in.web;

import org.larder.sharing.adapter.in.web.api.ThanksApi;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST adapter for the Thanks operations of the contract
 * {@code contracts/openapi/sharing.openapi.yaml}. Operations not yet
 * overridden answer 501 Not Implemented.
 */
@RestController("sharingThanksController")
@RequestMapping("/sharing")
class ThanksController implements ThanksApi {
}
