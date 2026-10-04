package org.larder.sharing.adapter.out.media;

import org.larder.platform.web.UpstreamClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.client.RestClient;

/** Media as seen from Sharing; base URL from {@code larder.sharing.upstream.media.base-url}. */
@Configuration(proxyBeanMethods = false)
class MediaClientConfiguration {

    @Bean
    MediaPictures sharingPictures(RestClient.Builder restClientBuilder, Environment environment) {
        return MediaPictures.create(restClientBuilder, UpstreamClients.baseUrl(environment, "sharing", "media"));
    }
}
