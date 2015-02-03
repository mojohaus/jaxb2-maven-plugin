package org.codehaus.mojo.jaxb2.shared.filters.pattern;

import java.net.URL;
import java.util.List;

/**
 * AbstractPatternFilter matching the string of URLs with to a set of Regular expressions.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 * @since 2.0
 */
public class PatternURLFilter extends AbstractPatternFilter<URL> {

    /**
     * Converter returning each URL's {@code toString()} form, after normalizing it, using the
     * algorithm {@code toConvert.toURI().normalize().toURL().toString();}
     */
    public static final StringConverter<URL> NORMALIZED_URL_CONVERTER = new StringConverter<URL>() {

        /**
         * {@inheritDoc}
         */
        @Override
        public String convert(final URL toConvert) {
            try {
                return toConvert.toURI().normalize().toURL().toString();
            } catch (Exception e) {

                // This should really not happen.
                throw new IllegalArgumentException("Could not handle URL [" + toConvert + "]", e);
            }
        }
    };

    /**
     * Creates a new ExclusionRegularExpressionURLFilter using the supplied patternStrings which are
     * matched against each full - normalized - URL. The {@code NORMALIZED_URL_CONVERTER} is used to convert
     * URLs to strings.
     *
     * @param patternStrings The list of patternStrings to be used as regular expression matchers against the
     *                       normalized URLs.
     * @see #NORMALIZED_URL_CONVERTER
     * @see #convert(java.util.List, String)
     */
    public PatternURLFilter(final List<String> patternStrings) {
        this(false, "", patternStrings, NORMALIZED_URL_CONVERTER, true);
    }

    /**
     * Compound constructor creating an ExclusionRegularExpressionURLFilter from the supplied parameters.
     *
     * @param processNullValues             if {@code true}, this ExclusionRegularExpressionURLFilter process null
     *                                      candidate values.
     * @param patterns                      The non-null list of Patters which should be applied within this
     *                                      ExclusionRegularExpressionURLFilter. A candidate of type T should only
     *                                      be accepted by this ExclusionRegularExpressionURLFilter if all supplied
     *                                      patterns matchAtLeastOnce the candidate.
     * @param converter                     The StringConverter used to convert T-type objects to Strings which should
     *                                      be matched by all supplied Patterns to T-object candidates.
     * @param acceptCandidateOnPatternMatch if {@code true}, this ExclusionRegularExpressionURLFilter will matchAtLeastOnce
     *                                      candidate objects that match at least one of the supplied patterns. if
     *                                      {@code false}, this ExclusionRegularExpressionURLFilter will noFilterMatches
     *                                      candidates that match at least one of the supplied patterns.
     */
    public PatternURLFilter(final boolean processNullValues,
                            final String patternPrefix,
                            final List<String> patterns,
                            final StringConverter<URL> converter,
                            final boolean acceptCandidateOnPatternMatch) {
        super();

        // Assign internal state
        setProcessNullValues(processNullValues);
        setAcceptCandidateOnPatternMatch(acceptCandidateOnPatternMatch);
        setPatternPrefix(patternPrefix);
        setPatterns(patterns);
        setConverter(converter);
    }
}
