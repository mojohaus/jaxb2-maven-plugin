package org.codehaus.mojo.jaxb2.shared.environment.sysprops;

import org.codehaus.mojo.jaxb2.BufferingLog;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SystemPropertySaveEnvironmentFacetTest {
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
    public void onRestoreAfterChangeOriginalOtherPropertyValueIsNotRestored() {
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
    public void onRestoreAfterChangeOriginalSavedPropertyValueIsRestored() {
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
    public void onRestoreAfterClearOriginalOtherPropertyValueIsNotRestored() {
        // Given:
        System.setProperty(PROPERTY_KEY_NOT_SAVED, "originalValue");
        final SystemPropertySaveEnvironmentFacet facet = createAndSetupFacet();

        // When:
        System.clearProperty(PROPERTY_KEY_NOT_SAVED);
        facet.restore();

        // Then:
        assertEquals(null, System.getProperty(PROPERTY_KEY_NOT_SAVED));
    }

    @Test
    public void onRestoreAfterClearOriginalSavedPropertyValueIsRestored() {
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
    public void onRestoreAfterSetOriginallyUnsetOtherPropertyIsNotCleared() {
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
    public void onRestoreAfterSetOriginallyUnsetSavedPropertyIsCleared() {
        // Given:
        System.clearProperty(PROPERTY_KEY_SAVED);
        final SystemPropertySaveEnvironmentFacet facet = createAndSetupFacet();

        // When:
        System.setProperty(PROPERTY_KEY_SAVED, "changedValue");
        facet.restore();

        // Then:
        assertEquals(null, System.getProperty(PROPERTY_KEY_SAVED));
    }
}
