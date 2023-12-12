package org.codehaus.mojo.jaxb2.shared.filters;

import java.util.SortedMap;

import org.codehaus.mojo.jaxb2.BufferingLog;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class T_AbstractFilterTest {

    // Shared state
    private BufferingLog log;

    @Before
    public void setupSharedState() {
        log = new BufferingLog();
    }

    @Test(expected = IllegalStateException.class)
    public void validateExceptionOnNotInitializingFilterBeforeFirstCall() {

        // Assemble
        final DebugFilter unitUnderTest = new DebugFilter(false);

        // Act & Assert
        unitUnderTest.accept("foobar!");
    }

    @Test
    public void validateCallOrderIfNotProcessingNulls() {

        // Assemble
        final DebugFilter unitUnderTest = new DebugFilter(false);

        // Act
        unitUnderTest.initialize(log);
        unitUnderTest.accept("first");
        unitUnderTest.accept(null);
        unitUnderTest.accept("third");

        // Assert
        final SortedMap<Integer, String> invocations = unitUnderTest.invocations;
        Assert.assertEquals(2, invocations.size());
        Assert.assertEquals("first", invocations.get(1));
        Assert.assertEquals("third", invocations.get(2));
    }

    @Test
    public void validateCallOrderIfProcessingNulls() {

        // Assemble
        final DebugFilter unitUnderTest = new DebugFilter(true);

        // Act
        unitUnderTest.initialize(log);
        unitUnderTest.accept("first");
        unitUnderTest.accept(null);
        unitUnderTest.accept("third");

        // Assert
        final SortedMap<Integer, String> invocations = unitUnderTest.invocations;
        Assert.assertEquals(3, invocations.size());
        Assert.assertEquals("first", invocations.get(1));
        Assert.assertEquals(null, invocations.get(2));
        Assert.assertEquals("third", invocations.get(3));
    }
}
