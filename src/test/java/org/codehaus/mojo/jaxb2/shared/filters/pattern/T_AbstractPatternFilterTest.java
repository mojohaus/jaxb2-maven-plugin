package org.codehaus.mojo.jaxb2.shared.filters.pattern;

import java.util.Arrays;
import java.util.List;
import java.util.SortedMap;

import org.codehaus.mojo.jaxb2.BufferingLog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
class T_AbstractPatternFilterTest {

    private static final StringConverter<String> UNITY_STRING_CONVERTER = new StringConverter<String>() {
        @Override
        public String convert(final String toConvert) {
            return toConvert;
        }
    };

    // Shared state
    private BufferingLog log;

    @BeforeEach
    void setupSharedState() {
        log = new BufferingLog(BufferingLog.LogLevel.DEBUG);
    }

    @Test
    void validateExceptionOnNullStringConverter() {
        assertThrows(NullPointerException.class, () -> {

            // Assemble
            final boolean processNullValues = false;
            final List<String> patterns = null;
            final boolean acceptCandidateOnPatternMatch = true;

            // Act & Assert
            new DebugPatternFilter(processNullValues, patterns, null, acceptCandidateOnPatternMatch);
        });
    }

    @Test
    void validateNoAcceptedResultsWhenProcessingMessagesOnNullFilters() {

        // Assemble
        final boolean processNullValues = false;
        final List<String> patterns = null;
        final boolean acceptCandidateOnPatternMatch = true;

        final DebugPatternFilter unitUnderTest = new DebugPatternFilter(
                processNullValues, patterns, UNITY_STRING_CONVERTER, acceptCandidateOnPatternMatch);

        // Act
        unitUnderTest.initialize(log);
        unitUnderTest.accept("first");
        unitUnderTest.accept(null);
        unitUnderTest.accept("third");

        // Assert
        final SortedMap<Integer, String[]> invocations = unitUnderTest.invocations;
        assertEquals(2, invocations.size());
        assertEquals("first", invocations.get(1)[0]);
        assertFalse(Boolean.parseBoolean(invocations.get(1)[1]));
        assertEquals("third", invocations.get(2)[0]);
        assertFalse(Boolean.parseBoolean(invocations.get(2)[1]));
    }

    @Test
    void validateAcceptingFilterMessages() {

        // Assemble
        final boolean processNullValues = false;
        final boolean acceptCandidateOnPatternMatch = true;
        final List<String> patterns = Arrays.asList("f.*t"); // Should match 'first' but not 'third'

        final DebugPatternFilter unitUnderTest = new DebugPatternFilter(
                processNullValues, patterns, UNITY_STRING_CONVERTER, acceptCandidateOnPatternMatch);

        // Act
        unitUnderTest.initialize(log);
        unitUnderTest.accept("first");
        unitUnderTest.accept(null);
        unitUnderTest.accept("third");

        // Assert
        final SortedMap<Integer, String[]> invocations = unitUnderTest.invocations;
        assertEquals(2, invocations.size());
        assertEquals("first", invocations.get(1)[0]);
        assertTrue(Boolean.parseBoolean(invocations.get(1)[1]));
        assertEquals("third", invocations.get(2)[0]);
        assertFalse(Boolean.parseBoolean(invocations.get(2)[1]));
    }
}
