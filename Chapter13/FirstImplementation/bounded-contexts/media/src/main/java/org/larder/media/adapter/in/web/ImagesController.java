package org.larder.media.adapter.in.web;

import org.larder.media.adapter.in.web.api.ImagesApi;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST adapter for the Images operations of the contract
 * {@code contracts/openapi/media.openapi.yaml}. Operations not yet
 * overridden answer 501 Not Implemented.
 */
@RestController("mediaImagesController")
@RequestMapping("/media")
class ImagesController implements ImagesApi {
}
