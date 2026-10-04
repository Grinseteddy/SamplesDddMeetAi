package org.larder.grandmaavatar.adapter.out.persistence;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Optional;
import java.util.UUID;

import org.larder.grandmaavatar.application.HandledHelpRequests;
import org.larder.grandmaavatar.domain.CookId;
import org.larder.grandmaavatar.domain.HandledHelpRequest;
import org.larder.grandmaavatar.domain.HelpId;
import org.larder.grandmaavatar.domain.HelpRequestId;
import org.larder.grandmaavatar.domain.HelpType;
import org.larder.grandmaavatar.domain.Outcome;
import org.springframework.jdbc.core.simple.JdbcClient;

/** Grandma's notebook in table {@code handled_help_request}; joins the caller's transaction. */
public class JdbcHandledHelpRequests implements HandledHelpRequests {

    private final JdbcClient jdbc;

    public JdbcHandledHelpRequests(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<HandledHelpRequest> find(HelpRequestId helpRequest) {
        return jdbc.sql("""
                        select help_request_id, requester, help_type, outcome, help_id, answer_title, handled_at
                          from handled_help_request
                         where help_request_id = :id
                        """)
                .param("id", helpRequest.value())
                .query(this::map)
                .optional();
    }

    /**
     * {@code on conflict do nothing}: a concurrent duplicate waits for the first transaction and then
     * inserts nothing - exactly one of them gets {@code true}.
     */
    @Override
    public boolean add(HandledHelpRequest handled) {
        return jdbc.sql("""
                        insert into handled_help_request
                               (help_request_id, requester, help_type, outcome, help_id, answer_title, handled_at)
                        values (:id, :requester, :type, :outcome, :helpId, :answerTitle, :handledAt)
                        on conflict (help_request_id) do nothing
                        """)
                .param("id", handled.helpRequest().value())
                .param("requester", handled.requester().value())
                .param("type", handled.type().name())
                .param("outcome", handled.outcome().name())
                .param("helpId", handled.help().map(HelpId::value).orElse(null))
                .param("answerTitle", handled.answerTitle().orElse(null))
                .param("handledAt", Timestamp.from(handled.handledAt()))
                .update() == 1;
    }

    private HandledHelpRequest map(ResultSet rs, int row) throws SQLException {
        return new HandledHelpRequest(
                new HelpRequestId(rs.getObject("help_request_id", UUID.class)),
                new CookId(rs.getObject("requester", UUID.class)),
                HelpType.valueOf(rs.getString("help_type")),
                Outcome.valueOf(rs.getString("outcome")),
                Optional.ofNullable(rs.getObject("help_id", UUID.class)).map(HelpId::new),
                Optional.ofNullable(rs.getString("answer_title")),
                rs.getTimestamp("handled_at").toInstant());
    }
}
