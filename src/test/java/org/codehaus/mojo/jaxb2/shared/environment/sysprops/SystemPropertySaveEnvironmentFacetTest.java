package org.codehaus.mojo.jaxb2.shared.environment.sysprops;

import org.codehaus.mojo.jaxb2.BufferingLog;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class SystemPropertySaveEnvironmentFacetTest {
    private static final String PROPERTY_KEY_NOT_SAVED = "some.other.property";
    private static final String PROPERTY_KEY_SAVED = "http.proxyHost";

    private SystemPropertySaveEnvironmentFacet createAndSetupFacet() {
        final BufferingLog log = new BufferingLog(BufferingLog.LogLevel.DEBUG);
        final SystemPropertySaveEnvironmentFacet facet =
                new SystemPropertySaveEnvironmentFacet(PROPERTY_KEY_SAVED, log);
        facet.setup();
        return facet;
    }

    @Test
    void onRestoreAfterChangeOriginalOtherPropertyValueIsNotRestored() {
        // Given:
        System.setProperty(PROPERTY_KEY_NOT_SAVED, "originalValue");
        final SystemPropertySaveEnvironmentFacet facet = createAndSetupFacet();

        // When:
        System.setProperty(PROPERTY_KEY_NOT_SAVED, "changedValue");
        facet.restore();

        // Then:
        assertEquals("changedValue", System.getProperty(PROPERTY_KEY_NOT_SAVED));
    }

    @Test
    void onRestoreAfterChangeOriginalSavedPropertyValueIsRestored() {
        // Given:
        System.setProperty(PROPERTY_KEY_SAVED, "originalValue");
        final SystemPropertySaveEnvironmentFacet facet = createAndSetupFacet();

        // When:
        System.setProperty(PROPERTY_KEY_SAVED, "changedValue");
        facet.restore();

        // Then:
        assertEquals("originalValue", System.getProperty(PROPERTY_KEY_SAVED));
    }

    @Test
    void onRestoreAfterClearOriginalOtherPropertyValueIsNotRestored() {
        // Given:
        System.setProperty(PROPERTY_KEY_NOT_SAVED, "originalValue");
        final SystemPropertySaveEnvironmentFacet facet = createAndSetupFacet();

        // When:
        System.clearProperty(PROPERTY_KEY_NOT_SAVED);
        facet.restore();

        // Then:
        assertNull(System.getProperty(PROPERTY_KEY_NOT_SAVED));
    }

    @Test
    void onRestoreAfterClearOriginalSavedPropertyValueIsRestored() {
        // Given:
        System.setProperty(PROPERTY_KEY_SAVED, "originalValue");
        final SystemPropertySaveEnvironmentFacet facet = createAndSetupFacet();

        // When:
        System.clearProperty(PROPERTY_KEY_SAVED);
        facet.restore();

        // Then:
        assertEquals("originalValue", System.getProperty(PROPERTY_KEY_SAVED));
    }

    @Test
    void onRestoreAfterSetOriginallyUnsetOtherPropertyIsNotCleared() {
        // Given:
        System.clearProperty(PROPERTY_KEY_NOT_SAVED);
        final SystemPropertySaveEnvironmentFacet facet = createAndSetupFacet();

        // When:
        System.setProperty(PROPERTY_KEY_NOT_SAVED, "changedValue");
        facet.restore();

        // Then:
        assertEquals("changedValue", System.getProperty(PROPERTY_KEY_NOT_SAVED));
    }

    @Test
    void onRestoreAfterSetOriginallyUnsetSavedPropertyIsCleared() {
        // Given:
        System.clearProperty(PROPERTY_KEY_SAVED);
        final SystemPropertySaveEnvironmentFacet facet = createAndSetupFacet();

        // When:
        System.setProperty(PROPERTY_KEY_SAVED, "changedValue");
        facet.restore();

        // Then:
        assertNull(System.getProperty(PROPERTY_KEY_SAVED));
    }
}
