package org.codehaus.mojo.jaxb2.schemageneration.postprocessing.schemaenhancement;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>
 */
class TransformSchemaTest {

    // Shared state
    private static final String URI = "http://www.mithlond.se/foo/bar";
    private static final String PREFIX = "bar";
    private static final String FILENAME = "mithlondBar";
    private static final TransformSchema unitUnderTest = new TransformSchema(URI, PREFIX, FILENAME);

    @Test
    void validateExceptionOnEmptyToFileArgument() {
        assertThrows(IllegalArgumentException.class, () -> {

            // Act & Assert
            unitUnderTest.setToFile("");
        });
    }

    @Test
    void validateExceptionOnEmptyToPrefixArgument() {
        assertThrows(IllegalArgumentException.class, () -> {

            // Act & Assert
            unitUnderTest.setToPrefix("");
        });
    }

    @Test
    void validateExceptionOnEmptyUriArgument() {
        assertThrows(IllegalArgumentException.class, () -> {

            // Act & Assert
            unitUnderTest.setUri("");
        });
    }
}
