package org.larder.sharing.adapter.out.media;

import org.larder.platform.security.BearerTokenRelay;
import org.larder.platform.web.UpstreamClients;
import org.larder.sharing.adapter.out.media.client.ApiClient;
import org.larder.sharing.adapter.out.media.client.api.ImagesApi;
import org.larder.sharing.application.NotPermittedException;
import org.larder.sharing.application.Pictures;
import org.larder.sharing.application.UpstreamUnavailableException;
import org.larder.sharing.domain.MediaId;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Anticorruption layer to Media: asks {@code GET /images/{mediaId}} through the client generated from
 * {@code contracts/openapi/media.openapi.yaml} with the calling cook's own token whether an image exists.
 * The contract has no cheaper existence check, so the image content is transferred and dropped.
 */
class MediaPictures implements Pictures {

    /** Version of the Media contract this client is generated from. */
    static final String VERSION = "1.0.0";

    private final ImagesApi images;

    MediaPictures(ImagesApi images) {
        this.images = images;
    }

    /** A client for Media at {@code baseUrl} that relays the caller's bearer token. */
    static MediaPictures create(RestClient.Builder restClientBuilder, String baseUrl) {
        var apiClient = new ApiClient(UpstreamClients.restClient(restClientBuilder, baseUrl, new BearerTokenRelay()));
        // The generated client builds absolute URLs from its own base path (the contract's server URL).
        apiClient.setBasePath(baseUrl);
        return new MediaPictures(new ImagesApi(apiClient));
    }

    @Override
    public boolean exists(MediaId image) {
        try {
            var media = images.getImageById(image.value(), VERSION);
            return media != null && image.value().equals(media.getMediaId());
        } catch (HttpClientErrorException.NotFound e) {
            return false;
        } catch (HttpClientErrorException.Forbidden e) {
            throw new NotPermittedException("Media does not show image " + image.value() + " to the cook");
        } catch (RestClientException e) {
            throw new UpstreamUnavailableException("Media cannot tell whether image " + image.value() + " exists: "
                    + e.getMessage(), e);
        }
    }
}
