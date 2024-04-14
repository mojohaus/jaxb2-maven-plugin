package org.codehaus.mojo.jaxb2.shared;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

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
        try {
            Validate.notEmpty("", argumentName);
        } catch (IllegalArgumentException expected) {
            assertEquals(expectedMsg, expected.getMessage());
        } catch (Exception e) {
            fail("Expected IllegalArgumentException, but got " + e);
        }
    }

    @Test
    void validateErrorMessageOnNullArgumentName() {

        // Act & Assert
        try {
            Validate.notEmpty("", null);
        } catch (IllegalArgumentException expected) {
            assertEquals("Cannot handle empty argument.", expected.getMessage());
        } catch (Exception e) {
            fail("Expected IllegalArgumentException, but got " + e);
        }
    }

    @Test
    void validateErrorMessageOnNullArgument() {

        // Assemble
        final String argumentName = "fooBar";
        final String expectedMsg = "Cannot handle null 'fooBar' argument.";

        // Act & Assert
        try {
            Validate.notNull(null, argumentName);
        } catch (NullPointerException expected) {
            assertEquals(expectedMsg, expected.getMessage());
        } catch (Exception e) {
            fail("Expected IllegalArgumentException, but got " + e);
        }
    }

    @Test
    void validateErrorMessageOnNullArgumentWithNullName() {

        // Act & Assert
        try {
            Validate.notNull(null, null);
        } catch (NullPointerException expected) {
            assertEquals("Cannot handle null argument.", expected.getMessage());
        } catch (Exception e) {
            fail("Expected IllegalArgumentException, but got " + e);
        }
    }
}
