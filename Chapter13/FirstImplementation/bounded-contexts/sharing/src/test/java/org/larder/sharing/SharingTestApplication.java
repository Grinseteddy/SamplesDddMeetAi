package org.larder.sharing;

import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Anchor for Spring test slices (e.g. {@code @WebMvcTest}) inside this module. The slice
 * decides what is scanned and auto-configured; this class only marks the package root.
 */
@SpringBootApplication
class SharingTestApplication {
}
