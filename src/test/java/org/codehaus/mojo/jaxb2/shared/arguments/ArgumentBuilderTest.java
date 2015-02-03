package org.codehaus.mojo.jaxb2.shared.arguments;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>
 */
public class ArgumentBuilderTest {

    @Test(expected = IllegalArgumentException.class)
    public void validateExceptionOnAddingAllWhitespaceFlag() {

        // Assemble
        final ArgumentBuilder unitUnderTest = new ArgumentBuilder();

        // Act & Assert
        unitUnderTest.withFlag(true, "  ");
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateExceptionOnAddingFlagContainingWhitespace() {

        // Assemble
        final ArgumentBuilder unitUnderTest = new ArgumentBuilder();

        // Act & Assert
        unitUnderTest.withFlag(true, "foo bar");
    }

    @Test(expected = NullPointerException.class)
    public void validateExceptionOnAddingNullFlag() {

        // Assemble
        final ArgumentBuilder unitUnderTest = new ArgumentBuilder();

        // Act & Assert
        unitUnderTest.withFlag(true, null);
    }

    @Test(expected = NullPointerException.class)
    public void validateExceptionOnAddingNullNamedArgument() {

        // Assemble
        final ArgumentBuilder unitUnderTest = new ArgumentBuilder();

        // Act & Assert
        unitUnderTest.withNamedArgument(null, "irrelevant");
    }

    @Test
    public void validateArgumentOrder() {

        // Assemble
        final ArgumentBuilder unitUnderTest = new ArgumentBuilder();

        // Act
        final String[] result = unitUnderTest
                .withFlag(true, "flag1")
                .withFlag(true, "-flag2")
                .build();

        // Assert
        Assert.assertEquals(2, result.length);
        Assert.assertEquals("-flag1", result[0]);
        Assert.assertEquals("-flag2", result[1]);
    }

    @Test
    public void validateMixedArguments() {

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
        Assert.assertEquals(3, result.length);
        Assert.assertEquals("-name1", result[0]);
        Assert.assertEquals("value1", result[1]);
        Assert.assertEquals("-flag2", result[2]);
    }
}
