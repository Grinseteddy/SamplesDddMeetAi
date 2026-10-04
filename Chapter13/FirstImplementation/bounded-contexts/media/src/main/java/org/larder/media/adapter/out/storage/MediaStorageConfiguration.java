package org.larder.media.adapter.out.storage;

import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.checksums.RequestChecksumCalculation;
import software.amazon.awssdk.core.checksums.ResponseChecksumValidation;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * The S3-compatible bucket of Media (ADR0001), configured by {@code larder.media.storage.*}.
 * Path-style access so it works with S3Mock and MinIO; the bucket is created at startup if missing.
 */
@Configuration(proxyBeanMethods = false)
class MediaStorageConfiguration {

    @Bean
    MediaStorageProperties mediaStorageProperties(Environment environment) {
        return Binder.get(environment).bindOrCreate(MediaStorageProperties.PREFIX, MediaStorageProperties.class);
    }

    @Bean
    S3Client mediaS3Client(MediaStorageProperties mediaStorageProperties) {
        return s3Client(mediaStorageProperties);
    }

    @Bean
    S3ImageStore mediaImageStore(S3Client mediaS3Client, MediaStorageProperties mediaStorageProperties) {
        var store = new S3ImageStore(mediaS3Client, mediaStorageProperties.bucket());
        store.ensureBucket();
        return store;
    }

    static S3Client s3Client(MediaStorageProperties properties) {
        return S3Client.builder()
                .endpointOverride(properties.endpoint())
                .region(Region.of(properties.region()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(properties.accessKey(), properties.secretKey())))
                .forcePathStyle(true)
                // checksums only where S3 requires them: not every S3-compatible store supports the newer defaults
                .requestChecksumCalculation(RequestChecksumCalculation.WHEN_REQUIRED)
                .responseChecksumValidation(ResponseChecksumValidation.WHEN_REQUIRED)
                .build();
    }
}
