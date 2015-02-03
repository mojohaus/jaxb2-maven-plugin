package org.codehaus.mojo.jaxb2.schemageneration.postprocessing.schemaenhancement;

import org.junit.Test;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>
 */
public class TransformSchemaTest {

    // Shared state
    private static final String URI = "http://www.mithlond.se/foo/bar";
    private static final String PREFIX = "bar";
    private static final String FILENAME = "mithlondBar";
    private static final TransformSchema unitUnderTest = new TransformSchema(URI, PREFIX, FILENAME);

    @Test(expected = IllegalArgumentException.class)
    public void validateExceptionOnEmptyToFileArgument() {

        // Act & Assert
        unitUnderTest.setToFile("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateExceptionOnEmptyToPrefixArgument() {

        // Act & Assert
        unitUnderTest.setToPrefix("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateExceptionOnEmptyUriArgument() {

        // Act & Assert
        unitUnderTest.setUri("");
    }
}
