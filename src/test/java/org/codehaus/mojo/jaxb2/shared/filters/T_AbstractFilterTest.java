package org.codehaus.mojo.jaxb2.shared.filters;

import java.util.SortedMap;

import org.codehaus.mojo.jaxb2.BufferingLog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
class T_AbstractFilterTest {

    // Shared state
    private BufferingLog log;

    @BeforeEach
    void setupSharedState() {
        log = new BufferingLog();
    }

    @Test
    void validateExceptionOnNotInitializingFilterBeforeFirstCall() {
        final DebugFilter unitUnderTest = new DebugFilter(false);
        assertThrows(IllegalStateException.class, () ->

            // Act & Assert
            unitUnderTest.accept("foobar!"));
    }

    @Test
    void validateCallOrderIfNotProcessingNulls() {

        // Assemble
        final DebugFilter unitUnderTest = new DebugFilter(false);

        // Act
        unitUnderTest.initialize(log);
        unitUnderTest.accept("first");
        unitUnderTest.accept(null);
        unitUnderTest.accept("third");

        // Assert
        final SortedMap<Integer, String> invocations = unitUnderTest.invocations;
        assertEquals(2, invocations.size());
        assertEquals("first", invocations.get(1));
        assertEquals("third", invocations.get(2));
    }

    @Test
    void validateCallOrderIfProcessingNulls() {

        // Assemble
        final DebugFilter unitUnderTest = new DebugFilter(true);

        // Act
        unitUnderTest.initialize(log);
        unitUnderTest.accept("first");
        unitUnderTest.accept(null);
        unitUnderTest.accept("third");

        // Assert
        final SortedMap<Integer, String> invocations = unitUnderTest.invocations;
        assertEquals(3, invocations.size());
        assertEquals("first", invocations.get(1));
        assertNull(invocations.get(2));
        assertEquals("third", invocations.get(3));
    }
}
