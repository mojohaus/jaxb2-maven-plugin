package org.codehaus.mojo.jaxb2.shared;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>
 */
class ValidateTest {

    @Test
    void validateErrorMessageOnSuppliedArgumentName() {

        // Assemble
        final String argumentName = "fooBar";
        final String expectedMsg = "Cannot handle empty 'fooBar' argument.";

        // Act & Assert
        final IllegalArgumentException expected =
                assertThrows(IllegalArgumentException.class, () -> Validate.notEmpty("", argumentName));
        assertEquals(expectedMsg, expected.getMessage());
    }

    @Test
    void validateErrorMessageOnNullArgumentName() {

        // Act & Assert
        final IllegalArgumentException expected =
                assertThrows(IllegalArgumentException.class, () -> Validate.notEmpty("", null));
        assertEquals("Cannot handle empty argument.", expected.getMessage());
    }

    @Test
    void validateErrorMessageOnNullArgument() {

        // Assemble
        final String argumentName = "fooBar";
        final String expectedMsg = "Cannot handle null 'fooBar' argument.";

        // Act & Assert
        final NullPointerException expected =
                assertThrows(NullPointerException.class, () -> Validate.notNull(null, argumentName));
        assertEquals(expectedMsg, expected.getMessage());
    }

    @Test
    void validateErrorMessageOnNullArgumentWithNullName() {

        // Act & Assert
        final NullPointerException expected =
                assertThrows(NullPointerException.class, () -> Validate.notNull(null, null));
        assertEquals("Cannot handle null argument.", expected.getMessage());
    }
}
