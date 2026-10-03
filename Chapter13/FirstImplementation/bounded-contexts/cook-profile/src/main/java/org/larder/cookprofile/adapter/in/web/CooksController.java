package org.larder.cookprofile.adapter.in.web;

import org.larder.cookprofile.adapter.in.web.api.CooksApi;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST adapter for the Cooks operations of the contract
 * {@code contracts/openapi/cook-profile.openapi.yaml}. Operations not yet
 * overridden answer 501 Not Implemented.
 */
@RestController("cookprofileCooksController")
@RequestMapping("/cook-profile")
class CooksController implements CooksApi {
}
