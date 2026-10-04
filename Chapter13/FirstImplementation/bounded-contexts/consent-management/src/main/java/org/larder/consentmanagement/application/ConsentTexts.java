package org.larder.consentmanagement.application;

import java.util.List;
import java.util.Optional;

import org.larder.consentmanagement.domain.ConsentText;
import org.larder.consentmanagement.domain.ConsentTextId;

/** Port: the consent texts, read-only for this context's API. */
public interface ConsentTexts {

    List<ConsentText> findAll();

    Optional<ConsentText> findById(ConsentTextId id);
}
