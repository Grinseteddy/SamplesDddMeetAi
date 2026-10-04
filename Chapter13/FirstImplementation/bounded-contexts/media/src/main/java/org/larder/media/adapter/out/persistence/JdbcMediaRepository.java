package org.larder.media.adapter.out.persistence;

import java.net.URI;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.larder.media.application.MediaRepository;
import org.larder.media.domain.BusinessObjectId;
import org.larder.media.domain.ImageFormat;
import org.larder.media.domain.Link;
import org.larder.media.domain.LinkType;
import org.larder.media.domain.Media;
import org.larder.media.domain.MediaId;
import org.larder.media.domain.UploaderId;
import org.springframework.jdbc.core.simple.JdbcClient;

class JdbcMediaRepository implements MediaRepository {

    private static final String SELECT = """
            select media_id, uploader, content_type, size_bytes, uploaded_at
              from media
            """;

    private final JdbcClient jdbc;

    JdbcMediaRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void add(Media media) {
        jdbc.sql("""
                insert into media (media_id, uploader, content_type, size_bytes, uploaded_at)
                values (:id, :uploader, :contentType, :size, :uploadedAt)
                """)
                .param("id", media.id().value())
                .param("uploader", media.uploader().value())
                .param("contentType", media.format().contentType())
                .param("size", media.size())
                .param("uploadedAt", Timestamp.from(media.uploadedAt()))
                .update();
        int position = 0;
        for (Link link : media.links()) {
            jdbc.sql("""
                    insert into media_link (media_id, position, link_type, url, business_object_id)
                    values (:id, :position, :type, :url, :businessObjectId)
                    """)
                    .param("id", media.id().value())
                    .param("position", position++)
                    .param("type", link.type().name())
                    .param("url", link.url().toString())
                    .param("businessObjectId", link.businessObjectId().value())
                    .update();
        }
    }

    @Override
    public Optional<Media> findById(MediaId id) {
        return jdbc.sql(SELECT + " where media_id = :id").param("id", id.value()).query(this::map).optional();
    }

    @Override
    public List<Media> findByBusinessObject(LinkType type, BusinessObjectId businessObject) {
        return jdbc.sql(SELECT + """
                 where media_id in (select media_id from media_link
                                     where link_type = :type and business_object_id = :businessObjectId)
                 order by uploaded_at, media_id
                """)
                .param("type", type.name())
                .param("businessObjectId", businessObject.value())
                .query(this::map).list();
    }

    @Override
    public void remove(MediaId id) {
        jdbc.sql("delete from media where media_id = :id").param("id", id.value()).update();
    }

    private Media map(ResultSet rs, int row) throws SQLException {
        UUID id = rs.getObject("media_id", UUID.class);
        return Media.restore(
                new MediaId(id),
                new UploaderId(rs.getObject("uploader", UUID.class)),
                ImageFormat.ofContentType(rs.getString("content_type")),
                rs.getLong("size_bytes"),
                links(id),
                rs.getTimestamp("uploaded_at").toInstant());
    }

    private List<Link> links(UUID mediaId) {
        return jdbc.sql("select link_type, url from media_link where media_id = :id order by position")
                .param("id", mediaId)
                .query((rs, row) -> new Link(LinkType.valueOf(rs.getString("link_type")), URI.create(rs.getString("url"))))
                .list();
    }
}
