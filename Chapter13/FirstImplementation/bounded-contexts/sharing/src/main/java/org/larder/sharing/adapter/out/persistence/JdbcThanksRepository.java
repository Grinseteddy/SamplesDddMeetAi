package org.larder.sharing.adapter.out.persistence;

import static org.larder.sharing.domain.ThanksRuleViolationException.THANKS_ALREADY_GIVEN;

import java.net.URI;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.larder.sharing.application.ConcurrentChangeException;
import org.larder.sharing.application.ThanksFilter;
import org.larder.sharing.application.ThanksRepository;
import org.larder.sharing.application.ThanksService;
import org.larder.sharing.domain.CookId;
import org.larder.sharing.domain.HelpId;
import org.larder.sharing.domain.MediaId;
import org.larder.sharing.domain.Picture;
import org.larder.sharing.domain.Recipient;
import org.larder.sharing.domain.RecipientId;
import org.larder.sharing.domain.RecipientType;
import org.larder.sharing.domain.Thanks;
import org.larder.sharing.domain.ThanksId;
import org.larder.sharing.domain.ThanksRuleViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.annotation.Transactional;

/**
 * Thanks in schema {@code sharing}. Every write is one transaction of its own: the use cases call
 * upstream contexts first and must not hold a connection meanwhile.
 */
class JdbcThanksRepository implements ThanksRepository {

    private static final String SELECT = """
            select t.thanks_id, t.giver, t.help_id, t.thanks_text, t.picture, t.picture_media_id,
                   t.created_at, t.updated_at, t.version
              from thanks t
            """;

    private final JdbcClient jdbc;

    JdbcThanksRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    @Transactional(transactionManager = ThanksService.TRANSACTIONS)
    public void add(Thanks thanks) {
        try {
            jdbc.sql("""
                    insert into thanks (thanks_id, giver, help_id, thanks_text, picture, picture_media_id,
                                        created_at, updated_at, version)
                    values (:id, :giver, :help, :text, :picture, :mediaId, :createdAt, :updatedAt, :version)
                    """)
                    .param("id", thanks.id().value())
                    .param("giver", thanks.giver().value())
                    .param("help", thanks.help().value())
                    .param("text", thanks.text())
                    .param("picture", thanks.picture().link().toString())
                    .param("mediaId", thanks.picture().mediaId().value())
                    .param("createdAt", Timestamp.from(thanks.createdAt()))
                    .param("updatedAt", Timestamp.from(thanks.updatedAt()))
                    .param("version", thanks.version())
                    .update();
        } catch (DuplicateKeyException e) {
            throw new ThanksRuleViolationException(THANKS_ALREADY_GIVEN,
                    "Thanks for help " + thanks.help().value() + " were already given; change them instead");
        }
        insertRecipients(thanks);
    }

    @Override
    @Transactional(transactionManager = ThanksService.TRANSACTIONS)
    public void update(Thanks thanks) {
        int updated = jdbc.sql("""
                update thanks
                   set thanks_text = :text, picture = :picture, picture_media_id = :mediaId,
                       updated_at = :updatedAt, version = version + 1
                 where thanks_id = :id and version = :version
                """)
                .param("id", thanks.id().value())
                .param("text", thanks.text())
                .param("picture", thanks.picture().link().toString())
                .param("mediaId", thanks.picture().mediaId().value())
                .param("updatedAt", Timestamp.from(thanks.updatedAt()))
                .param("version", thanks.version())
                .update();
        if (updated == 0) {
            throw new ConcurrentChangeException(thanks.id());
        }
        jdbc.sql("delete from thanks_recipient where thanks_id = :id").param("id", thanks.id().value()).update();
        insertRecipients(thanks);
    }

    @Override
    @Transactional(transactionManager = ThanksService.TRANSACTIONS)
    public boolean remove(ThanksId id) {
        return jdbc.sql("delete from thanks where thanks_id = :id").param("id", id.value()).update() > 0;
    }

    @Override
    public Optional<Thanks> findById(ThanksId id) {
        return query(SELECT + " where t.thanks_id = :id", Map.of("id", id.value())).stream().findFirst();
    }

    @Override
    public boolean existsForHelp(HelpId help) {
        return jdbc.sql("select exists (select 1 from thanks where help_id = :help)")
                .param("help", help.value()).query(Boolean.class).single();
    }

    @Override
    public List<Thanks> find(ThanksFilter filter) {
        StringBuilder sql = new StringBuilder(SELECT).append(" where true");
        Map<String, Object> params = new LinkedHashMap<>();
        if (filter.giver() != null) {
            sql.append(" and t.giver = :giver");
            params.put("giver", filter.giver().value());
        }
        if (filter.mentionedCook() != null) {
            sql.append(" and exists (select 1 from thanks_recipient_cook c where c.thanks_id = t.thanks_id and c.cook = :cook)");
            params.put("cook", filter.mentionedCook().value());
        }
        sql.append(" order by t.created_at desc, t.thanks_id");
        return query(sql.toString(), params);
    }

    private void insertRecipients(Thanks thanks) {
        int position = 0;
        for (Recipient recipient : thanks.recipients()) {
            jdbc.sql("""
                    insert into thanks_recipient (recipient_id, thanks_id, position, type, chef_name)
                    values (:id, :thanks, :position, :type, :chefName)
                    """)
                    .param("id", recipient.id().value())
                    .param("thanks", thanks.id().value())
                    .param("position", position++)
                    .param("type", recipient.type().name())
                    .param("chefName", recipient.chefName())
                    .update();
            int cookPosition = 0;
            for (CookId cook : recipient.cooks()) {
                jdbc.sql("""
                        insert into thanks_recipient_cook (recipient_id, thanks_id, position, cook)
                        values (:recipient, :thanks, :position, :cook)
                        """)
                        .param("recipient", recipient.id().value())
                        .param("thanks", thanks.id().value())
                        .param("position", cookPosition++)
                        .param("cook", cook.value())
                        .update();
            }
        }
    }

    private List<Thanks> query(String sql, Map<String, Object> params) {
        List<Row> rows = jdbc.sql(sql).params(params).query(JdbcThanksRepository::row).list();
        if (rows.isEmpty()) {
            return List.of();
        }
        Map<UUID, List<Recipient>> recipients = recipientsOf(rows.stream().map(Row::id).toList());
        return rows.stream().map(row -> row.toThanks(recipients.getOrDefault(row.id(), List.of()))).toList();
    }

    private Map<UUID, List<Recipient>> recipientsOf(List<UUID> thanksIds) {
        Map<UUID, List<CookId>> cooks = new LinkedHashMap<>();
        jdbc.sql("""
                        select recipient_id, cook from thanks_recipient_cook
                         where thanks_id in (:ids) order by recipient_id, position
                        """)
                .param("ids", thanksIds)
                .query((rs, row) -> Map.entry(rs.getObject("recipient_id", UUID.class),
                        new CookId(rs.getObject("cook", UUID.class))))
                .list()
                .forEach(entry -> cooks.computeIfAbsent(entry.getKey(), key -> new ArrayList<>()).add(entry.getValue()));
        Map<UUID, List<Recipient>> recipients = new LinkedHashMap<>();
        jdbc.sql("""
                        select thanks_id, recipient_id, type, chef_name from thanks_recipient
                         where thanks_id in (:ids) order by thanks_id, position
                        """)
                .param("ids", thanksIds)
                .query((rs, row) -> {
                    UUID recipientId = rs.getObject("recipient_id", UUID.class);
                    return Map.entry(rs.getObject("thanks_id", UUID.class), new Recipient(
                            new RecipientId(recipientId),
                            RecipientType.valueOf(rs.getString("type")),
                            rs.getString("chef_name"),
                            cooks.getOrDefault(recipientId, List.of())));
                })
                .list()
                .forEach(entry -> recipients.computeIfAbsent(entry.getKey(), key -> new ArrayList<>()).add(entry.getValue()));
        return recipients;
    }

    private static Row row(ResultSet rs, int row) throws SQLException {
        return new Row(
                rs.getObject("thanks_id", UUID.class),
                rs.getObject("giver", UUID.class),
                rs.getObject("help_id", UUID.class),
                rs.getString("thanks_text"),
                rs.getString("picture"),
                rs.getObject("picture_media_id", UUID.class),
                rs.getTimestamp("created_at").toInstant(),
                rs.getTimestamp("updated_at").toInstant(),
                rs.getLong("version"));
    }

    private record Row(UUID id, UUID giver, UUID help, String text, String picture, UUID mediaId,
                       java.time.Instant createdAt, java.time.Instant updatedAt, long version) {

        Thanks toThanks(List<Recipient> recipients) {
            return Thanks.restore(new ThanksId(id), new CookId(giver), new HelpId(help), recipients, text,
                    new Picture(URI.create(picture), new MediaId(mediaId)), createdAt, updatedAt, version);
        }
    }
}
