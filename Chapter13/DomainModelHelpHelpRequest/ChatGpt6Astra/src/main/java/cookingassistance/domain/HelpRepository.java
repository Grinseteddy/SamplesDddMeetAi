package cookingassistance.domain;

import java.util.Optional;
import cookingassistance.domain.Ids.HelpId;
/** Infrastructure implementation must participate in the acceptance unit of work. */
public interface HelpRepository {
    Optional<Help> findById(HelpId id);
    void save(Help aggregate);
}
