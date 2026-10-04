package org.larder.media.application;

import java.time.Clock;
import java.util.List;

import org.larder.media.domain.BusinessObjectId;
import org.larder.media.domain.ImageContent;
import org.larder.media.domain.Link;
import org.larder.media.domain.LinkType;
import org.larder.media.domain.Media;
import org.larder.media.domain.MediaId;
import org.larder.media.domain.UploaderId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Use cases of Media. Every authenticated cook with read scope may read all media (other contexts
 * show them); a cook uploads media as themselves and deletes only media they uploaded.
 *
 * <p><b>Consistency between bucket and database.</b> There is no transaction spanning both, so the
 * order of writes guarantees that metadata never point to missing bytes; the only possible
 * leftover is an orphaned object in the bucket, which is invisible through the API:
 * <ul>
 *   <li>Upload: the object is written first, then the metadata. If the metadata transaction
 *       rolls back, the object is deleted again (best effort).</li>
 *   <li>Delete: the metadata are removed first; the object is deleted only after that
 *       transaction committed (best effort; a failure leaves an orphaned object).</li>
 * </ul>
 */
@Service
public class MediaService {

    /** Transaction manager of this context's own schema. */
    public static final String TRANSACTIONS = "mediaTransactionManager";

    private static final Logger LOG = LoggerFactory.getLogger(MediaService.class);

    private final MediaRepository media;
    private final ImageStore images;
    private final Clock clock;

    public MediaService(MediaRepository media, ImageStore images, Clock clock) {
        this.media = media;
        this.images = images;
        this.clock = clock;
    }

    @Transactional(transactionManager = MediaService.TRANSACTIONS)
    public Media upload(UploaderId caller, ImageContent content, List<Link> links) {
        Media uploaded = Media.upload(caller, content, links, clock.instant());
        images.put(uploaded.id(), content);
        afterRollback(() -> discard(uploaded.id()));
        try {
            media.add(uploaded);
        } catch (RuntimeException e) {
            if (!TransactionSynchronizationManager.isSynchronizationActive()) {
                discard(uploaded.id());
            }
            throw e;
        }
        return uploaded;
    }

    @Transactional(transactionManager = MediaService.TRANSACTIONS)
    public void delete(UploaderId caller, MediaId id) {
        Media existing = metadata(id);
        if (!existing.isUploadedBy(caller)) {
            throw new NotPermittedException("A cook can only delete media they uploaded");
        }
        media.remove(id);
        afterCommit(() -> discard(id));
    }

    public Image image(MediaId id) {
        return withContent(metadata(id));
    }

    public List<Image> imagesOf(LinkType type, BusinessObjectId businessObject) {
        return media.findByBusinessObject(type, businessObject).stream().map(this::withContent).toList();
    }

    private Media metadata(MediaId id) {
        return media.findById(id).orElseThrow(() -> new NotFoundException("Media " + id.value() + " not found"));
    }

    private Image withContent(Media metadata) {
        return images.get(metadata.id())
                .map(content -> new Image(metadata, content))
                .orElseThrow(() -> new ImageStoreException("The bucket holds no image for media " + metadata.id().value()));
    }

    private void discard(MediaId id) {
        try {
            images.delete(id);
        } catch (RuntimeException e) {
            LOG.warn("Could not delete the image of media {} from the bucket; it is orphaned", id.value(), e);
        }
    }

    /** Runs {@code action} after the current transaction committed, or at once without a transaction. */
    private static void afterCommit(Runnable action) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            action.run();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                action.run();
            }
        });
    }

    /** Runs {@code action} if the current transaction rolls back; does nothing without a transaction. */
    private static void afterRollback(Runnable action) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status != STATUS_COMMITTED) {
                    action.run();
                }
            }
        });
    }
}
