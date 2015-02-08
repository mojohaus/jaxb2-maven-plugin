package org.codehaus.mojo.jaxb2.shared.filters.pattern;

import org.apache.maven.plugin.logging.Log;
import org.codehaus.mojo.jaxb2.shared.FileSystemUtilities;
import org.codehaus.mojo.jaxb2.shared.Validate;
import org.codehaus.mojo.jaxb2.shared.filters.Filter;
import org.codehaus.mojo.jaxb2.shared.filters.Filters;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <p>AbstractPatternFilter and FileFilter combination, using a set of Regular expressions
 * to accept the canonical absolute paths to Files.</p>
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 * @since 2.0
 */
public class PatternFileFilter extends AbstractPatternFilter<File> implements FileFilter {

    /**
     * Java RegExp pattern which should be prepended to any file suffix pattern.
     * For example, a pattern identifying files ending in "txt", the pattern is
     * <code>FILE_SUFFIX_PATTERN_PREFIX + "txt"</code>.
     */
    public static final String FILE_SUFFIX_PATTERN_PREFIX = "(\\p{javaLetterOrDigit}|\\p{Punct})+";

    /**
     * Converter returning the canonical and absolute path for a File.
     */
    public static final StringConverter<File> FILE_PATH_CONVERTER = new StringConverter<File>() {
        @Override
        public String convert(final File toConvert) {
            return FileSystemUtilities.getCanonicalPath(toConvert.getAbsoluteFile());
        }
    };

    /**
     * Compound constructor creating an PatternFileFilter from the supplied parameters.
     *
     * @param processNullValues             if {@code true}, this PatternFileFilter process null candidate values.
     * @param patternPrefix                 a prefix to be prepended to any patterns submitted to
     *                                      this PatternFileFilter
     * @param patterns                      The non-null list of Patters which should be applied within this
     *                                      PatternFileFilter.
     * @param converter                     The StringConverter which converts Files to Strings for Pattern matching.
     * @param acceptCandidateOnPatternMatch if {@code true}, this PatternFileFilter will matchAtLeastOnce
     *                                      candidate objects that match at least one of the supplied patterns.
     *                                      if {@code false}, this PatternFileFilter will noFilterMatches
     *                                      candidates that match at least one of the supplied patterns.
     */
    public PatternFileFilter(final boolean processNullValues,
                             final String patternPrefix,
                             final List<String> patterns,
                             final StringConverter<File> converter,
                             final boolean acceptCandidateOnPatternMatch) {
        super();

        // Assign internal state
        setProcessNullValues(processNullValues);
        setAcceptCandidateOnPatternMatch(acceptCandidateOnPatternMatch);
        setPatternPrefix(patternPrefix);
        setPatterns(patterns);
        setConverter(converter);
    }

    /**
     * Creates a new PatternFileFilter using the supplied patternStrings which are interpreted as file suffixes.
     * (I.e. prepended with {@code FILE_SUFFIX_PATTERN_PREFIX} and compiled to Patterns).
     * The {@code FILE_PATH_CONVERTER} is used to convert Files to strings.
     * The supplied {@code acceptCandidateOnPatternMatch} parameter indicates if this
     * PatternFileFilter accepts or rejects candidates that match any of the supplied patternStrings.
     *
     * @param patternStrings                The list of patternStrings to be used as file path suffixes.
     * @param acceptCandidateOnPatternMatch if {@code true}, this PatternFileFilter will matchAtLeastOnce
     *                                      candidate objects that match at least one of the supplied patterns.
     *                                      if {@code false}, this PatternFileFilter will noFilterMatches
     *                                      candidates that match at least one of the supplied patterns.
     * @see #FILE_PATH_CONVERTER
     * @see #FILE_SUFFIX_PATTERN_PREFIX
     * @see #convert(java.util.List, String)
     */
    public PatternFileFilter(final List<String> patternStrings, final boolean acceptCandidateOnPatternMatch) {
        this(false, FILE_SUFFIX_PATTERN_PREFIX, patternStrings, FILE_PATH_CONVERTER, acceptCandidateOnPatternMatch);
    }

    /**
     * Creates a new PatternFileFilter using the supplied patternStrings which are interpreted as file suffixes.
     * (I.e. prepended with {@code FILE_SUFFIX_PATTERN_PREFIX} and compiled to Patterns).
     * The {@code FILE_PATH_CONVERTER} is used to convert Files to strings.
     * The retrieved PatternFileFilter accepts candidates that match any of the supplied patternStrings.
     *
     * @param patterns The list of patternStrings to be used as file path suffixes.
     */
    public PatternFileFilter(final List<String> patterns) {
        this(false, FILE_SUFFIX_PATTERN_PREFIX, patterns, FILE_PATH_CONVERTER, true);
    }

    /**
     * <p>Creates a new PatternFileFilter with no patternStrings List, implying that calling this constructor must be
     * followed by a call to the {@code #setPatterns} method.</p>
     * <p>The default prefix is {@code FILE_SUFFIX_PATTERN_PREFIX}, the default StringConverter is
     * {@code FILE_PATH_CONVERTER} and this PatternFileFilter does by default accept candidates that match any of
     * the supplied PatternStrings (i.e. an include-mode filter)</p>
     */
    public PatternFileFilter() {
        this(false, FILE_SUFFIX_PATTERN_PREFIX, new ArrayList<String>(), FILE_PATH_CONVERTER, true);
    }

    /**
     * Creates a new List containing an exclude-mode PatternFileFilter using the supplied patternStrings which
     * are interpreted as file suffixes. (I.e. prepended with {@code FILE_SUFFIX_PATTERN_PREFIX} and compiled to
     * Patterns). The {@code FILE_PATH_CONVERTER} is used to convert Files to strings.
     *
     * @param patterns A List of suffix patterns to be used in creating a new ExclusionRegularExpressionFileFilter.
     * @param log      The active Maven Log.
     * @return A List containing a PatternFileFilter using the supplied suffix patterns to match Files.
     * @see PatternFileFilter
     */
    public static List<Filter<File>> createExcludeFilterList(final Log log,
                                                             final String... patterns) {
        return createFilterList(log, false, patterns);
    }

    /**
     * Creates a new List containing an include-mode PatternFileFilter using the supplied patternStrings which
     * are interpreted as file suffixes. (I.e. prepended with {@code FILE_SUFFIX_PATTERN_PREFIX} and compiled to
     * Patterns). The {@code FILE_PATH_CONVERTER} is used to convert Files to strings.
     *
     * @param patterns A List of suffix patterns to be used in creating a new ExclusionRegularExpressionFileFilter.
     * @param log      The active Maven Log.
     * @return A List containing a PatternFileFilter using the supplied suffix patterns to match Files.
     * @see PatternFileFilter
     */
    public static List<Filter<File>> createIncludeFilterList(final Log log,
                                                             final String... patterns) {
        return createFilterList(log, true, patterns);
    }

    //
    // Private helpers
    //

    private static List<Filter<File>> createFilterList(final Log log,
                                                       final boolean includeOperation,
                                                       final String... patterns) {

        // Check sanity
        Validate.notNull(patterns, "patterns");
        Validate.notNull(log, "log");

        // Convert and return.
        final List<Filter<File>> toReturn = new ArrayList<Filter<File>>();
        final List<String> patternStrings = Arrays.asList(patterns);
        toReturn.add(new PatternFileFilter(patternStrings, includeOperation));

        // Initialize the filters.
        Filters.initialize(log, toReturn);
        return toReturn;
    }
}
