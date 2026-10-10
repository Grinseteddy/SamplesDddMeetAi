package de.codecentric.cookingassistance.domain.help;

import de.codecentric.cookingassistance.domain.shared.HelpId;

import java.util.Optional;

/** Repository for the Help aggregate. Implemented outside the domain layer. */
public interface HelpRepository {

    Optional<Help> findById(HelpId helpId);

    void save(Help help);
}
