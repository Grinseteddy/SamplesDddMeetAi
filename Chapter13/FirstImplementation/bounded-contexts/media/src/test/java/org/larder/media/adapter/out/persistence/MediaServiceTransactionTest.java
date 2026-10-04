package org.larder.media.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.larder.media.TestData.COOK;
import static org.larder.media.TestData.NOW;
import static org.larder.media.TestData.PNG;
import static org.larder.media.TestData.RECIPE;

import java.time.Clock;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.larder.media.application.ImageStore;
import org.larder.media.application.MediaService;
import org.larder.media.domain.ImageContent;
import org.larder.media.domain.Media;
import org.larder.media.domain.MediaId;
import org.larder.platform.test.TestDatabase;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/** The bucket/database ordering of {@link MediaService} inside real transactions of the media schema. */
class MediaServiceTransactionTest {

    private final Map<MediaId, ImageContent> objects = new HashMap<>();
    private JdbcMediaRepository media;
    private MediaService service;
    private TransactionTemplate transaction;

    @BeforeEach
    void freshSchema() {
        var database = TestDatabase.forSchema("media");
        media = new JdbcMediaRepository(database.jdbcClient());
        service = new MediaService(media, new MapImageStore(), Clock.fixed(NOW, ZoneOffset.UTC));
        transaction = new TransactionTemplate(new DataSourceTransactionManager(database.dataSource()));
    }

    @Test
    void aRolledBackUploadRemovesItsObject() {
        assertThatThrownBy(() -> transaction.executeWithoutResult(status -> {
            service.upload(COOK, PNG, List.of(RECIPE));
            assertThat(objects).hasSize(1);
            throw new IllegalStateException("rollback");
        })).isInstanceOf(IllegalStateException.class);

        assertThat(objects).isEmpty();
    }

    @Test
    void theObjectIsDeletedOnlyAfterTheMetadataDeletionCommitted() {
        Media uploaded = transaction.execute(status -> service.upload(COOK, PNG, List.of(RECIPE)));

        assertThatThrownBy(() -> transaction.executeWithoutResult(status -> {
            service.delete(COOK, uploaded.id());
            throw new IllegalStateException("rollback");
        })).isInstanceOf(IllegalStateException.class);
        assertThat(media.findById(uploaded.id())).isPresent();
        assertThat(objects).containsKey(uploaded.id());

        transaction.executeWithoutResult(status -> {
            service.delete(COOK, uploaded.id());
            assertThat(objects).containsKey(uploaded.id());
        });
        assertThat(media.findById(uploaded.id())).isEmpty();
        assertThat(objects).isEmpty();
    }

    private class MapImageStore implements ImageStore {
        @Override
        public void put(MediaId id, ImageContent content) {
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
