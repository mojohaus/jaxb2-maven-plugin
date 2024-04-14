package org.codehaus.mojo.jaxb2.shared.arguments;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>
 */
class ArgumentBuilderTest {

    @Test
    void validateExceptionOnAddingAllWhitespaceFlag() {
        assertThrows(IllegalArgumentException.class, () -> {

            // Assemble
            final ArgumentBuilder unitUnderTest = new ArgumentBuilder();

            // Act & Assert
            unitUnderTest.withFlag(true, "  ");
        });
    }

    @Test
    void validateExceptionOnAddingFlagContainingWhitespace() {
        assertThrows(IllegalArgumentException.class, () -> {

            // Assemble
            final ArgumentBuilder unitUnderTest = new ArgumentBuilder();

            // Act & Assert
            unitUnderTest.withFlag(true, "foo bar");
        });
    }

    @Test
    void validateExceptionOnAddingNullFlag() {
        assertThrows(NullPointerException.class, () -> {

            // Assemble
            final ArgumentBuilder unitUnderTest = new ArgumentBuilder();

            // Act & Assert
            unitUnderTest.withFlag(true, null);
        });
    }

    @Test
    void validateExceptionOnAddingNullNamedArgument() {
        assertThrows(NullPointerException.class, () -> {

            // Assemble
            final ArgumentBuilder unitUnderTest = new ArgumentBuilder();

            // Act & Assert
            unitUnderTest.withNamedArgument(null, "irrelevant");
        });
    }

    @Test
    void validateArgumentOrder() {

        // Assemble
        final ArgumentBuilder unitUnderTest = new ArgumentBuilder();

        // Act
        final String[] result =
                unitUnderTest.withFlag(true, "flag1").withFlag(true, "-flag2").build();

        // Assert
        assertEquals(2, result.length);
        assertEquals("-flag1", result[0]);
        assertEquals("-flag2", result[1]);
    }

    @Test
    void validateMixedArguments() {

        // Assemble
        final ArgumentBuilder unitUnderTest = new ArgumentBuilder();

        // Act
        final String[] result = unitUnderTest
                .withNamedArgument(true, "name1", "value1")
                .withFlag(true, "flag2")
                .withNamedArgument(false, "name3", "value3")
                .withFlag(false, "flag4")
                .build();

        // Assert
        assertEquals(3, result.length);
        assertEquals("-name1", result[0]);
        assertEquals("value1", result[1]);
        assertEquals("-flag2", result[2]);
    }

    @Test
    void validatePrefixedArgument() {

        // Assemble
        final ArgumentBuilder unitUnderTest = new ArgumentBuilder();

        // Act
        final String[] result = unitUnderTest
                .withPrefixedArguments("X", Arrays.asList("plugin1", "plugin2"))
                .withPrefixedArguments("X", Arrays.asList("plugin3", "plugin4"))
                .build();

        // Assert
        assertEquals(4, result.length);
        assertEquals("-Xplugin1", result[0]);
        assertEquals("-Xplugin2", result[1]);
        assertEquals("-Xplugin3", result[2]);
        assertEquals("-Xplugin4", result[3]);
    }
}
