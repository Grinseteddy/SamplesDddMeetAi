package cookingassistance.domain;

import java.util.Optional;
import cookingassistance.domain.Ids.HelpRequestId;
/** Infrastructure implementation must participate in the acceptance unit of work. */
public interface HelpRequestRepository {
    Optional<HelpRequest> findById(HelpRequestId id);
    void save(HelpRequest aggregate);
}
