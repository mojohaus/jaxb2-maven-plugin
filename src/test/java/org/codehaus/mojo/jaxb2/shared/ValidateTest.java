package org.codehaus.mojo.jaxb2.shared;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>
 */
public class ValidateTest {

    @Test
    public void validateErrorMessageOnSuppliedArgumentName() {

        // Assemble
        final String argumentName = "fooBar";
        final String expectedMsg = "Cannot handle empty 'fooBar' argument.";

        // Act & Assert
        try {
            Validate.notEmpty("", argumentName);
        } catch (IllegalArgumentException expected) {
            Assert.assertEquals(expectedMsg, expected.getMessage());
        } catch (Exception e) {
            Assert.fail("Expected IllegalArgumentException, but got " + e);
        }
    }

    @Test
    public void validateErrorMessageOnNullArgumentName() {

        // Act & Assert
        try {
            Validate.notEmpty("", null);
        } catch (IllegalArgumentException expected) {
            Assert.assertEquals("Cannot handle empty argument.", expected.getMessage());
        } catch (Exception e) {
            Assert.fail("Expected IllegalArgumentException, but got " + e);
        }
    }

    @Test
    public void validateErrorMessageOnNullArgument() {

        // Assemble
        final String argumentName = "fooBar";
        final String expectedMsg = "Cannot handle null 'fooBar' argument.";

        // Act & Assert
        try {
            Validate.notNull(null, argumentName);
        } catch (NullPointerException expected) {
            Assert.assertEquals(expectedMsg, expected.getMessage());
        } catch (Exception e) {
            Assert.fail("Expected IllegalArgumentException, but got " + e);
        }
    }

    @Test
    public void validateErrorMessageOnNullArgumentWithNullName() {

        // Act & Assert
        try {
            Validate.notNull(null, null);
        } catch (NullPointerException expected) {
            Assert.assertEquals("Cannot handle null argument.", expected.getMessage());
        } catch (Exception e) {
            Assert.fail("Expected IllegalArgumentException, but got " + e);
        }
    }
}
