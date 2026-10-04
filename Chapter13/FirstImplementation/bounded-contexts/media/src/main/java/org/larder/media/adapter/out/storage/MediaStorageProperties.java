package org.larder.media.adapter.out.storage;

import java.net.URI;

import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * {@code larder.media.storage.*}: the S3-compatible bucket holding the image bytes.
 * The defaults fit the local S3Mock of {@code infra/docker-compose.yml} and are for local development only.
 */
record MediaStorageProperties(
        @DefaultValue("http://localhost:9090") URI endpoint,
        @DefaultValue("us-east-1") String region,
        @DefaultValue("larder-media") String bucket,
        @DefaultValue("larder") String accessKey,
        @DefaultValue("larder") String secretKey) {

    static final String PREFIX = "larder.media.storage";
}
