package org.larder.cookprofile.application;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import org.larder.cookprofile.domain.Cook;
import org.larder.cookprofile.domain.CookChange;
import org.larder.cookprofile.domain.CookId;
import org.larder.cookprofile.domain.EmailAddress;
import org.larder.cookprofile.domain.PersonName;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use cases of Cook Profile. A user registers themselves as a cook, and a cook changes and
 * deregisters only their own profile; everybody authenticated may look cooks up.
 *
 * <p>Cook Profile is meant as anticorruption layer in front of an external IAM. The contract
 * does not need anything from the IAM yet - the caller's id comes from the access token -
 * so profiles are only kept in this context's own schema for now.
 */
@Service
public class CookService {

    /** Transaction manager of this context's own schema. */
    public static final String TRANSACTIONS = "cookprofileTransactionManager";

    private final CookRepository cooks;
    private final Clock clock;

    public CookService(CookRepository cooks, Clock clock) {
        this.cooks = cooks;
        this.clock = clock;
    }

    @Transactional(transactionManager = CookService.TRANSACTIONS)
    public Cook register(CookId caller, EmailAddress email, PersonName name, PersonName givenName) {
        if (cooks.findById(caller).isPresent()) {
            throw new AlreadyRegisteredException("User " + caller.value() + " is already registered as a cook");
        }
        requireUnused(email, caller);
        Cook cook = Cook.register(caller, email, name, givenName, LocalDate.now(clock));
        cooks.save(cook);
        return cook;
    }

    @Transactional(transactionManager = CookService.TRANSACTIONS)
    public Cook change(CookId caller, CookId id, CookChange change) {
        Cook cook = cook(id);
        if (!cook.isProfileOf(caller)) {
            throw new NotPermittedException("A cook can only change their own profile");
        }
        if (change.email() != null) {
            requireUnused(change.email(), id);
        }
        cook.change(change);
        cooks.save(cook);
        return cook;
    }

    @Transactional(transactionManager = CookService.TRANSACTIONS)
    public void deregister(CookId caller, CookId id) {
        Cook cook = cook(id);
        if (!cook.isProfileOf(caller)) {
            throw new NotPermittedException("A cook is deleted only when the cook deregisters");
        }
        cooks.delete(cook.id());
    }

    public Cook cook(CookId id) {
        return cooks.findById(id).orElseThrow(() -> new NotFoundException("Cook " + id.value() + " not found"));
    }

    /** Cooks matching all given filters; a {@code null} filter matches every cook. */
    public List<Cook> cooks(PersonName name, EmailAddress email) {
        return cooks.findAll(name, email);
    }

    private void requireUnused(EmailAddress email, CookId owner) {
        cooks.findByEmail(email)
                .filter(other -> !other.isProfileOf(owner))
                .ifPresent(other -> {
                    throw new AlreadyRegisteredException("Email address " + email.value() + " belongs to another cook");
                });
    }
}
