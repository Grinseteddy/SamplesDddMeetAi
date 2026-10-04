package org.larder.consentmanagement.adapter.out.persistence;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.larder.consentmanagement.application.ConsentRepository;
import org.larder.consentmanagement.domain.Consent;
import org.larder.consentmanagement.domain.ConsentId;
import org.larder.consentmanagement.domain.ConsentText;
import org.larder.consentmanagement.domain.ConsentTextId;
import org.larder.consentmanagement.domain.SubjectId;
import org.springframework.jdbc.core.simple.JdbcClient;

class JdbcConsentRepository implements ConsentRepository {

    private static final String SELECT = """
            select consent_id, subject, consent_text_id, consent_text, given_at, revoked_at
              from consent
            """;

    private final JdbcClient jdbc;

    JdbcConsentRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void save(Consent consent) {
        jdbc.sql("""
                insert into consent (consent_id, subject, consent_text_id, consent_text, given_at, revoked_at)
                values (:id, :subject, :textId, :text, :givenAt, :revokedAt)
                on conflict (consent_id) do update set revoked_at = excluded.revoked_at
                """)
                .param("id", consent.id().value())
                .param("subject", consent.subject().value())
                .param("textId", consent.consentText().id().value())
                .param("text", consent.consentText().text())
                .param("givenAt", Timestamp.from(consent.givenAt()))
                .param("revokedAt", consent.revokedAt().map(Timestamp::from).orElse(null))
                .update();
    }

    @Override
    public Optional<Consent> findById(ConsentId id) {
        return jdbc.sql(SELECT + " where consent_id = :id").param("id", id.value()).query(this::map).optional();
    }

    @Override
    public List<Consent> findBySubject(SubjectId subject) {
        return jdbc.sql(SELECT + " where subject = :subject order by given_at")
                .param("subject", subject.value()).query(this::map).list();
    }

    private Consent map(ResultSet rs, int row) throws SQLException {
        Timestamp revokedAt = rs.getTimestamp("revoked_at");
        return Consent.restore(
                new ConsentId(rs.getObject("consent_id", UUID.class)),
                new SubjectId(rs.getObject("subject", UUID.class)),
                new ConsentText(new ConsentTextId(rs.getObject("consent_text_id", UUID.class)), rs.getString("consent_text")),
                rs.getTimestamp("given_at").toInstant(),
                revokedAt == null ? null : revokedAt.toInstant());
    }
}
