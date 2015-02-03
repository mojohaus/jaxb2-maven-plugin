package org.codehaus.mojo.jaxb2.shared.filters.pattern;

import org.codehaus.mojo.jaxb2.shared.Validate;
import org.codehaus.mojo.jaxb2.shared.filters.AbstractFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * <p>AbstractFilter implementation containing a Java Pattern which should be used to determine if candidate T objects
 * match any of the supplied regularExpressions. Since Java regexp Patterns only match strings, a pluggable StringConverter is
 * used to convert T-type objects to strings for the actual matching.</p>
 * <p>The structure of setter methods is provided to enable simpler configuration using the default Maven/Plexus
 * dependency injection mechanism. The internal state of each AbstractPatternFilter is not intended to be changed
 * after its creation.</p>
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 * @since 2.0
 */
public abstract class AbstractPatternFilter<T> extends AbstractFilter<T> {

    // Internal state
    private boolean acceptCandidateOnPatternMatch;
    private StringConverter<T> converter;
    private List<Pattern> regularExpressions;
    private String patternPrefix;
    private List<String> patterns;

    /**
     * DI-friendly constructor.
     */
    public AbstractPatternFilter() {

        // Delegate
        super();

        // Assign internal state
        converter = new ToStringConverter<T>();
        acceptCandidateOnPatternMatch = true;
    }

    /**
     * Convenience constructor creating an AbstractPatternFilter that does not process null values
     * and which uses the ToStringConverter.
     *
     * @param regularExpressions The non-null list of Patters which should be applied within this AbstractPatternFilter.

    protected AbstractPatternFilter(final List<Pattern> regularExpressions, final boolean acceptCandidateOnPatternMatch) {
    this(false, regularExpressions, new ToStringConverter<T>(), acceptCandidateOnPatternMatch);
    }
     */

    /**
     * Compound constructor creating an AbstractPatternFilter that use the supplied Patterns and StringConverter to
     * matchAtLeastOnce or refuse T-type objects.
     *
     * @param processNullValues             if {@code true}, this AbstractPatternFilter process null candidate values.
     * @param regularExpressions                      The Patters used within this AbstractPatternFilter to accept or reject
     *                                      T-type candidates. A candidate is accepted by this AbstractPatternFilter
     *                                      if all supplied regularExpressions matchAtLeastOnce the candidate.
     * @param converter                     The StringConverter used to convert T-type objects to Strings which should
     *                                      be matched by all supplied Patterns to T-object candidates.
     * @param acceptCandidateOnPatternMatch if {@code true}, this AbstractPatternFilter will matchAtLeastOnce
     *                                      candidate objects that match at least one of the supplied regularExpressions.
     *                                      if {@code false}, this AbstractPatternFilter will noFilterMatches
     *                                      candidates that match at least one of the supplied regularExpressions.

    protected AbstractPatternFilter(final boolean processNullValues,
    final List<Pattern> regularExpressions,
    final StringConverter<T> converter,
    final boolean acceptCandidateOnPatternMatch) {
    super(processNullValues);

    // Check sanity
    Validate.notNull(converter, "converter");

    // Assign internal state
    this.converter = converter;
    this.regularExpressions = regularExpressions;
    this.acceptCandidateOnPatternMatch = acceptCandidateOnPatternMatch;
    }
     */

    /**
     * Assigns a prefix to be prepended to any patterns submitted to this AbstractPatternFilter.
     *
     * @param patternPrefix A prefix to be prepended to each pattern to render a Pattern.
     *                      If a null argument is supplied, nothing will be prepended.
     * @see #convert(java.util.List, String)
     */
    public final void setPatternPrefix(final String patternPrefix) {

        // Check sanity
        validateDiSetterCalledBeforeInitialization("patternPrefix");

        // Check sanity
        if (patternPrefix != null) {

            // Assign internal state
            this.patternPrefix = patternPrefix;
        } else {
            addDelayedLogMessage("warn", "Received null patternPrefix for configuring AbstractPatternFilter of type ["
                    + getClass().getName() + "]. Ignoring and proceeding.");
        }
    }

    /**
     * Collects a List containing {@code java.text.Pattern} objects by concatenating
     * {@code prepend + current_pattern_string} and Pattern-compiling the result.
     *
     * @param patterns The List of PatternStrings to compile.
     * @see #convert(java.util.List, String)
     */
    public void setPatterns(final List<String> patterns) {

        // Check sanity
        validateDiSetterCalledBeforeInitialization("patternPrefix");

        // Check sanity
        if (patterns != null) {

            // Assign internal state
            this.patterns = new ArrayList<String>();
            this.patterns.addAll(patterns);
        } else {
            addDelayedLogMessage("warn", "Received null patterns for configuring AbstractPatternFilter of type ["
                    + getClass().getName() + "]. Ignoring and proceeding.");
        }
    }

    /**
     * Assigns the {@code acceptCandidateOnPatternMatch} parameter which defines the function of this
     * AbstractPatternFilter's accept method.
     *
     * @param acceptCandidateOnPatternMatch if {@code true}, this AbstractPatternFilter will matchAtLeastOnce
     *                                      candidate objects that match at least one of the supplied
     *                                      regularExpressions. if {@code false}, this AbstractPatternFilter will
     *                                      noFilterMatches candidates that match at least one of the supplied
     *                                      regularExpressions.
     */
    public final void setAcceptCandidateOnPatternMatch(final boolean acceptCandidateOnPatternMatch) {

        // Check sanity
        validateDiSetterCalledBeforeInitialization("acceptCandidateOnPatternMatch");

        // Assign internal state
        this.acceptCandidateOnPatternMatch = acceptCandidateOnPatternMatch;
    }

    /**
     * Assigns the StringConverter used to convert T-type objects to Strings.
     * This StringConverter is used to acquire input comparison values for all Patterns to T-object candidates.
     *
     * @param converter The StringConverter used to convert T-type objects to Strings which should
     *                  be matched by all supplied Patterns to T-object candidates.
     */
    public void setConverter(final StringConverter<T> converter) {

        // Check sanity
        Validate.notNull(converter, "converter");
        validateDiSetterCalledBeforeInitialization("converter");

        // Assign internal state
        this.converter = converter;
    }

    /**
     * Compiles the List of Patterns used by this AbstractPatternFilter to match candidates.
     * If no patterns are supplied (by configuration or constructor call), no regularExpressions Pattern List will be
     * created for use by this AbstractPatternFilter. Instead, some logging is emitted onto the console.
     *
     * @see #patterns
     */
    @Override
    protected void onInitialize() {

        if (patterns == null && log.isWarnEnabled()) {

            // Log somewhat
            log.warn("No Patterns configured for AbstractPatternFilter [" + getClass().getName() + "]. "
                    + "This could imply a configuration problem.");

        } else {
            // Complete internal state
            regularExpressions = convert(patterns, patternPrefix);
        }
    }

    /**
     * <p>Each nonNullCandidate is matched against all Patterns supplied to this AbstractPatternFilter.
     * The match table of this AbstractPatternFilter on each candidate is as follows:</p>
     * <table>
     * <tr>
     * <th>at least 1 filter matches</th>
     * <th>acceptCandidateOnPatternMatch</th>
     * <th>result</th>
     * </tr>
     * <tr>
     * <td>true</td>
     * <td>true</td>
     * <td>true</td>
     * </tr>
     * <tr>
     * <td>false</td>
     * <td>true</td>
     * <td>false</td>
     * </tr>
     * <tr>
     * <td>true</td>
     * <td>false</td>
     * <td>false</td>
     * </tr>
     * <tr>
     * <td>false</td>
     * <td>false</td>
     * <td>true</td>
     * </tr>
     * </table>
     * {@inheritDoc}
     */
    @Override
    protected boolean onCandidate(final T nonNullCandidate) {

        final String candidateString = convert(nonNullCandidate);
        boolean atLeastOnePatternMatched = false;

        if (regularExpressions != null) {
            for (Pattern current : regularExpressions) {
                if (current.matcher(candidateString).matches()) {

                    if (log.isDebugEnabled()) {
                        log.debug("CandidateString [" + candidateString + "] matched pattern ["
                                + current.pattern() + "]");
                    }

                    // Adjust and return
                    atLeastOnePatternMatched = true;
                    break;
                }
            }
        }

        // Apply the reverse match logic if applicable
        return acceptCandidateOnPatternMatch ? atLeastOnePatternMatched : !atLeastOnePatternMatched;
    }

    /**
     * <p>Method implementation which converts a non-null T object to a String
     * to be matched by the Java Regexp Pattern contained within this AbstractPatternFilter implementation.
     * Override for a non-standard conversion.</p>
     *
     * @param nonNullT A non-null T object.
     * @return A string to be converted.
     */
    protected String convert(final T nonNullT) {
        return converter.convert(nonNullT);
    }

    /**
     * Collects a List containing {@code java.text.Pattern} objects by concatenating
     * {@code prepend + current_pattern_string} and Pattern-compiling the result.
     *
     * @param patternStrings The List of PatternStrings to compile.
     * @param prepend        A string to prepend each pattern. If a null argument is supplied, nothing
     *                       will be prepended.
     * @return a List containing {@code java.text.Pattern} objects by concatenating
     * {@code prepend + current_pattern_string} and Pattern-compiling the result.
     */
    public static List<Pattern> convert(final List<String> patternStrings, final String prepend) {

        // Check sanity
        List<String> effectivePatternStrings = patternStrings;
        if (patternStrings == null) {
            effectivePatternStrings = new ArrayList<String>();
            effectivePatternStrings.add(".*");
        }
        final String effectivePrepend = prepend == null ? "" : prepend;

        // Convert
        final List<Pattern> toReturn = new ArrayList<Pattern>();
        for (String current : effectivePatternStrings) {
            toReturn.add(Pattern.compile(effectivePrepend + current, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE));
        }

        // All done.
        return toReturn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {

        final StringBuilder builder = new StringBuilder(super.toString());
        builder.append(TOSTRING_INDENT + "Accept on match: [").append(acceptCandidateOnPatternMatch).append("]\n");

        final int numPatterns = regularExpressions != null && regularExpressions.size() > 0 ? regularExpressions.size() : 0;
        builder.append(TOSTRING_INDENT).append(numPatterns).append(" regularExpressions ...\n");

        if (numPatterns > 0) {
            for (int i = 0; i < regularExpressions.size(); i++) {
                final String prefix = TOSTRING_INDENT + " [" + i + "/" + regularExpressions.size() + "]: ";
                builder.append(prefix).append(regularExpressions.get(i).pattern()).append("\n");
            }
        }

        // All done.
        return builder.toString().substring(0, builder.length() - 1);
    }
}
