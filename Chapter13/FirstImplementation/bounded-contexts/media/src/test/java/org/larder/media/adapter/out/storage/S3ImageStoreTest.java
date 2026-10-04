package org.larder.media.adapter.out.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.larder.media.TestData.PNG;

import java.net.URI;
import java.util.UUID;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.larder.media.application.ImageStoreException;
import org.larder.media.domain.MediaId;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import software.amazon.awssdk.services.s3.S3Client;

/** The S3 adapter against Adobe S3Mock (path-style, any credentials). */
class S3ImageStoreTest {

    private static final GenericContainer<?> S3 = new GenericContainer<>(DockerImageName.parse("adobe/s3mock:5.2.3"))
            .withExposedPorts(9090)
            .waitingFor(Wait.forHttp("/").forPort(9090).forStatusCode(200));

    private static S3Client s3;
    private S3ImageStore store;
    private String bucket;

    @BeforeAll
    static void startS3() {
        S3.start();
        s3 = MediaStorageConfiguration.s3Client(new MediaStorageProperties(
                URI.create("http://" + S3.getHost() + ":" + S3.getMappedPort(9090)), "us-east-1", "unused", "larder", "larder"));
    }

    @AfterAll
    static void stopS3() {
        s3.close();
        S3.stop();
    }

    @BeforeEach
    void freshBucket() {
        bucket = "larder-media-" + UUID.randomUUID();
        store = new S3ImageStore(s3, bucket);
        store.ensureBucket();
    }

    @Test
    void createsTheMissingBucketAndAcceptsAnExistingOne() {
        assertThat(s3.listBuckets().buckets()).anyMatch(b -> b.name().equals(bucket));

        store.ensureBucket();
    }

    @Test
    void storesReadsAndDeletesAnImageUnderItsMediaId() {
        MediaId id = MediaId.newId();

        store.put(id, PNG);

        assertThat(store.get(id)).contains(PNG);
        assertThat(s3.headObject(r -> r.bucket(bucket).key(id.value().toString())).contentType()).isEqualTo("image/png");

        store.delete(id);
        assertThat(store.get(id)).isEmpty();
    }

    @Test
    void aMissingImageIsEmptyAndDeletingItIsNoError() {
        MediaId id = MediaId.newId();

        assertThat(store.get(id)).isEmpty();
        store.delete(id);
    }

    @Test
    void aMissingBucketIsCreatedOnceAndThenUsed() {
        var fresh = new S3ImageStore(s3, "larder-media-" + java.util.UUID.randomUUID());

        fresh.ensureBucket();
        fresh.ensureBucket();
        MediaId id = MediaId.newId();
        fresh.put(id, PNG);

        assertThat(fresh.get(id)).contains(PNG);
    }

    @Test
    void anUnreachableBucketIsAStorageFailure() {
        try (S3Client unreachable = MediaStorageConfiguration.s3Client(new MediaStorageProperties(
                URI.create("http://localhost:1"), "us-east-1", "larder-media", "larder", "larder"))) {
            var broken = new S3ImageStore(unreachable, "larder-media");

            assertThatThrownBy(() -> broken.put(MediaId.newId(), PNG)).isInstanceOf(ImageStoreException.class);
            assertThatThrownBy(() -> broken.get(MediaId.newId())).isInstanceOf(ImageStoreException.class);
            assertThatThrownBy(() -> broken.delete(MediaId.newId())).isInstanceOf(ImageStoreException.class);
            assertThatThrownBy(broken::ensureBucket).isInstanceOf(ImageStoreException.class);
        }
    }
}
