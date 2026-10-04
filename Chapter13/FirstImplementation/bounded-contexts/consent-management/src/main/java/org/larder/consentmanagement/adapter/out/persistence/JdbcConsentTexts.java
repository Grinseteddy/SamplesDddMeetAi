package org.larder.consentmanagement.adapter.out.persistence;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.larder.consentmanagement.application.ConsentTexts;
import org.larder.consentmanagement.domain.ConsentText;
import org.larder.consentmanagement.domain.ConsentTextId;
import org.springframework.jdbc.core.simple.JdbcClient;

class JdbcConsentTexts implements ConsentTexts {

    private final JdbcClient jdbc;

    JdbcConsentTexts(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<ConsentText> findAll() {
        return jdbc.sql("select consent_text_id, text from consent_text order by text").query(this::map).list();
    }

    @Override
    public Optional<ConsentText> findById(ConsentTextId id) {
        return jdbc.sql("select consent_text_id, text from consent_text where consent_text_id = :id")
                .param("id", id.value()).query(this::map).optional();
    }

    private ConsentText map(ResultSet rs, int row) throws SQLException {
        return new ConsentText(new ConsentTextId(rs.getObject("consent_text_id", UUID.class)), rs.getString("text"));
    }
}
