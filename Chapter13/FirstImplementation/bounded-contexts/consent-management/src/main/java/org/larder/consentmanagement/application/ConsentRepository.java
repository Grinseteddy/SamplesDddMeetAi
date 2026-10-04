package org.larder.consentmanagement.application;

import java.util.List;
import java.util.Optional;

import org.larder.consentmanagement.domain.Consent;
import org.larder.consentmanagement.domain.ConsentId;
import org.larder.consentmanagement.domain.SubjectId;

/** Port: the consents given on Larder. */
public interface ConsentRepository {

    void save(Consent consent);

    Optional<Consent> findById(ConsentId id);

    List<Consent> findBySubject(SubjectId subject);
}
