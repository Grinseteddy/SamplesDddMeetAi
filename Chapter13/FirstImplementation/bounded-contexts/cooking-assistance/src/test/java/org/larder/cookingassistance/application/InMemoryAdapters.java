package org.larder.cookingassistance.application;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.larder.cookingassistance.domain.CookId;
import org.larder.cookingassistance.domain.Help;
import org.larder.cookingassistance.domain.HelpId;
import org.larder.cookingassistance.domain.HelpRequest;
import org.larder.cookingassistance.domain.HelpRequestId;
import org.larder.cookingassistance.domain.HelpRequestStatus;
import org.larder.cookingassistance.domain.HelpType;

/** In-memory fakes of the application's ports. */
final class InMemoryAdapters {

    private InMemoryAdapters() {
    }

    static class InMemoryHelpRequests implements HelpRequestRepository {
        final Map<HelpRequestId, HelpRequest> store = new LinkedHashMap<>();

        @Override
        public void save(HelpRequest request) {
            store.put(request.id(), request);
        }

        @Override
        public Optional<HelpRequest> findById(HelpRequestId id) {
            return Optional.ofNullable(store.get(id));
        }

        @Override
        public Optional<HelpRequest> findByIdForUpdate(HelpRequestId id) {
            return findById(id);
        }

        @Override
        public List<HelpRequest> search(CookId requester, HelpRequestStatus status) {
            return store.values().stream()
                    .filter(r -> requester == null || r.isRaisedBy(requester))
                    .filter(r -> status == null || r.status() == status)
                    .toList();
        }

        @Override
        public void delete(HelpRequestId id) {
            store.remove(id);
        }
    }

    static class InMemoryHelps implements HelpRepository {
        final Map<HelpId, Help> store = new LinkedHashMap<>();

        @Override
        public void save(Help help) {
            store.put(help.id(), help);
        }

        @Override
        public Optional<Help> findById(HelpId id) {
            return Optional.ofNullable(store.get(id));
        }

        @Override
        public boolean exists(HelpId id) {
            return store.containsKey(id);
        }

        @Override
        public boolean existsFor(HelpRequestId helpRequest) {
            return store.values().stream().anyMatch(h -> h.helpRequest().equals(helpRequest));
        }

        @Override
        public List<Help> search(HelpRequestId helpRequest, HelpType type, CookId helpProvider) {
            return store.values().stream()
                    .filter(h -> helpRequest == null || h.helpRequest().equals(helpRequest))
                    .filter(h -> type == null || h.answer().type() == type)
                    .filter(h -> helpProvider == null || h.isProvidedBy(helpProvider))
                    .toList();
        }

        @Override
        public void delete(HelpId id) {
            store.remove(id);
        }
    }

    /** Fake of the outbox port: records what would be published. */
    static class RecordedEvents implements HelpEvents {
        final List<HelpRequest> requested = new ArrayList<>();
        final List<Help> provided = new ArrayList<>();
        final List<UUID> providedCorrelationIds = new ArrayList<>();

        @Override
        public void helpRequested(HelpRequest request) {
            requested.add(request);
        }

        @Override
        public void helpProvided(Help help, UUID correlationId) {
            provided.add(help);
            providedCorrelationIds.add(correlationId);
        }
    }
}
