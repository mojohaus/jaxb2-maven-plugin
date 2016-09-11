package org.codehaus.mojo.jaxb2.shared.filters.pattern;

import org.codehaus.mojo.jaxb2.shared.FileSystemUtilities;
import org.junit.Assert;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class PatternURLFilterTest extends AbstractPatternFilterTest {

    // Shared state
    private URL[] urlList;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onSetup() {

        urlList = new URL[fileList.length];

        for (int i = 0; i < fileList.length; i++) {
            Assert.assertTrue(FileSystemUtilities.EXISTING_FILE.accept(fileList[i]));
            try {
                urlList[i] = fileList[i].toURI().normalize().toURL();
            } catch (MalformedURLException e) {
                throw new IllegalStateException("Could not populate urlList", e);
            }
        }
    }

    @Test(expected = IllegalStateException.class)
    public void validateExceptionOnNotInitializingFilterBeforeUse() {

        // Assemble
        final PatternURLFilter unitUnderTest = new PatternURLFilter(null);

        // Act & Assert
        unitUnderTest.accept(urlList[0]);
    }

    @Test
    public void validateAcceptNothingOnNullPatterns() {

        // Assemble
        final PatternURLFilter unitUnderTest = new PatternURLFilter(null);

        // Act
        unitUnderTest.initialize(log);
        final Map<String, Boolean> result = applyFilterAndRetrieveResults(unitUnderTest,
                urlList,
                PatternURLFilter.NORMALIZED_URL_CONVERTER);

        // Assert
        for (Map.Entry<String, Boolean> current : result.entrySet()) {
            Assert.assertFalse(current.getValue());
        }
    }

    @Test
    public void validateExcludingMatchingURLs() {

        // Assemble
        final String urlProtocol = "file";
        final List<String> txtFileSuffixExclusion = Arrays.asList(urlProtocol + ":/"
                + PatternFileFilter.PATTERN_LETTER_DIGIT_PUNCT + "\\.txt");
        final PatternURLFilter unitUnderTest = new PatternURLFilter(txtFileSuffixExclusion);
        unitUnderTest.setAcceptCandidateOnPatternMatch(false);
        unitUnderTest.initialize(log);

        // Act
        final Map<String, Boolean> result = applyFilterAndRetrieveResults(
                unitUnderTest,
                urlList,
                PatternURLFilter.NORMALIZED_URL_CONVERTER);

        // Assert
        for (Map.Entry<String, Boolean> current : result.entrySet()) {
            if (current.getKey().endsWith("xml")) {
                Assert.assertTrue(result.get(current.getKey()));
            } else {
                Assert.assertFalse(result.get(current.getKey()));
            }
        }
    }
}
