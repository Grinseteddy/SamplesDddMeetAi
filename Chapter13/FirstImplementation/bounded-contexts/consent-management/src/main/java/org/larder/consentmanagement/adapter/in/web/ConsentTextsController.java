package org.larder.consentmanagement.adapter.in.web;

import org.larder.consentmanagement.adapter.in.web.api.ConsentTextsApi;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST adapter for the ConsentTexts operations of the contract
 * {@code contracts/openapi/consent-management.openapi.yaml}. Operations not yet
 * overridden answer 501 Not Implemented.
 */
@RestController("consentmanagementConsentTextsController")
@RequestMapping("/consent-management")
class ConsentTextsController implements ConsentTextsApi {
}
