package org.larder.media.adapter.out.storage;

import java.util.Optional;

import org.larder.media.application.ImageStore;
import org.larder.media.application.ImageStoreException;
import org.larder.media.domain.ImageContent;
import org.larder.media.domain.MediaId;

import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.S3Exception;

/** The image bytes as objects in an S3-compatible bucket, key = media id (ADR0001). */
class S3ImageStore implements ImageStore {

    private final S3Client s3;
    private final String bucket;

    S3ImageStore(S3Client s3, String bucket) {
        this.s3 = s3;
        this.bucket = bucket;
    }

    /** Creates the bucket if it does not exist yet. */
    void ensureBucket() {
        try {
            s3.headBucket(request -> request.bucket(bucket));
        } catch (NoSuchBucketException e) {
            createBucket();
        } catch (S3Exception e) {
            if (e.statusCode() != 404) {
                throw new ImageStoreException("Cannot access bucket " + bucket, e);
            }
            createBucket();
        } catch (SdkException e) {
            throw new ImageStoreException("Cannot access bucket " + bucket, e);
        }
    }

    private void createBucket() {
        try {
            s3.createBucket(request -> request.bucket(bucket));
        } catch (SdkException e) {
            throw new ImageStoreException("Cannot create bucket " + bucket, e);
        }
    }

    @Override
    public void put(MediaId id, ImageContent content) {
        try {
            s3.putObject(request -> request.bucket(bucket).key(key(id)).contentType(content.format().contentType()),
                    RequestBody.fromBytes(content.bytes()));
        } catch (SdkException e) {
            throw new ImageStoreException("Cannot store the image of media " + id.value(), e);
        }
    }

    @Override
    public Optional<ImageContent> get(MediaId id) {
        try {
            byte[] bytes = s3.getObjectAsBytes(request -> request.bucket(bucket).key(key(id))).asByteArray();
            return Optional.of(ImageContent.of(bytes));
        } catch (NoSuchKeyException e) {
            return Optional.empty();
        } catch (SdkException e) {
            throw new ImageStoreException("Cannot read the image of media " + id.value(), e);
        }
    }

    @Override
    public void delete(MediaId id) {
        try {
            s3.deleteObject(request -> request.bucket(bucket).key(key(id)));
        } catch (SdkException e) {
            throw new ImageStoreException("Cannot delete the image of media " + id.value(), e);
        }
    }

    private static String key(MediaId id) {
        return id.value().toString();
    }
}
