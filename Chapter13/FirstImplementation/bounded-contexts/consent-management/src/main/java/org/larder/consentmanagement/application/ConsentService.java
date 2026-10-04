package org.larder.consentmanagement.application;

import java.time.Clock;
import java.util.List;

import org.larder.consentmanagement.domain.Consent;
import org.larder.consentmanagement.domain.ConsentId;
import org.larder.consentmanagement.domain.ConsentText;
import org.larder.consentmanagement.domain.ConsentTextId;
import org.larder.consentmanagement.domain.SubjectId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use cases of Consent Management. A cook gives and revokes only their own consents;
 * everybody authenticated may read consents, e.g. Sharing checks whether a mentioned cook consented.
 */
@Service
public class ConsentService {

    /** Transaction manager of this context's own schema. */
    public static final String TRANSACTIONS = "consentmanagementTransactionManager";

    private final ConsentRepository consents;
    private final ConsentTexts consentTexts;
    private final Clock clock;

    public ConsentService(ConsentRepository consents, ConsentTexts consentTexts, Clock clock) {
        this.consents = consents;
        this.consentTexts = consentTexts;
        this.clock = clock;
    }

    @Transactional(transactionManager = ConsentService.TRANSACTIONS)
    public Consent give(SubjectId caller, SubjectId subject, ConsentTextId consentTextId) {
        if (!caller.equals(subject)) {
            throw new NotPermittedException("A cook can only give consents of their own");
        }
        ConsentText text = consentTexts.findById(consentTextId)
                .orElseThrow(() -> new UnknownConsentTextException(consentTextId));
        Consent consent = Consent.give(subject, text, clock.instant());
        consents.save(consent);
        return consent;
    }

    @Transactional(transactionManager = ConsentService.TRANSACTIONS)
    public void revoke(SubjectId caller, ConsentId id) {
        Consent consent = consent(id);
        if (!consent.isGivenBy(caller)) {
            throw new NotPermittedException("A cook can only revoke consents of their own");
        }
        consent.revoke(clock.instant());
        consents.save(consent);
    }

    public Consent consent(ConsentId id) {
        return consents.findById(id).orElseThrow(() -> new NotFoundException("Consent " + id.value() + " not found"));
    }

    public List<Consent> consentsOf(SubjectId subject) {
        return consents.findBySubject(subject);
    }

    public List<ConsentText> consentTexts() {
        return consentTexts.findAll();
    }

    public ConsentText consentText(ConsentTextId id) {
        return consentTexts.findById(id)
                .orElseThrow(() -> new NotFoundException("Consent text " + id.value() + " not found"));
    }
}
