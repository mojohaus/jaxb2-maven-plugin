package org.codehaus.mojo.jaxb2.shared.filters;

import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class DebugFilter extends AbstractFilter<String> {

    // Shared state
    private final Object lock = new Object();
    public SortedMap<Integer, String> invocations = new TreeMap<Integer, String>();

    public DebugFilter(final boolean processNullValues) {
        super();
        setProcessNullValues(processNullValues);
    }

    @Override
    protected boolean onNullCandidate() {

        addInvocation(null);
        return super.onNullCandidate();
    }

    @Override
    protected boolean onCandidate(final String nonNullCandidate) {
        addInvocation(nonNullCandidate);
        return false;
    }

    //
    // Internal state
    //

    private void addInvocation(final String candidate) {

        synchronized (lock) {
            invocations.put(1 + invocations.size(), candidate);
        }
    }
}
