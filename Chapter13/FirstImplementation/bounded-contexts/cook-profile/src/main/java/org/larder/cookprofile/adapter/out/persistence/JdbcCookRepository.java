package org.larder.cookprofile.adapter.out.persistence;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.larder.cookprofile.application.CookRepository;
import org.larder.cookprofile.domain.Cook;
import org.larder.cookprofile.domain.CookId;
import org.larder.cookprofile.domain.CookStatus;
import org.larder.cookprofile.domain.EmailAddress;
import org.larder.cookprofile.domain.PersonName;
import org.springframework.jdbc.core.simple.JdbcClient;

class JdbcCookRepository implements CookRepository {

    private static final String SELECT = """
            select cook_id, email, name, given_name, member_since, status
              from cook
            """;

    private final JdbcClient jdbc;

    JdbcCookRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void save(Cook cook) {
        jdbc.sql("""
                insert into cook (cook_id, email, name, given_name, member_since, status)
                values (:id, :email, :name, :givenName, :memberSince, :status)
                on conflict (cook_id) do update
                   set email = excluded.email, name = excluded.name,
                       given_name = excluded.given_name, status = excluded.status
                """)
                .param("id", cook.id().value())
                .param("email", cook.email().value())
                .param("name", cook.name().value())
                .param("givenName", cook.givenName().value())
                .param("memberSince", Date.valueOf(cook.memberSince()))
                .param("status", cook.status().name())
                .update();
    }

    @Override
    public Optional<Cook> findById(CookId id) {
        return jdbc.sql(SELECT + " where cook_id = :id").param("id", id.value()).query(this::map).optional();
    }

    @Override
    public Optional<Cook> findByEmail(EmailAddress email) {
        return jdbc.sql(SELECT + " where lower(email) = :email")
                .param("email", email.normalized()).query(this::map).optional();
    }

    @Override
    public List<Cook> findAll(PersonName name, EmailAddress email) {
        return jdbc.sql(SELECT + """
                 where (cast(:name as varchar) is null or name = :name)
                   and (cast(:email as varchar) is null or lower(email) = :email)
                 order by name, given_name, cook_id
                """)
                .param("name", name == null ? null : name.value())
                .param("email", email == null ? null : email.normalized())
                .query(this::map).list();
    }

    @Override
    public void delete(CookId id) {
        jdbc.sql("delete from cook where cook_id = :id").param("id", id.value()).update();
    }

    private Cook map(ResultSet rs, int row) throws SQLException {
        return Cook.restore(
                new CookId(rs.getObject("cook_id", UUID.class)),
                new EmailAddress(rs.getString("email")),
                new PersonName(rs.getString("name")),
                new PersonName(rs.getString("given_name")),
                rs.getDate("member_since").toLocalDate(),
                CookStatus.valueOf(rs.getString("status")));
    }
}
