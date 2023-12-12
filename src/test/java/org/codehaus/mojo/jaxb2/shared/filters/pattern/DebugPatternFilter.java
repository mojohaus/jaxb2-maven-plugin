package org.codehaus.mojo.jaxb2.shared.filters.pattern;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class DebugPatternFilter extends AbstractPatternFilter<String> {

    // Shared state
    private final Object lock = new Object();
    public SortedMap<Integer, String[]> invocations = new TreeMap<Integer, String[]>();

    public DebugPatternFilter(final List<String> patterns, final boolean acceptCandidateOnPatternMatch) {
        super();
        setPatterns(patterns);
        setAcceptCandidateOnPatternMatch(acceptCandidateOnPatternMatch);
    }

    public DebugPatternFilter(
            final boolean processNullValues,
            final List<String> patterns,
            final StringConverter<String> converter,
            final boolean acceptCandidateOnPatternMatch) {
        super();
        setProcessNullValues(processNullValues);
        setPatterns(patterns);
        setConverter(converter);
        setAcceptCandidateOnPatternMatch(acceptCandidateOnPatternMatch);
    }

    @Override
    protected boolean onCandidate(final String nonNullCandidate) {
        boolean toReturn = super.onCandidate(nonNullCandidate);
        addInvocation(nonNullCandidate, toReturn);
        return toReturn;
    }

    //
    // Internal state
    //

    private void addInvocation(final String candidate, final boolean result) {

        synchronized (lock) {
            final String[] resultStruct = new String[2];
            resultStruct[0] = candidate;
            resultStruct[1] = "" + result;

            invocations.put(1 + invocations.size(), resultStruct);
        }
    }
}
