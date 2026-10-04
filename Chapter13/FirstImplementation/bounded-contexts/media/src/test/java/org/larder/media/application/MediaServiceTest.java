package org.larder.media.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.larder.media.TestData.COOK;
import static org.larder.media.TestData.HELP_REQUEST;
import static org.larder.media.TestData.NOW;
import static org.larder.media.TestData.OTHER_COOK;
import static org.larder.media.TestData.PNG;
import static org.larder.media.TestData.RECIPE;
import static org.larder.media.TestData.RECIPE_ID;

import java.time.Clock;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.larder.media.domain.BusinessObjectId;
import org.larder.media.domain.ImageContent;
import org.larder.media.domain.LinkType;
import org.larder.media.domain.Media;
import org.larder.media.domain.MediaId;

class MediaServiceTest {

    private final InMemoryMedia metadata = new InMemoryMedia();
    private final InMemoryImages images = new InMemoryImages();
    private final MediaService service = new MediaService(metadata, images, Clock.fixed(NOW, ZoneOffset.UTC));

    @Test
    void anUploadStoresTheBytesAndTheMetadata() {
        Media media = service.upload(COOK, PNG, List.of(RECIPE));

        assertThat(images.objects).containsEntry(media.id(), PNG);
        assertThat(metadata.findById(media.id())).isPresent();
        assertThat(media.isUploadedBy(COOK)).isTrue();
        assertThat(media.uploadedAt()).isEqualTo(NOW);
        assertThat(service.image(media.id())).isEqualTo(new Image(media, PNG));
    }

    @Test
    void failingMetadataLeaveNoObjectBehind() {
        metadata.failing = true;

        assertThatThrownBy(() -> service.upload(COOK, PNG, List.of(RECIPE))).isInstanceOf(IllegalStateException.class);
        assertThat(images.objects).isEmpty();
    }

    @Test
    void aFailingBucketStoresNoMetadata() {
        images.failing = true;

        assertThatThrownBy(() -> service.upload(COOK, PNG, List.of(RECIPE))).isInstanceOf(ImageStoreException.class);
        assertThat(metadata.store).isEmpty();
    }

    @Test
    void listsTheMediaOfOneBusinessObject() {
        Media recipePhoto = service.upload(COOK, PNG, List.of(RECIPE, HELP_REQUEST));
        service.upload(COOK, PNG, List.of(HELP_REQUEST));

        assertThat(service.imagesOf(LinkType.RECIPE, RECIPE_ID)).extracting(image -> image.media().id())
                .containsExactly(recipePhoto.id());
        assertThat(service.imagesOf(LinkType.THANKS, RECIPE_ID)).isEmpty();
    }

    @Test
    void onlyTheUploaderDeletesTheirMedia() {
        Media media = service.upload(COOK, PNG, List.of(RECIPE));

        assertThatThrownBy(() -> service.delete(OTHER_COOK, media.id())).isInstanceOf(NotPermittedException.class);
        assertThat(images.objects).containsKey(media.id());

        service.delete(COOK, media.id());
        assertThat(metadata.findById(media.id())).isEmpty();
        assertThat(images.objects).doesNotContainKey(media.id());
    }

    @Test
    void unknownMediaAreNotFound() {
        assertThatThrownBy(() -> service.image(MediaId.newId())).isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> service.delete(COOK, MediaId.newId())).isInstanceOf(NotFoundException.class);
    }

    @Test
    void metadataWithoutBytesAreAStorageFailure() {
        Media media = service.upload(COOK, PNG, List.of(RECIPE));
        images.objects.clear();

        assertThatThrownBy(() -> service.image(media.id())).isInstanceOf(ImageStoreException.class);
    }

    static class InMemoryMedia implements MediaRepository {
        private final Map<MediaId, Media> store = new HashMap<>();
        boolean failing;

        @Override
        public void add(Media media) {
            if (failing) {
                throw new IllegalStateException("database down");
            }
            store.put(media.id(), media);
        }

        @Override
        public Optional<Media> findById(MediaId id) {
            return Optional.ofNullable(store.get(id));
        }

        @Override
        public List<Media> findByBusinessObject(LinkType type, BusinessObjectId businessObject) {
            return store.values().stream().filter(media -> media.isUsedIn(type, businessObject)).toList();
        }

        @Override
        public void remove(MediaId id) {
            store.remove(id);
        }
    }

    static class InMemoryImages implements ImageStore {
        private final Map<MediaId, ImageContent> objects = new HashMap<>();
        boolean failing;

        @Override
        public void put(MediaId id, ImageContent content) {
            if (failing) {
                throw new ImageStoreException("bucket down");
            }
            objects.put(id, content);
        }

        @Override
        public Optional<ImageContent> get(MediaId id) {
            return Optional.ofNullable(objects.get(id));
        }

        @Override
        public void delete(MediaId id) {
            objects.remove(id);
        }
    }
}
