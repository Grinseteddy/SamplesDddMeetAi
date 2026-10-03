package org.larder.mealplanning;

import org.junit.jupiter.api.Test;
import org.larder.platform.test.BoundedContextArchitectureRules;

class ArchitectureTest {

    @Test
    void respectsHexagonalBoundaries() {
        BoundedContextArchitectureRules.verify("org.larder.mealplanning");
    }
}
