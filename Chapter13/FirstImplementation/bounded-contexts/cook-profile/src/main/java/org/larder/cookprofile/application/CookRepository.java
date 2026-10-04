package org.larder.cookprofile.application;

import java.util.List;
import java.util.Optional;

import org.larder.cookprofile.domain.Cook;
import org.larder.cookprofile.domain.CookId;
import org.larder.cookprofile.domain.EmailAddress;
import org.larder.cookprofile.domain.PersonName;

/** Port: the cooks registered on Larder. */
public interface CookRepository {

    void save(Cook cook);

    Optional<Cook> findById(CookId id);

    Optional<Cook> findByEmail(EmailAddress email);

    /** Cooks matching all given filters; a {@code null} filter matches every cook. */
    List<Cook> findAll(PersonName name, EmailAddress email);

    void delete(CookId id);
}
